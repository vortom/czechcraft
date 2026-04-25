#!/usr/bin/env python3
"""
CzechCraft asset validator.

Enforces the validator-checked rules from docs/design/asset-handbook.md:
- File name + path conventions
- Exact pixel dimensions per asset type
- Distinct color count per asset type
- Binary alpha rule for item/block textures
- Soft file-size limit

Uses only Python stdlib (zlib, struct) — no Pillow dependency.

Usage:
    python3 scripts/check-asset.py <path-to-png>
    python3 scripts/check-asset.py --all      # checks every CzechCraft asset
    python3 scripts/check-asset.py --help

Exit code: 0 = all checks pass, 1 = any failure.
"""

from __future__ import annotations

import argparse
import os
import re
import struct
import sys
import zlib
from dataclasses import dataclass
from pathlib import Path
from typing import Iterable

REPO_ROOT = Path(__file__).resolve().parent.parent
ASSETS_ROOT = REPO_ROOT / "common" / "src" / "main" / "resources" / "assets" / "czechcraft"


@dataclass(frozen=True)
class AssetSpec:
    name: str
    width: int
    height: int
    max_colors: int
    binary_alpha: bool
    max_file_bytes: int


ITEM_TEXTURE = AssetSpec(
    name="item texture", width=16, height=16,
    max_colors=8, binary_alpha=True, max_file_bytes=2_048,
)
BLOCK_TEXTURE = AssetSpec(
    name="block texture", width=16, height=16,
    max_colors=8, binary_alpha=True, max_file_bytes=2_048,
)
MOD_ICON = AssetSpec(
    name="mod icon", width=128, height=128,
    max_colors=64, binary_alpha=False, max_file_bytes=51_200,
)


def classify(path: Path) -> AssetSpec | None:
    """Identify which asset spec applies based on path. Returns None for unknown."""
    p = path.as_posix()
    if "/textures/item/" in p and p.endswith(".png"):
        return ITEM_TEXTURE
    if "/textures/block/" in p and p.endswith(".png"):
        return BLOCK_TEXTURE
    if p.endswith("/assets/czechcraft/icon.png"):
        return MOD_ICON
    return None


_FILENAME_RX = re.compile(r"^[a-z0-9][a-z0-9-]*\.png$")


def check_filename(path: Path) -> list[str]:
    name = path.name
    if not _FILENAME_RX.match(name):
        return [
            f"filename {name!r} must be lowercase ASCII, hyphens not underscores, "
            f"and end with .png"
        ]
    return []


@dataclass
class PngInfo:
    width: int
    height: int
    bit_depth: int
    color_type: int
    palette: list[tuple[int, int, int]] | None
    trns: bytes | None  # palette alpha lookup, or None
    raw_pixels: bytes  # decompressed IDAT, with row filter bytes still present


def parse_png(data: bytes) -> PngInfo:
    if data[:8] != b"\x89PNG\r\n\x1a\n":
        raise ValueError("not a PNG file")

    width = height = bit_depth = color_type = 0
    palette: list[tuple[int, int, int]] | None = None
    trns: bytes | None = None
    idat = bytearray()

    i = 8
    while i < len(data):
        length = struct.unpack(">I", data[i:i + 4])[0]
        ctype = data[i + 4:i + 8].decode("ascii", errors="replace")
        chunk_data = data[i + 8:i + 8 + length]
        if ctype == "IHDR":
            width, height, bit_depth, color_type = struct.unpack(">IIBB", chunk_data[:10])
        elif ctype == "PLTE":
            palette = [
                (chunk_data[k], chunk_data[k + 1], chunk_data[k + 2])
                for k in range(0, length, 3)
            ]
        elif ctype == "tRNS":
            trns = bytes(chunk_data)
        elif ctype == "IDAT":
            idat.extend(chunk_data)
        elif ctype == "IEND":
            break
        i += 12 + length

    raw = zlib.decompress(bytes(idat))
    return PngInfo(width, height, bit_depth, color_type, palette, trns, raw)


def _bytes_per_pixel(color_type: int, bit_depth: int) -> int:
    """How many bytes of raw image data each pixel occupies."""
    samples = {0: 1, 2: 3, 3: 1, 4: 2, 6: 4}[color_type]
    return max(1, samples * bit_depth // 8)


def _paeth(a: int, b: int, c: int) -> int:
    p = a + b - c
    pa, pb, pc = abs(p - a), abs(p - b), abs(p - c)
    if pa <= pb and pa <= pc:
        return a
    if pb <= pc:
        return b
    return c


def unfilter_rows(raw: bytes, width: int, height: int, bpp: int) -> bytes:
    """Reverse PNG row filtering. Returns a flat bytes buffer of width*height*bpp bytes."""
    stride = width * bpp
    out = bytearray()
    prev = bytes(stride)
    pos = 0
    for _y in range(height):
        ftype = raw[pos]
        row = bytearray(raw[pos + 1:pos + 1 + stride])
        pos += 1 + stride
        if ftype == 0:
            pass
        elif ftype == 1:  # Sub
            for x in range(stride):
                left = row[x - bpp] if x >= bpp else 0
                row[x] = (row[x] + left) & 0xFF
        elif ftype == 2:  # Up
            for x in range(stride):
                row[x] = (row[x] + prev[x]) & 0xFF
        elif ftype == 3:  # Average
            for x in range(stride):
                left = row[x - bpp] if x >= bpp else 0
                up = prev[x]
                row[x] = (row[x] + (left + up) // 2) & 0xFF
        elif ftype == 4:  # Paeth
            for x in range(stride):
                left = row[x - bpp] if x >= bpp else 0
                up = prev[x]
                upper_left = prev[x - bpp] if x >= bpp else 0
                row[x] = (row[x] + _paeth(left, up, upper_left)) & 0xFF
        else:
            raise ValueError(f"unknown row filter {ftype}")
        out.extend(row)
        prev = bytes(row)
    return bytes(out)


def pixel_iter(info: PngInfo) -> Iterable[tuple[int, int, int, int]]:
    """Yield (R, G, B, A) for every pixel, regardless of underlying color type."""
    if info.bit_depth != 8:
        raise NotImplementedError(
            f"only 8-bit PNGs supported (got bit_depth={info.bit_depth})"
        )
    bpp = _bytes_per_pixel(info.color_type, info.bit_depth)
    flat = unfilter_rows(info.raw_pixels, info.width, info.height, bpp)
    n = info.width * info.height

    if info.color_type == 6:  # RGBA
        for k in range(n):
            r, g, b, a = flat[4 * k:4 * k + 4]
            yield r, g, b, a
    elif info.color_type == 2:  # RGB
        for k in range(n):
            r, g, b = flat[3 * k:3 * k + 3]
            yield r, g, b, 255
    elif info.color_type == 3:  # palette
        if info.palette is None:
            raise ValueError("palette PNG missing PLTE")
        trns = info.trns or b""
        for k in range(n):
            idx = flat[k]
            r, g, b = info.palette[idx]
            a = trns[idx] if idx < len(trns) else 255
            yield r, g, b, a
    elif info.color_type == 0:  # gray
        for k in range(n):
            v = flat[k]
            yield v, v, v, 255
    elif info.color_type == 4:  # gray + alpha
        for k in range(n):
            v, a = flat[2 * k:2 * k + 2]
            yield v, v, v, a
    else:
        raise ValueError(f"unsupported color_type {info.color_type}")


def check_asset(path: Path, spec: AssetSpec) -> list[str]:
    failures: list[str] = []

    failures.extend(check_filename(path))

    file_size = path.stat().st_size
    if file_size > spec.max_file_bytes:
        failures.append(
            f"file size {file_size} bytes exceeds soft limit {spec.max_file_bytes} for {spec.name} "
            f"(consider re-exporting at lower compression or simplifying)"
        )

    with path.open("rb") as f:
        data = f.read()
    try:
        info = parse_png(data)
    except Exception as e:
        failures.append(f"PNG parse error: {e}")
        return failures

    if (info.width, info.height) != (spec.width, spec.height):
        failures.append(
            f"dimensions {info.width}×{info.height} != required {spec.width}×{spec.height} for {spec.name}"
        )

    try:
        pixels = list(pixel_iter(info))
    except Exception as e:
        failures.append(f"pixel decode error: {e}")
        return failures

    distinct = len({(r, g, b, a) for (r, g, b, a) in pixels})
    if distinct > spec.max_colors:
        failures.append(
            f"distinct colors {distinct} > max {spec.max_colors} for {spec.name} "
            f"(typical cause: down-scaling a high-res illustration; redraw on the {spec.width}×{spec.height} grid)"
        )

    if spec.binary_alpha:
        partial = sum(1 for (_r, _g, _b, a) in pixels if a not in (0, 255))
        if partial > 0:
            failures.append(
                f"{partial} pixel(s) have semi-transparent alpha; {spec.name} requires binary alpha "
                f"(every pixel must be alpha=0 or alpha=255)"
            )

    return failures


def find_all_assets() -> list[tuple[Path, AssetSpec]]:
    """Find every CzechCraft asset under common/.../assets/czechcraft/ that has a known spec."""
    found: list[tuple[Path, AssetSpec]] = []
    if not ASSETS_ROOT.exists():
        return found
    for path in ASSETS_ROOT.rglob("*.png"):
        spec = classify(path)
        if spec is not None:
            found.append((path, spec))
    return found


def main() -> int:
    p = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    g = p.add_mutually_exclusive_group(required=True)
    g.add_argument("path", nargs="?", help="path to a PNG asset")
    g.add_argument("--all", action="store_true", help="check every CzechCraft asset in the repo")
    args = p.parse_args()

    targets: list[tuple[Path, AssetSpec]]
    if args.all:
        targets = find_all_assets()
        if not targets:
            print("No CzechCraft assets found under " + str(ASSETS_ROOT), file=sys.stderr)
            return 1
    else:
        path = Path(args.path).resolve()
        if not path.is_file():
            print(f"Not a file: {path}", file=sys.stderr)
            return 1
        spec = classify(path)
        if spec is None:
            print(
                f"Cannot classify {path} — must be under "
                "common/.../assets/czechcraft/textures/{item,block}/ or be assets/czechcraft/icon.png",
                file=sys.stderr,
            )
            return 1
        targets = [(path, spec)]

    total_failures = 0
    for path, spec in targets:
        rel = path.relative_to(REPO_ROOT) if path.is_relative_to(REPO_ROOT) else path
        failures = check_asset(path, spec)
        if failures:
            total_failures += len(failures)
            print(f"FAIL {rel}  ({spec.name})")
            for f in failures:
                print(f"   - {f}")
        else:
            print(f"OK   {rel}  ({spec.name})")

    if total_failures:
        print(f"\n{total_failures} failure(s)", file=sys.stderr)
        return 1
    print("\nAll assets pass handbook rules.")
    return 0


if __name__ == "__main__":
    sys.exit(main())

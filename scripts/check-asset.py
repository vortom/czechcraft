#!/usr/bin/env python3
"""
CzechCraft asset validator.

Enforces the validator-checked rules from docs/design/asset-handbook.md §4:
- Filename convention (lowercase ASCII alphanumerics + underscores, ends with .png)
- File path under the correct directory
- Exact pixel dimensions per asset type
- Valid PNG format
- File-size soft limit per asset type

Aesthetic rules (color count, binary alpha, style) are NOT enforced here under
the current illustration-style direction (handbook §3) — those are reviewer
judgement calls.

Uses only Python stdlib (zlib, struct) — no Pillow dependency.

Usage:
    python3 scripts/check-asset.py <path-to-png>
    python3 scripts/check-asset.py --all      # checks every CzechCraft asset
    python3 scripts/check-asset.py --help

Exit code: 0 = all checks pass, 1 = any failure.
"""

from __future__ import annotations

import argparse
import re
import struct
import sys
from dataclasses import dataclass
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parent.parent
ASSETS_ROOT = REPO_ROOT / "common" / "src" / "main" / "resources" / "assets" / "czechcraft"


@dataclass(frozen=True)
class AssetSpec:
    name: str
    width: int
    height: int
    max_file_bytes: int


ITEM_TEXTURE = AssetSpec(name="item texture", width=16, height=16, max_file_bytes=5_120)
BLOCK_TEXTURE = AssetSpec(name="block texture", width=16, height=16, max_file_bytes=5_120)
MOD_ICON = AssetSpec(name="mod icon", width=128, height=128, max_file_bytes=102_400)


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


# Minecraft identifier convention: lowercase ASCII alphanumerics + underscores only.
_FILENAME_RX = re.compile(r"^[a-z0-9][a-z0-9_]*\.png$")


def check_filename(path: Path) -> list[str]:
    name = path.name
    if not _FILENAME_RX.match(name):
        return [
            f"filename {name!r} must be lowercase ASCII alphanumerics + underscores "
            f"(Minecraft convention — see vanilla iron_ingot.png) and end with .png"
        ]
    return []


def parse_png_header(data: bytes) -> tuple[int, int]:
    """Return (width, height) of a PNG. Raises ValueError on parse failure."""
    if data[:8] != b"\x89PNG\r\n\x1a\n":
        raise ValueError("not a PNG file")

    i = 8
    while i < len(data):
        length = struct.unpack(">I", data[i:i + 4])[0]
        ctype = data[i + 4:i + 8].decode("ascii", errors="replace")
        if ctype == "IHDR":
            w, h = struct.unpack(">II", data[i + 8:i + 16])
            return w, h
        i += 12 + length

    raise ValueError("PNG missing IHDR chunk")


def check_asset(path: Path, spec: AssetSpec) -> list[str]:
    failures: list[str] = []

    failures.extend(check_filename(path))

    file_size = path.stat().st_size
    if file_size > spec.max_file_bytes:
        failures.append(
            f"file size {file_size} bytes exceeds soft limit {spec.max_file_bytes} for {spec.name} "
            f"(consider re-exporting at higher PNG compression or simplifying)"
        )

    with path.open("rb") as f:
        data = f.read()
    try:
        w, h = parse_png_header(data)
    except Exception as e:
        failures.append(f"PNG parse error: {e}")
        return failures

    if (w, h) != (spec.width, spec.height):
        failures.append(
            f"dimensions {w}×{h} != required {spec.width}×{spec.height} for {spec.name}"
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
    p = argparse.ArgumentParser(
        description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter
    )
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

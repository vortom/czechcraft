# CzechCraft v1 — Artwork Brief

**For:** the designer producing the v1 artwork.
**Status:** Optional polish — v1 ships with placeholder PNGs; this brief defines the replacement assets.
**Date:** 2026-04-25

---

## 1. About the project

**CzechCraft** is a small Minecraft mod (Java Edition, Fabric loader, 1.21.1) that adds Czech-themed food and drinks to the game. v1 introduces a single item — **Rohlík**, a traditional Czech bread roll — crafted by placing two wheat side-by-side. Future versions will add more Czech food and drinks (e.g. *Pivo* — Czech beer).

The mod is published on [Modrinth](https://modrinth.com/mod/czechcraft) and is open-source under MIT (https://github.com/vortom/czechcraft). Tone: light, friendly, faithful to Minecraft's vanilla aesthetic — not realistic, not cartoony.

---

## 2. What we need

Two PNG files. Both will be dropped into the repo as drop-in replacements (no code changes).

### 2.1 In-game item texture — Rohlík

| Field | Value |
|---|---|
| **File** | `rohlik.png` |
| **Repo path** | `common/src/main/resources/assets/czechcraft/textures/item/rohlik.png` |
| **Dimensions** | **16 × 16 pixels** (mandatory — Minecraft's vanilla item texture size) |
| **Format** | PNG, RGBA (transparency required) |
| **Color mode** | sRGB, 8-bit per channel |
| **Compression** | Standard PNG (no need to optimise; file ends up < 1 KB) |

**Subject:** A single Czech rohlík — an elongated, slightly curved (crescent-shaped) golden-brown bread roll, dusted with poppy seeds or salt across the top crust. See [reference image](https://en.wikipedia.org/wiki/Rohl%C3%ADk_(bread_roll)) for the real-world shape.

**Style constraints (must follow Minecraft's vanilla item texture conventions):**
- **Pixel art.** No anti-aliasing inside the shape. Sharp pixel edges everywhere.
- **Limited palette.** Aim for ~4–6 distinct colors, all warm tones (cream, golden tan, light brown, dark brown crust, optional poppy-seed black/dark grey speckles).
- **Diagonal viewing angle.** Vanilla item icons read as if seen from a 30°-ish bird's-eye angle, not pure side-on. Think how vanilla bread, cookie, or apple is drawn.
- **Edge highlight + shadow.** One pixel of lighter shade on the upper-left contour, one pixel of darker shade on the lower-right. This is how vanilla items get visual depth at 16px.
- **Background fully transparent** (alpha = 0). Do not paint a frame, drop shadow, or background.
- **Centered**, with at least 1 pixel of transparent margin on each side (so it doesn't crowd the item slot).

**Visual references** (vanilla items shipped with Minecraft, all in the same style we want):
- Vanilla `bread.png` (golden loaf) — closest match for material/color
- Vanilla `cookie.png` — similar size in slot, similar palette weight
- Vanilla `wheat.png` — for color reference (since we're crafting Rohlík from wheat)

If you want the canonical vanilla pixel-art guide: [Minecraft Wiki — Texture#Item textures](https://minecraft.wiki/w/Texture#Item_textures).

### 2.2 Mod icon — CzechCraft

| Field | Value |
|---|---|
| **File** | `icon.png` |
| **Repo path** | `common/src/main/resources/assets/czechcraft/icon.png` |
| **Dimensions** | **128 × 128 pixels** (mandatory — Modrinth's icon recommendation, also used inside `fabric.mod.json`) |
| **Format** | PNG, RGB or RGBA |
| **Color mode** | sRGB, 8-bit per channel |

**Subject:** The CzechCraft brand mark. Two creative directions, pick whichever fits your style:
- **Option A (recommended):** A scaled-up, lovingly-rendered Rohlík on a dark contrasting background (e.g. deep red `#a52a2a` or chestnut brown `#3e2a1a`). Same pixel-art style as the in-game texture, just larger and a bit more detailed.
- **Option B:** The text "CzechCraft" set above or below a small Rohlík icon, with a subtle Czech-flag accent (white over red — colors `#ffffff` and `#d7141a`). Keep typography minimal — this needs to read well as a thumbnail at 64px.

**Style constraints:**
- This icon appears in Modrinth listings, the Modrinth App's "installed mods" grid, and the in-game mods menu. It must read well at thumbnail sizes (down to 32×32 or 48×48).
- **No fine details.** A single recognisable shape + clear silhouette beats clever-but-fussy.
- Avoid full-bleed text smaller than ~16px tall.
- Avoid anything Minecraft Mojang might object to — no Mojang/Minecraft logo, no Creeper face, no copyrighted IP.

---

## 3. Naming, delivery, handover

- File names exactly as listed above (lowercase, exact spelling).
- Deliver as two `.png` files (or a single zip containing them). No source files (PSD/AI/Aseprite) needed for v1, but they're welcome if you'd like to include them — we can keep them out of the public repo and store separately.
- Hand them back to the maintainer, and we'll do the drop-in replacement and a release commit. No code changes will be needed on your side.

---

## 4. Acceptance checklist

The replacement art is accepted when **all** of:
- ✅ Both files have the exact filenames and dimensions above.
- ✅ The 16×16 Rohlík reads clearly as a bread roll at 1:1 scale (no zoom).
- ✅ The 128×128 icon reads clearly at 32×32 thumbnail scale.
- ✅ Both have correct transparency (no white halos around the shape).
- ✅ Visual style is compatible with vanilla Minecraft assets (a casual player viewing a creative tab containing CzechCraft items + vanilla items shouldn't feel jarring contrast).

---

## 5. License & credit

Anything you produce will be incorporated into the mod under **MIT license** (your work becomes part of the open-source mod). You'll be credited in `CHANGELOG.md` and on the Modrinth project page (e.g., "Artwork by &lt;your name&gt;"). If you'd prefer a different credit format or no credit, let me know.

---

## 6. Out of scope (for v1)

- Animated textures (the GIF-like sprite-sheet approach for items like clocks/compasses)
- Block textures (no blocks in v1)
- Banner/promotional art for the Modrinth project header (we use the icon as-is for now)
- Texture variants (single Rohlík texture only — no enchant-glint or damage tiers)

These will be revisited in v2+ as the content grows. If your style works well, we'd love to bring you back for the Pivo (Czech beer) artwork in v2.

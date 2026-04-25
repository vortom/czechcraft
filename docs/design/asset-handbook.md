# CzechCraft Asset Design Handbook

**Audience:** anyone producing visual assets (textures, icons, banners) for CzechCraft.
**Status:** binding — every new asset MUST conform. v1 ships with assets that predate this handbook; #3 tracks bringing them into compliance.
**Last updated:** 2026-04-25

If you read nothing else, read [§4 Pixel-art rules](#4-pixel-art-rules) and [§7 Acceptance checklist](#7-acceptance-checklist). Run `python3 scripts/check-asset.py <file>` before submitting.

---

## 1. Brand & tone

CzechCraft adds Czech-themed food and drinks (rohlík, pivo, koláče, …) to Minecraft. The visual identity is:

- **Warm and inviting** — the food is comforting, slightly nostalgic. Bakery, not banquet.
- **Vanilla-compatible** — assets sit next to vanilla items (bread, wheat, cookie) and must feel like they belong in the same world. **No "HD" textures, no smooth-shaded illustrations, no realism.**
- **Czech without kitsch** — subtle Czech-flag accent (`#ffffff` over `#d7141a`) appears on the brand chrome (mod icon, optional gallery banners). Never plastered onto items themselves.
- **Affectionate, not slick** — small, slightly imperfect, characterful. We are not a corporate brand.

### 1.1 Brand color palette

Use these as anchors. You don't need to use all of them in any single asset, but every asset's palette should be drawn from this family or a neutral extension of it.

| Role | Hex | Use |
|---|---|---|
| Bread crumb (light) | `#e8c98a` | Highlights on rolls, cookies |
| Bread crust (mid) | `#d4a574` | Body of baked items |
| Bread crust (dark) | `#a67839` | Shadow on baked items, scoring lines |
| Burnt edge | `#5a3a18` | Deep contour shadow |
| Poppy / dark seed | `#1a1208` | Speckles on rohlík, beer head accents |
| Mod chrome (background) | `#3e2a1a` | Dark backgrounds for icons, banners |
| Czech flag white | `#ffffff` | Accent only |
| Czech flag red | `#d7141a` | Accent only |
| Beer body (warm) | `#f0c060` | (v2 Pivo) |
| Beer head (foam) | `#f8efd8` | (v2 Pivo) |

### 1.2 What to avoid

- Photographic textures, gradients, blur, anti-aliasing inside the silhouette.
- Drop shadows or glow effects on item textures.
- Logos / wordmarks on items.
- IP that isn't ours: Mojang/Minecraft logos, Creeper face, anything copyrighted.

---

## 2. Asset types and dimensions (mandatory)

Every CzechCraft asset has exactly one correct size. Wrong size = won't load or will look wrong in-game.

| Asset type | Dimensions | Format | Color mode | Alpha rule | Max distinct colors |
|---|---|---|---|---|---|
| **Item texture** | **16 × 16 px** | PNG, no interlace | sRGB, 8-bit | Binary (0 or 255 only) | **8** |
| **Block texture** | 16 × 16 px (per face) | PNG | sRGB, 8-bit | Binary | 8 |
| **GUI element** | 16 × 16 or 32 × 32 (case-by-case) | PNG | sRGB | Binary | 12 |
| **Mod icon** | **128 × 128 px**, square | PNG | sRGB, 8-bit | Free (alpha optional) | 64 |
| **Modrinth gallery image** | 1280 × 720 px (16:9) | PNG or JPG | sRGB | n/a | unlimited |
| **Modrinth feature banner** | 800 × 300 px | PNG or JPG | sRGB | n/a | unlimited |

**File-size soft limit:** item textures < 2 KB, mod icon < 50 KB, gallery images < 500 KB. If you're far over, it usually means the source is unnecessarily complex.

---

## 3. File names and repo paths

Filenames are lowercase, ASCII only, hyphens not underscores between words (matching Minecraft resource conventions where applicable). The path determines where Minecraft loads the asset from at runtime — do not deviate.

| Asset | Path |
|---|---|
| Item texture for `czechcraft:<id>` | `common/src/main/resources/assets/czechcraft/textures/item/<id>.png` |
| Block texture for `czechcraft:<id>` | `common/src/main/resources/assets/czechcraft/textures/block/<id>.png` |
| Mod icon (CzechCraft brand mark) | `common/src/main/resources/assets/czechcraft/icon.png` |
| Modrinth gallery / banner | NOT in the repo — uploaded directly to Modrinth dashboard |

**Identifier consistency:** the `<id>` part of the filename must match `FoodItems.<NAME>_PATH` (or the equivalent constant in `Drinks` etc.) **exactly** — same lowercase string. The recipe, model, lang key, and texture are all derived from this single identifier.

---

## 4. Pixel-art rules

These are the rules that make CzechCraft assets feel native to Minecraft.

### 4.1 Draw on the target grid, never down-scale

Open your editor at the **final pixel dimensions** (e.g., 16×16) and paint pixel by pixel. Do not draw a 512×512 illustration and shrink it — that produces ~125-color anti-aliased mush, not pixel-art (this is exactly the failure mode the v1 placeholder Rohlík exhibited).

Recommended tools: **Aseprite** (paid), **Piskel** (free, browser), **GraphicsGale** (free), **Photoshop** with "Nearest Neighbor" interpolation and pixel-grid view enabled. Avoid plain Photoshop with default brush settings — it'll antialias by default.

### 4.2 Limit your palette

| Asset | Max distinct colors (incl. transparent) |
|---|---|
| Item / block texture | 8 |
| Mod icon | 64 |
| GUI element | 12 |

If you can't depict your subject in the budget, pick a different subject or simplify. Vanilla `bread.png` uses 5 colors and reads as bread instantly.

### 4.3 Binary transparency (item & block textures)

Every pixel in an item or block texture is **either fully opaque (alpha = 255) or fully transparent (alpha = 0)**. No values in between. No anti-aliased edges. The silhouette must be hard.

The mod icon may use partial alpha (it's not subject to the binary rule — it's a UI thumbnail, not an in-game sprite).

### 4.4 Viewing angle (item textures)

Vanilla item icons are drawn as if seen from a slight bird's-eye angle (roughly 30°), not pure side-on, not pure top-down. The shape is foreshortened: e.g., a rohlík's two ends look closer to the viewer than its middle. Compare vanilla `bread.png`, `cookie.png`, `apple.png` for the convention.

### 4.5 Edge contour

For shape definition at 16×16, use:
- **One pixel of light shade** along the upper-left contour (approximating sunlight).
- **One pixel of mid-tone shadow** along the lower-right contour.
- **No outline** of pure black around the entire silhouette (this looks cartoonish, unlike vanilla).

### 4.6 Centering and margin

Item textures should sit roughly centered in the 16×16 frame, with **at least 1 pixel of transparent margin** on every side so the sprite doesn't crowd the inventory slot's visual edge. Diagonal items (like rohlík or a sword) can extend closer to the corners but still leave a 1-pixel buffer at the bounding box.

### 4.7 No animation in v1+

Animated item textures (compass, clock, prismarine block) require a sprite sheet plus an `.mcmeta` file and are out of scope until we have a clear use case. Don't draw frames hoping it'll animate.

---

## 5. Mod icon-specific guidance (128 × 128)

Different rules apply because the icon isn't an in-game sprite — it's a thumbnail in mod browsers.

- It MUST read at 32×32 (the smallest size Modrinth and the in-game mods menu render it at). View your work at 32×32 zoom-out before shipping. If letters smear or details vanish, simplify.
- Pixel art preferred but not strictly required. If the icon uses a vector or hand-illustration style, keep edges crisp (no Gaussian blur, no soft shadow).
- Use the dark-background frame from the v1 icon as a reference: `#3e2a1a` background, thin ornate border, optional Czech-flag accent below the wordmark.
- Wordmark "CzechCraft" if used: chunky pixel font (vanilla-Minecraft-default-font style) survives 32×32. Ornate serifs do not.
- No transparent mod icon (Modrinth tolerates it but rendering varies; a solid background is safer).

---

## 6. Designer workflow

1. **Read this handbook end to end.**
2. Open the relevant existing asset in the repo for visual context (e.g., `common/src/main/resources/assets/czechcraft/textures/item/rohlik.png` is your color-palette neighbor).
3. Pick a tool (Aseprite or Piskel, see §4.1). Open at the target size.
4. Paint within the palette and pixel-grid rules.
5. Export PNG. For item/block textures, ensure binary alpha (most pixel-art tools enforce this; verify in step 7).
6. Run the validator: `python3 scripts/check-asset.py <path>` (see §8). Fix any failures.
7. Drop the file into the repo at the path defined in §3.
8. **Test in-game** for item/block textures: `./gradlew :fabric:runClient`, find the item in the creative tab, check that the sprite renders cleanly at 1× and at zoom (hold Shift in inventory).
9. Commit with a `feat(art): ...` or `fix(art): ...` message. Don't bundle gameplay changes with art changes — keep them separate so reverts are surgical.
10. Open a PR. Include before/after screenshots if replacing an existing asset.

---

## 7. Acceptance checklist

A reviewer should run through these before merging any art change:

**Mandatory (validator-checked):**
- [ ] Filename matches `<id>.png`, lowercase, ASCII only, hyphens not underscores.
- [ ] File is at the correct repo path per §3.
- [ ] Dimensions match §2 exactly.
- [ ] Format is PNG.
- [ ] For item/block textures: distinct color count ≤ 8.
- [ ] For item/block textures: every pixel has alpha = 0 or alpha = 255 (no semi-transparent).
- [ ] For mod icon: distinct color count ≤ 64.
- [ ] File size within soft limit per §2.

**Mandatory (human-judged):**
- [ ] Palette is from §1.1 brand family or a justified neutral extension.
- [ ] No drop shadow / glow / gradient / blur inside the silhouette.
- [ ] No copyrighted IP, no Mojang/Minecraft assets, no Creeper face.
- [ ] Reads clearly at 1× and 32×32 (for icons).
- [ ] Doesn't visually clash with adjacent vanilla items in a creative tab (subjective but important).

**Recommended:**
- [ ] Source file (Aseprite `.aseprite`, Piskel `.piskel`, or layered PSD) shared with the project — kept separately, not committed to the public repo, so future revisions are easy.

---

## 8. Automated validator

`scripts/check-asset.py` enforces the validator-checked items above. It uses only Python stdlib (no Pillow dependency required).

Usage:

```bash
# check a single file
python3 scripts/check-asset.py common/src/main/resources/assets/czechcraft/textures/item/rohlik.png

# check all CzechCraft assets in the repo
python3 scripts/check-asset.py --all
```

Exit code: `0` if all checks pass; `1` if any check fails. CI runs `--all` on every push.

The script auto-detects the asset type from the filename + path (item texture vs block texture vs mod icon) and applies the appropriate rules.

---

## 9. Reference: vanilla items to study

Before producing a new texture, open Minecraft's vanilla resources at these paths (extractable from `~/.minecraft/versions/1.21.1/1.21.1.jar`) and study the closest analog:

- `bread.png` — the canonical baked-good reference.
- `cookie.png` — small round food item with poppy-seed-like detail.
- `wheat.png` — the source ingredient color reference.
- `apple.png` — fruit with simple round silhouette.
- `cake.png` — block with multiple faces (relevant when we add baked-block items).
- `potion.png` — for v2 Pivo (similar liquid-in-container shape, though Pivo will be in a mug not a flask).

Compare your in-progress work to these at the same scale. If yours has noticeably more colors or softer edges than vanilla, you're off-style.

---

## 10. License & attribution

All art produced for CzechCraft is incorporated into the mod under **MIT license** (your work becomes part of the open-source mod). Designers are credited in the relevant `CHANGELOG.md` entry and on the Modrinth project page.

If you cannot release under MIT, do not submit; talk to the maintainer about alternative arrangements first.

---

## 11. Process gotchas (learned the hard way)

- **Never edit the PNG inside the repo as your "source of truth."** PNG is lossy for working with — every save through some tools quantizes the palette differently. Keep your source file (Aseprite, Piskel) outside the repo and re-export PNG on every change.
- **Trust the validator over your eyes.** A texture that "looks like 8 colors" can have 50 due to subtle anti-aliasing in your tool. Run the validator.
- **Do not commit `*.aseprite` / `*.piskel` / `*.psd`** files to the public repo. They bloat the repo and aren't useful to most contributors. Store them in shared cloud storage instead.
- **Don't replace assets via the GitHub web editor.** Always go through git so the binary delta is real and the change is testable locally.

---

## 12. Changes to this handbook

This document evolves. To propose a rule change:

1. Open a PR modifying this file with a clear "Why" explanation.
2. Tag the maintainer for review.
3. After merge, also open a follow-up issue listing existing assets that violate the new rule, so they can be brought into compliance over time.

A changelog at the bottom is overkill for now; git history of this file is the changelog. If the file grows beyond ~500 lines, we'll split rules from rationale into separate documents.

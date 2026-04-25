# CzechCraft Asset Design Handbook

**Audience:** anyone producing visual assets (textures, icons, banners) for CzechCraft.
**Status:** binding for hard requirements; advisory for stylistic preferences.
**Last updated:** 2026-04-25

If you read nothing else, read [§4 Hard requirements](#4-hard-requirements) and [§7 Acceptance checklist](#7-acceptance-checklist). Run `python3 scripts/check-asset.py <file>` before submitting.

---

## 1. Brand & tone

CzechCraft adds Czech-themed food and drinks (rohlík, pivo, koláč, …) to Minecraft. The visual identity is:

- **Warm and inviting** — the food is comforting, slightly nostalgic. Bakery, not banquet.
- **Polished, readable, characterful** — illustration-style 16×16 sprites. We do not enforce strict vanilla pixel-art; what matters is that the whole CzechCraft set looks like one cohesive brand. (See §3 for the chosen style direction and the rationale.)
- **Czech without kitsch** — subtle Czech-flag accent (`#ffffff` over `#d7141a`) appears on the brand chrome (mod icon, optional gallery banners). Never plastered onto items themselves.
- **Affectionate, not slick** — small, lovingly-rendered, characterful. We are not a corporate brand.

### 1.1 Brand color palette (advisory)

These are anchors. You don't need to match them pixel-perfectly, but every asset's overall palette should land in this warm, food-forward family.

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
| Beer body (warm) | `#f0c060` | Pivo body |
| Beer head (foam) | `#f8efd8` | Pivo head |

### 1.2 What to avoid

- Photographic textures with heavy gradients or blur.
- Drop shadows or glow effects bleeding outside the silhouette.
- Logos / wordmarks on items themselves.
- IP that isn't ours: Mojang/Minecraft logos, Creeper face, anything copyrighted.

---

## 2. Asset types and dimensions (hard rule — wrong size = won't load)

| Asset type | Dimensions | Format | Color mode |
|---|---|---|---|
| **Item texture** | **16 × 16 px** | PNG, no interlace | sRGB, 8-bit |
| **Block texture** | 16 × 16 px (per face) | PNG | sRGB, 8-bit |
| **GUI element** | 16 × 16 or 32 × 32 (case-by-case) | PNG | sRGB |
| **Mod icon** | **128 × 128 px**, square | PNG | sRGB, 8-bit |
| **Modrinth gallery image** | 1280 × 720 px (16:9) | PNG or JPG | sRGB |
| **Modrinth feature banner** | 800 × 300 px | PNG or JPG | sRGB |

**File-size soft limit:** item textures < 5 KB, mod icon < 100 KB, gallery images < 500 KB. Far over usually means an unnecessarily large source export.

---

## 3. Style direction (chosen v1 style — applies until/unless changed)

CzechCraft is rendered in **polished illustration-style 16×16 sprites**, not strict vanilla-Minecraft pixel-art. Two reasons:

1. **Consistency over conformity.** The v1 designer's first round set the brand look. Every future item must match that look so the creative tab doesn't fragment visually.
2. **Style is a choice.** Many successful Modrinth mods use richer-than-vanilla textures (especially "tasty" food mods like Farmer's Delight, Croptopia). It reads as deliberate craft, not as a mistake, *as long as it's consistent*.

### 3.1 Concrete style markers (what to match)

Look at any existing CzechCraft item texture and reproduce these qualities:
- **~100–200 distinct colors per 16×16 sprite** (the natural result of a polished illustration export). Do not deliberately reduce — but if you're way over, the source is probably needlessly busy.
- **Soft anti-aliased silhouette** with ~50–80 partial-alpha pixels on the edge. The softness is what makes these read as "rendered" rather than "photo".
- **Diagonal viewing angle** — slight bird's-eye, food sits at a believable angle in the slot.
- **Centered subject** with a small empty margin around it.
- **No background** (RGBA with full transparency outside the silhouette).

### 3.2 If you prefer strict pixel-art

We don't reject it — but it MUST be visually consistent with the existing CzechCraft set when placed side-by-side in a creative tab. If your strict pixel-art rohlík would look out of place next to the existing kolač, it's the wrong call. The existing set is the truth; you match the set.

### 3.3 Process — drawing in your tool

- Editors: any tool that exports clean PNG works (Aseprite, Krita, Photoshop, Procreate, ChatGPT image generation if it produces consistent results, etc.).
- The v1 batch was produced by rendering at ~400 px and downscaling to 16×16 with high-quality resampling. This is fine.
- Always export final at the exact target dimension (16×16 for items, 128×128 for the icon). Do not commit oversized PNGs to the live assets path; oversized previews go in [`docs/design/proposed-artwork/`](#5-staging-directory-for-future-content).

---

## 4. Hard requirements (validator-enforced)

The validator (`scripts/check-asset.py`) enforces these. If they fail, the asset is rejected:

1. **Filename:** lowercase ASCII, alphanumerics + underscores only, ends with `.png`. (Minecraft convention is underscores — `iron_ingot.png`, `smazeny_syr.png` — NOT hyphens.)
2. **File path:** under the correct directory per §5.
3. **Dimensions:** exactly the size in §2 for the asset type.
4. **Format:** valid PNG.
5. **File size:** within the soft limit in §2.

Color count and alpha rules are NOT validator-enforced under the current illustration-style direction. They were in earlier handbook drafts; they're now style guidance only.

---

## 5. File names, repo paths, and the staging directory

### 5.1 Live (shipped) assets

These are bundled into the mod jar at build time:

| Asset | Path |
|---|---|
| Item texture for `czechcraft:<id>` | `common/src/main/resources/assets/czechcraft/textures/item/<id>.png` |
| Block texture for `czechcraft:<id>` | `common/src/main/resources/assets/czechcraft/textures/block/<id>.png` |
| Mod icon (CzechCraft brand mark) | `common/src/main/resources/assets/czechcraft/icon.png` |

The `<id>` part of the filename must match `<Name>.<NAME>_PATH` in the corresponding Java content class (e.g. `FoodItems.ROHLIK_PATH = "rohlik"` → `rohlik.png`). One identifier, used everywhere.

### 5.2 Staging directory for future content

| Asset | Path |
|---|---|
| Proposed artwork not yet shipped | `docs/design/proposed-artwork/<id>.png` |

Use this when a designer has produced art for an item that isn't implemented yet. The PR brings the art in here, the relevant GitHub issue links to the file via raw GitHub URL, and when the gameplay implementation lands the file moves from `docs/design/proposed-artwork/<id>.png` to `common/src/main/resources/assets/czechcraft/textures/item/<id>.png`.

### 5.3 Modrinth-only assets

Gallery images and feature banners are uploaded directly to the Modrinth dashboard — they are NOT in the repo.

---

## 6. Designer workflow

1. Read this handbook end to end.
2. Open the relevant existing asset for visual context (e.g., `common/src/main/resources/assets/czechcraft/textures/item/rohlik.png`). Match the style.
3. Produce your artwork in any tool you like.
4. Export final PNG at the exact target dimension (16×16 for items, 128×128 for icon).
5. Optionally also export a high-res preview at 300–500 px for documentation / Modrinth gallery use.
6. Drop the file into the repo at the path defined in §5.
7. Run the validator: `python3 scripts/check-asset.py <path>`. Fix any failures (these are dimension/filename problems — content style is on you to judge against §3).
8. **Test in-game** for item/block textures: `./gradlew :fabric:runClient`, find the item in the creative tab, check it renders cleanly at 1× and at zoom (hold Shift in inventory).
9. Commit with a `feat(art): ...` or `fix(art): ...` message.
10. Open a PR. Include a screenshot of the asset alongside an existing CzechCraft item in a creative tab.

---

## 7. Acceptance checklist

A reviewer should run through these before merging any art change:

**Mandatory (validator-checked):**
- [ ] Filename is lowercase ASCII alphanumerics + underscores, ends with `.png`.
- [ ] File is at the correct repo path per §5.
- [ ] Dimensions match §2 exactly.
- [ ] Format is PNG.
- [ ] File size within the soft limit per §2.

**Mandatory (human-judged):**
- [ ] Style is consistent with existing CzechCraft items (compare side-by-side).
- [ ] Palette feels at home in the brand family per §1.1.
- [ ] No copyrighted IP, no Mojang/Minecraft assets, no Creeper face.
- [ ] Reads clearly at 1× and at 32×32 (for the mod icon).
- [ ] Doesn't visually clash with adjacent CzechCraft items in a creative tab.

**Recommended:**
- [ ] High-res source preview (≥300 px) shared with the project for documentation use.
- [ ] Editor source file (Aseprite `.aseprite`, Procreate `.procreate`, layered PSD) kept separately so future revisions are easy. Do NOT commit source files to the repo.

---

## 8. Automated validator

`scripts/check-asset.py` enforces the mandatory validator-checked items above. It uses only Python stdlib (no Pillow dependency).

```bash
# check a single file
python3 scripts/check-asset.py common/src/main/resources/assets/czechcraft/textures/item/rohlik.png

# check all CzechCraft assets in the repo
python3 scripts/check-asset.py --all
```

Exit code: `0` if all checks pass; `1` if any check fails. CI runs `--all` on every push (once we've confirmed all live assets pass — currently wired off; tracked in the asset-CI follow-up issue).

The script auto-detects the asset type from the filename and path.

---

## 9. License & attribution

All art produced for CzechCraft is incorporated under **MIT license**. Designers are credited in the relevant `CHANGELOG.md` entry and on the Modrinth project page.

If you cannot release under MIT, talk to the maintainer about alternative arrangements before submitting.

---

## 10. Process gotchas (learned the hard way)

- **Never edit the live PNG inside the repo as your "source of truth."** Re-export PNG on every change from your editor source.
- **Trust the validator over your eyes** for dimensions and filename — these are the things that silently break runtime loading.
- **Do not commit `*.aseprite` / `*.psd` / `*.procreate`** files to the public repo. They bloat the repo and aren't useful to most contributors.
- **Don't replace assets via the GitHub web editor.** Always go through git so the binary delta is real and the change is testable locally.

---

## 11. Changes to this handbook

This document evolves. Propose rule changes via PR with a clear "Why". After merging a binding rule change, also open a follow-up issue listing existing assets that violate the new rule.

Earlier drafts of this handbook required strict vanilla-style pixel art (≤8 colors per item, binary alpha). After the v1 designer's batch came in, we recognised that the chosen brand look is illustration-style and updated the rules to match. The git history of this file is the authoritative changelog.

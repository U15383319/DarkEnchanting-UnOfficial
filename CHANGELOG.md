# Changelog

## 1.0.0 - 2026-11-09

First release of Dark Enchanting Umbra, a Forge port of Dark Enchanting 1.0.4 (Fabric 1.20.1) by Frqnny, for Minecraft 1.20.1, 1.19.2, 1.18.2 and 1.16.5.

Changes compared to the original:

- Ported from Fabric to Forge; mod id `dark_enchanting`.
- Enchanting screen rebuilt with vanilla widgets; fits the smallest "Auto" GUI scale (320x240).
- The screen stays open after enchanting or repairing, and refreshes for the updated item.
- Forge config files (client particles; server costs synced to players) replace OmegaConfig.
- Screen text is translatable (English, Simplified Chinese, Russian).
- Fixed: the above-max-level check was inverted and allowed levels above the maximum.
- Fixed: the `disabled` enchantment tag never matched.
- Fixed: the server trusted the client's block position, did not check reach, and could modify an empty stack.
- Fixed: the enchanting sound always played in the Overworld.
- Fixed: the enchanting slot accepted whole stacks; it now holds one item.
- Ships the GPL-3.0 text (COPYING) next to the LGPL-3.0 license.

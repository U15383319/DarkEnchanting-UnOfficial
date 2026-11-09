# Dark Enchanting Umbra

An unofficial Forge port of [Dark Enchanting](https://github.com/frqnny/dark-enchanting) by **Frqnny**, originally a Fabric mod. Ported and maintained by **Gvxwsur**, and not affiliated with or endorsed by the original author.

## Supported versions

| Minecraft | Forge   | Java | Branch         |
|-----------|---------|------|----------------|
| 1.20.1    | 47.x    | 17   | `main`         |
| 1.19.2    | 43.x    | 17   | `1.19.2-forge` |
| 1.18.2    | 40.x    | 17   | `1.18.2-forge` |
| 1.16.5    | 36.x    | 11+  | `1.16.5-forge` |

The mod must be installed on both the client and the server. It has no dependencies besides Forge. The 1.16.5 build needs Java 11 or newer; the Java 8 runtime bundled with the old launcher cannot load it.

## Features

- **Dark Enchanter**: put an item in and pick its enchantments directly with one slider per enchantment, paying with experience. Lowering a slider removes an enchantment and refunds part of its cost. The same table repairs items with experience.
- **Shrine discount**: rings of crying obsidian, bookshelves and Dark Conduits on obsidian pillars around the table make enchanting and repairing cheaper (up to 50% with the default config).
- **Table Upgrade**: turns a vanilla enchanting table into a Dark Enchanter in place.
- Works with enchantments added by other mods (checked against Majrusz's Enchantments on every version).

## Differences from the original

- Runs on Forge instead of Fabric. The mod id is `dark_enchanting` (Forge ids cannot contain `-`), so worlds from the Fabric version cannot be converted directly.
- The enchanting screen is rebuilt with vanilla widgets (no LibGui). It fits the smallest "Auto" GUI scale (320x240), and it stays open after enchanting or repairing, like the vanilla enchanting table.
- Settings are Forge config files: `config/dark_enchanting-client.toml` (particles) and `<world>/serverconfig/dark_enchanting-server.toml` (costs, synced to players). Per-enchantment overrides use the format `"enchantmentId,activated,personalFactor,personalReceiveFactor"`.
- Screen text is translatable (English, Simplified Chinese, Russian).
- Fixes for problems in the original:
  - `shouldRejectEnchantmentAttemptsAboveMaxValue` was inverted and allowed levels above the maximum.
  - The `disabled` enchantment tag never matched.
  - The server trusted the block position sent by the client, did not check that the player was in reach, and could modify an empty item stack.
  - The enchanting sound always played in the Overworld.
  - The enchanting slot accepted whole stacks.

See [CHANGELOG.md](CHANGELOG.md) for the release history.

## Building

```bash
./gradlew build
```

The jar is written to `build/libs/`. Each Minecraft version lives on its own branch (see the table above).

Compatibility tests against Majrusz's Enchantments run on a throwaway world:

- 1.20.1, 1.19.2, 1.18.2: `./gradlew runGameTestServer`
- 1.16.5: `./gradlew runServer -PcompatTests` (1.16.5 has no GameTest framework; the server run needs `run-gametest/eula.txt` with `eula=true`, i.e. accepting the [Minecraft EULA](https://aka.ms/MinecraftEULA))

## License

Licensed under the GNU Lesser General Public License v3.0, like the original mod: see [LICENSE](LICENSE) (LGPL-3.0) and [COPYING](COPYING) (GPL-3.0, which the LGPL builds on).

Based on Dark Enchanting 1.0.4 for Fabric 1.20.1 by Frqnny (<https://github.com/frqnny/dark-enchanting>, commit `4b2f871`). Modified by Gvxwsur in November 2026: ported to Forge for Minecraft 1.20.1, 1.19.2, 1.18.2 and 1.16.5, with the changes listed above.

# ATMons Weekend Boost

A server-side only NeoForge mod for ATMons that automatically boosts Cobblemon spawn rates, shiny chances, EXP, and Lucky Egg multiplier every weekend.

## Features
- ✅ Server-side only — players need zero client installation
- ✅ Automatically applies boosted config before Cobblemon loads
- ✅ Announces boost to all players on activation
- ✅ Re-announces every 6 hours while active
- ✅ Restores normal rates automatically on Monday

## Weekend Boosts
| Setting | Weekday | Weekend |
|---|---|---|
| Shiny Rate | 1/8192 | 1/2048 (4x) |
| Pokémon Per Chunk | 1.0 | 3.0 |
| Max Spawns Per Pass | 8 | 16 |
| EXP Multiplier | 2.0 | 4.0 |
| Lucky Egg Multiplier | 1.5 | 3.0 |
| Rare bucket | 0.5 | 8.0 |
| Ultra-rare bucket | 0.2 | 5.0 |

## Installation
Drop the `.jar` file into your server's `mods` folder. No client installation needed.

## Configuration
Edit the values in `WeekendBoostConfig.java` and rebuild.

## Building
This mod uses GitHub Actions to build automatically. Every push to `main` will produce a new release with the compiled `.jar`.

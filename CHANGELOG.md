# 1.0.2
## Added:
- Added devMode config
## Fixed:
- Fixed Non-stackable items ([#1](https://github.com/Herobrot/scalingdifficulty/issues/1))
## Changed:
- Refactor code for better readability
- Optimize a bit the code
- The command "/sddiag" now appears only if devMode is `true`
---
# 1.0.1
## Added:
- Levelplate compat
- Added command `sddiag` to check the lootDropChance increase
## Fixed:
- Fixed boss config not applied
- Fixed time using gameTime and not dayTime, making the server not change difficulty with time commands
## Changed:
- Boosted drop chance
- HUD Debug in time now using minutes
---
# 1.0.0
## Added:
- Initial release
## Fixed:
- Fixed bugs related to CarryOn and NBT copy-paste (bees entering and exiting bee nest)
## Changed:
- Comparing to the Fabric version, the id of the mod now is `scalingdifficulty`
- Added a better HUD Debug
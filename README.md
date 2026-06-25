# ScalingDifficulty
ScalingDifficulty is a mod that dynamically strengthens mobs over time, distance, and height, ensuring the game's challenge scales naturally as you progress through your world.

### Installation
ScalingDifficulty is built for [NeoForge](https://neoforged.net/) (1.21.1). It requires the [Cloth Config API](https://www.curseforge.com/minecraft/mc-mods/cloth-config) to be installed separately in your mods folder.

### Config
- `increasingDistance`: distance in blocks to increase the strength of mobs by the `distanceFactor`. <br>
  *Example: 300 blocks = 10% stronger, 600 blocks = 20% stronger. Can be disabled by setting it to 0.*
- `increasingTime`: time in minutes to increase the strength of mobs by the `timeFactor`. <br>
  *Example: 60 minutes = 5% stronger, 120 minutes = 10% stronger. Can be disabled by setting it to 0.*
- `heightDistance`: distance in blocks from the mob's Y coordinate (minus `startingHeight` / `heightDistance`) to increase the strength of mobs by the `heightFactor`. <br>
  *Example: 30 blocks above `startingHeight` = 10% stronger, 60 blocks above `startingHeight` = 20% stronger. Can be disabled by toggling `positiveHeightIncrement` or `negativeHeightIncrement`.*
- `maxFactorHealth`: max factor for health increase. <br>
  *Example: 1.0 = no increase at all, 2.0 = max double health, 3.0 = max triple health.*
- `maxFactorDamage`: max factor for damage increase. <br>
  *Example: see `maxFactorHealth`.*
- `maxFactorProtection`: max factor for protection/armor increase. <br>
  *Example: see `maxFactorHealth`.*
- `maxFactorSpeed`: max factor for speed increase. <br>
  *Example: see `maxFactorHealth`. Only applies to the special speedy zombies.*
- `allowRandomValues`: allow random stat modifiers to be applied to mobs based on `randomChance`.
- `randomChance`: percentage chance (0-100) for a mob to receive randomized stats if `allowRandomValues` is true.
- `randomFactor`: percentage variance applied when `allowRandomValues` triggers. <br>
  *Example: mobHealth = mobHealth * (1 - randomFactor + (random.nextDouble() * randomFactor * 2F)).*
- `extraXP`: dynamically calculates bonus XP drops based on the mob's current strength multiplier. <br>
  *Example: A strength multiplier of 2.0x (due to traveling 3000 blocks) = double the XP drops.*
- `maxXPFactor`: maximum allowed multiplier for XP drops. <br>
  *Example: 2.0 = maximum double XP drop.*
- `startingFactor`: the overall baseline multiplier for calculations. <br>
  *Example: 1.0 = normal (100% vanilla stats), 2.0 = double base stats.*
- `startingDistance`: the baseline distance from the world spawn point before the distance calculation begins. <br>
  *Example: 0 = scaling starts immediately at spawn, 100 = scaling begins 100 blocks away from spawn.*
- `startingTime`: the baseline time in minutes before the time calculation begins. <br>
  *Example: 0 = scaling starts at minute 0, 60 = scaling begins after 60 minutes.*

### Datapacks

If you want to apply specific difficulty settings to individual dimensions, you can use a Datapack.
If you don't know how to create a datapack, check out the [Data Pack Wiki](https://minecraft.wiki/w/Data_pack) to learn the basics.

Inside your datapack, your JSON files must be placed in the following exact folder path:
`data/scalingdifficulty/difficulty/YOURFILE.json`

**Note on Bosses:** Boss modifiers are applied to all mobs that carry the common community boss tag (#c:bosses). The mod automatically tags the Warden, Wither, Ender Dragon, and Elder Guardian.

(Tip: If the mod is used on a dedicated server that runs 24/7, turning the timeFactor to 0 is highly recommended to prevent new players from being overwhelmed. Or else use like 2440 minutes to add a +10% every two full days).

Settings that can be modified per-dimension:

- distanceCoordinatesX
- distanceCoordinatesZ
- increasingDistance
- distanceFactor
- increasingTime
- timeFactor
- heightDistance
- heightFactor
- maxFactorHealth
- maxFactorDamage
- maxFactorProtection
- maxFactorSpeed
- startingFactor
- startingDistance  
- startingTime  
- startingHeight  
- positiveHeightIncrement  
- negativeHeightIncrement  

Example (`data/scalingdifficulty/difficulty/nether.json`):
```json
{
    "dimension": "minecraft:the_nether",
    "increasingDistance": 300,
    "distanceFactor": 0.1,
    "increasingTime": 60,
    "timeFactor": 0.05,
    "heightDistance": 30,
    "heightFactor": 0.1,
    "maxFactorHealth": 3.0,
    "maxFactorDamage": 3.0,
    "maxFactorProtection": 1.5,
    "maxFactorSpeed": 2.0,
    "startingFactor": 1.0,
    "startingDistance": 0,
    "startingTime": 0,
    "startingHeight": 62,
    "positiveHeightIncrement": true,
    "negativeHeightIncrement": true
}
```
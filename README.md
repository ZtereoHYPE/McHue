# McHue
A Minecraft mod that attempts to sync your lights with the game context.

## WARNING:
This is still heavily work in progress and the lights might not match the surroundings well.
Many things are also hardcoded as an interface to configure them is not ready yet, but work is being made in my free time.

## [notepad for the developer]:
- based on biome
- based on lighting conditions
- CAVE DETECTION
- based on sky darkness (thunderstorms, rain, and lightning!)
- based on night vision lol
- i might have to reimplement everything to use entertainment zones if they use a special stream thing to make performance or latency better, although i need to research
- UPGRADE TO API V2!! (v1 mode for older bridges?) https://developers.meethue.com/develop/hue-entertainment/hue-entertainment-api/
  - // typically a streaming rate of 50-60Hz is used.
    - Okay so V2 exposes HueStream for less delay and location awareness!
    - THis means that we MUST support multiple channels (eg. ground, sky, left, right!!)

### things i noticed while playtesting:
- nether colour transitions are sharp and rough
- red flash when hurt or lava?
- light transitions can be jarring (often pass from yellow) (maybe force two consecutive light changes to make it quick?)
- make insides be warmer and cosyer
- while changing dimentions?? pinK???????
- too sensistive to light changes, they get annoying... Maybee should be average?
- less delay pls!



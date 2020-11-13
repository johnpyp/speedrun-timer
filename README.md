# Speedrun Timer - Fabric

## Usage

1. Install minecraft 1.16.x with fabric
   - Recommended to download and use [GDLauncher](https://gdevs.io/) for managing this,
     however MultiMC or Twitch Launcher will work fine. Each has an interface to create such a launcher.
2. Install [fabric-api](https://www.curseforge.com/minecraft/mc-mods/fabric-api).
   - In GDLauncher or the Twitch Launcher, you can install the mod
     easily through their mod adding interface. In MultiMC, you might need
     to manually download the jar or put in a link.
3. Download the latest version of `speedruntimer-X.X.X.jar` from the [releases tab](https://github.com/johnpyp/speedrun-timer/releases).
4. Load the jar file as a mod through your launcher, put the downloaded jar in the mods folder.
5. Play! No configuration yet.

## Build

```
git clone https://github.com/johnpyp/speedrun-timer
cd speedrun-timer
./gradlew build

# Mod jar is in build/libs/speedruntimer-X.X.X.jar
```

## License

MIT

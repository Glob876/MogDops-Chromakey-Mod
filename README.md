# MogDop's Chromakey Mod — Fabric 1.21.4

Chromakey blocks with eight color presets, a custom RGB palette and connected-wall controls.

## Installation

Minecraft **1.21.4**, Java **21+**, Fabric Loader 0.18.4+, Fabric API 0.119.4+1.21.4 and owo-lib 0.12.20+1.21.4.
Install the mod and its dependencies on both the client and the server. This JAR targets Fabric; other loaders are not advertised as supported without a separate compatibility test.

## Controls

- Sneak and right-click a chromakey block without the controller to toggle the connected wall's light.
- Right-click with the Chromakey Controller to open the palette, enter a `#RRGGBB` color or choose a preset, then press **Apply**.
- Colors persist in the world and synchronize to connected clients.
- Sneaking also allows instant block breaking, as in the original mod.

## Development

Use JDK 25 to run the Gradle wrapper. The compiled mod targets the Java version listed above.

```sh
./gradlew build
./gradlew runClient
```

Release JARs are in `build/libs/`. The `-sources.jar` is for development, not installation.

The isolated integration check creates a disposable world under `build/qa-client`:

```sh
./gradlew -Pqa runQaClient
# Headless Linux:
xvfb-run -a ./gradlew -Pqa runQaClient
```

It checks registration, connected lighting and color changes, disconnected blocks, NBT persistence, opening the menu with the controller, and client/server color synchronization. Its `chromakey-qa` entrypoint is excluded from release JARs. Results and screenshots are under `build/qa-client`.

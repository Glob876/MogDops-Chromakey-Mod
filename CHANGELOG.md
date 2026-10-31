# 3.3.4 — Fabric 1.16.5

- Port the current chromakey palette, eight presets and connected-wall controls to Minecraft 1.16.5.
- Preserve custom RGB colors across saves and synchronize updates to clients.
- Adapt registration, networking, rendering and data resources to this Minecraft version.
- Validate color edits on the server: loaded target, nearby non-spectator player, controller in hand.
- Replace amethyst with nether quartz in the controller recipe.
- Provide the palette without an owo-lib dependency; the mod runs on Java 8.

Validation: Gradle build and an isolated in-game integration check.

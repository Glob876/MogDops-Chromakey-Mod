# MogDop's Chromakey Mod 🎬🟢🔵🔴

A Fabric mod for Minecraft that adds functional, glowing chromakey blocks and convenient tools to control them. Designed for video editing, streaming, and special effects.

## ✨ Features

* **Connected Chromakey Blocks:** Supports Green, Blue, and Red colors for background removal.
* **Toggleable Lighting:** Blocks can emit light to eliminate unwanted shadows in your shots.
* **Flood Fill Propagation:** Toggling or recoloring a block instantly updates the entire connected structure.
* **Chromakey Controller:** A convenient handheld tool to cycle colors across whole walls.

## 🎮 Usage & Controls

* **Toggle Light (On/Off):** 
  Sneak (Shift) + Right-Click a block with an empty hand to toggle the light of the entire connected wall.
* **Change Wall Color:** 
  Right-Click any chromakey block while holding the **Chromakey Controller** to cycle the color of the entire structure: Green ➡️ Blue ➡️ Red ➡️ Green.

## 💻 Development & Building

* **Run Client (Testing):**
  ```bash
  ./gradlew runClient
  ```
* **Build Mod JAR:**
  ```bash
  ./gradlew build
  ```
  The compiled `.jar` file will be generated in the `build/libs/` directory.

This version ONLY for 1.21.11.
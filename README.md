# The Murk

**The Murk** is a horror-themed Fabric mod for Minecraft 1.20.1 that transforms darkness into a deadly threat. Venture into the shadows unprepared, and face the chilling consequences of *Murk's Grasp*, a debuff that creeps up in low light levels, bringing fear and danger to your world.

## Features

- **Murk's Grasp Effect**: 
  - Triggers in low light levels (default: below 3) after 15 seconds of darkness, with a warning message at 5 seconds.
  - Applies a harmful status effect with a dark purple hue, slowing movement by 15% and dealing scaling damage (0.5 to 2.0 hearts every 3 seconds by default) if you linger in the dark.
  - Persists for a configurable duration (default: 4 seconds) after entering a lit area, with optional Blindness effect for extra immersion.

- **Dynamic Light Detection**:
  - Supports held items, dropped items, trinket slots (via Trinkets), and nearby players’ light sources to reset the darkness timer.
  - Configurable light sources (e.g., torches, lanterns, glowstone) with customizable luminance and water sensitivity.
  - Integrates with **LambDynamicLights** for realistic light checks, requiring line-of-sight to light-emitting items.

- **Configurable Gameplay**:
  - Customize light thresholds, dimensions (e.g., Overworld, Nether), and effect behaviors via `config/murk.json5`.
  - Options to enable/disable underwater light checks, Creative mode effects, and warning messages.
  - Deduplicated dimension and light source lists to prevent config errors.

- **NOTES**:
  - Inspired by mods like *OminousDarkness* and *Grue* but built for Fabric with modern APIs and seamless integration.

## Dependencies

- **Required**: Fabric API, Cloth Config, Lodestone, Cardinal Components API
- **Optional**: Trinkets, LambDynamicLights, Sodium Dynamic Lights, ModMenu

## Configuration

Edit `config/murk.json5` to tweak:
- Light threshold (0-15)
- Dimensions where the effect applies (e.g., `minecraft:overworld`, `minecraft:the_nether`)
- Effect duration, damage, and Blindness toggle
- Light source properties and detection radii
- For **LambDynamicLights** integration, ensure each light-emitting item in `lightSource_lightSources` has a corresponding JSON file in `assets/murk/dynamiclights/item/` (e.g., `torch.json`, `lantern.json`) to make items visually emit light. See documentation: https://lambdaurora.dev/projects/lambdynamiclights/docs/v4/item.html

Use **ModMenu** for in-game configuration with categorized settings (General, Effect, Light Source) and tooltips for clarity.

## Installation

1. Install **Fabric Loader 0.16.14** for Minecraft 1.20.1.
2. Download and place **The Murk** and required dependencies in your `mods` folder.
3. Optionally add **Trinkets**, **LambDynamicLights**, and **ModMenu** for enhanced features.
4. Launch Minecraft and configure via `config/murk.json5` or **ModMenu**.

## Notes

- Built with Java 17 and Yarn mappings (1.20.1+build.10).
- Avoids deprecated APIs for stability and modpack compatibility.
- Tested for single-player and multiplayer, with logging to debug config issues.

Embrace the darkness—if you dare! Report issues or suggest features on our [GitHub](https://github.com/yourusername/themurk) or Modrinth page.

<p align="center"><img src="https://github.com/user-attachments/assets/547baf4d-088b-4311-9366-80260c2d1f55" alt="Logo"></p>
<h1 align="center">GregTech CEu: Modern</h1>
<p align="center">GregTech:CEu built on modern Minecraft versions for Forge(1.20.1) & NeoForge(1.21+).</p>
<h1 align="center">
    <a href="https://www.curseforge.com/minecraft/mc-mods/gregtechceu-modern"><img src="https://img.shields.io/badge/Available%20for-MC%201.20.1+%20-informational?style=for-the-badge" alt="Supported Versions"></a>
    <a href="https://github.com/GregTechCEu/GregTech-Modern/blob/1.20.1/LICENSE"><img src="https://img.shields.io/github/license/GregTechCEu/GregTech?style=for-the-badge" alt="License"></a>
    <a href="https://discord.gg/bWSWuYvURP"><img src="https://img.shields.io/discord/701354865217110096?color=5464ec&label=Discord&style=for-the-badge" alt="Discord"></a>
    <br>
    <a href="https://www.curseforge.com/minecraft/mc-mods/gregtechceu-modern"><img src="https://cf.way2muchnoise.eu/890405.svg?badge_style=for_the_badge" alt="CurseForge"></a>
    <a href="https://modrinth.com/mod/gregtechceu-modern"><img src="https://img.shields.io/modrinth/dt/gregtechceu-modern?logo=modrinth&label=&suffix=%20&style=for-the-badge&color=2d2d2d&labelColor=5ca424&logoColor=1c1c1c" alt="Modrinth"></a>
    <a href="https://github.com/GregTechCEu/GregTech-Modern/releases"><img src="https://img.shields.io/github/downloads/GregTechCEu/GregTech-Modern/total?sort=semver&logo=github&label=&style=for-the-badge&color=2d2d2d&labelColor=545454&logoColor=FFFFFF" alt="GitHub"></a>
</h1>

### [Wiki](https://gregtechceu.github.io/gtceu-modern-docs/)

## Developers

To add GTCEu: Modern (GTM) to your project as a dependency, add the following to your `build.gradle`:
```groovy
repositories {
    maven {
        name = 'GTCEu Maven'
        url = 'https://maven.gtceu.com'
        content {
            includeGroup 'com.gregtechceu.gtceu'
        }
    }
}
```
Then, you can add it as a dependency, with `${mc_version}` being your Minecraft version target and `${gtm_version}` being the version of GTM you want to use.
```groovy
dependencies {
	// Forge (see below block as well if you use Forge Gradle)
	implementation fg.deobf("com.gregtechceu.gtceu:gtceu-${mc_version}:${gtm_version}")

	// NeoForge
	implementation "com.gregtechceu.gtceu:gtceu-${mc_version}:${gtm_version}"

	// Architectury
	modImplementation "com.gregtechceu.gtceu:gtceu-${mc_version}:${gtm_version}"
}
```

### IDE Requirements (when using IntelliJ IDEA)

For contributing to this mod, the [Lombok plugin](https://plugins.jetbrains.com/plugin/6317-lombok) for IntelliJ IDEA is strictly required.  
Additionally, the [Minecraft Development plugin](https://plugins.jetbrains.com/plugin/8327-minecraft-development) is recommended.


## Credited Works
- Most textures are originally from [Gregtech: Refreshed](https://modrinth.com/resourcepack/gregtech-refreshed) by @ULSTICK. With some consistency edits and additions by @Ghostipedia.
- Some textures are originally from the **[ZedTech GTCEu Resourcepack](https://github.com/brachy84/zedtech-ceu)**, with some changes made by the community.
- New material item textures by @TTFTCUTS and @Rosethorns.
- Wooden Forms, World Accelerators, and the Extreme Combustion Engine are from the **[GregTech: New Horizons Modpack](https://www.curseforge.com/minecraft/modpacks/gt-new-horizons)**.
- Primitive Water Pump is from the **[IMPACT: GREGTECH EDITION Modpack](https://gt-impact.github.io/#/)**.
- Ender Fluid Link Cover, Auto-Maintenance Hatch, Optical Fiber, and Data Bank Textures are from **[TecTech](https://github.com/Technus/TecTech)**.
- Steam Grinder is from **[GregTech++](https://www.curseforge.com/minecraft/mc-mods/gregtech-gt-gtplusplus)**.
- Certificate of Not Being a Noob Anymore is from **[Crops++](https://www.curseforge.com/minecraft/mc-mods/berries)**.

See something we forgot to credit? Reach out to us on Discord, or open an issue and ask for appropriate credit, we will happily mark it here

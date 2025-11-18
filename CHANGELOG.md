# ChangeLog

## Version [v7.3.0](https://github.com/GregTechCEu/GregTech-Modern/compare/v7.2.1-1.20.1...v7.3.0-1.20.1)
### Added

- Re-enable using Screwdriver to toggle Drum auto output, if Allow Input From Output Side is disabled by @DilithiumThoride in [#4163](https://github.com/GregTechCEu/GregTech-Modern/pull/4163)
- Implement voiding mode by @nutant233 in [#3924](https://github.com/GregTechCEu/GregTech-Modern/pull/3924)
- Turbine Energy Voiding by @YoungOnionMC in [#4177](https://github.com/GregTechCEu/GregTech-Modern/pull/4177)
- Max Parallel Setting on place by @YoungOnionMC in [#4186](https://github.com/GregTechCEu/GregTech-Modern/pull/4186)
- Miner block replacement by @YoungOnionMC in [#4021](https://github.com/GregTechCEu/GregTech-Modern/pull/4021)
- Change some formulas to make them more accurate by @TarLaboratories in [#4017](https://github.com/GregTechCEu/GregTech-Modern/pull/4017)

### Fixed

- Fix CC integration with the monitor cover by @TarLaboratories in [#4143](https://github.com/GregTechCEu/GregTech-Modern/pull/4143)
- Add material existing checks before making pipe recipes by @jurrejelle in [#4146](https://github.com/GregTechCEu/GregTech-Modern/pull/4146)
- Fix toggling Advanced Energy Detector Cover between EU and % modes causing values to decrement by @DilithiumThoride in [#4159](https://github.com/GregTechCEu/GregTech-Modern/pull/4159)
- Fix Stone Pressure Plate / Button recipe conflict by @DilithiumThoride in [#4131](https://github.com/GregTechCEu/GregTech-Modern/pull/4131)
- Re-enable using Screwdriver to toggle Drum auto output, if Allow Input From Output Side is disabled by @DilithiumThoride in [#4163](https://github.com/GregTechCEu/GregTech-Modern/pull/4163)
- Validate KubeJS recipes to ensure no invalid/missing ingredients by @mikerooni in [#4170](https://github.com/GregTechCEu/GregTech-Modern/pull/4170)
- fix (steam) multiblocks not resetting progress on powerfail by @DilithiumThoride in [#4181](https://github.com/GregTechCEu/GregTech-Modern/pull/4181)
- Add missing tooltips for auto-output buttons in machine UIs by @mikerooni in [#4180](https://github.com/GregTechCEu/GregTech-Modern/pull/4180)
- Fix XEI display for recipes with IO and TickIO of the same ingredient type by @DilithiumThoride in [#4137](https://github.com/GregTechCEu/GregTech-Modern/pull/4137)
- Fix Jade provider displaying no energy for addon steam machines consuming 1mB/t by @DilithiumThoride in [#4127](https://github.com/GregTechCEu/GregTech-Modern/pull/4127)
- Fix ore veins with only one layer causing crashes by @screret in [#4183](https://github.com/GregTechCEu/GregTech-Modern/pull/4183)
- Fix Ranged Fluid Ingredients deserializing incorrectly in SMP by @DilithiumThoride in [#4150](https://github.com/GregTechCEu/GregTech-Modern/pull/4150)
- Swap Diluted HCl to return HCl on Circuit 1 by @DilithiumThoride in [#4125](https://github.com/GregTechCEu/GregTech-Modern/pull/4125)
- Fix Tool Matching using every Item tag by @YoungOnionMC in [#3945](https://github.com/GregTechCEu/GregTech-Modern/pull/3945)
- Ore Maceration Changes by @YoungOnionMC in [#4020](https://github.com/GregTechCEu/GregTech-Modern/pull/4020)

### Changed

- update ja_jp by @code-onigiri in [#4099](https://github.com/GregTechCEu/GregTech-Modern/pull/4099)
- Deprecate ChanceLogic.FIRST by @DilithiumThoride in [#4109](https://github.com/GregTechCEu/GregTech-Modern/pull/4109)
- Holistic Rebalance to the Large and Small Boiler by @Ghostipedia in [#4075](https://github.com/GregTechCEu/GregTech-Modern/pull/4075)
- Ore Maceration Changes by @YoungOnionMC in [#4020](https://github.com/GregTechCEu/GregTech-Modern/pull/4020)

 
## Version [v7.2.1](https://github.com/GregTechCEu/GregTech-Modern/compare/v.7.2.0-1.20.1...v7.2.1-1.20.1)
### Added

- Add flag to disable create compat by @jurrejelle in [#3956](https://github.com/GregTechCEu/GregTech-Modern/pull/3956)
- Make new flag DISABLE_MATERIAL_RECIPES to replace NO_UNIFICATION. by @Phoenixvine32908 in [#3999](https://github.com/GregTechCEu/GregTech-Modern/pull/3999)
- Alter Jade/TOP provider to display machine voltage tier by machine tier rather than by recipe/overclock tier by @DilithiumThoride in [#4002](https://github.com/GregTechCEu/GregTech-Modern/pull/4002)
- Buff facade recipe by @htmlcsjs in [#4007](https://github.com/GregTechCEu/GregTech-Modern/pull/4007)
- Format Numbers by @remakefactory in [#4111](https://github.com/GregTechCEu/GregTech-Modern/pull/4111)

### Fixed

- Make energy placeholder work with substations by @TarLaboratories in [#3964](https://github.com/GregTechCEu/GregTech-Modern/pull/3964)
- Fix Advanced Energy Detector Cover not working in % mode by @DilithiumThoride in [#3950](https://github.com/GregTechCEu/GregTech-Modern/pull/3950)
- Fix recipe search for Ranged Fluid Ingredients on 0 roll by @DilithiumThoride in [#3968](https://github.com/GregTechCEu/GregTech-Modern/pull/3968)
- Rename AdjacentFluid overload by @jurrejelle in [#3960](https://github.com/GregTechCEu/GregTech-Modern/pull/3960)
- Slightly increase Large Turbine energy output and fuel burn by @DilithiumThoride in [#3988](https://github.com/GregTechCEu/GregTech-Modern/pull/3988)
- Fix crates voiding the inventories of their stack after being placed down by @purebluez in [#4012](https://github.com/GregTechCEu/GregTech-Modern/pull/4012)
- Read and Display Subtick Overclock Parallels separately from Hatch-based Parallels by @DilithiumThoride in [#3961](https://github.com/GregTechCEu/GregTech-Modern/pull/3961)
- Fix central monitor crash by @TarLaboratories in [#4010](https://github.com/GregTechCEu/GregTech-Modern/pull/4010)
- Fix Machine explosions by @YoungOnionMC in [#3983](https://github.com/GregTechCEu/GregTech-Modern/pull/3983)
- Fix color spray not working with  blockstate by @bnjmn21 in [#3982](https://github.com/GregTechCEu/GregTech-Modern/pull/3982)
- Fix and align textures of (passthrough) hatch/buses, pipeline endpoints and pump hatch by @arsdragonfly in [#3944](https://github.com/GregTechCEu/GregTech-Modern/pull/3944)
- Add slice-by-slice recipe consumption by @jurrejelle in [#4006](https://github.com/GregTechCEu/GregTech-Modern/pull/4006)
- Fix representative recipes not generating by @YoungOnionMC in [#4019](https://github.com/GregTechCEu/GregTech-Modern/pull/4019)
- Downscale manual IO disabled button by @JuiceyBeans in [#4051](https://github.com/GregTechCEu/GregTech-Modern/pull/4051)
- fix registry removal by @NegaNote in [#4108](https://github.com/GregTechCEu/GregTech-Modern/pull/4108)
- Fix material decomp not working with KJS recipes by @YoungOnionMC in [#4116](https://github.com/GregTechCEu/GregTech-Modern/pull/4116)
- Fix Polished Stone Crafting Recipe by @YoungOnionMC in [#4071](https://github.com/GregTechCEu/GregTech-Modern/pull/4071)
- Fix laser and Optical pipe ignoring connections by @nutant233 in [#3939](https://github.com/GregTechCEu/GregTech-Modern/pull/3939)
- Fix turbines crashing when interacting with active rotor holders by @Taskeren in [#4047](https://github.com/GregTechCEu/GregTech-Modern/pull/4047)

### Changed

- Force modifier re-apply when recipe starts after being suspended by @jurrejelle in [#3971](https://github.com/GregTechCEu/GregTech-Modern/pull/3971)
- recipe manager handling refactor by @TechLord22 in [#3975](https://github.com/GregTechCEu/GregTech-Modern/pull/3975)
- Rename AdjacentFluid overload by @jurrejelle in [#3960](https://github.com/GregTechCEu/GregTech-Modern/pull/3960)
- Read and Display Subtick Overclock Parallels separately from Hatch-based Parallels by @DilithiumThoride in [#3961](https://github.com/GregTechCEu/GregTech-Modern/pull/3961)
- Translated using Weblate (Russian) for 1.20.1 by @marisathewitch in [#4078](https://github.com/GregTechCEu/GregTech-Modern/pull/4078)

 
## Version [v.7.2.0](https://github.com/GregTechCEu/GregTech-Modern/compare/v7.1.4-1.20.1...v.7.2.0-1.20.1)
### Added

- Add pipe casting molds by @FourIsTheNumber in [#3671](https://github.com/GregTechCEu/GregTech-Modern/pull/3671)
- Suspend machines immediately if they're already idle by @DilithiumThoride in [#3719](https://github.com/GregTechCEu/GregTech-Modern/pull/3719)
- Generic Research Lang Keys by @YoungOnionMC in [#3738](https://github.com/GregTechCEu/GregTech-Modern/pull/3738)
- Add CraftingComponent for emitter/sensor gems by @dz894 in [#3720](https://github.com/GregTechCEu/GregTech-Modern/pull/3720)
- Custom Material Lang name by @YoungOnionMC in [#3578](https://github.com/GregTechCEu/GregTech-Modern/pull/3578)
- Recycling Yield and Working Configs by @YoungOnionMC in [#3754](https://github.com/GregTechCEu/GregTech-Modern/pull/3754)
- Powerfailing bypass by @YoungOnionMC in [#3767](https://github.com/GregTechCEu/GregTech-Modern/pull/3767)
- Ranged Inputs by @DilithiumThoride in [#3694](https://github.com/GregTechCEu/GregTech-Modern/pull/3694)
- More ender link covers by @TarLaboratories in [#3598](https://github.com/GregTechCEu/GregTech-Modern/pull/3598)
- programmed circuit icon Unified 9 Display by @remakefactory in [#3791](https://github.com/GregTechCEu/GregTech-Modern/pull/3791)
- Tank Fluid Preview by @Taskeren in [#3716](https://github.com/GregTechCEu/GregTech-Modern/pull/3716)
- Large Bronze Tank by @TarLaboratories in [#3796](https://github.com/GregTechCEu/GregTech-Modern/pull/3796)
- Minor improvements to jetpacks by @TarLaboratories in [#3798](https://github.com/GregTechCEu/GregTech-Modern/pull/3798)
- Play metal pipe sound when a long rod item falls by @TarLaboratories in [#3829](https://github.com/GregTechCEu/GregTech-Modern/pull/3829)
- Fixes and minor additions to the placeholder system by @TarLaboratories in [#3790](https://github.com/GregTechCEu/GregTech-Modern/pull/3790)
- Wrench improvements by @TarLaboratories in [#3690](https://github.com/GregTechCEu/GregTech-Modern/pull/3690)
- Make redstone-related blocks mineable with wrenches by @TarLaboratories in [#3800](https://github.com/GregTechCEu/GregTech-Modern/pull/3800)
- Add API for setting batch mode by @YoungOnionMC in [#3872](https://github.com/GregTechCEu/GregTech-Modern/pull/3872)
- Added allow input from output side for drums and the config by @Taskeren in [#3789](https://github.com/GregTechCEu/GregTech-Modern/pull/3789)
- Allow ULV Input Hatches in Primitive Pump Multiblock by @Nanabell in [#3892](https://github.com/GregTechCEu/GregTech-Modern/pull/3892)
- Allow filling steam boiler water/fuel tank with buckets by @serenibyss in [#3519](https://github.com/GregTechCEu/GregTech-Modern/pull/3519)
- Hammer Prospecting by @TarLaboratories in [#3802](https://github.com/GregTechCEu/GregTech-Modern/pull/3802)
- Growing plants rendering system by @RubenVerg in [#3363](https://github.com/GregTechCEu/GregTech-Modern/pull/3363)
- Placeable Fluids by @YoungOnionMC in [#3558](https://github.com/GregTechCEu/GregTech-Modern/pull/3558)
- Added Jade provider for Data Bank by @Taskeren in [#3930](https://github.com/GregTechCEu/GregTech-Modern/pull/3930)

### Fixed

- Fix Storage Cover missing its cover icon texture in machine UI by @purebluez in [#3684](https://github.com/GregTechCEu/GregTech-Modern/pull/3684)
- Fix Large Maceration Tower not receiving dropped items as input by @dz894 in [#3687](https://github.com/GregTechCEu/GregTech-Modern/pull/3687)
- Fix Hammer AOE and sounds missing by @YoungOnionMC in [#3688](https://github.com/GregTechCEu/GregTech-Modern/pull/3688)
- Fix Icons not being centered in tool grid by @dz894 in [#3709](https://github.com/GregTechCEu/GregTech-Modern/pull/3709)
- Reduce polymer extrusion cost by @FourIsTheNumber in [#3670](https://github.com/GregTechCEu/GregTech-Modern/pull/3670)
- Fix Ignore NBT in Not Working Properly in Robot Arms/Fluid Regulators by @Bumperdo09 in [#3699](https://github.com/GregTechCEu/GregTech-Modern/pull/3699)
- Fix passthrough hatches  by @DilithiumThoride in [#3721](https://github.com/GregTechCEu/GregTech-Modern/pull/3721)
- Fix quantum tanks' fluid handlers not checking if the contained fluid is valid by @Taskeren in [#3715](https://github.com/GregTechCEu/GregTech-Modern/pull/3715)
- Queue an update for pattern buffers when they are placed by @jurrejelle in [#3724](https://github.com/GregTechCEu/GregTech-Modern/pull/3724)
- Update docs for creating custom coils. Fix tooltip on coil energy usage. by @DilithiumThoride in [#3739](https://github.com/GregTechCEu/GregTech-Modern/pull/3739)
- Give idle HPCA parts emissive textures by @YoungOnionMC in [#3746](https://github.com/GregTechCEu/GregTech-Modern/pull/3746)
- Improve name for research recipe failure case by @jurrejelle in [#3737](https://github.com/GregTechCEu/GregTech-Modern/pull/3737)
- Fix amount checking for fluid containers by @jurrejelle in [#3731](https://github.com/GregTechCEu/GregTech-Modern/pull/3731)
- Add check for recipe voltage in notifiableEnergyContainer by @jurrejelle in [#3735](https://github.com/GregTechCEu/GregTech-Modern/pull/3735)
- Fix world accelerator not working when reloaded by @WinExp in [#3743](https://github.com/GregTechCEu/GregTech-Modern/pull/3743)
- Offset facades by smaller number by @jurrejelle in [#3728](https://github.com/GregTechCEu/GregTech-Modern/pull/3728)
- Fix Multi-Smelter Energy Usage by @DilithiumThoride in [#3748](https://github.com/GregTechCEu/GregTech-Modern/pull/3748)
- Make Jade Provider not show %s Fluid Cell by @DilithiumThoride in [#3752](https://github.com/GregTechCEu/GregTech-Modern/pull/3752)
- Don't do chance rolls for ingredients with a chance of 0.  by @DilithiumThoride in [#3751](https://github.com/GregTechCEu/GregTech-Modern/pull/3751)
- Fix broken models of multi parts by @YoungOnionMC in [#3745](https://github.com/GregTechCEu/GregTech-Modern/pull/3745)
- Fix Partial NBT not matching by @jurrejelle in [#3761](https://github.com/GregTechCEu/GregTech-Modern/pull/3761)
- Increase priority on inventory mixin to prevent clashing with IU by @jurrejelle in [#3765](https://github.com/GregTechCEu/GregTech-Modern/pull/3765)
- Three fixes with Chanced and Ranged Outputs by @DilithiumThoride in [#3691](https://github.com/GregTechCEu/GregTech-Modern/pull/3691)
- Missing Formed AE Model Property by @YoungOnionMC in [#3773](https://github.com/GregTechCEu/GregTech-Modern/pull/3773)
- Fix AOE not working on tools other than Mining Hammer by @YoungOnionMC in [#3774](https://github.com/GregTechCEu/GregTech-Modern/pull/3774)
- Fix fluid cell dupe using AE2 by @TarLaboratories in [#3693](https://github.com/GregTechCEu/GregTech-Modern/pull/3693)
- Fix NPE in GUI preview when machine model is replaced by a resource pack by @FakeDomi in [#3786](https://github.com/GregTechCEu/GregTech-Modern/pull/3786)
- Fix rock breaker recipe condition by @TarLaboratories in [#3804](https://github.com/GregTechCEu/GregTech-Modern/pull/3804)
- Set default fluids to the rock breaker recipes by @YoungOnionMC in [#3805](https://github.com/GregTechCEu/GregTech-Modern/pull/3805)
- Added translate key to tooltips changing machine mode by @Taskeren in [#3795](https://github.com/GregTechCEu/GregTech-Modern/pull/3795)
- Fix #3792 Multi-mode machines won't check recipe after mode changes by @Taskeren in [#3794](https://github.com/GregTechCEu/GregTech-Modern/pull/3794)
- Fix Robot Arms ignoring Keep Exact behavior when taking input from a Pipe by @DilithiumThoride in [#3812](https://github.com/GregTechCEu/GregTech-Modern/pull/3812)
- More vanilla recipe helpers by @jurrejelle in [#3814](https://github.com/GregTechCEu/GregTech-Modern/pull/3814)
- Render Docs and Update Docs by @jurrejelle in [#3782](https://github.com/GregTechCEu/GregTech-Modern/pull/3782)
- Make deserialisation registry-aware by @jurrejelle in [#3815](https://github.com/GregTechCEu/GregTech-Modern/pull/3815)
- Fix Tree Felling happening for Logged off Players by @alegian in [#3806](https://github.com/GregTechCEu/GregTech-Modern/pull/3806)
- Fix ender link cover textures and translations by @TarLaboratories in [#3819](https://github.com/GregTechCEu/GregTech-Modern/pull/3819)
- Fixed potassium feldspar formula by @TarLaboratories in [#3823](https://github.com/GregTechCEu/GregTech-Modern/pull/3823)
- Research Data Holder no longer voids recipes by @jurrejelle in [#3826](https://github.com/GregTechCEu/GregTech-Modern/pull/3826)
- Update HPCA Render system by @jurrejelle in [#3803](https://github.com/GregTechCEu/GregTech-Modern/pull/3803)
- Fix bug in combine placeholder by @TarLaboratories in [#3840](https://github.com/GregTechCEu/GregTech-Modern/pull/3840)
- fix offsets bugging out when there are multiple faces passed to a FluidAreaRender by @NegaNote in [#3858](https://github.com/GregTechCEu/GregTech-Modern/pull/3858)
- Fixed Shaped Recipes for Warning Signs by @Atmudia in [#3864](https://github.com/GregTechCEu/GregTech-Modern/pull/3864)
- parity between centrifuging oilsands dust and oilsands ore by @Zoryn4163 in [#3878](https://github.com/GregTechCEu/GregTech-Modern/pull/3878)
- Fix Jade not reporting disabled after current cycle by @DilithiumThoride in [#3879](https://github.com/GregTechCEu/GregTech-Modern/pull/3879)
- Fix tooltip claiming item/fluid pipes were modified with wire cutters by @DilithiumThoride in [#3844](https://github.com/GregTechCEu/GregTech-Modern/pull/3844)
- Fix Conveyor cover Round Robin with Restriction mode by @DilithiumThoride in [#3855](https://github.com/GregTechCEu/GregTech-Modern/pull/3855)
- Add missing particles to item/fluid passthrough and laser hatches by @jtuc in [#3890](https://github.com/GregTechCEu/GregTech-Modern/pull/3890)
- Chainsaw/crowbar model fixes by @jtuc in [#3894](https://github.com/GregTechCEu/GregTech-Modern/pull/3894)
- Fix maintenance not happening as often as intended by @serenibyss in [#3740](https://github.com/GregTechCEu/GregTech-Modern/pull/3740)
- Redo of 3744 by @YoungOnionMC in [#3873](https://github.com/GregTechCEu/GregTech-Modern/pull/3873)
- Order EMI Machine List manually by @jurrejelle in [#3902](https://github.com/GregTechCEu/GregTech-Modern/pull/3902)
- Probably fixed npe in raytrace #3891 by @Taskeren in [#3904](https://github.com/GregTechCEu/GregTech-Modern/pull/3904)
- Set fluid block map colors from material color by @jtuc in [#3911](https://github.com/GregTechCEu/GregTech-Modern/pull/3911)
- Fix Ender Redstone Link Covers on non-default channels by @jtuc in [#3918](https://github.com/GregTechCEu/GregTech-Modern/pull/3918)
- Fixed missing animation using duct tape by @Taskeren in [#3920](https://github.com/GregTechCEu/GregTech-Modern/pull/3920)
- Allow only 1 item in a turbo charger slot by @TarLaboratories in [#3922](https://github.com/GregTechCEu/GregTech-Modern/pull/3922)
- fix calcium carbonate formula by @arsdragonfly in [#3931](https://github.com/GregTechCEu/GregTech-Modern/pull/3931)
- broadcast item break event to client side for sound effect by @arsdragonfly in [#3933](https://github.com/GregTechCEu/GregTech-Modern/pull/3933)
- Fix GTValues.VHA[LV] value by @jurrejelle in [#3914](https://github.com/GregTechCEu/GregTech-Modern/pull/3914)
- Remove tier-based chance boosting from Bauxite Slag by @DilithiumThoride in [#3948](https://github.com/GregTechCEu/GregTech-Modern/pull/3948)

### Changed

- Implement tool action support & Fix default enchantments being removable by @screret in [#3582](https://github.com/GregTechCEu/GregTech-Modern/pull/3582)
- Refactor the rock breaker recipe condition by @screret in [#3591](https://github.com/GregTechCEu/GregTech-Modern/pull/3591)
- Make HPCA coolant a tag instead of a fluid by @jurrejelle in [#3779](https://github.com/GregTechCEu/GregTech-Modern/pull/3779)
- Update zh_cn.json 1.20 by @iouter in [#3821](https://github.com/GregTechCEu/GregTech-Modern/pull/3821)

 
## Version [v7.1.4](https://github.com/GregTechCEu/GregTech-Modern/compare/v7.1.3-1.20.1...v7.1.4-1.20.1)
### Fixed

- Fix Machine power failing by @YoungOnionMC in [#3680](https://github.com/GregTechCEu/GregTech-Modern/pull/3680)

 
## Version [v7.1.3](https://github.com/GregTechCEu/GregTech-Modern/compare/v7.1.2-1.20.1...v7.1.3-1.20.1)
### Added

- Make Parallel calculations use longs for stack amounts by @jurrejelle in [#3650](https://github.com/GregTechCEu/GregTech-Modern/pull/3650)
- make multiblock maintenance time configurable by @DilithiumThoride in [#3652](https://github.com/GregTechCEu/GregTech-Modern/pull/3652)
- make the fe-to-eu and eu-to-fe ratios max at maxint by @NegaNote in [#3656](https://github.com/GregTechCEu/GregTech-Modern/pull/3656)

### Fixed

- 335,544.32% recipe logic improvement by @YoungOnionMC in [#3645](https://github.com/GregTechCEu/GregTech-Modern/pull/3645)
- Fix diodes not updating their model on sync by @DilithiumThoride in [#3651](https://github.com/GregTechCEu/GregTech-Modern/pull/3651)
- Fix recipe viewer integration not working with research recipes that contain fluids by @Ghostipedia in [#3655](https://github.com/GregTechCEu/GregTech-Modern/pull/3655)
- Fix Parallel logic calculating the required amount wrong when there are split ingredients by @YoungOnionMC in [#3658](https://github.com/GregTechCEu/GregTech-Modern/pull/3658)

### Changed

- Optimize Network Switch for repeat CWU/t requests by @serenibyss in [#3654](https://github.com/GregTechCEu/GregTech-Modern/pull/3654)

 
## Version [v7.0.0](https://github.com/GregTechCEu/GregTech-Modern/compare/v1.6.4-1.20.1...v7.0.0-1.20.1)
### Added

- Add more plasmas and plasma turbine fuels  by @omergunr100 in [#2974](https://github.com/GregTechCEu/GregTech-Modern/pull/2974)
- Send chat message when new ore veins are prospected by @JuiceyBeans in [#2902](https://github.com/GregTechCEu/GregTech-Modern/pull/2902)
- GameStages Recipe Conditions by @YoungOnionMC in [#2900](https://github.com/GregTechCEu/GregTech-Modern/pull/2900)
- Add FTB Quests recipe condition by @JuiceyBeans in [#2895](https://github.com/GregTechCEu/GregTech-Modern/pull/2895)
- Various Pipe Model changes by @YoungOnionMC in [#2984](https://github.com/GregTechCEu/GregTech-Modern/pull/2984)
- Add Heracles recipe condition by @JuiceyBeans in [#2904](https://github.com/GregTechCEu/GregTech-Modern/pull/2904)
- Add KJS Shaped Recipe Schema with GT Tool Symbols by @krossgg in [#3041](https://github.com/GregTechCEu/GregTech-Modern/pull/3041)
- Add recipes for crushing corals into calcite by @Miner239 in [#3087](https://github.com/GregTechCEu/GregTech-Modern/pull/3087)
- Add charge line to armor tooltip by @stivosha in [#3096](https://github.com/GregTechCEu/GregTech-Modern/pull/3096)
- Add special behaviors to the Large Maceration Tower by @YoungOnionMC in [#2968](https://github.com/GregTechCEu/GregTech-Modern/pull/2968)
- Add JADE info for the conversion mode of the Energy Converter by @NegaNote in [#3099](https://github.com/GregTechCEu/GregTech-Modern/pull/3099)
- Port Bauxite Processing from 1.12 by @GirixK in [#3126](https://github.com/GregTechCEu/GregTech-Modern/pull/3126)
- Remove Extruder Mold (Long Rod) by @GirixK in [#3190](https://github.com/GregTechCEu/GregTech-Modern/pull/3190)
- implement real XOR chance logic (replaces old, which is renamed FIRST) by @NegaNote in [#3187](https://github.com/GregTechCEu/GregTech-Modern/pull/3187)
- Add a block tag for surface indicators by @YoungOnionMC in [#3151](https://github.com/GregTechCEu/GregTech-Modern/pull/3151)
- Fix Electronic Circuit Recipe by @cewlboi in [#3133](https://github.com/GregTechCEu/GregTech-Modern/pull/3133)
- Adds Fluid Stack capability to Research Entries by @YoungOnionMC in [#3106](https://github.com/GregTechCEu/GregTech-Modern/pull/3106)
- Remove muffler hatch requirement from Large Brewing Vat by @GirixK in [#3231](https://github.com/GregTechCEu/GregTech-Modern/pull/3231)
- Add the Ender Fluid Link cover by @Arborsm in [#3024](https://github.com/GregTechCEu/GregTech-Modern/pull/3024)
- Make screwdriver able to invert buses by @jurrejelle in [#3235](https://github.com/GregTechCEu/GregTech-Modern/pull/3235)
- Add pattern dimensions to all Multiblock Controllers by @YoungOnionMC in [#3240](https://github.com/GregTechCEu/GregTech-Modern/pull/3240)
- Make tank widgets able to display the contents of the tank in the tooltip separately from the overlay by @GirixK in [#3221](https://github.com/GregTechCEu/GregTech-Modern/pull/3221)
- Add API for using custom blocks/items in es by @screret in [#3251](https://github.com/GregTechCEu/GregTech-Modern/pull/3251)
- Add Capes by @screret in [#3242](https://github.com/GregTechCEu/GregTech-Modern/pull/3242)
- Add recipes to oxidise and wax copper decorative blocks in the chem bath by @htmlcsjs in [#3263](https://github.com/GregTechCEu/GregTech-Modern/pull/3263)
- Add a tag for valid cleanroom doors by @jurrejelle in [#3264](https://github.com/GregTechCEu/GregTech-Modern/pull/3264)
- Feature: colour machine/cable highlights based on tier by @omergunr100 in [#3067](https://github.com/GregTechCEu/GregTech-Modern/pull/3067)
- add material armors by @screret in [#2656](https://github.com/GregTechCEu/GregTech-Modern/pull/2656)
- Batch Mode by @YoungOnionMC in [#3292](https://github.com/GregTechCEu/GregTech-Modern/pull/3292)
- Add color based input seperation by @jurrejelle in [#3237](https://github.com/GregTechCEu/GregTech-Modern/pull/3237)
- Add Visual Overlay for Colored Hatches by @YoungOnionMC in [#3313](https://github.com/GregTechCEu/GregTech-Modern/pull/3313)
- Use Super and Quantum Tanks as cells by @jurrejelle in [#3305](https://github.com/GregTechCEu/GregTech-Modern/pull/3305)
- Add initial computercraft support by @MatthiasMann in [#3310](https://github.com/GregTechCEu/GregTech-Modern/pull/3310)
- add CC support for getting a machine's recipe progress and turning them on/off by @MatthiasMann in [#3323](https://github.com/GregTechCEu/GregTech-Modern/pull/3323)
- Multi Amp Recipes by @YoungOnionMC in [#3299](https://github.com/GregTechCEu/GregTech-Modern/pull/3299)
- Implement Ranged Fluid Outputs (V3) by @DilithiumThoride in [#3269](https://github.com/GregTechCEu/GregTech-Modern/pull/3269)
- Clean Up EMI GUI for MultiAmp by @Ghostipedia in [#3384](https://github.com/GregTechCEu/GregTech-Modern/pull/3384)

### Fixed

- Fix advanced armor unlocalized name by @YoungOnionMC in [#2962](https://github.com/GregTechCEu/GregTech-Modern/pull/2962)
- Fix harder paper recipes consuming fluid container by @Spicierspace153 in [#2963](https://github.com/GregTechCEu/GregTech-Modern/pull/2963)
- Fix NPE in AssemblyLineMachine by @YoungOnionMC in [#2983](https://github.com/GregTechCEu/GregTech-Modern/pull/2983)
- Fix fertilizer interaction by @omergunr100 in [#2937](https://github.com/GregTechCEu/GregTech-Modern/pull/2937)
- Fix deepslate/tuff ore sounds by @Pyritie in [#2967](https://github.com/GregTechCEu/GregTech-Modern/pull/2967)
- Fix various issues with Long Distance Pipelines by @omergunr100 in [#2938](https://github.com/GregTechCEu/GregTech-Modern/pull/2938)
- Fix cover overlays to prevent Z-fighting by @omergunr100 in [#2976](https://github.com/GregTechCEu/GregTech-Modern/pull/2976)
- Fix Miner Pipe block breaking logic by @omergunr100 in [#2854](https://github.com/GregTechCEu/GregTech-Modern/pull/2854)
- Fix cover overlay renderer NPE by @omergunr100 in [#2987](https://github.com/GregTechCEu/GregTech-Modern/pull/2987)
- Fix ItemBusPartMachine incorrectly disabling circuit slot by @krossgg in [#2991](https://github.com/GregTechCEu/GregTech-Modern/pull/2991)
- Fix not being able to interact with prospector UI when no map mods were installed by @Pumpkin7266 in [#3002](https://github.com/GregTechCEu/GregTech-Modern/pull/3002)
- Fix rotor holder renderer by @omergunr100 in [#2973](https://github.com/GregTechCEu/GregTech-Modern/pull/2973)
- Fix recently connected pipes not transfering fluid/items from machines/busses/hatches by @PrototypeTrousers in [#3014](https://github.com/GregTechCEu/GregTech-Modern/pull/3014)
- Fix NC Items being used in decomp by @YoungOnionMC in [#3018](https://github.com/GregTechCEu/GregTech-Modern/pull/3018)
- Fix Tool charging resetting block break progress by @YoungOnionMC in [#2966](https://github.com/GregTechCEu/GregTech-Modern/pull/2966)
- Fix jade crashing when ae2 isn't installed by @omergunr100 in [#3019](https://github.com/GregTechCEu/GregTech-Modern/pull/3019)
- Fix  consuming inputs when matching recipe by @Ghostipedia in [#3028](https://github.com/GregTechCEu/GregTech-Modern/pull/3028)
- Fix  tags and incorrect lang by @krossgg in [#3030](https://github.com/GregTechCEu/GregTech-Modern/pull/3030)
- Fix crafting component issues by @krossgg in [#3037](https://github.com/GregTechCEu/GregTech-Modern/pull/3037)
- Fix turbines not being put into creative tab by @krossgg in [#3042](https://github.com/GregTechCEu/GregTech-Modern/pull/3042)
- Fix arm not swinging for certain wrench interactions by @YoungOnionMC in [#3032](https://github.com/GregTechCEu/GregTech-Modern/pull/3032)
- Fix Pyrolyse Oven double tooltip by @GirixK in [#3045](https://github.com/GregTechCEu/GregTech-Modern/pull/3045)
- Fix various divide by zero errors in material class by @YoungOnionMC in [#3048](https://github.com/GregTechCEu/GregTech-Modern/pull/3048)
- Fix Prussian Blue Voiding 75% of Input Chlorine by @Ghostipedia in [#3053](https://github.com/GregTechCEu/GregTech-Modern/pull/3053)
- Fix a bunch of visual bugs by @omergunr100 in [#2988](https://github.com/GregTechCEu/GregTech-Modern/pull/2988)
- fix KJS machine recipes erroring if user added multiple recipe conditions of different types by @screret in [#3074](https://github.com/GregTechCEu/GregTech-Modern/pull/3074)
- Fix LCE Obstruction when placed vertically by @YoungOnionMC in [#3062](https://github.com/GregTechCEu/GregTech-Modern/pull/3062)
- Fix  issues and add QOL by @YoungOnionMC in [#3049](https://github.com/GregTechCEu/GregTech-Modern/pull/3049)
- Fix GT Tool issue with EMI Crafting Fill by @krossgg in [#3043](https://github.com/GregTechCEu/GregTech-Modern/pull/3043)
- Fix cover slot by @omergunr100 in [#3068](https://github.com/GregTechCEu/GregTech-Modern/pull/3068)
- Fix Coke Oven Hatches getting stuck after Coke Oven gets backfilled by @PrototypeTrousers in [#3059](https://github.com/GregTechCEu/GregTech-Modern/pull/3059)
- Fix distinctness not being preserved by @omergunr100 in [#3016](https://github.com/GregTechCEu/GregTech-Modern/pull/3016)
- FIx PVA Recipe to only be in the LCR by @kdcjxbsdnbgfdg in [#3051](https://github.com/GregTechCEu/GregTech-Modern/pull/3051)
- Fix steam multiblocks failing to open UI by @nutant233 in [#3021](https://github.com/GregTechCEu/GregTech-Modern/pull/3021)
- Fix EU to FE conversion by using actually transferred energy amount by @rischiopoco in [#2969](https://github.com/GregTechCEu/GregTech-Modern/pull/2969)
- Fix issues with StoneTypeEntry and variant ores by @krossgg in [#3075](https://github.com/GregTechCEu/GregTech-Modern/pull/3075)
- Fixed corrupted texture in Distillation Tower recipe. by @nutant233 in [#3083](https://github.com/GregTechCEu/GregTech-Modern/pull/3083)
- Fix recipe naming by @vdegans in [#3076](https://github.com/GregTechCEu/GregTech-Modern/pull/3076)
- Rename palladium substation -> palladium substation casing by @JuiceyBeans in [#3069](https://github.com/GregTechCEu/GregTech-Modern/pull/3069)
- Fix typo in recipe name by @a-a-GiTHuB-a-a in [#3027](https://github.com/GregTechCEu/GregTech-Modern/pull/3027)
- Refactor ItemMaterialData and fix addon recipes load order by @nutant233 in [#3031](https://github.com/GregTechCEu/GregTech-Modern/pull/3031)
- Limit the rate of mob crushing by @YoungOnionMC in [#3127](https://github.com/GregTechCEu/GregTech-Modern/pull/3127)
- Fix Multiblock Pattern slot widgets not showing tooltips by @krossgg in [#3125](https://github.com/GregTechCEu/GregTech-Modern/pull/3125)
- Fix Cleanroom Tier Bug by @YoungOnionMC in [#3152](https://github.com/GregTechCEu/GregTech-Modern/pull/3152)
- Fix only multiblocks autogenerating the language values with KubeJS by @screret in [#3203](https://github.com/GregTechCEu/GregTech-Modern/pull/3203)
- Fix first hit on a Quantum Chest always inserting all items by @screret in [#3204](https://github.com/GregTechCEu/GregTech-Modern/pull/3204)
- fix all miners having some level of fortune unintentionally by @screret in [#3200](https://github.com/GregTechCEu/GregTech-Modern/pull/3200)
- remove unnecessary nbt clearing from custom item fluid handlers by @screret in [#3201](https://github.com/GregTechCEu/GregTech-Modern/pull/3201)
- Fixed large boiler explodes when loading the world by @nutant233 in [#3022](https://github.com/GregTechCEu/GregTech-Modern/pull/3022)
- Fix Large Boilers exploding when throttle is too low by @Ricky-fight in [#3064](https://github.com/GregTechCEu/GregTech-Modern/pull/3064)
- Change Substation Casing color to match Palladium's color by @GirixK in [#3227](https://github.com/GregTechCEu/GregTech-Modern/pull/3227)
- Make thorium and magnesite ores obtainable again by @BoomerBoxer in [#3234](https://github.com/GregTechCEu/GregTech-Modern/pull/3234)
- Fix the lighter model referencing a nonexistent texture by @JuiceyBeans in [#3257](https://github.com/GregTechCEu/GregTech-Modern/pull/3257)
- Add back isSameAxis() to RelativeDirection by @NegaNote in [#3260](https://github.com/GregTechCEu/GregTech-Modern/pull/3260)
- Fix KJS recipe types' slot overlays not working by @JuiceyBeans in [#3258](https://github.com/GregTechCEu/GregTech-Modern/pull/3258)
- Remove unused shader by @GirixK in [#3262](https://github.com/GregTechCEu/GregTech-Modern/pull/3262)
- Fix GTRecipeBuilder ranged item outputs by @DilithiumThoride in [#3268](https://github.com/GregTechCEu/GregTech-Modern/pull/3268)
- Fix Multiblock Tanks functioning when unformed by @nutant233 in [#3271](https://github.com/GregTechCEu/GregTech-Modern/pull/3271)
- fix magnetic tools crashing the server when  is immutable by @Spicierspace153 in [#3288](https://github.com/GregTechCEu/GregTech-Modern/pull/3288)
- Fix cover behavior on busses and hatches by @jurrejelle in [#3287](https://github.com/GregTechCEu/GregTech-Modern/pull/3287)
- Fix Recipe Runner Error Reporting by @YoungOnionMC in [#3296](https://github.com/GregTechCEu/GregTech-Modern/pull/3296)
- Clarify GTRecipeBuilder's error messages and fix the issues #3268 had by @screret in [#3293](https://github.com/GregTechCEu/GregTech-Modern/pull/3293)
- fix KubeJS machine recipes crashing if multiple conditions of different types are added by @screret in [#3302](https://github.com/GregTechCEu/GregTech-Modern/pull/3302)
- fix research holder items crashing if the research recipe's only output is a fluid by @screret in [#3301](https://github.com/GregTechCEu/GregTech-Modern/pull/3301)
- Fix Creative Energy Container not having an outline by @YoungOnionMC in [#3309](https://github.com/GregTechCEu/GregTech-Modern/pull/3309)
- fix per-tick CWU outputs being handled as negative values by @screret in [#3311](https://github.com/GregTechCEu/GregTech-Modern/pull/3311)
- Fix armor charging causing a crash if the offhand item is switched repeatedly by @PSR2144-3933 in [#3322](https://github.com/GregTechCEu/GregTech-Modern/pull/3322)
- Remove shouldSearchContent by @jurrejelle in [#3334](https://github.com/GregTechCEu/GregTech-Modern/pull/3334)
- Fix BedrockFluidDefinition losing data on world load by @MatthiasMann in [#3336](https://github.com/GregTechCEu/GregTech-Modern/pull/3336)
- Fix batch mode showing in the wrong multis by @Pumpkin7266 in [#3345](https://github.com/GregTechCEu/GregTech-Modern/pull/3345)
- Fix the quantum tank fluid render being offset in the inventory by @screret in [#3372](https://github.com/GregTechCEu/GregTech-Modern/pull/3372)
- Fix LCR recipemap tooltips by @Spicierspace153 in [#3373](https://github.com/GregTechCEu/GregTech-Modern/pull/3373)
- add the other standard tags to dough and wheat by @screret in [#3367](https://github.com/GregTechCEu/GregTech-Modern/pull/3367)
- Clean up unused and/or useless mixins by @screret in [#3359](https://github.com/GregTechCEu/GregTech-Modern/pull/3359)
- Fix Material Decomp being wrong for some recipes by @YoungOnionMC in [#3382](https://github.com/GregTechCEu/GregTech-Modern/pull/3382)
- Fix facades coloring grass etc. with the underlying block's color by @screret in [#3392](https://github.com/GregTechCEu/GregTech-Modern/pull/3392)
- Fix Jade Structure forming info by @YoungOnionMC in [#3400](https://github.com/GregTechCEu/GregTech-Modern/pull/3400)
- Fix ModernFix dynamic resources compatibility by @screret in [#3409](https://github.com/GregTechCEu/GregTech-Modern/pull/3409)
- Fix Batch Mode chance rolls by @YoungOnionMC in [#3411](https://github.com/GregTechCEu/GregTech-Modern/pull/3411)

### Changed

- Adds onEntitySwing to IinteractionItem for left click interactions by @Spicierspace153 in [#2930](https://github.com/GregTechCEu/GregTech-Modern/pull/2930)
- Change environmental hazards config to off by default by @omergunr100 in [#2978](https://github.com/GregTechCEu/GregTech-Modern/pull/2978)
- Update zh_cn.json by @iouter in [#2929](https://github.com/GregTechCEu/GregTech-Modern/pull/2929)
- Update zh_tw.json by @IamNotBrianZ in [#2934](https://github.com/GregTechCEu/GregTech-Modern/pull/2934)
- Refactor TagKey creations calls to use static constants instead by @JuiceyBeans in [#2970](https://github.com/GregTechCEu/GregTech-Modern/pull/2970)
- Allow multiblocks to disable circuit slots on parts by @omergunr100 in [#2960](https://github.com/GregTechCEu/GregTech-Modern/pull/2960)
- Refactor JourneyMap integration by @omergunr100 in [#2877](https://github.com/GregTechCEu/GregTech-Modern/pull/2877)
- Change error logging for KJS recipes and add missing builder method by @omergunr100 in [#2977](https://github.com/GregTechCEu/GregTech-Modern/pull/2977)
- Refactor misc by @omergunr100 in [#2958](https://github.com/GregTechCEu/GregTech-Modern/pull/2958)
- Change Singleblock Miner to have an output face by @omergunr100 in [#2910](https://github.com/GregTechCEu/GregTech-Modern/pull/2910)
- Add GT tool specific tags for crafting  by @GirixK in [#2810](https://github.com/GregTechCEu/GregTech-Modern/pull/2810)
- Fix Fluid Cell material properties by @YoungOnionMC in [#2927](https://github.com/GregTechCEu/GregTech-Modern/pull/2927)
- Change  tag to be plural by @omergunr100 in [#2990](https://github.com/GregTechCEu/GregTech-Modern/pull/2990)
- Change electric Wire Cutter IDs to be consistent with the unpowered version by @GirixK in [#2782](https://github.com/GregTechCEu/GregTech-Modern/pull/2782)
- Removes some item bloat by @YoungOnionMC in [#2994](https://github.com/GregTechCEu/GregTech-Modern/pull/2994)
- Refactor Crafting Components by @krossgg in [#2713](https://github.com/GregTechCEu/GregTech-Modern/pull/2713)
- Refactor Item Decomposition information system by @YoungOnionMC in [#2591](https://github.com/GregTechCEu/GregTech-Modern/pull/2591)
- Improve TagPrefix-based Recipe Generation by @TechLord22 in [#2616](https://github.com/GregTechCEu/GregTech-Modern/pull/2616)
- Refactor machine ownership to use player uuid by @omergunr100 in [#2905](https://github.com/GregTechCEu/GregTech-Modern/pull/2905)
- Distinct Fluid Behaviour and Recipe Runner Refactor by @YoungOnionMC in [#2544](https://github.com/GregTechCEu/GregTech-Modern/pull/2544)
- remove brine process by @screret in [#2634](https://github.com/GregTechCEu/GregTech-Modern/pull/2634)
- Allow Unbreaking and Mending on electric tools by @omergunr100 in [#2985](https://github.com/GregTechCEu/GregTech-Modern/pull/2985)
- Refactor GTTools ui logic and add tree felling switch for axes by @omergunr100 in [#2857](https://github.com/GregTechCEu/GregTech-Modern/pull/2857)
- Disallow covers on the front of Singleblock Machines by @omergunr100 in [#2830](https://github.com/GregTechCEu/GregTech-Modern/pull/2830)
- Rename  to . by @tomprince in [#2737](https://github.com/GregTechCEu/GregTech-Modern/pull/2737)
- Various Nitpick Changes by @YoungOnionMC in [#2683](https://github.com/GregTechCEu/GregTech-Modern/pull/2683)
- Helper methods for Team Ownership by @YoungOnionMC in [#3017](https://github.com/GregTechCEu/GregTech-Modern/pull/3017)
- Add extended facing to single-block machines by @omergunr100 in [#2823](https://github.com/GregTechCEu/GregTech-Modern/pull/2823)
- Refactor supplier memoizer by @omergunr100 in [#3004](https://github.com/GregTechCEu/GregTech-Modern/pull/3004)
- Improve visuals of Muffler Smoke VFX And Make Respect Particle Options by @Ghostipedia in [#3061](https://github.com/GregTechCEu/GregTech-Modern/pull/3061)
- Refactor ItemMaterialData#registerMaterialInfoItems to not use var-args by @TechLord22 in [#3057](https://github.com/GregTechCEu/GregTech-Modern/pull/3057)
- delete unused MufflerParticleOptions class by @screret in [#3073](https://github.com/GregTechCEu/GregTech-Modern/pull/3073)
- Refactor torch placement behaviour to check if offhand is empty by @JuiceyBeans in [#3047](https://github.com/GregTechCEu/GregTech-Modern/pull/3047)
- Refactor item decorators to item decorators instead of using a mixin by @omergunr100 in [#3079](https://github.com/GregTechCEu/GregTech-Modern/pull/3079)
- Refactor magnet recipes to have an empty blacklist filter by @omergunr100 in [#3066](https://github.com/GregTechCEu/GregTech-Modern/pull/3066)
- Refactor TOP / Jade Energy Provider to handle BigInteger by @mrquentin in [#3071](https://github.com/GregTechCEu/GregTech-Modern/pull/3071)
- Change steam & bronze machine casing russian translations by @vdegans in [#3077](https://github.com/GregTechCEu/GregTech-Modern/pull/3077)
- Refactor ItemMaterialData and fix addon recipes load order by @nutant233 in [#3031](https://github.com/GregTechCEu/GregTech-Modern/pull/3031)
- Clean up tag generation by @screret in [#3093](https://github.com/GregTechCEu/GregTech-Modern/pull/3093)
- Update zh_cn.json by @iouter in [#3104](https://github.com/GregTechCEu/GregTech-Modern/pull/3104)
- Add BigInteger support to Energy Detector Covers by @mrquentin in [#3081](https://github.com/GregTechCEu/GregTech-Modern/pull/3081)
- Translated using Weblate (Russian) for 1.20.1 by @marisathewitch in [#3145](https://github.com/GregTechCEu/GregTech-Modern/pull/3145)
- Remove  block property by @krossgg in [#3135](https://github.com/GregTechCEu/GregTech-Modern/pull/3135)
- Update zh_cn.json by @iouter in [#3143](https://github.com/GregTechCEu/GregTech-Modern/pull/3143)
- Update zh_tw.json (1.20.1) by @IamNotBrianZ in [#3166](https://github.com/GregTechCEu/GregTech-Modern/pull/3166)
- Remove redundant headers by @YoungOnionMC in [#3156](https://github.com/GregTechCEu/GregTech-Modern/pull/3156)
- Reduce auto boxing/unboxing by @krossgg in [#3124](https://github.com/GregTechCEu/GregTech-Modern/pull/3124)
- Removes the overclock configurator by @YoungOnionMC in [#3183](https://github.com/GregTechCEu/GregTech-Modern/pull/3183)
- Add a struct for research data to replace the generic pair by @screret in [#3209](https://github.com/GregTechCEu/GregTech-Modern/pull/3209)
- Remove Armor Mixin for damaging armor and use Forge methods instead by @screret in [#3205](https://github.com/GregTechCEu/GregTech-Modern/pull/3205)
- backport a better version of  by @screret in [#3206](https://github.com/GregTechCEu/GregTech-Modern/pull/3206)
- Refactor gtceu.universal.x Lang Key by @YoungOnionMC in [#3150](https://github.com/GregTechCEu/GregTech-Modern/pull/3150)
- use  instead of calling  manually by @screret in [#3208](https://github.com/GregTechCEu/GregTech-Modern/pull/3208)
- Update zh_cn.json by @iouter in [#3244](https://github.com/GregTechCEu/GregTech-Modern/pull/3244)
- fix all the lighter and TNT logic being terribly jank. by @screret in [#3202](https://github.com/GregTechCEu/GregTech-Modern/pull/3202)
- Cleanroom Logic & Balance Tweaks by @YoungOnionMC in [#3182](https://github.com/GregTechCEu/GregTech-Modern/pull/3182)
- clean up RelativeDirection and rename its methods to be more consistent with vanilla by @screret in [#3198](https://github.com/GregTechCEu/GregTech-Modern/pull/3198)
- Item Capability initialization cleanup & pre-emptive potion bottle bugfixes by @screret in [#3207](https://github.com/GregTechCEu/GregTech-Modern/pull/3207)
- Sort ME2 stocking busses and hatches by amount by @jurrejelle in [#3259](https://github.com/GregTechCEu/GregTech-Modern/pull/3259)
- Fast ingredient by @nutant233 in [#3270](https://github.com/GregTechCEu/GregTech-Modern/pull/3270)
- Remove the simulated circuit inventory from the Pattern Buffer by @krossgg in [#3285](https://github.com/GregTechCEu/GregTech-Modern/pull/3285)
- Refactor parallel logic in pursuit of TPS by @krossgg in [#3088](https://github.com/GregTechCEu/GregTech-Modern/pull/3088)
- Clean up MapIngredient conversion and backport recipe handler fixes from 1.21 by @screret in [#3273](https://github.com/GregTechCEu/GregTech-Modern/pull/3273)
- clarify CapeRegistry's javadocs and add a KubeJS event for it by @screret in [#3324](https://github.com/GregTechCEu/GregTech-Modern/pull/3324)
- Clean up spray paint color handling by @screret in [#3325](https://github.com/GregTechCEu/GregTech-Modern/pull/3325)
- Stop sending neighbor updates for all active blocks in a multi by @screret in [#3326](https://github.com/GregTechCEu/GregTech-Modern/pull/3326)
- Update zh_tw.json by @IamNotBrianZ in [#3283](https://github.com/GregTechCEu/GregTech-Modern/pull/3283)
- Remove shouldSearchContent by @jurrejelle in [#3334](https://github.com/GregTechCEu/GregTech-Modern/pull/3334)
- Translated using Weblate (Russian) for 1.20.1 by @marisathewitch in [#3282](https://github.com/GregTechCEu/GregTech-Modern/pull/3282)
- Switch networking code to use forge SimpleChannel directly, instead of ldlib networking wrapper by @gustovafing in [#3332](https://github.com/GregTechCEu/GregTech-Modern/pull/3332)
- Remove LDLib renderer usage from machine models & allow them to be modified by resource packs by @screret in [#3275](https://github.com/GregTechCEu/GregTech-Modern/pull/3275)
- Bump GTM version from 1.7.0 to 7.0.0 for next Major Update by @Ghostipedia in [#3164](https://github.com/GregTechCEu/GregTech-Modern/pull/3164)
- Start Deprecation of Chance Boosting and Migrate to New Chance Rates by @WithersChat in [#3026](https://github.com/GregTechCEu/GregTech-Modern/pull/3026)
- Clean up unused and/or useless mixins by @screret in [#3359](https://github.com/GregTechCEu/GregTech-Modern/pull/3359)
- Fix facades coloring grass etc. with the underlying block's color by @screret in [#3392](https://github.com/GregTechCEu/GregTech-Modern/pull/3392)
- Update zh_cn.json 1.20 by @iouter in [#3346](https://github.com/GregTechCEu/GregTech-Modern/pull/3346)

 
## Version [v1.6.4](https://github.com/GregTechCEu/GregTech-Modern/compare/v1.6.3-1.20.1...v1.6.4-1.20.1)
### Added

- Add magnet filters by @omergunr100 in [#2769](https://github.com/GregTechCEu/GregTech-Modern/pull/2769)
- Config option to enable/disable ore indicator generation by @JuiceyBeans in [#2767](https://github.com/GregTechCEu/GregTech-Modern/pull/2767)
- Add assembler recipes for fishing rods by @JuiceyBeans in [#2757](https://github.com/GregTechCEu/GregTech-Modern/pull/2757)
- Add campfire extinguishing behavior to shovels by @JuiceyBeans in [#2740](https://github.com/GregTechCEu/GregTech-Modern/pull/2740)
- Add ability to ignore tag prefixes when building Material by @tomprince in [#2627](https://github.com/GregTechCEu/GregTech-Modern/pull/2627)
- Add log-stripping recipes in the lathe by @a-a-GiTHuB-a-a in [#2775](https://github.com/GregTechCEu/GregTech-Modern/pull/2775)
- Refresh slot textures + change assembler slot texture by @JuiceyBeans in [#2818](https://github.com/GregTechCEu/GregTech-Modern/pull/2818)
- Add Day/night Recipe Condition by @JuiceyBeans in [#2885](https://github.com/GregTechCEu/GregTech-Modern/pull/2885)
- Add   builder for KJS by @krossgg in [#2866](https://github.com/GregTechCEu/GregTech-Modern/pull/2866)

### Fixed

- Filter nitpicks by @omergunr100 in [#2786](https://github.com/GregTechCEu/GregTech-Modern/pull/2786)
- Make steam miners also break miner pipe when broken by @a-a-GiTHuB-a-a in [#2776](https://github.com/GregTechCEu/GregTech-Modern/pull/2776)
- Fix data bank not requiring energy by @kitgxrl in [#2796](https://github.com/GregTechCEu/GregTech-Modern/pull/2796)
- Stop showing Running 1 parallel. in Jade/TOP. by @tomprince in [#2758](https://github.com/GregTechCEu/GregTech-Modern/pull/2758)
- Change circuits for tungsten carbide and titanium carbide by @JuiceyBeans in [#2750](https://github.com/GregTechCEu/GregTech-Modern/pull/2750)
- Fix  explosion logic by @omergunr100 in [#2824](https://github.com/GregTechCEu/GregTech-Modern/pull/2824)
- Add overrides for setFluid and getFluid calls to PotionItem by @loving2 in [#2770](https://github.com/GregTechCEu/GregTech-Modern/pull/2770)
- Implement vanilla getFoodProperties() method by @jtuc in [#2839](https://github.com/GregTechCEu/GregTech-Modern/pull/2839)
- Fix ME hatches crashing on server side by @dnk1234567 in [#2812](https://github.com/GregTechCEu/GregTech-Modern/pull/2812)
- Fix map overlay caches not clearing when they should by @omergunr100 in [#2744](https://github.com/GregTechCEu/GregTech-Modern/pull/2744)
- Fix the rotor holder renderer crashing due to syncing of held item by @omergunr100 in [#2820](https://github.com/GregTechCEu/GregTech-Modern/pull/2820)
- Fix storage cover widget icon by @JuiceyBeans in [#2817](https://github.com/GregTechCEu/GregTech-Modern/pull/2817)
- Fix Machine Controller Cover crashing without anything to control by @omergunr100 in [#2821](https://github.com/GregTechCEu/GregTech-Modern/pull/2821)
- Fix client crashing when failing to send keybinds to the server by @omergunr100 in [#2829](https://github.com/GregTechCEu/GregTech-Modern/pull/2829)
- Fix Log and Stripped Recipes causing NPE by @YoungOnionMC in [#2864](https://github.com/GregTechCEu/GregTech-Modern/pull/2864)
- Fix radaway dust decomposition resource duplication by @omergunr100 in [#2869](https://github.com/GregTechCEu/GregTech-Modern/pull/2869)
- Fix meta machines being valid spawn points by @omergunr100 in [#2872](https://github.com/GregTechCEu/GregTech-Modern/pull/2872)
- Fix fusion reactor renderer logic by @omergunr100 in [#2871](https://github.com/GregTechCEu/GregTech-Modern/pull/2871)
- Fix harmful effects applying for a tick with QuarkTech Helmet by @Irgendwer01 in [#2886](https://github.com/GregTechCEu/GregTech-Modern/pull/2886)
- Fix Charcoal Pile Igniter crash by @MatthiasMann in [#2889](https://github.com/GregTechCEu/GregTech-Modern/pull/2889)
- Fix Fisher Loot button not being persisted by @Echoloquate in [#2884](https://github.com/GregTechCEu/GregTech-Modern/pull/2884)
- Fix Research Machines not respecting Maintenance Config by @krossgg in [#2893](https://github.com/GregTechCEu/GregTech-Modern/pull/2893)
- Fix Text Formatting in Maintenance GUI by @YoungOnionMC in [#2870](https://github.com/GregTechCEu/GregTech-Modern/pull/2870)
- Fix StackOverflowError caused by infinite food recursion by @krossgg in [#2891](https://github.com/GregTechCEu/GregTech-Modern/pull/2891)
- Fix Fusion Reactor Recipe Widget inconsistencies by @krossgg in [#2890](https://github.com/GregTechCEu/GregTech-Modern/pull/2890)
- Enable cover behavior for basic (untiered) solar panels by @jtuc in [#2837](https://github.com/GregTechCEu/GregTech-Modern/pull/2837)
- Make Cleanroom Casings respect the casing amount config. by @YoungOnionMC in [#2918](https://github.com/GregTechCEu/GregTech-Modern/pull/2918)
- Add Comma Formatting to Cable Tooltips by @YoungOnionMC in [#2920](https://github.com/GregTechCEu/GregTech-Modern/pull/2920)
- Fix missing decomp for various Copper Blocks by @YoungOnionMC in [#2916](https://github.com/GregTechCEu/GregTech-Modern/pull/2916)
- Fix temperature for sulfuric light fuel by @JuiceyBeans in [#2914](https://github.com/GregTechCEu/GregTech-Modern/pull/2914)
- Fix ParallelHatchPartMachine starting with 0 parallels by @krossgg in [#2896](https://github.com/GregTechCEu/GregTech-Modern/pull/2896)
- Fix Detector Comparator calculation and add Latching toggle by @krossgg in [#2898](https://github.com/GregTechCEu/GregTech-Modern/pull/2898)
- Fix conflict between Gold Drum and Gold Pressure Plate by @GirixK in [#2865](https://github.com/GregTechCEu/GregTech-Modern/pull/2865)
- Fix nano saber model by @ko-lja in [#2917](https://github.com/GregTechCEu/GregTech-Modern/pull/2917)
- Fix Robot Arm Keep Exact functionality with item pipes by @BryanSer in [#2897](https://github.com/GregTechCEu/GregTech-Modern/pull/2897)
- Fix drums that are not acid proof allows to fill acid by @DiFFoZ in [#2784](https://github.com/GregTechCEu/GregTech-Modern/pull/2784)

### Changed

- Configurable Electric Singleblock miner speed by @eve336 in [#2785](https://github.com/GregTechCEu/GregTech-Modern/pull/2785)
- Rewrite biome coloring to not require rendering mod patches by @embeddedt in [#2773](https://github.com/GregTechCEu/GregTech-Modern/pull/2773)
- Use B instead of kmB in ME Input Hatches. by @tomprince in [#2759](https://github.com/GregTechCEu/GregTech-Modern/pull/2759)
- Restructure Gradle for Dev Efficiency by @krossgg in [#2787](https://github.com/GregTechCEu/GregTech-Modern/pull/2787)
- Lazily construct GTRecipeWrappers in JEI recipe category by @embeddedt in [#2714](https://github.com/GregTechCEu/GregTech-Modern/pull/2714)
- Remove lombok val for var by @mrquentin in [#2802](https://github.com/GregTechCEu/GregTech-Modern/pull/2802)
- Add utils functions to the IMachineOwner interface to ease the retrie… by @mrquentin in [#2801](https://github.com/GregTechCEu/GregTech-Modern/pull/2801)
- Replace some Bucket Item stacks with FluidContainerIngredient by @mrquentin in [#2804](https://github.com/GregTechCEu/GregTech-Modern/pull/2804)
- Allow setting material formula from builder. by @tomprince in [#2594](https://github.com/GregTechCEu/GregTech-Modern/pull/2594)
- Refactor fusion reactor renderer by @omergunr100 in [#2855](https://github.com/GregTechCEu/GregTech-Modern/pull/2855)
- Fix ME machine copy-paste tooltip by @jtuc in [#2838](https://github.com/GregTechCEu/GregTech-Modern/pull/2838)
- Update ru_ru by @marisathewitch in [#2873](https://github.com/GregTechCEu/GregTech-Modern/pull/2873)
- Add hardRedstoneRecipe for target block by @JuiceyBeans in [#2888](https://github.com/GregTechCEu/GregTech-Modern/pull/2888)
- Update zh_cn by @iouter in [#2863](https://github.com/GregTechCEu/GregTech-Modern/pull/2863)
- Add color to Cable Tooltips by @YoungOnionMC in [#2911](https://github.com/GregTechCEu/GregTech-Modern/pull/2911)
- Add Getter methods to BatteryMatchWrapper by @mrquentin in [#2903](https://github.com/GregTechCEu/GregTech-Modern/pull/2903)
- update ja_jp.json by @code-onigiri in [#2922](https://github.com/GregTechCEu/GregTech-Modern/pull/2922)
- Fix prospector waypoint message showing when no waypoint created by @Pumpkin7266 in [#2908](https://github.com/GregTechCEu/GregTech-Modern/pull/2908)
- Change Muffler hatch particles to campfire smoke by @JuiceyBeans in [#2913](https://github.com/GregTechCEu/GregTech-Modern/pull/2913)

 
## Version [v1.6.3](https://github.com/GregTechCEu/GregTech-Modern/compare/v1.6.2-1.20.1...v1.6.3-1.20.1)
### Added

- Add durability-based decomposition to Rotors and Tools by @YoungOnionMC in [#2623](https://github.com/GregTechCEu/GregTech-Modern/pull/2623)
- Add FTB Chunks ore prospector compatibility by @omergunr100 in [#2642](https://github.com/GregTechCEu/GregTech-Modern/pull/2642)
- Add Tooltips to Cells/Vials by @YoungOnionMC in [#2682](https://github.com/GregTechCEu/GregTech-Modern/pull/2682)
- Add tooltips to Ores on Xaero's World Map by @jtuc in [#2687](https://github.com/GregTechCEu/GregTech-Modern/pull/2687)
- Add ability for Nano/Quark boots to walk on powder snow 🥶 by @krossgg in [#2712](https://github.com/GregTechCEu/GregTech-Modern/pull/2712)
- Add various GUI changes relating to Parallel Recipes by @krossgg in [#2719](https://github.com/GregTechCEu/GregTech-Modern/pull/2719)
- Add freeze immunity to QuarkTech Chestplate by @krossgg in [#2728](https://github.com/GregTechCEu/GregTech-Modern/pull/2728)
- Add sign and sheep dying capability to Chemical Dyes by @krossgg in [#2730](https://github.com/GregTechCEu/GregTech-Modern/pull/2730)

### Fixed

- Fix tree felling helper memory leak by @omergunr100 in [#2677](https://github.com/GregTechCEu/GregTech-Modern/pull/2677)
- Fix AIOOBE Cleanroom crash by @YoungOnionMC in [#2678](https://github.com/GregTechCEu/GregTech-Modern/pull/2678)
- Add red sand to glass arc furnace recipe by @YoungOnionMC in [#2679](https://github.com/GregTechCEu/GregTech-Modern/pull/2679)
- Fix various Stone Type Entry Material issues by @YoungOnionMC in [#2680](https://github.com/GregTechCEu/GregTech-Modern/pull/2680)
- Fix Rotor Holder Lighting by @YoungOnionMC in [#2681](https://github.com/GregTechCEu/GregTech-Modern/pull/2681)
- Fix prospecting inconsistencies by @omergunr100 in [#2688](https://github.com/GregTechCEu/GregTech-Modern/pull/2688)
- Fix EMI and REI loaded checks by @krossgg in [#2709](https://github.com/GregTechCEu/GregTech-Modern/pull/2709)
- Fix missing spaces in stone recipes by @Electrolyte220 in [#2705](https://github.com/GregTechCEu/GregTech-Modern/pull/2705)
- Fix cleanrooms not allowing 4 full doors in structure by @FakeDomi in [#2717](https://github.com/GregTechCEu/GregTech-Modern/pull/2717)
- Fix rotors not being distinguishable by recipe viewers by @krossgg in [#2710](https://github.com/GregTechCEu/GregTech-Modern/pull/2710)
- Fix Voiding Cover + Soft Mallet interaction by @krossgg in [#2721](https://github.com/GregTechCEu/GregTech-Modern/pull/2721)
- Fix empty bucket showing fluid tooltip by @krossgg in [#2724](https://github.com/GregTechCEu/GregTech-Modern/pull/2724)
- Fix charcoal pile igniter pattern by @krossgg in [#2725](https://github.com/GregTechCEu/GregTech-Modern/pull/2725)
- Fix various steam miner issues by @krossgg in [#2729](https://github.com/GregTechCEu/GregTech-Modern/pull/2729)
- Fix solvent spray can not actually removing paint color by @tomprince in [#2732](https://github.com/GregTechCEu/GregTech-Modern/pull/2732)
- Fix miner duplication for self-dropping blocks by @krossgg in [#2734](https://github.com/GregTechCEu/GregTech-Modern/pull/2734)

### Changed

- Crafting Component Refactor by @YoungOnionMC in [#2652](https://github.com/GregTechCEu/GregTech-Modern/pull/2652)
- Optimize ParallelLogic loops slightly by @jtuc in [#2668](https://github.com/GregTechCEu/GregTech-Modern/pull/2668)
- Refactor LDLib calls by @omergunr100 in [#2609](https://github.com/GregTechCEu/GregTech-Modern/pull/2609)
- Optimize MultiPart and ParallelHatch performance by @krossgg in [#2684](https://github.com/GregTechCEu/GregTech-Modern/pull/2684)
- Change RTM coil texture and mcmeta files by @chemlzh in [#2691](https://github.com/GregTechCEu/GregTech-Modern/pull/2691)
- Update zh_cn.json by @iouter in [#2707](https://github.com/GregTechCEu/GregTech-Modern/pull/2707)
- Update ru_ru by @marisathewitch in [#2701](https://github.com/GregTechCEu/GregTech-Modern/pull/2701)
- Use  for Breadth First Search. by @tomprince in [#2697](https://github.com/GregTechCEu/GregTech-Modern/pull/2697)
- Allow Maintenance ability on layer 2 of DT by @krossgg in [#2711](https://github.com/GregTechCEu/GregTech-Modern/pull/2711)
- Mark required research data as a catalyst in XEI. by @tomprince in [#2718](https://github.com/GregTechCEu/GregTech-Modern/pull/2718)
- Change various Steam Bus elements by @krossgg in [#2722](https://github.com/GregTechCEu/GregTech-Modern/pull/2722)
- Change intermediate products in Ore Processing Page to BOTH by @krossgg in [#2723](https://github.com/GregTechCEu/GregTech-Modern/pull/2723)
- Remove Quartz Slab Cutter recipe due to conflict by @krossgg in [#2727](https://github.com/GregTechCEu/GregTech-Modern/pull/2727)
- Change damping to be a property of definition rather than class by @tomprince in [#2731](https://github.com/GregTechCEu/GregTech-Modern/pull/2731)

 
## Version [v1.6.2](https://github.com/GregTechCEu/GregTech-Modern/compare/v1.6.1-1.20.1...v1.6.2-1.20.1)
### Added

- Add High Pressure Variation of The Steam Miner Singleblock. by @Ghostipedia in [#2637](https://github.com/GregTechCEu/GregTech-Modern/pull/2637)
- Add Production Efficiency Modifier by @krossgg in [#2621](https://github.com/GregTechCEu/GregTech-Modern/pull/2621)
- Add multiblock constructors to KJS + lang by @krossgg in [#2667](https://github.com/GregTechCEu/GregTech-Modern/pull/2667)
- Updates registrate dependency by @Spicierspace153 in [#2625](https://github.com/GregTechCEu/GregTech-Modern/pull/2625)

### Fixed

- Fix Distillation Tower machines crashing by @krossgg in [#2646](https://github.com/GregTechCEu/GregTech-Modern/pull/2646)
- Fix Machine Recipe Modifiers by @krossgg in [#2647](https://github.com/GregTechCEu/GregTech-Modern/pull/2647)
- Fix Smart Filter not saving mode by @krossgg in [#2648](https://github.com/GregTechCEu/GregTech-Modern/pull/2648)
- Fix  builder overwriting values by @krossgg in [#2649](https://github.com/GregTechCEu/GregTech-Modern/pull/2649)
- Add LEVEL state to PotionFluid by @loving2 in [#2650](https://github.com/GregTechCEu/GregTech-Modern/pull/2650)
- Fix KJS replacement order & custom json recipes by @krossgg in [#2653](https://github.com/GregTechCEu/GregTech-Modern/pull/2653)
- Fix Cleanroom floor check by @krossgg in [#2654](https://github.com/GregTechCEu/GregTech-Modern/pull/2654)
- Fix Overclocking Logic not using correct tier for OCs by @Spicierspace153 in [#2661](https://github.com/GregTechCEu/GregTech-Modern/pull/2661)
- Fix Steam and Plasma turbines requiring mufflers by @Luexa in [#2655](https://github.com/GregTechCEu/GregTech-Modern/pull/2655)
- Fix oil spouts not updating the fluid on generation by @screret in [#2659](https://github.com/GregTechCEu/GregTech-Modern/pull/2659)
- Fix furnace recipe serialization by @krossgg in [#2657](https://github.com/GregTechCEu/GregTech-Modern/pull/2657)
- Fix MultiParts not calling  on unload by @krossgg in [#2663](https://github.com/GregTechCEu/GregTech-Modern/pull/2663)
- Fix Stone Variant maceration having conflicting recipes by @YoungOnionMC in [#2664](https://github.com/GregTechCEu/GregTech-Modern/pull/2664)
- Fix bucket being voided by TankWidget with not enough space by @Natanaelel in [#2643](https://github.com/GregTechCEu/GregTech-Modern/pull/2643)
- Change pipes to force connections to covers by @krossgg in [#2666](https://github.com/GregTechCEu/GregTech-Modern/pull/2666)

### Changed

- Fix typo in temperature parameter of BlastProperty builder by @Luexa in [#2636](https://github.com/GregTechCEu/GregTech-Modern/pull/2636)
- update ru_ru.json by @marisathewitch in [#2662](https://github.com/GregTechCEu/GregTech-Modern/pull/2662)

 
## Version [v1.6.1](https://github.com/GregTechCEu/GregTech-Modern/compare/v1.6.0-1.20.1...v1.6.1-1.20.1)
### Added

- Add new Continuous Rainbow Tooltips by @krossgg in [#2618](https://github.com/GregTechCEu/GregTech-Modern/pull/2618)
- Better error messages for . by @tomprince in [#2605](https://github.com/GregTechCEu/GregTech-Modern/pull/2605)

### Fixed

- Fix PBF issues by @krossgg in [#2615](https://github.com/GregTechCEu/GregTech-Modern/pull/2615)
- fixes netherite material color and add netherite tools by @Spicierspace153 in [#2614](https://github.com/GregTechCEu/GregTech-Modern/pull/2614)
- Fix Air Scrubber issues by @krossgg in [#2613](https://github.com/GregTechCEu/GregTech-Modern/pull/2613)
- Fix various issues with 1.6.0 by @krossgg in [#2611](https://github.com/GregTechCEu/GregTech-Modern/pull/2611)
- Fix killing/shearing mobs adding empty NBT tag by @screret in [#2610](https://github.com/GregTechCEu/GregTech-Modern/pull/2610)
- Fix Robot Arms not prioritizing existing stacks in slots by @Natanaelel in [#2612](https://github.com/GregTechCEu/GregTech-Modern/pull/2612)
- Add overrides for builder accessors by @krossgg in [#2620](https://github.com/GregTechCEu/GregTech-Modern/pull/2620)

### Changed

- Migrate Recent GTR Textures & Advanced Jetpack Item Sprite by @Ghostipedia in [#2617](https://github.com/GregTechCEu/GregTech-Modern/pull/2617)
- Remove Deprecated Dual Hatch config by @Pumpkin7266 in [#2606](https://github.com/GregTechCEu/GregTech-Modern/pull/2606)

 
## Version [v1.6.0](https://github.com/GregTechCEu/GregTech-Modern/compare/v1.5.4-1.20.1...v1.6.0-1.20.1)
### Added

- Add fluid rendering to select multiblocks by @omergunr100 in [#2439](https://github.com/GregTechCEu/GregTech-Modern/pull/2439)
- Add ability for Quantum Storages to hold more than INT_MAX by @krossgg in [#2447](https://github.com/GregTechCEu/GregTech-Modern/pull/2447)
- Add the Machine Memory Card (copy paste gadget) by @omergunr100 in [#2405](https://github.com/GregTechCEu/GregTech-Modern/pull/2405)
- Add pbf lava particles by @omergunr100 in [#2473](https://github.com/GregTechCEu/GregTech-Modern/pull/2473)
- Add Higher Tier Rotor Holders by @YoungOnionMC in [#2472](https://github.com/GregTechCEu/GregTech-Modern/pull/2472)
- dust to block molding by @YoungOnionMC in [#2471](https://github.com/GregTechCEu/GregTech-Modern/pull/2471)
- Fix data stick not copying distinct bus from me input bus by @omergunr100 in [#2448](https://github.com/GregTechCEu/GregTech-Modern/pull/2448)
- Add Empty Hand Interaction for Covers by @YoungOnionMC in [#2462](https://github.com/GregTechCEu/GregTech-Modern/pull/2462)
- Add Custom Recipe Category support to KubeJS by @krossgg in [#2476](https://github.com/GregTechCEu/GregTech-Modern/pull/2476)
- Adds a Recipe for Glass dust from Flint and Quartz sand dusts by @Makshime in [#2445](https://github.com/GregTechCEu/GregTech-Modern/pull/2445)
- Add Circuit Configuration XEI page by @YoungOnionMC in [#2494](https://github.com/GregTechCEu/GregTech-Modern/pull/2494)
- Add a material for Netherite by @a-a-GiTHuB-a-a in [#2484](https://github.com/GregTechCEu/GregTech-Modern/pull/2484)
- Various Recipe Builder Logging by @YoungOnionMC in [#2497](https://github.com/GregTechCEu/GregTech-Modern/pull/2497)
- Brewery Potion Recipe Handler by @screret in [#2506](https://github.com/GregTechCEu/GregTech-Modern/pull/2506)

### Fixed

- Fix EU/t gui tooltips now show correct value on multiblock generators by @omergunr100 in [#2460](https://github.com/GregTechCEu/GregTech-Modern/pull/2460)
- Fix empty crates and drums not stacking with new ones by @omergunr100 in [#2449](https://github.com/GregTechCEu/GregTech-Modern/pull/2449)
- Fix Item pipes teleporting items by @YoungOnionMC in [#2436](https://github.com/GregTechCEu/GregTech-Modern/pull/2436)
- Fix crash due to DT allowing 0 hatches by @Spicierspace153 in [#2452](https://github.com/GregTechCEu/GregTech-Modern/pull/2452)
- Fix pattern encoding for multiblock preview in EMI by @kitgxrl in [#2465](https://github.com/GregTechCEu/GregTech-Modern/pull/2465)
- Fix AIOOBE from Creative Tooltips by @krossgg in [#2468](https://github.com/GregTechCEu/GregTech-Modern/pull/2468)
- Fix Lang for LV & MV Macerator by @YoungOnionMC in [#2474](https://github.com/GregTechCEu/GregTech-Modern/pull/2474)
- Fix Honeycomb Dupe by @YoungOnionMC in [#2475](https://github.com/GregTechCEu/GregTech-Modern/pull/2475)
- Fix OutOfBounds when opening high tier machines by @YoungOnionMC in [#2469](https://github.com/GregTechCEu/GregTech-Modern/pull/2469)
- dust to block molding by @YoungOnionMC in [#2471](https://github.com/GregTechCEu/GregTech-Modern/pull/2471)
- Fix data stick not copying distinct bus from me input bus by @omergunr100 in [#2448](https://github.com/GregTechCEu/GregTech-Modern/pull/2448)
- Fix Creative Energy Hatch crash when choosing higher voltages by @YoungOnionMC in [#2481](https://github.com/GregTechCEu/GregTech-Modern/pull/2481)
- Fix sign & chain recipe conflicts by @Electrolyte220 in [#2486](https://github.com/GregTechCEu/GregTech-Modern/pull/2486)
- Fix some crashes caused by invalid GTOreDefinition references after reload by @cyb0124 in [#2495](https://github.com/GregTechCEu/GregTech-Modern/pull/2495)
- FTB Teams Optional NPE Fix by @YoungOnionMC in [#2500](https://github.com/GregTechCEu/GregTech-Modern/pull/2500)
- Fix MMC recipe (lv circuit tag instead of specific one) by @omergunr100 in [#2519](https://github.com/GregTechCEu/GregTech-Modern/pull/2519)
- Fix Coke Oven Hatch gaps by @eve336 in [#2442](https://github.com/GregTechCEu/GregTech-Modern/pull/2442)
- Fix AIOOBE due to recipe running too fast by @krossgg in [#2535](https://github.com/GregTechCEu/GregTech-Modern/pull/2535)
- Fix ME Output Parts being inefficient by @krossgg in [#2536](https://github.com/GregTechCEu/GregTech-Modern/pull/2536)
- Fix Chance Cache load NPE by @krossgg in [#2553](https://github.com/GregTechCEu/GregTech-Modern/pull/2553)
- Fixes LangHandler for bedrock fluid veins by @Ghostipedia in [#2545](https://github.com/GregTechCEu/GregTech-Modern/pull/2545)
- Fix ME Input Buses being extractable by @krossgg in [#2558](https://github.com/GregTechCEu/GregTech-Modern/pull/2558)
- Fix Programmed Circuit Crash on Output Parts by @Pumpkin7266 in [#2552](https://github.com/GregTechCEu/GregTech-Modern/pull/2552)
- Fix GT Fluid Attributes not registering on Source by @krossgg in [#2560](https://github.com/GregTechCEu/GregTech-Modern/pull/2560)
- Allow batteries to charge curios by @jtuc in [#2569](https://github.com/GregTechCEu/GregTech-Modern/pull/2569)
- Fix glass lens recycle category by @YoungOnionMC in [#2566](https://github.com/GregTechCEu/GregTech-Modern/pull/2566)
- Fix the handling of extra bocks in cleanroom floor, and adds some mod compatabiity. by @tomprince in [#2559](https://github.com/GregTechCEu/GregTech-Modern/pull/2559)
- Fix Tag Expr NPE and add support for untagged things by @krossgg in [#2564](https://github.com/GregTechCEu/GregTech-Modern/pull/2564)
- Fix concurrency error in DimensionCache by @krossgg in [#2565](https://github.com/GregTechCEu/GregTech-Modern/pull/2565)
- Fix shutter cover exposing item and fluid handlers by @omergunr100 in [#2572](https://github.com/GregTechCEu/GregTech-Modern/pull/2572)
- Refactor rotor holder check to support facing towards y-axis by @omergunr100 in [#2573](https://github.com/GregTechCEu/GregTech-Modern/pull/2573)
- Fix incorrect time display on  by @Pumpkin7266 in [#2575](https://github.com/GregTechCEu/GregTech-Modern/pull/2575)
- Fixes pipe block shape for empty-handed interaction by @omergunr100 in [#2576](https://github.com/GregTechCEu/GregTech-Modern/pull/2576)
- Fix Single Block Distillery Multiplier by @YoungOnionMC in [#2597](https://github.com/GregTechCEu/GregTech-Modern/pull/2597)
- Fix crop harvest yielding extra seeds by @omergunr100 in [#2582](https://github.com/GregTechCEu/GregTech-Modern/pull/2582)
- Fix Various KJS issues by @krossgg in [#2596](https://github.com/GregTechCEu/GregTech-Modern/pull/2596)

### Changed

- Refactors LCR Pattern to allow all coils to form multi by @omergunr100 in [#2461](https://github.com/GregTechCEu/GregTech-Modern/pull/2461)
- remove generic from  by @a-a-GiTHuB-a-a in [#2444](https://github.com/GregTechCEu/GregTech-Modern/pull/2444)
- Rework and refactor Chance Logic by @krossgg in [#2456](https://github.com/GregTechCEu/GregTech-Modern/pull/2456)
- Update ru_ru.json by @marisathewitch in [#2453](https://github.com/GregTechCEu/GregTech-Modern/pull/2453)
- Update zh_tw.json by @IamNotBrianZ in [#2446](https://github.com/GregTechCEu/GregTech-Modern/pull/2446)
- Add Machine Tooltip Coloring by @YoungOnionMC in [#2470](https://github.com/GregTechCEu/GregTech-Modern/pull/2470)
- Refactor Cleanroom and CharcoalPileIgnitor Structure Check by @YoungOnionMC in [#2466](https://github.com/GregTechCEu/GregTech-Modern/pull/2466)
- Make Adv Quark Suit Give Fire Immunity by @YoungOnionMC in [#2478](https://github.com/GregTechCEu/GregTech-Modern/pull/2478)
- Add explicit constructor to transfer covers by @krossgg in [#2480](https://github.com/GregTechCEu/GregTech-Modern/pull/2480)
- Fix Creative Energy dropdown to show MAX+ better by @teh-banana in [#2482](https://github.com/GregTechCEu/GregTech-Modern/pull/2482)
- Change multiblocks rendered fluid update by @omergunr100 in [#2477](https://github.com/GregTechCEu/GregTech-Modern/pull/2477)
- Remove LdLib Compass Support by @Ghostipedia in [#2467](https://github.com/GregTechCEu/GregTech-Modern/pull/2467)
- removed unused overclock divisor config by @YoungOnionMC in [#2488](https://github.com/GregTechCEu/GregTech-Modern/pull/2488)
- Rename integrated circuit in code by @YoungOnionMC in [#2489](https://github.com/GregTechCEu/GregTech-Modern/pull/2489)
- Refactors tank fluid renderer to show fluid level  by @omergunr100 in [#2487](https://github.com/GregTechCEu/GregTech-Modern/pull/2487)
- Missing Decomp Recipes by @YoungOnionMC in [#2502](https://github.com/GregTechCEu/GregTech-Modern/pull/2502)
- Force Centered Cleanroom Controller by @YoungOnionMC in [#2501](https://github.com/GregTechCEu/GregTech-Modern/pull/2501)
- Update ru_ru by @marisathewitch in [#2507](https://github.com/GregTechCEu/GregTech-Modern/pull/2507)
- Rework representative recipes and category interactions by @krossgg in [#2525](https://github.com/GregTechCEu/GregTech-Modern/pull/2525)
- Update zh_cn.json by @iouter in [#2527](https://github.com/GregTechCEu/GregTech-Modern/pull/2527)
- Uncreate Create Compat. by @Ghostipedia in [#2546](https://github.com/GregTechCEu/GregTech-Modern/pull/2546)
- Add fluid for Ruridit by @YoungOnionMC in [#2543](https://github.com/GregTechCEu/GregTech-Modern/pull/2543)
- Replace getGameTime calls with getTickCount for performance by @krossgg in [#2557](https://github.com/GregTechCEu/GregTech-Modern/pull/2557)
- Refactor RecipeModifier and OC system by @krossgg in [#2499](https://github.com/GregTechCEu/GregTech-Modern/pull/2499)
-  Refactors ore gen cache entries to expire after 30 seconds by @embeddedt in [#2580](https://github.com/GregTechCEu/GregTech-Modern/pull/2580)
- Refactor ColorSprayBehaviour using BFS implementation by @omergunr100 in [#2532](https://github.com/GregTechCEu/GregTech-Modern/pull/2532)
- KubeJS builder refactor by @screret in [#2574](https://github.com/GregTechCEu/GregTech-Modern/pull/2574)
- Refactor DataStick item and associated interaction interface by @omergunr100 in [#2561](https://github.com/GregTechCEu/GregTech-Modern/pull/2561)
- Internal Machine and Material Class Refactor by @YoungOnionMC in [#2562](https://github.com/GregTechCEu/GregTech-Modern/pull/2562)

 
## Version [v1.5.4](https://github.com/GregTechCEu/GregTech-Modern/compare/v1.5.3-1.20.1...v1.5.4-1.20.1)
### Added

- Make large miners mine upside down by @Echoloquate in [#2350](https://github.com/GregTechCEu/GregTech-Modern/pull/2350)
- Adds 2 more tiers of fishers with a toggle button to remove the junk items for double the string cost. by @Echoloquate in [#2359](https://github.com/GregTechCEu/GregTech-Modern/pull/2359)
- Add ghost circuit slot scroll input by @omergunr100 in [#2373](https://github.com/GregTechCEu/GregTech-Modern/pull/2373)
- Readd Frame Box Climbing by @YoungOnionMC in [#2385](https://github.com/GregTechCEu/GregTech-Modern/pull/2385)
- Adds the ability to get prospection data when you break a surface rock. by @YoungOnionMC in [#2388](https://github.com/GregTechCEu/GregTech-Modern/pull/2388)
- Add Steam Boiler Heat Status Jade Tooltip by @omergunr100 in [#2394](https://github.com/GregTechCEu/GregTech-Modern/pull/2394)
- Filter Cover Directionality by @YoungOnionMC in [#2343](https://github.com/GregTechCEu/GregTech-Modern/pull/2343)
- Add Machine Pausing to specialized RecipeLogic classes by @YoungOnionMC in [#2390](https://github.com/GregTechCEu/GregTech-Modern/pull/2390)
- add time to deletion to electric tool tooltips by @Spicierspace153 in [#2397](https://github.com/GregTechCEu/GregTech-Modern/pull/2397)
- Add more information to energy hatch tooltips by @YoungOnionMC in [#2434](https://github.com/GregTechCEu/GregTech-Modern/pull/2434)
- Add Maceration Recipe for Chiseled Bookshelf by @Spicierspace153 in [#2401](https://github.com/GregTechCEu/GregTech-Modern/pull/2401)

### Fixed

- Fix battery timer by @Spicierspace153 in [#2378](https://github.com/GregTechCEu/GregTech-Modern/pull/2378)
- Fixed ULV -> LV Showing a boosted chance for byproducts by @Echoloquate in [#2380](https://github.com/GregTechCEu/GregTech-Modern/pull/2380)
- adds a mallet tooltip to properly describe the shift-right click mallet pausing behavior by @Spicierspace153 in [#2381](https://github.com/GregTechCEu/GregTech-Modern/pull/2381)
- Fixes Missing Drum fill sound by @Spicierspace153 in [#2383](https://github.com/GregTechCEu/GregTech-Modern/pull/2383)
- Refactored config for machines exploding in weather  by @Echoloquate in [#2384](https://github.com/GregTechCEu/GregTech-Modern/pull/2384)
- Remove Distinct functionality from Output Parts by @YoungOnionMC in [#2386](https://github.com/GregTechCEu/GregTech-Modern/pull/2386)
- Limit Battery Slot Capacity by @Spicierspace153 in [#2392](https://github.com/GregTechCEu/GregTech-Modern/pull/2392)
- tungstensteel boiler int overflow error fix by @Spicierspace153 in [#2400](https://github.com/GregTechCEu/GregTech-Modern/pull/2400)
- Fix Machine Disabled Top Tooltip by @omergunr100 in [#2408](https://github.com/GregTechCEu/GregTech-Modern/pull/2408)
- Added config lang entry for jade steam boiler info provider by @omergunr100 in [#2417](https://github.com/GregTechCEu/GregTech-Modern/pull/2417)
- Fix NVGs giving infinite effect by @YoungOnionMC in [#2391](https://github.com/GregTechCEu/GregTech-Modern/pull/2391)
- Fix Item Tag Filter Null Pointer Exception by @Spicierspace153 in [#2402](https://github.com/GregTechCEu/GregTech-Modern/pull/2402)
- Fix recipe category NPE by @cyb0124 in [#2403](https://github.com/GregTechCEu/GregTech-Modern/pull/2403)
- Fixed large numbers to not be localized by java. by @iouter in [#2406](https://github.com/GregTechCEu/GregTech-Modern/pull/2406)
- Fixes circuit duplication glitch caused by #2373 by @omergunr100 in [#2407](https://github.com/GregTechCEu/GregTech-Modern/pull/2407)
- Fix Tank widgets by @krossgg in [#2409](https://github.com/GregTechCEu/GregTech-Modern/pull/2409)
- Fix recipe fluid stack output crashing top users by @omergunr100 in [#2421](https://github.com/GregTechCEu/GregTech-Modern/pull/2421)
- Fix environmental hazard tooltips showing when they're disabled by @omergunr100 in [#2427](https://github.com/GregTechCEu/GregTech-Modern/pull/2427)
- Fix chance logic not adjusting the roll chance in small parallel situations by @krossgg in [#2429](https://github.com/GregTechCEu/GregTech-Modern/pull/2429)
- Change potential bad casts to be saturated and remove redundant casts by @krossgg in [#2433](https://github.com/GregTechCEu/GregTech-Modern/pull/2433)

### Changed

- Remove ability to machine magnetized materials into magnetic parts by @YoungOnionMC in [#2355](https://github.com/GregTechCEu/GregTech-Modern/pull/2355)
- rename converter configs back to euToFE from euToPlatformNative by @screret in [#2387](https://github.com/GregTechCEu/GregTech-Modern/pull/2387)
- Change ore block to raw ore in xei viewers and adds ore processing to the uses of raw ore by @Spicierspace153 in [#2396](https://github.com/GregTechCEu/GregTech-Modern/pull/2396)
- Rework OverlayedFluidHandler to fix Fluid Parallel Limiting by @krossgg in [#2423](https://github.com/GregTechCEu/GregTech-Modern/pull/2423)
- Allow absolute-zero fluids by @a-a-GiTHuB-a-a in [#2430](https://github.com/GregTechCEu/GregTech-Modern/pull/2430)
- Rework Fluid slot behaviour by @Natanaelel in [#2422](https://github.com/GregTechCEu/GregTech-Modern/pull/2422)

 
## Version [v1.5.3](https://github.com/GregTechCEu/GregTech-Modern/compare/v1.5.2-1.20.1...v1.5.3-1.20.1)
### Fixed

 - Fixed recipe deserialization again so that both SP and MP worlds load ([#2376](https://github.com/GregTechCEu/GregTech-Modern/pull/2376))
 
## Version [v1.5.2](https://github.com/GregTechCEu/GregTech-Modern/compare/v1.5.1-1.20.1...v1.5.2-1.20.1)
### Added

- Add and tweak various Recipe Categories by @YoungOnionMC in [#2346](https://github.com/GregTechCEu/GregTech-Modern/pull/2346)

### Fixed

- Fix Recipe Serialization by @krossgg in [#2348](https://github.com/GregTechCEu/GregTech-Modern/pull/2348)
- Fix npe error on Conveyor and Pump Covers by @Spicierspace153 in [#2357](https://github.com/GregTechCEu/GregTech-Modern/pull/2357)
- Fix Lens decomposition recipes by @YoungOnionMC in [#2358](https://github.com/GregTechCEu/GregTech-Modern/pull/2358)
- Fix Prospector Widget causing IndexOutOfBounds Exception by @YoungOnionMC in [#2361](https://github.com/GregTechCEu/GregTech-Modern/pull/2361)
- Fix Multiblocks not staying suspended on unform/reform by @YoungOnionMC in [#2367](https://github.com/GregTechCEu/GregTech-Modern/pull/2367)
- Fix GTRecipePayload trying to call the Minecraft Server on Client Dist by @krossgg in [#2369](https://github.com/GregTechCEu/GregTech-Modern/pull/2369)

 
## Version [v1.5.1](https://github.com/GregTechCEu/GregTech-Modern/compare/v1.5.0-1.20.1...v1.5.1-1.20.1)
### Fixed

- Fix large miner chunk tooltip by @YoungOnionMC in [#2334](https://github.com/GregTechCEu/GregTech-Modern/pull/2334)
- Fix dedicated server loading client class by @cyb0124 in [#2333](https://github.com/GregTechCEu/GregTech-Modern/pull/2333)
- Fix Pump and Conveyor String format issue by @Spicierspace153 in [#2332](https://github.com/GregTechCEu/GregTech-Modern/pull/2332)
- Fix potential pipe collision NPE by @YoungOnionMC in [#2330](https://github.com/GregTechCEu/GregTech-Modern/pull/2330)
- Fix crash when dedicated server is being stopped by @cyb0124 in [#2337](https://github.com/GregTechCEu/GregTech-Modern/pull/2337)
- Fix filename sanitization in ClientCacheManager by @cyb0124 in [#2339](https://github.com/GregTechCEu/GregTech-Modern/pull/2339)

### Changed

- Change LuV item naming convention in code by @YoungOnionMC in [#2331](https://github.com/GregTechCEu/GregTech-Modern/pull/2331)

 
## Version [v1.5.0](https://github.com/GregTechCEu/GregTech-Modern/compare/v1.4.6...v1.5.0-1.20.1)
### Added

- Fix cuboid & classic ore vein generators by @screret in [#2186](https://github.com/GregTechCEu/GregTech-Modern/pull/2186)
- Add capacities of substation capacitors to their descriptions by @enitiaty in [#2207](https://github.com/GregTechCEu/GregTech-Modern/pull/2207)
- Modified to prevent things like extruder/solidifier molds from auto-filling ae2 patterns by @Echoloquate in [#2241](https://github.com/GregTechCEu/GregTech-Modern/pull/2241)
- Hide converters if they're disabled by @Zorbatron in [#2249](https://github.com/GregTechCEu/GregTech-Modern/pull/2249)
- Power Substation UI Coloring by @mikerooni in [#2256](https://github.com/GregTechCEu/GregTech-Modern/pull/2256)
- visual prospecting by @screret in [#1944](https://github.com/GregTechCEu/GregTech-Modern/pull/1944)
- OP Bypass for machine ownership by @YoungOnionMC in [#2276](https://github.com/GregTechCEu/GregTech-Modern/pull/2276)
- Add selective voiding and ME hatch capabillity to Distillation Tower multiblocks by @krossgg in [#2261](https://github.com/GregTechCEu/GregTech-Modern/pull/2261)
- Recipe Categories by @YoungOnionMC in [#2274](https://github.com/GregTechCEu/GregTech-Modern/pull/2274)
- Add BlastBuilder and Vacuum Freezer recipe overrides by @YoungOnionMC in [#2298](https://github.com/GregTechCEu/GregTech-Modern/pull/2298)
- Increase density of Realgar in Copper/Tin Veins by @Spicierspace153 in [#2222](https://github.com/GregTechCEu/GregTech-Modern/pull/2222)
- Machine Pausing by @YoungOnionMC in [#2311](https://github.com/GregTechCEu/GregTech-Modern/pull/2311)
- Add Tier-Boosted Chance overlay and tooltip line in Recipe Viewers by @YoungOnionMC in [#2293](https://github.com/GregTechCEu/GregTech-Modern/pull/2293)
- Add Smart Filters by @krossgg in [#1984](https://github.com/GregTechCEu/GregTech-Modern/pull/1984)
- Add waypoint creation in the prospector by @Sintinium in [#2319](https://github.com/GregTechCEu/GregTech-Modern/pull/2319)
- Add extra tooltip for large turbines by @Spicierspace153 in [#2326](https://github.com/GregTechCEu/GregTech-Modern/pull/2326)

### Fixed

- update LDLib to fix server freezing sometimes (and various other issues) by @Yefancy in [#2184](https://github.com/GregTechCEu/GregTech-Modern/pull/2184)
- Fix Research Stations allowing more than two energy hatches by @a-a-GiTHuB-a-a in [#2205](https://github.com/GregTechCEu/GregTech-Modern/pull/2205)
- fix ECE oxygen boosting by @screret in [#2211](https://github.com/GregTechCEu/GregTech-Modern/pull/2211)
- Gem to dust Maceration by @YoungOnionMC in [#2183](https://github.com/GregTechCEu/GregTech-Modern/pull/2183)
- Tree Felling Interaction Fixes by @YoungOnionMC in [#2218](https://github.com/GregTechCEu/GregTech-Modern/pull/2218)
- fix Large Bronze Boiler GUI  by @Spicierspace153 in [#2223](https://github.com/GregTechCEu/GregTech-Modern/pull/2223)
- Machine Ownership Config Wording by @YoungOnionMC in [#2225](https://github.com/GregTechCEu/GregTech-Modern/pull/2225)
- Fix Power Substation inworld preview by @YoungOnionMC in [#2230](https://github.com/GregTechCEu/GregTech-Modern/pull/2230)
- Change Wooden Tank Valve recipe to use copper rotor instead of lead rotor by @JuiceyBeans in [#2208](https://github.com/GregTechCEu/GregTech-Modern/pull/2208)
- Fix Power Substation display when there is no input or output by @teh-banana in [#2239](https://github.com/GregTechCEu/GregTech-Modern/pull/2239)
- Fix multiblock strcuture checking by @Yefancy in [#2247](https://github.com/GregTechCEu/GregTech-Modern/pull/2247)
- Fix Aluminium Cell not showing Fluid by @YoungOnionMC in [#2244](https://github.com/GregTechCEu/GregTech-Modern/pull/2244)
- Fixed input output mismatch in input bus/hatch lang entries by @ghost in [#2243](https://github.com/GregTechCEu/GregTech-Modern/pull/2243)
- Research Station OC Fix by @YoungOnionMC in [#2259](https://github.com/GregTechCEu/GregTech-Modern/pull/2259)
- Combustion Engine Logic Fixes by @YoungOnionMC in [#2287](https://github.com/GregTechCEu/GregTech-Modern/pull/2287)
- Fix Cables emitting smoke on first tick by @YoungOnionMC in [#2288](https://github.com/GregTechCEu/GregTech-Modern/pull/2288)
- Fix Smelting recipes being duplicated due to FURNACE_RECIPES by @YoungOnionMC in [#2306](https://github.com/GregTechCEu/GregTech-Modern/pull/2306)
- Fix miscalculation of max parallel by output for non-64 stacked items by @YoungOnionMC in [#2305](https://github.com/GregTechCEu/GregTech-Modern/pull/2305)
- Fix tape slot in Maintenance Hatches by @YoungOnionMC in [#2312](https://github.com/GregTechCEu/GregTech-Modern/pull/2312)
- Flip Red and Blue Steel composition and usage by @YoungOnionMC in [#2299](https://github.com/GregTechCEu/GregTech-Modern/pull/2299)
- Fix misaligned connected textures. by @WithersChat in [#2321](https://github.com/GregTechCEu/GregTech-Modern/pull/2321)
- adds platinum spring by @Spicierspace153 in [#2323](https://github.com/GregTechCEu/GregTech-Modern/pull/2323)
- Fix steam multiblock args by @krossgg in [#2324](https://github.com/GregTechCEu/GregTech-Modern/pull/2324)
- Fix NoClassFoundException when EMI was not installed by @krossgg in [#2325](https://github.com/GregTechCEu/GregTech-Modern/pull/2325)

### Changed

- Refactor configs for vanilla recipes by @JuiceyBeans in [#2197](https://github.com/GregTechCEu/GregTech-Modern/pull/2197)
- Move from LDLib's FluidStack implementation to Forge's FluidStack by @krossgg in [#1975](https://github.com/GregTechCEu/GregTech-Modern/pull/1975)
- Update zh_cn.json by @iouter in [#2191](https://github.com/GregTechCEu/GregTech-Modern/pull/2191)
- Update Japanese translate by @code-onigiri in [#2188](https://github.com/GregTechCEu/GregTech-Modern/pull/2188)
- Change EV-tier Voltage Coil material to Platinum by @Zorbatron in [#2233](https://github.com/GregTechCEu/GregTech-Modern/pull/2233)
- Update zh_tw.json by @IamNotBrianZ in [#2236](https://github.com/GregTechCEu/GregTech-Modern/pull/2236)
- Renames GCYM classes to proper capitalization by @YoungOnionMC in [#2047](https://github.com/GregTechCEu/GregTech-Modern/pull/2047)
- Rewrite OreDictExprFilter to fix tag filter functionality by @bdemmy in [#2220](https://github.com/GregTechCEu/GregTech-Modern/pull/2220)
- Move from LDLib's IItemTransfer to Forge's IItemHandler by @krossgg in [#2246](https://github.com/GregTechCEu/GregTech-Modern/pull/2246)
- Replace the Tungstensteel heating components with RTM Alloy by @a-a-GiTHuB-a-a in [#2275](https://github.com/GregTechCEu/GregTech-Modern/pull/2275)
- Update zh_cn.json by @iouter in [#2301](https://github.com/GregTechCEu/GregTech-Modern/pull/2301)
- Change Jade to show Steam usage by @krossgg in [#2309](https://github.com/GregTechCEu/GregTech-Modern/pull/2309)
- Swap crafting component for Laser Input/Output Hatches by @YoungOnionMC in [#2303](https://github.com/GregTechCEu/GregTech-Modern/pull/2303)
- Removes the old PA config by @YoungOnionMC in [#2308](https://github.com/GregTechCEu/GregTech-Modern/pull/2308)
- Refactor CleaningMaintenanceHatch by @YoungOnionMC in [#2307](https://github.com/GregTechCEu/GregTech-Modern/pull/2307)
- Replace all usage of OreDict with modern's Tag by @YoungOnionMC in [#2313](https://github.com/GregTechCEu/GregTech-Modern/pull/2313)
- Changed the gas-filter texture to be semi-isometric like circuits/lenses/etc by @Ghostipedia in [#2315](https://github.com/GregTechCEu/GregTech-Modern/pull/2315)
- Move some vanilla dye recipes to the Extractor by @Spicierspace153 in [#2322](https://github.com/GregTechCEu/GregTech-Modern/pull/2322)

 
## Version [v1.4.6](https://github.com/GregTechCEu/GregTech-Modern/compare/v1.4.5...v1.4.6)
### Added

- Add Programmed Circuits to GTEmiRecipe Catalysts by @krossgg in [#2175](https://github.com/GregTechCEu/GregTech-Modern/pull/2175)

### Fixed

- Fix NPE caused by filter not loading properly on cover by @YoungOnionMC in [#2176](https://github.com/GregTechCEu/GregTech-Modern/pull/2176)
- Fix issues with serialization by @krossgg in [#2177](https://github.com/GregTechCEu/GregTech-Modern/pull/2177)

 
## Version [v1.4.5](https://github.com/GregTechCEu/GregTech-Modern/compare/1.20.1-1.4.4...v1.4.5)
### Added

- Replace wrench with plate in bender recipe by @JuiceyBeans in [#2057](https://github.com/GregTechCEu/GregTech-Modern/pull/2057)
- Machine Ownership by @YoungOnionMC in [#1970](https://github.com/GregTechCEu/GregTech-Modern/pull/1970)
- Give coils an actual EU discount in EBF by @YoungOnionMC in [#2055](https://github.com/GregTechCEu/GregTech-Modern/pull/2055)
- Multiblock Display Text for Outputs by @YoungOnionMC in [#2048](https://github.com/GregTechCEu/GregTech-Modern/pull/2048)
- Make magnetic tool property pull mob drops too by @JuiceyBeans in [#2037](https://github.com/GregTechCEu/GregTech-Modern/pull/2037)
- Yeet Sand from OreVeins by @Ghostipedia in [#2078](https://github.com/GregTechCEu/GregTech-Modern/pull/2078)
- Give torch placing a sound by @YoungOnionMC in [#2091](https://github.com/GregTechCEu/GregTech-Modern/pull/2091)
- Port of 1.12 hard ebf recipe by @YoungOnionMC in [#2093](https://github.com/GregTechCEu/GregTech-Modern/pull/2093)
- Add tags for GT components by @JuiceyBeans in [#2111](https://github.com/GregTechCEu/GregTech-Modern/pull/2111)
- Add in-line filtering for item and fluid pipes by @YoungOnionMC in [#2126](https://github.com/GregTechCEu/GregTech-Modern/pull/2126)
- Add Storage Cover by @Spicierspace153 in [#1990](https://github.com/GregTechCEu/GregTech-Modern/pull/1990)
- Add placable surface rock items by @YoungOnionMC in [#2132](https://github.com/GregTechCEu/GregTech-Modern/pull/2132)
- Aisle Repetition Storage by @YoungOnionMC in [#2140](https://github.com/GregTechCEu/GregTech-Modern/pull/2140)
- Copy the EBF's temperature tooltip to the ABS and RHF by @Zorbatron in [#2142](https://github.com/GregTechCEu/GregTech-Modern/pull/2142)
- Add cutting recipe for stained glass panes by @YoungOnionMC in [#2144](https://github.com/GregTechCEu/GregTech-Modern/pull/2144)
- Add ability to set ghost circuit using held circuit item by @YoungOnionMC in [#2145](https://github.com/GregTechCEu/GregTech-Modern/pull/2145)
- Add dough by @YoungOnionMC in [#2147](https://github.com/GregTechCEu/GregTech-Modern/pull/2147)
- fluid crafting table recipes by @screret in [#2152](https://github.com/GregTechCEu/GregTech-Modern/pull/2152)

### Fixed

- Give coils an actual EU discount in EBF by @YoungOnionMC in [#2055](https://github.com/GregTechCEu/GregTech-Modern/pull/2055)
- Update tooltip capitalization for consistency by @JuiceyBeans in [#2061](https://github.com/GregTechCEu/GregTech-Modern/pull/2061)
- Misc recipe fixes by @JuiceyBeans in [#2056](https://github.com/GregTechCEu/GregTech-Modern/pull/2056)
- Torch Behaviour, now with less duping by @YoungOnionMC in [#2067](https://github.com/GregTechCEu/GregTech-Modern/pull/2067)
- Fix broken treated wood recipes by @Electrolyte220 in [#2045](https://github.com/GregTechCEu/GregTech-Modern/pull/2045)
- Remove any connected miner pipes when the machine is removed by @Vextin in [#2064](https://github.com/GregTechCEu/GregTech-Modern/pull/2064)
- Add missing flags for materials by @JuiceyBeans in [#2024](https://github.com/GregTechCEu/GregTech-Modern/pull/2024)
- Output Line Infinite formatting by @YoungOnionMC in [#2082](https://github.com/GregTechCEu/GregTech-Modern/pull/2082)
- AE2 Integration: Use smart cable connection type for ME Connected blocks by @RealKC in [#2087](https://github.com/GregTechCEu/GregTech-Modern/pull/2087)
- Missing progress lang for some multis by @YoungOnionMC in [#2090](https://github.com/GregTechCEu/GregTech-Modern/pull/2090)
- Fix fluid drilling rig not working with 2 hatches by @YoungOnionMC in [#2092](https://github.com/GregTechCEu/GregTech-Modern/pull/2092)
- Fix Wooden sign recipes by @YoungOnionMC in [#2094](https://github.com/GregTechCEu/GregTech-Modern/pull/2094)
- Stone tags and LPG localisation by @JuiceyBeans in [#2095](https://github.com/GregTechCEu/GregTech-Modern/pull/2095)
- Add more interactions + fixes for Lighter by @JuiceyBeans in [#2075](https://github.com/GregTechCEu/GregTech-Modern/pull/2075)
- Fix Large Miner bus requirement by @YoungOnionMC in [#2121](https://github.com/GregTechCEu/GregTech-Modern/pull/2121)
- Fix Assembly Line ordering for certain orientations by @YoungOnionMC in [#2119](https://github.com/GregTechCEu/GregTech-Modern/pull/2119)
- Fix Gem downcrafting recipes by @YoungOnionMC in [#2102](https://github.com/GregTechCEu/GregTech-Modern/pull/2102)
- Fix Recipe NBT Serialization by @YoungOnionMC in [#2105](https://github.com/GregTechCEu/GregTech-Modern/pull/2105)
- Fix Drum Item fluid property handling by @YoungOnionMC in [#2128](https://github.com/GregTechCEu/GregTech-Modern/pull/2128)
- Misc Recipe Fixes by @Electrolyte220 in [#2125](https://github.com/GregTechCEu/GregTech-Modern/pull/2125)
- Rubber Wood/Log Fixes by @Electrolyte220 in [#2097](https://github.com/GregTechCEu/GregTech-Modern/pull/2097)
- Fix Frame Block being deleted upon shift-click with pipe by @YoungOnionMC in [#2131](https://github.com/GregTechCEu/GregTech-Modern/pull/2131)
- Fix Heat Capacity not showing in ABS and EBF by @RealKC in [#2134](https://github.com/GregTechCEu/GregTech-Modern/pull/2134)
- Fix Debrominated brine not having any usage by @YoungOnionMC in [#2133](https://github.com/GregTechCEu/GregTech-Modern/pull/2133)
- Add back backwards compatibility for the SteamParallelMultiblock constructor by @krossgg in [#2139](https://github.com/GregTechCEu/GregTech-Modern/pull/2139)
- Fix voltage floor formula causing negative values by @YoungOnionMC in [#2141](https://github.com/GregTechCEu/GregTech-Modern/pull/2141)
- Fix insertion and extraction of rotors in rotor holders by @YoungOnionMC in [#2143](https://github.com/GregTechCEu/GregTech-Modern/pull/2143)
- Fix Lighter ground behavior by @YoungOnionMC in [#2146](https://github.com/GregTechCEu/GregTech-Modern/pull/2146)
- Fix magnetic double plate overlay by @YoungOnionMC in [#2148](https://github.com/GregTechCEu/GregTech-Modern/pull/2148)
- Fix Surface Rock model in inventories and player by @YoungOnionMC in [#2155](https://github.com/GregTechCEu/GregTech-Modern/pull/2155)
- Fix various recipe and item issues by @JuiceyBeans in [#2077](https://github.com/GregTechCEu/GregTech-Modern/pull/2077)
- Fix various Cleanroom issues by @krossgg in [#2156](https://github.com/GregTechCEu/GregTech-Modern/pull/2156)
- Fixed milk crash by @YoungOnionMC in [#2158](https://github.com/GregTechCEu/GregTech-Modern/pull/2158)
- Fix progress bar textures by @JuiceyBeans in [#2163](https://github.com/GregTechCEu/GregTech-Modern/pull/2163)
- Fix FoodStats tooltip by @JuiceyBeans in [#2161](https://github.com/GregTechCEu/GregTech-Modern/pull/2161)

### Changed

- Update manage-pr-labels.yml by @krossgg in [#2062](https://github.com/GregTechCEu/GregTech-Modern/pull/2062)
- Change Wooden Multiblock Tank recipe to use copper instead of lead by @JuiceyBeans in [#2107](https://github.com/GregTechCEu/GregTech-Modern/pull/2107)
- Allow Steam Multiblocks to have variable parallel amounts by @Deepacat in [#2108](https://github.com/GregTechCEu/GregTech-Modern/pull/2108)
- Refactor and add missing sign & trapdoor recipes by @Electrolyte220 in [#2098](https://github.com/GregTechCEu/GregTech-Modern/pull/2098)
- Rework Pump Machine logic to fix issues by @eragaxshim in [#2003](https://github.com/GregTechCEu/GregTech-Modern/pull/2003)

 
Version: 1.4.1

### ADDITIONS:
 
- Added Textures for Computer Casings styled around the new texture standard
- Fluid Tags for Molten Fluids & Plasmas
- Datasticks will now render the primary item within contained data when holding SHIFT

### CHANGES:

- Parallel Hatches can no longer be shared.
- SI Formatting for large fluid amounts
- Updated all hatches and busses recipes to match GTCEU : 1.12.2
- Hazard gear works in curios slots
- Changed the location of the dimension icon for dimension based recipes
- KubeJS Support for research methods
- Research recipes now display CWU total in JADE and use the proper EU values.
- PACK DEVS ! - Can now define render toggles for multiblocks
- ADDON DEVS ! - OCLogic has been modified, please verify your addons reflect these new changes.

### FIXES:
   
- Fixed Rubber Leaves and Saplings not being able to compost.
- Fix fluid output slots accepting manual inputs
- Large variety of recipe logic and OC logic fixes
- Fixed AEParts in assembly lines having wrong recipe voltages
- Fixes Crashes due to null recipes caused by HighTier being disabled
- Fixed PartialNBT usage in recipes
- Various KubeJS recipe fixes
- ME Export Bus is now considered an "EXPORT_ITEMS" part ability
- Fix bedrock ore miner not overclocking
- Assembly line Structure no longer allows MEBufferParts


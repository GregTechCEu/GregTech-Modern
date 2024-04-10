# ChangeLog

Version: 1.2.0

### ADDITIONS:
- Ported the new Research System for Late Game from 1.12:
  - Assembly lines now require "Research Data" to run recipes. Data is provided by Data Sticks in Hatches or Data Banks.
  - Later tier Research Data requires more complex means of computation. Introducing the HPCA and Research Station.
- Added Portable Scanner
- Added Multiblock Fluid Tanks

### CHANGES:
- Improved recipe viewer widgets
- Updated Power Substation UI to show separate values for input and output
- Removed the recipes for Processing Arrays
  - **IMPORTANT:** Your existing PAs will only continue to work until 1.3.0, at which point we will remove them entirely.  
    Please replace them with the appropriate specialized multiblocks until then!
- Updated Russian and Chinese translations

### FIXES:
- Fixed Multiblocks with only 1 Energy/Dynamo Hatch not overclocking
- Fixed Energy Hatch amounts in Multiblocks
- Fixed high-tier Emitters not existing
- Fixed surface rock descriptions
- Fixed plungers not working
- Fixed ME Input Hatch and ME Output Bus
- Fixed number formatting in certain places
- Fixed Large Boilers requiring a Maintenance Hatch when maintenance is disabled in the config
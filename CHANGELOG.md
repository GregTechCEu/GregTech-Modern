# ChangeLog

Version: 1.0.20

***WARNING: THIS RELEASE BREAKS MOST EXISTING TOOLS!***
The tool rework unfortunately requires some breaking changes.
You will still have your tool items and may still use some of them in crafting recipes,
but you will need to craft the ones you're actively using again to regain functionality.


**Additions:**
- port missing tooltips from 1.12
- add maintenance to large boilers
- add shape info to all machines using coils
- mark multiblock info blocks as inputs for the recipe viewer pattern preview
- add UHV+ electric motors

**Tool Rework:**
- revamps tools, bringing parity with 1.12 (except electric tools)
- adds spades, an AOE shovel
- adds functionality to most tools that were missing it previously
- colored crowbars
- fixes some tools' block break times etc.
- brings parity with forge & fabric on some tool-related functionality
- fixes some issues with crafting tools
- AOE tools now work in creative mode
- wrenches now rotate blocks other than GT machines
- crowbars now rotate rail blocks
- plungers made from different rubbers now have different durabilities

**Fixes:**
- fixed certain ore veins being too small
- fix LuV prospector recipe
- fix EMI screen loading error
- fix diodes being reset to 1A on chunk load
- fix diodes not being able to transfer more than 8A
- fix surface rocks not breaking when their supporting block is broken
- fix crash when placing pump covers on fluid pipes in certain cases
- fix voiding mode not working in quantum tanks
- fix charger not dropping contents when broken
- fix missing transfer size input in robot arms and fluid regulators, when a tag filter is used

Notes for addon devs:  
- GTItems.TOOL_ITEMS is now a table<Material, Type, Tool item> instead of the old <Tier, Type, Item>
- torch placing with pickaxes is currently disabled as none of us can find a fix for it deleting the tool.
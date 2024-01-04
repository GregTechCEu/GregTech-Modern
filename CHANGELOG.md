# ChangeLog

Version: 1.0.20

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

***WARNING: THIS BREAKS MOST EXISTING TOOLS!***

Notes for addon devs:  
- GTItems.TOOL_ITEMS is now a table<Material, Type, Tool item> instead of the old <Tier, Type, Item>
- torch placing with pickaxes is currently disabled as none of us can find a fix for it deleting the tool.
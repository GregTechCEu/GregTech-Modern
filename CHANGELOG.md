# ChangeLog

Version: 1.1.4

### ADDITIONS:
- add Curios slot for magnets
- add compass pages for coke oven, EBF, large boiler
- add collapsible entry for ore blocks in REI

### FIXES:
- fix proxy recipes not being found
- add dimension requirements for fluid veins
- fix world accelerator block entity duplication and/or voiding
- fix multi-amp transformer capacity
- fix machines not connecting to already placed cables
- fix battery buffers allowing stacked batteries
- fix bucket emptying behavior
- fix item collector behavior
- fix issues with EMI integration
- fix ConcurrentModificationException in recipe conditions
- fix large boiler fuel consumption and steam output display
- fix descriptions for impure ore cauldron actions
- fix item tooltip performance issues
- fix KJS not being able to remove GT tags
- improve performance in the JEI integration by caching recipe UI size
- improve ingredient lookup performance
- fix bucket model
- fix chainsaw durability consumption
- fix large miner crashes
- fix fluid drilling rig EU/t usage and add extra tooltips
- fix distinct bus mode not working with ghost circuits
- fix item filter mode resetting every time the UI is opened
- fix pumps inserting the same fluid into more than one slot
- fix pump/conveyor covers and other machines being able to extract from a creative tank's internal slot 

### CHANGES:
- updated Russian and Chinese translations
- buff the large miner's fortune level
- removed async recipe searching, as it's no longer necessary
- make UI titlebars only scroll/roll when hovering over them

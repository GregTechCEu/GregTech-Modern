---
title: The Central Monitor & Placeholder System
---

### The Central Monitor

The Central Monitor is a multiblock that allows you to insert modules into it to render images and text.<br>
Images update every 120 seconds, text update rate depends on the voltage provided to the multiblock.
Guide on how to use the central monitor:

1. Right-click the controller
2. In the UI, you will see a grid of monitors, the controller, energy hatch and (optionally) a data hatch
3. Select some of the monitors (in any configuration) by left-clicking on them
4. Click the "Create group" button
5. You should see a group appear on the left of the UI, click on it to select all monitors in that group, click again to unselect
6. Click on the gear icon next to the name of the group you want to edit
7. A UI with a single slot should open, put a module into that slot (while it is possible to put a stack of modules in, that does literally nothing)
8. If it's a text/image module a new field should appear, where you can enter some text (for image it'll be a single line for a URL)
9. Once you've entered your text, click on the green checkmark below the slot, that will save the text you entered
10. Click on the gear icon next to the group you're editing to go back to the main menu
11. You should see the text/image on the Central Monitor

To remove a group, select it and click "Remove from group". To remove a single monitor from a group select only it and click "Remove from group".
You cannot add monitors to a group after it has been created. Image dimensions are determined by the left-up corner of the group and the right-down corner,
the blocks between them have to be in the same group. The text module will only display text on monitors of its group.

!!! warning "The image module is a bit buggy, so the image may not appear immediately"

### Text Module

You may have noticed that the text module has a number input in its UI. It is the text scale, where 1 represents a line height of 1/16th of a block.
You may have also noticed that the text module has some additional slots on the left.
Those are referenced by placeholders, you can put any item in them. Most placeholders also need a target block to work. To select a target for your monitor group,
in the main UI of the controller select the group, right-click the block you want to target and click "Set target". You may want to target a block that is not in the
central monitor, to do that you have to use a Wireless Transmitter Cover. Place it on the block you want to target and right-click it with a data stick. Then put that
data stick into a data hatch in the Central Monitor multiblock. If you select the data hatch as a target, you will see a new number field appear. Enter the number of the
slot your data stick is in and click "Set target". The target will be set to the block the Wireless Transmitter Cover is on. It can work cross-dimensionally.

!!! note "For the Computer Monitor Cover, the targeted block is always the block it's placed on."

### Placeholders
Placeholders can be used by players in the monitor text module, or in the computer monitor cover (though a bit more limited).
For example, a player may write something like this in a text module:
```
Hello on day {calc {tick} / 20000}!
Current energy buffer: {formatInt {energy}}/{formatInt {energyCapacity}} EU\
{if {cmp {energy} < 5000000} {color red "\nLOW ENERGY!"}}
Here's some random stuff:
{repeat 5 {repeat {random 2 10} {block}}
```
And something like this would be displayed:
```
Hello on day 420!
Current energy buffer: 4.2M/6.9M EU
LOW ENERGY!
Here's some random stuff:
███████
██
█████
████
██████████
```
This system is turing-complete (i.e. if the player really wanted to play Doom on the Central Monitor, they could).<br>
All placeholders work on strings (or, more specifically, `Component`s to allow text formatting), so when you write `{calc {calc 2 + 4} * 3}`,
first `{calc 2 + 4}` will be evaluated into `6`, then it will be converted to a string and back to an int, and then it will be passed into the second placeholder
to evaluate `{calc 6 * 3}` into `18`, which will be turned into a string again. That also allows for things like `{calc 3 + 1}2`, which will evaluate into `42`,
since outside of placeholders text is simply concatenated. Placeholder arguments are separated by spaces, which may be a bit annoying, when wanting to pass a string
with a space into a placeholder, for example `{if 1 string with spaces}`, which will cause an error. In these cases, double quotes can be used: `{if 1 "string with spaces"}`
will work perfectly fine. There are placeholders that need reference items, to achieve that, there are 8 slots in the text module's UI on the left.
Items can be inserted/extracted from these slots automatically using the `ender` placeholder by interacting with Ender Item Links.<br>

!!! tip "The full list of placeholders with explanations on what they do and usage examples can be found in-game in the text module or computer monitor UI on the left."
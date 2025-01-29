# Machine Controller
The machine controller can be placed as a cover on anything with a working status, such as machines, multiblock controllers to control the working status on recipes, to control the auto inputting or outputting of input and output bus/hatches, or to control other covers on the same tile using redstone signal
## Usage

When placed on a tiles side, the default config starts in "normal" mode with a signal threshold of 1, meaning any redstone signal 1 or higher into the side with the controller will pause the tiles working state

Changing the covers mode to "inverted" will reverse this, causing it to be disabled unless it has a redstone signal of 1 or higher

The signal threshold may be changed, so on normal mode the redstone signal strength would have to be equal or higher than the number to disable working, or on inverted requiring equal or higher to enable working

The machine controller may also be used to control other covers on the tile instead of the working status of the tile, by opening the interface of the machine controller you may click on "control machine" to cycle through different things you may control, this allows you to redstone control the state of conveyors, pumps, their advanced variants, and shutters

When placed on a pipe, buffer, crate, or any other storage tile without a working status it will require another cover, as well as default to another cover in the interface
## Trivia
- Can be controlled through a solid block that redstone travels through, as expected with vanilla mechanics
- Can not be controlled through another side of the tile it is placed on, must be into the controllers side
- Disabling a machine via machine controller or rubber mallet will not void the ongoing recipe, only pause until working status resumes
- Currently bugged to not update on pipes till the configuration changes
- Several machine controllers can be placed on one tile to control seperate covers or have more advanced conditions to control the working status of the tile, or one cover
- Pausing a large boiler or fusion reactor will not retain their heat
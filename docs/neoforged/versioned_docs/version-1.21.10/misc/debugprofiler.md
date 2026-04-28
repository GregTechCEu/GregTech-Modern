# Debug Profiler

Minecraft provides a Debug Profiler that provides system data, current game settings, JVM data, level data, and sided tick information to find time consuming code. Considering things like `TickEvent`s and ticking `BlockEntity`s, this can be very useful for modders and server owners that want to find a lag source.

## Using the Debug Profiler

The Debug Profiler is very simple to use. It requires the debug keybind `F3 + L` to start the profiler. After 10 seconds, it will automatically stop; however, it can be stopped earlier by pressing the keybind again.

:::note
Naturally, you can only profile code paths that are actually being reached. `Entities` and `BlockEntities` that you want to profile must exist in the level to show up in the results.
:::

After you have stopped the debugger, it will create a new zip within the `debug/profiling` subdirectory in your run directory.
The file name will be formatted with the date and time as `yyyy-mm-dd_hh_mi_ss-WorldName-VersionNumber.zip`

## Reading a Profiling result

Within each sided folder (`client` and `server`), you will find a `profiling.txt` file containing the result data. At the top, it first tells you how long in milliseconds it was running and how many ticks ran in that time.

Below that, you will find information similar to the snippet below:

```
[00] tick(201/1) - 41.46%/41.46%
[01] |   levels(201/1) - 96.62%/40.05%
[02] |   |   ServerLevel[New World] minecraft:overworld(201/1) - 98.80%/39.58%
[03] |   |   |   tick(201/1) - 99.98%/39.57%
[04] |   |   |   |   entities(201/1) - 56.83%/22.49%
[05] |   |   |   |   |   tick(44717/222) - 95.81%/21.54%
[06] |   |   |   |   |   |   minecraft:skeleton(4585/23) - 13.91%/3.00%
[07] |   |   |   |   |   |   |   #tickNonPassenger 4585/22
[07] |   |   |   |   |   |   |   travel(4573/23) - 33.12%/0.99%
[08] |   |   |   |   |   |   |   |   #getChunkCacheMiss 7/0
[08] |   |   |   |   |   |   |   |   #getChunk 47227/234
[08] |   |   |   |   |   |   |   |   move(4573/23) - 40.10%/0.40%
[09] |   |   |   |   |   |   |   |   |   #getEntities 4573/22
[09] |   |   |   |   |   |   |   |   |   #getChunkCacheMiss 1353/6
[09] |   |   |   |   |   |   |   |   |   #getChunk 28482/141
[08] |   |   |   |   |   |   |   |   unspecified(4573/23) - 36.24%/0.36%
[08] |   |   |   |   |   |   |   |   rest(4573/23) - 23.66%/0.23%
[09] |   |   |   |   |   |   |   |   |   #getChunkCacheMiss 59/0
[09] |   |   |   |   |   |   |   |   |   #getChunk 65867/327
[09] |   |   |   |   |   |   |   |   |   #getChunkNow 531/2
```

Some entries look like `[03] tick(201/1) - 99.98%/39.57%` as a result of `ProfilerFiller#push` and `pop`. This means:

- `[03]` - The depth of the section.
- `tick` - The name of the section.
    - `unspecified` if the duration of time did not have an associated subsection.
- `201` - The number of times this section was called during the profiler's runtime.
- `1` - The average number of times, rounded down, this section was called during a single tick.
- `99.98%` - The percentage of time taken in relation to its parent.
    - For Layer 0, it is the percentage of the time a tick takes.
    - For Layer 1, it is the percentage of the time its parent takes.
- `39.57%` - The percentage of time taken from the entire tick.

There are also some entries that look like `[07] #tickNonPassenger 4585/22` as a result of `ProfileFiller#incrementCounter`. This means:

- `[07]` - The depth of the section.
- `#tickNonPassenger` - The name of the counter being incremented.
    - The `#` is prepended automatically.
- `4585` - The number of times this counter was incremented during the profiler's runtime.
- `22` - The average number of times, rounded down, this counter was incremented during a single tick.

## Profiling your own code

The Debug Profiler has basic support for `Entity` and `BlockEntity`. If you would like to profile something else, you may need to manually create your sections like so:

```java
Profiler.get().push("yourSectionName");
//The code you want to profile
Profiler.get().pop();
```

Now you just need to search the results file for your section name.

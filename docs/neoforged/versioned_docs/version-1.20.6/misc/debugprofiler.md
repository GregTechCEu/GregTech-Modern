# Debug Profiler

Minecraft provides a Debug Profiler that provides system data, current game settings, JVM data, level data, and sided tick information to find time consuming code. Considering things like `TickEvent`s and ticking `BlockEntities`, this can be very useful for modders and server owners that want to find a lag source.

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
[00] levels - 96.70%/96.70%
[01] |   Level Name - 99.76%/96.47%
[02] |   |   tick - 99.31%/95.81%
[03] |   |   |   entities - 47.72%/45.72%
[04] |   |   |   |   regular - 98.32%/44.95%
[04] |   |   |   |   blockEntities - 0.90%/0.41%
[05] |   |   |   |   |   unspecified - 64.26%/0.26%
[05] |   |   |   |   |   minecraft:furnace - 33.35%/0.14%
[05] |   |   |   |   |   minecraft:chest - 2.39%/0.01%
```

Here is a small explanation of what each part means:

| [02]                     | tick                    | 99.31%       | 95.81%       |
| :----------------------- | :---------------------- | :----------- | :----------- |
| The Depth of the section | The Name of the Section | The percentage of time it took in relation to it's parent. For Layer 0, it is the percentage of the time a tick takes. For Layer 1, it is the percentage of the time its parent takes. | The second percentage tells you how much time it took from the entire tick.

## Profiling your own code

The Debug Profiler has basic support for `Entity` and `BlockEntity`. If you would like to profile something else, you may need to manually create your sections like so:

```java
ProfilerFiller#push(yourSectionName : String);
//The code you want to profile
ProfilerFiller#pop();
```
You can obtain the `ProfilerFiller` instance from a `Level`, `MinecraftServer`, or `Minecraft` instance.
Now you just need to search the results file for your section name.

---
icon: "material/frequently-asked-questions"
title: "FAQ"
---


# :material-frequently-asked-questions: Frequently Asked Questions


## How to disable Shimmer's colored light monitor

Shimmer (one of our soft-dependencies) displays information about colored lights in development environments by default.  
You can turn it off using the command `/shimmer coloredLightMonitor` ingame.

Alternatively, if you want to disable it by default, put this in your KubeJS client scripts:

```js
// Disable Shimmer's colored light monitor:
let LightCounterRender = Java.loadClass("com.lowdragmc.shimmer.client.light.LightCounter$Render")
LightCounterRender.enable = false
```

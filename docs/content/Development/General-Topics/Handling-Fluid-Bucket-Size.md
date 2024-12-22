---
title: Handling Fluid Bucket Size
---


# Dealing with Fluid Bucket Sizes

The fluid systems of Forge and Fabric use different units. 
Make sure you use the correct units whenever you're handling fluid amounts.

To get the size of one bucket, use the following method:

```java
FluidHelper.getBucket(); // returns 1000 on Forge and 81000 on Fabric.
```
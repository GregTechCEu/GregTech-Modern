---
title: PartAbility
---
PartAbilities are a quick way of creating a PatternPredicate from a similar collection of blocks. Some examples are all EnergyHatches, every MufflerPart, all LaserHatches, etc.

When you register some block or machine, you can set that block or machine as the value for some specific part abilities tier key.

```java title="PartAbilityExample.java"
public static PartAbility myPartAbility = new PartAbility("myPartAbility"); // (1)
...
myPartAbility.register(0, Blocks.WOOD); // (2)
```

1. How to create a part ability.

2. How to register values to a part ability.




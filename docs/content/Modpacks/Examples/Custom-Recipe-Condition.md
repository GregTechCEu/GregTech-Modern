---
title: Custom Recipe Condition
---

!!! Warning 
    Custom recipe conditions are only supported in Java. Therefore, this page will only contain Java examples.

Recipe conditions are custom conditions for your recipe, like biome, machine tier, or anything else you can think of.

!!! Note
    The condition is run after recipe matching and before recipe execution. If the recipe condition doesn't match, the machine will be suspended and won't be updated again until something in the inputs/outputs changes.

They are registered using
```java
@Mod(ExampleMod.MOD_ID)
public class ExampleMod {
    
    public ExampleMod(FMLJavaModLoadingContext context) {
        var bus = context.getModEventBus();
        bus.addGenericListener(RecipeConditionType.class, this::registerConditions);
    }

    public static RecipeConditionType<ExampleCondition> EXAMPLE_CONDITION;
    
    public void registerConditions(GTCEuAPI.RegisterEvent<String, RecipeConditionType<?>> event) {
        EXAMPLE_CONDITION = GTRegistries.RECIPE_CONDITIONS.register("example_condition",
                new RecipeConditionType<>(
                        ExampleCondition::new, 
                        ExampleCondition.CODEC
                )
        );
    }
}
```

We will set up a condition that requires that the power buffer of the machine is above a certain Y level.
```java
public class ExampleCondition extends RecipeCondition {

    public int height;

    public static final Codec<ExampleCondition> CODEC = RecordCodecBuilder
            .create(instance -> RecipeCondition.isReverse(instance)
                    .and(Codec.INT.fieldOf("height").forGetter(val -> val.height))
                    .apply(instance, ExampleCondition::new));


    public ExampleCondition(boolean isReverse, int height) {
        this.isReverse = isReverse;
        this.height = height;
    }

    public ExampleCondition(int height) {
        this(false, height);
    }    
    
    public ExampleCondition() {
        this(false, 0);
    }

    @Override
    public RecipeConditionType<?> getType() {
        return ExampleMod.EXAMPLE_CONDITION;
    }

    @Override
    public Component getTooltips() {
        return Component.literal(String.format("Should be ran at least at height %d", height));
    }

    @Override
    protected boolean testCondition(@NotNull GTRecipe recipe, @NotNull RecipeLogic recipeLogic) {
        return recipeLogic.getMachine().getHolder().getCurrentPos().getY() >= height;
    }

    @Override
    public RecipeCondition createTemplate() {
        return new ExampleCondition(0);
    }
}
```

Lets step through this example. This will not be in order as it is in the file, but rather in the order that makes most sense.

Starting with:
```java
    @Override
    public RecipeConditionType<?> getType() {
        return ExampleMod.EXAMPLE_CONDITION;
    }

    @Override
    public Component getTooltips() {
        return Component.literal(String.format("Should be ran at least at height %d", height));
    }
```
This part is quite simple, and just returns the type and tooltip for the condition. The tooltip is what gets added in the recipe viewer's screen if this condition is present.

```java
    public ExampleCondition(boolean isReverse, int height) {
        this.isReverse = isReverse;
        this.height = height;
    }
    
    public ExampleCondition(int height) {
        this(false, height);
    }

    public ExampleCondition() {
        this(false, 0);
    }
```
These are the constructors. We need the `isReverse`, as it is part of the overarching `RecipeCondition` type. `isReverse` means that if the condition is met, your recipe won't be run. Furthermore, a no-arg constructor is required for (de)serialization.

```java
    @Override
    public RecipeCondition createTemplate() {
        return new ExampleCondition(0);
    }
```

This creates the basic "template" that might be used for serialization. This should return a default version of your condition.

```java
    @Override
    protected boolean testCondition(@NotNull GTRecipe recipe, @NotNull RecipeLogic recipeLogic) {
        return recipeLogic.getMachine().getHolder().getCurrentPos().getY() >= height;
    }
```

This is the actual condition.

```java
    public int height;

    public static final Codec<ExampleCondition> CODEC = RecordCodecBuilder
            .create(instance -> RecipeCondition.isReverse(instance)
                    .and(Codec.INT.fieldOf("height").forGetter(val -> val.height))
                    .apply(instance, ExampleCondition::new));
```

The CODEC is how java knows how to serialize/deserialize your condition. This is needed for syncing between client/server, and storing it to json to load when the world loads.
It consists of a few parts:

- `RecordCodecBuilder.create(instance -> ` means we will start a RecordCodecBuilder, or a builder that only consists of simple types.
- `RecipeCondition.isReverse(instance)` is a helper codec that serializes the isReverse boolean of your codec.
- `.and(` means this is the next field in the record.
- `Codec.INT.fieldOf("height").forGetter(val -> val.height)` means we want to serialize an INT, we want to call it "height" in the json, and to get the value you call `.height`.
- `.apply(instance, ExampleCondition::new)` means when deserializing back to an object, you apply these steps to get the values (in this case `bool isReverse, int height`) and call the constructor with those arguments.  
    In this case, this would call our `new ExampleCondition(isReverse, height)` constructor we have defined earlier.

With this, you should have everything you need to make a custom RecipeCondition.

To apply it to a recipeCondition, you would add to the Recipe Builder: `.condition(new ExampleCondition(70))` for a required height of 70
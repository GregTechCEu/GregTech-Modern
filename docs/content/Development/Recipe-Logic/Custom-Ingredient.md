---
title: "Custom Ingredient"
---

!!! Note
    Before reading / following this document, it is heavily recommended to have read [Recipe Logic](./Recipe-Logic.md), [Recipe Searching](./Recipe-Searching.md) and [Recipe Execution](./Recipe-Execution.md) first. 

If you want to make a custom ingredient, you need to do the following things

1. Create the Ingredient itself
2. Create a MapIngredient to hold/check the Ingredient
3. Create a RecipeCapability to process the Ingredient 
4. Create the NotifiableHatch to keep track of the Ingredient in the machine
5. Create a MultiPart that lets you interact with the ingredient in-game
6. Create the PartAbility for the MultiPart
7. Create a RecipeType so we can test with it
8. Register all these things

## Creating the Ingredient
For our example, we will be using a simple ingredient called Bonk that simply holds how often you right clicked the hatch with a hard hammer.

For our first step, we will be creating the ingredient:

```java title="BonkIngredient.java"
public class BonkIngredient {

    public static final BonkIngredient EMPTY = new BonkIngredient(0);

    public static final Codec<BonkIngredient> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("bonk").forGetter(BonkIngredient::getBonk)
    ).apply(instance, BonkIngredient::new));

    @Getter
    private int bonk;

    public BonkIngredient(int bonk){
        this.bonk = bonk;
    }

    public BonkIngredient copy(){
        return new BonkIngredient(bonk);
    }
    
    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof BonkIngredient other)) return false;
        return this.bonk == other.bonk;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(bonk);
    }

    @Override
    public String toString() {
        return "BonkIngredient{bonk=" + bonk + "}";
    }

    public static final class Serializer implements IContentSerializer<BonkIngredient> {
        public static final BonkIngredient.Serializer INSTANCE = new BonkIngredient.Serializer();

        @Override
        public BonkIngredient of(Object o) {
            if (o instanceof Integer integer) {
                return new BonkIngredient(integer);
            } else if (o instanceof BonkIngredient bonkIngredient) {
                return bonkIngredient;
            }
            return null;
        }

        @Override
        public BonkIngredient defaultValue() {
            return EMPTY;
        }

        @Override
        public Class<BonkIngredient> contentClass() {
            return BonkIngredient.class;
        }

        @Override
        public Codec<BonkIngredient> codec() {
            return CODEC;
        }
    }
}
```

This is mostly just a wrapper around an integer, but the methods are there in case your ingredient would need to hold more data or have more complicated (de)serialization. For more information on Codecs, read [the forge docs](https://docs.minecraftforge.net/en/latest/datastorage/codecs/).


## Creating the MapIngredient

```java title="MapBonkIngredient"
public class MapBonkIngredient extends AbstractMapIngredient {

    public final BonkIngredient ingredient;

    public MapBonkIngredient(BonkIngredient ingredient) {
        this.ingredient = ingredient;
    }

    @Override
    protected int hash() {
        return ingredient.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof MapBonkIngredient other)) return false;
        return other.ingredient.equals(this.ingredient);
    }

    @Override
    public String toString() {
        return "MapBonkIngredient{" + "bonk=" + ingredient + '}';
    }

    public static List<AbstractMapIngredient> convertToMapIngredient(BonkIngredient ingredient) {
        return Collections.singletonList(new MapBonkIngredient(ingredient));
    }
}
```


## Creating the RecipeCapability
```java title="BonkRecipeCapability"
public class BonkRecipeCapability extends RecipeCapability<BonkIngredient> {

    public final static BonkRecipeCapability CAP = new BonkRecipeCapability();

    protected BonkRecipeCapability() {
        super("bonk", 0x777777, false, 5, BonkIngredient.Serializer.INSTANCE);
    }

    @Override
    public BonkIngredient copyInner(BonkIngredient content) {
        return content.copy();
    }

    @Override
    public @Nullable List<AbstractMapIngredient> getDefaultMapIngredient(Object ingredient) {
        List<AbstractMapIngredient> ingredients = new ObjectArrayList<>(1);
        if (ingredient instanceof BonkIngredient bonkIngredient) ingredients.add(new MapBonkIngredient(bonkIngredient));
        return ingredients;
    }

    @Override
    public List<Object> compressIngredients(Collection<Object> ingredients) {
        int bonkTotal = 0;
        for(Object ingredient : ingredients){
            if (ingredient instanceof BonkIngredient bonkIngredient){
                bonkTotal += bonkIngredient.getBonk();
            }
        }
        if(bonkTotal > 0){
            return new ObjectArrayList<>(Collections.singleton(new BonkIngredient(bonkTotal)));
        }
        return Collections.emptyList();
    }

    @Override
    public void addXEIInfo(WidgetGroup group, int xOffset, GTRecipe recipe, List<Content> contents, boolean perTick,
                           boolean isInput, MutableInt yOffset) {
        for (var content : contents) {
            var bonkIngredient = BonkRecipeCapability.CAP.of(content);
            if(isInput){
                group.addWidget(new LabelWidget(3-xOffset, yOffset.addAndGet(10), "Bonk needed: " + bonkIngredient.getBonk()));
            }
            // Bonk output not supported for now
        }
    }
}
```

## Creating the NotifiableHatch
```java title="NotifiableBonkHandler"
public class NotifiableBonkHandler extends NotifiableRecipeHandlerTrait<BonkIngredient>
        implements ICapabilityTrait {

    @Getter
    public final IO handlerIO;
    @Getter
    public final IO capabilityIO;

    @Getter
    private int bonk;

    public NotifiableBonkHandler(MetaMachine machine, IO io) {
        this(machine, io, io);
    }

    public NotifiableBonkHandler(MetaMachine machine, IO handlerIO, IO capabilityIO) {
        super(machine);
        this.handlerIO = handlerIO;
        this.capabilityIO = capabilityIO;
    }

    public boolean addBonk(int bonkToAdd, boolean simulate){
        if(bonkToAdd < 0) return false;
        if((long) bonkToAdd + (long) this.bonk > Integer.MAX_VALUE) return false;
        if(simulate) return true;
        bonk += bonkToAdd;
        this.notifyListeners();
        return true;
    }


    public boolean drainBonk(int bonkToDrain, boolean simulate){
        if(bonkToDrain < 0) return false;
        if(bonkToDrain > this.bonk) return false;
        if(simulate) return true;
        bonk -= bonkToDrain;
        this.notifyListeners();
        return true;
    }

    @Override
    public List<BonkIngredient> handleRecipeInner(IO io, GTRecipe recipe, List<BonkIngredient> left, boolean simulate) {
        for (int i = 0; i < left.size(); i++) {
            BonkIngredient bonkIngredient = left.get(i);
            if (bonk >= bonkIngredient.getBonk()) {
                if (!simulate) {
                    bonk -= bonkIngredient.getBonk();
                }
                left.remove(i);
                break;
            }
        }
        return left.isEmpty() ? null : left;
    }

    @Override
    public @NotNull List<Object> getContents() {
        return List.of(new BonkIngredient(bonk));
    }

    @Override
    public double getTotalContentAmount() {
        return 1;
    }

    @Override
    public RecipeCapability<BonkIngredient> getCapability() {
        return BonkRecipeCapability.CAP;
    }
}
```

## Creating the MultiPart
```java title="BonkHatchPartMachine"
public class BonkHatchPartMachine extends TieredIOPartMachine {
    
    @Persisted
    public NotifiableBonkHandler bonkHandler;


    public BonkHatchPartMachine(IMachineBlockEntity holder, int tier, IO io) {
        super(holder, tier, io);
        // On creation the NotifiableBonkHandler attaches itself to the machine
        this.bonkHandler = new NotifiableBonkHandler(this, io);
    }

    @Override
    protected InteractionResult onHardHammerClick(Player playerIn, InteractionHand hand, Direction gridSide, BlockHitResult hitResult) {
        if(isRemote()) return InteractionResult.SUCCESS;
        if(bonkHandler.addBonk(1, false)){
            playerIn.sendSystemMessage(Component.literal("Bonk! Total bonk stored: " + bonkHandler.getBonk()));
            return InteractionResult.CONSUME;
        }
        return super.onHardHammerClick(playerIn, hand, gridSide, hitResult);
    }
}
```

We will register the actual part, as well as a "Large Bonk Reactor" which is an LCR that can use our part ability:

```java title="BonkMachines.java"
public class BonkMachines {

    public static final MachineDefinition BONK_HATCH = Bonk.REGISTRATE
            .machine("bonk_hatch", (holder) -> new BonkHatchPartMachine(holder, ZPM, IO.IN))
            .langValue("Bonk Hatch")
            .rotationState(RotationState.ALL)
            .tier(ZPM)
            .modelProperty(GTMachineModelProperties.IS_FORMED, false)
            .colorOverlayTieredHullModel(GTCEu.id("block/overlay/machine/overlay_pipe_in_emissive"), null,
                    GTCEu.id("block/overlay/machine/" + OVERLAY_ITEM_HATCH))
            .abilities(BonkPartAbilities.BONK_HATCH)
            .register();

    public static final MultiblockMachineDefinition LARGE_BONK_REACTOR = Bonk.REGISTRATE
            .multiblock("large_bonk_reactor", WorkableElectricMultiblockMachine::new)
            .langValue("Large Bonk Reactor")
            .rotationState(RotationState.ALL)
            .recipeType(BonkRecipeTypes.LARGE_BONK_RECIPES)
            .recipeModifiers(OC_PERFECT_SUBTICK, BATCH_MODE)
            .appearanceBlock(CASING_PTFE_INERT)
            .pattern(definition -> {
                var casing = blocks(CASING_PTFE_INERT.get()).setMinGlobalLimited(10);
                var abilities = Predicates.autoAbilities(definition.getRecipeTypes())
                        .or(Predicates.autoAbilities(true, false, false))
                        .or(Predicates.abilities(BonkPartAbilities.BONK_HATCH));
                return FactoryBlockPattern.start()
                        .aisle("XXX", "XCX", "XXX")
                        .aisle("XCX", "CPC", "XCX")
                        .aisle("XXX", "XSX", "XXX")
                        .where('S', Predicates.controller(blocks(definition.getBlock())))
                        .where('X', casing.or(abilities))
                        .where('P', blocks(CASING_POLYTETRAFLUOROETHYLENE_PIPE.get()))
                        .where('C', Predicates.heatingCoils().setExactLimit(1)
                                .or(abilities)
                                .or(casing))
                        .build();
            })
            .workableCasingModel(GTCEu.id("block/casings/solid/machine_casing_inert_ptfe"),
                    GTCEu.id("block/multiblock/large_chemical_reactor"))
            .register();

    public static void init() {
    }
}
```

## Creating the PartAbility
```java title="BonkPartAbilities.java"
public class BonkPartAbilities {
    public static final PartAbility BONK_HATCH = new PartAbility("bonk_hatch");
}
```

## Creating the RecipeType
```java title="BonkRecipeTypes.java"
public class BonkRecipeTypes {
    public static final GTRecipeType LARGE_BONK_RECIPES = register("large_bonk_reactor", MULTIBLOCK)
            .setMaxIOSize(3, 3, 5, 4)
            .setMaxSize(IO.IN, BonkRecipeCapability.CAP, 1)
            .setEUIO(IO.IN);

    public static void init() {}
    
    public static GTRecipeType register(String name, String group, RecipeType<?>... proxyRecipes) {
        var recipeType = new GTRecipeType(GTCEu.id(name), group, proxyRecipes);
        GTRegistries.register(BuiltInRegistries.RECIPE_TYPE, recipeType.registryName, recipeType);
        GTRegistries.register(BuiltInRegistries.RECIPE_SERIALIZER, recipeType.registryName, new GTRecipeSerializer());
        GTRegistries.RECIPE_TYPES.register(recipeType.registryName, recipeType);
        return recipeType;
    }
}
```

## Making a Recipe
```java title="BonkRecipes.java"
public class BonkRecipes {

    public static void init(Consumer<FinishedRecipe> provider) {
        LARGE_BONK_RECIPES.recipeBuilder(
                        GTCEu.id("test"))
                .inputItems(Items.STONE)
                .input(BonkRecipeCapability.CAP, new BonkIngredient(2))
                .outputItems(Items.COBBLESTONE)
                .duration(100)
                .EUt(GTValues.VA[GTValues.LV])
                .save(provider);
    }
}
```

## Registering Everything

```java title="BonkRecipeCapabilities.java"
public class BonkRecipeCapabilities {

    public static final BonkRecipeCapability BONK = BonkRecipeCapability.CAP;

    public static void init() {
        GTRegistries.RECIPE_CAPABILITIES.register(BONK.name, BONK);
    }
}
```
The following parts, you would do in your main java class and your main GTAddon class, assuming you are working off of the [Addon Template](https://github.com/JuiceyBeans/GregTech-Addon-Template):
```java title="Bonk.java"
@Mod(Bonk.MOD_ID)
public class Bonk {
    // ...
    private void registerRecipeTypes(GTCEuAPI.RegisterEvent<ResourceLocation, GTRecipeType> event) {
        BonkRecipeTypes.init();
    }

    private void registerMachines(GTCEuAPI.RegisterEvent<ResourceLocation, MachineDefinition> event) {
        BonkMachines.init();
    }
}
```

```java title="BonkGTAddon.java"
@GTAddon
public class BonkGTAddon implements IGTAddon {
    // ...
    @Override
    public void initializeAddon() {
        MapIngredientTypeManager.registerMapIngredient(BonkIngredient.class, MapBonkIngredient::convertToMapIngredient);
    }
    @Override
    public void addRecipes(Consumer<FinishedRecipe> provider) {
        BonkRecipes.init(provider);
    }

    @Override
    public void registerRecipeCapabilities() {
        BonkRecipeCapabilities.init();
    }
}

```

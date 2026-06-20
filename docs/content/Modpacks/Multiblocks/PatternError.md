---
title: PatternError
---
`PatternError`s are a descriptive way to tell the player why the multiblock has failed to form. They take in an optional BlockPos and required list of candidates.

Each PatternError also must implement the ui modifier, which is the way to display that information to the player (through the main multiblock GUI).

```java title="ExmaplePatternError.java

public class MyPatternError extends PatternError {
    
    // This codec is needed for serialization. It should send all the needed data to display the error on the client. 
    public static Codec<PlaceholderError> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    BlockPos.CODEC.fieldOf("pos").forGetter(PatternError::getPos),
                    Codec.list(Codec.list(BlockInfo.CODEC)).fieldOf("candidates").forGetter(PatternError::getCandidates))
            .apply(instance, MyPatternError::new));

    public static ResourceLocation ID = GTCEu.id("MyPatternError");

    public MyPatternError(@Nullable BlockPos pos, List<List<BlockInfo>> candidates) {
        super(pos, candidates);
    }
    
    // The UI modifier adds the widget to be displayed to the user if this error occurs.
    public PatternErrorUI getPatternErrorUIModifier() {
        return (widget) -> {
                widget.child(new ContextMenuButton<>("predicate")
                        .menuList(l -> l
                                .maxSize(40)
                                .children(candidates, candidate -> {
                                    return new ItemDrawable(candidate.getItemStackForm()).asWidget()
                                            .tooltip(r -> r.add(candidate.getItemStackForm().getHoverName()));
                                })));
        };
    }

    @Override
    public Codec<? extends PatternError> codec() {
        return CODEC;
    }
}
```

You also need to register your new PatternErrors statically:

```java

public class ExampleMod {
    public ExampleMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addGenericListener(WHAT.class, this::registerPatternErrors);
    }

    private void registerPatternErrors(GTCEuAPI.RegisterEvent<ResourceLocation, Codec<? extends PatternError>> event) {
        GTRegistries.PATTERN_ERRORS.register(MyPatternError.ID, MyPatternError.CODEC);
    }

}
```

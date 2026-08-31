---
title: PatternError
---
`PatternError`s are a descriptive way to tell the player why the multiblock has failed to form. They take in an optional BlockPos and required list of candidates.

Each PatternError also must implement the ui modifier, which is the way to display that information to the player (through the main multiblock GUI).

```java title="ExamplePatternError.java"
public class MyPatternError extends PatternError {

    // This codec is needed for serialization. It should send all the needed data to display the error on the client. 
    public static MapCodec<PlaceholderError> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    BlockPos.CODEC.fieldOf("pos").forGetter(PatternError::getPos),
                    Codec.list(Codec.list(BlockInfo.CODEC)).fieldOf("candidates").forGetter(PatternError::getCandidates))
            .apply(instance, MyPatternError::new));

    public static final PatternErrorType TYPE = new PatternErrorType(ExampleMod.id("my_pattern_error"), CODEC);

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
    public PatternErrorType type() {
        return TYPE;
    }
}
```

You also need to register your new PatternErrors statically:

```java
public class ExampleMod {
    public ExampleMod(IEventBus modBus) {
        PATTERN_ERROR_TYPES.register(modBus);
    }

    private static final DeferredRegister<PatternError.PatternErrorType> PATTERN_ERROR_TYPES = DeferredRegister.create(GTRegistries.Keys.PATTERN_ERROR_TYPE, ADDON_MOD_ID);

    public static final DeferredHolder<PatternError.PatternErrorType, PatternError.PatternErrorType> MY_PATTERN_ERROR_TYPE =
            PATTERN_ERROR_TYPES.register(MyPatternError.TYPE.id().getPath(), () -> MyPatternError.TYPE);

}
```

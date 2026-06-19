---
title: PatternError
---
`PatternError`s are a descriptive way to tell the player why the multiblock has failed to form. They take in an optional BlockPos and required list of candidates.

Each PatternError also must implement the ui modifier, which is the way to display that information to the player (through the main multiblock GUI).

```java title="ExmaplePatternError.java

public class MyPatternError extends PatternError {
    
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
    
}
```
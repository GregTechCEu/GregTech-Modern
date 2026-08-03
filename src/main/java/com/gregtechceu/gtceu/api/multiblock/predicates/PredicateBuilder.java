package com.gregtechceu.gtceu.api.multiblock.predicates;

import com.gregtechceu.gtceu.api.multiblock.MultiPredicate;
import com.gregtechceu.gtceu.api.multiblock.PredicateContext;
import com.gregtechceu.gtceu.api.multiblock.util.BlockInfo;

import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.ForgeRegistries;

import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

@Accessors(fluent = true)
public class PredicateBuilder {

    private final String name;
    @Setter
    private Predicate<PredicateContext> predicate;
    @Setter
    private Stream<BlockInfo> candidates = Stream.empty();
    @Setter
    private @Nullable Consumer<StringBuilder> contents;
    @Setter
    private @Nullable Consumer<PredicateContext> onError;

    public PredicateBuilder(String debugName) {
        this.name = debugName;
    }

    /// fills candidates and sets string contents with this block tag
    public PredicateBuilder blockTag(TagKey<Block> tag) {
        this.candidates = Objects.requireNonNull(ForgeRegistries.BLOCKS.tags())
                .getTag(tag).stream().map(BlockInfo::fromBlock);
        this.contents = builder -> builder.append(tag.location());
        return this;
    }

    /// fills candidates and sets string contents with this fluid tag
    public PredicateBuilder fluidTag(TagKey<Fluid> tag) {
        this.candidates = Objects.requireNonNull(ForgeRegistries.FLUIDS.tags())
                .getTag(tag).stream().map(BlockInfo::fromFluid);
        this.contents = builder -> builder.append(tag.location());
        return this;
    }

    public MultiPredicate toMultiPredicate() {
        return MultiPredicate.of(build());
    }

    public BasePredicate build() {
        return new TestablePredicate(name,
                Objects.requireNonNull(predicate, "predicate == null"),
                candidates,
                contents,
                onError);
    }
}

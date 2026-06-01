package com.gregtechceu.gtceu.api.multiblock.error;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;

import brachy.modularui.api.drawable.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class BlockMatchingError extends PatternError {

    private final Block[] blocks;

    public BlockMatchingError(BlockPos pos, Block[] blocks) {
        super(pos, Collections.emptyList());
        this.blocks = blocks;
    }

    @Override
    public PatternErrorUI applyErrorInformation() {
        return (parent) -> {
            List<Component> comps = new ArrayList<>();
            for (Block block : blocks) {
                comps.add(block.getName());
            }
            Objects.requireNonNull(pos);
            comps.add(Component.translatable("gtceu.pattern_predicate.blocks", pos.getX(), pos.getY(), pos.getZ()));
            comps.forEach(comp -> parent.child(Text.of(comp).asWidget()));
        };
    }
}

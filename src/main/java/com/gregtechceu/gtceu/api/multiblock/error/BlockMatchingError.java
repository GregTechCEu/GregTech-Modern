package com.gregtechceu.gtceu.api.multiblock.error;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;

import brachy.modularui.api.drawable.Text;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class BlockMatchingError extends PatternError {

    public static final Codec<BlockMatchingError> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockPos.CODEC.fieldOf("pos").forGetter(PatternError::getPos),
            BuiltInRegistries.BLOCK.byNameCodec().listOf().fieldOf("blocks")
                    .forGetter(BlockMatchingError::getBlocks))
            .apply(instance, BlockMatchingError::new));

    public static final PatternErrorType TYPE = new PatternErrorType(GTCEu.id("block_matching_error"), CODEC);

    @Getter
    private final List<Block> blocks;

    public BlockMatchingError(BlockPos pos, List<Block> blocks) {
        super(pos);
        this.blocks = blocks;
    }

    @Override
    public PatternErrorUI getPatternErrorUIModifier() {
        return (parent) -> {
            List<Component> comps = new ArrayList<>();
            for (Block block : blocks) {
                comps.add(block.getName());
            }
            comps.add(Component.translatable("gtceu.pattern_predicate.blocks", pos.getX(), pos.getY(), pos.getZ()));
            comps.forEach(comp -> parent.child(Text.of(comp).asWidget()));
        };
    }

    @Override
    public PatternErrorType type() {
        return TYPE;
    }
}

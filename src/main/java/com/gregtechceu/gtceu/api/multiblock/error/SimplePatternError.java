package com.gregtechceu.gtceu.api.multiblock.error;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.multiblock.util.BlockInfo;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

import brachy.modularui.api.drawable.Text;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class SimplePatternError extends PatternError {

    public static final Codec<SimplePatternError> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockPos.CODEC.fieldOf("pos").forGetter(PatternError::getPos),
            BlockInfo.CODEC.listOf().listOf().fieldOf("candidates").forGetter(SimplePatternError::getCandidates))
            .apply(instance, SimplePatternError::new));

    public static final PatternErrorType TYPE = new PatternErrorType(GTCEu.id("simple_pattern_error"), CODEC);
    @Getter
    private final List<List<BlockInfo>> candidates;

    public SimplePatternError(BlockPos pos, List<List<BlockInfo>> candidates) {
        super(pos);
        this.candidates = candidates;
    }

    @Override
    public PatternErrorUI getPatternErrorUIModifier() {
        return (parent) -> {
            List<Component> lines = new ArrayList<>();

            lines.add(Component.translatable("gtceu.multiblock.pattern.error.0"));
            lines.add(Component.translatable("gtceu.multiblock.pattern.error.1", pos.getX(), pos.getY(),
                    pos.getZ()));
            for (List<BlockInfo> candidate : candidates) {
                if (!candidate.isEmpty()) {
                    Component c = candidate.get(0).getItemStackForm().getHoverName();
                    lines.add(c);
                    // builder.append(c.toString());
                    // builder.append(COMMA_SEPERATOR_LITERAL);
                }
            }
            lines.forEach(comp -> parent.child(Text.of(comp).asWidget()));
        };
    }

    @Override
    public PatternErrorType type() {
        return TYPE;
    }
}

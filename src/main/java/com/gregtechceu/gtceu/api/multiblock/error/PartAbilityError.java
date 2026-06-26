package com.gregtechceu.gtceu.api.multiblock.error;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.drawable.Icon;
import brachy.modularui.drawable.ItemDrawable;
import brachy.modularui.widgets.layout.Flow;
import brachy.modularui.widgets.menu.ContextMenuButton;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;

import java.util.Collection;
import java.util.Collections;

public class PartAbilityError extends PatternError {

    public static MapCodec<PartAbilityError> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BlockPos.CODEC.fieldOf("pos").forGetter(PatternError::getPos),
            Codec.STRING.fieldOf("name").forGetter(PartAbilityError::getPartAbilityName))
            .apply(instance, (a, b) -> new PartAbilityError(a, PartAbility.VALUES.get(b))));

    public static final PatternErrorType TYPE = new PatternErrorType(GTCEu.id("part_ability_error"), CODEC);

    @Getter
    private final String partAbilityName;

    public PartAbilityError(BlockPos pos, PartAbility partAbility) {
        super(pos, Collections.emptyList());
        partAbilityName = partAbility.getName();
    }

    @Override
    public PatternErrorUI getPatternErrorUIModifier() {
        return (parent) -> {
            Collection<Block> blocks = PartAbility.VALUES.get(partAbilityName).getAllBlocks();
            Flow row = Flow.row()
                    .coverChildren();
            row.child(Text.str("Missing one " + partAbilityName + ": ").asWidget());
            row.child(new ContextMenuButton<>(partAbilityName)
                    .menuList(l -> l
                            .maxSize(40)
                            .coverChildrenWidth()
                            .collapseDisabledChildren()
                            .childSeparator(Icon.EMPTY_2PX)
                            .children(blocks, block -> {
                                return new ItemDrawable(block.asItem()).asWidget()
                                        .tooltip(r -> r.add(block.asItem().getDescription()));
                            })));
            parent.child(row);
        };
    }

    @Override
    public PatternErrorType type() {
        return TYPE;
    }
}

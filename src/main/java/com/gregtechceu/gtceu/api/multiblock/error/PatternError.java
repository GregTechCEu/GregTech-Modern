package com.gregtechceu.gtceu.api.multiblock.error;

import com.gregtechceu.gtceu.api.multiblock.PatternPredicate;
import com.gregtechceu.gtceu.api.multiblock.pattern.CurrentBlockInfo;
import com.gregtechceu.gtceu.api.multiblock.predicates.BasePredicate;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import brachy.modularui.api.drawable.Text;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PatternError {

    /**
     * Return this for your pattern errors if you want them to be a default error with the pos of the BlockWorldState
     * and candidates of the simple predicate's error.
     */
    public static final PatternError PLACEHOLDER = new PatternError(BlockPos.ZERO, Collections.emptyList());
    @Getter
    protected @Nullable BlockPos pos;
    @Getter
    protected List<List<ItemStack>> candidates;
    protected CurrentBlockInfo blockInfo;

    public PatternError(@Nullable BlockPos pos, List<List<ItemStack>> candidates) {
        this.pos = pos;
        this.candidates = candidates;
    }

    public PatternError(@Nullable BlockPos pos, PatternPredicate predicate) {
        this(pos, predicate.getCandidates());
    }

    public PatternError(@Nullable BlockPos pos, BasePredicate failingPredicate) {
        this(pos, Collections.singletonList(failingPredicate.getCandidateStacks()));
    }

    public Level getWorld() {
        return blockInfo.getLevel();
    }

    @NotNull
    public PatternErrorUI applyErrorInformation() {
        return (parent) -> {
            List<Component> lines = new ArrayList<>();

            if (pos != null) {
                lines.add(Component.translatable("gtceu.multiblock.pattern.error.0"));
                lines.add(Component.translatable("gtceu.multiblock.pattern.error.1", pos.getX(), pos.getY(),
                        pos.getZ()));
            }
            for (List<ItemStack> candidate : candidates) {
                if (!candidate.isEmpty()) {
                    Component c = candidate.get(0).getHoverName();
                    lines.add(c);
                    // builder.append(c.toString());
                    // builder.append(COMMA_SEPERATOR_LITERAL);
                }
            }
            lines.forEach(comp -> parent.child(Text.of(comp).asWidget()));
        };
    }
}

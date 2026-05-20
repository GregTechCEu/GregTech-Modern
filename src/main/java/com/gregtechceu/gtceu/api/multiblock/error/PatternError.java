package com.gregtechceu.gtceu.api.multiblock.error;

import com.gregtechceu.gtceu.api.multiblock.PatternPredicate;
import com.gregtechceu.gtceu.api.multiblock.pattern.CurrentBlockInfo;
import com.gregtechceu.gtceu.api.multiblock.predicates.BasePredicate;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import lombok.Getter;

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
    protected BlockPos pos;
    @Getter
    protected List<List<ItemStack>> candidates;
    protected CurrentBlockInfo blockInfo;

    public PatternError(BlockPos pos, List<List<ItemStack>> candidates) {
        this.pos = pos;
        this.candidates = candidates;
    }

    public PatternError(BlockPos pos, PatternPredicate predicate) {
        this(pos, predicate.getCandidates());
    }

    public PatternError(BlockPos pos, BasePredicate failingPredicate) {
        this(pos, Collections.singletonList(failingPredicate.getCandidates()));
    }

    public Level getWorld() {
        return blockInfo.getLevel();
    }

    public List<Component> getErrorInfo() {
        List<Component> lines = new ArrayList<>();
        lines.add(Component.translatable("gtceu.multiblock.pattern.error.0"));
        lines.add(Component.translatable("gtceu.multiblock.pattern.error.1", pos.getX(), pos.getY(),
                pos.getZ()));
        for (List<ItemStack> candidate : candidates) {
            if (!candidate.isEmpty()) {
                Component c = candidate.get(0).getHoverName();
                lines.add(c);
                // builder.append(c.toString());
                // builder.append(COMMA_SEPERATOR_LITERAL);
            }
        }
        // builder.append(CommonComponents.ELLIPSIS);

        return lines;
    }
}

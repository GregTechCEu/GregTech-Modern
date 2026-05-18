package com.gregtechceu.gtceu.api.multiblock.error;

import com.gregtechceu.gtceu.api.multiblock.PatternPredicate;
import com.gregtechceu.gtceu.api.multiblock.pattern.CurrentBlockInfo;
import com.gregtechceu.gtceu.api.multiblock.predicates.BasePredicate;

import com.gregtechceu.gtceu.api.sync_system.ISyncManaged;
import com.gregtechceu.gtceu.api.sync_system.SyncDataHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.gregtechceu.gtceu.utils.GTStringUtils.COMMA_SEPERATOR_LITERAL;

public class PatternError {
    /**
     * Return this for your pattern errors if you want them to be a default error with the pos of the BlockWorldState
     * and candidates of the simple predicate's error.
     */
    public static final PatternError PLACEHOLDER = new PatternError(BlockPos.ZERO, Collections.emptyList());
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

    public BlockPos getPos() {
        return blockInfo.getBlockPos();
    }

    public List<Component> getErrorInfo() {
        StringBuilder builder = new StringBuilder();
        for (List<ItemStack> candidate : candidates) {
            if (!candidate.isEmpty()) {
                builder.append(candidate.get(0));
                builder.append(COMMA_SEPERATOR_LITERAL);
            }
        }
        builder.append(CommonComponents.ELLIPSIS);
        List<Component> lines = new ArrayList<>();
        lines.add(Component.translatable("gtceu.multiblock.pattern.error.0", builder.toString()));
        lines.add(Component.translatable("gtceu.multiblock.pattern.error.1", pos.getX(), pos.getY(),
                pos.getZ()));
        return lines;
    }
}

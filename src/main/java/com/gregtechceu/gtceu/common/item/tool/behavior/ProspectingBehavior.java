package com.gregtechceu.gtceu.common.item.tool.behavior;

import com.gregtechceu.gtceu.api.item.IGTTool;
import com.gregtechceu.gtceu.api.item.tool.behavior.IToolBehavior;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.Tags;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ProspectingBehavior implements IToolBehavior {

    public static final ProspectingBehavior INSTANCE = new ProspectingBehavior();

    @Override
    public @NotNull InteractionResult onItemUse(UseOnContext context) {
        if (context.getItemInHand().getItem() instanceof IGTTool tool) {
            int tier = tool.getTotalHarvestLevel(context.getItemInHand());
            int depth = tool.getProspectingDepth();
            findOres(context.getLevel(), context.getClickedPos(), context.getClickedFace(), depth).forEach(c -> {
                if (context.getPlayer() != null && context.getLevel().isClientSide())
                    context.getPlayer().sendSystemMessage(c);
            });
            return InteractionResult.SUCCESS;
        } else return IToolBehavior.super.onItemUse(context);
    }

    private static List<MutableComponent> findOres(Level level, BlockPos pos, Direction direction, int depth) {
        Set<BlockState> foundBlocks = new HashSet<>();
        Set<Fluid> foundFluids = new HashSet<>();
        for (int i = 0; i < depth; i++) {
            for (BlockPos position : getSurroundingBlocks(pos, direction)) {
                if (!level.getFluidState(position).isEmpty())
                    foundFluids.add(level.getFluidState(position).getType());
                else foundBlocks.add(level.getBlockState(position));
            }
            if (direction.getAxis().isHorizontal()) {
                for (BlockPos position : getSurroundingBlocks(pos.below(), direction)) {
                    if (!level.getFluidState(position).isEmpty())
                        foundFluids.add(level.getFluidState(position).getType());
                    else foundBlocks.add(level.getBlockState(position));
                }
            }
            pos = pos.relative(direction.getOpposite());
        }
        List<MutableComponent> out = new ArrayList<>();
        int cnt = 0;
        for (BlockState state : foundBlocks) {
            if (state.is(Tags.Blocks.ORES)) {
                out.add(Component.translatable("item.gtceu.tool.behavior.prospecting.ore", state.getBlock().getName()));
            } else if (state.isAir()) out.add(Component.translatable("item.gtceu.tool.behavior.prospecting.air"));
            else cnt++;
        }
        for (Fluid state : foundFluids) {
            if (state.isSame(Fluids.WATER))
                out.add(Component.translatable("item.gtceu.tool.behavior.prospecting.water"));
            if (state.isSame(Fluids.LAVA)) out.add(Component.translatable("item.gtceu.tool.behavior.prospecting.lava"));
        }
        if (cnt >= 2) out.add(Component.translatable("item.gtceu.tool.behavior.prospecting.changing"));
        return out;
    }

    private static List<BlockPos> getSurroundingBlocks(BlockPos pos, Direction dir) {
        List<BlockPos> out = new ArrayList<>();
        for (Direction direction : Direction.values()) {
            if (direction.getAxis() != dir.getAxis()) out.add(pos.relative(direction));
        }
        out.add(pos);
        return out;
    }
}

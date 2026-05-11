package com.gregtechceu.gtceu.common.item.tool.behavior;

import com.gregtechceu.gtceu.api.item.IGTTool;
import com.gregtechceu.gtceu.api.item.tool.behavior.IToolBehavior;
import com.gregtechceu.gtceu.api.item.tool.behavior.ToolBehaviorType;
import com.gregtechceu.gtceu.common.data.GTToolBehaviors;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.Tags;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;

public class ProspectingBehavior implements IToolBehavior<ProspectingBehavior> {

    public static final ProspectingBehavior INSTANCE = new ProspectingBehavior();
    public static final Codec<ProspectingBehavior> CODEC = Codec.unit(INSTANCE);
    public static final StreamCodec<ByteBuf, ProspectingBehavior> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public @NotNull InteractionResult onItemUse(UseOnContext context) {
        if (!(context.getItemInHand().getItem() instanceof IGTTool tool)) {
            return IToolBehavior.super.onItemUse(context);
        }
        int depth = tool.getProspectingDepth();
        findOres(context.getLevel(), context.getClickedPos(), context.getClickedFace(), depth).forEach(c -> {
            if (context.getPlayer() != null && context.getLevel().isClientSide()) {
                context.getPlayer().sendSystemMessage(c);
            }
        });
        return InteractionResult.SUCCESS;
    }

    private static List<Component> findOres(Level level, BlockPos pos, Direction direction, int depth) {
        BlockPos.MutableBlockPos mutable = pos.mutable();
        Set<BlockState> foundBlocks = new HashSet<>();
        Set<Fluid> foundFluids = new HashSet<>();
        for (int i = 0; i < depth; i++) {
            findAndTrackBlocks(level, mutable, direction, foundBlocks, foundFluids);

            if (direction.getAxis().isHorizontal()) {
                // use the mutable position here as well to allocate less BlockPos objects
                findAndTrackBlocks(level, mutable.move(Direction.DOWN), direction, foundBlocks, foundFluids);
                // shift the cursor back up after
                mutable.move(Direction.UP);
            }

            mutable.move(direction.getOpposite());
        }

        List<Component> result = new ArrayList<>();
        int found = 0;
        for (BlockState state : foundBlocks) {
            if (state.is(Tags.Blocks.ORES)) {
                result.add(Component.translatable("item.gtceu.tool.behavior.prospecting.found",
                        state.getBlock().getName()));
            } else if (state.isAir()) {
                result.add(Component.translatable("item.gtceu.tool.behavior.prospecting.air"));
            } else {
                found++;
            }
        }
        for (Fluid fluid : foundFluids) {
            result.add(Component.translatable("item.gtceu.tool.behavior.prospecting.found",
                    fluid.getFluidType().getDescription()));
        }
        if (found >= 2) {
            result.add(Component.translatable("item.gtceu.tool.behavior.prospecting.changing"));
        }
        return result;
    }

    private static void findAndTrackBlocks(Level level, BlockPos pos, Direction direction,
                                           Set<BlockState> foundBlocks, Set<Fluid> foundFluids) {
        for (BlockPos position : SURROUNDING_BLOCK_OFFSETS.apply(direction)) {
            position = pos.offset(position);
            if (!level.getFluidState(position).isEmpty()) {
                foundFluids.add(level.getFluidState(position).getType());
            } else {
                foundBlocks.add(level.getBlockState(position));
            }
        }
    }

    private static Function<Direction, List<BlockPos>> SURROUNDING_BLOCK_OFFSETS = Util
            .memoize(ProspectingBehavior::getSurroundingBlocks);

    private static List<BlockPos> getSurroundingBlocks(Direction dir) {
        List<BlockPos> out = new ArrayList<>();
        for (Direction direction : GTUtil.DIRECTIONS) {
            if (direction.getAxis() != dir.getAxis()) {
                out.add(new BlockPos(direction.getStepX(), direction.getStepY(), direction.getStepZ()));
            }
        }
        return out;
    }

    @Override
    public ToolBehaviorType<ProspectingBehavior> getType() {
        return GTToolBehaviors.PROSPECTING;
    }
}

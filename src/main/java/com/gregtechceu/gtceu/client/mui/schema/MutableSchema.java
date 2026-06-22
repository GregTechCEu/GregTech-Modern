package com.gregtechceu.gtceu.client.mui.schema;

import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.multiblock.pattern.PatternState;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import brachy.modularui.drawable.schema.ISchema;
import brachy.modularui.drawable.schema.SchemaLevel;
import brachy.modularui.utils.BlockPosUtil;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.longs.Long2ReferenceMap;
import it.unimi.dsi.fastutil.longs.Long2ReferenceOpenHashMap;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.*;

import static com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine.DEFAULT_STRUCTURE;

public class MutableSchema implements ISchema {

    @Getter
    protected final Level level = new SchemaLevel();
    @Getter
    protected @NotNull BlockPos origin = BlockPos.ZERO;
    protected @NotNull Vector3f center = new Vector3f();
    @Getter
    private BlockPos controllerPos = BlockPos.ZERO;
    @Getter
    private Pair<BlockPos, BlockPos> bounds;

    @Getter
    protected final Long2ReferenceMap<BlockState> blocks = new Long2ReferenceOpenHashMap<>();

    public MutableSchema() {}

    public MutableSchema(Long2ReferenceMap<BlockState> blocks) {
        this();
        setBlocks(blocks);
    }

    public MutableSchema setBlocks(Long2ReferenceMap<BlockState> blocks) {
        // clear world
        for (Map.Entry<BlockPos, BlockState> entry : this) {
            getLevel().removeBlock(entry.getKey(), false);
        }
        this.blocks.clear();

        BlockPos.MutableBlockPos min = BlockPosUtil.MAX.mutable();
        BlockPos.MutableBlockPos max = BlockPosUtil.MIN.mutable();
        MultiblockControllerMachine controller = null;
        List<IMultiPart> parts = new ArrayList<>();

        for (long packed : blocks.keySet()) {
            BlockState block = blocks.get(packed);
            if (block.isAir()) continue;
            BlockPos pos = BlockPos.of(packed);

            // BE creation is already handled through here
            updateBlockState(pos, block);
            BlockPosUtil.setMin(min, pos);
            BlockPosUtil.setMax(max, pos);

            BlockEntity blockEntity = getLevel().getBlockEntity(pos);
            if (blockEntity instanceof MultiblockControllerMachine mcm && controller == null) {
                controller = mcm;
                controllerPos = pos;
            } else if (blockEntity instanceof IMultiPart part) {
                parts.add(part);
            }
        }

        if (controller != null) {
            controller.getParts().addAll(parts);
            controller.getPatternState(DEFAULT_STRUCTURE).setState(PatternState.CheckState.VALID_UNCACHED);
            controller.formStructure(DEFAULT_STRUCTURE);
        }

        this.origin = min.immutable();
        this.center = BlockPosUtil.getCenterF(min, max);
        this.bounds = Pair.of(min, max);
        return this;
    }

    public MutableSchema updateBlockState(BlockPos pos, BlockState state) {
        this.blocks.put(pos.asLong(), state);
        getLevel().setBlockAndUpdate(pos, state);
        return this;
    }

    @Override
    public Vector3fc getFocus() {
        return center;
    }

    @Override
    public @NotNull Iterator<Map.Entry<BlockPos, BlockState>> iterator() {
        return blocks.long2ReferenceEntrySet().stream()
                .map(e -> Map.entry(BlockPos.of(e.getLongKey()), e.getValue()))
                .iterator();
    }
}

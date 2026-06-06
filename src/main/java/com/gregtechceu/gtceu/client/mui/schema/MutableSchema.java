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
import it.unimi.dsi.fastutil.longs.Long2ReferenceMap;
import it.unimi.dsi.fastutil.longs.Long2ReferenceOpenHashMap;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.*;

import static com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine.DEFAULT_STRUCTURE;

public class MutableSchema implements ISchema {

    protected final Level level = new SchemaLevel();
    protected @NotNull BlockPos origin = BlockPos.ZERO;
    protected @NotNull Vector3f center = new Vector3f();

    @Getter
    protected final Long2ReferenceMap<BlockState> blocks = new Long2ReferenceOpenHashMap<>();

    public MutableSchema() {}

    public MutableSchema(Long2ReferenceMap<BlockState> blocks) {
        this();
        setBlocks(blocks);
    }

    @Override
    public Level getLevel() {
        return this.level;
    }

    public MutableSchema setBlocks(Long2ReferenceMap<BlockState> blocks) {
        this.blocks.clear();
        BlockPos.MutableBlockPos min = BlockPosUtil.MAX.mutable();
        BlockPos.MutableBlockPos max = BlockPosUtil.MIN.mutable();
        MultiblockControllerMachine controller = null;
        List<IMultiPart> parts = new ArrayList<>();

        for (long l : blocks.keySet()) {
            if (blocks.get(l).isAir()) continue;
            BlockState block = blocks.get(l);
            BlockPos pos = BlockPos.of(l);
            this.blocks.put(l, block);

            // BE creation is already handled through here
            getLevel().setBlockAndUpdate(pos, block);
            BlockPosUtil.setMin(min, pos);
            BlockPosUtil.setMax(max, pos);

            BlockEntity blockEntity = getLevel().getBlockEntity(pos);
            if (blockEntity instanceof MultiblockControllerMachine mcm && controller == null) {
                controller = mcm;
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
    public BlockPos getOrigin() {
        return origin;
    }

    @Override
    public @NotNull Iterator<Map.Entry<BlockPos, BlockState>> iterator() {
        return blocks.long2ReferenceEntrySet().stream()
                .map(e -> Map.entry(BlockPos.of(e.getLongKey()), e.getValue()))
                .iterator();
    }
}

package com.lowdragmc.lowdraglib.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public class TrackedDummyWorld extends com.lowdragmc.lowdraglib2.utils.virtuallevel.TrackedDummyWorld {

    public TrackedDummyWorld() {
        super();
    }

    public TrackedDummyWorld(Level level) {
        super(level);
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void addBlocks(Map blocks) {
        Map<BlockPos, com.lowdragmc.lowdraglib2.utils.data.BlockInfo> converted = new HashMap<>();
        for (Object object : blocks.entrySet()) {
            Map.Entry entry = (Map.Entry) object;
            Object value = entry.getValue();
            converted.put((BlockPos) entry.getKey(), value instanceof BlockInfo blockInfo ? blockInfo.toLDLib2() :
                    (com.lowdragmc.lowdraglib2.utils.data.BlockInfo) value);
        }
        super.addBlocks(converted);
    }

    public void addBlock(BlockPos pos, BlockInfo blockInfo) {
        super.addBlock(pos, blockInfo.toLDLib2());
    }

    public void setBlockFilter(Predicate<BlockPos> filter) {
        super.setBlockFilter(filter);
    }

    public Level getLevel() {
        return this;
    }

    public void setInnerBlockEntity(BlockEntity blockEntity) {}
}

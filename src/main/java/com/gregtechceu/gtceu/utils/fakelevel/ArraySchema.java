package com.gregtechceu.gtceu.utils.fakelevel;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import com.google.common.collect.AbstractIterator;
import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.chars.CharArraySet;
import it.unimi.dsi.fastutil.chars.CharSet;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.tuple.MutablePair;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.BiPredicate;

public class ArraySchema implements ISchema {

    public static Builder builder() {
        return new Builder();
    }

    public static ArraySchema of(Entity entity, int radius) {
        return of(entity.level(), BlockPos.containing(entity.position()), radius);
    }

    public static ArraySchema of(Level level, BlockPos center, int radius) {
        int s = 2 * radius + 1;
        BlockInfo[][][] blocks = new BlockInfo[s][s][s];

        MutableBlockPos pos = center.offset(-radius, -radius, -radius).mutable();
        for (int x = 0; x < s; x++) {
            for (int y = 0; y < s; y++) {
                for (int z = 0; z < s; z++) {
                    blocks[x][y][z] = BlockInfo.of(level, pos);
                    pos.move(0, 0, 1);
                }
                pos.move(0, 1, -s);
            }
            pos.move(1, -s, 0);
        }
        return new ArraySchema(blocks);
    }

    public static ArraySchema of(Level level, Vec3 center, Vec3 p1, Vec3 p2) {
        // todo: do what screret said to refactor this,
        // (later me problem)
        int x0 = (int) Math.min(p1.x, p2.x);
        int y0 = (int) Math.min(p1.y, p2.y);
        int z0 = (int) Math.min(p1.z, p2.z);

        int x1 = (int) Math.max(p1.x, p2.x);
        int y1 = (int) Math.max(p1.y, p2.y);
        int z1 = (int) Math.max(p1.z, p2.z);
        x0--;
        y0--;
        z0--;
        BlockInfo[][][] blocks = new BlockInfo[x1 - x0][y1 - y0][z1 - z0];
        for (BlockPos pos : MutableBlockPos.betweenClosed(x0, y0, z0, x1, y1, z1)) {
            blocks[pos.getX() - x0][pos.getY() - y0][pos.getZ() - z0] = BlockInfo.of(level, pos);
        }
        return new ArraySchema(blocks);
    }

    @Getter
    private final Level level;
    private final BlockInfo[][][] blocks;
    @Getter
    @Setter
    private BiPredicate<BlockPos, BlockInfo> renderFilter = (__, ___) -> true;
    private final Vec3 center;

    public ArraySchema(BlockInfo[][][] blocks) {
        this.blocks = blocks;
        this.level = new DummyLevel();
        MutableBlockPos current = new MutableBlockPos();
        MutableBlockPos max = BlockPosUtil.MIN.mutable();
        for (int x = 0; x < blocks.length; x++) {
            for (int y = 0; y < blocks[x].length; y++) {
                for (int z = 0; z < blocks[x][y].length; z++) {
                    BlockInfo block = blocks[x][y][z];
                    if (block == null) continue;
                    current.set(x, y, z);
                    BlockPosUtil.setMax(max, current);
                    block.apply(this.level, current);
                }
            }
        }
        this.center = BlockPosUtil.getCenterD(BlockPos.ZERO, BlockPosUtil.add(max, 1, 1, 1));
    }

    @Override
    public Vec3 getFocus() {
        return center;
    }

    @Override
    public BlockPos getOrigin() {
        return BlockPos.ZERO;
    }

    @NotNull
    @Override
    public Iterator<Map.Entry<BlockPos, BlockInfo>> iterator() {
        return new AbstractIterator<>() {

            private final MutableBlockPos pos = new MutableBlockPos();
            private final MutablePair<BlockPos, BlockInfo> pair = new MutablePair<>(pos, null);
            private int x = 0, y = 0, z = -1;

            @Override
            protected Map.Entry<BlockPos, BlockInfo> computeNext() {
                BlockInfo info;
                while (true) {
                    if (++z >= blocks[x][y].length) {
                        z = 0;
                        if (++y >= blocks[x].length) {
                            y = 0;
                            if (++x >= blocks.length) {
                                return endOfData();
                            }
                        }
                    }
                    pos.set(x, y, z);
                    info = blocks[x][y][z];
                    if (info != null && renderFilter.test(pos, info)) {
                        pair.setRight(info);
                        return pair;
                    }
                }
            }
        };
    }

    public static class Builder {

        private final List<String[]> tensor = new ArrayList<>();
        private final Char2ObjectMap<BlockInfo> blockMap = new Char2ObjectOpenHashMap<>();

        public Builder() {
            blockMap.put(' ', BlockInfo.EMPTY);
            blockMap.put('#', BlockInfo.EMPTY);
        }

        public Builder layer(String... layer) {
            this.tensor.add(layer);
            return this;
        }

        public Builder where(char c, BlockInfo info) {
            this.blockMap.put(c, info);
            return this;
        }

        public Builder whereAir(char c) {
            return where(c, BlockInfo.EMPTY);
        }

        public Builder where(char c, BlockState blockState) {
            return where(c, new BlockInfo(blockState));
        }

        public Builder where(char c, BlockState blockState, BlockEntity tile) {
            return where(c, new BlockInfo(blockState, tile));
        }

        public Builder where(char c, Block block) {
            return where(c, new BlockInfo(block));
        }

        public Builder where(char c, ResourceLocation registryName) {
            Optional<Block> block = BuiltInRegistries.BLOCK.getOptional(registryName);
            if (block.isEmpty())
                throw new IllegalArgumentException("Block with name " + registryName + " doesn't exist!");
            BlockState state = block.get().defaultBlockState();
            return where(c, new BlockInfo(state));
        }

        private void validate() {
            if (this.tensor.isEmpty()) {
                throw new IllegalArgumentException("no block matrix defined");
            }
            List<String> errors = new ArrayList<>();
            CharSet checkedChars = new CharArraySet();
            int layerSize = this.tensor.get(0).length;
            for (int x = 0; x < this.tensor.size(); x++) {
                String[] xLayer = this.tensor.get(x);
                if (xLayer.length == 0) {
                    errors.add(String.format("Layer %s is empty. This is not right", x + 1));
                } else if (xLayer.length != layerSize) {
                    errors.add(String.format("Invalid x-layer size. Expected %s, but got %s at layer %s", layerSize,
                            xLayer.length, x + 1));
                }
                int rowSize = xLayer[0].length();
                for (int y = 0; y < xLayer.length; y++) {
                    String yRow = xLayer[y];
                    if (yRow.isEmpty()) {
                        errors.add(String.format("Row %s in layer %s is empty. This is not right", y + 1, x + 1));
                    } else if (yRow.length() != rowSize) {
                        errors.add(String.format("Invalid x-layer size. Expected %s, but got %s at row %s in layer %s",
                                layerSize, xLayer.length, y + 1, x + 1));
                    }
                    for (int z = 0; z < yRow.length(); z++) {
                        char zChar = yRow.charAt(z);
                        if (!checkedChars.contains(zChar)) {
                            if (!this.blockMap.containsKey(zChar)) {
                                errors.add(String.format(
                                        "Found char '%s' at char %s in row %s in layer %s, but character was not found in map!",
                                        zChar, z + 1, y + 1, x + 1));
                            }
                            checkedChars.add(zChar);
                        }
                    }
                }
            }
            if (!errors.isEmpty()) {
                GTCEu.LOGGER.error("Error validating ArrayScheme BlockArray:");
                for (String e : errors) GTCEu.LOGGER.error("  - {}", e);
                throw new IllegalArgumentException("The ArraySchema builder was misconfigured. See message above.");
            }
        }

        public ArraySchema build() {
            validate();
            BlockInfo[][][] blocks = new BlockInfo[this.tensor.size()][this.tensor.get(0).length][this.tensor.get(0)[0]
                    .length()];
            for (int x = 0; x < this.tensor.size(); x++) {
                String[] xLayer = this.tensor.get(x);
                for (int y = 0; y < xLayer.length; y++) {
                    String yRow = xLayer[y];
                    for (int z = 0; z < yRow.length(); z++) {
                        char zChar = yRow.charAt(z);
                        BlockInfo info = this.blockMap.get(zChar);
                        // null -> any allowed -> don't need to check
                        if (info == null || info == BlockInfo.EMPTY) continue;
                        blocks[x][y][z] = info;
                    }
                }
            }
            return new ArraySchema(blocks);
        }
    }
}

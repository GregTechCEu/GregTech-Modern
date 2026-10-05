package com.gregtechceu.gtceu.api.mui;

import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.multiblock.MultiPredicate;
import com.gregtechceu.gtceu.api.multiblock.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.multiblock.pattern.ExpandablePattern;
import com.gregtechceu.gtceu.api.multiblock.pattern.IBlockPattern;
import com.gregtechceu.gtceu.api.multiblock.predicates.BasePredicate;
import com.gregtechceu.gtceu.api.multiblock.util.AbstractStructureHelper;
import com.gregtechceu.gtceu.api.multiblock.util.BlockInfo;
import com.gregtechceu.gtceu.api.sync_system.data_transformers.SyncSystemCodecs;
import com.gregtechceu.gtceu.client.mui.schema.MutableSchema;
import com.gregtechceu.gtceu.utils.codec.GTCodecUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import brachy.modularui.drawable.SchemaRenderer;
import brachy.modularui.widgets.SchemaWidget;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Table;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.chars.Char2ObjectArrayMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.fastutil.longs.Long2ReferenceMap;
import it.unimi.dsi.fastutil.longs.Long2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine.DEFAULT_STRUCTURE;

public class MultiblockSchemaInfo {

    // spotless:off
    public static final Codec<MultiblockSchemaInfo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            MultiblockMachineDefinition.CODEC.fieldOf("definition").forGetter(MultiblockSchemaInfo::getDefinition),
            GTCodecUtils.map(Codec.INT, Codec.INT).fieldOf("userSliceRepeats").forGetter(MultiblockSchemaInfo::getUserSliceRepeats),
            Codec.INT.listOf().fieldOf("userDimensions").forGetter(MultiblockSchemaInfo::getUserDimensions),
            GTCodecUtils.map(BlockPos.CODEC, BlockInfo.CODEC).fieldOf("userGlobalBlockPreferences").forGetter(MultiblockSchemaInfo::getUserGlobalBlockPreferences),
            GTCodecUtils.map(SyncSystemCodecs.CHAR, BlockInfo.CODEC).fieldOf("blockPreferences").forGetter(MultiblockSchemaInfo::getBlockPreferences)
            //GTCodecUtils.table(SyncSystemCodecs.CHAR, Codec.INT, GTCodecUtils.FAST_UTIL_INT_PAIR_CODEC).fieldOf("minMaxPreferences").forGetter(MultiblockSchemaInfo::getMinMaxPreferenceCharTable)
    ).apply(instance, MultiblockSchemaInfo::new));
    //spotless:on

    @Getter
    private final MultiblockMachineDefinition definition;
    @Getter
    @Setter
    private SchemaWidget multiSchema;
    @Getter
    @Setter
    private MutableSchema mapSchema;
    @Getter
    @Setter
    private SchemaRenderer renderer;
    @Getter
    @Setter
    private Reference2IntMap<Block> blockCounts = new Reference2IntOpenHashMap<>();
    @Getter
    private final Object2ObjectMap<BlockPos, BlockInfo> userGlobalBlockPreferences;
    @Getter
    protected final Char2ObjectMap<BlockInfo> blockPreferences = new Char2ObjectArrayMap<>();
    @Getter
    protected final HashBasedTable<MultiPredicate, BasePredicate, IntIntPair> minMaxPreferences = HashBasedTable
            .create();

    @Getter
    private Int2IntMap userSliceRepeats;
    @Getter
    private final IntList userDimensions;
    @Getter
    private final Map<BlockPos, BlockInfo> structureBlocks = new HashMap<>();

    @Getter
    private @Nullable AbstractStructureHelper structureHelper;

    public MultiblockSchemaInfo(MultiblockMachineDefinition definition) {
        this.definition = definition;
        this.userSliceRepeats = new Int2IntArrayMap();
        this.userDimensions = new IntArrayList();
        this.userGlobalBlockPreferences = new Object2ObjectOpenHashMap<>();
    }

    public MultiblockSchemaInfo(MultiblockMachineDefinition definition, Map<Integer, Integer> userSliceRepeats,
                                List<Integer> userDimensions,
                                Map<BlockPos, BlockInfo> userGlobalBlockPreferences,
                                Map<Character, BlockInfo> blockPreferenceMap) {
        this.definition = definition;
        this.userSliceRepeats = new Int2IntArrayMap(userSliceRepeats);
        this.userDimensions = new IntArrayList(userDimensions);
        this.userGlobalBlockPreferences = new Object2ObjectOpenHashMap<>(userGlobalBlockPreferences);

        IBlockPattern pattern = definition.getStructurePatterns()
                .get(DEFAULT_STRUCTURE).get();

        blockPreferences.putAll(blockPreferenceMap);

        /*
         * if (pattern instanceof BlockPattern blockPattern) {
         * for (var entry : minMaxPreferences.cellSet()) {
         * MultiPredicate pred = blockPattern.getPredicates().get(entry.getRowKey());
         * BasePredicate base = pred.predicates().get(entry.getColumnKey());
         * getMinMaxPreferences().put(pred, base, entry.getValue());
         * }
         * }
         */
    }

    public Table<Character, Integer, IntIntPair> getMinMaxPreferenceCharTable() {
        IBlockPattern pattern = definition.getStructurePatterns()
                .get(DEFAULT_STRUCTURE).get();

        Table<Character, Integer, IntIntPair> table = HashBasedTable.create();

        var predicateInverseMap = HashBiMap.create(pattern.getPredicates()).inverse();
        for (var entry : getMinMaxPreferences().cellSet()) {
            var row = predicateInverseMap.get(entry.getRowKey());
            var col = entry.getRowKey().predicates().indexOf(entry.getColumnKey());
            table.put(row, col, entry.getValue());
        }

        return table;
    }

    @ApiStatus.Internal
    public void refreshSchema(MultiblockMachineDefinition multiblockDefinition, Direction frontFacing,
                              Direction upFacing, boolean isFlipped, @Nullable Runnable onSchemaRefresh) {
        Map<BlockPos, BlockInfo> resultStructure = new HashMap<>();
        IBlockPattern pattern = multiblockDefinition.getStructurePatterns().get(DEFAULT_STRUCTURE).get();

        if (this.structureHelper == null) {
            if (pattern instanceof BlockPattern blockPattern) {
                if (this.userSliceRepeats.isEmpty()) {
                    for (int i = 0; i < blockPattern.getSlices().length; i++) {
                        this.userSliceRepeats.put(i, blockPattern.getSlices()[i].getMinRepeats());
                    }
                }
                // reinterpret slider values as slice repeats?
                this.structureHelper = AbstractStructureHelper.blockPattern(this.userSliceRepeats);

            } else if (pattern instanceof ExpandablePattern expandablePattern) {
                if (this.userDimensions.isEmpty()) {
                    expandablePattern.getBoundsConstraints().apply().stream()
                            .mapToInt(Pair::left)
                            .forEach(this.userDimensions::add);
                }
                // reinterpret slider values as bounds?
                this.structureHelper = AbstractStructureHelper.expandable(this.userDimensions);

            } else {
                // throw? log?
                return;
            }
        }

        this.structureHelper.populate(this, resultStructure, pattern, this.userGlobalBlockPreferences,
                frontFacing, upFacing, isFlipped);

        Long2ReferenceMap<BlockState> schemaMap = new Long2ReferenceOpenHashMap<>();
        this.blockCounts.clear();
        for (var entry : resultStructure.entrySet()) {
            BlockState state = entry.getValue().getBlockState();
            schemaMap.put(entry.getKey().asLong(), state);
            this.blockCounts.merge(state.getBlock(), 1, Integer::sum);
        }
        if (this.mapSchema == null) {
            this.mapSchema = new MutableSchema(schemaMap);
        } else {
            this.mapSchema.setBlocks(schemaMap);
        }
        this.structureBlocks.clear();
        this.structureBlocks.putAll(resultStructure);

        if (onSchemaRefresh != null) {
            onSchemaRefresh.run();
        }
    }

    public int getMinCount(MultiPredicate predicate, BasePredicate basePredicate) {
        if (!minMaxPreferences.contains(predicate, basePredicate))
            return Math.max(predicate.getMinCount(), basePredicate.getMinCount());
        return minMaxPreferences.get(predicate, basePredicate).leftInt();
    }

    public int getMaxCount(MultiPredicate predicate, BasePredicate basePredicate) {
        if (!minMaxPreferences.contains(predicate, basePredicate)) {
            if (predicate.getMaxCount() == -1) return basePredicate.getMaxCount();
            if (basePredicate.getMaxCount() == -1) return predicate.getMaxCount();
            return Math.min(predicate.getMaxCount(), basePredicate.getMaxCount());
        }
        return minMaxPreferences.get(predicate, basePredicate).rightInt();
    }

    public int getMinSliceCount(MultiPredicate predicate, BasePredicate basePredicate) {
        if (!minMaxPreferences.contains(predicate, basePredicate))
            return Math.max(predicate.getMinSliceCount(), basePredicate.getMinSliceCount());
        return minMaxPreferences.get(predicate, basePredicate).leftInt();
    }

    public int getMaxSliceCount(MultiPredicate predicate, BasePredicate basePredicate) {
        if (!minMaxPreferences.contains(predicate, basePredicate)) {
            if (predicate.getMaxSliceCount() == -1) return basePredicate.getMaxSliceCount();
            if (basePredicate.getMaxSliceCount() == -1) return predicate.getMaxSliceCount();
            return Math.min(predicate.getMaxSliceCount(), basePredicate.getMaxSliceCount());
        }
        return minMaxPreferences.get(predicate, basePredicate).rightInt();
    }

    public void clearUserPreferences() {
        this.userSliceRepeats.clear();
        this.userDimensions.clear();
    }
}

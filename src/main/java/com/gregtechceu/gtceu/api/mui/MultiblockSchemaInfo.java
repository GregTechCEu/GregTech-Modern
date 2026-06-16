package com.gregtechceu.gtceu.api.mui;

import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.multiblock.PatternPredicate;
import com.gregtechceu.gtceu.api.multiblock.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.multiblock.pattern.ExpandablePattern;
import com.gregtechceu.gtceu.api.multiblock.pattern.IBlockPattern;
import com.gregtechceu.gtceu.api.multiblock.predicates.BasePredicate;
import com.gregtechceu.gtceu.api.multiblock.util.BlockInfo;
import com.gregtechceu.gtceu.api.multiblock.util.BlockPatternStructureHelper;
import com.gregtechceu.gtceu.api.multiblock.util.ExpandablePatternStructureHelper;
import com.gregtechceu.gtceu.client.mui.schema.MutableSchema;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import brachy.modularui.drawable.SchemaRenderer;
import brachy.modularui.widgets.SchemaWidget;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ReferenceMap;
import it.unimi.dsi.fastutil.longs.Long2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

import static com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine.DEFAULT_STRUCTURE;

public class MultiblockSchemaInfo {

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
    private final Reference2IntMap<Block> blockCounts = new Reference2IntOpenHashMap<>();
    @Getter
    private final Long2ObjectMap<BlockInfo> userGlobalBlockPreferences = new Long2ObjectOpenHashMap<>();
    @Getter
    private final Table<PatternPredicate, BasePredicate, BlockInfo> userBasePredicateBlockPreferences = HashBasedTable
            .create();
    @Getter
    private final Table<PatternPredicate, BasePredicate, IntIntPair> userBasePredicateMinMaxPreferences = HashBasedTable
            .create();
    @Getter
    private Int2IntMap userSliceRepeats = new Int2IntArrayMap();
    @Getter
    @Setter
    private IntList userDimensions = IntLists.emptyList();
    @Getter
    private final Map<BlockPos, BlockInfo> structureBlocks = new HashMap<>();

    @Getter
    private @Nullable BlockPatternStructureHelper structureHelper;
    @Getter
    private @Nullable ExpandablePatternStructureHelper expandableStructureHelper;

    @ApiStatus.Internal
    public void refreshSchema(MultiblockMachineDefinition multiblockDefinition, Direction frontFacing,
                              Direction upFacing, boolean isFlipped, @Nullable Runnable onSchemaRefresh) {
        Map<BlockPos, BlockInfo> resultStructure = new HashMap<>();
        IBlockPattern pattern = multiblockDefinition.getStructurePatterns().get(DEFAULT_STRUCTURE).get();

        if (pattern instanceof BlockPattern blockPattern) {
            if (this.userSliceRepeats.isEmpty()) {
                for (int i = 0; i < blockPattern.getSlices().length; i++) {
                    this.userSliceRepeats.put(i, blockPattern.getSlices()[i].getMinRepeats());
                }
            }
            // reinterpret slider values as slice repeats?
            this.structureHelper = new BlockPatternStructureHelper(this.userBasePredicateBlockPreferences,
                    this.userBasePredicateMinMaxPreferences, this.userSliceRepeats);
            char[][][] flattenedCharPattern = this.structureHelper.flattenBlockPattern(blockPattern);
            char[][][] adjustedCharPattern = BlockPatternStructureHelper.rotateAndFlipPattern(flattenedCharPattern,
                    blockPattern.getDirections(),
                    frontFacing, upFacing, isFlipped);

            this.structureHelper.populateWithUserBlockPreferences(resultStructure, blockPattern, adjustedCharPattern,
                    this.userGlobalBlockPreferences,
                    frontFacing, upFacing, isFlipped);

            this.structureHelper.populateFromPattern(resultStructure, blockPattern, adjustedCharPattern,
                    frontFacing, upFacing, isFlipped);

            BlockPatternStructureHelper.fixRotationsAndFacing(resultStructure, frontFacing, upFacing,
                    multiblockDefinition.getBlock());
        } else if (pattern instanceof ExpandablePattern expandablePattern) {
            if (this.userDimensions.isEmpty()) {
                this.userDimensions = expandablePattern.getBoundsConstraints().apply().stream()
                        .mapToInt(Pair::left)
                        .collect(IntArrayList::new, IntList::add, IntList::addAll);
            }
            // reinterpret slider values as bounds?
            this.expandableStructureHelper = new ExpandablePatternStructureHelper(
                    this.userBasePredicateBlockPreferences,
                    this.userBasePredicateMinMaxPreferences, this.userDimensions);

            this.expandableStructureHelper.populateWithUserBlockPreferences(resultStructure, expandablePattern,
                    this.userGlobalBlockPreferences, frontFacing, upFacing, isFlipped);

            this.expandableStructureHelper.populateFromPattern(resultStructure, expandablePattern, frontFacing,
                    upFacing, isFlipped);

            BlockPatternStructureHelper.fixRotationsAndFacing(resultStructure, frontFacing, upFacing,
                    multiblockDefinition.getBlock());
        }

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
}

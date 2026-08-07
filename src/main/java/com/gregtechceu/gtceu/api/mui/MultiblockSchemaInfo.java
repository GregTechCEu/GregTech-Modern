package com.gregtechceu.gtceu.api.mui;

import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.multiblock.PatternPredicate;
import com.gregtechceu.gtceu.api.multiblock.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.multiblock.pattern.ExpandablePattern;
import com.gregtechceu.gtceu.api.multiblock.pattern.IBlockPattern;
import com.gregtechceu.gtceu.api.multiblock.predicates.BasePredicate;
import com.gregtechceu.gtceu.api.multiblock.util.AbstractStructureHelper;
import com.gregtechceu.gtceu.api.multiblock.util.BlockInfo;
import com.gregtechceu.gtceu.client.mui.schema.MutableSchema;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import brachy.modularui.drawable.SchemaRenderer;
import brachy.modularui.widgets.SchemaWidget;
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
    private final Int2IntMap userSliceRepeats = new Int2IntArrayMap();
    @Getter
    private final IntList userDimensions = new IntArrayList();
    @Getter
    private final Map<BlockPos, BlockInfo> structureBlocks = new HashMap<>();

    @Getter
    private @Nullable AbstractStructureHelper structureHelper;

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

        this.structureHelper.populate(resultStructure, pattern, this.userGlobalBlockPreferences,
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

    public void clearUserPreferences() {
        this.userSliceRepeats.clear();
        this.userDimensions.clear();
    }

    public void putPredicatePreference(PatternPredicate predicate, BasePredicate basePredicate, BlockInfo info) {
        this.structureHelper.getBlockPreferences().put(predicate, basePredicate, info);
    }
}

package com.gregtechceu.gtceu.api.machine;

import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.multiblock.pattern.IBlockPattern;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.utils.memoization.GTMemoizer;

import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import brachy.modularui.api.widget.IWidget;
import brachy.modularui.value.sync.PanelSyncManager;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import io.netty.handler.codec.DecoderException;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.function.TriFunction;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class MultiblockMachineDefinition extends MachineDefinition {

    // spotless:off
    public static final Codec<MultiblockMachineDefinition> CODEC = GTRegistries.MACHINES.byNameCodec().comapFlatMap(def -> {
        if (def instanceof MultiblockMachineDefinition mDef) return DataResult.success(mDef);
        else return DataResult.error(() -> "%s is not a multiblock machine definition".formatted(def.getId()));
    }, v -> v);

    public static final StreamCodec<RegistryFriendlyByteBuf, MultiblockMachineDefinition> STREAM_CODEC = ByteBufCodecs.registry(GTRegistries.Keys.MACHINE)
            .map(v -> {
                if (v instanceof MultiblockMachineDefinition multiblockMachineDefinition) {
                    return multiblockMachineDefinition;
                }
                throw new DecoderException("%s is not a multiblock machine definition".formatted(v.getId()));
            }, UnaryOperator.identity());
    //spotless:on

    @Getter
    private final boolean generator;
    @Getter
    private final Map<String, Supplier<IBlockPattern>> structurePatterns;
    @Getter
    private final boolean allowFlip;
    @Getter
    private final boolean renderXEIPreview;
    @Getter
    private final boolean renderWorldPreview;
    @Getter
    private final Supplier<ItemStack[]> recoveryItems;
    @Getter
    private final Function<MultiblockControllerMachine, Comparator<MultiblockPartMachine>> partSorter;
    @Getter
    private final TriFunction<MultiblockControllerMachine, MultiblockPartMachine, Direction, BlockState> partAppearance;
    @Getter
    private final BiFunction<MultiblockControllerMachine, PanelSyncManager, List<IWidget>> additionalDisplay;

    public MultiblockMachineDefinition(ResourceLocation id, Properties properties) {
        super(id, properties);
        this.generator = properties.generator;

        Map<String, Supplier<IBlockPattern>> structurePatterns = new Object2ObjectOpenHashMap<>();
        if (properties.patterns().isEmpty()) {
            throw new IllegalStateException("Missing default structure pattern for " + getName());
        }
        for (Map.Entry<String, Function<MultiblockMachineDefinition, IBlockPattern>> entry : properties.patterns
                .entrySet()) {
            structurePatterns.put(entry.getKey(), GTMemoizer.memoize(() -> entry.getValue().apply(this)));
        }
        this.structurePatterns = Collections.unmodifiableMap(structurePatterns);
        this.allowFlip = properties.allowFlip();
        this.renderXEIPreview = properties.renderMultiblockXEIPreview;
        this.renderWorldPreview = properties.renderMultiblockWorldPreview;
        this.recoveryItems = () -> properties.recoveryItems.stream().map(Supplier::get).flatMap(Arrays::stream)
                .toArray(ItemStack[]::new);
        this.partSorter = GTMemoizer.memoizeFunctionWeakIdent(properties.partSorter());
        this.partAppearance = properties.partAppearance();
        this.additionalDisplay = properties.additionalDisplay();
    }

    @Accessors(fluent = true)
    @Getter
    @Setter
    public static class Properties extends MachineDefinition.Properties {

        private boolean generator = false;
        private final Map<String, Function<MultiblockMachineDefinition, IBlockPattern>> patterns = new Object2ReferenceOpenHashMap<>();
        private boolean allowFlip = true;
        private final List<Supplier<ItemStack[]>> recoveryItems = new ArrayList<>();
        private Function<MultiblockControllerMachine, Comparator<MultiblockPartMachine>> partSorter = (c) -> (a,
                                                                                                              b) -> 0;
        private TriFunction<MultiblockControllerMachine, MultiblockPartMachine, Direction, BlockState> partAppearance = (controller,
                                                                                                                         part,
                                                                                                                         side) -> controller
                                                                                                                                 .getDefinition()
                                                                                                                                 .getAppearance()
                                                                                                                                 .get();
        private boolean renderMultiblockWorldPreview = true;
        private boolean renderMultiblockXEIPreview = true;
        private BiFunction<MultiblockControllerMachine, PanelSyncManager, List<IWidget>> additionalDisplay = (m,
                                                                                                              sm) -> Collections
                                                                                                                      .emptyList();
    }
}

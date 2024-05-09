package com.gregtechceu.gtceu.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.covers.filter.SimpleFluidFilter;
import com.gregtechceu.gtceu.api.covers.filter.SimpleItemFilter;
import com.gregtechceu.gtceu.api.covers.filter.TagFluidFilter;
import com.gregtechceu.gtceu.api.covers.filter.TagItemFilter;
import com.gregtechceu.gtceu.api.items.component.IMaterialPartItem;
import com.gregtechceu.gtceu.api.items.datacomponents.*;
import com.gregtechceu.gtceu.utils.ResearchManager;
import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Unit;
import net.neoforged.neoforge.fluids.SimpleFluidContent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class GTDataComponents {
    public static final DeferredRegister.DataComponents DATA_COMPONENTS = DeferredRegister.createDataComponents(GTCEu.MOD_ID);

    // Tool-related
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<GTTool>> GT_TOOL = DATA_COMPONENTS.registerComponentType("gt_tool", builder -> builder.persistent(GTTool.CODEC).networkSynchronized(GTTool.STREAM_CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ToolBehaviorsComponent>> TOOL_BEHAVIOURS = DATA_COMPONENTS.registerComponentType("tool_behaviours", builder -> builder.persistent(ToolBehaviorsComponent.CODEC).networkSynchronized(ToolBehaviorsComponent.STREAM_CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<AoESymmetrical>> AOE = DATA_COMPONENTS.registerComponentType("aoe", builder -> builder.persistent(AoESymmetrical.CODEC).networkSynchronized(AoESymmetrical.STREAM_CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Unit>> DISALLOW_CONTAINER_ITEM = DATA_COMPONENTS.registerComponentType("disallow_container_item", builder -> builder.persistent(Codec.unit(Unit.INSTANCE)).networkSynchronized(StreamCodec.unit(Unit.INSTANCE)));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Unit>> RELOCATE_MINED_BLOCKS = DATA_COMPONENTS.registerComponentType("relocate_mined_blocks", builder -> builder.persistent(Codec.unit(Unit.INSTANCE)).networkSynchronized(StreamCodec.unit(Unit.INSTANCE)));

    // Material-related
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<IMaterialPartItem.PartStats>> PART_STATS = DATA_COMPONENTS.registerComponentType("part_stats", builder -> builder.persistent(IMaterialPartItem.PartStats.CODEC).networkSynchronized(IMaterialPartItem.PartStats.STREAM_CODEC));

    // component item-related
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ResearchManager.ResearchItem>> RESEARCH_ITEM = DATA_COMPONENTS.registerComponentType("research_item", builder -> builder.persistent(ResearchManager.ResearchItem.CODEC).networkSynchronized(ResearchManager.ResearchItem.STREAM_CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> ITEM_MAGNET = DATA_COMPONENTS.registerComponentType("item_magnet", builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Byte>> SCANNER_MODE = DATA_COMPONENTS.registerComponentType("scanner_mode", builder -> builder.persistent(Codec.BYTE).networkSynchronized(ByteBufCodecs.BYTE));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<SimpleFluidFilter>> SIMPLE_FLUID_FILTER = DATA_COMPONENTS.registerComponentType("simple_fluid_filter", builder -> builder.persistent(SimpleFluidFilter.CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<TagFluidFilter>> TAG_FLUID_FILTER = DATA_COMPONENTS.registerComponentType("tag_fluid_filter", builder -> builder.persistent(TagFluidFilter.CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<SimpleItemFilter>> SIMPLE_ITEM_FILTER = DATA_COMPONENTS.registerComponentType("simple_item_filter", builder -> builder.persistent(SimpleItemFilter.CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<TagItemFilter>> TAG_ITEM_FILTER = DATA_COMPONENTS.registerComponentType("tag_item_filter", builder -> builder.persistent(TagItemFilter.CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> CIRCUIT_CONFIG = DATA_COMPONENTS.registerComponentType("circuit_config", builder -> builder.persistent(Codec.INT));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<SimpleFluidContent>> FLUID_CONTENT = DATA_COMPONENTS.registerComponentType("fluid_content", builder -> builder.persistent(SimpleFluidContent.CODEC).networkSynchronized(SimpleFluidContent.STREAM_CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<SimpleEnergyContent>> ENERGY_CONTENT = DATA_COMPONENTS.registerComponentType("energy_content", builder -> builder.persistent(SimpleEnergyContent.CODEC).networkSynchronized(SimpleEnergyContent.STREAM_CODEC));

    // misc
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<FacadeWrapper>> FACADE = DATA_COMPONENTS.registerComponentType("facade", builder -> builder.persistent(FacadeWrapper.CODEC).networkSynchronized(FacadeWrapper.STREAM_CODEC));

}

package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.item.capability.ElectricItem;
import com.gregtechceu.gtceu.api.item.component.IMaterialPartItem;
import com.gregtechceu.gtceu.api.item.components.AoESymmetrical;
import com.gregtechceu.gtceu.api.item.components.GTTool;
import com.gregtechceu.gtceu.api.item.components.ToolBehaviorsComponent;
import com.gregtechceu.gtceu.api.item.components.ToolCharge;
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
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Unit>> TREE_FELLING = DATA_COMPONENTS.registerComponentType("tree_felling", builder -> builder.persistent(Codec.unit(Unit.INSTANCE)).networkSynchronized(StreamCodec.unit(Unit.INSTANCE)));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Unit>> DISALLOW_CONTAINER_ITEM = DATA_COMPONENTS.registerComponentType("disallow_container_item", builder -> builder.persistent(Codec.unit(Unit.INSTANCE)).networkSynchronized(StreamCodec.unit(Unit.INSTANCE)));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Unit>> RELOCATE_MINED_BLOCKS = DATA_COMPONENTS.registerComponentType("relocate_mined_blocks", builder -> builder.persistent(Codec.unit(Unit.INSTANCE)).networkSynchronized(StreamCodec.unit(Unit.INSTANCE)));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ToolCharge>> TOOL_CHARGE = DATA_COMPONENTS.registerComponentType("tool_charge", builder -> builder.persistent(ToolCharge.CODEC).networkSynchronized(ToolCharge.STREAM_CODEC));

    // Material-related
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<IMaterialPartItem.PartStats>> PART_STATS = DATA_COMPONENTS.registerComponentType("part_stats", builder -> builder.persistent(IMaterialPartItem.PartStats.CODEC).networkSynchronized(IMaterialPartItem.PartStats.STREAM_CODEC));

    // misc
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ResearchManager.ResearchItem>> RESEARCH_ITEM = DATA_COMPONENTS.registerComponentType("research_item", builder -> builder.persistent(ResearchManager.ResearchItem.CODEC).networkSynchronized(ResearchManager.ResearchItem.STREAM_CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ElectricItem>> ELECTRIC_ITEM = DATA_COMPONENTS.registerComponentType("electric_item", builder -> builder.persistent(ElectricItem.CODEC).networkSynchronized(ElectricItem.STREAM_CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> ITEM_MAGNET = DATA_COMPONENTS.registerComponentType("item_magnet", builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Byte>> SCANNER_MODE = DATA_COMPONENTS.registerComponentType("scanner_mode", builder -> builder.persistent(Codec.BYTE).networkSynchronized(ByteBufCodecs.BYTE));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<SimpleFluidContent>> FLUID_CONTENT = DATA_COMPONENTS.registerComponentType("fluid_content", builder -> builder.persistent(SimpleFluidContent.CODEC).networkSynchronized(SimpleFluidContent.STREAM_CODEC));
}

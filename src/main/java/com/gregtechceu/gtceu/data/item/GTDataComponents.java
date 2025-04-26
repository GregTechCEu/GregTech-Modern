package com.gregtechceu.gtceu.data.item;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.cover.filter.SimpleFluidFilter;
import com.gregtechceu.gtceu.api.cover.filter.SimpleItemFilter;
import com.gregtechceu.gtceu.api.cover.filter.SmartItemFilter;
import com.gregtechceu.gtceu.api.item.LampBlockItem;
import com.gregtechceu.gtceu.api.item.datacomponents.*;
import com.gregtechceu.gtceu.api.material.material.Material;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.item.behavior.ItemMagnetBehavior;
import com.gregtechceu.gtceu.common.item.tool.behavior.ToolModeSwitchBehavior;
import com.gregtechceu.gtceu.utils.ResearchManager;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Unit;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.neoforge.fluids.SimpleFluidContent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;

public class GTDataComponents {

    private static final StreamCodec<ByteBuf, Unit> UNIT_STREAM_CODEC = StreamCodec.unit(Unit.INSTANCE);
    public static final DeferredRegister.DataComponents DATA_COMPONENTS = DeferredRegister
            .createDataComponents(Registries.DATA_COMPONENT_TYPE, GTCEu.MOD_ID);

    // Tool-related
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<GTTool>> GT_TOOL = DATA_COMPONENTS
            .registerComponentType("gt_tool",
                    builder -> builder.persistent(GTTool.CODEC).networkSynchronized(GTTool.STREAM_CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ToolBehaviors>> TOOL_BEHAVIORS = DATA_COMPONENTS
            .registerComponentType("tool_behaviors", builder -> builder.persistent(ToolBehaviors.CODEC)
                    .networkSynchronized(ToolBehaviors.STREAM_CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<AoESymmetrical>> AOE = DATA_COMPONENTS
            .registerComponentType("aoe", builder -> builder.persistent(AoESymmetrical.CODEC)
                    .networkSynchronized(AoESymmetrical.STREAM_CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Unit>> DISALLOW_CONTAINER_ITEM = DATA_COMPONENTS
            .registerComponentType("disallow_container_item", builder -> builder.persistent(Unit.CODEC)
                    .networkSynchronized(UNIT_STREAM_CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Unit>> RELOCATE_MINED_BLOCKS = DATA_COMPONENTS
            .registerComponentType("relocate_mined_blocks", builder -> builder.persistent(Unit.CODEC)
                    .networkSynchronized(UNIT_STREAM_CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Unit>> RELOCATE_MOB_DROPS = DATA_COMPONENTS
            .registerComponentType("relocate_mob_drops", builder -> builder.persistent(Unit.CODEC)
                    .networkSynchronized(UNIT_STREAM_CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> ACTIVE = DATA_COMPONENTS
            .registerComponentType("active", builder -> builder.persistent(Codec.BOOL)
                    .networkSynchronized(ByteBufCodecs.BOOL));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ToolModeSwitchBehavior.ModeType>> TOOL_MODE = DATA_COMPONENTS
            .registerComponentType("tool_mode", builder -> builder
                    .persistent(ToolModeSwitchBehavior.ModeType.CODEC)
                    .networkSynchronized(ToolModeSwitchBehavior.ModeType.STREAM_CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ItemEnchantments>> INNATE_ENCHANTMENTS = DATA_COMPONENTS
            .registerComponentType("innate_enchantments", builder -> builder
                    .persistent(ItemEnchantments.CODEC)
                    .networkSynchronized(ItemEnchantments.STREAM_CODEC)
                    .cacheEncoding());

    // Material-related
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Material>> ITEM_MATERIAL = DATA_COMPONENTS
            .registerComponentType("item_material", builder -> builder
                    .persistent(GTRegistries.MATERIALS.byNameCodec())
                    .networkSynchronized(GTRegistries.MATERIALS.streamCodec()));

    // Armor-related
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<GTArmor>> ARMOR_DATA = DATA_COMPONENTS
            .registerComponentType("armor",
                    builder -> builder.persistent(GTArmor.CODEC).networkSynchronized(GTArmor.STREAM_CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Unit>> FLY_MODE = DATA_COMPONENTS
            .registerComponentType("fly_mode", builder -> builder.persistent(Unit.CODEC)
                    .networkSynchronized(UNIT_STREAM_CODEC));

    // component item-related
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ResearchManager.ResearchItem>> RESEARCH_ITEM = DATA_COMPONENTS
            .registerComponentType("research_item", builder -> builder.persistent(ResearchManager.ResearchItem.CODEC)
                    .networkSynchronized(ResearchManager.ResearchItem.STREAM_CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> DATA_ITEM = DATA_COMPONENTS
            .registerComponentType("data_item", builder -> builder.persistent(Codec.BOOL)
                    .networkSynchronized(ByteBufCodecs.BOOL));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ItemMagnetBehavior.MagnetComponent>> MAGNET = DATA_COMPONENTS
            .registerComponentType("magnet",
                    builder -> builder.persistent(ItemMagnetBehavior.MagnetComponent.CODEC)
                            .networkSynchronized(ItemMagnetBehavior.MagnetComponent.STREAM_CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Byte>> SCANNER_MODE = DATA_COMPONENTS
            .registerComponentType("scanner_mode",
                    builder -> builder.persistent(Codec.BYTE).networkSynchronized(ByteBufCodecs.BYTE));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<SimpleFluidFilter>> SIMPLE_FLUID_FILTER = DATA_COMPONENTS
            .registerComponentType("simple_fluid_filter", builder -> builder.persistent(SimpleFluidFilter.CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<SimpleItemFilter>> SIMPLE_ITEM_FILTER = DATA_COMPONENTS
            .registerComponentType("simple_item_filter", builder -> builder.persistent(SimpleItemFilter.CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> TAG_FILTER_EXPRESSION = DATA_COMPONENTS
            .registerComponentType("tag_filter_expression",
                    builder -> builder.persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<SmartItemFilter.SmartFilteringMode>> SMART_ITEM_FILTER = DATA_COMPONENTS
            .registerComponentType("smart_item_filter",
                    builder -> builder.persistent(SmartItemFilter.SmartFilteringMode.CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> CIRCUIT_CONFIG = DATA_COMPONENTS
            .registerComponentType("circuit_config", builder -> builder.persistent(Codec.INT));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<SimpleFluidContent>> FLUID_CONTENT = DATA_COMPONENTS
            .registerComponentType("fluid_content", builder -> builder.persistent(SimpleFluidContent.CODEC)
                    .networkSynchronized(SimpleFluidContent.STREAM_CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<SimpleEnergyContent>> ENERGY_CONTENT = DATA_COMPONENTS
            .registerComponentType("energy_content", builder -> builder.persistent(SimpleEnergyContent.CODEC)
                    .networkSynchronized(SimpleEnergyContent.STREAM_CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<BlockPos>> DATA_COPY_POS = DATA_COMPONENTS
            .registerComponentType("data_copy_pos", builder -> builder.persistent(BlockPos.CODEC)
                    .networkSynchronized(BlockPos.STREAM_CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<CustomData>> DATA_COPY_TAG = DATA_COMPONENTS
            .registerComponentType("data_copy_tag", builder -> builder.persistent(CustomData.CODEC)
                    .networkSynchronized(CustomData.STREAM_CODEC));

    // machine info
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<LargeItemContent>> LARGE_ITEM_CONTENT = DATA_COMPONENTS
            .registerComponentType("large_item_content", builder -> builder
                    .persistent(LargeItemContent.CODEC)
                    .networkSynchronized(LargeItemContent.STREAM_CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<LargeFluidContent>> LARGE_FLUID_CONTENT = DATA_COMPONENTS
            .registerComponentType("large_fluid_content", builder -> builder
                    .persistent(LargeFluidContent.CODEC)
                    .networkSynchronized(LargeFluidContent.STREAM_CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<CreativeMachineInfo>> CREATIVE_MACHINE_INFO = DATA_COMPONENTS
            .registerComponentType("creative_machine_info", builder -> builder
                    .persistent(CreativeMachineInfo.CODEC)
                    .networkSynchronized(CreativeMachineInfo.STREAM_CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Unit>> TAPED = DATA_COMPONENTS
            .registerComponentType("taped",
                    builder -> builder.persistent(Unit.CODEC).networkSynchronized(UNIT_STREAM_CODEC));

    // misc
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<FacadeWrapper>> FACADE = DATA_COMPONENTS
            .registerComponentType("facade",
                    builder -> builder.persistent(FacadeWrapper.CODEC).networkSynchronized(FacadeWrapper.STREAM_CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<LampBlockItem.LampData>> LAMP_DATA = DATA_COMPONENTS
            .registerComponentType("lamp",
                    builder -> builder.persistent(LampBlockItem.LampData.CODEC)
                            .networkSynchronized(LampBlockItem.LampData.STREAM_CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> LIGHTER_OPEN = DATA_COMPONENTS
            .registerComponentType("lighter_open",
                    builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL));
}

package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.api.item.tool.behavior.IToolBehavior;
import com.gregtechceu.gtceu.api.item.tool.behavior.ToolBehaviorType;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.item.tool.behavior.*;
import com.gregtechceu.gtceu.common.registry.GTRegistration;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import com.mojang.serialization.Codec;
import com.tterrag.registrate.util.entry.RegistryEntry;

public class GTToolBehaviors {

    private GTToolBehaviors() {}

    // spotless:off
    public static final RegistryEntry<ToolBehaviorType<?>, ToolBehaviorType<AOEConfigUIBehavior>> AOE_CONFIG_UI = register("aoe_config_ui", AOEConfigUIBehavior.CODEC, AOEConfigUIBehavior.STREAM_CODEC);
    public static final RegistryEntry<ToolBehaviorType<?>, ToolBehaviorType<BlockRotatingBehavior>> BLOCK_ROTATING = register("rotate_block", BlockRotatingBehavior.CODEC, BlockRotatingBehavior.STREAM_CODEC);
    public static final RegistryEntry<ToolBehaviorType<?>, ToolBehaviorType<DisableShieldBehavior>> DISABLE_SHIELD = register("disable_shield", DisableShieldBehavior.CODEC, DisableShieldBehavior.STREAM_CODEC);
    public static final RegistryEntry<ToolBehaviorType<?>, ToolBehaviorType<EntityDamageBehavior>> ENTITY_DAMAGE = register("entity_damage", EntityDamageBehavior.CODEC, EntityDamageBehavior.STREAM_CODEC);
    public static final RegistryEntry<ToolBehaviorType<?>, ToolBehaviorType<GrassPathBehavior>> PATH = register("path", GrassPathBehavior.CODEC, GrassPathBehavior.STREAM_CODEC);
    public static final RegistryEntry<ToolBehaviorType<?>, ToolBehaviorType<HarvestCropsBehavior>> HARVEST_CROPS = register("harvest_crops", HarvestCropsBehavior.CODEC, HarvestCropsBehavior.STREAM_CODEC);
    public static final RegistryEntry<ToolBehaviorType<?>, ToolBehaviorType<HarvestIceBehavior>> HARVEST_ICE = register("harvest_ice", HarvestIceBehavior.CODEC, HarvestIceBehavior.STREAM_CODEC);
    public static final RegistryEntry<ToolBehaviorType<?>, ToolBehaviorType<HoeGroundBehavior>> HOE_GROUND = register("hoe_ground", HoeGroundBehavior.CODEC, HoeGroundBehavior.STREAM_CODEC);
    public static final RegistryEntry<ToolBehaviorType<?>, ToolBehaviorType<LogStripBehavior>> STRIP_LOG = register("strip_log", LogStripBehavior.CODEC, LogStripBehavior.STREAM_CODEC);
    public static final RegistryEntry<ToolBehaviorType<?>, ToolBehaviorType<PlungerBehavior>> PLUNGER = register("plunger", PlungerBehavior.CODEC, PlungerBehavior.STREAM_CODEC);
    public static final RegistryEntry<ToolBehaviorType<?>, ToolBehaviorType<RotateRailBehavior>> ROTATE_RAIL = register("rotate_rail", RotateRailBehavior.CODEC, RotateRailBehavior.STREAM_CODEC);
    public static final RegistryEntry<ToolBehaviorType<?>, ToolBehaviorType<ScrapeBehavior>> SCRAPE = register("scrape", ScrapeBehavior.CODEC, ScrapeBehavior.STREAM_CODEC);
    public static final RegistryEntry<ToolBehaviorType<?>, ToolBehaviorType<TorchPlaceBehavior>> TORCH_PLACE = register("torch_place", TorchPlaceBehavior.CODEC, TorchPlaceBehavior.STREAM_CODEC);
    public static final RegistryEntry<ToolBehaviorType<?>, ToolBehaviorType<TreeFellingBehavior>> TREE_FELLING = register("tree_felling", TreeFellingBehavior.CODEC, TreeFellingBehavior.STREAM_CODEC);
    public static final RegistryEntry<ToolBehaviorType<?>, ToolBehaviorType<WaxOffBehavior>> WAX_OFF = register("wax_off", WaxOffBehavior.CODEC, WaxOffBehavior.STREAM_CODEC);
    public static final RegistryEntry<ToolBehaviorType<?>, ToolBehaviorType<ToolModeSwitchBehavior>> MODE_SWITCH = register("mode_switch", ToolModeSwitchBehavior.CODEC, ToolModeSwitchBehavior.STREAM_CODEC);
    public static final RegistryEntry<ToolBehaviorType<?>, ToolBehaviorType<ProspectingBehavior>> PROSPECTING = register("prospecting", ProspectingBehavior.CODEC, ProspectingBehavior.STREAM_CODEC);
    // spotless:on
    public static void init() {}

    private static <
            T extends IToolBehavior<T>> RegistryEntry<ToolBehaviorType<?>, ToolBehaviorType<T>> register(String name,
                                                                                                         Codec<T> codec,
                                                                                                         StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {
        return GTRegistration.REGISTRATE.simple(name, GTRegistries.Keys.TOOL_BEHAVIOR,
                () -> new ToolBehaviorType<>(codec, streamCodec));
    }
}

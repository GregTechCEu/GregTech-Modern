package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.item.tool.behavior.ToolBehaviorType;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.item.tool.behavior.*;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class GTToolBehaviors {

    private GTToolBehaviors() {}

    private static final DeferredRegister<ToolBehaviorType<?>> TOOL_BEHAVIOR_TYPE = DeferredRegister
            .create(GTRegistries.Keys.TOOL_BEHAVIOR, GTCEu.MOD_ID);

    // spotless:off
    public static final DeferredHolder<ToolBehaviorType<?>, ToolBehaviorType<AOEConfigUIBehavior>> AOE_CONFIG_UI = TOOL_BEHAVIOR_TYPE.register(
            "aoe_config_ui", () -> new ToolBehaviorType<>(AOEConfigUIBehavior.CODEC, AOEConfigUIBehavior.STREAM_CODEC));
    public static final DeferredHolder<ToolBehaviorType<?>, ToolBehaviorType<BlockRotatingBehavior>> BLOCK_ROTATING = TOOL_BEHAVIOR_TYPE.register(
            "rotate_block", () -> new ToolBehaviorType<>(BlockRotatingBehavior.CODEC, BlockRotatingBehavior.STREAM_CODEC));
    public static final DeferredHolder<ToolBehaviorType<?>, ToolBehaviorType<DisableShieldBehavior>> DISABLE_SHIELD = TOOL_BEHAVIOR_TYPE.register(
            "disable_shield", () -> new ToolBehaviorType<>(DisableShieldBehavior.CODEC, DisableShieldBehavior.STREAM_CODEC));
    public static final DeferredHolder<ToolBehaviorType<?>, ToolBehaviorType<EntityDamageBehavior>> ENTITY_DAMAGE = TOOL_BEHAVIOR_TYPE.register(
            "entity_damage", () -> new ToolBehaviorType<>(EntityDamageBehavior.CODEC, EntityDamageBehavior.STREAM_CODEC));
    public static final DeferredHolder<ToolBehaviorType<?>, ToolBehaviorType<GrassPathBehavior>> PATH = TOOL_BEHAVIOR_TYPE.register(
            "path", () -> new ToolBehaviorType<>(GrassPathBehavior.CODEC, GrassPathBehavior.STREAM_CODEC));
    public static final DeferredHolder<ToolBehaviorType<?>, ToolBehaviorType<HarvestCropsBehavior>> HARVEST_CROPS = TOOL_BEHAVIOR_TYPE.register(
            "harvest_crops", () -> new ToolBehaviorType<>(HarvestCropsBehavior.CODEC, HarvestCropsBehavior.STREAM_CODEC));
    public static final DeferredHolder<ToolBehaviorType<?>, ToolBehaviorType<HarvestIceBehavior>> HARVEST_ICE = TOOL_BEHAVIOR_TYPE.register(
            "harvest_ice", () -> new ToolBehaviorType<>(HarvestIceBehavior.CODEC, HarvestIceBehavior.STREAM_CODEC));
    public static final DeferredHolder<ToolBehaviorType<?>, ToolBehaviorType<HoeGroundBehavior>> HOE_GROUND = TOOL_BEHAVIOR_TYPE.register(
            "hoe_ground", () -> new ToolBehaviorType<>(HoeGroundBehavior.CODEC, HoeGroundBehavior.STREAM_CODEC));
    public static final DeferredHolder<ToolBehaviorType<?>, ToolBehaviorType<LogStripBehavior>> STRIP_LOG = TOOL_BEHAVIOR_TYPE.register(
            "strip_log", () -> new ToolBehaviorType<>(LogStripBehavior.CODEC, LogStripBehavior.STREAM_CODEC));
    public static final DeferredHolder<ToolBehaviorType<?>, ToolBehaviorType<PlungerBehavior>> PLUNGER = TOOL_BEHAVIOR_TYPE.register(
            "plunger", () -> new ToolBehaviorType<>(PlungerBehavior.CODEC, PlungerBehavior.STREAM_CODEC));
    public static final DeferredHolder<ToolBehaviorType<?>, ToolBehaviorType<RotateRailBehavior>> ROTATE_RAIL = TOOL_BEHAVIOR_TYPE.register(
            "rotate_rail", () -> new ToolBehaviorType<>(RotateRailBehavior.CODEC, RotateRailBehavior.STREAM_CODEC));
    public static final DeferredHolder<ToolBehaviorType<?>, ToolBehaviorType<ScrapeBehavior>> SCRAPE = TOOL_BEHAVIOR_TYPE.register(
            "scrape", () -> new ToolBehaviorType<>(ScrapeBehavior.CODEC, ScrapeBehavior.STREAM_CODEC));
    public static final DeferredHolder<ToolBehaviorType<?>, ToolBehaviorType<TorchPlaceBehavior>> TORCH_PLACE = TOOL_BEHAVIOR_TYPE.register(
            "torch_place", () -> new ToolBehaviorType<>(TorchPlaceBehavior.CODEC, TorchPlaceBehavior.STREAM_CODEC));
    public static final DeferredHolder<ToolBehaviorType<?>, ToolBehaviorType<TreeFellingBehavior>> TREE_FELLING = TOOL_BEHAVIOR_TYPE.register(
            "tree_felling", () -> new ToolBehaviorType<>(TreeFellingBehavior.CODEC, TreeFellingBehavior.STREAM_CODEC));
    public static final DeferredHolder<ToolBehaviorType<?>, ToolBehaviorType<WaxOffBehavior>> WAX_OFF = TOOL_BEHAVIOR_TYPE.register(
            "wax_off", () -> new ToolBehaviorType<>(WaxOffBehavior.CODEC, WaxOffBehavior.STREAM_CODEC));
    public static final DeferredHolder<ToolBehaviorType<?>, ToolBehaviorType<ToolModeSwitchBehavior>> MODE_SWITCH = TOOL_BEHAVIOR_TYPE.register(
            "mode_switch", () -> new ToolBehaviorType<>(ToolModeSwitchBehavior.CODEC, ToolModeSwitchBehavior.STREAM_CODEC));
    public static final DeferredHolder<ToolBehaviorType<?>, ToolBehaviorType<ProspectingBehavior>> PROSPECTING = TOOL_BEHAVIOR_TYPE.register(
            "prospecting", () -> new ToolBehaviorType<>(ProspectingBehavior.CODEC, ProspectingBehavior.STREAM_CODEC));
    // spotless:on
    public static void init(IEventBus modBus) {
        TOOL_BEHAVIOR_TYPE.register(modBus);
    }
}

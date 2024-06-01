package com.gregtechceu.gtceu.data.tools;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.item.tool.behavior.ToolBehaviorType;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.item.tool.behavior.*;

import net.neoforged.fml.ModLoader;

public class GTToolBehaviors {

    static {
        GTRegistries.TOOL_BEHAVIORS.unfreeze();
    }

    private GTToolBehaviors() {}

    public static final ToolBehaviorType<BlockRotatingBehavior> BLOCK_ROTATING = GTRegistries.TOOL_BEHAVIORS.register(
            GTCEu.id("rotate_block"),
            new ToolBehaviorType<>(BlockRotatingBehavior.CODEC, BlockRotatingBehavior.STREAM_CODEC));
    public static final ToolBehaviorType<DisableShieldBehavior> DISABLE_SHIELD = GTRegistries.TOOL_BEHAVIORS.register(
            GTCEu.id("disable_shield"),
            new ToolBehaviorType<>(DisableShieldBehavior.CODEC, DisableShieldBehavior.STREAM_CODEC));
    public static final ToolBehaviorType<EntityDamageBehavior> ENTITY_DAMAGE = GTRegistries.TOOL_BEHAVIORS.register(
            GTCEu.id("entity_damage"),
            new ToolBehaviorType<>(EntityDamageBehavior.CODEC, EntityDamageBehavior.STREAM_CODEC));
    public static final ToolBehaviorType<GrassPathBehavior> PATH = GTRegistries.TOOL_BEHAVIORS.register(
            GTCEu.id("path"), new ToolBehaviorType<>(GrassPathBehavior.CODEC, GrassPathBehavior.STREAM_CODEC));
    public static final ToolBehaviorType<HarvestCropsBehavior> HARVEST_CROPS = GTRegistries.TOOL_BEHAVIORS.register(
            GTCEu.id("harvest_crops"),
            new ToolBehaviorType<>(HarvestCropsBehavior.CODEC, HarvestCropsBehavior.STREAM_CODEC));
    public static final ToolBehaviorType<HarvestIceBehavior> HARVEST_ICE = GTRegistries.TOOL_BEHAVIORS.register(
            GTCEu.id("harvest_ice"), new ToolBehaviorType<>(HarvestIceBehavior.CODEC, HarvestIceBehavior.STREAM_CODEC));
    public static final ToolBehaviorType<HoeGroundBehavior> HOE_GROUND = GTRegistries.TOOL_BEHAVIORS.register(
            GTCEu.id("hoe_ground"), new ToolBehaviorType<>(HoeGroundBehavior.CODEC, HoeGroundBehavior.STREAM_CODEC));
    public static final ToolBehaviorType<LogStripBehavior> STRIP_LOG = GTRegistries.TOOL_BEHAVIORS.register(
            GTCEu.id("strip_log"), new ToolBehaviorType<>(LogStripBehavior.CODEC, LogStripBehavior.STREAM_CODEC));
    public static final ToolBehaviorType<PlungerBehavior> PLUNGER = GTRegistries.TOOL_BEHAVIORS
            .register(GTCEu.id("plunger"), new ToolBehaviorType<>(PlungerBehavior.CODEC, PlungerBehavior.STREAM_CODEC));
    public static final ToolBehaviorType<RotateRailBehavior> ROTATE_RAIL = GTRegistries.TOOL_BEHAVIORS.register(
            GTCEu.id("rotate_rail"), new ToolBehaviorType<>(RotateRailBehavior.CODEC, RotateRailBehavior.STREAM_CODEC));
    public static final ToolBehaviorType<ScrapeBehavior> SCRAPE = GTRegistries.TOOL_BEHAVIORS
            .register(GTCEu.id("scrape"), new ToolBehaviorType<>(ScrapeBehavior.CODEC, ScrapeBehavior.STREAM_CODEC));
    public static final ToolBehaviorType<TorchPlaceBehavior> TORCH_PLACE = GTRegistries.TOOL_BEHAVIORS.register(
            GTCEu.id("torch_place"), new ToolBehaviorType<>(TorchPlaceBehavior.CODEC, TorchPlaceBehavior.STREAM_CODEC));
    public static final ToolBehaviorType<TreeFellingBehavior> TREE_FELLING = GTRegistries.TOOL_BEHAVIORS.register(
            GTCEu.id("tree_felling"),
            new ToolBehaviorType<>(TreeFellingBehavior.CODEC, TreeFellingBehavior.STREAM_CODEC));
    public static final ToolBehaviorType<WaxOffBehavior> WAX_OFF = GTRegistries.TOOL_BEHAVIORS
            .register(GTCEu.id("wax_off"), new ToolBehaviorType<>(WaxOffBehavior.CODEC, WaxOffBehavior.STREAM_CODEC));
    public static final ToolBehaviorType<ToolModeSwitchBehavior> MODE_SWITCH = GTRegistries.TOOL_BEHAVIORS
            .register(GTCEu.id("mode_switch"), new ToolBehaviorType<>(ToolModeSwitchBehavior.CODEC, ToolModeSwitchBehavior.STREAM_CODEC));

    public static void init() {
        ModLoader.postEvent(new GTCEuAPI.RegisterEvent<>(GTRegistries.TOOL_BEHAVIORS));
        GTRegistries.TOOL_BEHAVIORS.freeze();
    }
}

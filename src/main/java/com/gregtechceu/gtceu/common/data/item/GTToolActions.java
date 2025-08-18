package com.gregtechceu.gtceu.common.data.item;

import com.gregtechceu.gtceu.api.item.tool.GTToolType;

import net.minecraft.Util;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.common.extensions.IForgeBlock;

import com.google.common.collect.Sets;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GTToolActions {

    /**
     * Passed onto {@link IForgeBlock#getToolModifiedState} when a shovel wants to douse a campfire
     */
    public static final ToolAction SHOVEL_DOUSE = ToolAction.get("shovel_douse");
    /**
     * Passed onto {@link IForgeBlock#getToolModifiedState} when a hoe wants to harvest crops from soil
     */
    public static final ToolAction HOE_HARVEST = ToolAction.get("hoe_harvest");
    /**
     * Passed onto {@link IForgeBlock#getToolModifiedState} when a hoe wants to harvest crops from soil
     */
    public static final ToolAction AXE_FELL_TREE = ToolAction.get("axe_fell_tree");

    /**
     * Exposed by wire cutters to allow querying tool behaviours.
     */
    public static final ToolAction WIRE_CUTTER_DIG = ToolAction.get("wire_cutter_dig");
    /**
     * Exposed by knives to allow querying tool behaviours.
     */
    public static final ToolAction KNIFE_DIG = ToolAction.get("knife_dig");
    /**
     * Exposed by saws to allow querying tool behaviours.
     */
    public static final ToolAction SAW_DIG = ToolAction.get("saw_dig");
    /**
     * Exposed by hard hammers to allow querying tool behaviours.
     */
    public static final ToolAction HAMMER_DIG = ToolAction.get("hammer_dig");
    /**
     * Exposed by hard hammers that can mute machines.
     */
    public static final ToolAction HAMMER_MUTE = ToolAction.get("hammer_mute");
    /**
     * Exposed by soft mallets that can pause machines.
     */
    public static final ToolAction MALLET_PAUSE = ToolAction.get("mallet_pause");
    /**
     * Exposed by soft mallets that can configure machines.
     */
    public static final ToolAction MALLET_CONFIGURE = ToolAction.get("mallet_configure");

    /**
     * Exposed by wrenches to allow querying tool behaviours.
     * Basically a duplicate of {@link #WRENCH_DISMANTLE}, but with a more generic name.
     */
    public static final ToolAction WRENCH_DIG = ToolAction.get("wrench_dig");
    /**
     * Exposed by wrenches that can dismantle blocks.
     */
    public static final ToolAction WRENCH_DISMANTLE = ToolAction.get("wrench_dismantle");
    /**
     * Exposed by wrenches that can rotate blocks.
     */
    public static final ToolAction WRENCH_ROTATE = ToolAction.get("wrench_rotate");
    /**
     * Exposed by wrenches can connect blocks (like pipes).
     */
    public static final ToolAction WRENCH_CONNECT = ToolAction.get("wrench_connect");
    /**
     * Exposed by wrenches that can configure any output directions for a block.
     */
    public static final ToolAction WRENCH_CONFIGURE = ToolAction.get("wrench_configure");
    /**
     * Exposed by wrenches that can configure any output directions for a block.
     */
    public static final ToolAction WRENCH_CONFIGURE_ALL = ToolAction.get("wrench_configure_all");
    /**
     * Exposed by wrenches that can currently configure fluid output directions for a block.
     */
    public static final ToolAction WRENCH_CONFIGURE_FLUIDS = ToolAction.get("wrench_configure_fluids");
    /**
     * Exposed by wrenches that can configure item output directions for a block.
     */
    public static final ToolAction WRENCH_CONFIGURE_ITEMS = ToolAction.get("wrench_configure_items");
    /**
     * Exposed by wire cutters that can connect blocks (like cables).
     */
    public static final ToolAction WIRE_CUTTER_CONNECT = ToolAction.get("wire_cutter_connect");
    /**
     * Exposed by crowbars to allow querying tool behaviours.
     */
    public static final ToolAction CROWBAR_DIG = ToolAction.get("crowbar_dig");
    /**
     * Exposed by crowbars to allow querying tool behaviours.
     */
    public static final ToolAction CROWBAR_ROTATE = ToolAction.get("crowbar_rotate");
    /**
     * Exposed by tools that can remove covers.
     */
    public static final ToolAction CROWBAR_REMOVE_COVER = ToolAction.get("crowbar_remove_cover");
    /**
     * Exposed by crowbars to allow querying tool behaviours.
     */
    public static final ToolAction SCREWDRIVER_CONFIGURE = ToolAction.get("screwdriver_configure");

    /**
     * Exposed by tools that can interact with covers.
     */
    public static final ToolAction INTERACT_WITH_COVER = ToolAction.get("interact_with_cover");

    // spotless:off
    public static final Set<ToolAction> WRENCH_CONFIGURE_ACTIONS = of(WRENCH_CONFIGURE_ALL, WRENCH_CONFIGURE_ITEMS, WRENCH_CONFIGURE_FLUIDS);
    public static final Set<ToolAction> DEFAULT_WRENCH_ACTIONS = of(WRENCH_CONFIGURE_ACTIONS, WRENCH_ROTATE, WRENCH_DIG, WRENCH_DISMANTLE, WRENCH_CONNECT);
    public static final Set<ToolAction> DEFAULT_WIRE_CUTTER_ACTIONS = of(WIRE_CUTTER_DIG, WIRE_CUTTER_CONNECT);
    public static final Set<ToolAction> DEFAULT_KNIFE_ACTIONS = of(KNIFE_DIG);
    public static final Set<ToolAction> DEFAULT_SAW_ACTIONS = of(SAW_DIG);
    public static final Set<ToolAction> DEFAULT_MALLET_ACTIONS = of(MALLET_PAUSE, MALLET_CONFIGURE);
    public static final Set<ToolAction> DEFAULT_HAMMER_ACTIONS = of(HAMMER_DIG, HAMMER_MUTE);
    public static final Set<ToolAction> DEFAULT_CROWBAR_ACTIONS = of(CROWBAR_DIG, CROWBAR_ROTATE, CROWBAR_REMOVE_COVER);
    public static final Set<ToolAction> DEFAULT_SCREWDRIVER_ACTIONS = of(SCREWDRIVER_CONFIGURE, INTERACT_WITH_COVER);
    public static final Set<ToolAction> DEFAULT_DRILL_ACTIONS = of(ToolActions.DEFAULT_PICKAXE_ACTIONS, ToolActions.SHOVEL_DIG, ToolActions.HOE_DIG);

    public static final Set<ToolAction> DEFAULT_SHOVEL_ACTIONS = of(ToolActions.DEFAULT_SHOVEL_ACTIONS, SHOVEL_DOUSE);

    public static final Map<ToolAction, GTToolType> DEFAULT_TYPE_ASSOCIATIONS = Util.make(new HashMap<>(), map -> {
        ToolActions.DEFAULT_AXE_ACTIONS.forEach(ability -> map.put(ability, GTToolType.AXE));
        ToolActions.DEFAULT_PICKAXE_ACTIONS.forEach(ability -> map.put(ability, GTToolType.PICKAXE));
        ToolActions.DEFAULT_SHOVEL_ACTIONS.forEach(ability -> map.put(ability, GTToolType.SHOVEL));
        ToolActions.DEFAULT_SWORD_ACTIONS.forEach(ability -> map.put(ability, GTToolType.SWORD));
        ToolActions.DEFAULT_SHEARS_ACTIONS.forEach(ability -> map.put(ability, GTToolType.SHEARS));
        GTToolActions.DEFAULT_WRENCH_ACTIONS.forEach(ability -> map.put(ability, GTToolType.WRENCH));
        GTToolActions.DEFAULT_WIRE_CUTTER_ACTIONS.forEach(ability -> map.put(ability, GTToolType.WIRE_CUTTER));
        GTToolActions.DEFAULT_KNIFE_ACTIONS.forEach(ability -> map.put(ability, GTToolType.KNIFE));
        GTToolActions.DEFAULT_SAW_ACTIONS.forEach(ability -> map.put(ability, GTToolType.SAW));
        GTToolActions.DEFAULT_MALLET_ACTIONS.forEach(ability -> map.put(ability, GTToolType.SOFT_MALLET));
        GTToolActions.DEFAULT_HAMMER_ACTIONS.forEach(ability -> map.put(ability, GTToolType.HARD_HAMMER));
        GTToolActions.DEFAULT_CROWBAR_ACTIONS.forEach(ability -> map.put(ability, GTToolType.CROWBAR));
        GTToolActions.DEFAULT_SCREWDRIVER_ACTIONS.forEach(ability -> map.put(ability, GTToolType.SCREWDRIVER));
    });
    // spotless:on

    private static Set<ToolAction> of(ToolAction... actions) {
        return Stream.of(actions).collect(Collectors.toCollection(Sets::newIdentityHashSet));
    }

    private static Set<ToolAction> of(Set<ToolAction> base, ToolAction... actions) {
        return Stream.concat(base.stream(), Stream.of(actions))
                .collect(Collectors.toCollection(Sets::newIdentityHashSet));
    }
}

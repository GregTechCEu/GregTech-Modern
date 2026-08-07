package com.gregtechceu.gtceu.common.data.item;

import com.gregtechceu.gtceu.api.item.tool.GTToolType;

import net.minecraft.Util;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;
import net.neoforged.neoforge.common.extensions.IBlockExtension;

import com.google.common.collect.Sets;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GTItemAbilities {

    /**
     * Passed onto {@link IBlockExtension#getToolModifiedState} when a hoe wants to harvest crops from soil
     */
    public static final ItemAbility HOE_HARVEST = ItemAbility.get("hoe_harvest");
    /**
     * Passed onto {@link IBlockExtension#getToolModifiedState} when a hoe wants to harvest crops from soil
     */
    public static final ItemAbility AXE_FELL_TREE = ItemAbility.get("axe_fell_tree");

    /**
     * Exposed by wire cutters to allow querying tool behaviours.
     */
    public static final ItemAbility WIRE_CUTTER_DIG = ItemAbility.get("wire_cutter_dig");
    /**
     * Exposed by knives to allow querying tool behaviours.
     */
    public static final ItemAbility KNIFE_DIG = ItemAbility.get("knife_dig");
    /**
     * Exposed by saws to allow querying tool behaviours.
     */
    public static final ItemAbility SAW_DIG = ItemAbility.get("saw_dig");
    /**
     * Exposed by hard hammers to allow querying tool behaviours.
     */
    public static final ItemAbility HAMMER_DIG = ItemAbility.get("hammer_dig");
    /**
     * Exposed by hard hammers that can mute machines.
     */
    public static final ItemAbility HAMMER_MUTE = ItemAbility.get("hammer_mute");
    /**
     * Exposed by soft mallets that can pause machines.
     */
    public static final ItemAbility MALLET_PAUSE = ItemAbility.get("mallet_pause");
    /**
     * Exposed by soft mallets that can configure machines.
     */
    public static final ItemAbility MALLET_CONFIGURE = ItemAbility.get("mallet_configure");

    /**
     * Exposed by wrenches to allow querying tool behaviours.
     * Basically a duplicate of {@link #WRENCH_DISMANTLE}, but with a more generic name.
     */
    public static final ItemAbility WRENCH_DIG = ItemAbility.get("wrench_dig");
    /**
     * Exposed by wrenches that can dismantle blocks.
     */
    public static final ItemAbility WRENCH_DISMANTLE = ItemAbility.get("wrench_dismantle");
    /**
     * Exposed by wrenches that can rotate blocks.
     */
    public static final ItemAbility WRENCH_ROTATE = ItemAbility.get("wrench_rotate");
    /**
     * Exposed by wrenches can connect blocks (like pipes).
     */
    public static final ItemAbility WRENCH_CONNECT = ItemAbility.get("wrench_connect");
    /**
     * Exposed by wrenches that can configure any output directions for a block.
     */
    public static final ItemAbility WRENCH_CONFIGURE = ItemAbility.get("wrench_configure");
    /**
     * Exposed by wrenches that can configure any output directions for a block.
     */
    public static final ItemAbility WRENCH_CONFIGURE_ALL = ItemAbility.get("wrench_configure_all");
    /**
     * Exposed by wrenches that can currently configure fluid output directions for a block.
     */
    public static final ItemAbility WRENCH_CONFIGURE_FLUIDS = ItemAbility.get("wrench_configure_fluids");
    /**
     * Exposed by wrenches that can configure item output directions for a block.
     */
    public static final ItemAbility WRENCH_CONFIGURE_ITEMS = ItemAbility.get("wrench_configure_items");
    /**
     * Exposed by wire cutters that can connect blocks (like cables).
     */
    public static final ItemAbility WIRE_CUTTER_CONNECT = ItemAbility.get("wire_cutter_connect");
    /**
     * Exposed by crowbars to allow querying tool behaviours.
     */
    public static final ItemAbility CROWBAR_DIG = ItemAbility.get("crowbar_dig");
    /**
     * Exposed by crowbars to allow querying tool behaviours.
     */
    public static final ItemAbility CROWBAR_ROTATE = ItemAbility.get("crowbar_rotate");
    /**
     * Exposed by tools that can remove covers.
     */
    public static final ItemAbility CROWBAR_REMOVE_COVER = ItemAbility.get("crowbar_remove_cover");
    /**
     * Exposed by crowbars to allow querying tool behaviours.
     */
    public static final ItemAbility SCREWDRIVER_CONFIGURE = ItemAbility.get("screwdriver_configure");

    /**
     * Exposed by tools that can interact with covers.
     */
    public static final ItemAbility INTERACT_WITH_COVER = ItemAbility.get("interact_with_cover");

    // spotless:off
    public static final Set<ItemAbility> WRENCH_CONFIGURE_ACTIONS = of(WRENCH_CONFIGURE_ALL, WRENCH_CONFIGURE_ITEMS, WRENCH_CONFIGURE_FLUIDS);
    public static final Set<ItemAbility> DEFAULT_WRENCH_ACTIONS = of(WRENCH_CONFIGURE_ACTIONS, WRENCH_ROTATE, WRENCH_DIG, WRENCH_DISMANTLE, WRENCH_CONNECT);
    public static final Set<ItemAbility> DEFAULT_WIRE_CUTTER_ACTIONS = of(WIRE_CUTTER_DIG, WIRE_CUTTER_CONNECT);
    public static final Set<ItemAbility> DEFAULT_KNIFE_ACTIONS = of(KNIFE_DIG);
    public static final Set<ItemAbility> DEFAULT_SAW_ACTIONS = of(SAW_DIG);
    public static final Set<ItemAbility> DEFAULT_MALLET_ACTIONS = of(MALLET_PAUSE, MALLET_CONFIGURE);
    public static final Set<ItemAbility> DEFAULT_HAMMER_ACTIONS = of(HAMMER_DIG, HAMMER_MUTE);
    public static final Set<ItemAbility> DEFAULT_CROWBAR_ACTIONS = of(CROWBAR_DIG, CROWBAR_ROTATE, CROWBAR_REMOVE_COVER);
    public static final Set<ItemAbility> DEFAULT_SCREWDRIVER_ACTIONS = of(SCREWDRIVER_CONFIGURE, INTERACT_WITH_COVER);
    public static final Set<ItemAbility> DEFAULT_DRILL_ACTIONS = of(ItemAbilities.DEFAULT_PICKAXE_ACTIONS, ItemAbilities.SHOVEL_DIG, ItemAbilities.HOE_DIG);

    public static final Map<ItemAbility, GTToolType> DEFAULT_TYPE_ASSOCIATIONS = Util.make(new HashMap<>(), map -> {
        ItemAbilities.DEFAULT_AXE_ACTIONS.forEach(ability -> map.put(ability, GTToolType.AXE));
        ItemAbilities.DEFAULT_PICKAXE_ACTIONS.forEach(ability -> map.put(ability, GTToolType.PICKAXE));
        ItemAbilities.DEFAULT_SHOVEL_ACTIONS.forEach(ability -> map.put(ability, GTToolType.SHOVEL));
        ItemAbilities.DEFAULT_SWORD_ACTIONS.forEach(ability -> map.put(ability, GTToolType.SWORD));
        ItemAbilities.DEFAULT_SHEARS_ACTIONS.forEach(ability -> map.put(ability, GTToolType.SHEARS));
        GTItemAbilities.DEFAULT_WRENCH_ACTIONS.forEach(ability -> map.put(ability, GTToolType.WRENCH));
        GTItemAbilities.DEFAULT_WIRE_CUTTER_ACTIONS.forEach(ability -> map.put(ability, GTToolType.WIRE_CUTTER));
        GTItemAbilities.DEFAULT_KNIFE_ACTIONS.forEach(ability -> map.put(ability, GTToolType.KNIFE));
        GTItemAbilities.DEFAULT_SAW_ACTIONS.forEach(ability -> map.put(ability, GTToolType.SAW));
        GTItemAbilities.DEFAULT_MALLET_ACTIONS.forEach(ability -> map.put(ability, GTToolType.SOFT_MALLET));
        GTItemAbilities.DEFAULT_HAMMER_ACTIONS.forEach(ability -> map.put(ability, GTToolType.HARD_HAMMER));
        GTItemAbilities.DEFAULT_CROWBAR_ACTIONS.forEach(ability -> map.put(ability, GTToolType.CROWBAR));
        GTItemAbilities.DEFAULT_SCREWDRIVER_ACTIONS.forEach(ability -> map.put(ability, GTToolType.SCREWDRIVER));
    });
    // spotless:on

    private static Set<ItemAbility> of(ItemAbility... actions) {
        return Stream.of(actions).collect(Collectors.toCollection(Sets::newIdentityHashSet));
    }

    private static Set<ItemAbility> of(Set<ItemAbility> base, ItemAbility... actions) {
        return Stream.concat(base.stream(), Stream.of(actions))
                .collect(Collectors.toCollection(Sets::newIdentityHashSet));
    }
}

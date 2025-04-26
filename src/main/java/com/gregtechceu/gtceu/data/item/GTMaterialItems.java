package com.gregtechceu.gtceu.data.item;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.item.IGTTool;
import com.gregtechceu.gtceu.api.item.TagPrefixItem;
import com.gregtechceu.gtceu.api.item.datacomponents.ToolBehaviors;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.item.tool.IGTToolDefinition;
import com.gregtechceu.gtceu.api.item.tool.MaterialToolTier;
import com.gregtechceu.gtceu.api.material.material.Material;
import com.gregtechceu.gtceu.api.material.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.material.material.properties.ToolProperty;
import com.gregtechceu.gtceu.api.material.material.stack.MaterialEntry;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.gregtechceu.gtceu.api.tag.TagPrefix;

import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.component.Unbreakable;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;

import com.google.common.collect.ArrayTable;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.util.entry.ItemEntry;
import com.tterrag.registrate.util.entry.ItemProviderEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static com.gregtechceu.gtceu.common.registry.GTRegistration.REGISTRATE;
import static com.gregtechceu.gtceu.data.misc.GTCreativeModeTabs.MATERIAL_ITEM;
import static com.gregtechceu.gtceu.data.misc.GTCreativeModeTabs.TOOL;

public class GTMaterialItems {

    // Reference Table Builders
    static ImmutableTable.Builder<TagPrefix, Material, ItemEntry<TagPrefixItem>> MATERIAL_ITEMS_BUILDER = ImmutableTable
            .builder();

    // Reference Maps
    public static final Map<MaterialEntry, Supplier<? extends ItemLike>> toUnify = new HashMap<>();
    public static final Map<TagPrefix, TagPrefix> purifyMap = new HashMap<>();
    static {
        purifyMap.put(TagPrefix.crushed, TagPrefix.crushedPurified);
        purifyMap.put(TagPrefix.dustImpure, TagPrefix.dust);
        purifyMap.put(TagPrefix.dustPure, TagPrefix.dust);
    }

    // Reference Tables
    public static Table<TagPrefix, Material, ItemEntry<TagPrefixItem>> MATERIAL_ITEMS;
    public final static Table<Material, GTToolType, ItemProviderEntry<Item, ? extends IGTTool>> TOOL_ITEMS = ArrayTable
            .create(
                    GTCEuAPI.materialManager.stream()
                            .filter(mat -> mat.hasProperty(PropertyKey.TOOL))
                            .toList(),
                    GTToolType.getTypes().values().stream().toList());

    // Material Items
    public static void generateMaterialItems() {
        REGISTRATE.creativeModeTab(MATERIAL_ITEM);
        for (TagPrefix tagPrefix : GTRegistries.TAG_PREFIXES) {
            if (tagPrefix.doGenerateItem()) {
                for (Material material : GTCEuAPI.materialManager) {
                    GTRegistrate registrate = GTRegistrate.createIgnoringListenerErrors(material.getModid());
                    if (tagPrefix.doGenerateItem(material)) {
                        generateMaterialItem(tagPrefix, material, registrate);
                    }
                }
            }
        }
        MATERIAL_ITEMS = MATERIAL_ITEMS_BUILDER.build();
    }

    private static void generateMaterialItem(TagPrefix tagPrefix, Material material, GTRegistrate registrate) {
        MATERIAL_ITEMS_BUILDER.put(tagPrefix, material, registrate
                .item(tagPrefix.idPattern().formatted(material.getName()),
                        properties -> new TagPrefixItem(properties, tagPrefix, material))
                .onRegister(TagPrefixItem::onRegister)
                .setData(ProviderType.LANG, NonNullBiConsumer.noop())
                .transform(GTItems.unificationItem(tagPrefix, material))
                .properties(p -> p.stacksTo(tagPrefix.maxStackSize()))
                .model(NonNullBiConsumer.noop())
                .color(() -> TagPrefixItem::tintColor)
                .onRegister(GTItems::cauldronInteraction)
                .register());
    }

    // Material Tools
    public static void generateTools() {
        REGISTRATE.creativeModeTab(TOOL);
        for (GTToolType toolType : GTToolType.getTypes().values()) {
            for (Material material : GTCEuAPI.materialManager) {
                GTRegistrate registrate = GTRegistrate.createIgnoringListenerErrors(material.getModid());
                if (material.hasProperty(PropertyKey.TOOL)) {
                    var property = material.getProperty(PropertyKey.TOOL);
                    if (property.hasType(toolType)) {
                        generateTool(material, toolType, registrate);
                    }
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static void generateTool(final Material material, final GTToolType toolType, GTRegistrate registrate) {
        final MaterialToolTier tier = material.getToolTier();
        // spotless:off
        TOOL_ITEMS.put(material, toolType, (ItemProviderEntry<Item, ? extends IGTTool>) (ItemProviderEntry<Item, ?>) registrate
                .item(toolType.idFormat.formatted(tier.material.getName()), p -> toolType.constructor.create(toolType, tier, material, toolType.toolDefinition, p).asItem())
                .properties(p -> {
                    if (!toolType.toolDefinition.getAoEDefinition().isNone()) {
                        p.component(GTDataComponents.AOE, toolType.toolDefinition.getAoEDefinition());
                    }
                    return p.craftRemainder(Items.AIR);
                })
                .properties(p -> {
                    IGTToolDefinition toolStats = toolType.toolDefinition;
                    // Set other tool stats (durability)
                    ToolProperty toolProperty = material.getProperty(PropertyKey.TOOL);

                    Tool tool = toolStats.getTool();
                    List<Tool.Rule> rules = new ArrayList<>(tool.rules());
                    rules.add(Tool.Rule.deniesDrops(tier.getIncorrectBlocksForDrops()));

                    float harvestSpeed = toolStats.getEfficiencyMultiplier() * tier.getSpeed() + toolStats.getBaseEfficiency();
                    for (TagKey<Block> tag : toolType.harvestTags) {
                        rules.add(Tool.Rule.minesAndDrops(tag, harvestSpeed));
                    }
                    p.component(DataComponents.TOOL, new Tool(rules, tool.defaultMiningSpeed(), tool.damagePerBlock()));
                    p.component(GTDataComponents.TOOL_BEHAVIORS, new ToolBehaviors(toolType.toolDefinition.getBehaviors()));


                    float baseDamage = toolStats.getBaseDamage();
                    float attackDamage = 0;
                    // represents a tool that should always have an attack damage value of 0
                    // formatted like this to have attackDamage be final for the lambda.
                    if (baseDamage != Float.MIN_VALUE) {
                        attackDamage = toolProperty.getAttackDamage() + baseDamage;
                    }
                    ItemAttributeModifiers modifiers = ItemAttributeModifiers.builder()
                            .add(Attributes.ATTACK_DAMAGE,
                                    new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, attackDamage, AttributeModifier.Operation.ADD_VALUE),
                                    EquipmentSlotGroup.MAINHAND)
                            .add(Attributes.ATTACK_SPEED,
                                    new AttributeModifier(Item.BASE_ATTACK_SPEED_ID,
                                            toolStats.getEfficiencyMultiplier() * toolProperty.getAttackSpeed() + toolStats.getAttackSpeed(),
                                            AttributeModifier.Operation.ADD_VALUE),
                                    EquipmentSlotGroup.MAINHAND)
                            .build()
                            // don't show the normal vanilla damage and attack speed tooltips, we handle those ourselves
                            .withTooltip(false);
                    p.component(DataComponents.ATTRIBUTE_MODIFIERS, modifiers);

                    // Durability formula we are working with:
                    // Final Durability = (material durability * material durability
                    // multiplier) + (tool definition durability *
                    // definition durability multiplier) - 1
                    // Subtracts 1 internally since Minecraft treats "0" as a valid
                    // durability, but we don't want to display this.

                    int durability = toolProperty.getDurability() * toolProperty.getDurabilityMultiplier();

                    // Most Tool Definitions do not set a base durability, which will lead
                    // to ignoring the multiplier if present. So
                    // apply the multiplier to the material durability if that would happen
                    if (toolStats.getBaseDurability() == 0) {
                        durability *= (int) toolStats.getDurabilityMultiplier();
                    } else {
                        durability += (int) (toolStats.getBaseDurability() * toolStats.getDurabilityMultiplier());
                    }

                    p.durability(durability - 1);
                    if (toolProperty.isUnbreakable()) {
                        p.component(DataComponents.UNBREAKABLE, new Unbreakable(true));
                    }

                    // Set behaviours
                    if (toolProperty.isMagnetic()) {
                        p.component(GTDataComponents.RELOCATE_MINED_BLOCKS, Unit.INSTANCE);
                        p.component(GTDataComponents.RELOCATE_MOB_DROPS, Unit.INSTANCE);
                    }
                    return p;
                })
                .setData(ProviderType.LANG, NonNullBiConsumer.noop())
                .model(NonNullBiConsumer.noop())
                .color(() -> IGTTool::tintColor)
                .register());
        // spotless:on
    }
}

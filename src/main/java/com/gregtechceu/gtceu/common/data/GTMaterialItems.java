package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.ArmorProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.ToolProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialEntry;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.item.IGTTool;
import com.gregtechceu.gtceu.api.item.TagPrefixItem;
import com.gregtechceu.gtceu.api.item.datacomponents.ToolBehaviors;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.item.tool.IGTToolDefinition;
import com.gregtechceu.gtceu.api.item.tool.MaterialToolTier;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.gregtechceu.gtceu.common.data.item.GTDataComponents;
import com.gregtechceu.gtceu.common.item.armor.GTArmorItem;

import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

import com.google.common.collect.ArrayTable;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.util.entry.ItemEntry;
import com.tterrag.registrate.util.entry.ItemProviderEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;

import java.util.*;
import java.util.function.Supplier;

import static com.gregtechceu.gtceu.common.data.GTCreativeModeTabs.MATERIAL_ITEM;
import static com.gregtechceu.gtceu.common.data.GTCreativeModeTabs.TOOL;
import static com.gregtechceu.gtceu.common.registry.GTRegistration.REGISTRATE;

public class GTMaterialItems {

    // spotless:off

    // Reference Table Builders
    static ImmutableTable.Builder<TagPrefix, Material, ItemEntry<? extends Item>> MATERIAL_ITEMS_BUILDER = ImmutableTable.builder();

    // Reference Maps
    public static final Map<MaterialEntry, Supplier<? extends ItemLike>> toUnify = new HashMap<>();
    public static final Map<TagPrefix, TagPrefix> purifyMap = new HashMap<>();
    static {
        purifyMap.put(TagPrefix.crushed, TagPrefix.crushedPurified);
        purifyMap.put(TagPrefix.dustImpure, TagPrefix.dust);
        purifyMap.put(TagPrefix.dustPure, TagPrefix.dust);
    }

    // Reference Tables
    public static Table<TagPrefix, Material, ItemEntry<? extends Item>> MATERIAL_ITEMS;
    private static final List<ArmorType> HUMANOID_ARMOR_TYPES = Arrays.stream(ArmorType.values())
            .filter(type -> type.getSlot().getType() == EquipmentSlot.Type.HUMANOID_ARMOR)
            .toList();

    public final static Table<Material, GTToolType, ItemProviderEntry<Item, ? extends IGTTool>> TOOL_ITEMS = ArrayTable.create(
            GTCEuAPI.materialManager.stream().filter(mat -> mat.hasProperty(PropertyKey.TOOL)).toList(),
            GTToolType.getTypes().values().stream().toList());

    public static final Table<Material, ArmorType, ItemEntry<? extends GTArmorItem>> ARMOR_ITEMS = ArrayTable.create(
            GTCEuAPI.materialManager.stream().filter(mat -> mat.hasProperty(PropertyKey.ARMOR)).toList(),
            HUMANOID_ARMOR_TYPES);

    // spotless:on

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
                        properties -> tagPrefix.itemConstructor()
                                .create(material.hasFlag(MaterialFlags.FIRE_RESISTANT) ? properties.fireResistant() :
                                        properties, tagPrefix, material))
                .setData(ProviderType.LANG, NonNullBiConsumer.noop())
                .properties(p -> p.stacksTo(tagPrefix.maxStackSize()))
                .color(() -> TagPrefixItem.tintColor(material))
                .model(() -> NonNullBiConsumer.noop())
                .onRegister(GTItems::cauldronInteraction)
                .transform(GTItems.unificationItem(tagPrefix, material))
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
                    if (!toolType.toolDefinition.getAoEDefinition().isZero()) {
                        p.component(GTDataComponents.AOE, toolType.toolDefinition.getAoEDefinition());
                    }
                    return p;
                })
                .properties(p -> {
                    IGTToolDefinition toolStats = toolType.toolDefinition;
                    // Set other tool stats (durability)
                    ToolProperty toolProperty = material.getProperty(PropertyKey.TOOL);

                    Tool tool = toolStats.getTool();
                    List<Tool.Rule> rules = new ArrayList<>(tool.rules());
                    rules.add(Tool.Rule.deniesDrops(ToolHelper.holderSet(tier.getIncorrectBlocksForDrops())));

                    float harvestSpeed = toolStats.getEfficiencyMultiplier() * tier.getSpeed() + toolStats.getBaseEfficiency();
                    for (TagKey<Block> tag : toolType.harvestTags) {
                        rules.add(Tool.Rule.minesAndDrops(ToolHelper.holderSet(tag), harvestSpeed));
                    }
                    p.component(DataComponents.TOOL,
                            new Tool(rules, tool.defaultMiningSpeed(), tool.damagePerBlock(),
                                    tool.canDestroyBlocksInCreative()));
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
                                    EquipmentSlotGroup.MAINHAND, ItemAttributeModifiers.Display.hidden())
                            .add(Attributes.ATTACK_SPEED,
                                    new AttributeModifier(Item.BASE_ATTACK_SPEED_ID,
                                            toolStats.getEfficiencyMultiplier() * toolProperty.getAttackSpeed() + toolStats.getAttackSpeed(),
                                            AttributeModifier.Operation.ADD_VALUE),
                                    EquipmentSlotGroup.MAINHAND, ItemAttributeModifiers.Display.hidden())
                            // don't show the normal vanilla damage and attack speed tooltips, we handle those ourselves
                            .build();
                    p.attributes(modifiers);

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
                        p.component(DataComponents.UNBREAKABLE, Unit.INSTANCE);
                    }

                    // Set behaviours
                    if (toolProperty.isMagnetic()) {
                        p.component(GTDataComponents.RELOCATE_MINED_BLOCKS, Unit.INSTANCE);
                        p.component(GTDataComponents.RELOCATE_MOB_DROPS, Unit.INSTANCE);
                    }
                    return p;
                })
                .setData(ProviderType.LANG, NonNullBiConsumer.noop())
                .color(() -> IGTTool::tintColor)
                .model(() -> NonNullBiConsumer.noop())
                .register());
        // spotless:on
    }

    // Material Armors
    public static void generateArmors() {
        REGISTRATE.creativeModeTab(TOOL);
        for (ArmorType type : HUMANOID_ARMOR_TYPES) {
            for (Material material : GTCEuAPI.materialManager) {
                GTRegistrate registrate = GTRegistrate.createIgnoringListenerErrors(material.getModid());
                if (material.hasProperty(PropertyKey.ARMOR)) {
                    generateArmor(material, type, registrate);
                }
            }
        }
    }

    private static void generateArmor(final Material material, final ArmorType type, GTRegistrate registrate) {
        final ArmorProperty property = material.getProperty(PropertyKey.ARMOR);
        String id = "%s_%s".formatted(material.getName(), type.getName());
        ARMOR_ITEMS.put(material, type, registrate
                .item(id, p -> new GTArmorItem(type, p, material, property))
                .setData(ProviderType.LANG, NonNullBiConsumer.noop())
                .model(() -> NonNullBiConsumer.noop())
                .clientExtension(() -> () -> new IClientItemExtensions() {

                    @Override
                    public int getArmorLayerTintColor(ItemStack stack, EquipmentClientInfo.Layer layer, int layerIdx,
                                                      int fallbackColor) {
                        int maxColors = material.getMaterialInfo().getColors().size();
                        if (layerIdx >= 0 && layerIdx < maxColors) {
                            return material.getLayerARGB(layerIdx);
                        }
                        return IClientItemExtensions.super.getArmorLayerTintColor(stack, layer, layerIdx,
                                fallbackColor);
                    }

                    @Override
                    public Identifier getArmorTexture(ItemStack stack, EquipmentClientInfo.LayerType layerType,
                                                      EquipmentClientInfo.Layer layer, Identifier fallbackTexture) {
                        Identifier texture = property.getCustomTextureGetter()
                                .getCustomTexture(stack, null, type.getSlot(), layer);
                        return texture == null ? fallbackTexture : texture;
                    }
                })
                .color(() -> TagPrefixItem.tintColor(material))
                .register());
    }
}

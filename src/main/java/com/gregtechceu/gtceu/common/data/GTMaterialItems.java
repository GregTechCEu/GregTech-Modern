package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialEntry;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.item.IGTTool;
import com.gregtechceu.gtceu.api.item.TagPrefixItem;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.gregtechceu.gtceu.common.item.armor.GTArmorItem;
import com.gregtechceu.gtceu.common.item.armor.GTDyeableArmorItem;

import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.util.entry.ItemEntry;
import com.tterrag.registrate.util.entry.ItemProviderEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import snownee.jade.addon.harvest.HarvestToolProvider;
import snownee.jade.addon.harvest.SimpleToolHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import static com.gregtechceu.gtceu.common.data.GTCreativeModeTabs.MATERIAL_ITEM;
import static com.gregtechceu.gtceu.common.data.GTCreativeModeTabs.TOOL;
import static com.gregtechceu.gtceu.common.registry.GTRegistration.REGISTRATE;

@SuppressWarnings("UnstableApiUsage")
public class GTMaterialItems {

    public static void init() {}

    // Reference Table Builders
    static ImmutableTable.Builder<TagPrefix, Material, ItemEntry<? extends Item>> MATERIAL_ITEMS_BUILDER = ImmutableTable
            .builder();

    static ImmutableTable.Builder<Material, GTToolType, ItemProviderEntry<IGTTool>> TOOL_ITEMS_BUILDER = ImmutableTable
            .builder();

    static ImmutableTable.Builder<Material, ArmorItem.Type, ItemEntry<? extends ArmorItem>> ARMOR_ITEMS_BUILDER = ImmutableTable
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
    public static Table<TagPrefix, Material, ItemEntry<? extends Item>> MATERIAL_ITEMS;
    public static Table<Material, GTToolType, ItemProviderEntry<IGTTool>> TOOL_ITEMS;
    public static Table<Material, ArmorItem.Type, ItemEntry<? extends ArmorItem>> ARMOR_ITEMS;

    // Material Items
    public static void generateMaterialItems() {
        REGISTRATE.creativeModeTab(() -> MATERIAL_ITEM);
        for (var tagPrefix : GTRegistries.TAG_PREFIXES) {
            if (tagPrefix.doGenerateItem()) {
                for (Material material : GTRegistries.MATERIALS) {
                    if (tagPrefix.doGenerateItem(material)) {
                        generateMaterialItem(tagPrefix, material,
                                GTRegistrate.createIgnoringListenerErrors(material.getModid()));
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
                .transform(GTItems.unificationItem(tagPrefix, material))
                .properties(p -> p.stacksTo(tagPrefix.maxStackSize()))
                .model(NonNullBiConsumer.noop())
                .color(() -> () -> TagPrefixItem.tintColor(material))
                .onRegister(GTItems::cauldronInteraction)
                .register());
    }

    // Material Tools
    public static void generateTools() {
        REGISTRATE.creativeModeTab(() -> TOOL);
        for (GTToolType toolType : GTToolType.getTypes().values()) {
            for (Material material : GTRegistries.MATERIALS) {
                if (material.hasProperty(PropertyKey.TOOL)) {
                    var property = material.getProperty(PropertyKey.TOOL);
                    if (property.hasType(toolType)) {
                        generateTool(material, toolType,
                                GTRegistrate.createIgnoringListenerErrors(material.getModid()));
                    }
                }
            }
        }
        TOOL_ITEMS = TOOL_ITEMS_BUILDER.build();
    }

    @SuppressWarnings("unchecked")
    private static void generateTool(Material material, GTToolType toolType, GTRegistrate registrate) {
        var tier = material.getToolTier();
        TOOL_ITEMS_BUILDER.put(material, toolType, (ItemProviderEntry<IGTTool>) (ItemProviderEntry<?>) registrate
                .item(toolType.idFormat.formatted(tier.material.getName()),
                        p -> toolType.constructor.apply(toolType, tier, material,
                                toolType.toolDefinition, p).asItem())
                .properties(p -> p.craftRemainder(Items.AIR))
                .setData(ProviderType.LANG, NonNullBiConsumer.noop())
                .model(NonNullBiConsumer.noop())
                .color(() -> IGTTool::tintColor)
                .register());
    }

    // Material Armors
    public static void generateArmors() {
        REGISTRATE.creativeModeTab(() -> TOOL);
        for (ArmorItem.Type type : ArmorItem.Type.values()) {
            for (Material material : GTRegistries.MATERIALS) {
                if (material.hasProperty(PropertyKey.ARMOR)) {
                    generateArmor(material, type, GTRegistrate.createIgnoringListenerErrors(material.getModid()));
                }
            }
        }
        ARMOR_ITEMS = ARMOR_ITEMS_BUILDER.build();
    }

    private static void generateArmor(final Material material, final ArmorItem.Type type, GTRegistrate registrate) {
        var property = material.getProperty(PropertyKey.ARMOR);
        if (property.isDyeable()) {
            ARMOR_ITEMS_BUILDER.put(material, type, registrate
                    .item("%s_%s".formatted(material.getName(), type.getName()),
                            p -> new GTDyeableArmorItem(property.getArmorMaterial(), type, p,
                                    material, property))
                    .setData(ProviderType.LANG, NonNullBiConsumer.noop())
                    .model(NonNullBiConsumer.noop())
                    .color(() -> GTArmorItem::tintColor)
                    .register());
        } else {
            ARMOR_ITEMS_BUILDER.put(material, type, registrate
                    .item("%s_%s".formatted(material.getName(), type.getName()),
                            p -> new GTArmorItem(property.getArmorMaterial(), type, p,
                                    material, property))
                    .setData(ProviderType.LANG, NonNullBiConsumer.noop())
                    .model(NonNullBiConsumer.noop())
                    .color(() -> GTArmorItem::tintColor)
                    .register());
        }
    }

    public static class JadeCallWrapper {

        public static void registerToolHandlers() {
            GTMaterialItems.TOOL_ITEMS.columnMap().forEach((type, map) -> {
                if (type.harvestTags.isEmpty() || type.harvestTags.get(0).location().getNamespace().equals("minecraft"))
                    return;
                HarvestToolProvider.registerHandler(new SimpleToolHandler(type.name, type.harvestTags.get(0),
                        map.values().stream().filter(Objects::nonNull).filter(ItemProviderEntry::isPresent)
                                .map(ItemProviderEntry::asItem).toArray(Item[]::new)));
            });
        }
    }
}

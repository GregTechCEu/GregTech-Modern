package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.chemical.material.registry.MaterialRegistry;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialEntry;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.item.IGTTool;
import com.gregtechceu.gtceu.api.item.TagPrefixItem;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.gregtechceu.gtceu.common.item.armor.GTArmorItem;
import com.gregtechceu.gtceu.common.item.armor.GTDyeableArmorItem;

import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

import com.google.common.collect.ArrayTable;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.util.entry.ItemEntry;
import com.tterrag.registrate.util.entry.ItemProviderEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static com.gregtechceu.gtceu.common.data.GTCreativeModeTabs.MATERIAL_ITEM;
import static com.gregtechceu.gtceu.common.data.GTCreativeModeTabs.TOOL;
import static com.gregtechceu.gtceu.common.registry.GTRegistration.REGISTRATE;

@SuppressWarnings("UnstableApiUsage")
public class GTMaterialItems {

    // Reference Table Builders
    static ImmutableTable.Builder<TagPrefix, Material, ItemEntry<? extends Item>> MATERIAL_ITEMS_BUILDER = ImmutableTable
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
    public static final Table<Material, GTToolType, ItemProviderEntry<IGTTool>> TOOL_ITEMS = ArrayTable.create(
            GTCEuAPI.materialManager.getRegisteredMaterials().stream()
                    .filter(mat -> mat.hasProperty(PropertyKey.TOOL))
                    .toList(),
            GTToolType.getTypes().values().stream().toList());
    public static final Table<Material, ArmorItem.Type, ItemEntry<? extends ArmorItem>> ARMOR_ITEMS = ArrayTable.create(
            GTCEuAPI.materialManager.getRegisteredMaterials().stream()
                    .filter(mat -> mat.hasProperty(PropertyKey.ARMOR))
                    .toList(),
            Arrays.asList(ArmorItem.Type.values()));

    // Material Items
    public static void generateMaterialItems() {
        REGISTRATE.creativeModeTab(() -> MATERIAL_ITEM);
        for (var tagPrefix : TagPrefix.values()) {
            if (tagPrefix.doGenerateItem()) {
                for (MaterialRegistry registry : GTCEuAPI.materialManager.getRegistries()) {
                    GTRegistrate registrate = registry.getRegistrate();
                    for (Material material : registry.getAllMaterials()) {
                        if (tagPrefix.doGenerateItem(material)) {
                            generateMaterialItem(tagPrefix, material, registrate);
                        }
                    }
                }
            }
        }
        MATERIAL_ITEMS = MATERIAL_ITEMS_BUILDER.build();
    }

    private static void generateMaterialItem(TagPrefix tagPrefix, Material material, GTRegistrate registrate) {
        MATERIAL_ITEMS_BUILDER.put(tagPrefix, material, registrate
                .item(tagPrefix.idPattern().formatted(material.getName()),
                        properties -> tagPrefix.itemConstructor().create(properties, tagPrefix, material))
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
            for (MaterialRegistry registry : GTCEuAPI.materialManager.getRegistries()) {
                GTRegistrate registrate = registry.getRegistrate();
                for (Material material : registry.getAllMaterials()) {
                    if (material.hasProperty(PropertyKey.TOOL)) {
                        var property = material.getProperty(PropertyKey.TOOL);
                        if (property.hasType(toolType)) {
                            generateTool(material, toolType, registrate);
                        }
                    }
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static void generateTool(Material material, GTToolType toolType, GTRegistrate registrate) {
        var tier = material.getToolTier();
        TOOL_ITEMS.put(material, toolType, (ItemProviderEntry<IGTTool>) (ItemProviderEntry<?>) registrate
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
            for (MaterialRegistry registry : GTCEuAPI.materialManager.getRegistries()) {
                GTRegistrate registrate = registry.getRegistrate();
                for (Material material : registry.getAllMaterials()) {
                    if (material.hasProperty(PropertyKey.ARMOR)) {
                        generateArmor(material, type, registrate);
                    }
                }
            }
        }
    }

    private static void generateArmor(final Material material, final ArmorItem.Type type, GTRegistrate registrate) {
        var property = material.getProperty(PropertyKey.ARMOR);
        if (property.isDyeable()) {
            ARMOR_ITEMS.put(material, type, registrate
                    .item("%s_%s".formatted(material.getName(), type.getName()),
                            p -> new GTDyeableArmorItem(property.getArmorMaterial(), type, p,
                                    material, property))
                    .setData(ProviderType.LANG, NonNullBiConsumer.noop())
                    .model(NonNullBiConsumer.noop())
                    .color(() -> GTArmorItem::tintColor)
                    .register());
        } else {
            ARMOR_ITEMS.put(material, type, registrate
                    .item("%s_%s".formatted(material.getName(), type.getName()),
                            p -> new GTArmorItem(property.getArmorMaterial(), type, p,
                                    material, property))
                    .setData(ProviderType.LANG, NonNullBiConsumer.noop())
                    .model(NonNullBiConsumer.noop())
                    .color(() -> GTArmorItem::tintColor)
                    .register());
        }
    }
}

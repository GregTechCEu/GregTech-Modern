package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.chemical.material.registry.MaterialRegistry;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialEntry;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.item.IGTTool;
import com.gregtechceu.gtceu.api.item.TagPrefixItem;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;

import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

import com.google.common.collect.ArrayTable;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.util.entry.ItemEntry;
import com.tterrag.registrate.util.entry.ItemProviderEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static com.gregtechceu.gtceu.common.data.GTCreativeModeTabs.MATERIAL_ITEM;
import static com.gregtechceu.gtceu.common.data.GTCreativeModeTabs.TOOL;
import static com.gregtechceu.gtceu.common.registry.GTRegistration.REGISTRATE;

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
    public final static Table<Material, GTToolType, ItemProviderEntry<IGTTool>> TOOL_ITEMS = ArrayTable.create(
            GTCEuAPI.materialManager.getRegisteredMaterials().stream()
                    .filter(mat -> mat.hasProperty(PropertyKey.TOOL))
                    .toList(),
            GTToolType.getTypes().values().stream().toList());

    static FileWriter MaterialCSV;
    static FileWriter TagPrefixCSV;
    static FileWriter MTPCSV;

    // Material Items
    public static void generateMaterialItems() {
        try {
            MaterialCSV = new FileWriter("material.csv");
            TagPrefixCSV = new FileWriter("tagprefix.csv");
            MTPCSV = new FileWriter("mtp.csv");
            String matColumns = "id,material_name,molar_mass,price_per_pound\n";
            String tagPrefixColumns = "id,tag_prefix,material_amount\n";
            MaterialCSV.write(matColumns);
            TagPrefixCSV.write(tagPrefixColumns);
        } catch (IOException ignored) {

        }

        int id = 1;
        Object2IntMap<Material> materialIDs = new Object2IntOpenHashMap<>();
        for (var registry : GTCEuAPI.materialManager.getRegistries()) {
            for (var mat : registry.getAllMaterials()) {
                float price = GTValues.RNG.nextFloat() * 160.0f;
                try {
                    StringBuffer s = new StringBuffer();
                    String[] strings = mat.getResourceLocation().getPath().split("_");
                    String name = "";
                    for (var st : strings) {
                        name += st.substring(0, 1).toUpperCase() + st.substring(1);
                    }
                    materialIDs.put(mat, id);
                    s.append(id).append(",").append(name).append(",").append(mat.getMass()).append(",")
                            .append(String.format("%.2f", price)).append('\n');
                    MaterialCSV.write(s.toString());
                } catch (IOException ignored) {}
                id++;
            }
        }

        id = 1;
        Object2IntMap<TagPrefix> tagIDs = new Object2IntOpenHashMap<>();
        for (var tagPrefix : TagPrefix.values()) {
            if (tagPrefix.materialAmount() == -1) continue;
            try {
                StringBuffer s = new StringBuffer();
                tagIDs.put(tagPrefix, id);
                s.append(id).append(",").append(tagPrefix.name).append(',')
                        .append(String.format("%.4f", tagPrefix.materialAmount() / (float) GTValues.M)).append('\n');
                TagPrefixCSV.write(s.toString());
            } catch (IOException ignored) {}
            id++;
        }
        var fluidTag = new TagPrefix("fluid");
        tagIDs.put(fluidTag, tagIDs.size());

        try {
            MaterialCSV.close();
            TagPrefixCSV.close();
        } catch (IOException ignored) {}

        REGISTRATE.creativeModeTab(() -> MATERIAL_ITEM);
        for (var tagPrefix : TagPrefix.values()) {
            if (tagPrefix.doGenerateItem()) {
                for (MaterialRegistry registry : GTCEuAPI.materialManager.getRegistries()) {
                    GTRegistrate registrate = registry.getRegistrate();
                    for (Material material : registry.getAllMaterials()) {
                        if (tagPrefix.doGenerateItem(material)) {
                            if (tagPrefix.materialAmount() != -1) {
                                StringBuilder s = new StringBuilder();
                                s.append(materialIDs.getInt(material)).append(",").append(tagIDs.getInt(tagPrefix))
                                        .append("\n");
                                try {
                                    MTPCSV.write(s.toString());
                                } catch (IOException ignored) {}
                            }
                            generateMaterialItem(tagPrefix, material, registrate);
                        }
                    }
                }
            }
        }

        for (MaterialRegistry registry : GTCEuAPI.materialManager.getRegistries()) {
            for (Material material : registry.getAllMaterials()) {
                if (material.hasProperty(PropertyKey.FLUID)) {
                    StringBuilder s = new StringBuilder();
                    s.append(materialIDs.getInt(material)).append(",").append(tagIDs.getInt(fluidTag)).append("\n");
                    try {
                        MTPCSV.write(s.toString());
                    } catch (IOException ignored) {}
                }
            }
        }

        try {
            MTPCSV.close();
        } catch (IOException ignored) {}

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
}

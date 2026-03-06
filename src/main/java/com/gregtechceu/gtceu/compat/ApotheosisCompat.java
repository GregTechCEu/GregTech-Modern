package com.gregtechceu.gtceu.compat;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.common.data.GTMaterialItems;

import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.fml.InterModComms;

import java.util.Map;
import java.util.Objects;

public final class ApotheosisCompat {

    private ApotheosisCompat() {}

    public static void init() {
        if (!GTCEu.isModLoaded("apotheosis")) return;

        // Tools
        GTMaterialItems.TOOL_ITEMS.columnMap().forEach((toolType, byMaterial) -> {
            String cat = mapToolType(toolType);
            if (cat == null) return;

            byMaterial.values().stream()
                    .filter(Objects::nonNull)
                    .map(entry -> entry.get().get()) // ItemStack
                    .map(stack -> stack.getItem())
                    .distinct()
                    .forEach(item -> sendLootCat(item, cat));
        });

        // Armor
        GTMaterialItems.ARMOR_ITEMS.columnMap().forEach((armorType, byMaterial) -> {
            String cat = mapArmorType(armorType);
            if (cat == null) return;

            byMaterial.values().stream()
                    .filter(Objects::nonNull)
                    .map(entry -> (Item) entry.get())
                    .distinct()
                    .forEach(item -> sendLootCat(item, cat));
        });
    }

    private static String mapArmorType(ArmorItem.Type type) {
        return switch (type) {
            case HELMET -> "apotheosis:helmet";
            case CHESTPLATE -> "apotheosis:chestplate";
            case LEGGINGS -> "apotheosis:leggings";
            case BOOTS -> "apotheosis:boots";
        };
    }

    private static String mapToolType(GTToolType type) {
        // If this fails to compile due to 'name' not existing, replace with: switch (type.toString())
        return switch (type.name) {
            case "SWORD", "KATANA", "SABRE", "KNIFE" -> "apotheosis:sword";
            case "PICKAXE", "MINING_DRILL" -> "apotheosis:pickaxe";
            case "AXE", "CHAINSAW" -> "apotheosis:axe";
            case "SHOVEL" -> "apotheosis:shovel";
            case "HOE" -> "apotheosis:hoe";

            case "HAMMER" -> "apotheosis:pickaxe";
            case "WRENCH", "SCREWDRIVER", "SOFT_MALLET", "HARD_HAMMER", "WIRE_CUTTER", "CROWBAR", "FILE", "SAW" -> "apotheosis:axe";

            default -> null;
        };
    }

    private static void sendLootCat(Item item, String categoryId) {
        InterModComms.sendTo("apotheosis", "loot_category_override", () -> Map.entry(item, categoryId));
    }
}

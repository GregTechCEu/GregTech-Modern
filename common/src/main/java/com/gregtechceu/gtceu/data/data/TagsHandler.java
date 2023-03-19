package com.gregtechceu.gtceu.data.data;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.MarkerMaterials;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.tterrag.registrate.providers.RegistrateTagsProvider;
import net.minecraft.core.Registry;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import static com.gregtechceu.gtceu.api.tag.TagPrefix.*;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;

/**
 * @author KilaBash
 * @date 2023/2/28
 * @implNote ItemTagsHandler
 */
public class TagsHandler {
    public static void initItem(RegistrateTagsProvider<Item> provider) {
        provider.getOrCreateRawBuilder(ChemicalHelper.getTag(craftingLens, MarkerMaterials.Color.White)).addElement(GTItems.MATERIAL_ITEMS.get(lens, Glass).getId()).build();
        ChemicalHelper.UNIFICATION_ENTRY_ITEM.forEach((entry, itemLikes) -> {
            if (itemLikes.isEmpty()) return;
            var material = entry.material;
            if (material != null) {
                var materialTags = entry.tagPrefix.getSubItemTags(material);
                for (TagKey<Item> materialTag : materialTags) {
                    var builder = provider.getOrCreateRawBuilder(materialTag);
                    itemLikes.forEach(item -> builder.addElement(Registry.ITEM.getKey(item.asItem())));
                    builder.build();
                }
            }
            var tagPrefixes = entry.tagPrefix.getItemTags();
            for (TagKey<Item> tagPrefixTag : tagPrefixes) {
                var builder = provider.getOrCreateRawBuilder(tagPrefixTag);
//                itemLikes.forEach(item -> builder.addElement(Registry.ITEM.getKey(item.asItem())));
                if (material != null) {
                    var materialTags = entry.tagPrefix.getSubItemTags(material);
                    for (TagKey<Item> materialTag : materialTags) {
                        builder.addTag(materialTag.location());
                    }
                }
                builder.build();
            }

        });
    }

    public static void initBlock(RegistrateTagsProvider<Block> provider) {
        // while probably we dont need to add block tags for materials?
    }
}

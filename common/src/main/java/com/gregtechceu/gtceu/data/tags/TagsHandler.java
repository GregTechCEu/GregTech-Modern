package com.gregtechceu.gtceu.data.tags;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.tterrag.registrate.providers.RegistrateTagsProvider;
import net.minecraft.core.Registry;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

/**
 * @author KilaBash
 * @date 2023/2/28
 * @implNote ItemTagsHandler
 */
public class TagsHandler {

    public static void initItem(RegistrateTagsProvider<Item> provider) {
            //var tagPrefixes = entry.tagPrefix.getItemTags();
            //for (TagKey<Item> tagPrefixTag : tagPrefixes) {
            //    var builder = provider.getOrCreateRawBuilder(tagPrefixTag);
//                itemLikes.forEach(item -> builder.addElement(Registry.ITEM.getKey(item.asItem())));
            //    if (material != null) {
            //        var materialTags = entry.tagPrefix.getSubItemTags(material);
            //        for (TagKey<Item> materialTag : materialTags) {
            //            builder.addTag(materialTag.location());
            //        }
             //   }
            //    builder.build();
            //}
        TagLoader.init(provider);
    }

    public static void initBlock(RegistrateTagsProvider<Block> provider) {
        // while probably we dont need to add block tags for materials?
    }
}

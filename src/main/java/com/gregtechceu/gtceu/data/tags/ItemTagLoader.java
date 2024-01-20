package com.gregtechceu.gtceu.data.tags;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.MarkerMaterials.Color;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.data.tag.TagUtil;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.tterrag.registrate.providers.RegistrateTagsProvider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.lens;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;

public class ItemTagLoader {

    public static void init(RegistrateTagsProvider<Item> provider) {
        create(provider, CustomTags.CONCRETE_ITEM, Items.WHITE_CONCRETE, Items.ORANGE_CONCRETE, Items.MAGENTA_CONCRETE, Items.LIGHT_BLUE_CONCRETE, Items.YELLOW_CONCRETE, Items.LIME_CONCRETE, Items.PINK_CONCRETE, Items.GRAY_CONCRETE, Items.LIGHT_GRAY_CONCRETE, Items.CYAN_CONCRETE, Items.PURPLE_CONCRETE, Items.BLUE_CONCRETE, Items.BROWN_CONCRETE, Items.GREEN_CONCRETE, Items.RED_CONCRETE, Items.BLACK_CONCRETE);
        create(provider, CustomTags.CONCRETE_POWDER_ITEM, Items.WHITE_CONCRETE_POWDER, Items.ORANGE_CONCRETE_POWDER, Items.MAGENTA_CONCRETE_POWDER, Items.LIGHT_BLUE_CONCRETE_POWDER, Items.YELLOW_CONCRETE_POWDER, Items.LIME_CONCRETE_POWDER, Items.PINK_CONCRETE_POWDER, Items.GRAY_CONCRETE_POWDER, Items.LIGHT_GRAY_CONCRETE_POWDER, Items.CYAN_CONCRETE_POWDER, Items.PURPLE_CONCRETE_POWDER, Items.BLUE_CONCRETE_POWDER, Items.BROWN_CONCRETE_POWDER, Items.GREEN_CONCRETE_POWDER, Items.RED_CONCRETE_POWDER, Items.BLACK_CONCRETE_POWDER);
        create(provider, lens, Color.White, GTItems.MATERIAL_ITEMS.get(lens, Glass).getId(), GTItems.MATERIAL_ITEMS.get(lens, NetherStar).getId());
        create(provider, lens, Color.LightBlue, GTItems.MATERIAL_ITEMS.get(lens, Diamond).getId());
        create(provider, lens, Color.Red, GTItems.MATERIAL_ITEMS.get(lens, Ruby).getId());
        create(provider, lens, Color.Green, GTItems.MATERIAL_ITEMS.get(lens, Emerald).getId());
        create(provider, lens, Color.Blue, GTItems.MATERIAL_ITEMS.get(lens, Sapphire).getId());

        create(provider, "pistons", rl("piston"), rl("sticky_piston"));

        // TODO add to planks mc tag?
        //for (Material material : new Material[]{GTMaterials.Wood, GTMaterials.TreatedWood}) {
        //    for (ItemLike woodPlateStack : ChemicalHelper.getItems(new UnificationEntry(TagPrefix.plate, material))) {
        //        ChemicalHelper.registerUnificationEntry(woodPlateStack, TagPrefix.plank, material);
        //    }
        //}

        // todo match ae2 certus quartz tag
        //OreDictionary.registerUnificationEntry("crystalCertusQuartz", ChemicalHelper.get(TagPrefix.gem, GTMaterials.CertusQuartz));
    }

    private static void create(RegistrateTagsProvider<Item> provider, String tagName, ResourceLocation... rls) {
        create(provider, TagUtil.createItemTag(tagName), rls);
    }

    private static void create(RegistrateTagsProvider<Item> provider, TagPrefix prefix, Material material, ResourceLocation... rls) {
        create(provider, ChemicalHelper.getTag(prefix, material), rls);
    }

    public static void create(RegistrateTagsProvider<Item> provider, TagKey<Item> tagKey, ResourceLocation... rls) {
        var builder = provider.addTag(tagKey);
        for (ResourceLocation rl : rls) {
            builder.addOptional(rl);
        }
    }

    public static void create(RegistrateTagsProvider<Item> provider, TagKey<Item> tagKey, Item... rls) {
        var builder = provider.addTag(tagKey);
        for (Item block : rls) {
            builder.addOptional(BuiltInRegistries.ITEM.getKey(block));
        }
    }

    private static ResourceLocation rl(String name) {
        return new ResourceLocation(name);
    }
}

package com.gregtechceu.gtceu.data.tags;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.MarkerMaterials.Color;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.data.tag.TagUtil;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.tterrag.registrate.providers.RegistrateTagsProvider;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import java.util.Objects;

import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.*;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;

public class ItemTagLoader {

    public static void init(RegistrateTagsProvider<Item> provider) {
        create(provider, lens, Color.White, GTItems.MATERIAL_ITEMS.get(lens, Glass).getId());
        create(provider, lens, Color.White, GTItems.MATERIAL_ITEMS.get(lens, NetherStar).getId());
        create(provider, lens, Color.LightBlue, GTItems.MATERIAL_ITEMS.get(lens, Diamond).getId());
        create(provider, lens, Color.Red, GTItems.MATERIAL_ITEMS.get(lens, Ruby).getId());
        create(provider, lens, Color.Green, GTItems.MATERIAL_ITEMS.get(lens, Emerald).getId());
        create(provider, lens, Color.Blue, GTItems.MATERIAL_ITEMS.get(lens, Sapphire).getId());

        create(provider, "pistons", rl("piston"), rl("sticky_piston"));

        create(provider, CustomTags.TREE_FELLING_TOOLS, GTItems.TOOL_ITEMS.column(GTToolType.AXE).values().stream().filter(Objects::nonNull).map(RegistryEntry::getId).toArray(ResourceLocation[]::new));
        create(provider, CustomTags.AOE_TOOLS, GTItems.TOOL_ITEMS.column(GTToolType.MINING_HAMMER).values().stream().filter(Objects::nonNull).map(RegistryEntry::getId).toArray(ResourceLocation[]::new));

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

    private static void create(RegistrateTagsProvider<Item> provider, TagKey<Item> tagKey, ResourceLocation... rls) {
        var builder = provider.addTag(tagKey);
        for (ResourceLocation rl : rls) {
            builder.addOptional(rl);
        }
    }

    private static ResourceLocation rl(String name) {
        return new ResourceLocation(name);
    }
}

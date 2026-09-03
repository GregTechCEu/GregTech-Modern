package com.gregtechceu.gtceu.api.registry.registrate.entry;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialStack;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;

import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredHolder;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.util.entry.RegistryEntry;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Supplier;

@SuppressWarnings("unused")
public class TagPrefixEntry extends RegistryEntry<TagPrefix, TagPrefix> {

    public final String name;

    public TagPrefixEntry(AbstractRegistrate<?> owner, DeferredHolder<TagPrefix, TagPrefix> key) {
        super(owner, key);
        name = key.getKey().location().getPath();
    }

    public boolean isIgnored(Material material) {
        return value().isIgnored(material);
    }

    @SafeVarargs
    public final void setIgnored(Material material, Supplier<? extends ItemLike>... items) {
        value().setIgnored(material, Arrays.asList(items));
    }

    public void setIgnored(Material material, Collection<Supplier<? extends ItemLike>> items) {
        value().setIgnored(material, items);
    }

    public void setIgnored(Material material, ItemLike... items) {
        value().setIgnored(material, items);
    }

    public void setIgnoredBlock(Material material, Block... blocks) {
        value().setIgnoredBlock(material, blocks);
    }

    public void setIgnored(Material material) {
        value().setIgnored(material);
    }

    public void addSecondaryMaterial(MaterialStack secondaryMaterial) {
        value().addSecondaryMaterial(secondaryMaterial);
    }

    public void modifyMaterialAmount(Material material, float amount) {
        value().modifyMaterialAmount(material, amount);
    }

    public long materialAmount() {
        return value().materialAmount();
    }

    public long getMaterialAmount(Material material) {
        return value().getMaterialAmount(material);
    }

    public boolean doGenerateItem(Material material) {
        return value().doGenerateItem(material);
    }
}

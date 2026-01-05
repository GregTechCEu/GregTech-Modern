package com.gregtechceu.gtceu.data.datagen.tag;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.data.tag.CustomTags;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.EnchantmentTagsProvider;
import net.minecraft.world.item.enchantment.Enchantments;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class EnchantmentTagsLoader extends EnchantmentTagsProvider {

    public EnchantmentTagsLoader(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider,
                                 @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, GTCEu.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.@NotNull Provider provider) {
        this.tag(CustomTags.PREVENTS_HAMMER_CRUSHING).add(Enchantments.SILK_TOUCH);
    }
}

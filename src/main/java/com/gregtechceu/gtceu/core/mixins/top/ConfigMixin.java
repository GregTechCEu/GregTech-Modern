package com.gregtechceu.gtceu.core.mixins.top;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.item.tool.MaterialToolTier;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import mcjty.theoneprobe.config.Config;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Mixin(value = Config.class, remap = false)
public class ConfigMixin {

    @Shadow private static Map<ResourceLocation, String> tooltypeTagsSet;
    @Shadow private static Map<ResourceLocation, String> harvestabilityTagsSet;

    @Inject(method = "getTooltypeTags", at = @At(value = "INVOKE", target = "Ljava/util/List;iterator()Ljava/util/Iterator;"))
    private static void gtceu$injectToolTags(CallbackInfoReturnable<Map<ResourceLocation, String>> cir) {
        for (GTToolType type : GTToolType.getTypes().values()) {
            for (TagKey<Item> tag : type.itemTags) {
                if (!tooltypeTagsSet.containsKey(tag.location())) tooltypeTagsSet.put(tag.location(), Component.translatable("gtceu.tool.class." + type.name).getString());
            }
        }
    }

    @Inject(method = "getHarvestabilityTags", at = @At(value = "INVOKE", target = "Ljava/util/List;iterator()Ljava/util/Iterator;"))
    private static void gtceu$injectHarvestTags(CallbackInfoReturnable<Map<ResourceLocation, String>> cir) {
        Set<Integer> passedTiers = new HashSet<>();
        for (Material mat : GTRegistries.MATERIALS) {
            if (mat.hasProperty(PropertyKey.TOOL)) {
                MaterialToolTier tier = mat.getToolTier();
                int harvestLevel = tier.getLevel();
                if (!passedTiers.contains(harvestLevel)) {
                    passedTiers.add(harvestLevel);
                    TagKey<Block> tag = CustomTags.TOOL_TIERS[harvestLevel];
                    if (!harvestabilityTagsSet.containsKey(tag.location())) harvestabilityTagsSet.put(tag.location(), mat.getLocalizedName().getString());
                }
            }
        }
    }
}

package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.block.MaterialBlock;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconType;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.FluidProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.client.renderer.block.MaterialBlockRenderer;
import com.gregtechceu.gtceu.client.renderer.block.OreBlockRenderer;
import com.gregtechceu.gtceu.client.renderer.item.TagPrefixItemRenderer;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.core.MixinHelpers;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Map;
import java.util.Set;

@Mixin(value = ModelBakery.class, priority = 999)
public abstract class ModelBakeryMixin {

    @Shadow
    public abstract UnbakedModel getModel(ResourceLocation modelLocation);

    @Shadow @Final
    private Map<ResourceLocation, UnbakedModel> unbakedCache;

    @Shadow @Final private Map<ResourceLocation, UnbakedModel> topLevelModels;

    @Shadow @Final private ResourceManager resourceManager;

    @Shadow @Final private static Set<net.minecraft.client.resources.model.Material> UNREFERENCED_TEXTURES;

    /**
     * register additional models as what forge does
     */
    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V", ordinal = 4))
    private void gtceu$injectModelBakery(ProfilerFiller profiler, String name) { // Have to use a redirect here cuz it's to constructor and mixin doesn't like that much
        for (Material material : GTRegistries.MATERIALS.values()) {
            MaterialIconSet iconSet = material.getMaterialIconSet();

            {
                if (material.hasProperty(PropertyKey.ORE) && !material.hasProperty(PropertyKey.GEM)) {
                    MaterialBlockRenderer.getOrCreate(MaterialIconType.rawOreBlock, iconSet).setBlockTexture(gtceu$generateBlockTexture(iconSet, MaterialIconType.rawOreBlock));
                }
                if (material.hasProperty(PropertyKey.INGOT) || material.hasProperty(PropertyKey.GEM) || material.hasFlag(MaterialFlags.FORCE_GENERATE_BLOCK)) {
                    MaterialBlockRenderer.getOrCreate(MaterialIconType.block, iconSet).setBlockTexture(gtceu$generateBlockTexture(iconSet, MaterialIconType.block));
                }
                if (material.hasProperty(PropertyKey.DUST) && material.hasFlag(MaterialFlags.GENERATE_FRAME)) {
                    MaterialBlockRenderer.getOrCreate(MaterialIconType.frameGt, iconSet).setBlockTexture(gtceu$generateBlockTexture(iconSet, MaterialIconType.frameGt));
                }
                if (material.hasProperty(PropertyKey.FLUID)) {
                    FluidProperty prop = material.getProperty(PropertyKey.FLUID);
                    prop.setStillTexture(gtceu$generateBlockTexture(iconSet, MaterialIconType.fluid));
                    prop.setFlowTexture(prop.getStillTexture());
                    MaterialBlockRenderer.getOrCreate(MaterialIconType.fluid, iconSet).setBlockTexture(prop.getStillTexture());
                    MixinHelpers.addFluidTexture(material, prop);
                }
            }

            prefixLoop:
            for (TagPrefix tagPrefix : TagPrefix.values()) {
                MaterialIconType type = tagPrefix.materialIconType();

                if (material.hasProperty(PropertyKey.ORE)) {
                    BlockEntry<? extends MaterialBlock> blockEntry = GTBlocks.MATERIAL_BLOCKS.get(tagPrefix, material);
                    if (blockEntry != null && blockEntry.isPresent()) {
                        MaterialBlock block = blockEntry.get();
                        if (block.getRenderer(block.defaultBlockState()) instanceof OreBlockRenderer oreRenderer) {
                            oreRenderer.setOverlayTexture(gtceu$generateBlockTexture(iconSet, type));
                        }
                        continue prefixLoop;
                    }
                }

                if (tagPrefix.doGenerateItem(material)) {
                    ResourceLocation model = GTCEu.id(String.format("item/material_sets/%s/%s", iconSet.name, type.name()));
                    ResourceLocation foundModel = type.getItemModelPath(iconSet, false);

                    UnbakedModel unbakedmodel = this.getModel(foundModel);
                    this.unbakedCache.put(model, unbakedmodel);
                    this.topLevelModels.put(model, unbakedmodel);
                    TagPrefixItemRenderer.getOrCreate(type, iconSet).setModelLocation(model);
                }
            }
        }
        profiler.popPush(name);
    }

    @Unique
    private ResourceLocation gtceu$generateBlockTexture(MaterialIconSet iconSet, MaterialIconType type) {
        ResourceLocation texture = GTCEu.id(String.format("block/material_sets/%s/%s", iconSet.name, type.name()));
        ResourceLocation foundTexture = type.getBlockTexturePath(iconSet, false);

        ResourceLocation path = GTCEu.id(String.format("textures/block/material_sets/%s/%s.png", iconSet.name, type.name()));
        foundTexture = this.resourceManager.getResource(path).isPresent() ? texture : foundTexture;

        UNREFERENCED_TEXTURES.add(new net.minecraft.client.resources.model.Material(TextureAtlas.LOCATION_BLOCKS, foundTexture));
        return foundTexture;
    }
}

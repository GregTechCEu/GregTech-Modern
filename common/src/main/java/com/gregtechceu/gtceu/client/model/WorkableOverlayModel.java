package com.gregtechceu.gtceu.client.model;

import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.lowdragmc.lowdraglib.LDLib;
import com.lowdragmc.lowdraglib.client.bakedpipeline.FaceQuad;
import com.lowdragmc.lowdraglib.client.model.ModelFactory;
import com.lowdragmc.lowdraglib.client.renderer.IItemRendererProvider;
import com.lowdragmc.lowdraglib.utils.ResourceHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.function.Consumer;

/**
 * @author KilaBash
 * @date 2023/2/20
 * @implNote WorkableOverlayModel
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class WorkableOverlayModel {
    public enum OverlayFace {
        FRONT, BACK, TOP, BOTTOM, SIDE;

        public static final OverlayFace[] VALUES = values();

        public static OverlayFace bySide(Direction side) {
            return switch (side) {
                case DOWN -> BOTTOM;
                case UP -> TOP;
                case NORTH -> FRONT;
                case SOUTH -> BACK;
                case WEST, EAST -> SIDE;
            };
        }
    }

    protected final ResourceLocation location;

    @Environment(EnvType.CLIENT)
    public Map<OverlayFace, ActivePredicate> sprites;

    @Environment(EnvType.CLIENT)
    public static class ActivePredicate {

        private final ResourceLocation normalSprite;
        private final ResourceLocation activeSprite;
        private final ResourceLocation pausedSprite;

        private final ResourceLocation normalSpriteEmissive;
        private final ResourceLocation activeSpriteEmissive;
        private final ResourceLocation pausedSpriteEmissive;

        public ActivePredicate(@Nullable ResourceLocation normalSprite,
                               @Nullable ResourceLocation activeSprite,
                               @Nullable ResourceLocation pausedSprite,
                               @Nullable ResourceLocation normalSpriteEmissive,
                               @Nullable ResourceLocation activeSpriteEmissive,
                               @Nullable ResourceLocation pausedSpriteEmissive) {

            this.normalSprite = normalSprite;
            this.activeSprite = activeSprite;
            this.pausedSprite = pausedSprite;
            this.normalSpriteEmissive = normalSpriteEmissive;
            this.activeSpriteEmissive = activeSpriteEmissive;
            this.pausedSpriteEmissive = pausedSpriteEmissive;
        }

        @Nullable
        public TextureAtlasSprite getSprite(boolean active, boolean workingEnabled) {
            return getTextureAtlasSprite(active, workingEnabled, activeSprite, pausedSprite, normalSprite);
        }

        @Nullable
        public TextureAtlasSprite getEmissiveSprite(boolean active, boolean workingEnabled) {
            return getTextureAtlasSprite(active, workingEnabled, activeSpriteEmissive, pausedSpriteEmissive, normalSpriteEmissive);
        }

        @Nullable
        private TextureAtlasSprite getTextureAtlasSprite(boolean active, boolean workingEnabled, @Nullable ResourceLocation activeSprite, @Nullable ResourceLocation pausedSprite, @Nullable ResourceLocation normalSprite) {
            if (active) {
                if (workingEnabled) {
                    return activeSprite == null ? null : ModelFactory.getBlockSprite(activeSprite);
                } else if (pausedSprite != null) {
                    return ModelFactory.getBlockSprite(pausedSprite);
                }
            }
            return normalSprite == null ? null : ModelFactory.getBlockSprite(normalSprite);
        }
    }

    public WorkableOverlayModel(ResourceLocation location) {
        this.location = location;
        if (LDLib.isClient()) {
            this.sprites = new EnumMap<>(OverlayFace.class);
            this.caches = Tables.newCustomTable(new EnumMap<>(Direction.class), () -> new EnumMap<>(Direction.class));
        }
    }

    @Environment(EnvType.CLIENT)
    public Table<Direction, Direction, List<BakedQuad>[][]> caches;

    @Environment(EnvType.CLIENT)
    public List<BakedQuad> bakeQuads(@Nullable Direction side, Direction frontFacing, boolean isActive, boolean isWorkingEnabled) {
        synchronized (caches) {
            if (side == null) return Collections.emptyList();
            if (!caches.contains(side, frontFacing)) {
                caches.put(side, frontFacing, new List[2][2]);
            }
            var cache = caches.get(side, frontFacing);
            assert cache != null;
            if (cache[isActive ? 0 : 1][isWorkingEnabled ? 0 : 1] == null) {
                var quads = new ArrayList<BakedQuad>();
                for (Direction renderSide : Direction.values()) {
                    var rotation = ModelFactory.getRotation(frontFacing);
                    ActivePredicate predicate = sprites.get(OverlayFace.bySide(renderSide));
                    if (predicate != null) {
                        var texture = predicate.getSprite(isActive, isWorkingEnabled);
                        if (texture != null) {
                            var quad = FaceQuad.bakeFace(FaceQuad.BLOCK, renderSide, texture, rotation, -1, 0, true, true);
                            if (quad.getDirection() == side) {
                                quads.add(quad);
                            }
                        }

                        texture = predicate.getEmissiveSprite(isActive, isWorkingEnabled);
                        if (texture != null) {
                            if (ConfigHolder.INSTANCE.client.machinesEmissiveTextures) {
                                var quad = FaceQuad.bakeFace(FaceQuad.BLOCK, renderSide, texture, rotation, 0, 15, true, false);
                                if (quad.getDirection() == side) {
                                    quads.add(quad);
                                }
                            } else {
                                var quad = FaceQuad.bakeFace(FaceQuad.BLOCK, renderSide, texture, rotation, -1, 0, true, true);
                                if (quad.getDirection() == side) {
                                    quads.add(quad);
                                }
                            }
                        }
                    }
                }
//                return quads;
                cache[isActive ? 0 : 1][isWorkingEnabled ? 0 : 1] = quads;
            }
            return cache[isActive ? 0 : 1][isWorkingEnabled ? 0 : 1];
        }
    }

    @NotNull
    @Environment(EnvType.CLIENT)
    public TextureAtlasSprite getParticleTexture() {
        for (WorkableOverlayModel.ActivePredicate predicate : sprites.values()) {
            TextureAtlasSprite sprite = predicate.getSprite(false, false);
            if (sprite != null) return sprite;
        }
        return ModelFactory.getBlockSprite(MissingTextureAtlasSprite.getLocation());
    }

    @Environment(EnvType.CLIENT)
    public void renderItem(ItemStack stack, ItemDisplayContext transformType, boolean leftHand, PoseStack matrixStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay, BakedModel model) {
        IItemRendererProvider.disabled.set(true);
        Minecraft.getInstance().getItemRenderer().render(stack, transformType, leftHand, matrixStack, buffer, combinedLight, combinedOverlay,
                (ItemBakedModel) (state, direction, random) -> bakeQuads(direction, Direction.NORTH, false, false));
        IItemRendererProvider.disabled.set(false);
    }

    @Environment(EnvType.CLIENT)
    public void registerTextureAtlas(Consumer<ResourceLocation> register) {
        sprites.clear();
//        caches.clear();
        for (OverlayFace overlayFace : OverlayFace.VALUES) {
            final String overlayPath = "/overlay_" + overlayFace.name().toLowerCase(Locale.ROOT);

            var normalSprite = new ResourceLocation(location.getNamespace(), location.getPath() + overlayPath);
            if (!ResourceHelper.isTextureExist(normalSprite)) continue;
            register.accept(normalSprite);

            // normal

            final String active = String.format("%s_active", overlayPath);
            ResourceLocation activeSprite = new ResourceLocation(location.getNamespace(), location.getPath() + active);
            if (ResourceHelper.isTextureExist(activeSprite)) register.accept(activeSprite); else activeSprite = normalSprite;


            final String paused = String.format("%s_paused", overlayPath);
            ResourceLocation pausedSprite = new ResourceLocation(location.getNamespace(), location.getPath() + paused);
            if (ResourceHelper.isTextureExist(pausedSprite)) register.accept(pausedSprite); else pausedSprite = normalSprite;


            // emissive
            ResourceLocation normalSpriteEmissive = new ResourceLocation(location.getNamespace(), location.getPath() + "_emissive");
            if (ResourceHelper.isTextureExist(normalSpriteEmissive)) register.accept(normalSpriteEmissive); else normalSpriteEmissive = null;

            ResourceLocation activeSpriteEmissive = new ResourceLocation(location.getNamespace(), location.getPath() + active + "_emissive");
            if (ResourceHelper.isTextureExist(activeSpriteEmissive)) register.accept(activeSpriteEmissive); else activeSpriteEmissive = null;

            ResourceLocation pausedSpriteEmissive = new ResourceLocation(location.getNamespace(), location.getPath() + paused + "_emissive");
            if (ResourceHelper.isTextureExist(pausedSpriteEmissive)) register.accept(pausedSpriteEmissive); else pausedSpriteEmissive = null;

            sprites.put(overlayFace, new ActivePredicate(normalSprite, activeSprite, pausedSprite,
                    normalSpriteEmissive, activeSpriteEmissive, pausedSpriteEmissive));
        }
    }

}

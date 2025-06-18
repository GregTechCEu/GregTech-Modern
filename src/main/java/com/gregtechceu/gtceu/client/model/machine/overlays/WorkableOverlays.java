package com.gregtechceu.gtceu.client.model.machine.overlays;

import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.registry.registrate.provider.GTBlockstateProvider;
import com.gregtechceu.gtceu.common.data.models.GTMachineModels;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.data.ExistingFileHelper;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class WorkableOverlays {

    public static WorkableOverlays get(ResourceLocation textureDir, ExistingFileHelper fileHelper) {
        WorkableOverlays model = new WorkableOverlays(textureDir);

        for (OverlayFace overlayFace : OverlayFace.VALUES) {
            final String overlayPath = "/" + GTMachineModels.OVERLAY_PREFIX + overlayFace.getName();

            // normal
            var normalSprite = textureDir.withSuffix(overlayPath);
            if (!fileHelper.exists(normalSprite, GTBlockstateProvider.TEXTURE)) {
                model.textures.put(overlayFace, StatusTextures.EMPTY);
                continue;
            }
            ResourceLocation activeSprite = normalSprite.withSuffix("_active");
            if (!fileHelper.exists(activeSprite, GTBlockstateProvider.TEXTURE)) activeSprite = normalSprite;

            ResourceLocation pausedSprite = normalSprite.withSuffix("_paused");
            if (!fileHelper.exists(pausedSprite, GTBlockstateProvider.TEXTURE)) pausedSprite = normalSprite;

            // emissive
            ResourceLocation normalSpriteEmissive = normalSprite.withSuffix("_emissive");
            if (!fileHelper.exists(normalSpriteEmissive, GTBlockstateProvider.TEXTURE)) normalSpriteEmissive = null;

            ResourceLocation activeSpriteEmissive = activeSprite.withSuffix("_emissive");
            if (!fileHelper.exists(activeSpriteEmissive, GTBlockstateProvider.TEXTURE)) activeSpriteEmissive = null;

            ResourceLocation pausedSpriteEmissive = pausedSprite.withSuffix("_emissive");
            if (!fileHelper.exists(pausedSpriteEmissive, GTBlockstateProvider.TEXTURE)) pausedSpriteEmissive = null;

            model.textures.put(overlayFace, new StatusTextures(normalSprite, activeSprite, pausedSprite,
                    normalSpriteEmissive, activeSpriteEmissive, pausedSpriteEmissive));
        }
        return model;
    }

    @Getter
    private final ResourceLocation location;

    @Getter
    private final Map<OverlayFace, StatusTextures> textures = new EnumMap<>(OverlayFace.class);

    public WorkableOverlays(ResourceLocation location) {
        this.location = location;
    }

    public enum OverlayFace {

        FRONT,
        BACK,
        TOP,
        BOTTOM,
        SIDE;

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

        public String getName() {
            return this.name().toLowerCase(Locale.ROOT);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class StatusTextures {

        public static final StatusTextures EMPTY = new StatusTextures();

        private final Map<RecipeLogic.Status, ResourceLocation> textures = new HashMap<>();
        private final Map<RecipeLogic.Status, ResourceLocation> emissiveTextures = new HashMap<>();

        public StatusTextures(@Nullable ResourceLocation normalSprite,
                              @Nullable ResourceLocation activeSprite,
                              @Nullable ResourceLocation pausedSprite,
                              @Nullable ResourceLocation normalSpriteEmissive,
                              @Nullable ResourceLocation activeSpriteEmissive,
                              @Nullable ResourceLocation pausedSpriteEmissive) {
            textures.put(RecipeLogic.Status.IDLE, normalSprite);
            emissiveTextures.put(RecipeLogic.Status.IDLE, normalSpriteEmissive);

            textures.put(RecipeLogic.Status.WORKING, activeSprite);
            emissiveTextures.put(RecipeLogic.Status.WORKING, activeSpriteEmissive);
            textures.put(RecipeLogic.Status.WAITING, activeSprite);
            emissiveTextures.put(RecipeLogic.Status.WAITING, activeSpriteEmissive);

            textures.put(RecipeLogic.Status.SUSPEND, pausedSprite);
            emissiveTextures.put(RecipeLogic.Status.SUSPEND, pausedSpriteEmissive);
        }

        private StatusTextures() {}

        public @Nullable ResourceLocation getTexture(@NotNull RecipeLogic.Status status) {
            return textures.get(status);
        }

        public @Nullable ResourceLocation getEmissiveTexture(@NotNull RecipeLogic.Status status) {
            return emissiveTextures.get(status);
        }
    }
}

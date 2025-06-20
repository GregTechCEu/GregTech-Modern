package com.gregtechceu.gtceu.client.model.machine.overlays;

import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic.Status;
import com.gregtechceu.gtceu.api.registry.registrate.provider.GTBlockstateProvider;
import com.gregtechceu.gtceu.common.data.models.GTMachineModels;
import com.gregtechceu.gtceu.common.data.models.GTModels;

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

        private final Map<Status, ResourceLocation> textures = new EnumMap<>(Status.class);
        private final Map<Status, ResourceLocation> emissiveTextures = new EnumMap<>(Status.class);

        public StatusTextures(@Nullable ResourceLocation normalSprite,
                              @Nullable ResourceLocation activeSprite,
                              @Nullable ResourceLocation pausedSprite,
                              @Nullable ResourceLocation normalSpriteEmissive,
                              @Nullable ResourceLocation activeSpriteEmissive,
                              @Nullable ResourceLocation pausedSpriteEmissive) {
            textures.put(Status.IDLE, normalSprite);
            emissiveTextures.put(Status.IDLE, normalSpriteEmissive);

            textures.put(Status.WORKING, activeSprite);
            emissiveTextures.put(Status.WORKING, activeSpriteEmissive);
            textures.put(Status.WAITING, activeSprite);
            emissiveTextures.put(Status.WAITING, activeSpriteEmissive);

            textures.put(Status.SUSPEND, pausedSprite);
            emissiveTextures.put(Status.SUSPEND, pausedSpriteEmissive);
        }

        private StatusTextures() {}

        public @NotNull ResourceLocation getTexture(@NotNull Status status) {
            ResourceLocation value = textures.get(status);
            return value != null ? value : GTModels.BLANK_TEXTURE;
        }

        public @NotNull ResourceLocation getEmissiveTexture(@NotNull Status status) {
            ResourceLocation value = emissiveTextures.get(status);
            return value != null ? value : GTModels.BLANK_TEXTURE;
        }
    }
}

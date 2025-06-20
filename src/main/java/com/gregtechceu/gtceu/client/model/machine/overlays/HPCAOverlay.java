package com.gregtechceu.gtceu.client.model.machine.overlays;

import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic.Status;
import com.gregtechceu.gtceu.api.registry.registrate.provider.GTBlockstateProvider;
import com.gregtechceu.gtceu.common.data.models.GTModels;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.data.ExistingFileHelper;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class HPCAOverlay {

    // spotless:off
    public static HPCAOverlay get(ResourceLocation normalSprite, ResourceLocation damagedSprite,
                                  ExistingFileHelper fileHelper) {
        // normal
        if (!fileHelper.exists(normalSprite, GTBlockstateProvider.TEXTURE)) {
            return HPCAOverlay.EMPTY;
        }
        ResourceLocation activeSprite = normalSprite.withSuffix("_active");
        if (!fileHelper.exists(activeSprite, GTBlockstateProvider.TEXTURE)) activeSprite = normalSprite;

        ResourceLocation damagedActiveSprite = damagedSprite.withSuffix("_active");
        if (!fileHelper.exists(damagedActiveSprite, GTBlockstateProvider.TEXTURE)) damagedActiveSprite = damagedSprite;

        // emissive
        ResourceLocation normalSpriteEmissive = normalSprite.withSuffix("_emissive");
        if (!fileHelper.exists(normalSpriteEmissive, GTBlockstateProvider.TEXTURE)) normalSpriteEmissive = null;

        ResourceLocation activeSpriteEmissive = activeSprite.withSuffix("_emissive");
        if (!fileHelper.exists(activeSpriteEmissive, GTBlockstateProvider.TEXTURE)) activeSpriteEmissive = null;

        ResourceLocation damagedSpriteEmissive = damagedSprite.withSuffix("_emissive");
        if (!fileHelper.exists(damagedSpriteEmissive, GTBlockstateProvider.TEXTURE)) damagedSpriteEmissive = null;

        ResourceLocation damagedActiveSpriteEmissive = damagedActiveSprite.withSuffix("_emissive");
        if (!fileHelper.exists(damagedActiveSpriteEmissive, GTBlockstateProvider.TEXTURE)) damagedActiveSpriteEmissive = null;

        return new HPCAOverlay(normalSprite, activeSprite, damagedSprite, damagedActiveSprite,
                normalSpriteEmissive, activeSpriteEmissive, damagedSpriteEmissive, damagedActiveSpriteEmissive);
    }
    // spotless:on

    public static final HPCAOverlay EMPTY = new HPCAOverlay();

    private final Map<Status, ResourceLocation> textures = new EnumMap<>(Status.class);
    private final Map<Status, ResourceLocation> emissiveTextures = new EnumMap<>(Status.class);

    public HPCAOverlay(@Nullable ResourceLocation normalSprite,
                       @Nullable ResourceLocation activeSprite,
                       @Nullable ResourceLocation damagedSprite,
                       @Nullable ResourceLocation damagedActiveSprite,
                       @Nullable ResourceLocation normalSpriteEmissive,
                       @Nullable ResourceLocation activeSpriteEmissive,
                       @Nullable ResourceLocation damagedSpriteEmissive,
                       @Nullable ResourceLocation damagedActiveSpriteEmissive) {
        textures.put(Status.IDLE, normalSprite);
        emissiveTextures.put(Status.IDLE, normalSpriteEmissive);

        textures.put(Status.WORKING, activeSprite);
        emissiveTextures.put(Status.WORKING, activeSpriteEmissive);

        textures.put(Status.WAITING, damagedActiveSprite);
        emissiveTextures.put(Status.WAITING, damagedActiveSpriteEmissive);
        textures.put(Status.SUSPEND, damagedSprite);
        emissiveTextures.put(Status.SUSPEND, damagedSpriteEmissive);
    }

    private HPCAOverlay() {}

    private static Status getStatus(boolean active, boolean damaged) {
        if (damaged && active) return Status.WAITING;
        else if (damaged) return Status.SUSPEND;
        else if (active) return Status.WORKING;
        else return Status.IDLE;
    }

    public @NotNull ResourceLocation getTexture(boolean active, boolean damaged) {
        ResourceLocation value = textures.get(getStatus(active, damaged));
        return value != null ? value : GTModels.BLANK_TEXTURE;
    }

    public @NotNull ResourceLocation getEmissiveTexture(boolean active, boolean damaged) {
        ResourceLocation value = emissiveTextures.get(getStatus(active, damaged));
        return value != null ? value : GTModels.BLANK_TEXTURE;
    }
}

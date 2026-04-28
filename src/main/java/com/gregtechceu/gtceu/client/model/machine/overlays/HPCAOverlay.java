package com.gregtechceu.gtceu.client.model.machine.overlays;

import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic.Status;
import com.gregtechceu.gtceu.api.registry.registrate.provider.GTBlockstateProvider;
import com.gregtechceu.gtceu.common.data.models.GTModels;
import com.gregtechceu.gtceu.utils.data.ExistingFileHelper;
import com.gregtechceu.gtceu.utils.data.RuntimeExistingFileHelper;

import net.minecraft.resources.Identifier;

import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class HPCAOverlay {

    // spotless:off
    public static HPCAOverlay get(Identifier normalSprite, Identifier damagedSprite,
                                  ExistingFileHelper fileHelper) {
        if (fileHelper instanceof RuntimeExistingFileHelper runtimeFileHelper) {
            // if fileHelper is an instance of RuntimeExistingFileHelper, we have to enable its existence checking.
            // the AutoCloseable warning is suppressed here because there's no clean way to
            // use a try-with-resources statement in this.
            //noinspection resource
            fileHelper = runtimeFileHelper.activeHelper();
        }

        try {
            // normal
            if (!fileHelper.exists(normalSprite, GTBlockstateProvider.TEXTURE)) {
                return HPCAOverlay.EMPTY;
            }
            Identifier activeSprite = normalSprite.withSuffix("_active");
            if (!fileHelper.exists(activeSprite, GTBlockstateProvider.TEXTURE)) activeSprite = normalSprite;

            Identifier damagedActiveSprite = damagedSprite.withSuffix("_active");
            if (!fileHelper.exists(damagedActiveSprite, GTBlockstateProvider.TEXTURE)) damagedActiveSprite = damagedSprite;

            // emissive
            Identifier normalSpriteEmissive = normalSprite.withSuffix("_emissive");
            if (!fileHelper.exists(normalSpriteEmissive, GTBlockstateProvider.TEXTURE)) normalSpriteEmissive = null;

            Identifier activeSpriteEmissive = activeSprite.withSuffix("_emissive");
            if (!fileHelper.exists(activeSpriteEmissive, GTBlockstateProvider.TEXTURE)) activeSpriteEmissive = null;

            Identifier damagedSpriteEmissive = damagedSprite.withSuffix("_emissive");
            if (!fileHelper.exists(damagedSpriteEmissive, GTBlockstateProvider.TEXTURE)) damagedSpriteEmissive = null;

            Identifier damagedActiveSpriteEmissive = damagedActiveSprite.withSuffix("_emissive");
            if (!fileHelper.exists(damagedActiveSpriteEmissive, GTBlockstateProvider.TEXTURE)) damagedActiveSpriteEmissive = null;

            return new HPCAOverlay(normalSprite, activeSprite, damagedSprite, damagedActiveSprite,
                    normalSpriteEmissive, activeSpriteEmissive, damagedSpriteEmissive, damagedActiveSpriteEmissive);
        } finally {
            if (fileHelper instanceof RuntimeExistingFileHelper.Active activeHelper) {
                // close the active helper, just for good measure.
                // Also in case we ever make it do anything, this won't be forgotten.
                activeHelper.close();
            }
        }
    }
    // spotless:on

    public static final HPCAOverlay EMPTY = new HPCAOverlay();

    private final Map<Status, Identifier> textures = new EnumMap<>(Status.class);
    private final Map<Status, Identifier> emissiveTextures = new EnumMap<>(Status.class);

    public HPCAOverlay(@Nullable Identifier normalSprite,
                       @Nullable Identifier activeSprite,
                       @Nullable Identifier damagedSprite,
                       @Nullable Identifier damagedActiveSprite,
                       @Nullable Identifier normalSpriteEmissive,
                       @Nullable Identifier activeSpriteEmissive,
                       @Nullable Identifier damagedSpriteEmissive,
                       @Nullable Identifier damagedActiveSpriteEmissive) {
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

    public @NotNull Identifier getTexture(boolean active, boolean damaged) {
        Identifier value = textures.get(getStatus(active, damaged));
        return value != null ? value : GTModels.BLANK_TEXTURE;
    }

    public @NotNull Identifier getEmissiveTexture(boolean active, boolean damaged) {
        Identifier value = emissiveTextures.get(getStatus(active, damaged));
        return value != null ? value : GTModels.BLANK_TEXTURE;
    }
}

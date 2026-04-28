package com.gregtechceu.gtceu.integration.kjs.builders;

import com.gregtechceu.gtceu.api.capability.recipe.*;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.sound.SoundEntry;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.integration.kjs.helpers.GTResourceLocation;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import dev.latvian.mods.kubejs.registry.BuilderBase;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@SuppressWarnings("unused")
@Accessors(chain = true)
public class GTRecipeTypeBuilder extends BuilderBase<GTRecipeType> {

    public transient String name, category;
    public transient final Object2IntMap<RecipeCapability<?>> maxInputs;
    public transient final Object2IntMap<RecipeCapability<?>> maxOutputs;
    @Nullable
    private Supplier<?> progressBarTexture;
    @Nullable
    private Supplier<?> steamProgressBarTexture;
    private GTRecipeType.ProgressBarDirection progressBarMoveType;
    private GTRecipeType.ProgressBarDirection steamMoveType;
    private transient final Map<Byte, Supplier<?>> slotOverlays;
    @Setter
    @Nullable
    protected transient SoundEntry sound;
    @Setter
    protected transient boolean hasResearchSlot;
    @Setter
    protected transient int maxTooltips;

    @Setter
    @Nullable
    private transient GTRecipeType smallRecipeMap;
    @Setter
    @Nullable
    private transient Supplier<ItemStack> iconSupplier;
    @Nullable
    @Setter
    protected transient GTRecipeType.RecipeUIBuilder uiBuilder;

    public GTRecipeTypeBuilder(ResourceLocation i) {
        super(GTResourceLocation.implicitAsGtceu(i));
        name = this.id.getPath();
        category = "custom";
        maxInputs = new Object2IntOpenHashMap<>();
        maxOutputs = new Object2IntOpenHashMap<>();
        steamProgressBarTexture = null;
        progressBarMoveType = GTRecipeType.ProgressBarDirection.LEFT_TO_RIGHT;
        steamMoveType = GTRecipeType.ProgressBarDirection.LEFT_TO_RIGHT;
        slotOverlays = new HashMap<>();
        this.sound = null;
        this.hasResearchSlot = false;
        this.maxTooltips = 4;
        this.smallRecipeMap = null;
        this.iconSupplier = null;
        this.uiBuilder = null;
    }

    public GTRecipeTypeBuilder category(String category) {
        this.category = category;
        return this;
    }

    public GTRecipeTypeBuilder setMaxIOSize(int maxInputs, int maxOutputs, int maxFluidInputs, int maxFluidOutputs) {
        return setMaxSize(IO.IN, ItemRecipeCapability.CAP, maxInputs)
                .setMaxSize(IO.IN, FluidRecipeCapability.CAP, maxFluidInputs)
                .setMaxSize(IO.OUT, ItemRecipeCapability.CAP, maxOutputs)
                .setMaxSize(IO.OUT, FluidRecipeCapability.CAP, maxFluidOutputs);
    }

    public GTRecipeTypeBuilder setEUIO(IO io) {
        if (io.support(IO.IN)) {
            setMaxSize(IO.IN, EURecipeCapability.CAP, 1);
        }
        if (io.support(IO.OUT)) {
            setMaxSize(IO.OUT, EURecipeCapability.CAP, 1);
        }
        return this;
    }

    public GTRecipeTypeBuilder setMaxSize(IO io, RecipeCapability<?> cap, int max) {
        if (io == IO.IN || io == IO.BOTH) {
            maxInputs.put(cap, max);
        }
        if (io == IO.OUT || io == IO.BOTH) {
            maxOutputs.put(cap, max);
        }
        return this;
    }

    public GTRecipeTypeBuilder setSlotOverlay(boolean isOutput, boolean isFluid, Object slotOverlay) {
        return this.setSlotOverlay(isOutput, isFluid, false, slotOverlay).setSlotOverlay(isOutput, isFluid, true,
                slotOverlay);
    }

    public GTRecipeTypeBuilder setSlotOverlay(boolean isOutput, boolean isFluid, boolean isLast,
                                              Object slotOverlay) {
        this.slotOverlays.put(GTRecipeType.overlayKey(isOutput, isFluid, isLast), () -> slotOverlay);
        return this;
    }

    public GTRecipeTypeBuilder setProgressBar(Object progressBar, Object moveType) {
        this.progressBarTexture = () -> progressBar;
        this.progressBarMoveType = parseDirection(moveType);
        return this;
    }

    public GTRecipeTypeBuilder setSteamProgressBar(Object progressBar, Object moveType) {
        this.steamProgressBarTexture = () -> progressBar;
        this.steamMoveType = parseDirection(moveType);
        return this;
    }

    @Override
    public String getTranslationKeyGroup() {
        return GTRecipeType.LANGUAGE_KEY_PATH;
    }

    @Override
    public GTRecipeType createObject() {
        var type = GTRecipeTypes.register(name, category);
        type.maxInputs.putAll(maxInputs);
        type.maxOutputs.putAll(maxOutputs);
        slotOverlays.forEach(type::setSlotOverlay);
        if (progressBarTexture != null) {
            type.setProgressBar(progressBarTexture, progressBarMoveType);
        }
        if (steamProgressBarTexture != null) {
            type.setSteamProgressBar(steamProgressBarTexture, steamMoveType);
        }
        type.setSound(sound);
        type.setHasResearchSlot(hasResearchSlot);
        type.setMaxTooltips(maxTooltips);
        type.setSmallRecipeMap(smallRecipeMap);
        type.setIconSupplier(iconSupplier);
        type.setUiBuilder(uiBuilder);
        return type;
    }

    private static GTRecipeType.ProgressBarDirection parseDirection(Object moveType) {
        if (moveType instanceof GTRecipeType.ProgressBarDirection direction) {
            return direction;
        }
        if (moveType instanceof Enum<?> direction) {
            return GTRecipeType.ProgressBarDirection.valueOf(direction.name());
        }
        return GTRecipeType.ProgressBarDirection.valueOf(String.valueOf(moveType));
    }
}

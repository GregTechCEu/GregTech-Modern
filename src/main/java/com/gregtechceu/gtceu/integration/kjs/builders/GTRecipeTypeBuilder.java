package com.gregtechceu.gtceu.integration.kjs.builders;

import com.gregtechceu.gtceu.api.capability.recipe.*;
import com.gregtechceu.gtceu.api.gui.SteamTexture;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.registry.registrate.BuilderBase;
import com.gregtechceu.gtceu.api.sound.SoundEntry;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ProgressTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.utils.Position;
import com.lowdragmc.lowdraglib.utils.Rect;
import com.lowdragmc.lowdraglib.utils.Size;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectArrayMap;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class GTRecipeTypeBuilder extends BuilderBase<GTRecipeType> {
    public transient String name, category;
    public transient final Object2IntMap<RecipeCapability<?>> maxInputs;
    public transient final Object2IntMap<RecipeCapability<?>> maxOutputs;
    private ProgressTexture progressBarTexture;
    private SteamTexture steamProgressBarTexture;
    private ProgressTexture.FillDirection steamMoveType;
    private transient IGuiTexture specialTexture;
    private transient Rect specialTexturePosition;
    private transient final Byte2ObjectMap<IGuiTexture> slotOverlays;
    @Nullable
    protected SoundEntry sound;
    protected int maxTooltips;
    protected boolean isFuelRecipeType;

    private GTRecipeType smallRecipeMap;
    private Supplier<ItemStack> iconSupplier;
    @Nullable
    protected BiConsumer<GTRecipe, WidgetGroup> uiBuilder;

    public GTRecipeTypeBuilder(ResourceLocation i, Object... args) {
        super(i);
        name = i.getPath();
        maxInputs = new Object2IntOpenHashMap<>();
        maxOutputs = new Object2IntOpenHashMap<>();
        progressBarTexture = new ProgressTexture();
        steamProgressBarTexture = null;
        steamMoveType = ProgressTexture.FillDirection.LEFT_TO_RIGHT;
        slotOverlays = new Byte2ObjectArrayMap<>();
        this.sound = null;
        this.maxTooltips = 3;
        this.isFuelRecipeType = false;
        this.smallRecipeMap = null;
        this.iconSupplier = null;
        this.uiBuilder = null;
    }

    public GTRecipeTypeBuilder category(String category) {
        this.category = category;
        return this;
    }

    public GTRecipeTypeBuilder setMaxIOSize(int maxInputs, int maxOutputs, int maxFluidInputs, int maxFluidOutputs) {
        return setMaxSize(IO.IN, ItemRecipeCapability.CAP, maxInputs).setMaxSize(IO.IN, FluidRecipeCapability.CAP, maxFluidInputs)
                .setMaxSize(IO.OUT, ItemRecipeCapability.CAP, maxOutputs).setMaxSize(IO.OUT, FluidRecipeCapability.CAP, maxFluidOutputs);
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

    @Deprecated
    public GTRecipeTypeBuilder setSpecialTexture(int x, int y, int width, int height, IGuiTexture area) {
        this.specialTexturePosition = Rect.of(new Position(x, y), new Size(width, height));
        this.specialTexture = area;
        return this;
    }

    public GTRecipeTypeBuilder setSlotOverlay(boolean isOutput, boolean isFluid, IGuiTexture slotOverlay) {
        return this.setSlotOverlay(isOutput, isFluid, false, slotOverlay).setSlotOverlay(isOutput, isFluid, true, slotOverlay);
    }

    public GTRecipeTypeBuilder setSlotOverlay(boolean isOutput, boolean isFluid, boolean isLast, IGuiTexture slotOverlay) {
        this.slotOverlays.put((byte) ((isOutput ? 2 : 0) + (isFluid ? 1 : 0) + (isLast ? 4 : 0)), slotOverlay);
        return this;
    }

    public GTRecipeTypeBuilder setProgressBar(ResourceTexture progressBar, ProgressTexture.FillDirection moveType) {
        this.progressBarTexture = new ProgressTexture(progressBar.getSubTexture(0, 0, 1, 0.5), progressBar.getSubTexture(0, 0.5, 1, 0.5)).setFillDirection(moveType);
        return this;
    }

    public GTRecipeTypeBuilder setSteamProgressBar(SteamTexture progressBar, ProgressTexture.FillDirection moveType) {
        this.steamProgressBarTexture = progressBar;
        this.steamMoveType = moveType;
        return this;
    }

    public GTRecipeTypeBuilder setSound(SoundEntry sound) {
        this.sound = sound;
        return this;
    }

    public GTRecipeTypeBuilder setMaxTooltips(int maxTooltips) {
        this.maxTooltips = maxTooltips;
        return this;
    }

    public GTRecipeTypeBuilder setFuelRecipeType(boolean isFuelRecipeType) {
        this.isFuelRecipeType = isFuelRecipeType;
        return this;
    }

    public GTRecipeTypeBuilder setSmallRecipeMap(GTRecipeType smallRecipeMap) {
        this.smallRecipeMap = smallRecipeMap;
        return this;
    }

    public GTRecipeTypeBuilder setIconSupplier(Supplier<ItemStack> iconSupplier) {
        this.iconSupplier = iconSupplier;
        return this;
    }

    public GTRecipeTypeBuilder setUiBuilder(BiConsumer<GTRecipe, WidgetGroup> uiBuilder) {
        this.uiBuilder = uiBuilder;
        return this;
    }

    @Override
    public GTRecipeType register() {
        var type = GTRecipeTypes.register(name, category);
        type.maxInputs.putAll(maxInputs);
        type.maxOutputs.putAll(maxOutputs);
        type.setSpecialTexture(specialTexturePosition, specialTexture);
        type.getSlotOverlays().putAll(slotOverlays);
        type.setProgressBarTexture(progressBarTexture);
        type.setSteamProgressBarTexture(steamProgressBarTexture);
        type.setSteamMoveType(steamMoveType);
        type.setSound(sound);
        type.setMaxTooltips(maxTooltips);
        type.setFuelRecipeType(isFuelRecipeType);
        type.setSmallRecipeMap(smallRecipeMap);
        type.setIconSupplier(iconSupplier);
        type.setUiBuilder(uiBuilder);
        return value = type;
    }
}

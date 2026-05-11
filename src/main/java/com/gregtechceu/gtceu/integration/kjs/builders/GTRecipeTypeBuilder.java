package com.gregtechceu.gtceu.integration.kjs.builders;

import com.gregtechceu.gtceu.api.capability.recipe.*;
import com.gregtechceu.gtceu.api.gui.SteamTexture;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.sound.SoundEntry;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ProgressTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import dev.latvian.mods.kubejs.registry.BuilderBase;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectArrayMap;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

@SuppressWarnings("unused")
@Accessors(chain = true)
public class GTRecipeTypeBuilder extends BuilderBase<GTRecipeType> {

    public transient String category;
    public transient final Object2IntMap<RecipeCapability<?>> maxInputs = new Object2IntOpenHashMap<>();
    public transient final Object2IntMap<RecipeCapability<?>> maxOutputs = new Object2IntOpenHashMap<>();

    private ProgressTexture progressBarTexture = new ProgressTexture();
    private @Nullable SteamTexture steamProgressBarTexture = null;
    private ProgressTexture.FillDirection steamMoveType = ProgressTexture.FillDirection.LEFT_TO_RIGHT;
    private transient final Byte2ObjectMap<IGuiTexture> slotOverlays = new Byte2ObjectArrayMap<>();
    @Setter
    protected transient @Nullable SoundEntry sound = null;
    @Setter
    protected transient boolean hasResearchSlot;
    @Setter
    protected transient int maxTooltips = 4;

    @Setter
    private transient @Nullable GTRecipeType smallRecipeMap;
    @Setter
    private transient @Nullable Supplier<ItemStack> iconSupplier;
    @Setter
    protected transient @Nullable BiConsumer<GTRecipe, WidgetGroup> uiBuilder;

    public GTRecipeTypeBuilder(ResourceLocation id) {
        super(id);
        category = "custom";
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

    public GTRecipeTypeBuilder setSlotOverlay(boolean isOutput, boolean isFluid, IGuiTexture slotOverlay) {
        return this.setSlotOverlay(isOutput, isFluid, false, slotOverlay).setSlotOverlay(isOutput, isFluid, true,
                slotOverlay);
    }

    public GTRecipeTypeBuilder setSlotOverlay(boolean isOutput, boolean isFluid, boolean isLast,
                                              IGuiTexture slotOverlay) {
        this.slotOverlays.put((byte) ((isOutput ? 2 : 0) + (isFluid ? 1 : 0) + (isLast ? 4 : 0)), slotOverlay);
        return this;
    }

    public GTRecipeTypeBuilder setProgressBar(ResourceTexture progressBar, ProgressTexture.FillDirection moveType) {
        this.progressBarTexture = new ProgressTexture(progressBar.getSubTexture(0, 0, 1, 0.5),
                progressBar.getSubTexture(0, 0.5, 1, 0.5)).setFillDirection(moveType);
        return this;
    }

    public GTRecipeTypeBuilder setSteamProgressBar(SteamTexture progressBar, ProgressTexture.FillDirection moveType) {
        this.steamProgressBarTexture = progressBar;
        this.steamMoveType = moveType;
        return this;
    }

    @Override
    public String getTranslationKeyGroup() {
        return GTRecipeType.LANGUAGE_KEY_PATH;
    }

    @Override
    public GTRecipeType createObject() {
        var type = GTRecipeTypes.register(this.id, this.category);
        type.maxInputs.putAll(this.maxInputs);
        type.maxOutputs.putAll(this.maxOutputs);
        type.getRecipeUI().getSlotOverlays().putAll(this.slotOverlays);
        type.getRecipeUI().setProgressBarTexture(this.progressBarTexture);
        type.getRecipeUI().setSteamProgressBarTexture(this.steamProgressBarTexture);
        type.getRecipeUI().setSteamMoveType(this.steamMoveType);
        type.setSound(this.sound);
        type.setHasResearchSlot(this.hasResearchSlot);
        type.setMaxTooltips(this.maxTooltips);
        type.setSmallRecipeMap(this.smallRecipeMap);
        type.setIconSupplier(this.iconSupplier);
        type.setUiBuilder(this.uiBuilder);

        return type;
    }
}

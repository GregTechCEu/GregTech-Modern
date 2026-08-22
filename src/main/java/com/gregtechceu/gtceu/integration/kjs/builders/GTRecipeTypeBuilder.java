package com.gregtechceu.gtceu.integration.kjs.builders;

import com.gregtechceu.gtceu.api.capability.recipe.*;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.gui.GTRecipeTypeUILayout;
import com.gregtechceu.gtceu.api.recipe.gui.ProgressBarTextureSet;
import com.gregtechceu.gtceu.api.sound.SoundEntry;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.api.drawable.Text;
import dev.latvian.mods.kubejs.registry.BuilderBase;
import dev.latvian.mods.rhino.util.HideFromJS;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@SuppressWarnings("unused")
@Accessors(chain = true)
public class GTRecipeTypeBuilder extends BuilderBase<GTRecipeType> {

    public transient String category;
    public transient final Object2IntMap<RecipeCapability<?>> maxInputs;
    public transient final Object2IntMap<RecipeCapability<?>> maxOutputs;
    @Nullable
    protected SoundEntry sound;
    protected boolean hasResearchSlot;
    protected int maxTooltips;

    private GTRecipeType smallRecipeMap;
    private Supplier<ItemStack> iconSupplier;
    private Consumer<GTRecipeTypeUILayout.Builder> layout;

    public GTRecipeTypeBuilder(ResourceLocation id) {
        super(id);
        category = "custom";
        maxInputs = new Object2IntOpenHashMap<>();
        maxOutputs = new Object2IntOpenHashMap<>();
        this.sound = null;
        this.hasResearchSlot = false;
        this.maxTooltips = 4;
        this.smallRecipeMap = null;
        this.iconSupplier = null;
    }

    public GTRecipeTypeBuilder category(String category) {
        this.category = category;
        return this;
    }

    @HideFromJS
    public GTRecipeTypeBuilder ui(Consumer<GTRecipeTypeUILayout.Builder> builder) {
        this.layout = this.layout == null ? builder : this.layout.andThen(builder);
        return this;
    }

    /**
     * Sets the progress bar texture, e.g. {@code GTGuiTextures.PROGRESS_ARROW}.
     */
    public GTRecipeTypeBuilder setProgressBar(ProgressBarTextureSet progressBar) {
        return ui(builder -> builder.setProgressBar(progressBar));
    }

    /**
     * Adds an overlay to a single slot.
     *
     * @param ioMode    The IO of the slot.
     * @param slotIndex The index of the slot.
     * @param cap       The slot capability.
     * @param overlay   The slot overlay.
     */
    public GTRecipeTypeBuilder setSlotOverlay(IO ioMode, int slotIndex, RecipeCapability<?> cap, IDrawable overlay) {
        return ui(builder -> builder.setSlotOverlay(ioMode, slotIndex, cap, overlay));
    }

    /**
     * Adds an overlay to a range of slots, both ends inclusive.
     *
     * @param ioMode         The IO of the slots.
     * @param slotIndexStart The first slot to add the overlay to.
     * @param slotIndexEnd   The last slot to add the overlay to.
     * @param cap            The slot capability.
     * @param overlay        The slot overlay.
     */
    public GTRecipeTypeBuilder setSlotsOverlay(IO ioMode, int slotIndexStart, int slotIndexEnd,
                                               RecipeCapability<?> cap, IDrawable overlay) {
        return ui(builder -> builder.setSlotsOverlay(ioMode, slotIndexStart, slotIndexEnd, cap, overlay));
    }

    /**
     * Adds an overlay to a single item slot.
     */
    public GTRecipeTypeBuilder setItemSlotOverlay(IO ioMode, int slotIndex, IDrawable overlay) {
        return setSlotOverlay(ioMode, slotIndex, ItemRecipeCapability.CAP, overlay);
    }

    /**
     * Adds an overlay to a range of item slots, both ends inclusive.
     */
    public GTRecipeTypeBuilder setItemSlotsOverlay(IO ioMode, int slotIndexStart, int slotIndexEnd,
                                                   IDrawable overlay) {
        return setSlotsOverlay(ioMode, slotIndexStart, slotIndexEnd, ItemRecipeCapability.CAP, overlay);
    }

    /**
     * Adds an overlay to a single fluid slot.
     */
    public GTRecipeTypeBuilder setFluidSlotOverlay(IO ioMode, int slotIndex, IDrawable overlay) {
        return setSlotOverlay(ioMode, slotIndex, FluidRecipeCapability.CAP, overlay);
    }

    /**
     * Adds an overlay to a range of fluid slots, both ends inclusive.
     */
    public GTRecipeTypeBuilder setFluidSlotsOverlay(IO ioMode, int slotIndexStart, int slotIndexEnd,
                                                    IDrawable overlay) {
        return setSlotsOverlay(ioMode, slotIndexStart, slotIndexEnd, FluidRecipeCapability.CAP, overlay);
    }

    /**
     * Adds a line of text below recipes of this type in the recipe viewer.
     * <p>
     * The function is given the recipe being displayed and returns the text to show for it. Return an
     * empty component to show no line for a given recipe. Call this multiple times to add several lines.
     *
     * @param info Returns the text to show for a recipe.
     */
    public GTRecipeTypeBuilder addRecipeInfo(Function<GTRecipe, Component> info) {
        return ui(builder -> builder.addRecipeUIModifier((recipe, widget) -> {
            Component text = info.apply(recipe);
            if (text == null || text.getString().isEmpty()) return;
            widget.textComponents.child(Text.of(text).asWidget());
        }));
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

    public GTRecipeTypeBuilder setSound(SoundEntry sound) {
        this.sound = sound;
        return this;
    }

    public GTRecipeTypeBuilder setHasResearchSlot(boolean hasResearchSlot) {
        this.hasResearchSlot = hasResearchSlot;
        return this;
    }

    public GTRecipeTypeBuilder setMaxTooltips(int maxTooltips) {
        this.maxTooltips = maxTooltips;
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

    @Override
    public GTRecipeType createObject() {
        var type = GTRecipeTypes.register(this.id, this.category);
        type.maxInputs.putAll(maxInputs);
        type.maxOutputs.putAll(maxOutputs);
        if (this.layout != null) {
            var builder = new GTRecipeTypeUILayout.Builder(type);
            this.layout.accept(builder);
            type.setUiLayout(builder.build());
        }
        type.setSound(sound);
        type.setHasResearchSlot(hasResearchSlot);
        type.setMaxTooltips(maxTooltips);
        type.setSmallRecipeMap(smallRecipeMap);
        type.setIconSupplier(iconSupplier);
        return type;
    }
}

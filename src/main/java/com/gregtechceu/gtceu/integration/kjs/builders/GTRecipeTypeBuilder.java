package com.gregtechceu.gtceu.integration.kjs.builders;

import com.gregtechceu.gtceu.api.capability.recipe.*;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.gui.GTRecipeTypeUILayout;
import com.gregtechceu.gtceu.api.registry.registrate.BuilderBase;
import com.gregtechceu.gtceu.api.sound.SoundEntry;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Supplier;

@SuppressWarnings("unused")
public class GTRecipeTypeBuilder extends BuilderBase<GTRecipeType> {

    public transient String name, category;
    public transient final Object2IntMap<RecipeCapability<?>> maxInputs;
    public transient final Object2IntMap<RecipeCapability<?>> maxOutputs;
    @Nullable
    protected SoundEntry sound;
    protected boolean hasResearchSlot;
    protected int maxTooltips;

    private GTRecipeType smallRecipeMap;
    private Supplier<ItemStack> iconSupplier;
    private Consumer<GTRecipeTypeUILayout.Builder> layout;

    public GTRecipeTypeBuilder(ResourceLocation i) {
        super(i);
        name = i.getPath();
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

    public GTRecipeTypeBuilder ui(Consumer<GTRecipeTypeUILayout.Builder> builder) {
        this.layout = builder;
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
    public GTRecipeType register() {
        var type = GTRecipeTypes.register(name, category);
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
        return value = type;
    }
}

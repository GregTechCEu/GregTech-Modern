package com.gregtechceu.gtceu.api.item.module;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import lombok.Getter;

import java.util.function.BiFunction;

public abstract class TieredItemModule extends ItemModule implements ITieredItemModule {

    @Getter
    private final int tier;
    @Getter
    private TieredItemModule[] otherTierModules;

    public TieredItemModule(ResourceLocation id, int tier) {
        super(id);
        this.tier = tier;
    }

    @Override
    public boolean canApplyTo(ItemStack stack) {
        if (otherTierModules == null) return super.canApplyTo(stack);
        IModularItem modularItem = GTCapabilityHelper.getModularItem(stack);
        if (modularItem == null) return false;
        for (TieredItemModule module : otherTierModules) if (modularItem.getModule(module) != null) return false;
        return super.canApplyTo(stack);
    }

    public static TieredItemModule[] create(ResourceLocation id, int minTier, int maxTier,
                                            BiFunction<ResourceLocation, Integer, TieredItemModule> constructor) {
        TieredItemModule[] result = new TieredItemModule[maxTier - minTier + 1];
        for (int i = 0; i <= maxTier - minTier; i++) {
            ResourceLocation resourceLocation = id.withSuffix("_" + (i + minTier));
            result[i] = constructor.apply(resourceLocation, i + minTier);
            result[i].otherTierModules = result;
        }
        return result;
    }

    public static TieredItemModule[] create(ResourceLocation id,
                                            BiFunction<ResourceLocation, Integer, TieredItemModule> constructor) {
        return create(id, GTValues.ULV, GTValues.MAX, constructor);
    }
}

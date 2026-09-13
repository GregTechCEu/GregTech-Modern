package com.gregtechceu.gtceu.api.item.module;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.common.mui.drawable.BorderDrawable;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import brachy.modularui.api.drawable.IDrawable;
import lombok.Getter;

import java.util.function.BiFunction;
import java.util.function.Function;

public class TieredItemModuleSlot extends ItemModuleSlot {

    @Getter
    private final int tier;

    public TieredItemModuleSlot(int tier) {
        this.tier = tier;
    }

    @Override
    public boolean acceptsModule(ItemModule module) {
        return !(module instanceof ITieredItemModule tieredModule) || tieredModule.getTier() <= getTier();
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("metaarmor.tooltip.modifier_slot.tiered", GTValues.VNF[getTier()]);
    }

    public static TieredItemModuleSlot[] create(int minTier, int maxTier,
                                                Function<Integer, TieredItemModuleSlot> constructor) {
        TieredItemModuleSlot[] result = new TieredItemModuleSlot[maxTier - minTier + 1];
        for (int i = 0; i <= maxTier - minTier; i++) {
            result[i] = constructor.apply(i + minTier);
        }
        return result;
    }

    public static TieredItemModuleSlot[] create(Function<Integer, TieredItemModuleSlot> constructor) {
        return create(GTValues.ULV, GTValues.MAX, constructor);
    }

    @Override
    public IDrawable getSlotTexture() {
        return new BorderDrawable(0xFF000000 | GTValues.VCM[getTier()], 1);
    }
}

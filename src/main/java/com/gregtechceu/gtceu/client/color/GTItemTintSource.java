package com.gregtechceu.gtceu.client.color;

import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

public record GTItemTintSource(int tintIndex) implements ItemTintSource {

    public static final MapCodec<GTItemTintSource> MAP_CODEC = Codec.INT.optionalFieldOf("index", 0)
            .xmap(GTItemTintSource::new, GTItemTintSource::tintIndex);

    @Override
    public int calculate(ItemStack stack, ClientLevel level, LivingEntity entity) {
        return GTItemColors.getColor(stack, tintIndex);
    }

    @Override
    public MapCodec<GTItemTintSource> type() {
        return MAP_CODEC;
    }
}

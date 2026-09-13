package com.gregtechceu.gtceu.api.item.module;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;

import net.minecraft.world.item.ItemStack;

import com.mojang.datafixers.Products;
import com.mojang.datafixers.util.Function3;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;

public abstract class TieredItemModule extends ItemModule implements ITieredItemModule {

    // spotless:off
    protected static <M extends TieredItemModule> Products.P3<RecordCodecBuilder.Mu<M>, Boolean, ItemStack, Integer> tieredBaseCodec(RecordCodecBuilder.Instance<M> instance) {
        return instance.group(Codec.BOOL.fieldOf("enabled").forGetter(ItemModule::isEnabled),
                ItemStack.CODEC.fieldOf("module_item").forGetter(ItemModule::getModuleItem),
                Codec.INT.fieldOf("tier").forGetter(TieredItemModule::getTier));
    }

    protected static <M extends TieredItemModule> Codec<M> tieredSimpleCodec(Function3<Boolean, ItemStack, Integer, M> function) {
        return RecordCodecBuilder.create(instance -> tieredBaseCodec(instance).apply(instance, function));
    }
    //spotless:on

    @Getter
    private final int tier;

    public TieredItemModule(boolean isEnabled, ItemStack moduleItem, int tier) {
        super(isEnabled, moduleItem);
        this.tier = tier;
    }

    public TieredItemModule(ItemStack moduleItem, int tier) {
        super(moduleItem);
        this.tier = tier;
    }

    @Override
    public boolean canApplyTo(ItemStack stack) {
        IModularItem modularItem = GTCapabilityHelper.getModularItem(stack);
        if (modularItem == null ||
                modularItem.getModules().stream().anyMatch(i -> i.getClass().equals(this.getClass())))
            return false;
        return super.canApplyTo(stack);
    }
}

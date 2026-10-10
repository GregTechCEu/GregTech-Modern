package com.gregtechceu.gtceu.common.module;

import com.gregtechceu.gtceu.api.item.module.ModuleContext;
import com.gregtechceu.gtceu.api.item.module.TieredAttributeItemModule;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.common.ForgeMod;

import java.util.UUID;

public class BlockReachItemModule extends TieredAttributeItemModule {

    private static final UUID ADD_BLOCK_REACH_UUID = UUID.fromString("c5bd81ea-b3af-4cca-8866-f3e62f5f68f1");

    public BlockReachItemModule(ResourceLocation id, int tier) {
        super(id, tier);
    }

    @Override
    public Component getInfo() {
        return Component.translatable("module.gtceu.block_reach.description", getTier() / 2d);
    }

    @Override
    public Attribute getAttribute(ModuleContext moduleContext) {
        return ForgeMod.BLOCK_REACH.get();
    }

    @Override
    public double getMaxAttributeAmount() {
        return getTier() / 2d;
    }

    @Override
    public AttributeModifier getAttributeModifier(ModuleContext moduleContext) {
        return new AttributeModifier(ADD_BLOCK_REACH_UUID, "Block Reach Modifier", getMaxAttributeAmount(),
                AttributeModifier.Operation.ADDITION);
    }
}

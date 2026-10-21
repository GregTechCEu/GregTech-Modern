package com.gregtechceu.gtceu.common.module;

import com.gregtechceu.gtceu.api.item.module.*;
import com.gregtechceu.gtceu.api.item.module.ui.ItemModuleSettingsBuilder;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.value.sync.PanelSyncManager;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;

import java.util.Objects;

public class JumpBoostItemModule extends TieredItemModule implements IJumpBoostItemModule {

    public JumpBoostItemModule(ResourceLocation id, int tier) {
        super(id, tier);
    }

    @Override
    public MapCodec<? extends ModuleData> moduleDataCodec() {
        return JumpBoostModuleData.CODEC;
    }

    @Override
    public Class<? extends ModuleData> moduleDataClass() {
        return JumpBoostModuleData.class;
    }

    @Override
    public ModuleData defaultModuleData(int slot, ItemModule module, ItemStack moduleStack) {
        return new JumpBoostModuleData(slot, module, moduleStack, true, getMaxJumpBoost());
    }

    @Override
    public Component getInfo() {
        return Component.translatable("module.gtceu.jump_boost.description", getMaxJumpBoost());
    }

    public float getMaxJumpBoost() {
        return getTier();
    }

    @Override
    public float getJumpHeight(ModuleContext moduleContext) {
        float height = moduleContext.getData(JumpBoostModuleData.class).getJumpBoost();
        return Float.isFinite(height) ? Mth.clamp(height, 0, getMaxJumpBoost()) : 0;
    }

    public void setJumpBoost(ModuleContext moduleContext, float jumpBoost) {
        jumpBoost = Mth.clamp(jumpBoost, 0, getMaxJumpBoost());
        moduleContext.setData(moduleContext.getData(JumpBoostModuleData.class).withJumpBoost(jumpBoost));
    }

    @Override
    public ItemModuleSettingsBuilder getSettings(ModuleContext moduleContext, PanelSyncManager psm, int id) {
        return super.getSettings(moduleContext, psm, id)
                .num(Text.lang("gui.gtceu.item_module.jump_boost"),
                        () -> getJumpHeight(moduleContext),
                        x -> setJumpBoost(moduleContext, (float) x),
                        0, getMaxJumpBoost(),
                        value -> Component.translatable("gui.gtceu.item_module.jump_height_value",
                                "%.2f".formatted(value)).getString());
    }

    public static class JumpBoostModuleData extends ModuleData {

        // spotless:off
        public static final MapCodec<JumpBoostModuleData> CODEC = RecordCodecBuilder.mapCodec(instance -> baseCodec(instance).and(
                Codec.FLOAT.fieldOf("jump_boost").forGetter(JumpBoostModuleData::getJumpBoost)
        ).apply(instance, JumpBoostModuleData::new));
        //spotless:on

        @Getter
        private final float jumpBoost;

        public JumpBoostModuleData(int slot, ItemModule module, ItemStack moduleItem, boolean enabled,
                                   float jumpBoost) {
            super(slot, module, moduleItem, enabled);
            this.jumpBoost = jumpBoost;
        }

        @Override
        public ModuleData copy() {
            return new JumpBoostModuleData(slot, module, moduleItem.copy(), enabled, jumpBoost);
        }

        @Override
        public ModuleData withModuleItem(ItemStack moduleItem) {
            return new JumpBoostModuleData(slot, module, moduleItem, enabled, jumpBoost);
        }

        public JumpBoostModuleData withJumpBoost(float jumpBoost) {
            return new JumpBoostModuleData(slot, module, moduleItem, enabled, jumpBoost);
        }

        @Override
        public ModuleData withEnabled(boolean enabled) {
            return new JumpBoostModuleData(slot, module, moduleItem, enabled, jumpBoost);
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof JumpBoostModuleData other)) return false;
            return super.equals(obj) && jumpBoost == other.jumpBoost;
        }

        @Override
        public int hashCode() {
            return Objects.hash(slot, module, moduleItem, enabled, jumpBoost);
        }
    }
}

package com.gregtechceu.gtceu.common.module;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.item.module.*;
import com.gregtechceu.gtceu.api.item.module.ui.ItemModuleSettingsBuilder;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.value.sync.PanelSyncManager;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;

import java.util.List;
import java.util.Objects;

public class JumpBoostItemModule extends TieredItemModule implements IJumpBoostItemModule {

    public JumpBoostItemModule(ResourceLocation id, int tier) {
        super(id, tier);
    }

    @Override
    public Codec<? extends ModuleData> moduleDataCodec() {
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
        return Component.translatable("module.gtceu.jump.description", getTier() / 4f);
    }

    @Override
    public void appendHoverText(ModuleContext moduleContext, Level level, List<Component> tooltips,
                                TooltipFlag isAdvanced) {
        super.appendHoverText(moduleContext, level, tooltips, isAdvanced);
        tooltips.add(Component.translatable("module.gtceu.jump", GTValues.VNF[getTier()]));
    }

    public float getMaxJumpBoost() {
        return getTier() / 4f;
    }

    @Override
    public float getJumpBoost(ModuleContext moduleContext) {
        return moduleContext.getData(JumpBoostModuleData.class).getJumpBoost();
    }

    public void setJumpBoost(ModuleContext moduleContext, float jumpBoost) {
        moduleContext.setData(moduleContext.getData(JumpBoostModuleData.class).withJumpBoost(jumpBoost));
    }

    @Override
    public ItemModuleSettingsBuilder getSettings(ModuleContext moduleContext, PanelSyncManager psm, int id) {
        return super.getSettings(moduleContext, psm, id)
                .num(Text.lang("module.gtceu.gui.jump_boost"),
                        () -> getJumpBoost(moduleContext),
                        x -> setJumpBoost(moduleContext, (float) x),
                        0, getMaxJumpBoost());
    }

    public static class JumpBoostModuleData extends ModuleData {

        // spotless:off
        public static final Codec<JumpBoostModuleData> CODEC = RecordCodecBuilder.create(instance -> baseCodec(instance).and(
                Codec.FLOAT.fieldOf("jump_boost").forGetter(JumpBoostModuleData::getJumpBoost)
        ).apply(instance, JumpBoostModuleData::new));
        //spotless:on

        @Getter
        private float jumpBoost;

        public JumpBoostModuleData(int slot, ItemModule module, ItemStack moduleItem, boolean enabled,
                                   float jumpBoost) {
            super(slot, module, moduleItem, enabled);
            this.jumpBoost = jumpBoost;
        }

        public JumpBoostModuleData withJumpBoost(float jumpBoost) {
            return new JumpBoostModuleData(slot, module, moduleItem, enabled, jumpBoost);
        }

        @Override
        public ModuleData copy() {
            return new JumpBoostModuleData(slot, module, moduleItem.copy(), enabled, jumpBoost);
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

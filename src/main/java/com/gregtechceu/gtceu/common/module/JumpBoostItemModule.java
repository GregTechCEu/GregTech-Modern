package com.gregtechceu.gtceu.common.module;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.item.module.*;
import com.gregtechceu.gtceu.common.data.GTItemModules;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.value.sync.PanelSyncManager;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;

import java.util.List;

public class JumpBoostItemModule extends TieredItemModule implements IJumpBoostItemModule {

    // spotless:off
    public static final Codec<JumpBoostItemModule> CODEC = RecordCodecBuilder.create(instance -> tieredBaseCodec(instance).and(
            Codec.FLOAT.fieldOf("jump_boost").forGetter(JumpBoostItemModule::getJumpBoost)
    ).apply(instance, JumpBoostItemModule::new));
    //spotless:on

    @Getter
    private float jumpBoost = 0;

    public JumpBoostItemModule(boolean isEnabled, ItemStack attachItem, int tier, float jumpBoost) {
        super(isEnabled, attachItem, tier);
        this.jumpBoost = jumpBoost;
    }

    public JumpBoostItemModule(ItemStack attachItem, int tier) {
        super(attachItem, tier);
        this.jumpBoost = getMaxJumpBoost();
    }

    @Override
    public ItemModuleType<JumpBoostItemModule> type() {
        return GTItemModules.JUMP_BOOST[getTier()];
    }

    @Override
    public Component getInfo() {
        return Component.translatable("gtceu.module.jump", getTier() / 4f);
    }

    @Override
    public void appendHoverText(Level level, TooltipFlag isAdvanced, List<Component> tooltips) {
        super.appendHoverText(level, isAdvanced, tooltips);
        tooltips.add(Component.translatable("metaarmor.tooltip.modifier.jump", GTValues.VNF[getTier()]));
    }

    public float getMaxJumpBoost() {
        return getTier() / 4f;
    }

    public void setJumpBoost(float jumpBoost) {
        this.jumpBoost = jumpBoost;
        getModularItemStack().saveModuleData();
    }

    @Override
    public ItemModuleSettingsBuilder getSettings(PanelSyncManager psm, int id) {
        return super.getSettings(psm, id)
                .num(Text.lang("gtceu.module.gui.jump_boost"),
                        this::getJumpBoost,
                        x -> setJumpBoost((float) x),
                        0, getMaxJumpBoost());
    }
}

package com.gregtechceu.gtceu.common.module;

import brachy.modularui.api.drawable.IKey;
import brachy.modularui.value.sync.PanelSyncManager;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.item.module.AppliedItemModule;
import com.gregtechceu.gtceu.api.item.module.IJumpBoostItemModule;
import com.gregtechceu.gtceu.api.item.module.TieredItemModule;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class JumpBoostItemModule extends TieredItemModule implements IJumpBoostItemModule {
    private static final String JUMP_BOOST_KEY = "jump_boost";

    public JumpBoostItemModule(ResourceLocation id, int tier) {
        super(id, tier);
    }

    @Override
    public Component getInfo() {
        return Component.translatable("gtceu.module.jump", getTier() / 4f);
    }

    @Override
    public float getJumpBoost(AppliedItemModule module) {
        if (module.getTag().contains(JUMP_BOOST_KEY))
            return module.getTag().getFloat(JUMP_BOOST_KEY);
        return getMaxJumpBoost();
    }

    @Override
    public void appendHoverText(Level level, TooltipFlag isAdvanced, List<Component> tooltips,
                                AppliedItemModule module) {
        super.appendHoverText(level, isAdvanced, tooltips, module);
        tooltips.add(Component.translatable("metaarmor.tooltip.modifier.jump", GTValues.VNF[getTier()]));
    }

    public float getMaxJumpBoost() {
        return getTier() / 4f;
    }

    public void setJumpBoost(AppliedItemModule module, float jumpBoost) {
        module.getTag().putFloat(JUMP_BOOST_KEY, jumpBoost);
    }

    @Override
    public Settings getSettings(AppliedItemModule module, PanelSyncManager psm, int id) {
        return super.getSettings(module, psm, id)
                .num(IKey.lang("gtceu.module.gui.jump_boost"),
                        () -> getJumpBoost(module),
                        x -> setJumpBoost(module, (float) x),
                        0, getMaxJumpBoost());
    }
}

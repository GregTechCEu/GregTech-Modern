package com.gregtechceu.gtceu.common.module;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.item.module.IJumpBoostItemModule;
import com.gregtechceu.gtceu.api.item.module.ModuleContext;
import com.gregtechceu.gtceu.api.item.module.TieredItemModule;
import com.gregtechceu.gtceu.api.item.module.ui.ItemModuleSettingsBuilder;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.value.sync.PanelSyncManager;
import org.jetbrains.annotations.UnknownNullability;

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
    public float getJumpBoost(ModuleContext moduleContext) {
        if (moduleContext.getData().getTag().contains(JUMP_BOOST_KEY))
            return moduleContext.getData().getTag().getFloat(JUMP_BOOST_KEY);
        return getMaxJumpBoost();
    }

    @Override
    public void appendHoverText(ModuleContext moduleContext, Level level, TooltipFlag isAdvanced, List<Component> tooltips) {
        super.appendHoverText(moduleContext, level, isAdvanced, tooltips);
        tooltips.add(Component.translatable("metaarmor.tooltip.modifier.jump", GTValues.VNF[getTier()]));
    }

    public float getMaxJumpBoost() {
        return getTier() / 4f;
    }

    public void setJumpBoost(ModuleContext moduleContext, float jumpBoost) {
        moduleContext.getData().getTag().putFloat(JUMP_BOOST_KEY, jumpBoost);
    }

    @Override
    public ItemModuleSettingsBuilder getSettings(ModuleContext moduleContext, PanelSyncManager psm, int id) {
        return super.getSettings(moduleContext, psm, id)
                .num(Text.lang("gtceu.module.gui.jump_boost"),
                        () -> getJumpBoost(moduleContext),
                        x -> setJumpBoost(moduleContext, (float) x),
                        0, getMaxJumpBoost());
    }
}

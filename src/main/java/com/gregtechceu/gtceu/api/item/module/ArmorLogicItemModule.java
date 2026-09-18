package com.gregtechceu.gtceu.api.item.module;

import com.gregtechceu.gtceu.api.item.armor.IArmorLogic;
import com.gregtechceu.gtceu.api.item.component.IItemHUDProvider;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import org.jetbrains.annotations.Nullable;

public abstract class ArmorLogicItemModule extends ItemModule implements IHUDProviderItemModule {

    public ArmorLogicItemModule(ResourceLocation id) {
        super(id);
    }

    protected abstract @Nullable IArmorLogic getArmorLogic(ModuleContext moduleContext);

    @Override
    public void onArmorTick(ModuleContext moduleContext, LivingEntity entity) {
        super.onArmorTick(moduleContext, entity);
        IArmorLogic armorLogic = getArmorLogic(moduleContext);
        if (armorLogic == null) return;
        if (entity instanceof Player player)
            armorLogic.onArmorTick(player.level(), player, moduleContext.getAppliedTo());
    }

    @Override
    public void onEquip(ModuleContext moduleContext, LivingEntity entity) {
        super.onEquip(moduleContext, entity);
        IArmorLogic armorLogic = getArmorLogic(moduleContext);
        if (armorLogic == null) return;
        if (entity instanceof Player player) armorLogic.onEquip(player);
    }

    @Override
    public void onUnequip(ModuleContext moduleContext, LivingEntity entity) {
        super.onUnequip(moduleContext, entity);
        IArmorLogic armorLogic = getArmorLogic(moduleContext);
        if (armorLogic == null) return;
        if (entity instanceof Player player) armorLogic.onUnequip(player);
    }

    @Override
    public boolean isPPE(ModuleContext moduleContext) {
        IArmorLogic armorLogic = getArmorLogic(moduleContext);
        if (armorLogic == null) return super.isPPE(moduleContext);
        return armorLogic.isPPE(moduleContext.getAppliedTo()) && super.isEnabled(moduleContext);
    }

    @Override
    public boolean shouldDrawHUD(ModuleContext moduleContext) {
        return getArmorLogic(moduleContext) instanceof IItemHUDProvider;
    }

    @Override
    public void drawHUD(ModuleContext moduleContext, GuiGraphics graphics) {
        if (getArmorLogic(moduleContext) instanceof IItemHUDProvider provider) {
            IItemHUDProvider.tryDrawHud(provider, moduleContext.getAppliedTo(), graphics);
        }
    }
}

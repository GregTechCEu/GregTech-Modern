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

    protected abstract @Nullable IArmorLogic getArmorLogic(ModuleData module);

    @Override
    public void onArmorTick(LivingEntity entity, ModuleData module) {
        super.onArmorTick(entity, module);
        IArmorLogic armorLogic = getArmorLogic(module);
        if (armorLogic == null) return;
        if (entity instanceof Player player) armorLogic.onArmorTick(player.level(), player, module.getAppliedTo());
    }

    @Override
    public void onEquip(LivingEntity entity, ModuleData module) {
        super.onEquip(entity, module);
        IArmorLogic armorLogic = getArmorLogic(module);
        if (armorLogic == null) return;
        if (entity instanceof Player player) armorLogic.onEquip(player);
    }

    @Override
    public void onUnequip(LivingEntity entity, ModuleData module) {
        super.onUnequip(entity, module);
        IArmorLogic armorLogic = getArmorLogic(module);
        if (armorLogic == null) return;
        if (entity instanceof Player player) armorLogic.onUnequip(player);
    }

    @Override
    public boolean isPPE(ModuleData module) {
        IArmorLogic armorLogic = getArmorLogic(module);
        if (armorLogic == null) return super.isPPE(module);
        return armorLogic.isPPE(module.getAppliedTo()) && super.isEnabled(module);
    }

    @Override
    public boolean shouldDrawHUD(ModuleData module) {
        return getArmorLogic(module) instanceof IItemHUDProvider;
    }

    @Override
    public void drawHUD(ModuleData module, GuiGraphics graphics) {
        if (getArmorLogic(module) instanceof IItemHUDProvider provider) {
            IItemHUDProvider.tryDrawHud(provider, module.getAppliedTo(), graphics);
        }
    }
}

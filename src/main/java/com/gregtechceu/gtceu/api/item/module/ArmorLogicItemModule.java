package com.gregtechceu.gtceu.api.item.module;

import com.gregtechceu.gtceu.api.item.armor.IArmorLogic;
import com.gregtechceu.gtceu.api.item.component.IItemHUDProvider;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;

public abstract class ArmorLogicItemModule extends ItemModule implements IHUDProviderItemModule {

    public ArmorLogicItemModule(boolean isEnabled, ItemStack moduleItem) {
        super(isEnabled, moduleItem);
    }

    public ArmorLogicItemModule(ItemStack moduleItem) {
        super(moduleItem);
    }

    protected abstract @Nullable IArmorLogic getArmorLogic();

    @Override
    public void onArmorTick(LivingEntity entity) {
        super.onArmorTick(entity);
        IArmorLogic armorLogic = getArmorLogic();
        if (armorLogic == null) return;
        if (entity instanceof Player player) armorLogic.onArmorTick(player.level(), player, getAppliedTo());
    }

    @Override
    public void onEquip(LivingEntity entity) {
        super.onEquip(entity);
        IArmorLogic armorLogic = getArmorLogic();
        if (armorLogic == null) return;
        if (entity instanceof Player player) armorLogic.onEquip(player);
    }

    @Override
    public void onUnequip(LivingEntity entity) {
        super.onUnequip(entity);
        IArmorLogic armorLogic = getArmorLogic();
        if (armorLogic == null) return;
        if (entity instanceof Player player) armorLogic.onUnequip(player);
    }

    @Override
    public boolean isPPE() {
        IArmorLogic armorLogic = getArmorLogic();
        if (armorLogic == null) return super.isPPE();
        return armorLogic.isPPE(getAppliedTo());
    }

    @Override
    public boolean shouldDrawHUD() {
        return getArmorLogic() instanceof IItemHUDProvider;
    }

    @Override
    public void drawHUD(GuiGraphics graphics) {
        if (getArmorLogic() instanceof IItemHUDProvider provider) {
            IItemHUDProvider.tryDrawHud(provider, getAppliedTo(), graphics);
        }
    }
}

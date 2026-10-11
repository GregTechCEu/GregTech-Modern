package com.gregtechceu.gtceu.common.item.armor;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.api.item.armor.ArmorComponentItem;
import com.gregtechceu.gtceu.api.item.armor.ArmorUtils;
import com.gregtechceu.gtceu.api.item.jetpack.AbstractJetpackLogic;
import com.gregtechceu.gtceu.api.item.jetpack.ElectricJetpackLogic;
import com.gregtechceu.gtceu.api.item.jetpack.IJetpackLogic;
import com.gregtechceu.gtceu.api.item.jetpack.IJetpackLogic.JetpackStats;
import com.gregtechceu.gtceu.core.IFireImmuneEntity;
import com.gregtechceu.gtceu.utils.input.SyncedKeyMappings;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.ints.IntList;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.List;

public class AdvancedQuarkTechSuite extends QuarkTechSuite {

    // A replacement for checking the current world time, to get around the gamerule that stops it
    private long timer = 0L;
    private List<Pair<NonNullList<ItemStack>, IntList>> inventoryIndexMap;
    private final AbstractJetpackLogic jetpackLogic;

    public AdvancedQuarkTechSuite(int energyPerUse, long capacity, int tier) {
        super(ArmorItem.Type.CHESTPLATE, energyPerUse, capacity, tier);
        JetpackStats stats = new JetpackStats(0.065, 0.9, 0.15, 0.15, 0.012, 4.39, true,
                getEnergyPerUse(), Mth.ceil(getEnergyPerUse() * 6.0), null);
        JetpackStats hoverStats = new JetpackStats(0.065, 0.18, 0.15, 0.4, 0.012, 4.39, true,
                getEnergyPerUse(), Mth.ceil(getEnergyPerUse() * 6.0), null);
        this.jetpackLogic = new ElectricJetpackLogic(stats, hoverStats);
    }

    @Override
    public void onArmorTick(Level world, Player player, ItemStack item) {
        IElectricItem cont = GTCapabilityHelper.getElectricItem(item);
        if (cont == null) {
            return;
        }

        CompoundTag data = item.getOrCreateTag();
        byte toggleTimer = data.getByte("toggleTimer");
        boolean canShare = data.getBoolean("canShare");

        String messageKey = null;
        if (toggleTimer == 0) {
            if (SyncedKeyMappings.ARMOR_CHARGING.isKeyDown(player)) {
                canShare = !canShare;
                if (canShare && cont.getCharge() == 0) { // Only allow for charging to be enabled if charge is nonzero
                    messageKey = "metaarmor.qts.share.error";
                    canShare = false;
                } else {
                    messageKey = "metaarmor.qts.share." + (canShare ? "enable" : "disable");
                }
                data.putBoolean("canShare", canShare);
            }

            if (messageKey != null) {
                toggleTimer = 5;
                if (!world.isClientSide) player.displayClientMessage(Component.translatable(messageKey), true);
            }
        }

        if (toggleTimer > 0) toggleTimer--;
        data.putByte("toggleTimer", toggleTimer);

        if (type == ArmorItem.Type.CHESTPLATE && !player.fireImmune()) {
            ((IFireImmuneEntity) player).gtceu$setFireImmune(true);
            if (player.isOnFire()) player.extinguishFire();
        }

        // Charging mechanics
        if (canShare && !world.isClientSide) {
            // Check for new things to charge every 5 seconds
            if (timer % 100 == 0)
                inventoryIndexMap = ArmorUtils.getChargeableItem(player, cont.getTier());

            if (inventoryIndexMap != null && !inventoryIndexMap.isEmpty()) {
                // Charge all inventory slots
                for (int i = 0; i < inventoryIndexMap.size(); i++) {
                    Pair<NonNullList<ItemStack>, IntList> inventoryMap = inventoryIndexMap.get(i);
                    var inventoryIterator = inventoryMap.getSecond().iterator();
                    while (inventoryIterator.hasNext()) {
                        int slot = inventoryIterator.nextInt();
                        IElectricItem chargable = GTCapabilityHelper.getElectricItem(inventoryMap.getFirst().get(slot));

                        // Safety check the null, it should not actually happen. Also don't try and charge itself
                        if (chargable == null || chargable == cont) {
                            inventoryIterator.remove();
                            continue;
                        }

                        long attemptedChargeAmount = chargable.getTransferLimit() * 10;

                        // Accounts for tick differences when charging items
                        if (chargable.getCharge() < chargable.getMaxCharge() && cont.canUse(attemptedChargeAmount) &&
                                timer % 10 == 0) {
                            long delta = chargable.charge(attemptedChargeAmount, cont.getTier(), true, false);
                            if (delta > 0) {
                                cont.discharge(delta, cont.getTier(), true, false, false);
                            }
                            if (chargable.getCharge() == chargable.getMaxCharge()) {
                                inventoryIterator.remove();
                            }
                            player.inventoryMenu.sendAllDataToRemote();
                        }
                    }

                    if (inventoryMap.getSecond().isEmpty())
                        inventoryIndexMap.remove(inventoryMap);
                }
            }
        }

        timer++;
        if (timer == Long.MAX_VALUE)
            timer = 0;
    }

    @Override
    public void addInfo(ItemStack itemStack, List<Component> lines) {
        super.addInfo(itemStack, lines);
        CompoundTag data = itemStack.getOrCreateTag();
        Component state;
        boolean canShare = data.contains("canShare") && data.getBoolean("canShare");
        state = canShare ? Component.translatable("metaarmor.hud.status.enabled") :
                Component.translatable("metaarmor.hud.status.disabled");
        lines.add(Component.translatable("metaarmor.energy_share.tooltip", state));
        lines.add(Component.translatable("metaarmor.energy_share.tooltip.guide"));
        jetpackLogic.addInformation(itemStack, lines);
    }

    @Override
    public InteractionResultHolder<ItemStack> onRightClick(Level world, Player player, InteractionHand hand) {
        ItemStack armor = player.getItemInHand(hand);
        if (armor.getItem() instanceof ArmorComponentItem && player.isShiftKeyDown()) {
            CompoundTag data = armor.getOrCreateTag();
            boolean canShare = data.contains("canShare") && data.getBoolean("canShare");
            IElectricItem cont = GTCapabilityHelper.getElectricItem(armor);
            if (cont == null) {
                return InteractionResultHolder.fail(armor);
            }

            canShare = !canShare;
            if (!world.isClientSide) {
                if (canShare && cont.getCharge() == 0) {
                    player.sendSystemMessage(Component.translatable("metaarmor.energy_share.error"));
                } else if (canShare) {
                    player.sendSystemMessage(Component.translatable("metaarmor.energy_share.enable"));
                } else {
                    player.sendSystemMessage(Component.translatable("metaarmor.energy_share.disable"));
                }
            }

            canShare = canShare && (cont.getCharge() != 0);
            data.putBoolean("canShare", canShare);
            return InteractionResultHolder.success(armor);
        } else {
            return super.onRightClick(world, player, hand);
        }
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void drawHUD(ItemStack item, GuiGraphics guiGraphics, MutableInt drawOffsetY) {
        addCapacityHUD(item, this.HUD);
        IElectricItem cont = GTCapabilityHelper.getElectricItem(item);
        if (cont != null && cont.canUse(energyPerUse)) {
            jetpackLogic.addHUDLines(item, this.HUD);
            CompoundTag data = item.getTag();
            if (data != null && data.contains("canShare")) {
                String status = data.getBoolean("canShare") ? "metaarmor.hud.status.enabled" :
                        "metaarmor.hud.status.disabled";
                this.HUD.newString(Component.translatable("mataarmor.hud.supply_mode", Component.translatable(status)));
            }
        }
        this.HUD.draw(guiGraphics, drawOffsetY);
        this.HUD.reset();
    }

    /*
     * @Override
     * public ArmorProperties getProperties(EntityLivingBase player, @NotNull ItemStack armor, DamageSource source,
     * double damage, EntityEquipmentSlot equipmentSlot) {
     * int damageLimit = Integer.MAX_VALUE;
     * IElectricItem item = armor.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
     * if (item == null) {
     * return new ArmorProperties(0, 0, damageLimit);
     * }
     * if (energyPerUse > 0) {
     * damageLimit = (int) Math.min(damageLimit, 25.0D * item.getCharge() / (energyPerUse * 250.0D));
     * }
     * return new ArmorProperties(8, getDamageAbsorption() * getAbsorption(armor), damageLimit);
     * }
     *
     * @Override
     * public boolean handleUnblockableDamage(EntityLivingBase entity, @NotNull ItemStack armor, DamageSource source,
     * double damage, EntityEquipmentSlot equipmentSlot) {
     * return source != DamageSource.FALL && source != DamageSource.DROWN && source != DamageSource.STARVE;
     * }
     */

    @Override
    public ResourceLocation getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        return GTCEu.id("textures/armor/advanced_quark_tech_suite_1.png");
    }

    @Override
    public double getDamageAbsorption() {
        return 1.5D;
    }

    @Override
    public IJetpackLogic getJetpackLogic(ItemStack stack) {
        return jetpackLogic;
    }
}

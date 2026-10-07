package com.gregtechceu.gtceu.common.machine.electric;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.compat.FeCompat;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.TieredEnergyMachine;
import com.gregtechceu.gtceu.api.machine.trait.notifiable.NotifiableEnergyContainer;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.utils.ExtendedUseOnContext;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

import top.theillusivec4.curios.api.CuriosApi;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class WirelessChargerMachine extends TieredEnergyMachine {

    @SaveField
    @SyncToClient
    private boolean turbo = true;

    private TickableSubscription chargeSubscription;
    private Set<UUID> previousPlayers = new HashSet<>();

    public WirelessChargerMachine(BlockEntityCreationInfo info, int tier) {
        super(info, tier, new WirelessChargerEnergyContainer(tier));
        ((WirelessChargerEnergyContainer) energyContainer).machine = this;
    }

    public static int getRange(int tier, boolean turbo) {
        return (turbo ? 32 : 64) << (tier - GTValues.LV);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!isRemote()) chargeSubscription = subscribeServerTick(this::chargePlayers);
    }

    @Override
    public void onUnload() {
        if (chargeSubscription != null) {
            chargeSubscription.unsubscribe();
            chargeSubscription = null;
        }
        previousPlayers.clear();
        super.onUnload();
    }

    private void chargePlayers() {
        if (getOffsetTimer() % (turbo ? 4 : 20) != 0) return;
        if (energyContainer.getEnergyStored() <= 0) {
            previousPlayers.clear();
            return;
        }
        var owner = getOwner();
        var level = getLevel();
        if (owner == null || level == null || level.getServer() == null) return;

        int range = getRange(getTier(), turbo);
        var bounds = new AABB(getBlockPos()).inflate(range);
        Set<UUID> players = new HashSet<>();
        Set<UUID> members = new HashSet<>(owner.getMembers());
        members.add(owner.getPlayerUUID());
        long chargeAmount = GTValues.V[getTier()] * (turbo ? 4 : 1);
        for (UUID member : members) {
            Player player = level.getPlayerByUUID(id);
            if (player == null !bounds.intersects(player.getBoundingBox())) continue;
            players.add(member);
            if (!previousPlayers.contains(member)) {
                player.displayClientMessage(Component.translatable("gtceu.machine.wireless_charger.enter_range"), true);
            }
            chargeInventory(player, chargeAmount);
        }
        for (var id : previousPlayers) {
            if (players.contains(id)) continue;
            var player = level.getServer().getPlayerList().getPlayer(id);
            if (player != null) {
                player.displayClientMessage(Component.translatable("gtceu.machine.wireless_charger.left_range"), true);
            }
        }
        previousPlayers = players;
    }

    private void chargeInventory(Player player, long chargeAmount) {
        if (energyContainer.getEnergyStored() <= 0) return;
        if (GTCEu.Mods.isCuriosLoaded()) {
            CuriosApi.getCuriosInventory(player).ifPresent(inventory -> {
                var curios = inventory.getEquippedCurios();
                for (int slot = 0; slot < curios.getSlots(); slot++) {
                    chargeItem(curios.getStackInSlot(slot), chargeAmount);
                }
            });
        }
        var inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            chargeItem(inventory.getItem(slot), chargeAmount);
        }
    }

    private void chargeItem(ItemStack stack, long chargeAmount) {
        long available = Math.min(chargeAmount, energyContainer.getEnergyStored());
        if (stack.isEmpty() || available <= 0) return;
        var electricItem = GTCapabilityHelper.getElectricItem(stack);
        if (electricItem != null) {
            if (electricItem.chargeable()) {
                energyContainer.changeEnergy(-electricItem.charge(available, getTier(), true, false));
            }
            return;
        }
        if (!ConfigHolder.INSTANCE.compat.energy.nativeEUToFE) return;
        var forgeEnergyItem = GTCapabilityHelper.getForgeEnergyItem(stack);
        if (forgeEnergyItem != null && forgeEnergyItem.canReceive()) {
            energyContainer.changeEnergy(-FeCompat.insertEu(forgeEnergyItem, available, false));
        }
    }

    @Override
    protected InteractionResult onScrewdriverClick(ExtendedUseOnContext context) {
        if (!isRemote()) {
            turbo = !turbo;
            getSyncDataHolder().markClientSyncFieldDirty("turbo");
            setChanged();
            if (context.getPlayer() != null) {
                context.getPlayer().displayClientMessage(Component.translatable(
                        turbo ? "gtceu.machine.wireless_charger.mode.turbo" :
                                "gtceu.machine.wireless_charger.mode.standard",
                        FormattingUtil.formatNumbers(getRange(getTier(), turbo))), false);
            }
        }
        return InteractionResult.CONSUME;
    }

    private static final class WirelessChargerEnergyContainer extends NotifiableEnergyContainer {

        private WirelessChargerMachine machine;

        private WirelessChargerEnergyContainer(int tier) {
            super(GTValues.V[tier] * 64L, GTValues.V[tier], 4, 0L, 0L);
        }

        @Override
        public long getInputAmperage() {
            return machine != null && machine.turbo ? 4 : 1;
        }
    }
}

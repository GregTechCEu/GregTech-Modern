package com.gregtechceu.gtceu.common.module;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.api.item.module.*;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.common.data.GTItemModules;
import com.gregtechceu.gtceu.common.machine.electric.BatteryBufferMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.PowerSubstationMachine;
import com.gregtechceu.gtceu.common.machine.owner.PlayerOwner;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class AutoChargeItemModule extends TieredItemModule {

    public AutoChargeItemModule(ResourceLocation id, int tier) {
        super(id, tier);
    }

    @Override
    public MapCodec<? extends ModuleData> moduleDataCodec() {
        return AutoChargeModuleData.CODEC;
    }

    @Override
    public Class<? extends ModuleData> moduleDataClass() {
        return AutoChargeModuleData.class;
    }

    @Override
    public ModuleData defaultModuleData(int slot, ItemModule module, ItemStack moduleStack) {
        return new AutoChargeModuleData(slot, module, moduleStack);
    }

    @Override
    public Component getInfo() {
        if (getTier() < GTValues.IV)
            return Component.translatable("gtceu.module.wireless_charging", getRange(), GTValues.VNF[getTier()]);
        else return Component.translatable("gtceu.module.wireless_charging.interdimensional", getRange(),
                GTValues.VNF[getTier()], GTValues.VNF[GTValues.IV]);
    }

    @Override
    public void onInventoryTick(ModuleContext moduleContext, Player player) {
        super.onInventoryTick(moduleContext, player);
        long energy = getEnergyToTransfer(player, moduleContext);
        MetaMachine machine = getLinkedMachine(player.getServer(), moduleContext);
        IElectricItem electricItem = GTCapabilityHelper.getElectricItem(moduleContext.getAppliedTo());
        if (electricItem == null) return;
        if (energy > 0) {
            if (machine instanceof PowerSubstationMachine substation) {
                electricItem.charge(substation.getEnergyBank().drain(energy), electricItem.getTier(), false, false);
            } else if (machine instanceof BatteryBufferMachine charger) {
                electricItem.charge(charger.energyContainer.removeEnergy(energy), charger.getTier(), false, false);
            }
        }
    }

    private long getEnergyToTransfer(Player player, ModuleContext moduleContext) {
        IElectricItem electricItem = GTCapabilityHelper.getElectricItem(moduleContext.getAppliedTo());
        if (electricItem == null) return 0;
        long energy = Math.min(electricItem.getMaxCharge() - electricItem.getCharge(), electricItem.getTransferLimit());
        if (energy <= 0) return 0;
        MetaMachine machine = getLinkedMachine(player.getServer(), moduleContext);
        if (machine == null) return 0;
        int interdimensionalTier = -1;
        @Nullable
        Holder<ItemModule>[] damageBlock = GTItemModules.DAMAGE_BLOCK;
        for (int i = 0; i < damageBlock.length; i++) {
            var module = damageBlock[i];
            if (module == null) continue;
            ItemModule shieldModule = module.value();
            IModularItem modularItem = GTCapabilityHelper.getModularItem(moduleContext.getAppliedTo());
            if (modularItem != null && modularItem.getModuleContext(shieldModule) != null)
                interdimensionalTier = i + 1;
        }
        interdimensionalTier = Math.min(interdimensionalTier, getTier());
        if (machine.getLevel() != player.level() && interdimensionalTier < GTValues.IV) return 0;
        if (interdimensionalTier < GTValues.IV && machine.getBlockPos().distSqr(player.blockPosition()) > getRange())
            return 0;
        return Math.min(energy, GTValues.V[machine.getLevel() == player.level() ? getTier() : interdimensionalTier]);
    }

    private @Nullable MetaMachine getLinkedMachine(MinecraftServer server, ModuleContext moduleContext) {
        var linkedPos = moduleContext.getData(AutoChargeModuleData.class).getLinkedPos();
        if (linkedPos == null) return null;
        Level level = server.getLevel(linkedPos.dimension());
        if (level == null) return null;
        return MetaMachine.getMachine(level, linkedPos.pos());
    }

    @Override
    public InteractionResult onItemUseFirst(ModuleContext moduleContext, UseOnContext context) {
        BlockPos pos = context.getClickedPos();
        if (context.getLevel().isClientSide()) return super.onItemUseFirst(moduleContext, context);

        MetaMachine machine = MetaMachine.getMachine(context.getLevel(), pos);
        if (machine instanceof PowerSubstationMachine || machine instanceof BatteryBufferMachine) {
            PlayerOwner owner = machine.getPlayerOwner();
            Player player = context.getPlayer();
            if (owner == null || player != null && owner.isPlayerFriendly(player.getUUID())) {
                moduleContext.setData(moduleContext.getData(AutoChargeModuleData.class)
                        .withLinkedPos(GlobalPos.of(context.getLevel().dimension(), pos)));
                if (player != null) player.sendSystemMessage(Component.translatable("behaviour.charger_linked"));
            }
        }
        return super.onItemUseFirst(moduleContext, context);
    }

    private double getRange() {
        return (8 << getTier());
    }

    @Override
    public void appendHoverText(ModuleContext moduleContext, Item.TooltipContext context, TooltipFlag isAdvanced,
                                List<Component> tooltips) {
        super.appendHoverText(moduleContext, context, isAdvanced, tooltips);
        tooltips.add(Component.translatable("metaarmor.tooltip.modifier.wireless_charging", GTValues.VNF[getTier()]));
    }

    public static class AutoChargeModuleData extends ModuleData {

        // spotless:off
        public static final MapCodec<AutoChargeModuleData> CODEC = RecordCodecBuilder.mapCodec(instance -> baseCodec(instance).and(
                GlobalPos.CODEC.optionalFieldOf("linked_pos").forGetter(AutoChargeModuleData::getLinkedPosOptional)
        ).apply(instance, AutoChargeModuleData::new));
        //spotless:on

        @Getter
        private final @Nullable GlobalPos linkedPos;

        @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
        public AutoChargeModuleData(int slot, ItemModule module, ItemStack moduleItem, boolean enabled,
                                    Optional<GlobalPos> globalPos) {
            super(slot, module, moduleItem, enabled);
            this.linkedPos = globalPos.orElse(null);
        }

        public AutoChargeModuleData(int slot, ItemModule module, ItemStack moduleItem, boolean enabled,
                                    @Nullable GlobalPos globalPos) {
            super(slot, module, moduleItem, enabled);
            this.linkedPos = globalPos;
        }

        public AutoChargeModuleData(int slot, ItemModule module, ItemStack moduleItem) {
            super(slot, module, moduleItem, true);
            this.linkedPos = null;
        }

        public Optional<GlobalPos> getLinkedPosOptional() {
            return Optional.ofNullable(linkedPos);
        }

        @Override
        public ModuleData withModuleItem(ItemStack moduleItem) {
            return new AutoChargeModuleData(slot, module, moduleItem, enabled, linkedPos);
        }

        @Override
        public ModuleData withEnabled(boolean enabled) {
            return new AutoChargeModuleData(slot, module, moduleItem, enabled, linkedPos);
        }

        public ModuleData withLinkedPos(GlobalPos linkedPos) {
            return new AutoChargeModuleData(slot, module, moduleItem, enabled, linkedPos);
        }

        @Override
        public ModuleData copy() {
            return new AutoChargeModuleData(slot, module, moduleItem.copy(), enabled, linkedPos);
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof AutoChargeModuleData other)) return false;
            return super.equals(obj) && Objects.equals(linkedPos, other.linkedPos);
        }

        @Override
        public int hashCode() {
            return Objects.hash(slot, module, moduleItem, enabled, linkedPos);
        }
    }
}

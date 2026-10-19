package com.gregtechceu.gtceu.common.module;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.api.capability.compat.FeCompat;
import com.gregtechceu.gtceu.api.item.module.*;
import com.gregtechceu.gtceu.api.item.module.ui.ItemModuleSettingsBuilder;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.common.machine.electric.BatteryBufferMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.PowerSubstationMachine;
import com.gregtechceu.gtceu.common.machine.owner.PlayerOwner;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.IItemHandler;

import brachy.modularui.api.IPanelHandler;
import brachy.modularui.api.drawable.Text;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.value.sync.IntSyncValue;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widgets.ButtonWidget;
import brachy.modularui.widgets.Dialog;
import brachy.modularui.widgets.ListWidget;
import brachy.modularui.widgets.layout.Flow;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;

public class AutoChargeItemModule extends TieredItemModule {

    public AutoChargeItemModule(ResourceLocation id, int tier) {
        super(id, tier);
    }

    @Override
    public Codec<? extends ModuleData> moduleDataCodec() {
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
        return Component.translatable(getDescriptionLanguageKey(), GTValues.VNF[getTier()]);
    }

    @Override
    public void onInventoryTick(ModuleContext moduleContext, Player player) {
        super.onInventoryTick(moduleContext, player);
        if (player.level().isClientSide || !isEnabled(moduleContext)) return;
        MetaMachine machine = getLinkedMachine(player.getServer(), moduleContext);
        IElectricItem electricItem = GTCapabilityHelper.getElectricItem(moduleContext.getAppliedTo());
        if (electricItem == null) return;
        AutoChargeModuleData data = moduleContext.getData(AutoChargeModuleData.class);
        long budget = GTValues.V[data.getVoltageTier()] * (long) data.getAmperage();
        int chargerTier = machine instanceof BatteryBufferMachine buffer ?
                Math.min(buffer.getTier(), data.getVoltageTier()) : data.getVoltageTier();
        long energy = electricItem.charge(budget, chargerTier, true, true);
        if (energy > 0) {
            if (machine instanceof PowerSubstationMachine substation && substation.isFormed()) {
                electricItem.charge(substation.getEnergyBank().drain(energy), chargerTier, true, false);
            } else if (machine instanceof BatteryBufferMachine charger) {
                electricItem.charge(charger.energyContainer.removeEnergy(energy), chargerTier, true, false);
            }
        }
        chargeCarriedItems(player, moduleContext.getAppliedTo(), electricItem, budget, data.getVoltageTier());
    }

    private static void chargeCarriedItems(Player player, ItemStack sourceStack, IElectricItem source, long budget,
                                           int voltageTier) {
        long remaining = Math.min(budget, source.getCharge());
        if (remaining <= 0) return;
        record Candidate(ItemStack stack, long demand) {}
        var stacks = new ArrayList<ItemStack>();
        if (GTCEu.Mods.isCuriosLoaded()) {
            IItemHandler curios = CuriosApi.getCuriosInventory(player)
                    .<IItemHandler>map(ICuriosItemHandler::getEquippedCurios).orElse(null);
            if (curios != null) {
                for (int i = 0; i < curios.getSlots(); i++) stacks.add(curios.getStackInSlot(i));
            }
        }
        var inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) stacks.add(inventory.getItem(i));
        var candidates = new ArrayList<Candidate>();
        for (ItemStack stack : stacks) {
            long demand = chargeCarriedItem(remaining, sourceStack, source, stack, voltageTier, true);
            if (demand > 0) candidates.add(new Candidate(stack, demand));
        }
        if (candidates.isEmpty()) return;
        // Evenly charge all items that can be charged.
        Collections.rotate(candidates, Math.floorMod(player.tickCount, candidates.size()));
        candidates.sort(Comparator.comparingLong(Candidate::demand));
        for (int i = 0; i < candidates.size() && remaining > 0; i++) {
            Candidate candidate = candidates.get(i);
            long share = Math.min(candidate.demand(), remaining / (candidates.size() - i));
            if (share > 0) {
                remaining -= chargeCarriedItem(share, sourceStack, source, candidate.stack(), voltageTier, false);
            }
        }
    }

    private static long chargeCarriedItem(long remaining, ItemStack sourceStack, IElectricItem source,
                                          ItemStack target, int voltageTier, boolean simulate) {
        if (target == sourceStack) return 0;
        IElectricItem targetEnergy = GTCapabilityHelper.getElectricItem(target);
        if (targetEnergy != null && !targetEnergy.canProvideChargeExternally()) {
            long available = source.discharge(remaining, source.getTier(), true, false, true);
            long accepted = targetEnergy.charge(available, Math.min(voltageTier, source.getTier()), true, true);
            if (simulate) return accepted;
            long extracted = source.discharge(accepted, source.getTier(), true, false, false);
            long received = targetEnergy.charge(extracted, Math.min(voltageTier, source.getTier()), true, false);
            if (received < extracted) source.charge(extracted - received, source.getTier(), true, false);
            return received;
        }
        if (ConfigHolder.INSTANCE.compat.energy.nativeEUToFE) {
            var targetFE = GTCapabilityHelper.getForgeEnergyItem(target);
            if (targetFE != null && targetFE.canReceive()) {
                long available = source.discharge(remaining, source.getTier(), true, false, true);
                long accepted = FeCompat.insertEu(targetFE, available, true);
                if (simulate) return accepted;
                long extracted = source.discharge(accepted, source.getTier(), true, false, false);
                long received = FeCompat.insertEu(targetFE, extracted, false);
                if (received < extracted) source.charge(extracted - received, source.getTier(), true, false);
                return received;
            }
        }
        return 0;
    }

    @Override
    public ItemModuleSettingsBuilder getSettings(ModuleContext context, PanelSyncManager psm, int id) {
        ItemModuleSettingsBuilder settings = super.getSettings(context, psm, id);
        IntSyncValue tier = new IntSyncValue(() -> context.getData(AutoChargeModuleData.class).getVoltageTier(),
                value -> context.setData(context.getData(AutoChargeModuleData.class)
                        .withVoltageTier(Mth.clamp(value, 0, getTier()))))
                .allowC2S();
        IntSyncValue amps = new IntSyncValue(() -> context.getData(AutoChargeModuleData.class).getAmperage(),
                value -> context.setData(context.getData(AutoChargeModuleData.class)
                        .withAmperage(Mth.clamp(value, 1, 16))))
                .allowC2S();
        psm.syncValue("charger_tier", id, tier);
        psm.syncValue("charger_amps", id, amps);
        IPanelHandler tiers = psm.syncedPanel("charger_tiers_" + id, false,
                (manager, handler) -> createSelector("charger_tiers_" + id, handler, tier, 0, getTier() + 1,
                        value -> GTValues.VNF[value]));
        IPanelHandler amperages = psm.syncedPanel("charger_amps_" + id, false,
                (manager, handler) -> createSelector("charger_amps_" + id, handler, amps, 1, 16,
                        value -> value + "A"));
        settings.add(Flow.row().coverChildren().childPadding(5)
                .child(Text.lang("gtceu.top.cable_voltage").asWidget())
                .child(new ButtonWidget<>().size(50, 18)
                        .overlay(Text.dynamic(() -> Component.literal(GTValues.VNF[tier.getIntValue()])))
                        .onMousePressed((ctx, button) -> {
                            if (button != 0) return false;
                            amperages.closePanel();
                            tiers.togglePanel();
                            return true;
                        })));
        settings.add(Flow.row().coverChildren().childPadding(5)
                .child(Text.lang("gtceu.creative.energy.amperage").asWidget())
                .child(new ButtonWidget<>().size(50, 18)
                        .overlay(Text.dynamic(() -> Component.literal(amps.getIntValue() + "A")))
                        .onMousePressed((ctx, button) -> {
                            if (button != 0) return false;
                            tiers.closePanel();
                            amperages.togglePanel();
                            return true;
                        })));
        return settings;
    }

    private ModularPanel<?> createSelector(String name, IPanelHandler handler, IntSyncValue value, int first, int count,
                                           java.util.function.IntFunction<String> label) {
        return new Dialog<>(name).disablePanelsBelow(false).closeOnOutOfBoundsClick(true).size(76, 108)
                .child(new ListWidget<>().pos(4, 4).size(68, 100)
                        .children(count, index -> new ButtonWidget<>().size(60, 18)
                                .overlay(Text.str(label.apply(first + index)))
                                .onMousePressed((ctx, button) -> {
                                    if (button != 0) return false;
                                    value.setIntValue(first + index);
                                    handler.closePanel();
                                    return true;
                                })));
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

        MetaMachine machine = MetaMachine.getMachine(context.getLevel(), pos);
        if (machine instanceof PowerSubstationMachine || machine instanceof BatteryBufferMachine) {
            PlayerOwner owner = machine.getPlayerOwner();
            Player player = context.getPlayer();
            if (owner == null || player != null && owner.isPlayerFriendly(player.getUUID())) {
                if (context.getLevel().isClientSide()) return InteractionResult.SUCCESS;
                moduleContext.setData(moduleContext.getData(AutoChargeModuleData.class)
                        .withLinkedPos(GlobalPos.of(context.getLevel().dimension(), pos)));
                if (player != null) player.sendSystemMessage(Component.translatable("behaviour.charger_linked"));
                context.getLevel().playSound(null, pos, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.5f,
                        1.2f);
                return InteractionResult.CONSUME;
            }
        }
        return super.onItemUseFirst(moduleContext, context);
    }

    public static class AutoChargeModuleData extends ModuleData {

        // spotless:off
        public static final Codec<AutoChargeModuleData> CODEC = RecordCodecBuilder.create(instance -> baseCodec(instance).and(instance.group(
                GlobalPos.CODEC.optionalFieldOf("linked_pos").forGetter(AutoChargeModuleData::getLinkedPosOptional),
                Codec.intRange(0, GTValues.TIER_COUNT - 1).optionalFieldOf("voltage_tier").forGetter(data -> Optional.of(data.getVoltageTier())),
                Codec.intRange(1, 16).optionalFieldOf("amperage", 1).forGetter(AutoChargeModuleData::getAmperage)
        )).apply(instance, AutoChargeModuleData::new));
        // spotless:on

        @Getter
        private final @Nullable GlobalPos linkedPos;

        @Getter
        private final int voltageTier;
        @Getter
        private final int amperage;

        @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
        public AutoChargeModuleData(int slot, ItemModule module, ItemStack moduleItem, boolean enabled,
                                    Optional<GlobalPos> globalPos, Optional<Integer> voltageTier, int amperage) {
            this(slot, module, moduleItem, enabled, globalPos.orElse(null),
                    voltageTier.orElse(((TieredItemModule) module).getTier()), amperage);
        }

        public AutoChargeModuleData(int slot, ItemModule module, ItemStack moduleItem, boolean enabled,
                                    @Nullable GlobalPos globalPos) {
            this(slot, module, moduleItem, enabled, globalPos, ((TieredItemModule) module).getTier(), 1);
        }

        public AutoChargeModuleData(int slot, ItemModule module, ItemStack moduleItem, boolean enabled,
                                    @Nullable GlobalPos globalPos, int voltageTier, int amperage) {
            super(slot, module, moduleItem, enabled);
            this.linkedPos = globalPos;
            this.voltageTier = Mth.clamp(voltageTier, 0, ((TieredItemModule) module).getTier());
            this.amperage = Mth.clamp(amperage, 1, 16);
        }

        public AutoChargeModuleData(int slot, ItemModule module, ItemStack moduleItem) {
            this(slot, module, moduleItem, true, null);
        }

        public Optional<GlobalPos> getLinkedPosOptional() {
            return Optional.ofNullable(linkedPos);
        }

        @Override
        public ModuleData withEnabled(boolean enabled) {
            return new AutoChargeModuleData(slot, module, moduleItem, enabled, linkedPos, voltageTier, amperage);
        }

        @Override
        public ModuleData withModuleItem(ItemStack moduleItem) {
            return new AutoChargeModuleData(slot, module, moduleItem, enabled, linkedPos, voltageTier, amperage);
        }

        public ModuleData withLinkedPos(GlobalPos linkedPos) {
            return new AutoChargeModuleData(slot, module, moduleItem, enabled, linkedPos, voltageTier, amperage);
        }

        public AutoChargeModuleData withVoltageTier(int voltageTier) {
            return new AutoChargeModuleData(slot, module, moduleItem, enabled, linkedPos, voltageTier, amperage);
        }

        public AutoChargeModuleData withAmperage(int amperage) {
            return new AutoChargeModuleData(slot, module, moduleItem, enabled, linkedPos, voltageTier, amperage);
        }

        @Override
        public ModuleData copy() {
            return new AutoChargeModuleData(slot, module, moduleItem.copy(), enabled, linkedPos, voltageTier, amperage);
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof AutoChargeModuleData other)) return false;
            return super.equals(obj) && Objects.equals(linkedPos, other.linkedPos) &&
                    voltageTier == other.voltageTier && amperage == other.amperage;
        }

        @Override
        public int hashCode() {
            return Objects.hash(slot, module, moduleItem, enabled, linkedPos, voltageTier, amperage);
        }
    }
}

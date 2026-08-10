package com.gregtechceu.gtceu.common.network.packets;

import com.gregtechceu.gtceu.api.gui.widget.TankWidget;
import com.gregtechceu.gtceu.api.recipe.ingredient.fluid.FluidIngredient;
import com.gregtechceu.gtceu.common.network.GTNetwork;

import com.lowdragmc.lowdraglib.gui.modular.ModularUIContainer;
import com.lowdragmc.lowdraglib.jei.IngredientIO;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class CPacketEmiFluidTransfer implements GTNetwork.INetPacket {

    private final int containerId;
    private final List<Integer> sourceSlots;
    private final List<FluidIngredient> ingredients;
    private final int multiplier;

    public CPacketEmiFluidTransfer(int containerId, List<Integer> sourceSlots, List<FluidIngredient> ingredients,
                                   int multiplier) {
        this.containerId = containerId;
        this.sourceSlots = List.copyOf(sourceSlots);
        this.ingredients = List.copyOf(ingredients);
        this.multiplier = multiplier;
    }

    public CPacketEmiFluidTransfer(FriendlyByteBuf buffer) {
        containerId = buffer.readVarInt();
        sourceSlots = buffer.readList(FriendlyByteBuf::readVarInt);
        ingredients = buffer.readList(FluidIngredient::fromNetwork);
        multiplier = buffer.readVarInt();
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeVarInt(containerId);
        buffer.writeCollection(sourceSlots, FriendlyByteBuf::writeVarInt);
        buffer.writeCollection(ingredients, (buf, ingredient) -> ingredient.toNetwork(buf));
        buffer.writeVarInt(multiplier);
    }

    @Override
    public void execute(NetworkEvent.Context context) {
        ServerPlayer player = context.getSender();
        if (player == null || multiplier < 1 || player.containerMenu.containerId != containerId ||
                !(player.containerMenu instanceof ModularUIContainer menu)) {
            return;
        }

        List<Slot> sources = new ArrayList<>();
        for (int index : sourceSlots) {
            if (index < 0 || index >= menu.slots.size()) {
                return;
            }
            Slot slot = menu.slots.get(index);
            if (slot.container != player.getInventory()) {
                return;
            }
            sources.add(slot);
        }
        //
        List<TankTarget> targets = menu.getModularUI().getFlatWidgetCollection().stream()
                .filter(w -> w instanceof TankWidget)
                .map(TankWidget.class::cast)
                .filter(t -> t.getIngredientIO() == IngredientIO.INPUT)
                .filter(t -> t.getFluidTank() != null)
                .map(t -> new TankTarget(t.getFluidTank(), t.getTank()))
                .collect(java.util.stream.Collectors.collectingAndThen(
                        java.util.stream.Collectors.toCollection(LinkedHashSet::new), ArrayList::new));

        if (targets.isEmpty()) {
            return;
        }

        Set<TankTarget> usedTargets = new LinkedHashSet<>();
        for (FluidIngredient ingredient : ingredients) {
            long requested = (long) ingredient.getAmount() * multiplier;
            int remaining = (int) Math.min(Integer.MAX_VALUE, requested);
            TankTarget target = findTarget(targets, usedTargets, ingredient);
            if (target == null) {
                continue;
            }
            usedTargets.add(target);
            for (Slot source : sources) {
                if (remaining <= 0) break;
                while (remaining > 0) {
                    ItemStack stack = source.getItem();
                    var container = FluidUtil.getFluidHandler(stack).resolve().orElse(null);
                    if (container == null || !containsMatchingFluid(container, ingredient)) break;

                    boolean transferred = false;
                    int before = target.getFluidAmount();
                    FluidActionResult result = FluidUtil.tryEmptyContainer(stack.copyWithCount(1), target.handler,
                            remaining, null, true);
                    int moved = target.getFluidAmount() - before;
                    if (result.isSuccess() && moved > 0) {
                        stack.shrink(1);
                        if (stack.isEmpty()) {
                            source.setByPlayer(result.getResult());
                        } else {
                            player.getInventory().placeItemBackInInventory(result.getResult());
                        }
                        source.setChanged();
                        remaining -= moved;
                        transferred = true;
                    }
                    if (!transferred) break;
                }
            }
        }
        menu.broadcastChanges();
    }

    private static boolean containsMatchingFluid(IFluidHandler handler, FluidIngredient ingredient) {
        for (int tank = 0; tank < handler.getTanks(); tank++) {
            if (ingredient.test(handler.getFluidInTank(tank))) return true;
        }
        return false;
    }

    private static TankTarget findTarget(List<TankTarget> targets, Set<TankTarget> usedTargets,
                                         FluidIngredient ingredient) {
        for (TankTarget target : targets) {
            if (!usedTargets.contains(target) && ingredient.test(target.getFluid())) return target;
        }
        for (TankTarget target : targets) {
            if (usedTargets.contains(target) || !target.getFluid().isEmpty()) continue;
            for (var fluid : ingredient.getFluids()) {
                if (target.isFluidValid(fluid)) return target;
            }
        }
        return null;
    }

    private record TankTarget(IFluidHandler handler, int tank) {

        private FluidStack getFluid() {
            return handler.getFluidInTank(tank);
        }

        private int getFluidAmount() {
            return getFluid().getAmount();
        }

        private boolean isFluidValid(FluidStack fluid) {
            return handler.isFluidValid(tank, fluid);
        }
    }
}

package com.gregtechceu.gtceu.integration.emi.recipe;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.gui.widget.GhostCircuitSlotWidget;
import com.gregtechceu.gtceu.api.recipe.ingredient.fluid.FluidIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.item.IntCircuitIngredient;
import com.gregtechceu.gtceu.common.item.IntCircuitBehaviour;
import com.gregtechceu.gtceu.common.network.GTNetwork;
import com.gregtechceu.gtceu.common.network.packets.CPacketEmiFluidTransfer;
import com.gregtechceu.gtceu.common.network.packets.CPacketEmiSetCircuit;

import com.lowdragmc.lowdraglib.gui.modular.ModularUIContainer;
import com.lowdragmc.lowdraglib.gui.widget.SlotWidget;
import com.lowdragmc.lowdraglib.jei.IngredientIO;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

import dev.emi.emi.api.forge.ForgeEmiStack;
import dev.emi.emi.api.recipe.EmiPlayerInventory;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.recipe.handler.EmiCraftContext;
import dev.emi.emi.api.recipe.handler.StandardRecipeHandler;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.Widget;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.emi.emi.platform.EmiClient;
import dev.emi.emi.registry.EmiRecipeFiller;
import dev.emi.emi.runtime.EmiDrawContext;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GTEmiRecipeHandler implements StandardRecipeHandler<ModularUIContainer> {

    @Override
    public List<Slot> getInputSources(ModularUIContainer handler) {
        return handler.getModularUI().getSlotMap().values().stream()
                .filter(e -> e.getIngredientIO() == IngredientIO.INPUT || e.isPlayerContainer || e.isPlayerHotBar)
                .map(SlotWidget::getHandler)
                .toList();
    }

    @Override
    public List<Slot> getCraftingSlots(ModularUIContainer handler) {
        return handler.getModularUI().getSlotMap().values().stream()
                .filter(e -> e.getIngredientIO() == IngredientIO.INPUT && !(e instanceof GhostCircuitSlotWidget))
                .map(SlotWidget::getHandler)
                .toList();
    }

    @Override
    public List<Slot> getCraftingSlots(EmiRecipe recipe, ModularUIContainer handler) {
        if (recipe instanceof ItemTransferRecipe) {
            return getCraftingSlots(handler);
        }
        return StandardRecipeHandler.super.getCraftingSlots(recipe, handler);
    }

    @Override
    public EmiPlayerInventory getInventory(AbstractContainerScreen<ModularUIContainer> screen) {
        List<EmiStack> stacks = new ArrayList<>();
        for (Slot slot : getInputSources(screen.getMenu())) {
            stacks.add(EmiStack.of(slot.getItem()));
            FluidUtil.getFluidHandler(slot.getItem()).resolve().ifPresent(handler -> {
                for (int i = 0; i < handler.getTanks(); i++) {
                    FluidStack fluid = handler.getFluidInTank(i);
                    if (!fluid.isEmpty()) {
                        stacks.add(ForgeEmiStack.of(fluid)
                                .setAmount((long) fluid.getAmount() * slot.getItem().getCount()));
                    }
                }
            });
        }
        if (screen.getMenu()
                .getModularUI().holder instanceof com.gregtechceu.gtceu.api.machine.feature.IHasCircuitSlot) {
            for (int configuration = IntCircuitIngredient.CIRCUIT_MIN; configuration <=
                    IntCircuitIngredient.CIRCUIT_MAX; configuration++) {
                stacks.add(EmiStack.of(IntCircuitBehaviour.stack(configuration)));
            }
        }
        return new EmiPlayerInventory(stacks);
    }

    @Override
    public boolean craft(EmiRecipe recipe, EmiCraftContext<ModularUIContainer> context) {
        if (!(recipe instanceof GTEmiRecipe gtRecipe) || !canCraft(recipe, context)) return false;

        ItemTransferRecipe itemRecipe = new ItemTransferRecipe(gtRecipe);
        int requestedBatches = Math.max(1, context.getAmount());
        int itemBatches = requestedBatches;
        if (!itemRecipe.getInputs().isEmpty()) {
            List<ItemStack> stacks = EmiRecipeFiller.getStacks(this, itemRecipe, context.getScreen(),
                    requestedBatches);
            if (stacks == null) return false;
            itemBatches = Math.min(requestedBatches, getItemBatchCount(itemRecipe, stacks));

            Minecraft.getInstance().setScreen(context.getScreen());
            if (!EmiClient.onServer) {
                if (!EmiRecipeFiller.clientFill(this, itemRecipe, context.getScreen(), stacks,
                        context.getDestination())) {
                    return false;
                }
            } else {
                EmiClient.sendFillRecipe(this, context.getScreen(), context.getScreenHandler().containerId,
                        switch (context.getDestination()) {
                            case NONE -> 0;
                            case CURSOR -> 1;
                            case INVENTORY -> 2;
                        }, stacks, itemRecipe);
            }
        } else if (context.getDestination() != EmiCraftContext.Destination.NONE) {
            return false;
        }
        sendFluidTransfers(gtRecipe, context, itemBatches);
        sendCircuitConfiguration(gtRecipe, context);
        return true;
    }

    private static void sendFluidTransfers(GTEmiRecipe recipe, EmiCraftContext<ModularUIContainer> context,
                                           int itemBatches) {
        var ingredients = recipe.recipe.getInputContents(FluidRecipeCapability.CAP);
        if (ingredients.isEmpty()) return;

        List<Integer> sourceSlots = context.getScreenHandler().getModularUI().getSlotMap().values().stream()
                .filter(slot -> slot.isPlayerContainer || slot.isPlayerHotBar)
                .map(SlotWidget::getHandler)
                .filter(java.util.Objects::nonNull)
                .map(slot -> slot.index)
                .toList();
        int multiplier = Math.min(itemBatches,
                getAvailableFluidBatches(context.getScreenHandler(), sourceSlots, ingredients));
        if (multiplier <= 0) return;
        GTNetwork.sendToServer(new CPacketEmiFluidTransfer(context.getScreenHandler().containerId, sourceSlots,
                ingredients, multiplier));
    }

    private static int getItemBatchCount(ItemTransferRecipe recipe, List<ItemStack> stacks) {
        int batches = Integer.MAX_VALUE;
        List<EmiIngredient> inputs = recipe.getInputs();
        for (int i = 0; i < inputs.size(); i++) {
            ItemStack stack = stacks.get(i);
            if (!stack.isEmpty()) {
                batches = Math.min(batches, (int) (stack.getCount() / Math.max(1, inputs.get(i).getAmount())));
            }
        }
        return batches;
    }

    private static int getAvailableFluidBatches(ModularUIContainer menu, List<Integer> sourceSlots,
                                                List<FluidIngredient> ingredients) {
        long batches = Long.MAX_VALUE;
        for (FluidIngredient ingredient : ingredients) {
            long available = 0;
            for (int index : sourceSlots) {
                if (index < 0 || index >= menu.slots.size()) continue;
                ItemStack stack = menu.slots.get(index).getItem();
                var handler = FluidUtil.getFluidHandler(stack).resolve().orElse(null);
                if (handler == null) continue;
                for (int tank = 0; tank < handler.getTanks(); tank++) {
                    FluidStack fluid = handler.getFluidInTank(tank);
                    if (ingredient.test(fluid)) {
                        available += (long) fluid.getAmount() * stack.getCount();
                    }
                }
            }
            batches = Math.min(batches, available / Math.max(1, ingredient.getAmount()));
        }
        return (int) Math.min(Integer.MAX_VALUE, batches);
    }

    private static void sendCircuitConfiguration(GTEmiRecipe recipe, EmiCraftContext<ModularUIContainer> context) {
        for (var ingredient : recipe.recipe.getInputContents(ItemRecipeCapability.CAP)) {
            if (ingredient instanceof IntCircuitIngredient circuit) {
                GTNetwork.sendToServer(new CPacketEmiSetCircuit(context.getScreenHandler().containerId,
                        circuit.getConfiguration()));
            }
        }
    }

    @Override
    public boolean supportsRecipe(EmiRecipe recipe) {
        return recipe instanceof GTEmiRecipe;
    }

    @Override
    public void render(EmiRecipe recipe, EmiCraftContext<ModularUIContainer> context, List<Widget> widgets,
                       GuiGraphics graphics) {
        List<Boolean> availability = context.getInventory().getCraftAvailability(recipe);
        if (availability.size() != recipe.getInputs().size()) return;

        EmiDrawContext draw = EmiDrawContext.wrap(graphics);
        draw.enableDepthTest();
        for (Widget widget : widgets) {
            if (!(widget instanceof dev.emi.emi.api.widget.SlotWidget slot) || slot.getRecipe() != null) continue;
            for (int i = 0; i < recipe.getInputs().size(); i++) {
                if (!EmiIngredient.areEqual(slot.getStack(), recipe.getInputs().get(i)) || availability.get(i))
                    continue;
                Bounds bounds = slot.getBounds();
                draw.fill(bounds.x(), bounds.y(), bounds.width(), bounds.height(), 1157562368);
                break;
            }
        }
    }

    private static final class ItemTransferRecipe implements EmiRecipe {

        private final GTEmiRecipe recipe;
        private final List<EmiIngredient> inputs;

        private ItemTransferRecipe(GTEmiRecipe recipe) {
            this.recipe = recipe;
            this.inputs = recipe.recipe.getInputContents(ItemRecipeCapability.CAP).stream()
                    .filter(ingredient -> !(ingredient instanceof IntCircuitIngredient))
                    .map(ingredient -> EmiIngredient.of(Arrays.stream(ingredient.getItems())
                            .map(EmiStack::of)
                            .toList()))
                    .toList();
        }

        @Override
        public EmiRecipeCategory getCategory() {
            return recipe.getCategory();
        }

        @Override
        public @Nullable net.minecraft.resources.ResourceLocation getId() {
            return recipe.getId();
        }

        @Override
        public List<EmiIngredient> getInputs() {
            return inputs;
        }

        @Override
        public List<EmiStack> getOutputs() {
            return recipe.getOutputs();
        }

        @Override
        public int getDisplayWidth() {
            return recipe.getDisplayWidth();
        }

        @Override
        public int getDisplayHeight() {
            return recipe.getDisplayHeight();
        }

        @Override
        public void addWidgets(WidgetHolder widgets) {}
    }
}

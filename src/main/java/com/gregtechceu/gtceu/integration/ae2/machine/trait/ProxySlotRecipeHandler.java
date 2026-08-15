package com.gregtechceu.gtceu.integration.ae2.machine.trait;

import com.gregtechceu.gtceu.api.capability.recipe.*;
import com.gregtechceu.gtceu.api.machine.trait.*;
import com.gregtechceu.gtceu.api.machine.trait.notifiable.NotifiableRecipeHandlerTrait;
import com.gregtechceu.gtceu.api.machine.trait.recipe.IRecipeHandlerTrait;
import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeHandlerGroupDistinctness;
import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeHandlerList;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.integration.ae2.machine.MEPatternBufferPartMachine;
import com.gregtechceu.gtceu.integration.ae2.machine.MEPatternBufferProxyPartMachine;
import com.gregtechceu.gtceu.utils.ISubscription;

import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class ProxySlotRecipeHandler {

    @Getter
    private final List<RecipeHandlerList> proxySlotHandlers;
    private final ProxyRHL proxyRHL;

    public ProxySlotRecipeHandler(MEPatternBufferProxyPartMachine machine) {
        this.proxyRHL = new ProxyRHL(machine);
        this.proxySlotHandlers = List.of(proxyRHL);
    }

    public void updateProxy(MEPatternBufferPartMachine patternBuffer) {
        proxyRHL.setBuffer(patternBuffer);
    }

    public void clearProxy() {
        proxyRHL.clearBuffer();
    }

    private static class ProxyRHL extends RecipeHandlerList {

        private final ProxyItemRecipeHandler circuit;
        private final ProxyItemRecipeHandler sharedItem;
        private final ProxyItemRecipeHandler slotItem;
        private final ProxyFluidRecipeHandler sharedFluid;
        private final ProxyFluidRecipeHandler slotFluid;

        private @Nullable MEPatternBufferPartMachine buffer;

        public ProxyRHL(MEPatternBufferProxyPartMachine machine) {
            super(IO.IN);
            circuit = machine.attachTrait(new ProxyItemRecipeHandler());
            sharedItem = machine.attachTrait(new ProxyItemRecipeHandler());
            slotItem = machine.attachTrait(new ProxyItemRecipeHandler());
            sharedFluid = machine.attachTrait(new ProxyFluidRecipeHandler());
            slotFluid = machine.attachTrait(new ProxyFluidRecipeHandler());
            addHandlers(circuit, sharedItem, slotItem, sharedFluid, slotFluid);
            this.setGroup(RecipeHandlerGroupDistinctness.BUS_DISTINCT);
        }

        public void setBuffer(MEPatternBufferPartMachine buffer) {
            this.buffer = buffer;
            circuit.setProxy(buffer.getCircuitSlot());
            sharedItem.setProxy(buffer.getShareInventory());
            sharedFluid.setProxy(buffer.getShareTank());
            slotItem.setProxy(buffer.getAggregateItemHandler());
            slotFluid.setProxy(buffer.getAggregateFluidHandler());
        }

        public void clearBuffer() {
            this.buffer = null;
            circuit.setProxy(null);
            sharedItem.setProxy(null);
            sharedFluid.setProxy(null);
            slotItem.setProxy(null);
            slotFluid.setProxy(null);
        }

        @Override
        public Map<RecipeCapability<?>, List<Object>> handleRecipe(IO io, GTRecipe recipe,
                                                                   Map<RecipeCapability<?>, List<Object>> contents,
                                                                   boolean simulate) {
            MEPatternBufferPartMachine buf = buffer;
            if (buf == null) return contents;
            return buf.getBufferRecipeHandler().handleRecipe(io, recipe, contents, simulate);
        }

        @Override
        public boolean isDistinct() {
            return true;
        }

        @Override
        public void setDistinct(boolean ignored, boolean notify) {}
    }

    @Getter
    private static class ProxyItemRecipeHandler extends NotifiableRecipeHandlerTrait<SizedIngredient> {

        private @Nullable IRecipeHandlerTrait<SizedIngredient> proxy = null;
        private @Nullable ISubscription proxySub = null;

        private final IO handlerIO = IO.IN;
        private final RecipeCapability<SizedIngredient> capability = ItemRecipeCapability.CAP;
        private final boolean isDistinct = true;

        public ProxyItemRecipeHandler() {
            super();
        }

        public void setProxy(@Nullable IRecipeHandlerTrait<SizedIngredient> proxy) {
            this.proxy = proxy;
            if (proxySub != null) {
                proxySub.unsubscribe();
                proxySub = null;
            }
            if (proxy != null) {
                proxySub = proxy.addChangedListener(this::notifyListeners);
            }
        }

        @Override
        public List<SizedIngredient> handleRecipeInner(IO io, GTRecipe recipe, List<SizedIngredient> left,
                                                       boolean simulate) {
            if (proxy == null) return left;
            return proxy.handleRecipeInner(io, recipe, left, simulate);
        }

        @Override
        public int getSize() {
            if (proxy == null) return 0;
            return proxy.getSize();
        }

        @Override
        public List<Object> getContents() {
            if (proxy == null) return Collections.emptyList();
            return proxy.getContents();
        }

        @Override
        public double getTotalContentAmount() {
            if (proxy == null) return 0;
            return proxy.getTotalContentAmount();
        }

        public int getPriority() {
            if (proxy == null) return IFilteredHandler.LOW;
            return proxy.getPriority();
        }
    }

    @Getter
    private static class ProxyFluidRecipeHandler extends NotifiableRecipeHandlerTrait<SizedFluidIngredient> {

        private @Nullable IRecipeHandlerTrait<SizedFluidIngredient> proxy = null;
        private @Nullable ISubscription proxySub = null;

        private final IO handlerIO = IO.IN;
        private final RecipeCapability<SizedFluidIngredient> capability = FluidRecipeCapability.CAP;
        private final boolean isDistinct = true;

        public ProxyFluidRecipeHandler() {
            super();
        }

        public void setProxy(@Nullable IRecipeHandlerTrait<SizedFluidIngredient> proxy) {
            this.proxy = proxy;
            if (proxySub != null) {
                proxySub.unsubscribe();
                proxySub = null;
            }
            if (proxy != null) {
                proxySub = proxy.addChangedListener(this::notifyListeners);
            }
        }

        @Override
        public List<SizedFluidIngredient> handleRecipeInner(IO io, GTRecipe recipe, List<SizedFluidIngredient> left,
                                                            boolean simulate) {
            if (proxy == null) return left;
            return proxy.handleRecipeInner(io, recipe, left, simulate);
        }

        @Override
        public int getSize() {
            if (proxy == null) return 0;
            return proxy.getSize();
        }

        @Override
        public List<Object> getContents() {
            if (proxy == null) return Collections.emptyList();
            return proxy.getContents();
        }

        @Override
        public double getTotalContentAmount() {
            if (proxy == null) return 0;
            return proxy.getTotalContentAmount();
        }

        public int getPriority() {
            if (proxy == null) return IFilteredHandler.LOW;
            return proxy.getPriority();
        }
    }
}

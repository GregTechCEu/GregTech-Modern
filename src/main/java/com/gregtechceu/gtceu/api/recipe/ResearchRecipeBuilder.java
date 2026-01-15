package com.gregtechceu.gtceu.api.recipe;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.item.IComponentItem;
import com.gregtechceu.gtceu.api.item.component.IDataItem;
import com.gregtechceu.gtceu.api.item.component.IItemComponent;
import com.gregtechceu.gtceu.api.recipe.ingredient.EnergyStack;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import com.gregtechceu.gtceu.utils.GTStringUtils;
import com.gregtechceu.gtceu.utils.ResearchManager;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

public abstract class ResearchRecipeBuilder<T extends ResearchRecipeBuilder<T>> {

    protected ItemStack itemResearchStack = ItemStack.EMPTY;
    protected FluidStack fluidResearchStack = FluidStack.EMPTY;
    protected ItemStack dataStack;
    protected String researchId;
    protected EnergyStack eut;

    public T researchStack(@NotNull ItemStack researchStack) {
        if (!researchStack.isEmpty()) {
            this.itemResearchStack = researchStack;
        }
        return (T) this;
    }

    public T researchFluidStack(@NotNull FluidStack researchStack) {
        if (!researchStack.isEmpty()) {
            this.fluidResearchStack = researchStack;
        }
        return (T) this;
    }

    public T dataStack(@NotNull ItemStack dataStack) {
        if (!dataStack.isEmpty()) {
            this.dataStack = dataStack;
        }
        return (T) this;
    }

    public T researchId(String researchId) {
        this.researchId = researchId;
        return (T) this;
    }

    public T EUt(long eut) {
        return EUt(eut, 1);
    }

    public T EUt(long eut, long amperage) {
        this.eut = new EnergyStack(eut, amperage);
        return (T) this;
    }

    protected void validateResearchItem(ResourceLocation recipeId) {
        if (itemResearchStack.isEmpty() && fluidResearchStack.isEmpty()) {
            throw new IllegalArgumentException(String.format(
                    "Research recipe must have an item or fluid stack, id: %s", recipeId));
        }

        if (researchId == null) {
            if (!itemResearchStack.isEmpty()) researchId = GTStringUtils.itemStackToString(itemResearchStack);
            else researchId = GTStringUtils.fluidStackToString(fluidResearchStack);
        }

        if (dataStack == null) {
            dataStack = getDefaultDataItem();
        }

        boolean foundBehavior = false;
        if (dataStack.getItem() instanceof IComponentItem metaItem) {
            for (IItemComponent behaviour : metaItem.getComponents()) {
                if (behaviour instanceof IDataItem) {
                    foundBehavior = true;
                    dataStack = dataStack.copy();
                    dataStack.setCount(1);
                    break;
                }
            }
        }
        if (!foundBehavior) {
            throw new IllegalArgumentException("Data ItemStack must have the IDataItem behavior");
        }
    }

    public abstract ItemStack getDefaultDataItem();

    public abstract GTRecipeBuilder.ResearchRecipeEntry build(ResourceLocation recipeId);

    @NoArgsConstructor
    public static class ScannerRecipeBuilder extends ResearchRecipeBuilder<ScannerRecipeBuilder> {

        public static final int DEFAULT_SCANNER_DURATION = 1200; // 60 secs
        public static final int DEFAULT_SCANNER_EUT = GTValues.VA[GTValues.HV];

        private int duration;

        public ScannerRecipeBuilder duration(int duration) {
            this.duration = duration;
            return this;
        }

        @Override
        public ItemStack getDefaultDataItem() {
            return ResearchManager.getDefaultScannerItem();
        }

        @Override
        public GTRecipeBuilder.ResearchRecipeEntry build(ResourceLocation recipeId) {
            validateResearchItem(recipeId);
            if (duration <= 0) duration = DEFAULT_SCANNER_DURATION;
            if (eut == null || eut.voltage() <= 0) eut = new EnergyStack(DEFAULT_SCANNER_EUT, 1);
            return new GTRecipeBuilder.ResearchRecipeEntry(researchId, itemResearchStack, fluidResearchStack, dataStack,
                    duration, eut, 0);
        }
    }

    @NoArgsConstructor
    public static class StationRecipeBuilder extends ResearchRecipeBuilder<StationRecipeBuilder> {

        public static final int DEFAULT_STATION_EUT = GTValues.VA[GTValues.LuV];
        // By default, the total CWU needed will be 200 seconds if exactly enough CWU/t is provided.
        // Providing more CWU/t will allow it to take less time.
        public static final int DEFAULT_STATION_TOTAL_CWUT = 4000;

        private int cwut;
        private int totalCWU;

        public StationRecipeBuilder CWUt(int cwut) {
            this.cwut = cwut;
            this.totalCWU = cwut * DEFAULT_STATION_TOTAL_CWUT;
            return this;
        }

        public StationRecipeBuilder CWUt(int cwut, int totalCWU) {
            this.cwut = cwut;
            this.totalCWU = totalCWU;
            return this;
        }

        @Override
        public ItemStack getDefaultDataItem() {
            return ResearchManager.getDefaultResearchStationItem(cwut);
        }

        @Override
        public GTRecipeBuilder.ResearchRecipeEntry build(ResourceLocation recipeId) {
            validateResearchItem(recipeId);
            if (cwut <= 0 || totalCWU <= 0) {
                throw new IllegalArgumentException("CWU/t and total CWU must both be set, and non-zero!");
            }
            if (cwut > totalCWU) {
                throw new IllegalArgumentException("Total CWU cannot be greater than CWU/t!");
            }

            // "duration" is the total CWU/t.
            // Not called duration in API because logic does not treat it like normal duration.
            int duration = totalCWU;
            if (eut == null || eut.voltage() <= 0) eut = new EnergyStack(DEFAULT_STATION_EUT, 1);

            return new GTRecipeBuilder.ResearchRecipeEntry(researchId, itemResearchStack, fluidResearchStack, dataStack,
                    duration, eut, cwut);
        }
    }
}

package com.gregtechceu.gtceu.api.recipe;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.gregtechceu.gtceu.api.item.component.IDataItem;
import com.gregtechceu.gtceu.api.item.component.IItemComponent;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import com.gregtechceu.gtceu.utils.AssemblyLineManager;
import com.gregtechceu.gtceu.utils.GTStringUtils;
import lombok.NoArgsConstructor;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public abstract class ResearchRecipeBuilder<T extends ResearchRecipeBuilder<T>> {

    protected ItemStack researchStack;
    protected ItemStack dataStack;
    protected String researchId;
    protected int eut;

    public T researchStack(@Nonnull ItemStack researchStack) {
        if (!researchStack.isEmpty()) {
            this.researchStack = researchStack;
        }
        return (T) this;
    }

    public T dataStack(@Nonnull ItemStack dataStack) {
        if (!dataStack.isEmpty()) {
            this.dataStack = dataStack;
        }
        return (T) this;
    }

    public T researchId(String researchId) {
        this.researchId = researchId;
        return (T) this;
    }

    public T EUt(int eut) {
        this.eut = eut;
        return (T) this;
    }

    protected void validateResearchItem() {
        if (researchStack == null) {
            throw new IllegalArgumentException("Research stack cannot be null or empty!");
        }

        if (researchId == null) {
            researchId = GTStringUtils.itemStackToString(researchStack);
        }

        if (dataStack == null) {
            dataStack = getDefaultDataItem();
        }

        boolean foundBehavior = false;
        if (dataStack.getItem() instanceof ComponentItem metaItem) {
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

    public abstract GTRecipeBuilder.ResearchRecipeEntry build();

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
            return AssemblyLineManager.getDefaultScannerItem();
        }

        @Override
        public GTRecipeBuilder.ResearchRecipeEntry build() {
            validateResearchItem();
            if (duration <= 0) duration = DEFAULT_SCANNER_DURATION;
            if (eut <= 0) eut = DEFAULT_SCANNER_EUT;
            return new GTRecipeBuilder.ResearchRecipeEntry(researchId, researchStack, dataStack, duration, eut, 0);
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
            return AssemblyLineManager.getDefaultResearchStationItem(cwut);
        }

        @Override
        public GTRecipeBuilder.ResearchRecipeEntry build() {
            validateResearchItem();
            if (cwut <= 0 || totalCWU <= 0) {
                throw new IllegalArgumentException("CWU/t and total CWU must both be set, and non-zero!");
            }
            if (cwut > totalCWU) {
                throw new IllegalArgumentException("Total CWU cannot be greater than CWU/t!");
            }

            // "duration" is the total CWU/t.
            // Not called duration in API because logic does not treat it like normal duration.
            int duration = totalCWU;
            if (eut <= 0) eut = DEFAULT_STATION_EUT;

            return new GTRecipeBuilder.ResearchRecipeEntry(researchId, researchStack, dataStack, duration, eut, cwut);
        }
    }
}
package com.gregtechceu.gtceu.common.machine.multiblock.part;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.IParallelHatch;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredPartMachine;
import com.lowdragmc.lowdraglib.gui.widget.*;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import lombok.Getter;
import net.minecraft.util.Mth;

import javax.annotation.Nonnull;
import java.util.function.Function;
import java.util.function.IntSupplier;

public class ParallelHatchPartMachine extends TieredPartMachine implements IFancyUIMachine, IParallelHatch {
    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(ParallelHatchPartMachine.class, MultiblockPartMachine.MANAGED_FIELD_HOLDER);
    private static final int MIN_PARALLEL = 1;

    private final int maxParallel;

    @Persisted
    @Getter
    private int currentParallel;

    public ParallelHatchPartMachine(IMachineBlockEntity holder, int tier) {
        super(holder, tier);
        this.maxParallel = (int) Math.pow(4, tier - GTValues.EV);
    }

    public void setCurrentParallel(int parallelAmount) {
        this.currentParallel = Mth.clamp(this.currentParallel + parallelAmount, MIN_PARALLEL, this.maxParallel);
    }

    @Override
    public Widget createUIWidget() {
        WidgetGroup parallelAmountGroup = new WidgetGroup();
        parallelAmountGroup.addWidget(new ImageWidget(62, 36, 53, 20, GuiTextures.DISPLAY)
                .setHoverTooltips("gtceu.machine.parallel_hatch.display"));

        parallelAmountGroup.addWidget(new ButtonWidget(118, 36, 30, 20, (cd) -> {
            int amount = cd.isCtrlClick ? cd.isShiftClick ? 64 : 16 : cd.isShiftClick ? 4 : 1;
            setCurrentParallel(amount);
        }).setHoverTooltips("gui.widget.incrementButton.default_tooltip"));

        parallelAmountGroup.addWidget(new ButtonWidget(29, 36, 30, 20, (cd) -> {
            int amount = cd.isCtrlClick ? cd.isShiftClick ? -64 : -16 : cd.isShiftClick ? -4 : -1;
            setCurrentParallel(amount);
        }).setHoverTooltips("gui.widget.incrementButton.default_tooltip"));

        parallelAmountGroup.addWidget(new TextFieldWidget(63, 42, 51, 20, this::getParallelAmountToString, val -> {
            if (val != null && !val.isEmpty()) {
                setCurrentParallel(Integer.parseInt(val));
            }
        })
                .setNumbersOnly(1, this.maxParallel)
                .setMaxStringLength(3)
                .setValidator(getTextFieldValidator(() -> this.maxParallel)));

        return parallelAmountGroup;
    }


    public String getParallelAmountToString() {
        return Integer.toString(this.currentParallel);
    }



    public static @Nonnull Function<String, String> getTextFieldValidator(IntSupplier maxSupplier) {
        return val -> {
            if (val.isEmpty())
                return String.valueOf(MIN_PARALLEL);
            int max = maxSupplier.getAsInt();
            int num;
            try {
                num = Integer.parseInt(val);
            } catch (NumberFormatException ignored) {
                return String.valueOf(max);
            }
            if (num < MIN_PARALLEL)
                return String.valueOf(MIN_PARALLEL);
            if (num > max)
                return String.valueOf(max);
            return val;
        };
    }

    @Override
    @Nonnull
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }
}

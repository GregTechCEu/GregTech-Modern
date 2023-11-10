package com.gregtechceu.gtceu.common.cover.voiding;

import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.ToggleButtonWidget;
import com.gregtechceu.gtceu.common.cover.PumpCover;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import com.lowdragmc.lowdraglib.side.fluid.IFluidTransfer;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import lombok.Getter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Map;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class FluidVoidingCover extends PumpCover {
    @Persisted @Getter
    protected boolean isEnabled = false;

    public FluidVoidingCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide) {
        super(definition, coverHolder, attachedSide, 0);
    }

    @Override
    protected boolean isSubscriptionActive() {
        return isWorkingEnabled() && isEnabled();
    }

    //////////////////////////////////////////////
    //***********     COVER LOGIC    ***********//
    //////////////////////////////////////////////

    @Override
    protected void update() {
        if (coverHolder.getOffsetTimer() % 5 != 0)
            return;

        doVoidFluids();
        subscriptionHandler.updateSubscription();
    }

    protected void doVoidFluids() {
        IFluidTransfer fluidTransfer = getOwnFluidTransfer();
        if (fluidTransfer == null) {
            return;
        }
        voidAny(fluidTransfer);
    }

    void voidAny(IFluidTransfer fluidTransfer) {
        final Map<FluidStack, Long> fluidAmounts = enumerateDistinctFluids(fluidTransfer, TransferDirection.EXTRACT);

        for (FluidStack fluidStack : fluidAmounts.keySet()) {
            if (!filterHandler.getFilter().test(fluidStack))
                continue;

            var toDrain = fluidStack.copy();
            toDrain.setAmount(fluidAmounts.get(fluidStack));

            fluidTransfer.drain(toDrain, false);
        }
    }

    public void setWorkingEnabled(boolean workingEnabled) {
        isWorkingEnabled = workingEnabled;
        subscriptionHandler.updateSubscription();
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
        subscriptionHandler.updateSubscription();
    }

    //////////////////////////////////////
    //***********     GUI    ***********//
    //////////////////////////////////////

    @Override
    public Widget createUIWidget() {
        final var group = new WidgetGroup(0, 0, 176, 120);
        group.addWidget(new LabelWidget(10, 5, getUITitle()));

        group.addWidget(new ToggleButtonWidget(10, 20, 20, 20,
                GuiTextures.BUTTON_POWER, this::isEnabled, this::setEnabled));

        //group.addWidget(filterHandler.createFilterSlotUI(36, 21));
        group.addWidget(filterHandler.createFilterSlotUI(148, 91));
        group.addWidget(filterHandler.createFilterConfigUI(10, 50, 126, 60));

        buildAdditionalUI(group);

        return group;
    }

    @NotNull
    protected String getUITitle() {
        return "cover.fluid.voiding.title";
    }

    protected void buildAdditionalUI(WidgetGroup group) {
        // Do nothing in the base implementation. This is intended to be overridden by subclasses.
    }

    protected void configureFilter() {
        // Do nothing in the base implementation. This is intended to be overridden by subclasses.
    }

    //////////////////////////////////////
    //*****     LDLib SyncData    ******//
    //////////////////////////////////////

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(FluidVoidingCover.class, PumpCover.MANAGED_FIELD_HOLDER);

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }
}

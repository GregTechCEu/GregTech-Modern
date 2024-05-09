package com.gregtechceu.gtceu.common.covers.voiding;

import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.covers.CoverDefinition;
import com.gregtechceu.gtceu.api.guis.GuiTextures;
import com.gregtechceu.gtceu.api.guis.widget.ToggleButtonWidget;
import com.gregtechceu.gtceu.common.covers.PumpCover;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.side.fluid.IFluidHandlerModifiable;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import lombok.Getter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
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
        IFluidHandlerModifiable fluidTransfer = getOwnFluidTransfer();
        if (fluidTransfer == null) {
            return;
        }
        voidAny(fluidTransfer);
    }

    void voidAny(IFluidHandlerModifiable fluidTransfer) {
        final Map<FluidStack, Integer> fluidAmounts = enumerateDistinctFluids(fluidTransfer, TransferDirection.EXTRACT);

        for (FluidStack fluidStack : fluidAmounts.keySet()) {
            if (!filterHandler.test(fluidStack))
                continue;

            var toDrain = fluidStack.copy();
            toDrain.setAmount(fluidAmounts.get(fluidStack));

            fluidTransfer.drain(toDrain, IFluidHandler.FluidAction.EXECUTE);
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

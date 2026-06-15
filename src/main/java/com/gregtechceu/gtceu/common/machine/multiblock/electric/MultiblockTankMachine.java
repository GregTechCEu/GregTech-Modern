package com.gregtechceu.gtceu.common.machine.multiblock.electric;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.fluids.PropertyFluidFilter;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.TankWidget;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.transfer.fluid.IFluidHandlerModifiable;
import com.gregtechceu.gtceu.utils.ExtendedUseOnContext;

import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class MultiblockTankMachine extends MultiblockControllerMachine implements IFancyUIMachine {

    @SaveField
    @Getter
    private final NotifiableFluidTank tank;

    public MultiblockTankMachine(BlockEntityCreationInfo info, int capacity, @Nullable PropertyFluidFilter filter) {
        super(info);

        this.tank = attachTrait(new NotifiableFluidTank(1, capacity, IO.BOTH));
        if (filter != null) tank.setFilter(filter);
    }

    @Override
    public InteractionResult onUse(ExtendedUseOnContext context) {
        var superResult = super.onUse(context);

        if (superResult != InteractionResult.PASS) return superResult;
        if (!isFormed()) return InteractionResult.FAIL;

        return InteractionResult.PASS; // Otherwise let MetaMachineBlock.use() open the UI
    }

    @Override
    @Nullable
    public IFluidHandlerModifiable getFluidHandlerCap(@Nullable Direction side, boolean useCoverCapability) {
        if (isFormed) {
            return super.getFluidHandlerCap(side, useCoverCapability);
        }
        return null;
    }

    /////////////////////////////////////
    // *********** GUI ***********//
    /////////////////////////////////////

    @Override
    public Widget createUIWidget() {
        var group = new WidgetGroup(0, 0, 90, 63);
        group.setBackground(GuiTextures.BACKGROUND_INVERSE);

        group.addWidget(new ImageWidget(4, 4, 82, 55, GuiTextures.DISPLAY));
        group.addWidget(new LabelWidget(8, 8, "gtceu.gui.fluid_amount"));
        group.addWidget(new LabelWidget(8, 18, this::getFluidLabel).setTextColor(-1).setDropShadow(true));
        group.addWidget(new TankWidget(tank.getStorages()[0], 68, 23, true, true)
                .setBackground(GuiTextures.FLUID_SLOT));

        return group;
    }

    private String getFluidLabel() {
        return String.valueOf(tank.getFluidInTank(0).getAmount());
    }
}

package com.gregtechceu.gtceu.common.cover;

import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.covers.CoverBehavior;
import com.gregtechceu.gtceu.api.covers.CoverDefinition;
import com.gregtechceu.gtceu.api.covers.IUICover;
import com.gregtechceu.gtceu.api.covers.filter.FluidFilter;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.side.fluid.FluidTransferHelper;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * @author KilaBash
 * @date 2023/3/13
 * @implNote ItemFilterCover
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FluidFilterCover extends CoverBehavior implements IUICover {

    protected FluidFilter fluidFilter;

    public FluidFilterCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide) {
        super(definition, coverHolder, attachedSide);
    }

    @Override
    public boolean canAttach() {
        return FluidTransferHelper.getFluidTransfer(coverHolder.getLevel(), coverHolder.getPos(), attachedSide) != null;
    }

    public FluidFilter getFluidFilter() {
        if (fluidFilter == null) {
            fluidFilter = FluidFilter.loadFilter(attachItem);
        }
        return fluidFilter;
    }

    @Override
    public Widget createUIWidget() {
        final var group = new WidgetGroup(0, 0, 176, 80);
        group.addWidget(new LabelWidget(5, 3, attachItem.getDescriptionId()));
        group.addWidget(getFluidFilter().openConfigurator((176 - 80) / 2, (60 - 55) / 2 + 15));
        return group;
    }
}

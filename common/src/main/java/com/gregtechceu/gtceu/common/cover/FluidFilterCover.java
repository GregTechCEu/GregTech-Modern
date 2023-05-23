package com.gregtechceu.gtceu.common.cover;

import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.cover.IUICover;
import com.gregtechceu.gtceu.api.cover.filter.FluidFilter;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.UITemplate;
import com.gregtechceu.gtlib.gui.modular.ModularUI;
import com.gregtechceu.gtlib.gui.widget.LabelWidget;
import com.gregtechceu.gtlib.side.fluid.FluidTransferHelper;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;

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
    public ModularUI createUI(Player entityPlayer) {
        return new ModularUI(176, 157, this, entityPlayer)
                .background(GuiTextures.BACKGROUND)
                .widget(new LabelWidget(5, 3, attachItem.getDescriptionId()))
                .widget(getFluidFilter().openConfigurator((176 - 80) / 2, (60 - 55) / 2 + 15))
                .widget(UITemplate.bindPlayerInventory(entityPlayer.getInventory(),  GuiTextures.SLOT, 7, 75, true));
    }
}

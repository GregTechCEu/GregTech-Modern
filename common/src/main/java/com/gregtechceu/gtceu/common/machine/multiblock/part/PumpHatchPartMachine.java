package com.gregtechceu.gtceu.common.machine.multiblock.part;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.UITemplate;
import com.gregtechceu.gtceu.api.gui.widget.ToggleButtonWidget;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.TankWidget;
import com.lowdragmc.lowdraglib.side.fluid.FluidHelper;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.entity.player.Player;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class PumpHatchPartMachine extends FluidHatchPartMachine {

    public PumpHatchPartMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, 0, IO.OUT, FluidHelper.getBucket(), 1, args);
    }

    @Override
    protected NotifiableFluidTank createTank(long initialCapacity, int slots, Object... args) {
        return super.createTank(initialCapacity, slots).setFilter(fluidStack -> fluidStack.getFluid() == GTMaterials.Water.getFluid());
    }

    @Override
    public ModularUI createUI(Player entityPlayer) {
        return new ModularUI(176, 166, this, entityPlayer)
                .background(GuiTextures.BACKGROUND)
                .widget(new ImageWidget(7, 16, 81, 55, GuiTextures.DISPLAY))
                .widget(new LabelWidget(11, 20, "gtceu.gui.fluid_amount"))
                .widget(new LabelWidget(11, 30, () -> String.valueOf(tank.getFluidInTank(0).getAmount())).setTextColor(-1).setDropShadow(true))
                .widget(new LabelWidget(6, 6, getBlockState().getBlock().getDescriptionId()))
                .widget(new TankWidget(tank.storages[0], 90, 35, true, io.support(IO.IN))
                        .setBackground(GuiTextures.FLUID_SLOT))
                .widget(new ToggleButtonWidget(7, 53, 18, 18,
                        GuiTextures.BUTTON_FLUID_OUTPUT, this::isWorkingEnabled, this::setWorkingEnabled)
                        .setShouldUseBaseBackground()
                        .setTooltipText("gtceu.gui.fluid_auto_input.tooltip"))
                .widget(UITemplate.bindPlayerInventory(entityPlayer.getInventory(), GuiTextures.SLOT, 7, 84, true));
    }
}

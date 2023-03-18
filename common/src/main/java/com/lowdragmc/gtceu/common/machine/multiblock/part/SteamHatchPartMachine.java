package com.lowdragmc.gtceu.common.machine.multiblock.part;

import com.lowdragmc.gtceu.api.capability.recipe.IO;
import com.lowdragmc.gtceu.api.gui.GuiTextures;
import com.lowdragmc.gtceu.api.gui.UITemplate;
import com.lowdragmc.gtceu.api.gui.widget.ToggleButtonWidget;
import com.lowdragmc.gtceu.api.machine.IMetaMachineBlockEntity;
import com.lowdragmc.gtceu.api.machine.feature.IUIMachine;
import com.lowdragmc.gtceu.api.machine.trait.NotifiableFluidTank;
import com.lowdragmc.gtceu.common.libs.GTMaterials;
import com.lowdragmc.gtceu.config.ConfigHolder;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.TankWidget;
import com.lowdragmc.lowdraglib.side.fluid.FluidHelper;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.entity.player.Player;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * @author KilaBash
 * @date 2023/3/4
 * @implNote SteamHatchPartMachine
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SteamHatchPartMachine extends FluidHatchPartMachine implements IUIMachine {
    public static final long INITIAL_TANK_CAPACITY = 64 * FluidHelper.getBucket();
    public static final boolean IS_STEEL = ConfigHolder.machines.steelSteamMultiblocks;

    public SteamHatchPartMachine(IMetaMachineBlockEntity holder, Object... args) {
        super(holder, 0, IO.IN, args);
    }

    @Override
    protected NotifiableFluidTank createTank(Object... args) {
        return super.createTank(args).setFilter(fluidStack -> fluidStack.getFluid() == GTMaterials.Steam.getFluid());
    }

    @Override
    protected long getTankCapacity() {
        return INITIAL_TANK_CAPACITY;
    }

    @Override
    public ModularUI createUI(Player entityPlayer) {
        return new ModularUI(176, 166, this, entityPlayer)
                .background(GuiTextures.BACKGROUND_STEAM.get(IS_STEEL))
                .widget(new ImageWidget(7, 16, 81, 55, GuiTextures.DISPLAY_STEAM.get(IS_STEEL)))
                .widget(new LabelWidget(11, 20, "gtceu.gui.fluid_amount"))
                .widget(new LabelWidget(11, 30, () -> tank.getFluidInTank(0).getAmount() + "").setTextColor(-1).setDropShadow(true))
                .widget(new LabelWidget(6, 6, getBlockState().getBlock().getDescriptionId()))
                .widget(new TankWidget(tank.storages[0], 90, 35, true, true)
                        .setBackground(GuiTextures.FLUID_SLOT))
                .widget(UITemplate.bindPlayerInventory(entityPlayer.getInventory(), GuiTextures.SLOT_STEAM.get(IS_STEEL), 7, 84, true));
    }
}

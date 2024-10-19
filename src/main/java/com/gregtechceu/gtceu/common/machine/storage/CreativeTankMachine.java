package com.gregtechceu.gtceu.common.machine.storage;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.PhantomFluidWidget;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;

import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.ResourceBorderTexture;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.*;
import com.lowdragmc.lowdraglib.syncdata.annotation.DropSaved;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

import org.jetbrains.annotations.NotNull;

public class CreativeTankMachine extends QuantumTankMachine {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(CreativeTankMachine.class,
            QuantumTankMachine.MANAGED_FIELD_HOLDER);

    @Persisted
    @DropSaved
    private int mBPerCycle = 1;
    @Persisted
    @DropSaved
    private int ticksPerCycle = 1;

    public CreativeTankMachine(IMachineBlockEntity holder) {
        super(holder, GTValues.MAX, -1);
    }

    protected NotifiableFluidTank createCacheFluidHandler(Object... args) {
        return new InfiniteTank(this);
    }

    protected void checkAutoOutput() {
        if (getOffsetTimer() % ticksPerCycle == 0) {
            if (isAutoOutputFluids() && getOutputFacingFluids() != null) {
                cache.exportToNearby(getOutputFacingFluids());
            }
            updateAutoOutputSubscription();
        }
    }

    private void updateStored(FluidStack fluid) {
        cache.setFluidInTank(0, new FluidStack(fluid, 1000));
        stored = cache.getFluidInTank(0);
    }

    @Override
    public InteractionResult onUse(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand,
                                   BlockHitResult hit) {
        var heldItem = player.getItemInHand(hand);
        if (hit.getDirection() == getFrontFacing() && !isRemote()) {
            // Clear fluid if empty + shift-rclick
            if (heldItem.isEmpty() && player.isCrouching() && !stored.isEmpty()) {
                updateStored(FluidStack.EMPTY);
                return InteractionResult.SUCCESS;
            }

            // If no fluid set and held-item has fluid, set fluid
            if (stored.isEmpty()) {
                FluidUtil.getFluidContained(heldItem).ifPresent(this::updateStored);
                return InteractionResult.SUCCESS;
            }

            // If held item can take fluid from tank, do it, otherwise set new fluid
            var handler = FluidUtil.getFluidHandler(heldItem).resolve().orElse(null);
            if (handler != null) {
                var copy = stored.copy();
                copy.setAmount(Integer.MAX_VALUE);
                int filled = handler.fill(copy, FluidAction.SIMULATE);
                if (filled > 0) {
                    handler.fill(copy, FluidAction.EXECUTE);
                    player.setItemInHand(hand, handler.getContainer());
                } else updateStored(handler.getFluidInTank(0));
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public WidgetGroup createUIWidget() {
        var group = new WidgetGroup(0, 0, 176, 131);
        group.addWidget(new PhantomFluidWidget(this.cache.getStorages()[0], 0, 36, 6, 18, 18,
                this::getStored, this::updateStored)
                .setShowAmount(false).setBackground(GuiTextures.FLUID_SLOT));
        group.addWidget(new LabelWidget(7, 9, "gtceu.creative.tank.fluid"));
        group.addWidget(new ImageWidget(7, 45, 154, 14, GuiTextures.DISPLAY));
        group.addWidget(new TextFieldWidget(9, 47, 152, 10, () -> String.valueOf(mBPerCycle), value -> {
            if (!value.isEmpty()) {
                mBPerCycle = Integer.parseInt(value);
            }
        }).setMaxStringLength(11).setNumbersOnly(1, Integer.MAX_VALUE));
        group.addWidget(new LabelWidget(7, 28, "gtceu.creative.tank.mbpc"));

        group.addWidget(new ImageWidget(7, 82, 154, 14, GuiTextures.DISPLAY));
        group.addWidget(new TextFieldWidget(9, 84, 152, 10, () -> String.valueOf(ticksPerCycle), value -> {
            if (!value.isEmpty()) {
                ticksPerCycle = Integer.parseInt(value);
            }
        }).setMaxStringLength(11).setNumbersOnly(1, Integer.MAX_VALUE));
        group.addWidget(new LabelWidget(7, 65, "gtceu.creative.tank.tpc"));

        group.addWidget(new SwitchWidget(7, 101, 162, 20, (clickData, value) -> setWorkingEnabled(value))
                .setTexture(
                        new GuiTextureGroup(ResourceBorderTexture.BUTTON_COMMON,
                                new TextTexture("gtceu.creative.activity.off")),
                        new GuiTextureGroup(ResourceBorderTexture.BUTTON_COMMON,
                                new TextTexture("gtceu.creative.activity.on")))
                .setPressed(isWorkingEnabled()));

        return group;
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    private class InfiniteTank extends NotifiableFluidTank {

        public InfiniteTank(MetaMachine holder) {
            super(holder, 1, FluidType.BUCKET_VOLUME, IO.BOTH, IO.BOTH);
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            if (!stored.isEmpty()) return resource.getAmount();
            return 0;
        }

        @Override
        public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
            if (!stored.isEmpty()) return new FluidStack(stored, mBPerCycle);
            return FluidStack.EMPTY;
        }

        @Override
        public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
            if (!stored.isEmpty() && stored.isFluidEqual(resource)) return new FluidStack(resource, mBPerCycle);
            return FluidStack.EMPTY;
        }
    }
}

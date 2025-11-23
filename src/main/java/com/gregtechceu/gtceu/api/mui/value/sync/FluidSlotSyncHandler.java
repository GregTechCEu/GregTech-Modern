package com.gregtechceu.gtceu.api.mui.value.sync;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.misc.forge.FluidTankHandler;
import com.gregtechceu.gtceu.api.mui.utils.MouseData;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.SoundAction;
import net.minecraftforge.common.SoundActions;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.*;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Accessors(fluent = true, chain = true)
public class FluidSlotSyncHandler extends ValueSyncHandler<FluidStack> {

    public static final int SYNC_FLUID = 0;
    public static final int SYNC_CLICK = 1;
    public static final int SYNC_SCROLL = 2;
    public static final int SYNC_CONTROLS_AMOUNT = 3;

    private @NotNull FluidStack cache = FluidStack.EMPTY;
    @Getter
    private final IFluidTank fluidTank;
    private final IFluidHandler fluidHandler;
    @Getter
    @Setter
    private boolean canFillSlot = true, canDrainSlot = true, phantom = false;
    @Getter
    private boolean controlsAmount = true;
    @Nullable
    private FluidStack lastStoredPhantomFluid;

    public FluidSlotSyncHandler(IFluidTank fluidTank) {
        this.fluidTank = fluidTank;
        this.fluidHandler = FluidTankHandler.getTankFluidHandler(fluidTank);
    }

    @Nullable
    @Override
    public FluidStack getValue() {
        return this.cache;
    }

    @Override
    public void setValue(@NotNull FluidStack value, boolean setSource, boolean sync) {
        this.cache = value.copy();
        if (setSource) {
            this.fluidTank.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.EXECUTE);
            if (!value.isEmpty()) {
                this.fluidTank.fill(value.copy(), IFluidHandler.FluidAction.EXECUTE);
            }
        }
        if (sync) {
            if (GTCEu.isClientThread()) {
                syncToServer(SYNC_FLUID, this::write);
            } else {
                syncToClient(SYNC_FLUID, this::write);
            }
        }
        onValueChanged();
    }

    public boolean needsSync() {
        FluidStack current = this.fluidTank.getFluid();
        if (current == this.cache) return false;
        if (current.isEmpty() && this.cache.isEmpty()) return true;
        return !current.isFluidStackIdentical(this.cache);
    }

    @Override
    public boolean updateCacheFromSource(boolean isFirstSync) {
        if (isFirstSync || needsSync()) {
            setValue(this.fluidTank.getFluid(), false, false);
            return true;
        }
        return false;
    }

    @Override
    public void notifyUpdate() {
        setValue(this.fluidTank.getFluid(), false, true);
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        this.cache.writeToPacket(buffer);
    }

    @Override
    public void read(FriendlyByteBuf buffer) {
        setValue(FluidStack.readFromPacket(buffer), true, false);
    }

    @Override
    public void readOnClient(int id, FriendlyByteBuf buf) {
        if (id == SYNC_FLUID) {
            read(buf);
        } else if (id == SYNC_CONTROLS_AMOUNT) {
            this.controlsAmount = buf.readBoolean();
        }
    }

    @Override
    public void readOnServer(int id, FriendlyByteBuf buf) {
        if (id == SYNC_FLUID) {
            if (this.phantom) {
                read(buf);
            }
        } else if (id == SYNC_CLICK) {
            if (this.phantom) {
                tryClickPhantom(MouseData.readPacket(buf));
            } else {
                tryClickContainer(MouseData.readPacket(buf));
            }
        } else if (id == SYNC_SCROLL) {
            if (this.phantom) {
                tryScrollPhantom(MouseData.readPacket(buf));
            }
        } else if (id == SYNC_CONTROLS_AMOUNT) {
            this.controlsAmount = buf.readBoolean();
        }
    }

    private void tryClickContainer(MouseData mouseData) {
        Player player = getSyncManager().getPlayer();
        ItemStack currentStack = player.containerMenu.getCarried();
        if (!currentStack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM, null).isPresent()) {
            return;
        }
        int maxAttempts = mouseData.shift() ? currentStack.getCount() : 1;
        if (mouseData.mouseButton() == 0 && this.canFillSlot) {
            boolean performedTransfer = false;
            for (int i = 0; i < maxAttempts; i++) {
                FluidActionResult result = FluidUtil.tryEmptyContainer(currentStack, this.fluidHandler,
                        Integer.MAX_VALUE, null, false);
                ItemStack remainingStack = result.getResult();
                if (!result.isSuccess() || (currentStack.getCount() > 1 && !remainingStack.isEmpty() &&
                        !player.getInventory().add(remainingStack))) {
                    player.drop(remainingStack, true);
                    break; // do not continue if we can't add resulting container into inventory
                }

                remainingStack = FluidUtil.tryEmptyContainer(currentStack, this.fluidHandler, Integer.MAX_VALUE, null,
                        true).result;
                if (currentStack.getCount() == 1) {
                    currentStack = remainingStack;
                } else {
                    currentStack.shrink(1);
                }
                performedTransfer = true;
                if (currentStack.isEmpty()) {
                    break;
                }
            }
            FluidStack fluid = this.fluidTank.getFluid();
            if (performedTransfer && !fluid.isEmpty()) {
                playSound(fluid, SoundActions.BUCKET_EMPTY);
                getSyncManager().setCursorItem(currentStack);
            }
            return;
        }
        FluidStack currentFluid = this.fluidTank.getFluid();
        if (mouseData.mouseButton() == 1 && this.canDrainSlot && !currentFluid.isEmpty()) {
            boolean performedTransfer = false;
            for (int i = 0; i < maxAttempts; i++) {
                FluidActionResult result = FluidUtil.tryFillContainer(currentStack, this.fluidHandler,
                        Integer.MAX_VALUE, null, false);
                ItemStack remainingStack = result.getResult();
                if (!result.isSuccess() || (currentStack.getCount() > 1 && !remainingStack.isEmpty() &&
                        !player.getInventory().add(remainingStack))) {
                    break; // do not continue if we can't add resulting container into inventory
                }

                remainingStack = FluidUtil.tryFillContainer(currentStack, this.fluidHandler, Integer.MAX_VALUE, null,
                        true).result;
                if (currentStack.getCount() == 1) {
                    currentStack = remainingStack;
                } else {
                    currentStack.shrink(1);
                }
                performedTransfer = true;
                if (currentStack.isEmpty()) {
                    break;
                }
            }
            if (performedTransfer) {
                playSound(currentFluid, SoundActions.BUCKET_FILL);
                getSyncManager().setCursorItem(currentStack);
            }
        }
    }

    public void tryClickPhantom(MouseData mouseData) {
        Player player = getSyncManager().getPlayer();
        ItemStack currentStack = player.containerMenu.getCarried();
        FluidStack currentFluid = this.fluidTank.getFluid();
        IFluidHandlerItem fluidHandlerItem = currentStack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM, null)
                .resolve().orElse(null);

        if (mouseData.mouseButton() == 0) {
            if (currentStack.isEmpty() || fluidHandlerItem == null) {
                if (this.canDrainSlot) {
                    this.fluidTank.drain(mouseData.shift() ? Integer.MAX_VALUE : 1000,
                            IFluidHandler.FluidAction.EXECUTE);
                }
            } else {
                FluidStack cellFluid = fluidHandlerItem.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.SIMULATE);
                if ((this.controlsAmount || currentFluid.isEmpty()) && !cellFluid.isEmpty()) {
                    if (this.canFillSlot) {
                        if (!this.controlsAmount) {
                            cellFluid.setAmount(1);
                        }
                        if (this.fluidTank.fill(cellFluid, IFluidHandler.FluidAction.EXECUTE) > 0) {
                            this.lastStoredPhantomFluid = cellFluid.copy();
                        }
                    }
                } else {
                    if (this.canDrainSlot) {
                        this.fluidTank.drain(mouseData.shift() ? Integer.MAX_VALUE : FluidType.BUCKET_VOLUME,
                                IFluidHandler.FluidAction.EXECUTE);
                    }
                }
            }
        } else if (mouseData.mouseButton() == 1) {
            if (this.canFillSlot) {
                if (!currentFluid.isEmpty()) {
                    if (this.controlsAmount) {
                        FluidStack toFill = currentFluid.copy();
                        toFill.setAmount(FluidType.BUCKET_VOLUME);
                        this.fluidTank.fill(toFill, IFluidHandler.FluidAction.EXECUTE);
                    }
                } else if (this.lastStoredPhantomFluid != null) {
                    FluidStack toFill = this.lastStoredPhantomFluid.copy();
                    toFill.setAmount(this.controlsAmount ? FluidType.BUCKET_VOLUME : 1);
                    this.fluidTank.fill(toFill, IFluidHandler.FluidAction.EXECUTE);
                }
            }
        } else if (mouseData.mouseButton() == 2 && !currentFluid.isEmpty() && this.canDrainSlot) {
            this.fluidTank.drain(mouseData.shift() ? Integer.MAX_VALUE : FluidType.BUCKET_VOLUME,
                    IFluidHandler.FluidAction.EXECUTE);
        }
    }

    public void tryScrollPhantom(MouseData mouseData) {
        FluidStack currentFluid = this.fluidTank.getFluid();
        int amount = mouseData.mouseButton();
        if (mouseData.shift()) {
            amount *= 10;
        }
        if (mouseData.ctrl()) {
            amount *= 100;
        }
        if (mouseData.alt()) {
            amount *= 1000;
        }
        if (currentFluid.isEmpty()) {
            if (amount > 0 && this.lastStoredPhantomFluid != null) {
                FluidStack toFill = this.lastStoredPhantomFluid.copy();
                toFill.setAmount(this.controlsAmount ? amount : 1);
                this.fluidTank.fill(toFill, IFluidHandler.FluidAction.EXECUTE);
            }
            return;
        }
        if (amount > 0 && this.controlsAmount) {
            FluidStack toFill = currentFluid.copy();
            toFill.setAmount(amount);
            this.fluidTank.fill(toFill, IFluidHandler.FluidAction.EXECUTE);
        } else if (amount < 0) {
            this.fluidTank.drain(-amount, IFluidHandler.FluidAction.EXECUTE);
        }
    }

    private void playSound(FluidStack fluid, SoundAction action) {
        Player player = getSyncManager().getPlayer();
        SoundEvent sound = fluid.getFluid().getFluidType().getSound(fluid, action);
        if (sound == null) return;
        player.level().playSound(null, player.getX(), player.getY() + 0.5, player.getZ(), sound, SoundSource.BLOCKS,
                1.0F, 1.0F);
    }

    public FluidSlotSyncHandler controlsAmount(boolean controlsAmount) {
        this.controlsAmount = controlsAmount;
        if (isValid()) {
            sync(SYNC_CONTROLS_AMOUNT, buffer -> buffer.writeBoolean(controlsAmount));
        }
        return this;
    }
}

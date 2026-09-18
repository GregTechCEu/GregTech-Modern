package brachy.modularui.value.sync;

import brachy.modularui.utils.FluidTankHandler;
import brachy.modularui.utils.IMultiFluidTankHandler;
import brachy.modularui.utils.MouseData;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.SoundAction;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.FluidActionResult;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.IFluidTank;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Accessors(fluent = true, chain = true)
public class FluidSlotSyncHandler extends ValueSyncHandler<RegistryFriendlyByteBuf, FluidStack, FluidSlotSyncHandler> {

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
        allowC2S();
    }

    public FluidSlotSyncHandler(IMultiFluidTankHandler fluidTank, int index) {
        this(fluidTank.getFluidTank(index));
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
        onValueChanged();
        if (sync) sync();
    }

    public boolean needsSync() {
        FluidStack current = this.fluidTank.getFluid();
        if (current == this.cache) return false;
        if (current.isEmpty() && this.cache.isEmpty()) return true;
        return !FluidStack.matches(current, this.cache);
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
    public Class<FluidStack> getValueType() {
        return FluidStack.class;
    }

    @Override
    public void notifyUpdate() {
        setValue(this.fluidTank.getFluid(), false, true);
    }

    @Override
    public void write(RegistryFriendlyByteBuf buffer) {
        FluidStack.OPTIONAL_STREAM_CODEC.encode(buffer, this.cache);
    }

    @Override
    public void read(RegistryFriendlyByteBuf buffer) {
        setValue(FluidStack.OPTIONAL_STREAM_CODEC.decode(buffer), true, false);
    }

    @Override
    public void readOnClient(int id, RegistryFriendlyByteBuf buf) {
        if (id == SYNC_VALUE) {
            read(buf);
        } else if (id == SYNC_CONTROLS_AMOUNT) {
            this.controlsAmount = buf.readBoolean();
        }
    }

    @Override
    public void readOnServer(int id, RegistryFriendlyByteBuf buf) {
        if (id == SYNC_VALUE) {
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
            } else {
                tryScroll(MouseData.readPacket(buf));
            }
        } else if (id == SYNC_CONTROLS_AMOUNT) {
            this.controlsAmount = buf.readBoolean();
        }
    }

    private void tryClickContainer(MouseData mouseData) {
        Player player = getSyncManager().getPlayer();
        ItemStack currentStack = player.containerMenu.getCarried();
        if (currentStack.getCapability(Capabilities.FluidHandler.ITEM) == null) {
            return;
        }

        int maxAttempts = mouseData.shift() ? currentStack.getCount() : 1;
        if (mouseData.isLeftMouseButton()) {
            if (!this.canFillSlot || !fillSlot(player, currentStack, maxAttempts, Integer.MAX_VALUE, true)) {
                if (this.canDrainSlot) drainSlot(player, currentStack, maxAttempts, Integer.MAX_VALUE, true);
            }
        } else if (mouseData.isRightMouseButton() && this.canDrainSlot) {
            drainSlot(player, currentStack, maxAttempts, Integer.MAX_VALUE, true);
        }
    }

    private boolean fillSlot(Player player, ItemStack currentStack, int maxAttempts, int maxAmount, boolean playSound) {
        if (maxAmount <= 0) return false;
        boolean performedTransfer = false;
        for (int i = 0; i < maxAttempts; i++) {
            FluidActionResult result = FluidUtil.tryEmptyContainer(currentStack, this.fluidHandler, maxAmount, null, false);
            ItemStack remainingStack = result.getResult();
            if (!result.isSuccess() ||
                    (currentStack.getCount() > 1 && !remainingStack.isEmpty() && !player.getInventory().add(remainingStack))) {
                player.drop(remainingStack, true);
                break; // do not continue if we can't add resulting container into inventory
            }

            remainingStack = FluidUtil.tryEmptyContainer(currentStack, this.fluidHandler, maxAmount, null, true).result;
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
            if (playSound) playSound(player, fluid, SoundActions.BUCKET_EMPTY);
            player.containerMenu.setCarried(currentStack);
        }
        return performedTransfer;
    }

    private boolean drainSlot(Player player, ItemStack currentStack, int maxAttempts, int maxAmount, boolean playSound) {
        FluidStack currentFluid = this.fluidTank.getFluid();
        if (currentFluid.isEmpty() || maxAmount <= 0) return false;
        boolean performedTransfer = false;
        for (int i = 0; i < maxAttempts; i++) {
            FluidActionResult result = FluidUtil.tryFillContainer(currentStack, this.fluidHandler, maxAmount, null, false);
            ItemStack remainingStack = result.getResult();
            if (!result.isSuccess() ||
                    (currentStack.getCount() > 1 && !remainingStack.isEmpty() && !player.getInventory().add(remainingStack))) {
                break; // do not continue if we can't add resulting container into inventory
            }

            remainingStack = FluidUtil.tryFillContainer(currentStack, this.fluidHandler, maxAmount, playSound ? player : null, true).result;
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
            if (playSound) playSound(player, currentFluid, SoundActions.BUCKET_FILL);
            player.containerMenu.setCarried(currentStack);
        }
        return performedTransfer;
    }

    public void tryClickPhantom(MouseData mouseData) {
        Player player = getSyncManager().getPlayer();
        ItemStack currentStack = player.containerMenu.getCarried();
        FluidStack currentFluid = this.fluidTank.getFluid();
        IFluidHandlerItem fluidHandlerItem = currentStack.getCapability(Capabilities.FluidHandler.ITEM);

        if (mouseData.isLeftMouseButton()) {
            if (this.canFillSlot) {
                // set slot to fluid of tank item
                if (fluidHandlerItem != null) {
                    FluidStack cellFluid = fluidHandlerItem.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.SIMULATE);
                    if (!cellFluid.isEmpty()) {
                        if (FluidStack.isSameFluidSameComponents(cellFluid, currentFluid)) {
                            // if it already contains the same fluid increase the amount by the cells amount
                            if (this.controlsAmount) {
                                currentFluid.grow(cellFluid.getAmount());
                            }
                            return;
                        }
                        cellFluid = cellFluid.copy();
                        if (!this.controlsAmount) cellFluid.setAmount(1);
                        if (this.fluidTank.fill(cellFluid, IFluidHandler.FluidAction.EXECUTE) > 0) {
                            this.lastStoredPhantomFluid = cellFluid.copy();
                        }
                        // only play sound when setting a new fluid
                        playSound(player, cellFluid, SoundActions.BUCKET_FILL);
                        return;
                    }
                }
                if (!currentFluid.isEmpty()) {
                    // increase fluid amount by a bucket
                    if (this.controlsAmount) {
                        FluidStack toFill = currentFluid.copy();
                        toFill.setAmount(FluidType.BUCKET_VOLUME);
                        this.fluidTank.fill(toFill, IFluidHandler.FluidAction.EXECUTE);
                    }
                } else if (this.lastStoredPhantomFluid != null) {
                    // slot is empty and try to use last stored phantom fluid
                    FluidStack toFill = this.lastStoredPhantomFluid.copy();
                    toFill.setAmount(this.controlsAmount ? FluidType.BUCKET_VOLUME : 1);
                    this.fluidTank.fill(toFill, IFluidHandler.FluidAction.EXECUTE);
                    // only play sound when setting a new fluid
                    playSound(player, toFill, SoundActions.BUCKET_FILL);
                }
                return;
            }
            if (this.canDrainSlot) {
                // drain if this can't fill on left click
                drainSlotPhantom(mouseData.shift(), player, currentFluid, fluidHandlerItem);
            }
            return;
        }
        if (mouseData.isRightMouseButton() && this.canDrainSlot) {
            drainSlotPhantom(mouseData.shift(), player, currentFluid, fluidHandlerItem);
        }
    }

    private void drainSlotPhantom(boolean shift, Player player, FluidStack currentFluid, IFluidHandlerItem fluidHandlerItem) {
        currentFluid = currentFluid.copy();
        if (this.controlsAmount && !shift) {
            if (fluidHandlerItem != null) {
                // use the fluid cells fluid amount if it has the same fluid
                FluidStack cellFluid = fluidHandlerItem.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.SIMULATE);
                if (!cellFluid.isEmpty() && cellFluid.isFluidEqual(currentFluid)) {
                    this.fluidTank.drain(cellFluid.getAmount(), IFluidHandler.FluidAction.EXECUTE);
                    if (this.fluidTank.getFluid().isEmpty()) {
                        // only play sound when setting a new fluid
                        playSound(player, currentFluid, SoundActions.BUCKET_EMPTY);
                    }
                    return;
                }
            }
            this.fluidTank.drain(FluidType.BUCKET_VOLUME, IFluidHandler.FluidAction.EXECUTE);
        } else {
            this.fluidTank.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.EXECUTE);
        }
        if (this.fluidTank.getFluid().isEmpty()) {
            // only play sound when setting a new fluid
            playSound(player, currentFluid, SoundActions.BUCKET_EMPTY);
        }
    }

    public void tryScroll(MouseData mouseData) {
        Player player = getSyncManager().getPlayer();
        ItemStack currentStack = player.containerMenu.getCarried();
        if (currentStack.getCount() != 1) return;
        if (currentStack.getCapability(Capabilities.FluidHandler.ITEM) == null) return;

        int amount = 1;
        if (mouseData.shift()) amount *= 10;
        if (mouseData.ctrl()) amount *= 100;
        if (mouseData.alt()) amount *= 1000;

        if (mouseData.isScrollUp()) {
            fillSlot(player, currentStack, 1, amount, false);
        } else if (mouseData.isScrollDown()) {
            drainSlot(player, currentStack, 1, amount, false);
        }
    }

    public void tryScrollPhantom(MouseData mouseData) {
        FluidStack currentFluid = this.fluidTank.getFluid();
        int amount = mouseData.mouseButton();
        if (mouseData.shift()) amount *= 10;
        if (mouseData.ctrl()) amount *= 100;
        if (mouseData.alt()) amount *= 1000;
        if (currentFluid.isEmpty()) {
            if (amount > 0 && this.lastStoredPhantomFluid != null) {
                FluidStack toFill = this.lastStoredPhantomFluid.copy();
                toFill.setAmount(this.controlsAmount ? amount : 1);
                this.fluidTank.fill(toFill, IFluidHandler.FluidAction.EXECUTE);
                playSound(getSyncManager().getPlayer(), toFill, SoundActions.BUCKET_EMPTY);
            }
            return;
        }
        if (amount > 0 && this.controlsAmount) {
            FluidStack toFill = currentFluid.copy();
            toFill.setAmount(amount);
            this.fluidTank.fill(toFill, IFluidHandler.FluidAction.EXECUTE);
        } else if (amount < 0) {
            currentFluid = currentFluid.copy();
            this.fluidTank.drain(-amount, IFluidHandler.FluidAction.EXECUTE);
            if (this.fluidTank.getFluid().isEmpty()) {
                playSound(getSyncManager().getPlayer(), currentFluid, SoundActions.BUCKET_EMPTY);
            }
        }
    }

    public void playSound(Player player, FluidStack fluid, SoundAction action) {
        SoundEvent sound = fluid.getFluid().getFluidType().getSound(fluid, action);
        if (sound == null) return;
        // on client, it needs the player to play the sound
        // on server the player is an exception
        player.level().playSound(getSyncManager().isClient() ? player : null, player.getX(), player.getY() + 0.5, player.getZ(), sound,
                SoundSource.BLOCKS, 1.0F, 1.0F);
    }

    public FluidSlotSyncHandler controlsAmount(boolean controlsAmount) {
        this.controlsAmount = controlsAmount;
        if (isValid()) {
            sync(SYNC_CONTROLS_AMOUNT, buffer -> buffer.writeBoolean(controlsAmount));
        }
        return this;
    }
}

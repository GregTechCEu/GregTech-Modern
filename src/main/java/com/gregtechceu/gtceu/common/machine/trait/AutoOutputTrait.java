package com.gregtechceu.gtceu.common.machine.trait;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.trait.*;
import com.gregtechceu.gtceu.api.machine.trait.feature.IFrontFacingTrait;
import com.gregtechceu.gtceu.api.machine.trait.feature.IInteractionTrait;
import com.gregtechceu.gtceu.api.machine.trait.feature.IRenderingTrait;
import com.gregtechceu.gtceu.api.sync_system.annotations.RerenderOnChanged;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.common.item.tool.behavior.ToolModeSwitchBehavior;
import com.gregtechceu.gtceu.utils.ExtendedUseOnContext;
import com.gregtechceu.gtceu.utils.GTTransferUtils;
import com.gregtechceu.gtceu.utils.ISubscription;

import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;

import com.mojang.datafixers.util.Pair;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import static com.gregtechceu.gtceu.api.item.tool.ToolHelper.getBehaviorsTag;

public class AutoOutputTrait extends MachineTrait implements IRenderingTrait, IInteractionTrait, IFrontFacingTrait {

    public static final MachineTraitType<AutoOutputTrait> TYPE = new MachineTraitType<>(AutoOutputTrait.class);

    @Getter
    protected final List<IItemHandler> itemHandlers;
    @Getter
    protected final List<IFluidHandler> fluidHandlers;

    @SaveField
    @SyncToClient
    @RerenderOnChanged
    protected @Nullable Direction itemOutputDirection = Direction.UP, fluidOutputDirection = Direction.UP;
    @Getter
    @SaveField
    @SyncToClient
    @RerenderOnChanged
    protected boolean autoOutputItems = false;
    @Getter
    @SaveField
    @SyncToClient
    @RerenderOnChanged
    protected boolean autoOutputFluids = false;
    @Setter
    @SaveField
    protected boolean allowItemInputFromOutputSide = false;
    @Setter
    @SaveField
    protected boolean allowFluidInputFromOutputSide = false;

    @Setter
    @Getter
    protected int ticksPerCycle = 5;
    @Setter
    protected Predicate<@Nullable Direction> itemOutputDirectionValidator = $ -> true;
    @Setter
    protected Predicate<@Nullable Direction> fluidOutputDirectionValidator = $ -> true;
    protected @Nullable TickableSubscription itemOutputSub, fluidOutputSub;
    protected List<ISubscription> itemSubs = new ArrayList<>();
    protected List<ISubscription> fluidSubs = new ArrayList<>();
    private final boolean useDefaultToolHandlers;

    public AutoOutputTrait(List<IItemHandler> itemHandlers, List<IFluidHandler> fluidHandlers,
                           boolean useDefaultToolHandlers) {
        super();

        this.itemHandlers = itemHandlers.stream().filter(h -> {
            if (h.getSlots() == 0) return false;
            if (h instanceof ICapabilityTrait cap) return cap.canCapOutput();
            return true;
        }).toList();
        this.fluidHandlers = fluidHandlers.stream().filter(h -> {
            if (h.getTanks() == 0) return false;
            if (h instanceof ICapabilityTrait cap) return cap.canCapOutput();
            return true;
        }).toList();
        this.useDefaultToolHandlers = useDefaultToolHandlers;
    }

    public AutoOutputTrait(List<IItemHandler> itemHandlers, List<IFluidHandler> fluidHandlers) {
        this(itemHandlers, fluidHandlers, true);
    }

    @Override
    public MachineTraitType<AutoOutputTrait> getTraitType() {
        return TYPE;
    }

    public static AutoOutputTrait ofItems(IItemHandler... itemHandlers) {
        return new AutoOutputTrait(Arrays.asList(itemHandlers), List.of());
    }

    public static AutoOutputTrait ofFluids(IFluidHandler... fluidHandlers) {
        return new AutoOutputTrait(List.of(), Arrays.asList(fluidHandlers));
    }

    @Override
    public void onMachineLoad() {
        super.onMachineLoad();

        this.itemOutputDirection = getMachine().hasFrontFacing() ? getMachine().getFrontFacing().getOpposite() :
                Direction.UP;
        this.fluidOutputDirection = itemOutputDirection;

        if (getLevel() instanceof ServerLevel serverLevel) {
            serverLevel.getServer().tell(new TickTask(0, this::updateFluidOutputSubscription));
            serverLevel.getServer().tell(new TickTask(0, this::updateItemOutputSubscription));
        }
        for (var handler : itemHandlers) {
            if (handler instanceof NotifiableItemStackHandler notifiable)
                itemSubs.add(notifiable.addChangedListener(this::updateItemOutputSubscription));
        }

        for (var handler : fluidHandlers) {
            if (handler instanceof NotifiableFluidTank notifiable)
                fluidSubs.add(notifiable.addChangedListener(this::updateFluidOutputSubscription));
        }
    }

    @Override
    public void onMachineUnload() {
        if (itemOutputSub != null) {
            itemOutputSub.unsubscribe();
            itemOutputSub = null;
        }
        if (fluidOutputSub != null) {
            fluidOutputSub.unsubscribe();
            fluidOutputSub = null;
        }
        itemSubs.forEach(ISubscription::unsubscribe);
        itemSubs.clear();
        fluidSubs.forEach(ISubscription::unsubscribe);
        fluidSubs.clear();
        super.onMachineUnload();
    }

    @Override
    public void onMachineNeighborChanged(Block block, BlockPos fromPos, boolean isMoving) {
        updateItemOutputSubscription();
        updateFluidOutputSubscription();
    }

    public boolean supportsAutoOutputItems() {
        return !itemHandlers.isEmpty();
    }

    public boolean supportsAutoOutputFluids() {
        return !fluidHandlers.isEmpty();
    }

    public @Nullable Direction getItemOutputDirection() {
        return supportsAutoOutputItems() ? itemOutputDirection : null;
    }

    public @Nullable Direction getFluidOutputDirection() {
        return supportsAutoOutputFluids() ? fluidOutputDirection : null;
    }

    public boolean allowsItemInputFromOutputSide() {
        return allowItemInputFromOutputSide;
    }

    public boolean allowsFluidInputFromOutputSide() {
        return allowFluidInputFromOutputSide;
    }

    public void setAllowAutoOutputItems(boolean allow) {
        if (supportsAutoOutputItems()) {
            this.autoOutputItems = allow;
            syncDataHolder.markClientSyncFieldDirty("autoOutputItems");
            updateItemOutputSubscription();
        }
    }

    public void setAllowAutoOutputFluids(boolean allow) {
        if (supportsAutoOutputFluids()) {
            this.autoOutputFluids = allow;
            syncDataHolder.markClientSyncFieldDirty("autoOutputFluids");
            updateFluidOutputSubscription();
        }
    }

    public void setFluidOutputDirection(@Nullable Direction outputFacing) {
        if (supportsAutoOutputFluids()) {
            if (!fluidOutputDirectionValidator.test(outputFacing) ||
                    (getMachine().hasFrontFacing() && getMachine().getFrontFacing() == outputFacing))
                return;
            this.fluidOutputDirection = outputFacing;
            syncDataHolder.markClientSyncFieldDirty("outputFacingFluids");
            updateFluidOutputSubscription();
        }
    }

    public void setItemOutputDirection(@Nullable Direction outputFacing) {
        if (supportsAutoOutputItems()) {
            if (!itemOutputDirectionValidator.test(outputFacing) ||
                    (getMachine().hasFrontFacing() && getMachine().getFrontFacing() == outputFacing))
                return;
            this.itemOutputDirection = outputFacing;
            syncDataHolder.markClientSyncFieldDirty("outputFacingItems");
            updateItemOutputSubscription();
        }
    }

    private boolean shouldKeepItemSubscription() {
        if (!supportsAutoOutputItems()) return false;

        if (!isAutoOutputItems() || getItemOutputDirection() == null ||
                !GTTransferUtils.hasAdjacentItemHandler(getLevel(), getBlockPos(), getItemOutputDirection()))
            return false;
        return true;
    }

    private boolean shouldKeepFluidSubscription() {
        if (!supportsAutoOutputFluids()) return false;
        if (!isAutoOutputFluids() || getFluidOutputDirection() == null ||
                !GTTransferUtils.hasAdjacentFluidHandler(getLevel(), getBlockPos(), getFluidOutputDirection()))
            return false;
        return true;
    }

    protected void updateItemOutputSubscription() {
        if (shouldKeepItemSubscription()) {
            itemOutputSub = subscribeServerTick(itemOutputSub, this::autoOutputItems);
        } else if (itemOutputSub != null) {
            itemOutputSub.unsubscribe();
            itemOutputSub = null;
        }
    }

    protected void updateFluidOutputSubscription() {
        if (shouldKeepFluidSubscription()) {
            fluidOutputSub = subscribeServerTick(fluidOutputSub, this::autoOutputFluids);
        } else if (fluidOutputSub != null) {
            fluidOutputSub.unsubscribe();
            fluidOutputSub = null;
        }
    }

    protected void autoOutputItems() {
        if (getMachine().getOffsetTimer() % ticksPerCycle == 0 && getItemOutputDirection() != null) {
            itemHandlers.forEach(this::exportItemToNearby);
        }
        updateItemOutputSubscription();
    }

    protected void autoOutputFluids() {
        if (getMachine().getOffsetTimer() % ticksPerCycle == 0 && getFluidOutputDirection() != null) {
            fluidHandlers.forEach(this::exportFluidToNearby);
        }
        updateFluidOutputSubscription();
    }

    private void exportFluidToNearby(IFluidHandler handler) {
        var filter = getMachine().getFluidCapFilter(getFluidOutputDirection(), IO.OUT);
        GTTransferUtils.getAdjacentFluidHandler(getLevel(), getBlockPos(), getFluidOutputDirection())
                .ifPresent(adj -> GTTransferUtils.transferFluidsFiltered(handler, adj, filter));
    }

    private void exportItemToNearby(IItemHandler handler) {
        var filter = getMachine().getItemCapFilter(getItemOutputDirection(), IO.OUT);
        GTTransferUtils.getAdjacentItemHandler(getLevel(), getBlockPos(), getItemOutputDirection())
                .ifPresent(adj -> GTTransferUtils.transferItemsFiltered(handler, adj, filter));
    }

    @Override
    public boolean isValidFrontFace(Direction direction) {
        return direction != getItemOutputDirection() && direction != getFluidOutputDirection();
    }

    @Override
    public boolean shouldRenderGridOverlay(Player player, BlockPos pos, BlockState state, ItemStack held,
                                           Set<GTToolType> toolTypes) {
        return toolTypes.contains(GTToolType.SCREWDRIVER) || toolTypes.contains(GTToolType.WRENCH);
    }

    @Override
    public @Nullable ResourceTexture getGridOverlayIcon(Player player, BlockPos pos, BlockState state,
                                                        Set<GTToolType> toolTypes, Direction side) {
        if (toolTypes.contains(GTToolType.WRENCH)) {
            if (!player.isShiftKeyDown()) {
                if (!getMachine().hasFrontFacing() || side != getMachine().getFrontFacing()) {
                    var canSwitchItemOutputToSide = supportsAutoOutputItems() &&
                            itemOutputDirectionValidator.test(side) && side != getItemOutputDirection();
                    var canSwitchFluidOutputToSide = supportsAutoOutputFluids() &&
                            fluidOutputDirectionValidator.test(side) && side != getFluidOutputDirection();
                    if (canSwitchItemOutputToSide || canSwitchFluidOutputToSide)
                        return GuiTextures.TOOL_IO_FACING_ROTATION;
                }
            }
        }
        if (toolTypes.contains(GTToolType.SCREWDRIVER)) {
            if (side == getItemOutputDirection() || side == getFluidOutputDirection()) {
                if (player.isShiftKeyDown()) return GuiTextures.TOOL_ALLOW_INPUT;
                return GuiTextures.TOOL_AUTO_OUTPUT;
            }
        }
        return null;
    }

    @Override
    public Pair<GTToolType, InteractionResult> onToolClick(ExtendedUseOnContext context) {
        var toolType = context.getToolType();
        if (useDefaultToolHandlers) {
            if (toolType.contains(GTToolType.WRENCH)) {
                return Pair.of(GTToolType.WRENCH, onWrenchClick(context));
            }
            if (toolType.contains(GTToolType.SCREWDRIVER)) {
                return Pair.of(GTToolType.SCREWDRIVER, onScrewdriverClick(context));
            }
        }
        return IInteractionTrait.super.onToolClick(context);
    }

    private InteractionResult onWrenchClick(ExtendedUseOnContext context) {
        var itemStack = context.getItemInHand();
        var gridSide = context.getGridSide();

        var tagCompound = getBehaviorsTag(itemStack);
        ToolModeSwitchBehavior.WrenchModeType type = ToolModeSwitchBehavior.WrenchModeType.VALUES[tagCompound
                .getByte("Mode")];

        boolean hasChanged = false;
        if (type.isItem()) {
            if ((!getMachine().hasFrontFacing() || gridSide != getMachine().getFrontFacing()) &&
                    itemOutputDirectionValidator.test(gridSide)) {
                setItemOutputDirection(gridSide);
                hasChanged = true;
            }
        }
        if (type.isFluid()) {
            if ((!getMachine().hasFrontFacing() || gridSide != getMachine().getFrontFacing()) &&
                    fluidOutputDirectionValidator.test(gridSide)) {
                setFluidOutputDirection(gridSide);
                hasChanged = true;
            }
        }
        return hasChanged ? InteractionResult.sidedSuccess(isRemote()) : InteractionResult.PASS;
    }

    private InteractionResult onScrewdriverClick(ExtendedUseOnContext context) {
        var player = context.getPlayer();
        var gridSide = context.getGridSide();

        boolean hasChanged = false;
        if (player.isShiftKeyDown()) {
            if (getItemOutputDirection() == gridSide) {
                setAllowItemInputFromOutputSide(!allowsItemInputFromOutputSide());
                player.displayClientMessage(Component
                        .translatable("gtceu.machine.basic.input_from_output_side." +
                                (allowsItemInputFromOutputSide() ? "allow" : "disallow"))
                        .append(Component.translatable("gtceu.creative.chest.item")), true);
                hasChanged = true;
            }

            if (getFluidOutputDirection() == gridSide) {
                setAllowFluidInputFromOutputSide(!allowsFluidInputFromOutputSide());
                player.displayClientMessage(Component
                        .translatable("gtceu.machine.basic.input_from_output_side." +
                                (allowsFluidInputFromOutputSide() ? "allow" : "disallow"))
                        .append(Component.translatable("gtceu.creative.tank.fluid")), true);
                hasChanged = true;
            }

        } else {
            if (getItemOutputDirection() == gridSide) {
                setAllowAutoOutputItems(!isAutoOutputItems());
                hasChanged = true;
            }
            if (getFluidOutputDirection() == gridSide) {
                setAllowAutoOutputFluids(!isAutoOutputFluids());
                hasChanged = true;
            }
        }
        return hasChanged ? InteractionResult.sidedSuccess(player.level().isClientSide) : InteractionResult.PASS;
    }
}

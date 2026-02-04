package com.gregtechceu.gtceu.api.machine.trait;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.trait.feature.IFrontFacingTrait;
import com.gregtechceu.gtceu.api.machine.trait.feature.IInteractionTrait;
import com.gregtechceu.gtceu.api.machine.trait.feature.IRenderingTrait;
import com.gregtechceu.gtceu.api.sync_system.annotations.RerenderOnChanged;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.common.item.tool.behavior.ToolModeSwitchBehavior;
import com.gregtechceu.gtceu.utils.GTTransferUtils;
import com.gregtechceu.gtceu.utils.ISubscription;

import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
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

    public static MachineTraitType<AutoOutputTrait> TYPE = new MachineTraitType<>(AutoOutputTrait.class);

    @Override
    public MachineTraitType<AutoOutputTrait> getTraitType() {
        return TYPE;
    }

    @Getter
    protected final List<IItemHandler> itemHandlers;
    @Getter
    protected final List<IFluidHandler> fluidHandlers;

    @SaveField
    @SyncToClient
    @RerenderOnChanged
    protected @Nullable Direction itemOutputDirection, fluidOutputDirection;
    @Getter
    @SaveField
    @SyncToClient
    @RerenderOnChanged
    protected boolean autoOutputItems, autoOutputFluids = false;
    @Setter
    @SaveField
    protected boolean allowItemInputFromOutputSide, allowFluidInputFromOutputSide = false;

    @Setter
    @Getter
    protected int ticksPerCycle = 5;
    @Setter
    protected Predicate<@Nullable Direction> itemOutputDirectionValidator, fluidOutputDirectionValidator = $ -> true;
    @Setter
    protected boolean neverAllowInputFromOutputSide = false;
    protected @Nullable TickableSubscription outputSub;
    protected List<ISubscription> itemSubs, fluidSubs;

    public AutoOutputTrait(MetaMachine machine, List<IItemHandler> itemHandlers, List<IFluidHandler> fluidHandlers) {
        super(machine);

        this.itemOutputDirection = machine.hasFrontFacing() ? machine.getFrontFacing().getOpposite() : Direction.UP;
        this.fluidOutputDirection = itemOutputDirection;

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

        itemSubs = new ArrayList<>();
        fluidSubs = new ArrayList<>();
    }

    public static AutoOutputTrait ITEMS(MetaMachine machine, IItemHandler... itemHandlers) {
        return new AutoOutputTrait(machine, Arrays.stream(itemHandlers).toList(), List.of());
    }

    public static AutoOutputTrait FLUIDS(MetaMachine machine, IFluidHandler... fluidHandlers) {
        return new AutoOutputTrait(machine, List.of(), Arrays.stream(fluidHandlers).toList());
    }

    @Override
    public void onMachineLoad() {
        super.onMachineLoad();
        if (getLevel() instanceof ServerLevel serverLevel) {
            serverLevel.getServer().tell(new TickTask(0, this::updateAutoOutputSubscription));
        }
        for (var handler : itemHandlers) {
            if (handler instanceof NotifiableItemStackHandler notifiable)
                itemSubs.add(notifiable.addChangedListener(this::updateAutoOutputSubscription));
        }

        for (var handler : fluidHandlers) {
            if (handler instanceof NotifiableFluidTank notifiable)
                fluidSubs.add(notifiable.addChangedListener(this::updateAutoOutputSubscription));
        }
    }

    @Override
    public void onMachineUnload() {
        if (outputSub != null) {
            outputSub.unsubscribe();
            outputSub = null;
        }
        itemSubs.forEach(ISubscription::unsubscribe);
        itemSubs.clear();
        fluidSubs.forEach(ISubscription::unsubscribe);
        fluidSubs.clear();
        super.onMachineUnload();
    }

    @Override
    public void onMachineNeighborChanged(Block block, BlockPos fromPos, boolean isMoving) {
        updateAutoOutputSubscription();
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

    public boolean isAllowItemInputFromOutputSide() {
        return !neverAllowInputFromOutputSide && allowItemInputFromOutputSide;
    }

    public boolean isAllowFluidInputFromOutputSide() {
        return !neverAllowInputFromOutputSide && allowFluidInputFromOutputSide;
    }

    public void setAutoOutputItems(boolean allow) {
        if (supportsAutoOutputItems()) {
            this.autoOutputItems = allow;
            syncDataHolder.markClientSyncFieldDirty("autoOutputItems");
            updateAutoOutputSubscription();
        }
    }

    public void setAutoOutputFluids(boolean allow) {
        if (supportsAutoOutputFluids()) {
            this.autoOutputFluids = allow;
            syncDataHolder.markClientSyncFieldDirty("autoOutputFluids");
            updateAutoOutputSubscription();
        }
    }

    public void setFluidOutputDirection(@Nullable Direction outputFacing) {
        if (supportsAutoOutputFluids()) {
            if (!fluidOutputDirectionValidator.test(outputFacing) ||
                    (machine.hasFrontFacing() && machine.getFrontFacing() == outputFacing))
                return;
            this.fluidOutputDirection = outputFacing;
            syncDataHolder.markClientSyncFieldDirty("outputFacingFluids");
            updateAutoOutputSubscription();
        }
    }

    public void setItemOutputDirection(@Nullable Direction outputFacing) {
        if (supportsAutoOutputItems()) {
            if (!itemOutputDirectionValidator.test(outputFacing) ||
                    (machine.hasFrontFacing() && machine.getFrontFacing() == outputFacing))
                return;
            this.itemOutputDirection = outputFacing;
            syncDataHolder.markClientSyncFieldDirty("outputFacingItems");
            updateAutoOutputSubscription();
        }
    }

    private boolean shouldKeepSubscription() {
        if (!supportsAutoOutputItems() && !supportsAutoOutputFluids()) return false;

        if (!isAutoOutputItems() || getItemOutputDirection() == null ||
                !GTTransferUtils.hasAdjacentItemHandler(getLevel(), machine.getBlockPos(), getItemOutputDirection()))
            return false;
        if (!isAutoOutputFluids() || getFluidOutputDirection() == null ||
                !GTTransferUtils.hasAdjacentFluidHandler(getLevel(), machine.getBlockPos(), getFluidOutputDirection()))
            return false;

        return true;
    }

    protected void updateAutoOutputSubscription() {
        if (shouldKeepSubscription()) {
            outputSub = machine.subscribeServerTick(outputSub, this::autoOutput);
        } else if (outputSub != null) {
            outputSub.unsubscribe();
            outputSub = null;
        }
    }

    protected void autoOutput() {
        if (machine.getOffsetTimer() % ticksPerCycle == 0) {
            if (isAutoOutputFluids() && getFluidOutputDirection() != null) {
                fluidHandlers.forEach(this::exportFluidToNearby);
            }
            if (isAutoOutputItems() && getItemOutputDirection() != null) {
                itemHandlers.forEach(this::exportItemToNearby);
            }
        }
        updateAutoOutputSubscription();
    }

    private void exportFluidToNearby(IFluidHandler handler) {
        var filter = getMachine().getFluidCapFilter(getFluidOutputDirection(), IO.OUT);
        GTTransferUtils.getAdjacentFluidHandler(getLevel(), machine.getBlockPos(), getFluidOutputDirection())
                .ifPresent(adj -> GTTransferUtils.transferFluidsFiltered(handler, adj, filter));
    }

    private void exportItemToNearby(IItemHandler handler) {
        var filter = getMachine().getItemCapFilter(getItemOutputDirection(), IO.OUT);
        GTTransferUtils.getAdjacentItemHandler(getLevel(), machine.getBlockPos(), getItemOutputDirection())
                .ifPresent(adj -> GTTransferUtils.transferItemsFiltered(handler, adj, filter));
    }

    @Override
    public boolean isValidFrontFace(Direction direction) {
        return direction != getItemOutputDirection() && direction != getFluidOutputDirection();
    }

    @Override
    public boolean shouldRenderGridOverlay(Player player, BlockPos pos, BlockState state, ItemStack held,
                                           Set<GTToolType> toolTypes) {
        return toolTypes.contains(GTToolType.SCREWDRIVER);
    }

    @Override
    public @Nullable ResourceTexture getGridOverlayIcon(Player player, BlockPos pos, BlockState state,
                                                        Set<GTToolType> toolTypes, Direction side) {
        if (toolTypes.contains(GTToolType.WRENCH)) {
            if (!player.isShiftKeyDown()) {
                if (!machine.hasFrontFacing() || side != machine.getFrontFacing()) {
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
    public Pair<GTToolType, InteractionResult> onToolClick(Set<GTToolType> toolType, Player player,
                                                           InteractionHand hand, Direction gridSide,
                                                           BlockHitResult hitResult) {
        if (toolType.contains(GTToolType.WRENCH)) {
            return Pair.of(GTToolType.WRENCH, onWrenchClick(player, hand, gridSide, hitResult));
        }
        if (toolType.contains(GTToolType.SCREWDRIVER)) {
            return Pair.of(GTToolType.SCREWDRIVER, onScrewdriverClick(player, hand, gridSide, hitResult));
        }

        return IInteractionTrait.super.onToolClick(toolType, player, hand, gridSide, hitResult);
    }

    private InteractionResult onWrenchClick(Player player, InteractionHand hand, Direction gridSide,
                                            BlockHitResult hitResult) {
        var itemStack = player.getItemInHand(hand);
        var tagCompound = getBehaviorsTag(itemStack);
        ToolModeSwitchBehavior.WrenchModeType type = ToolModeSwitchBehavior.WrenchModeType.values()[tagCompound
                .getByte("Mode")];

        if (type.isItem()) {
            if ((!machine.hasFrontFacing() || gridSide != machine.getFrontFacing()) &&
                    itemOutputDirectionValidator.test(gridSide)) {
                setItemOutputDirection(gridSide);
            }
        }
        if (type.isFluid()) {
            if ((!machine.hasFrontFacing() || gridSide != machine.getFrontFacing()) &&
                    fluidOutputDirectionValidator.test(gridSide)) {
                setFluidOutputDirection(gridSide);
            }
        }
        return InteractionResult.sidedSuccess(machine.isRemote());
    }

    private InteractionResult onScrewdriverClick(Player player, InteractionHand hand, Direction gridSide,
                                                 BlockHitResult hitResult) {
        if (player.isShiftKeyDown()) {
            if (neverAllowInputFromOutputSide) return InteractionResult.PASS;
            if (getItemOutputDirection() == gridSide) {
                setAllowItemInputFromOutputSide(!isAllowItemInputFromOutputSide());
                player.displayClientMessage(Component
                        .translatable("gtceu.machine.basic.input_from_output_side." +
                                (isAllowItemInputFromOutputSide() ? "allow" : "disallow"))
                        .append(Component.translatable("gtceu.creative.chest.item")), true);
            }

            if (getFluidOutputDirection() == gridSide) {
                setAllowFluidInputFromOutputSide(!isAllowFluidInputFromOutputSide());
                player.displayClientMessage(Component
                        .translatable("gtceu.machine.basic.input_from_output_side." +
                                (isAllowFluidInputFromOutputSide() ? "allow" : "disallow"))
                        .append(Component.translatable("gtceu.creative.tank.fluid")), true);
            }

        } else {
            if (getItemOutputDirection() == gridSide) {
                setAutoOutputItems(!isAutoOutputItems());
            }
            if (getFluidOutputDirection() == gridSide) {
                setAutoOutputFluids(!isAutoOutputFluids());
            }
        }
        return InteractionResult.sidedSuccess(player.level().isClientSide);
    }
}

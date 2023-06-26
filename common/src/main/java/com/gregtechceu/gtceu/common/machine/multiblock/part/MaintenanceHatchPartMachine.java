package com.gregtechceu.gtceu.common.machine.multiblock.part;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.IMaintenanceHatch;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.UITemplate;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IMachineModifyDrops;
import com.gregtechceu.gtceu.api.machine.feature.IUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMaintenance;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.syncdata.RequireRerender;
import com.gregtechceu.gtceu.api.syncdata.UpdateListener;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.util.ClickData;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.SlotWidget;
import com.lowdragmc.lowdraglib.gui.widget.TextBoxWidget;
import com.lowdragmc.lowdraglib.misc.ItemStackTransfer;
import com.lowdragmc.lowdraglib.side.item.IItemTransfer;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Tuple;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

public class MaintenanceHatchPartMachine extends TieredPartMachine implements IMachineModifyDrops, IMaintenanceHatch, IUIMachine {
    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(MaintenanceHatchPartMachine.class, MultiblockPartMachine.MANAGED_FIELD_HOLDER);

    @Getter
    private final boolean isConfigurable;
    @Persisted @DescSynced
    private final NotifiableItemStackHandler itemStackHandler;
    @Getter
    @Persisted @DescSynced
    @RequireRerender @UpdateListener(methodName = "onSetTaped")
    private boolean isTaped;

    // Used to store state temporarily if the Controller is broken
    @Getter
    @Persisted @DescSynced
    @UpdateListener(methodName = "onUpdateProblems")
    private byte maintenanceProblems = -1;
    @Getter
    @Persisted @DescSynced
    @UpdateListener(methodName = "onUpdateTimeActive")
    private int timeActive = -1;

    @Persisted @DescSynced
    private BigDecimal durationMultiplier = BigDecimal.ONE;

    // Some stats used for the Configurable Maintenance Hatch
    private static final BigDecimal MAX_DURATION_MULTIPLIER = BigDecimal.valueOf(1.1);
    private static final BigDecimal MIN_DURATION_MULTIPLIER = BigDecimal.valueOf(0.9);
    private static final BigDecimal DURATION_ACTION_AMOUNT = BigDecimal.valueOf(0.01);
    private static final Function<Double, Double> TIME_ACTION = (d) -> {
        if (d < 1.0)
            return -20.0 * d + 21;
        else
            return -8.0 * d + 9;
    };

    public MaintenanceHatchPartMachine(IMachineBlockEntity metaTileEntityId, boolean isConfigurable) {
        super(metaTileEntityId, isConfigurable ? 3 : 1);
        this.isConfigurable = isConfigurable;
        this.itemStackHandler = createInventory();
    }

    protected NotifiableItemStackHandler createInventory() {
        return new TapeItemStackHandler(this, 1);
    }

    @Override
    public void onDrops(List<ItemStack> drops, Player entity) {
        clearInventory(drops, itemStackHandler);
    }

    /**
     * Sets this Maintenance Hatch as being duct taped
     * @param isTaped is the state of the hatch being taped or not
     */
    @Override
    public void setTaped(boolean isTaped) {
        this.isTaped = isTaped;
        if (!getLevel().isClientSide) {
            markDirty();
        }
    }

    private void onSetTaped(boolean newValue, boolean oldValue) {
        scheduleRenderUpdate();
        this.setTaped(newValue);
    }

    public void onUpdateProblems(byte newValue, byte oldValue) {
        this.maintenanceProblems = newValue;
    }

    public void onUpdateTimeActive(int newValue, int oldValue) {
        this.timeActive = newValue;
    }

    @Nullable
    public IMultiController getController() {
        return getControllers().size() > 0 ? getControllers().get(0) : null;
    }

    /**
     * Stores maintenance data to this MetaTileEntity
     * @param maintenanceProblems is the byte value representing the problems
     * @param timeActive is the int value representing the total time the parent multiblock has been active
     */
    @Override
    public void storeMaintenanceData(byte maintenanceProblems, int timeActive) {
        this.maintenanceProblems = maintenanceProblems;
        this.timeActive = timeActive;
    }

    /**
     *
     * @return whether this maintenance hatch has maintenance data
     */
    @Override
    public boolean hasMaintenanceData() {
        return this.maintenanceProblems != -1;
    }

    /**
     * reads this MetaTileEntity's maintenance data
     * @return Tuple of Byte, Integer corresponding to the maintenance problems, and total time active
     */
    @Override
    public Tuple<Byte, Integer> readMaintenanceData() {
        Tuple<Byte, Integer> data = new Tuple<>(this.maintenanceProblems, this.timeActive);
        storeMaintenanceData((byte) -1, -1);
        return data;
    }

    @Override
    public boolean startWithoutProblems() {
        return isConfigurable;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        this.subscribeServerTick(this::update);
    }

    public void update() {
        if (!getLevel().isClientSide && getOffsetTimer() % 20 == 0) {
            if (this.getController() instanceof IMaintenance maintenance) {
                if (maintenance.hasMaintenanceProblems()) {
                    if (consumeDuctTape(this.itemStackHandler, 0)) {
                        fixAllMaintenanceProblems();
                        setTaped(true);
                    }
                }
            }
        }
    }

    /**
     * Fixes the maintenance problems of this hatch's Multiblock Controller
     * @param entityPlayer the player performing the fixing
     */
    private void fixMaintenanceProblems(@Nullable Player entityPlayer) {
        if (!(this.getController() instanceof IMaintenance))
            return;

        if (!((IMaintenance) this.getController()).hasMaintenanceProblems())
            return;

        if (entityPlayer != null) {
            // Fix automatically on slot click by player in Creative Mode
            if (entityPlayer.isCreative()) {
                fixAllMaintenanceProblems();
                return;
            }
            // Then for every slot in the player's main inventory, try to duct tape fix
            for (int i = 0; i < entityPlayer.getInventory().items.size(); i++) {
                if (consumeDuctTape(new ItemStackTransfer(entityPlayer.getInventory().items), i)) {
                    fixAllMaintenanceProblems();
                    setTaped(true);
                    return;
                }
            }
            // Lastly for each problem the multi has, try to fix with tools
            fixProblemsWithTools(((IMaintenance) this.getController()).getMaintenanceProblems(), entityPlayer);
        }
    }

    /**
     *
     * Handles duct taping for manual and auto-taping use
     *
     * @param handler is the handler to get duct tape from
     * @param slot is the inventory slot to check for tape
     * @return true if tape was consumed, else false
     */
    private boolean consumeDuctTape(@Nullable IItemTransfer handler, int slot) {
        if (handler == null)
            return false;
        return consumeDuctTape(null, handler.getStackInSlot(slot));
    }

    private boolean consumeDuctTape(@Nullable Player player, ItemStack itemStack) {
        if (!itemStack.isEmpty() && itemStack.is(GTItems.DUCT_TAPE.get())) {
            if (player == null || !player.isCreative()) {
                itemStack.shrink(1);
            }
            return true;
        }
        return false;
    }

    /**
     * Attempts to fix a provided maintenance problem with a tool in the player's
     * inventory, if the tool exists.
     *
     * @param problems Problem Flags
     * @param entityPlayer Target Player which their inventory would be scanned for tools to fix
     */
    private void fixProblemsWithTools(byte problems, Player entityPlayer) {
        List<GTToolType> toolsToMatch = Arrays.asList(new GTToolType[6]);
        boolean proceed = false;
        for (byte index = 0; index < 6; index++) {
            if (((problems >> index) & 1) == 0) {
                proceed = true;
                switch (index) {
                    case 0 -> toolsToMatch.set(0, GTToolType.WRENCH);
                    case 1 -> toolsToMatch.set(1, GTToolType.SCREWDRIVER);
                    case 2 -> toolsToMatch.set(2, GTToolType.SOFT_MALLET);
                    case 3 -> toolsToMatch.set(3, GTToolType.HARD_HAMMER);
                    case 4 -> toolsToMatch.set(4, GTToolType.WIRE_CUTTER);
                    case 5 -> toolsToMatch.set(5, GTToolType.CROWBAR);
                }
            }
        }
        if (!proceed) {
            return;
        }

        for (int i = 0; i < toolsToMatch.size(); i++) {
            GTToolType toolToMatch = toolsToMatch.get(i);
            if (toolToMatch != null) {
                // Try to use the item in the player's "hand" (under the cursor)
                ItemStack heldItem = entityPlayer.containerMenu.getCarried();
                if (ToolHelper.is(heldItem, toolToMatch)) {
                    fixProblemWithTool(i, heldItem, entityPlayer);

                    if (toolsToMatch.stream().allMatch(Objects::isNull)) {
                        return;
                    }
                }

                // Then try all the remaining inventory slots
                for (ItemStack itemStack : entityPlayer.getInventory().items) {
                    if (ToolHelper.is(itemStack, toolToMatch)) {
                        fixProblemWithTool(i, itemStack, entityPlayer);

                        if (toolsToMatch.stream().allMatch(Objects::isNull)) {
                            return;
                        }
                    }
                }

                if (entityPlayer instanceof ServerPlayer player) {
                    for (ItemStack stack : entityPlayer.getInventory().items) {
                        if (ToolHelper.is(stack, toolToMatch)) {
                            for (IMultiController controller : this.getControllers()) {
                                if (controller instanceof IMaintenance maintenance) {
                                    maintenance.setMaintenanceFixed(i);
                                    ToolHelper.damageItem(stack, GTValues.RNG, player);
                                    if (toolsToMatch.stream().allMatch(Objects::isNull)) {
                                        return;
                                    }
                                }
                            }
                        }
                    }
                }

            }
        }
    }

    private void fixProblemWithTool(int problemIndex, ItemStack stack, Player player) {
        if (this.getController() instanceof IMaintenance maintenance) {
            maintenance.setMaintenanceFixed(problemIndex);
            if (player instanceof ServerPlayer serverPlayer) {
                ToolHelper.damageItem(stack, GTValues.RNG, serverPlayer);
            }
            setTaped(false);
        }
    }

    /**
     * Fixes every maintenance problem of the controller
     */
    public void fixAllMaintenanceProblems() {
        if (this.getController() instanceof IMaintenance maintenance)
            for (int i = 0; i < 6; i++) maintenance.setMaintenanceFixed(i);
    }

    @Override
    public boolean isFullAuto() {
        return false;
    }

    @Override
    public double getDurationMultiplier() {
        return durationMultiplier.doubleValue();
    }

    @Override
    public double getTimeMultiplier() {
        return BigDecimal.valueOf(TIME_ACTION.apply(durationMultiplier.doubleValue()))
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private void incInternalMultiplier(ClickData data) {
        if (durationMultiplier.compareTo(MAX_DURATION_MULTIPLIER) == 0) return;
        durationMultiplier = durationMultiplier.add(DURATION_ACTION_AMOUNT);
    }

    private void decInternalMultiplier(ClickData data) {
        if (durationMultiplier.compareTo(MIN_DURATION_MULTIPLIER) == 0) return;
        durationMultiplier = durationMultiplier.subtract(DURATION_ACTION_AMOUNT);
    }

    @Override
    public void onUnload() {
        if (getController() instanceof IMaintenance controller) {
            if (!getLevel().isClientSide)
                controller.storeTaped(isTaped);
        }
        super.onUnload();
    }

    @Override
    public InteractionResult onUse(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (getController() instanceof IMaintenance && ((IMaintenance) getController()).hasMaintenanceProblems()) {
            if (consumeDuctTape(player, player.getItemInHand(hand))) {
                fixAllMaintenanceProblems();
                setTaped(true);
                return InteractionResult.CONSUME;
            }
        }
        return IUIMachine.super.onUse(state, world, pos, player, hand, hit);
    }

    @Override
    public ModularUI createUI(Player entityPlayer) {
        ModularUI modular = new ModularUI(176, 18 * 3 + 98, this, entityPlayer)
                .background(GuiTextures.BACKGROUND)
                .widget(new LabelWidget(10, 5, getBlockState().getBlock().getDescriptionId()))
                .widget(UITemplate.bindPlayerInventory(entityPlayer.getInventory(), GuiTextures.SLOT, 7, 18 * 3 + 16, true));

        /*if (!isConfigurable && GTValues.FOOLS.get()) {
            modular.widget(new FixWiringTaskWidget(48, 15, 80, 50)
                    .setOnFinished(this::fixAllMaintenanceProblems)
                    .setCanInteractPredicate(this::hasController));
        } else */{
            modular.widget(new SlotWidget(itemStackHandler, 0, 89 - 10, 18 - 1)
                            .setBackgroundTexture(GuiTextures.SLOT).setHoverTooltips("gregtech.machine.maintenance_hatch_tape_slot.tooltip").setBackground(GuiTextures.DUCT_TAPE_OVERLAY))
                    .widget(new ButtonWidget(89 - 10 - 1, 18 * 2 + 3, 20, 20, new TextTexture(""), data -> fixMaintenanceProblems(entityPlayer))
                            .setButtonTexture(GuiTextures.MAINTENANCE_ICON).setHoverTooltips("gregtech.machine.maintenance_hatch_tool_slot.tooltip"));
        }
        if (isConfigurable) {
            modular.widget(new TextBoxWidget(5, 25, 60, getTextWidgetText("duration", this::getDurationMultiplier)))
                    .widget(new TextBoxWidget(5, 39, 60, getTextWidgetText("time", this::getTimeMultiplier)))
                    .widget(new ButtonWidget(9, 18 * 3 + 16 - 18, 12, 12, new TextTexture("-"), this::decInternalMultiplier))
                    .widget(new ButtonWidget(9 + 18 * 2, 18 * 3 + 16 - 18, 12, 12, new TextTexture("+"), this::incInternalMultiplier));
        }
        return modular;
    }

    private static List<String> getTextWidgetText(String type, Supplier<Double> multiplier) {
        List<String> list = new ArrayList<>();
        Component tooltip;
        if (multiplier.get() == 1.0) {
            tooltip = Component.translatable("gtceu.maintenance.configurable_" + type + ".unchanged_description");
        } else {
            tooltip = Component.translatable("gtceu.maintenance.configurable_" + type + ".changed_description", multiplier.get());
        }
        list.add(Component.translatable("gtceu.maintenance.configurable_" + type, multiplier.get())
                .setStyle(Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, tooltip))).getString());
        return list;
    }

    @Override
    public boolean canShared() {
        return false;
    }

    public static class TapeItemStackHandler extends NotifiableItemStackHandler {

        public TapeItemStackHandler(MetaMachine machine, int size) {
            super(machine, size, IO.BOTH, IO.NONE);
        }

        @Override
        @Nonnull
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
            if (!stack.isEmpty() && GTItems.DUCT_TAPE.is(stack)) {
                return super.insertItem(slot, stack, simulate);
            }
            return stack;
        }
    }
}
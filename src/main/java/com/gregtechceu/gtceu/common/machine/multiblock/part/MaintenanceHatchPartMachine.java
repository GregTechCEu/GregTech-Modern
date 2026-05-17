package com.gregtechceu.gtceu.common.machine.multiblock.part;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.IMuiMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMaintenanceMachine;
import com.gregtechceu.gtceu.api.machine.mui.MachineUIPanel;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredPartMachine;
import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.client.model.machine.MachineRenderState;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.utils.ExtendedUseOnContext;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.api.drawable.Text;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.drawable.ItemDrawable;
import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.UISettings;
import brachy.modularui.utils.Alignment;
import brachy.modularui.value.sync.FloatSyncValue;
import brachy.modularui.value.sync.InteractionSyncHandler;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widgets.ButtonWidget;
import brachy.modularui.widgets.TextWidget;
import brachy.modularui.widgets.layout.Flow;
import brachy.modularui.widgets.slot.ItemSlot;
import brachy.modularui.widgets.slot.ModularSlot;
import brachy.modularui.widgets.textfield.TextFieldWidget;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MaintenanceHatchPartMachine extends TieredPartMachine
                                         implements IMuiMachine, IMaintenanceMachine {

    private static final float MAX_DURATION_MULTIPLIER = 1.1f;
    private static final float MIN_DURATION_MULTIPLIER = 0.9f;
    private static final float DURATION_ACTION_AMOUNT = 0.01f;

    @Getter
    private final boolean isConfigurable;
    @SaveField
    private final NotifiableItemStackHandler itemStackHandler;
    @Getter
    @SaveField
    @SyncToClient
    private boolean isTaped;
    @Getter
    @Setter
    @SaveField
    protected int timeActive;
    @Getter
    @SaveField
    @SyncToClient
    protected byte maintenanceProblems = startProblems();
    @SaveField
    private float durationMultiplier = 1f;
    @Nullable
    protected TickableSubscription maintenanceSubs;

    public MaintenanceHatchPartMachine(BlockEntityCreationInfo info, boolean isConfigurable) {
        super(info, isConfigurable ? GTValues.HV : GTValues.LV);
        this.isConfigurable = isConfigurable;
        this.itemStackHandler = attachTrait(createInventory());
        this.itemStackHandler.setFilter(itemStack -> itemStack.is(GTItems.DUCT_TAPE.get()));
    }

    //////////////////////////////////////
    // ****** Initialization ******//
    //////////////////////////////////////
    protected NotifiableItemStackHandler createInventory() {
        return new NotifiableItemStackHandler(1, IO.BOTH, IO.BOTH);
    }

    @Override
    public byte startProblems() {
        return ALL_PROBLEMS;
    }

    public void setDurationMultiplier(float durationMultiplier) {
        this.durationMultiplier = durationMultiplier;
        syncDataHolder.markClientSyncFieldDirty("durationMultiplier");
    }

    //////////////////////////////////////
    // ********* Logic **********//
    //////////////////////////////////////
    @Override
    public void setMaintenanceProblems(byte problems) {
        this.maintenanceProblems = problems;
        updateMaintenanceSubscription();
        syncDataHolder.markClientSyncFieldDirty("maintenanceProblems");
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!isRemote()) {
            updateMaintenanceSubscription();

            // fix the model being invalid after the tape property rename
            MachineRenderState renderState = getRenderState();
            if (renderState.hasProperty(GTMachineModelProperties.IS_TAPED) &&
                    this.isTaped != renderState.getValue(GTMachineModelProperties.IS_TAPED)) {
                setRenderState(renderState.setValue(GTMachineModelProperties.IS_TAPED, this.isTaped));
            }
        }
    }

    protected void updateMaintenanceSubscription() {
        if (hasMaintenanceProblems()) {
            maintenanceSubs = subscribeServerTick(maintenanceSubs, this::update);
        } else if (maintenanceSubs != null) {
            maintenanceSubs.unsubscribe();
            maintenanceSubs = null;
        }
    }

    public void update() {
        if (getOffsetTimer() % 20 == 0) {
            if (hasMaintenanceProblems()) {
                if (consumeDuctTape(this.itemStackHandler, 0)) {
                    fixAllMaintenanceProblems();
                    setTaped(true);
                }
            } else {
                updateMaintenanceSubscription();
            }
        }
    }

    /**
     * Fixes the maintenance problems of this hatch's Multiblock Controller
     * 
     * @param entityPlayer the player performing the fixing
     */
    private void fixMaintenanceProblems(@Nullable Player entityPlayer) {
        if (!hasMaintenanceProblems())
            return;

        if (entityPlayer != null) {
            // Fix automatically on slot click by player in Creative Mode
            if (entityPlayer.isCreative()) {
                fixAllMaintenanceProblems();
                return;
            }
            // Then for every slot in the player's main inventory, try to duct tape fix
            for (int i = 0; i < entityPlayer.getInventory().items.size(); i++) {
                if (consumeDuctTape(new InvWrapper(entityPlayer.getInventory()), i)) {
                    fixAllMaintenanceProblems();
                    setTaped(true);
                    return;
                }
            }
            // Lastly for each problem the multi has, try to fix with tools
            fixProblemsWithTools(getMaintenanceProblems(), entityPlayer);
        }
    }

    /**
     *
     * Handles duct taping for manual and auto-taping use
     *
     * @param handler is the handler to get duct tape from
     * @param slot    is the inventory slot to check for tape
     * @return true if tape was consumed, else false
     */
    private boolean consumeDuctTape(IItemHandler handler, int slot) {
        var stored = handler.getStackInSlot(slot);
        if (!stored.isEmpty() && stored.is(GTItems.DUCT_TAPE.get())) {
            return handler.extractItem(slot, 1, false).is(GTItems.DUCT_TAPE.get());
        }
        return false;
    }

    private boolean consumeDuctTape(Player player, InteractionHand hand) {
        var held = player.getItemInHand(hand);
        if (!held.isEmpty() && held.is(GTItems.DUCT_TAPE.get())) {
            if (!player.isCreative()) {
                held.shrink(1);
            }
            return true;
        }
        return false;
    }

    /**
     * Attempts to fix a provided maintenance problem with a tool in the player's
     * inventory, if the tool exists.
     *
     * @param problems     Problem Flags
     * @param entityPlayer Target Player which their inventory would be scanned for tools to fix
     */
    private void fixProblemsWithTools(byte problems, Player entityPlayer) {
        List<@Nullable GTToolType> toolsToMatch = Arrays.asList(new GTToolType[6]);
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
                            setMaintenanceFixed(i);
                            ToolHelper.damageItem(stack, player, 1);
                            if (toolsToMatch.stream().allMatch(Objects::isNull)) {
                                return;
                            }
                        }
                    }
                }

            }
        }
    }

    private void fixProblemWithTool(int problemIndex, ItemStack stack, Player player) {
        setMaintenanceFixed(problemIndex);
        if (player instanceof ServerPlayer serverPlayer) {
            ToolHelper.damageItem(stack, serverPlayer, 1);
        }
        setTaped(false);
    }

    /**
     * Fixes every maintenance problem of the controller
     */
    public void fixAllMaintenanceProblems() {
        for (int i = 0; i < 6; i++) setMaintenanceFixed(i);
    }

    @Override
    public boolean isFullAuto() {
        return false;
    }

    @Override
    public void setTaped(boolean isTaped) {
        if (this.isTaped != isTaped) {
            this.isTaped = isTaped;
            setRenderState(getRenderState().setValue(GTMachineModelProperties.IS_TAPED, isTaped));
        }
    }

    @Override
    public float getTimeMultiplier() {
        var result = 1f;
        if (durationMultiplier < 1.0)
            result = -20 * durationMultiplier + 21;
        else
            result = -8 * durationMultiplier + 9;
        return BigDecimal.valueOf(result)
                .setScale(2, RoundingMode.HALF_UP)
                .floatValue();
    }

    //////////////////////////////////////
    // ******* INTERACTION *******//
    //////////////////////////////////////

    @Override
    public InteractionResult onUseWithItem(ExtendedUseOnContext context) {
        if (hasMaintenanceProblems()) {
            if (consumeDuctTape(context.getPlayer(), context.getHand())) {
                fixAllMaintenanceProblems();
                setTaped(true);
                return InteractionResult.SUCCESS;
            }
        }
        return super.onUseWithItem(context);
    }

    //////////////////////////////////////
    // ******** GUI *********//
    //////////////////////////////////////

    @Override
    public void buildMainUI(ParentWidget<?> mainWidget, PosGuiData guiData, PanelSyncManager syncManager,
                            UISettings settings) {
        InteractionSyncHandler syncHandler = new InteractionSyncHandler();
        // syncManager.syncValue("button_idk", syncHandler);
        Flow maintenanceStatusWidget = Flow.column()
                .crossAxisAlignment(Alignment.CrossAxis.START)
                .coverChildren()
                .padding(5)
                .childPadding(2);
        Runnable updateWidget = () -> {
            while (!maintenanceStatusWidget.getChildren().isEmpty()) maintenanceStatusWidget.remove(0);
            maintenanceStatusWidget.child(new TextWidget<>(Text.dynamic(() -> hasMaintenanceProblems() ?
                    Component.translatable("gtceu.top.maintenance_broken") :
                    Component.translatable("gtceu.top.maintenance_fixed"))))
                    .child(Flow.row()
                            .coverChildren()
                            .children(Stream.iterate(Byte.valueOf("0"), i -> i < 6, i -> ++i)
                                    .filter(i -> ((getMaintenanceProblems() >> i) & 1) == 0)
                                    .map(GTUtil::getMaintenanceText)
                                    .map(i -> new IDrawable.DrawableWidget(new ItemDrawable(i.getA())))
                                    .map(IWidget.class::cast)
                                    .toList()));
        };
        syncHandler.setOnMousePressed((button) -> {
            fixMaintenanceProblems(guiData.getPlayer());
            updateWidget.run();
        });
        updateWidget.run();
        mainWidget.child(Flow.column()
                .size(MachineUIPanel.DEFAULT_CONTENT_WIDTH, MachineUIPanel.DEFAULT_CONTENT_HEIGHT)
                .crossAxisAlignment(Alignment.CrossAxis.START)
                .childIf(this.isConfigurable, () -> Flow.column()
                        .coverChildren()
                        .padding(5)
                        .paddingLeft(0)
                        .marginLeft(5)
                        .child(Flow.row()
                                .coverChildren()
                                .childPadding(5)
                                .leftRel(0)
                                .child(new TextWidget<>(
                                        Text.lang("gtceu.maintenance.configurable_duration.modify")))
                                .child(new TextFieldWidget()
                                        .setNumbersDouble(() -> MIN_DURATION_MULTIPLIER,
                                                () -> MAX_DURATION_MULTIPLIER)
                                        .setDefaultNumber(1)
                                        .value(new FloatSyncValue(this::getDurationMultiplier,
                                                this::setDurationMultiplier))
                                        .addTooltipElement(Text.dynamic(() -> getDurationMultiplier() == 1.0 ?
                                                Component.translatable(
                                                        "gtceu.maintenance.configurable_duration.unchanged_description") :
                                                Component.translatable(
                                                        "gtceu.maintenance.configurable_duration.changed_description")))))
                        .child(new TextWidget<>(Text.lang("gtceu.maintenance.configurable_time",
                                this.getTimeMultiplier()))
                                .leftRel(0)))
                .child(Flow.row()
                        .leftRel(0.5f)
                        .coverChildren()
                        .padding(5)
                        .child(new ItemSlot()
                                .slot(new ModularSlot(itemStackHandler, 0).changeListener(
                                        (newItem, onlyAmountChanged, client, init) -> updateWidget.run()))
                                .background(GTGuiTextures.SLOT, GTGuiTextures.DUCT_TAPE_OVERLAY))
                        .child(new ButtonWidget<>()
                                .background(GTGuiTextures.BUTTON_MAINTENANCE)
                                .disableHoverBackground()
                                .addTooltipElement(
                                        Text.lang("gtceu.machine.maintenance_hatch_tool_slot.tooltip"))
                                .syncHandler(syncHandler)))
                .child(maintenanceStatusWidget));
    }
}

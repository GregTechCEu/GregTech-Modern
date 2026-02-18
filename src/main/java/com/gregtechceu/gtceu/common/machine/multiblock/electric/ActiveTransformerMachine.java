package com.gregtechceu.gtceu.common.machine.multiblock.electric;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.IControllable;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.ConditionalSubscriptionHandler;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.misc.EnergyContainerList;
import com.gregtechceu.gtceu.api.mui.base.drawable.IKey;
import com.gregtechceu.gtceu.api.mui.drawable.Icon;
import com.gregtechceu.gtceu.api.mui.factory.PosGuiData;
import com.gregtechceu.gtceu.api.mui.utils.Alignment;
import com.gregtechceu.gtceu.api.mui.value.sync.BooleanSyncValue;
import com.gregtechceu.gtceu.api.mui.value.sync.LongSyncValue;
import com.gregtechceu.gtceu.api.mui.value.sync.PanelSyncManager;
import com.gregtechceu.gtceu.api.mui.widget.ParentWidget;
import com.gregtechceu.gtceu.api.mui.widget.Widget;
import com.gregtechceu.gtceu.api.mui.widgets.ListWidget;
import com.gregtechceu.gtceu.api.mui.widgets.SlotGroupWidget;
import com.gregtechceu.gtceu.api.mui.widgets.layout.Flow;
import com.gregtechceu.gtceu.api.pattern.TraceabilityPredicate;
import com.gregtechceu.gtceu.client.mui.screen.ModularPanel;
import com.gregtechceu.gtceu.client.mui.screen.UISettings;
import com.gregtechceu.gtceu.common.data.mui.GTMuiWidgets;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.common.mui.GTGuis;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.level.block.Block;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

import static com.gregtechceu.gtceu.api.pattern.Predicates.abilities;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ActiveTransformerMachine extends WorkableElectricMultiblockMachine
                                      implements IControllable {

    private IEnergyContainer powerOutput;
    private IEnergyContainer powerInput;
    protected ConditionalSubscriptionHandler converterSubscription;

    public ActiveTransformerMachine(BlockEntityCreationInfo info) {
        super(info);
        this.powerOutput = new EnergyContainerList(new ArrayList<>());
        this.powerInput = new EnergyContainerList(new ArrayList<>());

        this.converterSubscription = new ConditionalSubscriptionHandler(this, this::convertEnergyTick,
                this::isSubscriptionActive);
    }

    public void convertEnergyTick() {
        if (isWorkingEnabled()) {
            getRecipeLogic()
                    .setStatus(isSubscriptionActive() ? RecipeLogic.Status.WORKING : RecipeLogic.Status.SUSPEND);
        }
        if (isWorkingEnabled()) {
            long canDrain = powerInput.getEnergyStored();
            long totalDrained = powerOutput.changeEnergy(canDrain);
            powerInput.removeEnergy(totalDrained);
        }
        converterSubscription.updateSubscription();
    }

    @SuppressWarnings("RedundantIfStatement") // It is cleaner to have the final return true separate.
    protected boolean isSubscriptionActive() {
        if (!isFormed()) return false;

        if (powerInput == null || powerInput.getEnergyStored() <= 0) return false;
        if (powerOutput == null) return false;
        if (powerOutput.getEnergyStored() >= powerOutput.getEnergyCapacity()) return false;

        return true;
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        // capture all energy containers
        List<IEnergyContainer> powerInput = new ArrayList<>();
        List<IEnergyContainer> powerOutput = new ArrayList<>();
        Long2ObjectMap<IO> ioMap = getMultiblockState().getMatchContext().getOrCreate("ioMap",
                Long2ObjectMaps::emptyMap);

        for (IMultiPart part : getPrioritySortedParts()) {
            IO io = ioMap.getOrDefault(part.self().getBlockPos().asLong(), IO.BOTH);
            if (io == IO.NONE) continue;
            var handlerLists = part.getRecipeHandlers();
            for (var handlerList : handlerLists) {
                if (!handlerList.isValid(io)) continue;

                var containers = handlerList.getCapability(EURecipeCapability.CAP).stream()
                        .filter(IEnergyContainer.class::isInstance)
                        .map(IEnergyContainer.class::cast)
                        .toList();

                if (handlerList.getHandlerIO().support(IO.IN)) {
                    powerInput.addAll(containers);
                } else if (handlerList.getHandlerIO().support(IO.OUT)) {
                    powerOutput.addAll(containers);
                }

                traitSubscriptions
                        .add(handlerList.subscribe(converterSubscription::updateSubscription, EURecipeCapability.CAP));
            }
        }

        // Invalidate the structure if there is not at least one output and one input
        if (powerInput.isEmpty() || powerOutput.isEmpty()) {
            this.onStructureInvalid();
        }

        this.powerOutput = new EnergyContainerList(powerOutput);
        this.powerInput = new EnergyContainerList(powerInput);

        converterSubscription.updateSubscription();
    }

    @NotNull
    private List<IMultiPart> getPrioritySortedParts() {
        return getParts().stream().sorted(Comparator.comparingInt(part -> {
            if (part instanceof MetaMachine partMachine) {
                Block partBlock = partMachine.getBlockState().getBlock();

                if (PartAbility.OUTPUT_ENERGY.isApplicable(partBlock))
                    return 1;

                if (PartAbility.SUBSTATION_OUTPUT_ENERGY.isApplicable(partBlock))
                    return 2;

                if (PartAbility.OUTPUT_LASER.isApplicable(partBlock))
                    return 3;
            }

            return 4;
        })).toList();
    }

    @Override
    public void onStructureInvalid() {
        if ((isWorkingEnabled() && recipeLogic.getStatus() == RecipeLogic.Status.WORKING) &&
                !ConfigHolder.INSTANCE.machines.harmlessActiveTransformers) {
            GTUtil.doExplosion(getLevel(), getBlockPos(), 6f + getTier());
        }
        super.onStructureInvalid();
        this.powerOutput = new EnergyContainerList(new ArrayList<>());
        this.powerInput = new EnergyContainerList(new ArrayList<>());
        getRecipeLogic().setStatus(RecipeLogic.Status.SUSPEND);
        converterSubscription.unsubscribe();
    }

    public static TraceabilityPredicate getHatchPredicates() {
        return abilities(PartAbility.INPUT_ENERGY).setPreviewCount(1)
                .or(abilities(PartAbility.OUTPUT_ENERGY).setPreviewCount(2))
                .or(abilities(PartAbility.SUBSTATION_INPUT_ENERGY).setPreviewCount(1))
                .or(abilities(PartAbility.SUBSTATION_OUTPUT_ENERGY).setPreviewCount(1))
                .or(abilities(PartAbility.INPUT_LASER).setPreviewCount(1))
                .or(abilities(PartAbility.OUTPUT_LASER).setPreviewCount(1));
    }

    @Override
    public ModularPanel buildUI(PosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        var panel = GTGuis.createPanel(this, 176, 176);

        panel.child(GTMuiWidgets.createTitleBar(this.getDefinition(), 176))
                .child(new ParentWidget<>()
                        .widthRel(0.95f)
                        .heightRel(.45f)
                        .margin(4, 0)
                        .left(3).top(5)
                        .child(Flow.row()
                                .child(getMainTextPanel(syncManager, 170, 84))))
                .child(Flow.column()
                        .coverChildren()
                        .leftRel(1.0f)
                        .reverseLayout(true)
                        .bottom(16)
                        .padding(0, 8, 4, 4)
                        .childPadding(2)
                        .background(GTGuiTextures.BACKGROUND.getSubArea(0.25f, 0f, 1.0f, 1.0f))
                        .child(GTMuiWidgets.createPowerButton(this, syncManager))
                        .child(GTMuiWidgets.createVoidingButton(this, syncManager))
                        .excludeAreaInXei())
                .child(SlotGroupWidget.playerInventory(false).left(7).bottom(7));
        return panel;
    }

    public Widget<?> getMainTextPanel(PanelSyncManager syncManager, int width, int height) {
        var parentWidget = new ParentWidget<>();
        var listWidget = new ListWidget<>();
        listWidget
                .width(width - 6)
                .height(height - 6)
                .childSeparator(Icon.EMPTY_2PX)
                .crossAxisAlignment(Alignment.CrossAxis.START)
                .alignX(Alignment.CenterLeft)
                .left(3)
                .top(3);
        parentWidget.size(width, height)
                .background(GTGuiTextures.MUI_DISPLAY);
        // Machine generic sync handlers
        BooleanSyncValue isFormed = syncManager.getOrCreateSyncHandler("isFormed", BooleanSyncValue.class,
                () -> new BooleanSyncValue(this::isFormed));
        BooleanSyncValue workingEnabled = syncManager.getOrCreateSyncHandler("workingEnabled", BooleanSyncValue.class,
                () -> new BooleanSyncValue(this.recipeLogic::isWorkingEnabled, this.recipeLogic::setWorkingEnabled));
        BooleanSyncValue active = syncManager.getOrCreateSyncHandler("isActive", BooleanSyncValue.class,
                () -> new BooleanSyncValue(this.recipeLogic::isActive));

        // Machine specific sync handlers
        // These will not be called anywhere else, so we can create them directly instead of using
        // getOrCreateSyncHandler

        LongSyncValue inputVoltage = new LongSyncValue(this.powerInput::getInputVoltage);
        syncManager.syncValue("inputVoltage", inputVoltage);

        LongSyncValue outputVoltage = new LongSyncValue(this.powerOutput::getOutputVoltage);
        syncManager.syncValue("outputVoltage", outputVoltage);

        LongSyncValue inputAmperage = new LongSyncValue(this.powerInput::getInputAmperage);
        syncManager.syncValue("inputAmperage", inputAmperage);

        LongSyncValue outputAmperage = new LongSyncValue(this.powerOutput::getOutputAmperage);
        syncManager.syncValue("outputAmperage", outputAmperage);

        LongSyncValue inputPerSec = new LongSyncValue(this.powerInput::getInputPerSec);
        syncManager.syncValue("inputPerSec", inputPerSec);

        LongSyncValue outputPerSec = new LongSyncValue(this.powerOutput::getOutputPerSec);
        syncManager.syncValue("outputPerSec", outputPerSec);

        listWidget.child(IKey.lang(Component.translatable("gtceu.multiblock.work_paused"))
                .asWidget()
                .setEnabledIf((widget) -> isFormed.getBoolValue() && !workingEnabled.getBoolValue()));

        listWidget.child(IKey.lang(Component.translatable("gtceu.multiblock.running"))
                .asWidget()
                .setEnabledIf(
                        (widget) -> isFormed.getBoolValue() && workingEnabled.getBoolValue() && active.getBoolValue()));

        listWidget.child(IKey.dynamic(() -> Component
                .translatable("gtceu.multiblock.active_transformer.max_input",
                        FormattingUtil.formatNumbers(
                                Math.abs(inputVoltage.getLongValue() * inputAmperage.getLongValue()))))
                .asWidget()
                .setEnabledIf(
                        (widget) -> isFormed.getBoolValue() && workingEnabled.getBoolValue() && active.getBoolValue()));

        listWidget.child(IKey.dynamic(() -> Component
                .translatable("gtceu.multiblock.active_transformer.max_output",
                        FormattingUtil.formatNumbers(
                                Math.abs(outputVoltage.getLongValue() * outputAmperage.getLongValue()))))
                .asWidget()
                .setEnabledIf(
                        (widget) -> isFormed.getBoolValue() && workingEnabled.getBoolValue() && active.getBoolValue()));

        listWidget.child(IKey.dynamic(() -> Component
                .translatable("gtceu.multiblock.active_transformer.average_in",
                        FormattingUtil.formatNumbers(Math.abs(inputPerSec.getLongValue() / 20))))
                .asWidget()
                .setEnabledIf(
                        (widget) -> isFormed.getBoolValue() && workingEnabled.getBoolValue() && active.getBoolValue()));

        listWidget.child(IKey.dynamic(() -> Component
                .translatable("gtceu.multiblock.active_transformer.average_out",
                        FormattingUtil.formatNumbers(Math.abs(outputPerSec.getLongValue() / 20))))
                .asWidget()
                .setEnabledIf(
                        (widget) -> isFormed.getBoolValue() && workingEnabled.getBoolValue() && active.getBoolValue()));

        listWidget.child(IKey.lang(Component.translatable("gtceu.multiblock.active_transformer.danger_enabled"))
                .asWidget()
                .setEnabledIf((widget) -> isFormed.getBoolValue() && workingEnabled.getBoolValue() &&
                        active.getBoolValue() && !ConfigHolder.INSTANCE.machines.harmlessActiveTransformers));

        listWidget.child(IKey.lang(Component.translatable("gtceu.multiblock.idling"))
                .asWidget()
                .setEnabledIf((widget) -> isFormed.getBoolValue() && workingEnabled.getBoolValue() &&
                        !active.getBoolValue()));

        listWidget.child(IKey.dynamic(() -> {
            Component tooltip = Component.translatable("gtceu.multiblock.invalid_structure.tooltip")
                    .withStyle(ChatFormatting.GRAY);
            return Component.translatable("gtceu.multiblock.invalid_structure")
                    .withStyle(Style.EMPTY.withColor(ChatFormatting.RED)
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, tooltip)));
        })
                .asWidget()
                .setEnabledIf((widget) -> !isFormed.getBoolValue()));

        parentWidget.child(listWidget);
        return parentWidget;
    }
}

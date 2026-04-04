package com.gregtechceu.gtceu.common.machine.storage;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IControllable;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.capability.ILaserContainer;
import com.gregtechceu.gtceu.api.machine.TieredMachine;
import com.gregtechceu.gtceu.api.machine.feature.IMuiMachine;
import com.gregtechceu.gtceu.api.machine.mui.MachineUIPanel;
import com.gregtechceu.gtceu.api.machine.mui.MachineUIPanelBuilder;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

import brachy.modularui.api.IPanelHandler;
import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.api.drawable.IKey;
import brachy.modularui.drawable.DynamicDrawable;
import brachy.modularui.drawable.GuiTextures;
import brachy.modularui.drawable.Rectangle;
import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.RichTooltip;
import brachy.modularui.screen.UISettings;
import brachy.modularui.utils.Alignment;
import brachy.modularui.utils.MouseData;
import brachy.modularui.value.sync.BooleanSyncValue;
import brachy.modularui.value.sync.IntSyncValue;
import brachy.modularui.value.sync.LongSyncValue;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widgets.ButtonWidget;
import brachy.modularui.widgets.Dialog;
import brachy.modularui.widgets.ListWidget;
import brachy.modularui.widgets.ToggleButton;
import brachy.modularui.widgets.layout.Flow;
import brachy.modularui.widgets.textfield.TextFieldWidget;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CreativeEnergyContainerMachine extends TieredMachine
                                            implements ILaserContainer, IMuiMachine, IControllable {

    @SaveField
    private long voltage = 0;
    @SaveField
    private int amps = 1;
    @SaveField
    private int setTier = 0;
    @SaveField
    @Getter
    @Setter
    private boolean workingEnabled = false;
    @SaveField
    private boolean source = true;
    @SaveField
    private long energyIOPerSec = 0;
    private long lastAverageEnergyIOPerTick = 0;
    private long ampsReceived = 0;
    private boolean doExplosion = false;

    public CreativeEnergyContainerMachine(BlockEntityCreationInfo info) {
        super(info, GTValues.MAX);
    }

    //////////////////////////////////////
    // ***** Initialization ******//
    //////////////////////////////////////

    @Override
    public void onLoad() {
        super.onLoad();
        subscribeServerTick(this::updateEnergyTick);
    }

    //////////////////////////////////////
    // ********** MISC ***********//
    //////////////////////////////////////

    protected void updateEnergyTick() {
        if (getOffsetTimer() % 20 == 0) {
            this.setIOSpeed(energyIOPerSec / 20);
            energyIOPerSec = 0;
            if (doExplosion) {
                getLevel().explode(null, getBlockPos().getX() + 0.5, getBlockPos().getY() + 0.5,
                        getBlockPos().getZ() + 0.5,
                        1, Level.ExplosionInteraction.NONE);
                doExplosion = false;
            }
        }
        ampsReceived = 0;
        if (!workingEnabled || !source || voltage <= 0 || amps <= 0) return;
        int ampsUsed = 0;
        for (var facing : GTUtil.DIRECTIONS) {
            var opposite = facing.getOpposite();
            IEnergyContainer container = GTCapabilityHelper.getEnergyContainer(getLevel(),
                    getBlockPos().relative(facing),
                    opposite);
            // Try to get laser capability
            if (container == null)
                container = GTCapabilityHelper.getLaser(getLevel(), getBlockPos().relative(facing), opposite);

            if (container != null && container.inputsEnergy(opposite) && container.getEnergyCanBeInserted() > 0) {
                ampsUsed += container.acceptEnergyFromNetwork(opposite, voltage, amps - ampsUsed);
                if (ampsUsed >= amps) {
                    break;
                }
            }
        }
        energyIOPerSec += ampsUsed * voltage;
    }

    @Override
    public long acceptEnergyFromNetwork(Direction side, long voltage, long amperage) {
        if (source || !workingEnabled || ampsReceived >= amps) {
            return 0;
        }
        if (voltage > this.voltage) {
            if (doExplosion)
                return 0;
            doExplosion = true;
            return Math.min(amperage, getInputAmperage() - ampsReceived);
        }
        long amperesAccepted = Math.min(amperage, getInputAmperage() - ampsReceived);
        if (amperesAccepted > 0) {
            ampsReceived += amperesAccepted;
            energyIOPerSec += amperesAccepted * voltage;
            return amperesAccepted;
        }
        return 0;
    }

    @Override
    public boolean inputsEnergy(Direction side) {
        return !source;
    }

    @Override
    public boolean outputsEnergy(Direction side) {
        return source;
    }

    @Override
    public long changeEnergy(long differenceAmount) {
        if (source || !workingEnabled) {
            return 0;
        }
        energyIOPerSec += differenceAmount;
        return differenceAmount;
    }

    @Override
    public long getEnergyStored() {
        return 69;
    }

    @Override
    public long getEnergyCapacity() {
        return 420;
    }

    @Override
    public long getInputAmperage() {
        return source ? 0 : amps;
    }

    @Override
    public long getInputVoltage() {
        return source ? 0 : voltage;
    }

    @Override
    public long getOutputVoltage() {
        return source ? voltage : 0;
    }

    @Override
    public long getOutputAmperage() {
        return source ? amps : 0;
    }

    public void setIOSpeed(long energyIOPerSec) {
        if (this.lastAverageEnergyIOPerTick != energyIOPerSec) {
            this.lastAverageEnergyIOPerTick = energyIOPerSec;
        }
    }

    //////////////////////////////////////
    // *********** GUI ***********//
    //////////////////////////////////////

    @Override
    public MachineUIPanelBuilder getPanelBuilder(PosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        return MachineUIPanelBuilder.defaultPanelBuilder(this).attachInventory(false);
    }

    @Override
    public void buildMainUI(ParentWidget<?> mainWidget, PosGuiData guiData, PanelSyncManager syncManager,
                            UISettings settings) {
        // syncing
        LongSyncValue voltage = new LongSyncValue(() -> this.voltage, (v) -> this.voltage = v);
        IntSyncValue amps = new IntSyncValue(() -> this.amps, (a) -> this.amps = Math.max(a, 1));
        IntSyncValue tier = new IntSyncValue(() -> this.tier, (t) -> this.setTier = t);
        BooleanSyncValue sourceSync = new BooleanSyncValue(() -> this.source, (b) -> this.source = b);
        syncManager.syncValue("tier", tier);

        IPanelHandler panelSyncHandler = syncManager.syncedPanel("voltage popup", false,
                (manager, handler) -> createAmpSelector(voltage, tier));

        mainWidget
                .child(Flow.col()
                        .size(MachineUIPanel.DEFAULT_CONTENT_WIDTH, 101)
                        .name("main")
                        .padding(7)
                        .mainAxisAlignment(Alignment.MainAxis.START)
                        .child(createVoltageRow(panelSyncHandler, voltage))
                        .child(createAmpRow(amps))
                        .child(new Rectangle().color(0xFF555555).asWidget()
                                .height(1).widthRel(0.95f).marginBottom(4).marginTop(4))
                        .child(createSourceSelector(sourceSync)));
    }

    private Flow createVoltageRow(IPanelHandler panel, LongSyncValue voltage) {
        return Flow.row()
                .height(18)
                .marginBottom(4)
                .child(IKey.str("Voltage").asWidget()
                        .marginRight(4)
                        .width(50)
                        .verticalCenter())
                .child(new TextFieldWidget()
                        .setTextAlignment(Alignment.CENTER)
                        .setNumbersLong(() -> 1, () -> Long.MAX_VALUE)
                        .value(voltage))
                .child(new ButtonWidget<>()
                        .height(18)
                        .width(40)
                        .overlay(IKey.dynamic(
                                () -> Component.literal(GTValues.VNF[GTUtil.getTierByVoltage(voltage.getLongValue())]))
                                .shadow(true))

                        // .width(32)
                        .marginLeft(4)
                        .tooltip(new RichTooltip().add("Click to Change Tier"))
                        .onMousePressed((a, b, c) -> {
                            if (panel.isPanelOpen()) {
                                panel.closePanel();
                            } else {
                                panel.openPanel();
                            }
                            return true;
                        })

                );
    }

    static Flow createAmpRow(IntSyncValue amps) {
        return Flow.row()
                .coverChildrenHeight()
                .child(IKey.lang("gtceu.creative.energy.amperage").asWidget()
                        .marginRight(4)
                        .verticalCenter().width(50))
                .child(
                        new TextFieldWidget()
                                .setTextAlignment(Alignment.CENTER)
                                .setNumbers(1, Integer.MAX_VALUE)
                                .value(amps)
                                .setDefaultNumber(1))
                .child(new ButtonWidget<>()
                        .overlay(new DynamicDrawable(() -> {
                            MouseData mouseData = MouseData.create(-1);
                            if (mouseData.shift()) {
                                return IKey.str("1/2x");
                            } else if (mouseData.ctrl()) {
                                return IKey.str("4x");
                            } else {
                                return IKey.str("2x");
                            }

                        }))
                        .width(40)
                        .height(18)
                        .tooltip(new RichTooltip().addLine("Click to Double Amperage")
                                .addLine("Shift to half current Amperage"))
                        .onMousePressed((a, b, c) -> {
                            MouseData mouseData = MouseData.create(c);
                            if (mouseData.shift()) {
                                amps.setValue(amps.getValue() / 2);
                            } else if (mouseData.ctrl()) {
                                amps.setValue(amps.getValue() * 4);
                            } else {
                                amps.setValue(amps.getValue() * 2);
                            }
                            return true;
                        })
                        .marginLeft(4));
    }

    private Flow createSourceSelector(BooleanSyncValue sourceSync) {
        return Flow.column()
                .coverChildrenHeight()
                .child(Flow.row()
                        .coverChildrenHeight()
                        .name("button")
                        .childPadding(2)
                        .child(new ToggleButton()
                                .overlay(new DynamicDrawable(() -> {
                                    if (sourceSync.getValue()) {
                                        return GuiTextures.CHECK_BOX.getSubArea(0, .5f, 1, 1f);
                                    }
                                    return IDrawable.EMPTY;
                                }))
                                .value(sourceSync))
                        .child(IKey.lang("gtceu.creative.energy.source").asWidget())
                        .paddingBottom(2))
                .child(Flow.row()
                        .coverChildrenHeight()
                        .name("button")
                        .coverChildrenHeight()
                        .childPadding(2)
                        .child(new ToggleButton()
                                .overlay(new DynamicDrawable(() -> {
                                    if (!sourceSync.getValue()) {
                                        return GuiTextures.CHECK_BOX.getSubArea(0, .5f, 1, 1f);
                                    }
                                    return IDrawable.EMPTY;
                                }))
                                .value(new BooleanSyncValue(() -> !source, bool -> source = !bool)))
                        .child(IKey.lang("gtceu.creative.energy.sink").asWidget()));
    }

    private ModularPanel<?> createAmpSelector(LongSyncValue voltage, IntSyncValue tier) {
        return new Dialog<>("amp_selector")
                .disablePanelsBelow(false)
                .draggable(true)
                .closeOnOutOfBoundsClick(true)
                .width(72)
                .height(104)
                .child(Flow.column()
                        .child(IKey.lang("gtceu.top.cable_voltage").asWidget().top(4).left(3))
                        .child(new Rectangle().color(0xFF555555).asWidget()
                                .height(1).widthRel(0.80f).horizontalCenter().top(19))
                        .child(new ListWidget<>()
                                .widthRel(1.0f)
                                .height(120)
                                .maxSize(80)
                                .crossAxisAlignment(Alignment.CrossAxis.CENTER)
                                .children(GTValues.TIER_COUNT, v -> new ButtonWidget<>()
                                        .width(36)
                                        .overlay(IKey.lang(Component.literal(GTValues.VNF[v])))
                                        .onMousePressed((x, y, b) -> {
                                            voltage.setValue(GTValues.V[v]);
                                            tier.setValue(v);
                                            return true;
                                        }))
                                .top(20))
                        .child(new Rectangle().color(0xFF555555).asWidget()
                                .height(1).widthRel(0.80f).horizontalCenter().top(99)));
    }
}

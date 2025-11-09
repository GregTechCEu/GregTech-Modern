package com.gregtechceu.gtceu.common.machine.storage;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.capability.ILaserContainer;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.TieredMachine;
import com.gregtechceu.gtceu.api.machine.feature.IMuiMachine;
import com.gregtechceu.gtceu.api.mui.base.IPanelHandler;
import com.gregtechceu.gtceu.api.mui.base.drawable.IIcon;
import com.gregtechceu.gtceu.api.mui.base.drawable.IKey;
import com.gregtechceu.gtceu.api.mui.base.widget.IWidget;
import com.gregtechceu.gtceu.api.mui.drawable.ItemDrawable;
import com.gregtechceu.gtceu.api.mui.drawable.Rectangle;
import com.gregtechceu.gtceu.api.mui.factory.PosGuiData;
import com.gregtechceu.gtceu.api.mui.utils.Alignment;
import com.gregtechceu.gtceu.api.mui.utils.MouseData;
import com.gregtechceu.gtceu.api.mui.value.sync.IntSyncValue;
import com.gregtechceu.gtceu.api.mui.value.sync.LongSyncValue;
import com.gregtechceu.gtceu.api.mui.value.sync.PanelSyncManager;
import com.gregtechceu.gtceu.api.mui.value.sync.SyncHandlers;
import com.gregtechceu.gtceu.api.mui.widgets.ButtonWidget;
import com.gregtechceu.gtceu.api.mui.widgets.ListWidget;
import com.gregtechceu.gtceu.api.mui.widgets.layout.Column;
import com.gregtechceu.gtceu.api.mui.widgets.layout.Flow;
import com.gregtechceu.gtceu.api.mui.widgets.layout.Row;
import com.gregtechceu.gtceu.api.mui.widgets.textfield.TextFieldWidget;
import com.gregtechceu.gtceu.client.mui.screen.ModularPanel;
import com.gregtechceu.gtceu.client.mui.screen.RichTooltip;
import com.gregtechceu.gtceu.client.mui.screen.UISettings;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.utils.GTUtil;

import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

import javax.annotation.ParametersAreNonnullByDefault;

import static com.gregtechceu.gtceu.common.mui.GTGuis.defaultPopupPanel;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CreativeEnergyContainerMachine extends TieredMachine implements ILaserContainer, IMuiMachine {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            CreativeEnergyContainerMachine.class, MetaMachine.MANAGED_FIELD_HOLDER);

    @Persisted
    @Setter
    private long voltage = 0;
    @Persisted
    @Getter
    @Setter
    private int amps = 1;
    @Persisted
    @Getter
    @Setter
    private int tier = 0;
    @Persisted
    private boolean active = false;
    @Persisted
    private boolean source = true;
    @Persisted
    private long energyIOPerSec = 0;
    private long lastAverageEnergyIOPerTick = 0;
    private long ampsReceived = 0;
    private boolean doExplosion = false;

    public CreativeEnergyContainerMachine(IMachineBlockEntity holder) {
        super(holder, GTValues.MAX);
    }

    //////////////////////////////////////
    // ***** Initialization ******//
    //////////////////////////////////////
    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        subscribeServerTick(this::updateEnergyTick);
    }

    public long getVoltage() {
        return voltage * amps;
    }


    //////////////////////////////////////
    // ********** MISC ***********//
    //////////////////////////////////////

    protected void updateEnergyTick() {
        if (getOffsetTimer() % 20 == 0) {
            this.setIOSpeed(energyIOPerSec / 20);
            energyIOPerSec = 0;
            if (doExplosion) {
                getLevel().explode(null, getPos().getX() + 0.5, getPos().getY() + 0.5, getPos().getZ() + 0.5,
                        1, Level.ExplosionInteraction.NONE);
                doExplosion = false;
            }
        }
        ampsReceived = 0;
        if (!active || !source || voltage <= 0 || amps <= 0) return;
        int ampsUsed = 0;
        for (var facing : GTUtil.DIRECTIONS) {
            var opposite = facing.getOpposite();
            IEnergyContainer container = GTCapabilityHelper.getEnergyContainer(getLevel(), getPos().relative(facing),
                    opposite);
            // Try to get laser capability
            if (container == null)
                container = GTCapabilityHelper.getLaser(getLevel(), getPos().relative(facing), opposite);

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
        if (source || !active || ampsReceived >= amps) {
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
        if (source || !active) {
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
    public ModularPanel buildUI(PosGuiData data, PanelSyncManager syncManager, UISettings settings) {


        //syncing
        LongSyncValue voltage = new LongSyncValue(this::getVoltage, this::setVoltage);
        IntSyncValue amps = new IntSyncValue(this::getAmps, this::setAmps);
        IntSyncValue tier = new IntSyncValue(this::getTier, this::setTier);
        syncManager.syncValue("tier", tier);


        IPanelHandler panelSyncHandler = syncManager.panel("voltage popup", (manager, handler) -> createAmpSelector(voltage, tier), false);

        return new ModularPanel("main panel")
                .height(176)
                .width(166)
                .background(GTGuiTextures.BACKGROUND)
                .child( new Column()
                        .coverChildrenHeight()
                        .child(new Row()
                                .alignX(0)
                                .marginBottom(4)
                                .height(16)
                                .child(new ItemDrawable(this.getDefinition()
                                        .getItem())
                                        .asWidget()
                                        .size(16)
                                        .marginRight(4)
                                )
                                .child(IKey.lang(this.getDefinition().getName())
                                        .asWidget()
                                        .heightRel(1)
                                )
                        )

                        .widthRel(1)
                        .name("main")
                        .padding(7)
                        .mainAxisAlignment(Alignment.MainAxis.START)
                        .child(new Row()
                                .child(new TextFieldWidget()
                                        .setTextAlignment(Alignment.CENTER)
                                        .setNumbersLong( () -> 1, () -> Long.MAX_VALUE)
                                        .value(voltage)
                                )
                                        .child(new ButtonWidget<>()
                                                .overlay(IKey.dynamic(() -> {
                                                    int voltageTier = GTUtil.getTierByVoltage(voltage.getLongValue());
                                                    return Component.literal(GTValues.VNF[voltageTier]);
                                                        })
                                                        .shadow(true)
                                                        .asIcon())
                                                .height(16)
                                                .width(32)
                                                .marginLeft(4)

                                                .tooltip(new RichTooltip().add("Click to Change Tier"))
                                                .onMousePressed((a,b,c) -> {
                                                    if(panelSyncHandler.isPanelOpen()) {

                                                        panelSyncHandler.closePanel();
                                                    }
                                                    else{
                                                        panelSyncHandler.openPanel();
                                                    }
                                                    return true;
                                                })
                                        )
                                .child(IKey.str("Voltage").asWidget()
                                        .anchorRight(0)
                                        .verticalCenter()

                                )
                        )
                        .child(new Row()
                                        .child(
                                        new TextFieldWidget()
                                                .setTextAlignment(Alignment.CENTER)

                                                .setNumbers(1, Integer.MAX_VALUE)
                                                .value(amps)
                                                .setDefaultNumber(1)
                                        )

                                .child(IKey.str("Amperage")
                                        .asWidget()
                                        .anchorRight(0)
                                        .verticalCenter()
                                )
                                .child(new ButtonWidget<>()
                                        .size(16)
                                        .tooltip(new RichTooltip().addLine("Click to Double Amperage")
                                                .addLine("Shift to half current Amperage")
                                        )
                                        .onMousePressed((a,b,c) ->{
                                            MouseData mouseData = MouseData.create(c);
                                            if(mouseData.shift()) {
                                                amps.setValue(amps.getValue() / 2);
                                            }
                                            else{
                                                amps.setValue(amps.getValue() * 2);
                                            }
                                            return true;
                                        })
                                        .marginLeft(4 + 32 / 4 )
                                )
                        )
                        .child(new Rectangle().setColor( 0xFF555555).asWidget()
                                .height(1).widthRel(0.9f).marginBottom(4).marginTop(4))
                        .child(new Row()
                                .coverChildrenWidth()
                                .horizontalCenter()
                                .child(new ButtonWidget<>()
                                        )
                                .child(new ButtonWidget()
                                )
                        )
                );


    }
    private ModularPanel createAmpSelector(LongSyncValue syncer, IntSyncValue tier) {
        int buttonHeight = 16;
        ListWidget<IWidget, ?> list = new ListWidget<>();
        for (int i = 0; i < GTValues.TIER_COUNT; i++) {
            String tierName = GTValues.VNF[i];
            long tierVoltage = GTValues.V[i];
            int tierValue  = i;
            list.child(new ButtonWidget<>().onMousePressed((a, b, c) -> {
                                syncer.setLongValue(tierVoltage);
                                tier.setIntValue(tierValue);
                                return true;
                            })
                            .overlay(IKey.dynamic(() -> Component.literal(tierName)))
                            .size(48, buttonHeight))
                    .alignX(0F);
        }

        return defaultPopupPanel("voltageSelector:")
                .child(new Column()
                        .child(list.size(54, buttonHeight * 5).childSeparator(IIcon.EMPTY_2PX))
                        .margin(5)
                        .childPadding(3)
                        .coverChildren())
                .coverChildren();
    }

//    @Override
//    public ModularUI createUI(Player entityPlayer) {
//        return new ModularUI(176, 166, this, entityPlayer)
//                .background(GuiTextures.BACKGROUND)
//                .widget(new LabelWidget(7, 32, "gtceu.creative.energy.voltage"))
//                .widget(new TextFieldWidget(9, 47, 152, 16, () -> String.valueOf(voltage),
//                        value -> {
//                            voltage = Long.parseLong(value);
//                            setTier = GTUtil.getTierByVoltage(voltage);
//                        }).setNumbersOnly(0L, Long.MAX_VALUE))
//                .widget(new LabelWidget(7, 74, "gtceu.creative.energy.amperage"))
//                .widget(new ButtonWidget(7, 87, 20, 20,
//                        new GuiTextureGroup(ResourceBorderTexture.BUTTON_COMMON, new TextTexture("-")),
//                        cd -> amps = --amps == -1 ? 0 : amps))
//                .widget(new TextFieldWidget(31, 89, 114, 16, () -> String.valueOf(amps),
//                        value -> amps = Integer.parseInt(value)).setNumbersOnly(0, Integer.MAX_VALUE))
//                .widget(new ButtonWidget(149, 87, 20, 20,
//                        new GuiTextureGroup(ResourceBorderTexture.BUTTON_COMMON, new TextTexture("+")),
//                        cd -> {
//                            if (amps < Integer.MAX_VALUE) {
//                                amps++;
//                            }
//                        }))
//                .widget(new LabelWidget(7, 110,
//                        () -> "Average Energy I/O per tick: " + this.lastAverageEnergyIOPerTick))
//                .widget(new SwitchWidget(7, 139, 77, 20, (clickData, value) -> active = value)
//                        .setTexture(
//                                new GuiTextureGroup(ResourceBorderTexture.BUTTON_COMMON,
//                                        new TextTexture("gtceu.creative.activity.off")),
//                                new GuiTextureGroup(ResourceBorderTexture.BUTTON_COMMON,
//                                        new TextTexture("gtceu.creative.activity.on")))
//                        .setPressed(active))
//                .widget(new SwitchWidget(85, 139, 77, 20, (clickData, value) -> {
//                    source = value;
//                    if (source) {
//                        voltage = 0;
//                        amps = 0;
//                        setTier = 0;
//                    } else {
//                        voltage = GTValues.V[14];
//                        amps = Integer.MAX_VALUE;
//                        setTier = 14;
//                    }
//                }).setTexture(
//                        new GuiTextureGroup(ResourceBorderTexture.BUTTON_COMMON,
//                                new TextTexture("gtceu.creative.energy.sink")),
//                        new GuiTextureGroup(ResourceBorderTexture.BUTTON_COMMON,
//                                new TextTexture("gtceu.creative.energy.source")))
//                        .setPressed(source))
//                .widget(new SelectorWidget(7, 7, 50, 20, Arrays.stream(GTValues.VNF).toList(), -1)
//                        .setOnChanged(tier -> {
//                            setTier = ArrayUtils.indexOf(GTValues.VNF, tier);
//                            voltage = GTValues.VEX[setTier];
//                        })
//                        .setSupplier(() -> GTValues.VNF[setTier])
//                        .setButtonBackground(ResourceBorderTexture.BUTTON_COMMON)
//                        .setBackground(ColorPattern.BLACK.rectTexture())
//                        .setValue(GTValues.VNF[setTier]));
//    }
}

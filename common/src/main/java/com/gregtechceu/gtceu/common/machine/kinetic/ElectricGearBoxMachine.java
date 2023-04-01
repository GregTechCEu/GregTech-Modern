package com.gregtechceu.gtceu.common.machine.kinetic;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.UITemplate;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.TieredEnergyMachine;
import com.gregtechceu.gtceu.api.machine.feature.IUIMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableEnergyContainer;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.texture.ColorBorderTexture;
import com.lowdragmc.lowdraglib.gui.texture.ColorRectTexture;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import lombok.Getter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * @author KilaBash
 * @date 2023/3/31
 * @implNote ElectricGearBoxMachine
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ElectricGearBoxMachine extends TieredEnergyMachine implements IKineticMachine, IUIMachine {
    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(ElectricGearBoxMachine.class, TieredEnergyMachine.MANAGED_FIELD_HOLDER);
    public final int maxAmps;
    @Getter
    @Persisted @DescSynced
    protected int currentAmps;

    public ElectricGearBoxMachine(IMachineBlockEntity holder, int tier, int maxAmps) {
        super(holder, tier, maxAmps);
        this.maxAmps = maxAmps;
        this.currentAmps = maxAmps;
    }

    //////////////////////////////////////
    //*****     Initialization     *****//
    //////////////////////////////////////

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    protected NotifiableEnergyContainer createEnergyContainer(Object... args) {
        var tierVoltage = GTValues.V[tier];
        var amps = (int)args[0];
        NotifiableEnergyContainer container;
        if (isEnergyEmitter()) {
            container = NotifiableEnergyContainer.emitterContainer(this, tierVoltage * 64L, tierVoltage, amps);
            container.setSideOutputCondition(dir -> dir.getAxis() != getRotationFacing().getAxis());
        } else {
            container = NotifiableEnergyContainer.receiverContainer(this, tierVoltage * 64L, tierVoltage, amps);
            container.setSideInputCondition(dir -> dir.getAxis() != getRotationFacing().getAxis());
        }
        container.setCapabilityValidator(dir -> dir.getAxis() != getRotationFacing().getAxis());
        return container;
    }

    @Override
    protected boolean isEnergyEmitter() {
        return !getKineticDefinition().isSource();
    }

    @Override
    protected long getMaxInputOutputAmperage() {
        return maxAmps;
    }

    public void setCurrentAmps(int currentAmps) {
        this.currentAmps = Mth.clamp(currentAmps, 0 , maxAmps);
    }

    public float getCurrentRPM() {
        return getCurrentAmps() * 8;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        subscribeServerTick(this::outputRotation);
    }

    @Override
    public void onRotated(Direction oldFacing, Direction newFacing) {
        super.onRotated(oldFacing, newFacing);
        if (!isRemote()) {
            if (oldFacing.getAxis() != newFacing.getAxis()) {
                var holder = getKineticHolder();
                if (holder.hasNetwork()) {
                    holder.getOrCreateNetwork().remove(holder);
                }
                holder.detachKinetics();
                holder.removeSource();
            }
        }
    }

    //////////////////////////////////////
    //*****     Rotation Logic     *****//
    //////////////////////////////////////


    @Override
    public float getRotationSpeedModifier(Direction direction) {
        if (direction == getRotationFacing().getOpposite())
            return -1;
        return 1;
    }

    protected void outputRotation() {
        if (getKineticDefinition().isSource()) {
            if (getCurrentAmps() == 0) {
                getKineticHolder().stopWorking();
            }
            var maxCharged = getCurrentAmps() * energyContainer.getInputVoltage();
            var charged = energyContainer.removeEnergy( maxCharged);
            if (charged > 0) {
                getKineticHolder().scheduleWorking(getCurrentRPM() * getKineticDefinition().getTorque() * charged / maxCharged);
            } else {
                getKineticHolder().stopWorking();
            }
        }
    }

    @Override
    public ModularUI createUI(Player entityPlayer) {
        return new ModularUI(176, 148, this, entityPlayer)
                .background(GuiTextures.BACKGROUND)
                .widget(new LabelWidget(5, 5, getDefinition().getDescriptionId()))
                .widget(new ImageWidget(42, 20, 92, 20, new TextTexture("").setSupplier(() -> "Speed: " + getKineticHolder().workingSpeed)))
                .widget(new ButtonWidget(10, 40, 30, 20,
                        new GuiTextureGroup(GuiTextures.VANILLA_BUTTON, new TextTexture("-8rpm")), cd -> {
                    if (!cd.isRemote) {
                        int amount = cd.isCtrlClick ? cd.isShiftClick ? 32 : 16 : cd.isShiftClick ? 4 : 1;
                        setCurrentAmps(currentAmps - amount);
                    }
                }).setHoverTooltips("gui.widget.incrementButton.default_tooltip"))
                .widget(new ButtonWidget(136, 40, 30, 20,
                        new GuiTextureGroup(GuiTextures.VANILLA_BUTTON, new TextTexture("+8rpm")), cd -> {
                    if (!cd.isRemote) {
                        int amount = cd.isCtrlClick ? cd.isShiftClick ? 32 : 16 : cd.isShiftClick ? 4 : 1;
                        setCurrentAmps(currentAmps + amount);
                    }
                }).setHoverTooltips("gui.widget.incrementButton.default_tooltip"))
                .widget(new ImageWidget(42, 40, 92, 20, new GuiTextureGroup(new ColorRectTexture(0xff000000),
                        new ColorBorderTexture(1, -1), new TextTexture("").setSupplier(() -> getCurrentRPM() + "rpm"))))
                .widget(UITemplate.bindPlayerInventory(entityPlayer.getInventory(), GuiTextures.SLOT, 7, 66, true));
    }

}

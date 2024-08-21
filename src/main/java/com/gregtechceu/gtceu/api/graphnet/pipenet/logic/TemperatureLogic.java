package com.gregtechceu.gtceu.api.graphnet.pipenet.logic;

import com.gregtechceu.gtceu.api.graphnet.MultiNodeHelper;
import com.gregtechceu.gtceu.api.graphnet.NetNode;
import com.gregtechceu.gtceu.api.graphnet.logic.INetLogicEntryListener;
import com.gregtechceu.gtceu.api.graphnet.logic.NetLogicData;
import com.gregtechceu.gtceu.api.graphnet.logic.NetLogicEntry;
import com.gregtechceu.gtceu.api.graphnet.pipenet.NodeLossResult;
import com.gregtechceu.gtceu.api.graphnet.pipenet.physical.IBurnable;
import com.gregtechceu.gtceu.api.graphnet.pipenet.physical.IFreezable;
import com.gregtechceu.gtceu.api.graphnet.traverse.util.CompleteLossOperator;
import com.gregtechceu.gtceu.api.graphnet.traverse.util.MultLossOperator;
import gregtech.client.particle.GTOverheatParticle;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;

public final class TemperatureLogic extends NetLogicEntry<TemperatureLogic, CompoundTag> {

    public static final TemperatureLogic INSTANCE = new TemperatureLogic();

    public static final int DEFAULT_TEMPERATURE = 298;

    private WeakReference<INetLogicEntryListener> netListener;
    private boolean isMultiNodeHelper = false;

    private int temperatureMaximum;
    private @Nullable Integer partialBurnTemperature;
    private int temperatureMinimum;
    private float energy;
    private int thermalMass;

    private @NotNull TemperatureLossFunction temperatureLossFunction = new TemperatureLossFunction();
    private int functionPriority;
    private long lastRestorationTick;

    private TemperatureLogic() {
        super("Temperature");
    }

    public TemperatureLogic getWith(@NotNull TemperatureLossFunction temperatureRestorationFunction,
                                    int temperatureMaximum) {
        return getWith(temperatureRestorationFunction, temperatureMaximum, 1);
    }

    public TemperatureLogic getWith(@NotNull TemperatureLossFunction temperatureRestorationFunction,
                                    int temperatureMaximum, int temperatureMinimum) {
        return getWith(temperatureRestorationFunction, temperatureMaximum, temperatureMinimum, 1000);
    }

    public TemperatureLogic getWith(@NotNull TemperatureLossFunction temperatureRestorationFunction,
                                    int temperatureMaximum, int temperatureMinimum, int thermalMass) {
        return getWith(temperatureRestorationFunction, temperatureMaximum, temperatureMinimum, thermalMass, 0);
    }

    public TemperatureLogic getWith(@NotNull TemperatureLossFunction temperatureRestorationFunction,
                                    int temperatureMaximum, int temperatureMinimum, int thermalMass,
                                    @Nullable Integer partialBurnTemperature) {
        return getWith(temperatureRestorationFunction, temperatureMaximum, temperatureMinimum, thermalMass,
                partialBurnTemperature, 0);
    }

    public TemperatureLogic getWith(@NotNull TemperatureLossFunction temperatureRestorationFunction,
                                    int temperatureMaximum, int temperatureMinimum, int thermalMass,
                                    @Nullable Integer partialBurnTemperature, int functionPriority) {
        return getNew()
                .setRestorationFunction(temperatureRestorationFunction)
                .setTemperatureMaximum(temperatureMaximum)
                .setTemperatureMinimum(temperatureMinimum)
                .setThermalMass(thermalMass)
                .setPartialBurnTemperature(partialBurnTemperature)
                .setFunctionPriority(functionPriority);
    }

    @Override
    public void registerToMultiNodeHelper(MultiNodeHelper helper) {
        this.isMultiNodeHelper = true;
        this.netListener = new WeakReference<>(helper);
    }

    @Override
    public void registerToNetLogicData(NetLogicData data) {
        if (!isMultiNodeHelper) this.netListener = new WeakReference<>(data);
    }

    @Override
    public void deregisterFromNetLogicData(NetLogicData data) {
        if (!isMultiNodeHelper) this.netListener = new WeakReference<>(null);
    }

    public @NotNull TemperatureLogic getNew() {
        return new TemperatureLogic();
    }

    public boolean isOverMaximum(int temperature) {
        return temperature > getTemperatureMaximum();
    }

    public boolean isOverPartialBurnThreshold(int temperature) {
        return getPartialBurnTemperature() != null && temperature > getPartialBurnTemperature();
    }

    public boolean isUnderMinimum(int temperature) {
        return temperature < getTemperatureMinimum();
    }

    @Nullable
    public NodeLossResult getLossResult(long tick) {
        int temp = getTemperature(tick);
        if (isUnderMinimum(temp)) {
            return new NodeLossResult(n -> {
                World world = n.getNet().getLevel();
                BlockPos pos = n.getEquivalencyData();
                IBlockState state = world.getBlockState(pos);
                if (state.getBlock() instanceof IFreezable freezable) {
                    freezable.fullyFreeze(state, world, pos);
                } else {
                    world.setBlockToAir(pos);
                }
            }, CompleteLossOperator.INSTANCE);
        } else if (isOverMaximum(temp)) {
            return new NodeLossResult(n -> {
                World world = n.getNet().getLevel();
                BlockPos pos = n.getEquivalencyData();
                IBlockState state = world.getBlockState(pos);
                if (state.getBlock() instanceof IBurnable burnable) {
                    burnable.fullyBurn(state, world, pos);
                } else {
                    world.setBlockToAir(pos);
                }
            }, CompleteLossOperator.INSTANCE);
        } else if (isOverPartialBurnThreshold(temp)) {
            return new NodeLossResult(n -> {
                World world = n.getNet().getLevel();
                BlockPos pos = n.getEquivalencyData();
                IBlockState state = world.getBlockState(pos);
                if (state.getBlock() instanceof IBurnable burnable) {
                    burnable.partialBurn(state, world, pos);
                }
            }, MultLossOperator.TENTHS[5]);
        } else {
            return null;
        }
    }

    public void applyThermalEnergy(float energy, long tick) {
        restoreTemperature(tick);
        this.energy += energy;
        // since the decay logic is synced and deterministic,
        // the only time client and server will desync is on external changes.
        INetLogicEntryListener listener = this.netListener.get();
        if (listener != null) listener.markLogicEntryAsUpdated(this, false);
    }

    public void moveTowardsTemperature(int temperature, long tick, float mult, boolean noParticle) {
        int temp = getTemperature(tick);
        float thermalEnergy = mult * (temperature - temp);
        if (noParticle) {
            float thermalMax = this.thermalMass * (GTOverheatParticle.TEMPERATURE_CUTOFF - DEFAULT_TEMPERATURE);
            if (thermalMax > this.energy) return;
            if (thermalEnergy + this.energy > thermalMax) {
                thermalEnergy = thermalMax - this.energy;
            }
        }
        if (thermalEnergy > 0) applyThermalEnergy(thermalEnergy, tick);
    }

    public int getTemperature(long tick) {
        restoreTemperature(tick);
        return (int) (this.energy / this.thermalMass) + DEFAULT_TEMPERATURE;
    }

    private void restoreTemperature(long tick) {
        long timePassed = tick - lastRestorationTick;
        this.lastRestorationTick = tick;
        float energy = this.energy;
        if (timePassed != 0) {
            if (timePassed >= Integer.MAX_VALUE || timePassed < 0) {
                this.energy = 0;
            } else this.energy = temperatureLossFunction
                    .restoreTemperature(energy, (int) timePassed);
        }
    }

    public TemperatureLogic setRestorationFunction(TemperatureLossFunction temperatureRestorationFunction) {
        this.temperatureLossFunction = temperatureRestorationFunction;
        return this;
    }

    public TemperatureLossFunction getRestorationFunction() {
        return temperatureLossFunction;
    }

    public TemperatureLogic setFunctionPriority(int functionPriority) {
        this.functionPriority = functionPriority;
        return this;
    }

    public int getFunctionPriority() {
        return functionPriority;
    }

    public TemperatureLogic setTemperatureMaximum(int temperatureMaximum) {
        this.temperatureMaximum = temperatureMaximum;
        return this;
    }

    public int getTemperatureMaximum() {
        return temperatureMaximum;
    }

    public TemperatureLogic setPartialBurnTemperature(@Nullable Integer partialBurnTemperature) {
        this.partialBurnTemperature = partialBurnTemperature;
        return this;
    }

    public @Nullable Integer getPartialBurnTemperature() {
        return partialBurnTemperature;
    }

    public TemperatureLogic setTemperatureMinimum(int temperatureMinimum) {
        this.temperatureMinimum = temperatureMinimum;
        return this;
    }

    public int getTemperatureMinimum() {
        return temperatureMinimum;
    }

    public TemperatureLogic setThermalMass(int thermalMass) {
        this.thermalMass = thermalMass;
        return this;
    }

    public int getThermalMass() {
        return thermalMass;
    }

    @Override
    public boolean mergedToMultiNodeHelper() {
        return true;
    }

    @Override
    public void merge(NetNode otherOwner, NetLogicEntry<?, ?> unknown) {
        if (!(unknown instanceof TemperatureLogic other)) return;
        if (other.getTemperatureMinimum() > this.getTemperatureMinimum())
            this.setTemperatureMinimum(other.getTemperatureMinimum());
        if (other.getTemperatureMaximum() < this.getTemperatureMaximum())
            this.setTemperatureMaximum(other.getTemperatureMaximum());
        // since merge also occurs during nbt load, ignore the other's thermal energy.
        if (other.getThermalMass() < this.getThermalMass()) this.setThermalMass(other.getThermalMass());
        if (other.getFunctionPriority() > this.getFunctionPriority()) {
            this.setRestorationFunction(other.getRestorationFunction());
            this.setFunctionPriority(other.getFunctionPriority());
        }
        if (other.getPartialBurnTemperature() != null && (this.getPartialBurnTemperature() == null ||
                other.getPartialBurnTemperature() < this.getPartialBurnTemperature()))
            this.setPartialBurnTemperature(other.getPartialBurnTemperature());
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.setFloat("ThermalEnergy", this.energy);
        tag.setInteger("TemperatureMax", this.temperatureMaximum);
        tag.setInteger("TemperatureMin", this.temperatureMinimum);
        tag.setInteger("ThermalMass", this.thermalMass);
        tag.setTag("RestorationFunction", this.temperatureLossFunction.serializeNBT());
        tag.setInteger("FunctionPrio", this.functionPriority);
        if (partialBurnTemperature != null) tag.setInteger("PartialBurn", partialBurnTemperature);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        this.energy = nbt.getFloat("ThermalEnergy");
        this.temperatureMaximum = nbt.getInteger("TemperatureMax");
        this.temperatureMinimum = nbt.getInteger("TemperatureMin");
        this.thermalMass = nbt.getInteger("ThermalMass");
        this.temperatureLossFunction = new TemperatureLossFunction(nbt.getCompoundTag("RestorationFunction"));
        this.functionPriority = nbt.getInteger("FunctionPrio");
        if (nbt.hasKey("PartialBurn")) {
            this.partialBurnTemperature = nbt.getInteger("PartialBurn");
        } else this.partialBurnTemperature = null;
    }

    @Override
    public void encode(PacketBuffer buf, boolean fullChange) {
        buf.writeFloat(this.energy);
        if (fullChange) {
            buf.writeVarInt(this.temperatureMaximum);
            buf.writeVarInt(this.temperatureMinimum);
            buf.writeVarInt(this.thermalMass);
            this.temperatureLossFunction.encode(buf);
            buf.writeVarInt(this.functionPriority);
            // laughs in java 9
            // noinspection ReplaceNullCheck
            if (this.partialBurnTemperature == null) buf.writeVarInt(-1);
            else buf.writeVarInt(this.partialBurnTemperature);
        }
    }

    @Override
    public void decode(PacketBuffer buf, boolean fullChange) {
        this.lastRestorationTick = FMLCommonHandler.instance().getMinecraftServerInstance().getTickCounter();
        this.energy = buf.readFloat();
        if (fullChange) {
            this.temperatureMaximum = buf.readVarInt();
            this.temperatureMinimum = buf.readVarInt();
            this.thermalMass = buf.readVarInt();
            this.temperatureLossFunction.decode(buf);
            this.functionPriority = buf.readVarInt();
            int partialBurn = buf.readVarInt();
            if (partialBurn != -1) this.partialBurnTemperature = partialBurn;
            else this.partialBurnTemperature = null;
        }
    }
}

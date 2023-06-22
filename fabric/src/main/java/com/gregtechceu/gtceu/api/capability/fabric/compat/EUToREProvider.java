package com.gregtechceu.gtceu.api.capability.fabric.compat;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.capability.IPlatformEnergyStorage;
import com.gregtechceu.gtceu.api.capability.PlatformEnergyCompat;
import com.gregtechceu.gtceu.api.capability.fabric.GTEnergyHelperImpl;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.utils.GTUtil;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;

public class EUToREProvider extends CompatApiProvider<IEnergyContainer, EnergyStorage, Direction> {

    /**
     * Internally used RE Buffer so that a very large packet of EU is not partially destroyed
     * on the conversion to RE. This is hidden from the player, but ensures that no energy
     * is ever lost on conversion, no matter the voltage tier or RE storage abilities.
     */
    private long feBuffer;

    public EUToREProvider(BlockApiLookup.BlockApiProvider<EnergyStorage, Direction> upvalue) {
        super(upvalue);
    }

    @Override
    public @Nullable IEnergyContainer find(Level world, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, Direction context) {
        if (!ConfigHolder.INSTANCE.compat.energy.nativeEUToPlatformNative)
            return null;

        EnergyStorage energyStorage = findUpvalue(world, pos, state, blockEntity, context);
        return energyStorage != null ? new GTEnergyWrapper(energyStorage) : null;
    }


    public class GTEnergyWrapper implements IEnergyContainer {

        private final IPlatformEnergyStorage energyStorage;

        public GTEnergyWrapper(EnergyStorage energyStorage) {
            this.energyStorage = GTEnergyHelperImpl.toPlatformEnergyStorage(energyStorage);
        }

        @Override
        public long acceptEnergyFromNetwork(Direction facing, long voltage, long amperage) {

            long receive = 0;

            // Try to use the internal buffer before consuming a new packet
            if (feBuffer > 0) {

                receive = energyStorage.insert(feBuffer, true);

                if (receive == 0)
                    return 0;

                // Internal Buffer could provide the max RF the consumer could consume
                if (feBuffer > receive) {
                    feBuffer -= receive;
                    energyStorage.insert(receive, false);
                    return 0;

                    // Buffer could not provide max value, save the remainder and continue processing
                } else {
                    receive = feBuffer;
                    feBuffer = 0;
                }
            }

            long maxPacket = PlatformEnergyCompat.toNativeLong(voltage, PlatformEnergyCompat.ratio(false));
            long maximalValue = maxPacket * amperage;

            // Try to consume our remainder buffer plus a fresh packet
            if (receive != 0) {

                long consumable = energyStorage.insert(maximalValue + receive, true);

                // Machine unable to consume any power
                if (consumable == 0)
                    return 0;

                // Only able to consume our buffered amount
                if (consumable == receive) {
                    energyStorage.insert(consumable, false);
                    return 0;
                }

                // Able to consume our full packet as well as our remainder buffer
                if (consumable == maximalValue + receive) {
                    energyStorage.insert(consumable, false);
                    return amperage;
                }

                long newPower = consumable - receive;

                // Able to consume buffered amount plus an even amount of packets (no buffer needed)
                if (newPower % maxPacket == 0) {
                    return energyStorage.insert(consumable, false) / maxPacket;
                }

                // Able to consume buffered amount plus some amount of power with a packet remainder
                long ampsToConsume = (newPower / maxPacket) + 1;
                feBuffer = (maxPacket * ampsToConsume) - consumable;
                energyStorage.insert(consumable, false);
                return ampsToConsume;

                // Else try to draw 1 full packet
            } else {

                long consumable = energyStorage.insert(maximalValue, true);

                // Machine unable to consume any power
                if (consumable == 0)
                    return 0;

                // Able to accept the full amount of power
                if (consumable == maximalValue) {
                    energyStorage.insert(consumable, false);
                    return amperage;
                }

                // Able to consume an even amount of packets
                if (consumable % maxPacket == 0) {
                    return energyStorage.insert(consumable, false) / maxPacket;
                }

                // Able to consume power with some amount of power remainder in the packet
                long ampsToConsume = (consumable / maxPacket) + 1;
                feBuffer = (maxPacket * ampsToConsume) - consumable;
                energyStorage.insert(consumable, false);
                return ampsToConsume;
            }
        }

        @Override
        public long changeEnergy(long delta) {
            if (delta == 0) return 0;
            else if (delta < 0) return PlatformEnergyCompat.extractEu(energyStorage, -delta);
            else return PlatformEnergyCompat.insertEu(energyStorage, delta);
        }

        @Override
        public long getEnergyCapacity() {
            return PlatformEnergyCompat.toEu(energyStorage.getCapacity(), PlatformEnergyCompat.ratio(false));
        }

        @Override
        public long getEnergyStored() {
            return PlatformEnergyCompat.toEu(energyStorage.getAmount(), PlatformEnergyCompat.ratio(false));
        }

        /**
         * Most RF/FE cables blindly try to insert energy without checking if there is space, since the receiving IEnergyStorage should handle it.
         * This simulates that behavior in most places by allowing our "is there space" checks to pass and letting the cable attempt to insert energy.
         * If the wrapped TE actually cannot accept any more energy, the energy transfer will return 0 before any changes to our internal rf buffer.
         */
        @Override
        public long getEnergyCanBeInserted() {
            return Math.max(1, getEnergyCapacity() - getEnergyStored());
        }

        @Override
        public long getInputAmperage() {
            return getInputVoltage() == 0 ? 0 : 2;
        }

        @Override
        public long getInputVoltage() {
            long maxInput = energyStorage.insert(Integer.MAX_VALUE, true);

            if (maxInput == 0) return 0;
            return GTValues.V[GTUtil.getTierByVoltage(PlatformEnergyCompat.toEu(maxInput, PlatformEnergyCompat.ratio(false)))];
        }

        @Override
        public boolean inputsEnergy(Direction facing) {
            return energyStorage.supportsInsertion();
        }

        /**
         * Wrapped FE-consumers should not be able to output EU.
         */
        @Override
        public boolean outputsEnergy(Direction facing) {
            return false;
        }

        /**
         * Hide this BlockEntity EU-capability in TOP. Allows FE-machines to
         * "silently" accept EU without showing their charge in EU in TOP.
         * Let the machine display it in FE instead, however it chooses to.
         */
        @Override
        public boolean isOneProbeHidden() {
            return true;
        }
    }
}

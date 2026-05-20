package com.gregtechceu.gtceu.common.pipelike;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.pipenet.property.*;

public class SegmentPropertyTypes {

    // Wires
    public static SegmentPropertyType<LongSegmentProperty> MAX_VOLTAGE = new SegmentPropertyType<>(
            GTCEu.id("max_voltage"));
    public static SegmentPropertyType<IntSegmentProperty> MAX_AMPS = new SegmentPropertyType<>(GTCEu.id("max_amps"));
    public static SegmentPropertyType<IntSegmentProperty> LOSS_PER_BLOCK = new SegmentPropertyType<>(GTCEu.id("loss"));
    public static SegmentPropertyType<BoolSegmentProperty> IS_SUPERCONDUCTOR = new SegmentPropertyType<>(
            GTCEu.id("is_superconductor"));

    // Fluid pipes
    public static SegmentPropertyType<IntSegmentProperty> FLUID_THROUGHPUT = new SegmentPropertyType<>(
            GTCEu.id("fluid_throughput"));
    public static SegmentPropertyType<IntSegmentProperty> MAX_TEMPERATURE = new SegmentPropertyType<>(
            GTCEu.id("max_temperature"));
    public static SegmentPropertyType<IntSegmentProperty> CHANNELS = new SegmentPropertyType<>(
            GTCEu.id("fluid_channels"));
    public static SegmentPropertyType<BoolSegmentProperty> GAS_PROOF = new SegmentPropertyType<>(GTCEu.id("gas_proof"));
    public static SegmentPropertyType<BoolSegmentProperty> ACID_PROOF = new SegmentPropertyType<>(
            GTCEu.id("acid_proof"));
    public static SegmentPropertyType<BoolSegmentProperty> CRYO_PROOF = new SegmentPropertyType<>(
            GTCEu.id("cryo_proof"));
    public static SegmentPropertyType<BoolSegmentProperty> PLASMA_PROOF = new SegmentPropertyType<>(
            GTCEu.id("plasma_proof"));

    public static SegmentPropertyType<FloatSegmentProperty> TRANSFER_RATE = new SegmentPropertyType<>(
            GTCEu.id("transfer_rate"));
    public static SegmentPropertyType<IntSegmentProperty> PRIORITY = new SegmentPropertyType<>(GTCEu.id("priority"));
}

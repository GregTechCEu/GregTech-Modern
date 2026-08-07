package com.gregtechceu.gtceu.api.fluids;

public final class FluidConstants {

    public static final int ROOM_TEMPERATURE = 293;

    /**
     * Base liquid temperature for primarily solid materials
     */
    public static final int SOLID_LIQUID_TEMPERATURE = 1200;

    /**
     * Base plasma temperature, and offset for materials with blast temperatures when as plasma
     */
    public static final int BASE_PLASMA_TEMPERATURE = 10000;

    /**
     * Offset for materials with blast temperatures, when as liquid
     */
    public static final int LIQUID_TEMPERATURE_OFFSET = 0;

    /**
     * Offset for materials with blast temperatures, when as gases
     */
    public static final int GAS_TEMPERATURE_OFFSET = 100;

    public static final int DEFAULT_LIQUID_DENSITY = 1000;
    public static final int DEFAULT_GAS_DENSITY = -100;
    public static final int DEFAULT_PLASMA_DENSITY = -100000;
    public static final int DEFAULT_MOLTEN_DENSITY = 1500;

    /**
     * Viscosity for sticky materials
     */
    public static final int STICKY_LIQUID_VISCOSITY = 2000;

    public static final int DEFAULT_LIQUID_VISCOSITY = 1000;
    public static final int DEFAULT_GAS_VISCOSITY = 200;
    public static final int DEFAULT_PLASMA_VISCOSITY = 10;
    public static final int DEFAULT_MOLTEN_VISCOSITY = 2000;

    /**
     * Threshold for fluids to be considered cryogenic.
     * <p>
     * Temperatures lower than this are considered cryogenic.
     */
    public static final int CRYOGENIC_FLUID_THRESHOLD = 120;

    private FluidConstants() {}
}

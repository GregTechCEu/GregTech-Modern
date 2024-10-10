package com.gregtechceu.gtceu.api.graphnet.pipenet.logic;

/**
 * A bunch of loss functions. By the power of Wolfram Alpha.
 * <a href="https://www.desmos.com/calculator/vjuksr3ut0">Demonstration Graph</a>
 */
public enum LossFunction {

    // DO NOT REORDER FUNCTIONS, THE ORDER IS USED FOR NBT SERIALIZATION
    /**
     * x value is lost every tick.
     * <br>
     * A constant rate.
     */
    ARITHMETIC {

        @Override
        public float applyLoss(float value, float factorX, float factorY, int timePassed) {
            float initialThermalEnergy = value;
            value -= Math.signum(value) * factorX;
            if (value < initialThermalEnergy) return 0;
            return tolerate(value);
        }
    },
    /**
     * x% of value is lost every tick.
     * <br>
     * Faster than {@link LossFunction#ARITHMETIC} at large values, but slower at small values.
     */
    GEOMETRIC {

        @Override
        public float applyLoss(float value, float factorX, float factorY, int timePassed) {
            value *= Math.pow(1 - (factorX / 100), timePassed);
            return tolerate(value);
        }
    },
    /**
     * value is raised to the power of 1 - x every tick.
     * <br>
     * Faster than {@link LossFunction#GEOMETRIC} at large values, but incredibly slow at small values.
     */
    POWER {

        @Override
        public float applyLoss(float value, float factorX, float factorY, int timePassed) {
            value = (float) (Math.signum(value) *
                    Math.pow(Math.abs(value), Math.pow(1 - factorX, timePassed)));
            return tolerate(value);
        }
    },
    /**
     * x% of value is lost, then y more, every tick.
     * <br>
     * Slightly faster than {@link LossFunction#GEOMETRIC} at large values,
     * slightly faster than {@link LossFunction#ARITHMETIC} at small values.
     */
    GEOMETRIC_ARITHMETIC {

        @Override
        public float applyLoss(float value, float factorX, float factorY, int timePassed) {
            float initialThermalEnergy = value;

            float a = 1 - (factorX / 100);
            float b = Math.signum(value) * factorY;
            value = (float) ((b - Math.pow(a, timePassed) *
                    (-a * value + b + value)) / (a - 1));

            if (value < initialThermalEnergy) return 0;
            return tolerate(value);
        }
    },
    /**
     * value is raised to the power of 1 - x, then y% more is lost, every tick.
     * <br>
     * Slightly faster than {@link LossFunction#POWER} at large values,
     * slightly faster than {@link LossFunction#GEOMETRIC} at small values.
     */
    POWER_GEOMETRIC {

        @Override
        public float applyLoss(float value, float factorX, float factorY, int timePassed) {
            float c = 1 - factorX;
            value = (float) (Math.pow(1 - (factorY / 100), (Math.pow(c, timePassed) - 1) / (c - 1)) *
                    Math.pow(Math.abs(value), Math.pow(c, timePassed)) * Math.signum(value));
            return tolerate(value);
        }
    },
    /**
     * The evaluation of value = value - x * (value ^ y) is recursively found for every tick passed.
     */
    WEAK_SCALING {

        @Override
        public float applyLoss(float value, float factorX, float factorY, int timePassed) {
            for (int i = 0; i < timePassed; i++) {
                if (value < 0) value += factorX * Math.pow(-value, factorY);
                else if (value > 0) value -= factorX * Math.pow(value, factorY);
            }
            return tolerate(value);
        }
    };

    public static final float TOLERANCE = 0.1f;

    protected float tolerate(float value) {
        return Math.abs(value) < TOLERANCE ? 0 : value;
    }

    public abstract float applyLoss(float value, float factorX, float factorY, int timePassed);
}

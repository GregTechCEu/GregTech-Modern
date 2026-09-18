package brachy.modularui.api.drawable;

/**
 * A function which interpolates between two values.
 */
public interface IInterpolation {

    /**
     * Calculates a new value between a and b based on a curve.
     *
     * @param a start value
     * @param b end value
     * @param x progress (between 0.0 and 1.0)
     * @return new value
     */
    float interpolate(float a, float b, float x);
}

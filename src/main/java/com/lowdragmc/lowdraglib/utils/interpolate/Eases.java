package com.lowdragmc.lowdraglib.utils.interpolate;

public class Eases {

    public static final IEase LINEAR = progress -> progress;
    public static final IEase EaseQuadOut = progress -> 1 - (1 - progress) * (1 - progress);
    public static final IEase EASE_IN_OUT_CUBIC = progress -> progress < 0.5f ?
            4 * progress * progress * progress :
            1 - (float) Math.pow(-2 * progress + 2, 3) / 2;

    public static IEase easeLinear() {
        return LINEAR;
    }
}

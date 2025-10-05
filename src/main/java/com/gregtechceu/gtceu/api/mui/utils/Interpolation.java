package com.gregtechceu.gtceu.api.mui.utils;

import com.gregtechceu.gtceu.api.mui.base.drawable.IInterpolation;

import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;

import org.jetbrains.annotations.NotNull;

/**
 * Check out <a href=https://easings.net/en>this website</a> to find your desired interpolation method.
 */
public enum Interpolation implements IInterpolation, StringRepresentable {

    LINEAR("linear") {

        @Override
        public float interpolate(float a, float b, float x) {
            return Interpolations.lerp(a, b, x);
        }
    },
    QUAD_IN("quad_in") {

        @Override
        public float interpolate(float a, float b, float x) {
            return a + (b - a) * x * x;
        }
    },
    QUAD_OUT("quad_out") {

        @Override
        public float interpolate(float a, float b, float x) {
            return a - (b - a) * x * (x - 2);
        }
    },
    QUAD_INOUT("quad_inout") {

        @Override
        public float interpolate(float a, float b, float x) {
            x *= 2;

            if (x < 1F) return a + (b - a) / 2 * x * x;

            x -= 1;

            return a - (b - a) / 2 * (x * (x - 2) - 1);
        }
    },
    CUBIC_IN("cubic_in") {

        @Override
        public float interpolate(float a, float b, float x) {
            return a + (b - a) * x * x * x;
        }
    },
    CUBIC_OUT("cubic_out") {

        @Override
        public float interpolate(float a, float b, float x) {
            x -= 1;
            return a + (b - a) * (x * x * x + 1);
        }
    },
    CUBIC_INOUT("cubic_inout") {

        @Override
        public float interpolate(float a, float b, float x) {
            x *= 2;

            if (x < 1F) return a + (b - a) / 2 * x * x * x;

            x -= 2;

            return a + (b - a) / 2 * (x * x * x + 2);
        }
    },
    EXP_IN("exp_in") {

        @Override
        public float interpolate(float a, float b, float x) {
            return a + (b - a) * (float) Math.pow(2, 10 * (x - 1));
        }
    },
    EXP_OUT("exp_out") {

        @Override
        public float interpolate(float a, float b, float x) {
            return a + (b - a) * (float) (-Math.pow(2, -10 * x) + 1);
        }
    },
    EXP_INOUT("exp_inout") {

        @Override
        public float interpolate(float a, float b, float x) {
            if (x == 0) return a;
            if (x == 1) return b;

            x *= 2;

            if (x < 1F) return a + (b - a) / 2 * (float) Math.pow(2, 10 * (x - 1));

            x -= 1;

            return a + (b - a) / 2 * (float) (-Math.pow(2, -10 * x) + 2);
        }
    },
    /* Following interpolations below were copied from: https://easings.net/ */
    BACK_IN("back_in") {

        @Override
        public float interpolate(float a, float b, float x) {
            final float c1 = 1.70158F;
            final float c3 = c1 + 1;

            return Interpolations.lerp(a, b, c3 * x * x * x - c1 * x * x);
        }
    },
    BACK_OUT("back_out") {

        @Override
        public float interpolate(float a, float b, float x) {
            final float c1 = 1.70158F;
            final float c3 = c1 + 1;

            return Interpolations.lerp(a, b, 1 + c3 * (float) Math.pow(x - 1, 3) + c1 * (float) Math.pow(x - 1, 2));
        }
    },
    BACK_INOUT("back_inout") {

        @Override
        public float interpolate(float a, float b, float x) {
            final float c1 = 1.70158F;
            final float c2 = c1 * 1.525F;

            float factor = x < 0.5 ? ((float) Math.pow(2 * x, 2) * ((c2 + 1) * 2 * x - c2)) / 2 :
                    ((float) Math.pow(2 * x - 2, 2) * ((c2 + 1) * (x * 2 - 2) + c2) + 2) / 2;

            return Interpolations.lerp(a, b, factor);
        }
    },
    ELASTIC_IN("elastic_in") {

        @Override
        public float interpolate(float a, float b, float x) {
            final float c4 = (2 * (float) Math.PI) / 3;

            float factor = x == 0 ? 0 :
                    (x == 1 ? 1 : -(float) Math.pow(2, 10 * x - 10) * (float) Math.sin((x * 10 - 10.75) * c4));

            return Interpolations.lerp(a, b, factor);
        }
    },
    ELASTIC_OUT("elastic_out") {

        @Override
        public float interpolate(float a, float b, float x) {
            final float c4 = (2 * (float) Math.PI) / 3;

            float factor = x == 0 ? 0 :
                    (x == 1 ? 1 : (float) Math.pow(2, -10 * x) * (float) Math.sin((x * 10 - 0.75) * c4) + 1);

            return Interpolations.lerp(a, b, factor);
        }
    },
    ELASTIC_INOUT("elastic_inout") {

        @Override
        public float interpolate(float a, float b, float x) {
            final float c5 = (2 * (float) Math.PI) / 4.5F;

            float v = (float) Math.sin((20 * x - 11.125) * c5);
            float factor = x == 0 ? 0 : (x == 1 ? 1 :
                    (x < 0.5 ? -((float) Math.pow(2, 20 * x - 10) * v) / 2 :
                            ((float) Math.pow(2, -20 * x + 10) * v) / 2 + 1));

            return Interpolations.lerp(a, b, factor);
        }
    },
    BOUNCE_IN("bounce_in") {

        @Override
        public float interpolate(float a, float b, float x) {
            return Interpolations.lerp(a, b, 1 - BOUNCE_OUT.interpolate(0, 1, 1 - x));
        }
    },
    BOUNCE_OUT("bounce_out") {

        @Override
        public float interpolate(float a, float b, float x) {
            final float n1 = 7.5625F;
            final float d1 = 2.75F;
            float factor;

            if (x < 1 / d1) {
                factor = n1 * x * x;
            } else if (x < 2 / d1) {
                factor = n1 * (x -= 1.5F / d1) * x + 0.75F;
            } else if (x < 2.5 / d1) {
                factor = n1 * (x -= 2.25F / d1) * x + 0.9375F;
            } else {
                factor = n1 * (x -= 2.625F / d1) * x + 0.984375F;
            }

            return Interpolations.lerp(a, b, factor);
        }
    },
    BOUNCE_INOUT("bounce_inout") {

        @Override
        public float interpolate(float a, float b, float x) {
            float factor = x < 0.5 ? (1 - BOUNCE_OUT.interpolate(0, 1, 1 - 2 * x)) / 2 :
                    (1 + BOUNCE_OUT.interpolate(0, 1, 2 * x - 1)) / 2;

            return Interpolations.lerp(a, b, factor);
        }
    },
    SINE_IN("sine_in") {

        @Override
        public float interpolate(float a, float b, float x) {
            float factor = 1 - (float) Math.cos((x * Math.PI) / 2);

            return Interpolations.lerp(a, b, factor);
        }
    },
    SINE_OUT("sine_out") {

        @Override
        public float interpolate(float a, float b, float x) {
            float factor = (float) Math.sin((x * Math.PI) / 2);

            return Interpolations.lerp(a, b, factor);
        }
    },
    SINE_INOUT("sine_inout") {

        @Override
        public float interpolate(float a, float b, float x) {
            float factor = (float) (-(Math.cos(Math.PI * x) - 1) / 2);

            return Interpolations.lerp(a, b, factor);
        }
    },
    QUART_IN("quart_in") {

        @Override
        public float interpolate(float a, float b, float x) {
            float factor = x * x * x * x;

            return Interpolations.lerp(a, b, factor);
        }
    },
    QUART_OUT("quart_out") {

        @Override
        public float interpolate(float a, float b, float x) {
            float factor = 1 - (float) Math.pow(1 - x, 4);

            return Interpolations.lerp(a, b, factor);
        }
    },
    QUART_INOUT("quart_inout") {

        @Override
        public float interpolate(float a, float b, float x) {
            float factor = x < 0.5 ? 8 * x * x * x * x : 1 - (float) Math.pow(-2 * x + 2, 4) / 2;

            return Interpolations.lerp(a, b, factor);
        }
    },
    QUINT_IN("quint_in") {

        @Override
        public float interpolate(float a, float b, float x) {
            float factor = x * x * x * x * x;

            return Interpolations.lerp(a, b, factor);
        }
    },
    QUINT_OUT("quint_out") {

        @Override
        public float interpolate(float a, float b, float x) {
            float factor = 1 - (float) Math.pow(1 - x, 5);

            return Interpolations.lerp(a, b, factor);
        }
    },
    QUINT_INOUT("quint_inout") {

        @Override
        public float interpolate(float a, float b, float x) {
            float factor = x < 0.5 ? 16 * x * x * x * x * x : 1 - (float) Math.pow(-2 * x + 2, 5) / 2;

            return Interpolations.lerp(a, b, factor);
        }
    },
    CIRCLE_IN("circle_in") {

        @Override
        public float interpolate(float a, float b, float x) {
            x = Mth.clamp(x, 0, 1);

            float factor = 1 - (float) Math.sqrt(1 - Math.pow(x, 2));

            return Interpolations.lerp(a, b, factor);
        }
    },
    CIRCLE_OUT("circle_out") {

        @Override
        public float interpolate(float a, float b, float x) {
            x = Mth.clamp(x, 0, 1);

            float factor = (float) Math.sqrt(1 - Math.pow(x - 1, 2));

            return Interpolations.lerp(a, b, factor);
        }
    },
    CIRCLE_INOUT("circle_inout") {

        @Override
        public float interpolate(float a, float b, float x) {
            x = Mth.clamp(x, 0, 1);

            float factor = x < 0.5 ? (float) (1 - Math.sqrt(1 - Math.pow(2 * x, 2))) / 2 :
                    (float) (Math.sqrt(1 - Math.pow(-2 * x + 2, 2)) + 1) / 2;

            return Interpolations.lerp(a, b, factor);
        }
    };

    public final String name;

    Interpolation(String name) {
        this.name = name;
    }

    @Override
    public @NotNull String getSerializedName() {
        return this.name;
    }
}

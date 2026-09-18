package brachy.modularui;

import brachy.modularui.utils.Color;

import org.apache.commons.lang3.mutable.MutableInt;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.AssertionFailureBuilder.assertionFailure;
import static brachy.modularui.utils.Color.*;

public class ColorTest {

    @Test
    void rgbConversion() {
        // tests conversion accuracy by repeatedly converting the same color for random colors
        TestUtil.repeatRnd(10, rnd -> {
            int start = Color.random(rnd, 0);
            MutableInt color = new MutableInt(start);
            TestUtil.repeat(10, i -> {
                int c = color.intValue();
                color.setValue(argb(getRed(c), getGreen(c), getBlue(c), 0));
                assertColor(c, color.intValue(), 0, "Iteration: " + i);
            });
            assertColor(start, color.intValue(), "Final assert");
        });
    }

    @Test
    void hsvConversion() {
        TestUtil.repeatRnd(10, rnd -> {
            int start = Color.random(rnd, 0);
            MutableInt color = new MutableInt(start);
            TestUtil.repeat(10, i -> {
                int c = color.intValue();
                color.setValue(ofHSV(getHue(c), getHSVSaturation(c), getValue(c), 0));
                assertColor(c, color.intValue(), "Iteration: " + i);
            });
            assertColor(start, color.intValue(), "Final assert");
        });
    }

    @Test
    void hslConversion() {
        TestUtil.repeatRnd(10, rnd -> {
            int start = Color.random(rnd, 0);
            MutableInt color = new MutableInt(start);
            TestUtil.repeat(10, i -> {
                int c = color.intValue();
                color.setValue(ofHSL(getHue(c), getHSLSaturation(c), getLightness(c), 0));
                assertColor(c, color.intValue(), "Iteration: " + i);
            });
            assertColor(start, color.intValue(), "Final assert");
        });
    }

    @Test
    void cmykConversion() {
        TestUtil.repeatRnd(10, rnd -> {
            int start = Color.random(rnd, 0);
            MutableInt color = new MutableInt(start);
            TestUtil.repeat(10, i -> {
                int c = color.intValue();
                color.setValue(ofCMYK(getCyan(c), getMagenta(c), getYellow(c), getBlack(c)));
                assertColor(c, color.intValue(), "Iteration: " + i);
            });
            assertColor(start, color.intValue(), "Final assert");
        });
    }

    static void assertColor(int c1, int c2) {
        assertColor(c1, c2, null);
    }

    static void assertColor(int c1, int c2, int tolerance) {
        assertColor(c1, c2, tolerance, null);
    }

    static void assertColor(int c1, int c2, String extraMsg) {
        assertColor(c1, c2, 0, extraMsg);
    }

    static void assertColor(int c1, int c2, int tolerance, String extraMsg) {
        if (!Color.areSameColor(c1, c2, tolerance)) {
            throw assertionFailure()
                    .expected(Arrays.toString(Color.getARGBValues(c1)))
                    .actual(Arrays.toString(Color.getARGBValues(c2)))
                    .reason("Color components differ by " + Color.getLargestDiff(c1, c2) + ", but only " + tolerance + " is allowed. ")
                    .message(extraMsg)
                    .build();
        }
    }
}

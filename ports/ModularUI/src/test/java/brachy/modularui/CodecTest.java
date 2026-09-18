package brachy.modularui;

import brachy.modularui.api.IThemeApi;
import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.api.drawable.Text;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.drawable.Circle;
import brachy.modularui.drawable.GuiTextures;
import brachy.modularui.drawable.Rectangle;
import brachy.modularui.drawable.text.ModularComponent;
import brachy.modularui.theme.WidgetTheme;
import brachy.modularui.utils.Alignment;
import brachy.modularui.utils.Color;
import brachy.modularui.utils.serialization.codec.MutableDecoder;
import brachy.modularui.utils.serialization.codec.MutableObjectCodec;
import brachy.modularui.utils.serialization.json.JsonHelper;
import brachy.modularui.widget.Widget;
import brachy.modularui.widget.sizer.StandardResizer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.junit.jupiter.api.Test;

import java.util.Objects;
import java.util.Random;
import java.util.function.BiPredicate;
import java.util.function.Supplier;

import static org.junit.jupiter.api.AssertionFailureBuilder.assertionFailure;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CodecTest {

    @Test
    void resizer() {
        widgetTest(new Widget<>()
                .name("cool name")
                .syncHandler("sync_handler", 9)
                .padding(4, 6)
                .margin(4)
                .coverChildrenHeight(11)
                .left(5)
                .bottomRel(0.75f, -67, 0.42f)
                .width(20)
                .decoration());
    }

    @Test
    void drawable() {
        drawableTest(IDrawable.EMPTY);
        drawableTest(IDrawable.NONE);
        decodeTest(new JsonPrimitive("null"), IDrawable.CODEC, IDrawable.EMPTY);
        drawableTest(IDrawable.of(new Rectangle().color(Color.GREEN.main), GuiTextures.BOOKMARK, new Circle().color(Color.RED.main, Color.BLUE.main)));
    }

    @Test
    void text() {
        // NOTE: integer colors do not work properly when they have an alpha value due to a Minecraft bug.
        // I fixed this in TextColorMixin, but mixins are not applied in testing.
        test(ModularComponent.CODEC.mutableCodec(), Text.str("Hello"), false);
        test(ModularComponent.CODEC.mutableCodec(), Text.str("World").style(Text.UNDERLINE).color(Color.withAlpha(Color.GREEN.main, 0)), false);
        test(ModularComponent.CODEC.mutableCodec(), Text.comp(
                Text.str("Hello ").color(Color.withAlpha(Color.BLUE.main, 0)),
                Text.lang("World").scale(1.5f)).alignment(Alignment.BottomCenter), false);
    }

    @Test
    void alignment() {
        test(Alignment.CODEC, Alignment.TopLeft, true);
        test(Alignment.CODEC, Alignment.TopCenter, true);
        test(Alignment.CODEC, Alignment.TopRight, true);
        test(Alignment.CODEC, Alignment.CenterLeft, true);
        test(Alignment.CODEC, Alignment.Center, true);
        test(Alignment.CODEC, Alignment.CenterRight, true);
        test(Alignment.CODEC, Alignment.BottomLeft, true);
        test(Alignment.CODEC, Alignment.BottomCenter, true);
        test(Alignment.CODEC, Alignment.BottomRight, true);

        TestUtil.repeatRnd(10, rnd -> test(Alignment.CODEC, new Alignment(rnd.nextFloat(), rnd.nextFloat()), true));

        testEnum(Alignment.MainAxis.CODEC, Alignment.MainAxis.class);
        testEnum(Alignment.CrossAxis.CODEC, Alignment.CrossAxis.class);
    }

    @Test
    void color() {
        TestUtil.repeatRnd(10, CodecTest::testColorHex);
        TestUtil.repeatRnd(10, CodecTest::testColorRGB);
        TestUtil.repeatRnd(10, CodecTest::testColorHSV);
        TestUtil.repeatRnd(10, CodecTest::testColorHSL);
        TestUtil.repeatRnd(10, CodecTest::testColorCMYK);
    }

    @Test
    void widgetTheme() {
        test(IThemeApi.FALLBACK.getCodec().codec(), WidgetTheme.darkTextNoShadow(1, 2, GuiTextures.MENU_BACKGROUND), false);
    }

    private static void testColorHex(Random rnd) {
        int c = rnd.nextInt();
        JsonElement json = new JsonPrimitive("#" + Color.argbToFullHexString(c));
        testColor(c, json);
    }

    private static void testColorRGB(Random rnd) {
        int c = rnd.nextInt();
        JsonObject json = new JsonObject();
        json.addProperty("r", Color.getRedF(c));
        json.addProperty("g", Color.getGreenF(c));
        json.addProperty("b", Color.getBlueF(c));
        json.addProperty("a", Color.getAlphaF(c));
        testColor(c, json);
    }

    private static void testColorHSV(Random rnd) {
        int c = rnd.nextInt();
        JsonObject json = new JsonObject();
        json.addProperty("h", Color.getHue(c));
        json.addProperty("s", Color.getHSVSaturation(c));
        json.addProperty("v", Color.getValue(c));
        json.addProperty("a", Color.getAlphaF(c));
        testColor(c, json);
    }

    private static void testColorHSL(Random rnd) {
        int c = rnd.nextInt();
        JsonObject json = new JsonObject();
        json.addProperty("h", Color.getHue(c));
        json.addProperty("s", Color.getHSLSaturation(c));
        json.addProperty("l", Color.getLightness(c));
        json.addProperty("a", Color.getAlphaF(c));
        testColor(c, json);
    }

    private static void testColorCMYK(Random rnd) {
        int c = rnd.nextInt();
        JsonObject json = new JsonObject();
        json.addProperty("c", Color.getCyan(c));
        json.addProperty("m", Color.getMagenta(c));
        json.addProperty("y", Color.getYellow(c));
        json.addProperty("k", Color.getBlack(c));
        json.addProperty("a", Color.getAlphaF(c));
        testColor(c, json);
    }

    private static void testColor(int color, JsonElement json) {
        int c2 = fromJson(Color.CODEC, json);
        ColorTest.assertColor(color, c2);
        JsonElement j2 = toJson(Color.CODEC, c2);
        int c3 = fromJson(Color.CODEC, j2);
        ColorTest.assertColor(c2, c3);
    }

    private static <E extends Enum<E>> void testEnum(Codec<E> codec, Class<E> c) {
        for (E e : c.getEnumConstants()) {
            test(codec, e, true);
        }
    }

    private static void resizerTest(IWidget widget) {
        test(StandardResizer.COMPACT_CODEC, widget.resizer(), () -> new Widget<>().resizer(), false);
    }

    private static void widgetTest(Widget<?> widget) {
        test(Widget.CODEC, widget, Widget::new, true);
    }

    private static void drawableTest(IDrawable widget) {
        test(IDrawable.CODEC, widget, true);
    }

    private static <A> void test(MutableObjectCodec<A> codec, A obj1, Supplier<A> supplier, boolean checkObjEquals) {
        JsonElement json1 = toJson(codec.codec(), obj1);
        A obj2 = fromJson(codec.mutableCodec(), json1, supplier.get());
        if (checkObjEquals) {
            assertEq(codec, obj1, obj2, Objects::equals, null);
            return;
        }
        JsonElement json2 = toJson(codec.codec(), obj2);
        assertEquals(json1, json2);
        System.out.println(JsonHelper.GSON.toJson(json1));
    }

    private static <A> void test(Codec<A> codec, A obj, boolean checkObjEquals) {
        JsonElement json1 = toJson(codec, obj);
        A obj2 = fromJson(codec, json1);
        if (checkObjEquals) {
            assertEquals(obj, obj2);
            return;
        }
        JsonElement json2 = toJson(codec, obj2);
        assertEquals(json1, json2);
        System.out.println(JsonHelper.GSON.toJson(json1));
    }

    private static <A> void decodeTest(JsonElement json, Codec<A> codec, A obj) {
        assertEquals(obj, fromJson(codec, json));
    }

    public static <T> T fromJson(MutableDecoder<T> codec, JsonElement json, T instance) {
        var d = codec.parse(JsonOps.INSTANCE, json, instance);
        var err = d.error();
        assertTrue(err.isEmpty(), () -> "Expected no error, but got: " + err.get().message());
        return instance;
    }

    public static <T> T fromJson(Codec<T> codec, JsonElement json) {
        var d = codec.parse(JsonOps.INSTANCE, json);
        var err = d.error();
        assertTrue(err.isEmpty(), () -> "Expected no error, but got: " + err.get().message());
        return d.result().orElseThrow();
    }

    public static <T> JsonElement toJson(Codec<T> codec, T input) {
        var d = codec.encodeStart(JsonOps.INSTANCE, input);
        var err = d.error();
        assertTrue(err.isEmpty(), () -> "Expected no error, but got: " + err.get().message());
        return d.result().orElseThrow();
    }

    static <T> void assertEq(MutableObjectCodec<T> codec, T t1, T t2, BiPredicate<T, T> eq, String extraMsg) {
        if (!eq.test(t1, t2)) {
            throw assertionFailure()
                    .expected(codec.convertToString(t1, true))
                    .actual(codec.convertToString(t2, true))
                    .reason("Objects are not equal")
                    .message(extraMsg)
                    .build();
        }
    }
}

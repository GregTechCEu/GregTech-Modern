package brachy.modularui;

import brachy.modularui.screen.RichTooltip;
import brachy.modularui.utils.Color;

import net.minecraft.ChatFormatting;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.BooleanValue;
import net.neoforged.neoforge.common.ModConfigSpec.ConfigValue;
import net.neoforged.neoforge.common.ModConfigSpec.DoubleValue;
import net.neoforged.neoforge.common.ModConfigSpec.EnumValue;
import net.neoforged.neoforge.common.ModConfigSpec.IntValue;

import java.util.Objects;

public class ModularUIConfig {

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec CONFIG;

    static {
        BUILDER.push("ui");
    }

    public static final IntValue DEFAULT_SCROLL_SPEED = BUILDER
            .comment("Amount of pixels scrolled")
            .translation("config.modularui.defaultScrollSpeed")
            .defineInRange("defaultScrollSpeed", 30, 1, 100);
    public static final BooleanValue SMOOTH_PROGRESS_BARS = BUILDER
            .comment("If progress bars should step in texture pixels or screen pixels. (Screen pixels are way smaller and therefore smoother)")
            .translation("config.modularui.smoothProgressBars")
            .define("smoothProgressBars", false);
    public static final IntValue ANIMATION_TIME = BUILDER
            .comment("Duration of UI animations in ms.", "Default: 100")
            .translation("config.modularui.animationTime")
            .defineInRange("animationTime", 100, 0, 500);
    // Default direction
    public static final EnumValue<RichTooltip.Pos> TOOLTIP_POS = BUILDER
            .comment("Default tooltip position around the widget or its panel.")
            .translation("config.modularui.tooltipPos")
            .defineEnum("tooltipPos", RichTooltip.Pos.NEXT_TO_MOUSE);
    public static final BooleanValue ESC_RESTORES_LAST_TEXT = BUILDER
            .comment("If true, pressing ESC key in the text field will restore the last text instead of confirming current one.")
            .translation("config.modularui.escRestoresLastText")
            .define("escRestoresLastText", false);
    public static final BooleanValue USE_DARK_THEME_BY_DEFAULT = BUILDER
            .comment("If true and not specified otherwise, screens will try to use the 'vanilla_dark' theme.")
            .translation("config.modularui.useDarkThemeByDefault")
            .define("useDarkThemeByDefault", false);
    public static final BooleanValue ENABLE_TEST_GUIS = BUILDER
            .comment("Enables a test block, test item with a test gui and opening a gui by right clicking a diamond.")
            .translation("config.modularui.enableTestGuis")
            .gameRestart()
            .define("enableTestGuis", ModularUI.isDev());
    public static final BooleanValue ENABLE_TEST_OVERLAYS = BUILDER
            .comment("Enables a test overlay shown on title screen and watermark shown on every GuiContainer.")
            .translation("config.modularui.enableTestOverlays")
            .gameRestart()
            .define("enableTestOverlays", false);
    public static final BooleanValue REPLACE_VANILLA_TOOLTIPS = BUILDER
            .comment("If true, vanilla tooltip will be replaced with MUI's RichTooltip")
            .translation("config.modularui.replaceVanillaTooltips")
            .define("replaceVanillaTooltips", false);
    public static final ConfigValue<String> MOD_NAME_FORMAT = BUILDER
            .comment("The format prefix of the mod name tooltip line.", "Default: 'blue italic' (converted to §9§o)")
            .translation("config.modularui.modNameFormat")
            .define("modNameFormat", ChatFormatting.BLUE.getName() + " " + ChatFormatting.ITALIC.getName());
    public static final ConfigValue<String> DEBUG_TEXT_COLOR = BUILDER
            .comment("Debug text color. Prefix Hex values with a #. Common colors can be referred by their name.")
            .translation("config.modularui.debugTextColor")
            .define("debugTextColor", "#FFAAAAAA");
    public static final ConfigValue<String> DEBUG_OUTLINE_COLOR = BUILDER
            .comment("Debug outline color. Prefix Hex values with a #. Common colors can be referred by their name.")
            .translation("config.modularui.debugOutlineColor")
            .define("debugOutlineColor", "#DCB42873");

    static {
        BUILDER.pop().push("dev");
    }

    public static final BooleanValue DEBUG_UI = BUILDER
            .comment("Debug UI? (Will draw widget outlines and widget information)", "Default: false")
            .translation("config.modularui.dev.debugUI")
            .define("debugUI", ModularUI.isDev());
    public static final ConfigValue<String> CURSOR_COLOR = BUILDER
            .comment("Color for cursor in debug mode, in #AARRGGBB")
            .translation("config.modularui.dev.cursorColor")
            .define("cursorColor", "#FF4CAF50");
    public static final DoubleValue SCALE = BUILDER
            .comment("Scale of debug text", "Default: 0.8f")
            .translation("config.modularui.dev.debugTextScale")
            .defineInRange("scale", 0.8, 0.1, 10.0);

    public static final BooleanValue SHOW_HOVERED = BUILDER
            .translation("config.modularui.dev.showHovered")
            .define("showHovered", true);
    public static final BooleanValue SHOW_POS = BUILDER
            .translation("config.modularui.dev.showPos")
            .define("showPos", true);
    public static final BooleanValue SHOW_SIZE = BUILDER
            .translation("config.modularui.dev.showSize")
            .define("showSize", true);
    public static final BooleanValue SHOW_WIDGET_THEME = BUILDER
            .translation("config.modularui.dev.showWidgetTheme")
            .define("showWidgetTheme", true);
    public static final BooleanValue SHOW_EXTRA = BUILDER
            .translation("config.modularui.dev.showExtra")
            .define("showExtra", true);
    public static final BooleanValue SHOW_OUTLINE = BUILDER
            .translation("config.modularui.dev.showOutline")
            .define("showOutline", true);

    public static final BooleanValue SHOW_PARENT = BUILDER
            .translation("config.modularui.dev.showParent")
            .define("showParent", true);
    public static final BooleanValue SHOW_PARENT_POS = BUILDER
            .translation("config.modularui.dev.showParentPos")
            .define("showParentPos", true);
    public static final BooleanValue SHOW_PARENT_SIZE = BUILDER
            .translation("config.modularui.dev.showParentSize")
            .define("showParentSize", true);
    public static final BooleanValue SHOW_PARENT_WIDGET_THEME = BUILDER
            .translation("config.modularui.dev.showParentWidgetTheme")
            .define("showParentWidgetTheme", true);
    public static final BooleanValue SHOW_PARENT_OUTLINE = BUILDER
            .translation("config.modularui.dev.showParentOutline")
            .define("showParentOutline", true);

    static {
        BUILDER.pop();

        CONFIG = BUILDER.build();
    }

    public static int defaultScrollSpeed() {
        return DEFAULT_SCROLL_SPEED.getAsInt();
    }

    public static boolean smoothProgressBars() {
        return SMOOTH_PROGRESS_BARS.getAsBoolean();
    }

    public static int animationTime() {
        return ANIMATION_TIME.get();
    }

    public static RichTooltip.Pos tooltipPos() {
        return TOOLTIP_POS.get();
    }

    public static boolean escRestoresLastText() {
        return ESC_RESTORES_LAST_TEXT.getAsBoolean();
    }

    public static boolean useDarkThemeByDefault() {
        return USE_DARK_THEME_BY_DEFAULT.getAsBoolean();
    }

    public static boolean enableTestGuis() {
        return ENABLE_TEST_GUIS.getAsBoolean();
    }

    public static boolean enableTestOverlays() {
        return ENABLE_TEST_OVERLAYS.getAsBoolean();
    }

    public static boolean replaceVanillaTooltips() {
        return REPLACE_VANILLA_TOOLTIPS.getAsBoolean();
    }

    private static String lastValue = null;
    private static ChatFormatting[] lastParsed = null;

    public static ChatFormatting[] getModNameFormat() {
        String unparsed = MOD_NAME_FORMAT.get();
        if (!Objects.equals(unparsed, lastValue)) {
            lastValue = unparsed;
            String[] split = lastValue.split("\\s");
            lastParsed = new ChatFormatting[split.length];
            for (int i = 0; i < split.length; i++) {
                String name = split[i];
                lastParsed[i] = ChatFormatting.getByName(name);
            }
        }
        return lastParsed;
    }

    public static final class Dev {

        private Dev() {}

        public static boolean debugUI() {
            return ModularUI.isTestEnv() || DEBUG_UI.getAsBoolean();
        }

        public static int textColor() {
            return Long.decode(DEBUG_TEXT_COLOR.get()).intValue();
        }

        public static int outlineColor() {
            return Long.decode(DEBUG_OUTLINE_COLOR.get()).intValue();
        }

        public static int cursorColor() {
            return Long.decode(CURSOR_COLOR.get()).intValue();
        }

        public static float scale() {
            return SCALE.get().floatValue();
        }

        public static boolean showHovered() {
            return SHOW_HOVERED.getAsBoolean();
        }

        public static boolean showPos() {
            return SHOW_POS.getAsBoolean();
        }

        public static boolean showSize() {
            return SHOW_SIZE.getAsBoolean();
        }

        public static boolean showWidgetTheme() {
            return SHOW_WIDGET_THEME.getAsBoolean();
        }

        public static boolean showExtra() {
            return SHOW_EXTRA.getAsBoolean();
        }

        public static boolean showOutline() {
            return SHOW_OUTLINE.getAsBoolean();
        }

        public static boolean showParent() {
            return SHOW_PARENT.getAsBoolean();
        }

        public static boolean showParentPos() {
            return SHOW_PARENT_POS.getAsBoolean();
        }

        public static boolean showParentSize() {
            return SHOW_PARENT_SIZE.getAsBoolean();
        }

        public static boolean showParentWidgetTheme() {
            return SHOW_PARENT_WIDGET_THEME.getAsBoolean();
        }

        public static boolean showParentOutline() {
            return SHOW_PARENT_OUTLINE.getAsBoolean();
        }
    }
}

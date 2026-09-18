package brachy.modularui.drawable;

import brachy.modularui.ModularUI;
import brachy.modularui.api.GuiAxis;

import static brachy.modularui.drawable.UITexture.icon;

public interface GuiTextures {

    UITexture GEAR = icon("gear", 0, 0);
    UITexture MORE = icon("more", 16, 0);
    UITexture SAVED = icon("saved", 32, 0);
    UITexture SAVE = icon("save", 48, 0);
    UITexture ADD = icon("add", 64, 0);
    UITexture DUPE = icon("dupe", 80, 0);
    UITexture REMOVE = icon("remove", 96, 0);
    UITexture POSE = icon("pose", 112, 0);
    UITexture FILTER = icon("filter", 128, 0);
    UITexture MOVE_UP = icon("move_up", 144, 0, 16, 8);
    UITexture MOVE_DOWN = icon("move_down", 144, 8, 16, 8);
    UITexture LOCKED = icon("locked", 160, 0);
    UITexture UNLOCKED = icon("unlocked", 176, 0);
    UITexture COPY = icon("copy", 192, 0);
    UITexture PASTE = icon("paste", 208, 0);
    UITexture CUT = icon("cut", 224, 0);
    UITexture REFRESH = icon("refresh", 240, 0);

    UITexture DOWNLOAD = icon("download", 0, 16);
    UITexture UPLOAD = icon("upload", 16, 16);
    UITexture SERVER = icon("server", 32, 16);
    UITexture FOLDER = icon("folder", 48, 16);
    UITexture IMAGE = icon("image", 64, 16);
    UITexture EDIT = icon("edit", 80, 16);
    UITexture MATERIAL = icon("material", 96, 16);
    UITexture CLOSE = icon("close", 112, 16);
    UITexture LIMB = icon("limb", 128, 16);
    UITexture CODE = icon("code", 144, 16);
    UITexture MOVE_LEFT = icon("move_left", 144, 16, 8, 16);
    UITexture MOVE_RIGHT = icon("move_right", 152, 16, 8, 16);
    UITexture HELP = icon("help", 160, 16);
    UITexture LEFT_HANDLE = icon("left_handle", 176, 16);
    UITexture MAIN_HANDLE = icon("main_handle", 192, 16);
    UITexture RIGHT_HANDLE = icon("right_handle", 208, 16);
    UITexture REVERSE = icon("reverse", 224, 16);
    UITexture BLOCK = icon("", 240, 16);

    UITexture FAVORITE = icon("block", 0, 32);
    UITexture VISIBLE = icon("visible", 16, 32);
    UITexture INVISIBLE = icon("invisible", 32, 32);
    UITexture PLAY = icon("play", 48, 32);
    UITexture PAUSE = icon("pause", 64, 32);
    UITexture MAXIMIZE = icon("maximize", 80, 32);
    UITexture MINIMIZE = icon("minimize", 96, 32);
    UITexture STOP = icon("stop", 112, 32);
    UITexture FULLSCREEN = icon("fullscreen", 128, 32);
    UITexture ALL_DIRECTIONS = icon("all_directions", 144, 32);
    UITexture SPHERE = icon("sphere", 160, 32);
    UITexture SHIFT_TO = icon("shift_to", 176, 32);
    UITexture SHIFT_FORWARD = icon("shift_forward", 192, 32);
    UITexture SHIFT_BACKWARD = icon("shift_backward", 208, 32);
    UITexture MOVE_TO = icon("move_to", 224, 32);
    UITexture GRAPH = icon("graph", 240, 32);

    UITexture WRENCH = icon("wrench", 0, 48);
    UITexture EXCLAMATION = icon("exclamation", 16, 48);
    UITexture LEFTLOAD = icon("leftload", 32, 48);
    UITexture RIGHTLOAD = icon("rightload", 48, 48);
    UITexture BUBBLE = icon("bubble", 64, 48);
    UITexture FILE = icon("file", 80, 48);
    UITexture PROCESSOR = icon("processor", 96, 48);
    UITexture MAZE = icon("maze", 112, 48);
    UITexture BOOKMARK = icon("bookmark", 128, 48);
    UITexture SOUND = icon("sound", 144, 48);
    UITexture SEARCH = icon("search", 160, 48);
    UITexture CHECKMARK = icon("checkmark", 176, 48);

    UITexture CHECKBOARD = icon("checkboard", 0, 240);
    UITexture DISABLED = icon("disabled", 16, 240);
    UITexture CURSOR = icon("cursor", 32, 240);

    UITexture MUI_LOGO = UITexture.builder()
            .location(ModularUI.id("modular_ui_logo"))
            .imageSize(603, 603)
            .name("logo")
            .build();

    UITexture MC_BACKGROUND = UITexture.builder()
            .location(ModularUI.id("gui/background/vanilla_background"))
            .imageSize(195, 136)
            .adaptable(4)
            .name("vanilla_background")
            .defaultColorType()
            .build();

    UITexture MENU_BACKGROUND = UITexture.builder()
            .location(ModularUI.id("gui/background/menu"))
            .imageSize(18, 18)
            .adaptable(1)
            .name("menu")
            .defaultColorType()
            .build();

    UITexture MC_BUTTON = UITexture.builder()
            .location(ModularUI.id("gui/widgets/mc_button"))
            .imageSize(16, 32) // texture is 32x64, but this looks nicer
            .subAreaUV(0f, 0f, 1f, 0.5f)
            .adaptable(2).tiled()
            .name("mc_button")
            .defaultColorType()
            .build();

    UITexture MC_BUTTON_PRESSED = UITexture.builder()
            .location(ModularUI.id("gui/widgets/mc_button"))
            .imageSize(16, 32)
            .subAreaUV(0f, 0.5f, 1f, 1f)
            .adaptable(2).tiled()
            .name("mc_button_pressed")
            .defaultColorType()
            .build();

    UITexture MC_BUTTON_HOVERED = UITexture.builder()
            .location(ModularUI.id("gui/widgets/mc_button_hovered"))
            .imageSize(16, 32)
            .subAreaUV(0f, 0f, 1f, 0.5f)
            .adaptable(2).tiled()
            .name("mc_button_hovered")
            .build();

    UITexture MC_BUTTON_HOVERED_PRESSED = UITexture.builder()
            .location(ModularUI.id("gui/widgets/mc_button_hovered"))
            .imageSize(16, 32)
            .subAreaUV(0f, 0.5f, 1f, 1f)
            .adaptable(2).tiled()
            .name("mc_button_hovered_pressed")
            .build();

    UITexture MC_BUTTON_DISABLED = UITexture.builder()
            .location(ModularUI.id("gui/widgets/mc_button_disabled"))
            .imageSize(16, 16)
            .fullImage()
            .adaptable(1).tiled()
            .name("mc_button_disabled")
            .defaultColorType()
            .build();

    UITexture BUTTON_CLEAN = UITexture.builder()
            .location(ModularUI.id("gui/widgets/base_button"))
            .imageSize(18, 18)
            .adaptable(1)
            .name("vanilla_button").canApplyTheme()
            .build();

    UITexture DISPLAY = UITexture.builder()
            .location(ModularUI.id("gui/background/display"))
            .imageSize(143, 75)
            .adaptable(2)
            .name("display")
            .build();

    UITexture DISPLAY_SMALL = UITexture.builder()
            .location(ModularUI.id("gui/background/display_small"))
            .imageSize(18, 18)
            .adaptable(1)
            .name("display_small")
            .build();

    UITexture SLOT_ITEM = UITexture.builder()
            .location(ModularUI.id("gui/slot/item"))
            .imageSize(18, 18)
            .adaptable(1)
            .canApplyTheme()
            .name("slot_item")
            .build();

    UITexture SLOT_FLUID = UITexture.builder()
            .location(ModularUI.id("gui/slot/fluid"))
            .imageSize(18, 18)
            .adaptable(1)
            .canApplyTheme()
            .name("slot_fluid")
            .build();

    UITexture PROGRESS_ARROW = UITexture.builder()
            .location(ModularUI.id("gui/widgets/progress_bar_arrow"))
            .imageSize(20, 40)
            .canApplyTheme()
            .build();

    UITexture PROGRESS_CYCLE = UITexture.builder()
            .location(ModularUI.id("gui/widgets/progress_bar_mixer"))
            .imageSize(20, 40)
            .canApplyTheme()
            .build();

    UITexture CYCLE_BUTTON_DEMO = UITexture.builder()
            .location(ModularUI.id("gui/widgets/cycle_button_demo"))
            .imageSize(18, 54)
            .build();

    UITexture CHECK_BOX = UITexture.fullImage(ModularUI.id("gui/widgets/toggle_config"));
    UITexture CROSS = UITexture.fullImage(ModularUI.id("gui/icons/cross"));
    UITexture CROSS_TINY = UITexture.fullImage(ModularUI.id("gui/icons/cross_tiny"));
    UITexture CHECK_BOX_EMPTY = CHECK_BOX.getSubArea(0, 0, 1f, 0.5f);
    UITexture CHECK_BOX_FULL = CHECK_BOX.getSubArea(0, 0.5f, 1f, 1f);

    UITexture ANIMATED_TEXTURE_TEST = UITexture.fullImage(ModularUI.id("gui/icons/animated_gradient"));

    TabTexture TAB_TOP = TabTexture.of(UITexture.fullImage(ModularUI.id("gui/tab/tabs_top"), ColorType.DEFAULT), GuiAxis.Y, false, 28, 32, 4);
    TabTexture TAB_BOTTOM = TabTexture.of(UITexture.fullImage(ModularUI.id("gui/tab/tabs_bottom"), ColorType.DEFAULT), GuiAxis.Y, true, 28, 32, 4);
    TabTexture TAB_LEFT = TabTexture.of(UITexture.fullImage(ModularUI.id("gui/tab/tabs_left"), ColorType.DEFAULT), GuiAxis.X, false, 32, 28, 4);
    TabTexture TAB_RIGHT = TabTexture.of(UITexture.fullImage(ModularUI.id("gui/tab/tabs_right"), ColorType.DEFAULT), GuiAxis.X, true, 32, 28, 4);
}

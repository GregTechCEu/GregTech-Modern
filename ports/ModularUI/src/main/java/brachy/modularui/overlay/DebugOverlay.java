package brachy.modularui.overlay;

import brachy.modularui.ModularUI;
import brachy.modularui.ModularUIConfig;
import brachy.modularui.api.IMuiScreen;
import brachy.modularui.api.ITheme;
import brachy.modularui.api.drawable.IIcon;
import brachy.modularui.api.drawable.Text;
import brachy.modularui.api.value.IBoolValue;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.drawable.GuiTextures;
import brachy.modularui.drawable.NamedDrawableRow;
import brachy.modularui.drawable.Rectangle;
import brachy.modularui.screen.CustomModularScreen;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.viewport.GuiContext;
import brachy.modularui.screen.viewport.ModularGuiContext;
import brachy.modularui.utils.Color;
import brachy.modularui.utils.TreeUtil;
import brachy.modularui.utils.serialization.json.JsonHelper;
import brachy.modularui.value.BoolValue;
import brachy.modularui.widget.WidgetTree;
import brachy.modularui.widgets.ButtonWidget;
import brachy.modularui.widgets.ListWidget;
import brachy.modularui.widgets.ToggleButton;
import brachy.modularui.widgets.menu.ContextMenuButton;
import brachy.modularui.widgets.menu.Menu;

import net.neoforged.neoforge.common.ModConfigSpec;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DebugOverlay extends CustomModularScreen {

    private static final IIcon CHECKMARK = GuiTextures.CHECKMARK.asIcon().size(8);

    private final IMuiScreen parent;

    public DebugOverlay(IMuiScreen screen) {
        super(ModularUI.MOD_ID);
        this.parent = screen;
    }

    @Override
    public @NotNull ModularPanel<?> buildUI(ModularGuiContext context) {
        return new ModularPanel<>("debug")
                .fullScreenInvisible()
                .child(new ContextMenuButton<>("menu_debug_options")
                        .horizontalCenter()
                        .bottom(0)
                        .height(12)
                        .width(160)
                        .background(new Rectangle()
                                .color(Color.withAlpha(ModularUIConfig.Dev.outlineColor(), 0.4f))
                                .cornerRadius(4))
                        .disableHoverBackground()
                        .overlay(Text.str("Debug Options"))
                        .openUp()
                        .menuList(l1 -> l1
                                .name("menu_list")
                                .maxSize(100)
                                .widthRel(1f)
                                .child(new ButtonWidget<>().name("print_widget_tree_button")
                                        .height(12)
                                        .widthRel(1f)
                                        .invisible()
                                        .overlay(Text.str("Print widget trees"))
                                        .onMousePressed(this::logWidgetTrees))
                                .child(new ButtonWidget<>().name("print_resizer_tree_button")
                                        .height(12)
                                        .widthRel(1f)
                                        .invisible()
                                        .overlay(Text.str("Print resizer tree"))
                                        .onMousePressed((context1, b) -> {
                                            TreeUtil.print(parent.screen().getResizeNode());
                                            return true;
                                        }))
                                .child(new ButtonWidget<>().name("print_theme")
                                        .height(12)
                                        .widthRel(1f)
                                        .invisible()
                                        .overlay(Text.str("Print Theme json"))
                                        .onMousePressed(this::logTheme))
                                .child(new ContextMenuButton<>("menu_hover_info")
                                        .height(10)
                                        .widthRel(1f)
                                        .overlay(Text.str("Widget hover info"))
                                        .openRightUp()
                                        .menu(new Menu<>()
                                                .width(100)
                                                .coverChildrenHeight()
                                                .padding(2)
                                                .child(new ListWidget<>()
                                                        .maxSize(100)
                                                        .widthRel(1f)
                                                        .child(toggleOption(0, "Any", "showHovered"))
                                                        .child(toggleOption(1, "Pos", "showPos"))
                                                        .child(toggleOption(2, "Size", "showSize"))
                                                        .child(toggleOption(3, "Widget Theme", "showWidgetTheme"))
                                                        .child(toggleOption(4, "Extra info", "showExtra"))
                                                        .child(toggleOption(5, "Outline", "showOutline")))))
                                .child(new ContextMenuButton<>("menu_parent_hover_info")
                                        .name("menu_button_parent_hover_info")
                                        .height(10)
                                        .widthRel(1f)
                                        .overlay(Text.str("Parent widget hover info"))
                                        .openRightUp()
                                        .menu(new Menu<>()
                                                .width(100)
                                                .coverChildrenHeight()
                                                .padding(2)
                                                .child(new ListWidget<>()
                                                        .maxSize(100)
                                                        .widthRel(1f)
                                                        .child(toggleOption(10, "Any", "showParent"))
                                                        .child(toggleOption(11, "Pos", "showParentPos"))
                                                        .child(toggleOption(12, "Size", "showParentSize"))
                                                        .child(toggleOption(13, "Widget Theme", "showParentWidgetTheme"))
                                                        .child(toggleOption(14, "Outline", "showParentOutline")))))));
    }

    public static IWidget toggleOption(int i, String name, String field) {
        Object config = ModularUIConfig.CONFIG.getValues().get(List.of("dev", field));
        if (!(config instanceof ModConfigSpec.ConfigValue<?> configValue) || !(configValue.get() instanceof Boolean)) {
            throw new IllegalArgumentException("Config field 'dev.%s' is not a boolean value!".formatted(field));
        }
        @SuppressWarnings("unchecked")
        ModConfigSpec.ConfigValue<Boolean> configField = (ModConfigSpec.ConfigValue<Boolean>) config;
        IBoolValue<?> val = new BoolValue.Dynamic(configField::get, configField::set);

        return new ToggleButton()
                .name("hover_info_toggle" + i)
                .invisible()
                .widthRel(1f)
                .height(12)
                .value(val)
                .overlay(true, new NamedDrawableRow()
                        .name(Text.str(name))
                        .drawable(CHECKMARK))
                .overlay(false, new NamedDrawableRow()
                        .name(Text.str(name)));
    }

    private boolean logWidgetTrees(GuiContext context, int b) {
        for (ModularPanel<?> panel : parent.screen().getPanelManager().getOpenPanels()) {
            WidgetTree.print(panel);
        }
        return true;
    }

    private boolean logTheme(GuiContext context, int b) {
        var s = JsonHelper.toJsonString(ITheme.ENCODER, this.parent.screen().getCurrentTheme());
        ModularUI.LOGGER.info("Theme JSON of screen {}:\n{}", this.parent.screen(), s);
        return true;
    }
}

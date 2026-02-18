package com.gregtechceu.gtceu.api.mui.overlay;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.mui.base.IMuiScreen;
import com.gregtechceu.gtceu.api.mui.base.drawable.IIcon;
import com.gregtechceu.gtceu.api.mui.base.drawable.IKey;
import com.gregtechceu.gtceu.api.mui.base.value.IBoolValue;
import com.gregtechceu.gtceu.api.mui.base.widget.IWidget;
import com.gregtechceu.gtceu.api.mui.drawable.NamedDrawableRow;
import com.gregtechceu.gtceu.api.mui.drawable.Rectangle;
import com.gregtechceu.gtceu.api.mui.utils.Color;
import com.gregtechceu.gtceu.api.mui.utils.TreeUtil;
import com.gregtechceu.gtceu.api.mui.value.BoolValue;
import com.gregtechceu.gtceu.api.mui.widget.WidgetTree;
import com.gregtechceu.gtceu.api.mui.widgets.ButtonWidget;
import com.gregtechceu.gtceu.api.mui.widgets.ListWidget;
import com.gregtechceu.gtceu.api.mui.widgets.ToggleButton;
import com.gregtechceu.gtceu.api.mui.widgets.menu.ContextMenuButton;
import com.gregtechceu.gtceu.api.mui.widgets.menu.Menu;
import com.gregtechceu.gtceu.client.mui.screen.CustomModularScreen;
import com.gregtechceu.gtceu.client.mui.screen.ModularPanel;
import com.gregtechceu.gtceu.client.mui.screen.viewport.ModularGuiContext;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.config.ConfigHolder;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;

public class DebugOverlay extends CustomModularScreen {

    private static final IIcon CHECKMARK = GTGuiTextures.CHECK_BOX.asIcon().size(8);

    private final IMuiScreen parent;

    public DebugOverlay(IMuiScreen screen) {
        super(GTCEu.MOD_ID);
        this.parent = screen;
    }

    @Override
    public @NotNull ModularPanel buildUI(ModularGuiContext context) {
        return new ModularPanel("debug")
                .fullScreenInvisible()
                .child(new ContextMenuButton<>("menu_debug_options")
                        .horizontalCenter()
                        .bottom(0)
                        .height(12)
                        .width(160)
                        .background(
                                new Rectangle()
                                        .color(Color.withAlpha(
                                                Color.parseString(ConfigHolder.INSTANCE.dev.mui.outlineColor),
                                                0.4f))
                                        .cornerRadius(4))
                        .disableHoverBackground()
                        .overlay(IKey.str("Debug Options"))
                        .openUp()
                        .menuList(l1 -> l1
                                .name("menu_list")
                                .maxSize(100)
                                .widthRel(1f)
                                .child(new ButtonWidget<>().name("print_widget_tree_button")
                                        .height(12)
                                        .widthRel(1f)
                                        .invisible()
                                        .overlay(IKey.str("Print widget trees"))
                                        .onMousePressed((x, y, b) -> this.logWidgetTrees(b)))
                                .child(new ButtonWidget<>().name("print_resizer_tree_button")
                                        .height(12)
                                        .widthRel(1f)
                                        .invisible()
                                        .overlay(IKey.str("Print resizer tree"))
                                        .onMousePressed((x, y, b) -> {
                                            TreeUtil.print(parent.getScreen().getResizeNode());
                                            return true;
                                        }))
                                .child(new ContextMenuButton<>("menu_hover_info")
                                        .height(10)
                                        .widthRel(1f)
                                        .overlay(IKey.str("Widget hover info"))
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
                                        .overlay(IKey.str("Parent widget hover info"))
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
                                                        .child(toggleOption(13, "Widget Theme",
                                                                "showParentWidgetTheme"))
                                                        .child(toggleOption(14, "Outline", "showParentOutline")))))));
    }

    public static IWidget toggleOption(int i, String name, String field) {
        ConfigHolder.DeveloperConfigs.MuiConfigs c = ConfigHolder.INSTANCE.dev.mui;
        Field f;
        try {
            f = c.getClass().getField(field);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }

        IBoolValue<?> val = new BoolValue.Dynamic(() -> {
            try {
                return (boolean) f.get(c);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }, v -> {
            try {
                f.set(c, v);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });

        return new ToggleButton()
                .name("hover_info_toggle" + i)
                .invisible()
                .widthRel(1f)
                .height(12)
                .value(val)
                .overlay(true, new NamedDrawableRow()
                        .name(IKey.str(name))
                        .drawable(CHECKMARK))
                .overlay(false, new NamedDrawableRow()
                        .name(IKey.str(name)));
    }

    private boolean logWidgetTrees(int b) {
        for (ModularPanel panel : parent.getScreen().getPanelManager().getOpenPanels()) {
            WidgetTree.print(panel);
        }
        return true;
    }
}

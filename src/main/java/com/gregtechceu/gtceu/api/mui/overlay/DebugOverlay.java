package com.gregtechceu.gtceu.api.mui.overlay;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.mui.base.IMuiScreen;
import com.gregtechceu.gtceu.api.mui.base.drawable.IIcon;
import com.gregtechceu.gtceu.api.mui.base.drawable.IKey;
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
                                                Long.decode(ConfigHolder.INSTANCE.dev.mui.outlineColor).intValue(),
                                                0.4f)))
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
                                                        .child(toggleOption(0, "Any",
                                                                ConfigHolder.INSTANCE.dev.mui.showHovered))
                                                        .child(toggleOption(1, "Pos",
                                                                ConfigHolder.INSTANCE.dev.mui.showPos))
                                                        .child(toggleOption(2, "Size",
                                                                ConfigHolder.INSTANCE.dev.mui.showSize))
                                                        .child(toggleOption(3, "Widget Theme",
                                                                ConfigHolder.INSTANCE.dev.mui.showWidgetTheme))
                                                        .child(toggleOption(4, "Extra info",
                                                                ConfigHolder.INSTANCE.dev.mui.showExtra))
                                                        .child(toggleOption(5, "Outline",
                                                                ConfigHolder.INSTANCE.dev.mui.showOutline)))))
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
                                                        .child(toggleOption(10, "Any",
                                                                ConfigHolder.INSTANCE.dev.mui.showParent))
                                                        .child(toggleOption(11, "Pos",
                                                                ConfigHolder.INSTANCE.dev.mui.showParentPos))
                                                        .child(toggleOption(12, "Size",
                                                                ConfigHolder.INSTANCE.dev.mui.showParentSize))
                                                        .child(toggleOption(13, "Widget Theme",
                                                                ConfigHolder.INSTANCE.dev.mui.showParentWidgetTheme))
                                                        .child(toggleOption(14, "Outline",
                                                                ConfigHolder.INSTANCE.dev.mui.showParentOutline)))))));
    }

    public static IWidget toggleOption(int i, String name, boolean boolValue) {
        return new ToggleButton()
                .name("hover_info_toggle" + i)
                .invisible()
                .widthRel(1f)
                .height(12)
                .value(new BoolValue(boolValue))
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

package com.gregtechceu.gtceu.common.mui.widgets;

import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.mui.GTGuiScreen;
import com.gregtechceu.gtceu.api.multiblock.MultiPredicate;
import com.gregtechceu.gtceu.api.multiblock.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.multiblock.util.BlockInfo;

import net.minecraft.Util;
import net.minecraft.world.item.ItemStack;

import brachy.modularui.api.ITheme;
import brachy.modularui.api.value.IValue;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.api.widget.Interactable;
import brachy.modularui.overlay.OverlayStack;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.ModularScreen;
import brachy.modularui.theme.ThemeAPI;
import brachy.modularui.theme.WidgetThemeEntry;
import brachy.modularui.widget.Widget;
import brachy.modularui.widget.sizer.Area;
import brachy.modularui.widgets.ItemDisplayWidget;
import brachy.modularui.widgets.ListWidget;
import brachy.modularui.widgets.layout.Flow;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

public class OverlayButton extends Widget<OverlayButton> implements Interactable {

    private final MultiblockMachineDefinition definition;
    private @Nullable ModularScreen overlay;
    private boolean opened = false;
    private final Supplier<ListWidget<?, ?>> items;

    public OverlayButton(MultiblockMachineDefinition definition) {
        this.definition = definition;
        size(16);
        items = () -> {
            var first = definition.getStructurePatterns()
                    .values()
                    .stream()
                    .map(Supplier::get)
                    .filter(p -> p instanceof BlockPattern)
                    .map(p -> (BlockPattern) p)
                    .findFirst();
            if (first.isPresent()) {
                BlockPattern pattern = first.get();
                Collection<MultiPredicate> predicates = pattern.getPredicates()
                        .values().stream()
                        .filter(mp -> !mp.getCandidates().isEmpty())
                        .toList();
                return new ListWidget<>()
                        .height(160)
                        .coverChildrenWidth(16)
                        .children(predicates, p -> {
                            List<IWidget> list = p.getCandidates()
                                    .stream()
                                    .filter(c -> !c.isEmpty())
                                    .map(c -> c.stream()
                                            .map(BlockInfo::getItemStackForm)
                                            .toArray(ItemStack[]::new))
                                    .map(a -> {
                                        IValue<ItemStack> value = new IValue<>() {

                                            private final int cycleTime = 1000;

                                            @Override
                                            public ItemStack getValue() {
                                                return a[(int) (Util.getMillis() % (this.cycleTime * a.length)) /
                                                        this.cycleTime];
                                            }

                                            @Override
                                            public void setValue(ItemStack value) {}

                                            @Override
                                            public Class<ItemStack> getValueType() {
                                                return ItemStack.class;
                                            }
                                        };
                                        return (IWidget) new ItemDisplayWidget()
                                                .displayAmount(false)
                                                .item(value)
                                                .tooltipAutoUpdate(true)
                                                .tooltipDynamic(rt -> rt.addFromItem(value.getValue()));
                                    })
                                    .toList();
                            return Flow.row()
                                    .addTooltipLine(p.toString())
                                    .children(list)
                                    .height(16)
                                    .coverChildrenWidth(16);
                        });
            } else {
                return new ListWidget<>();
            }
        };
    }

    private ModularScreen constructOverlay() {
        final String ctx_theme = "modularui.context_menu";
        final ModularPanel<?> panel = ModularPanel.defaultPanel("test")
                .themeOverride(ctx_theme)
                .fullScreenInvisible()
                .child(items.get()
                        .pos(getArea().x, getArea().y + getArea().height));
        return new GTGuiScreen(panel);
    }

    @Override
    protected WidgetThemeEntry<?> getWidgetThemeInternal(ITheme theme) {
        return theme.getWidgetTheme(ThemeAPI.BUTTON);
    }

    protected ModularScreen getScreenOverlay() {
        if (overlay == null) {
            overlay = constructOverlay();
        }
        return overlay;
    }

    @Override
    @SuppressWarnings("UnstableApiUsage")
    public @NotNull Result onMousePressed(int button) {
        if (!opened) {
            ModularScreen screen = this.getScreenOverlay();
            screen.constructOverlay(getScreen().getScreenWrapper().wrappedScreen());
            OverlayStack.open(screen);
            Area screenArea = getScreen().getScreenArea();
            screen.onResize(screenArea.w(), screenArea.h());
            opened = true;
        } else {
            disposeOverlay();
            opened = false;
        }
        Interactable.playButtonClickSound();
        return Result.SUCCESS;
    }

    @Override
    public void dispose() {
        disposeOverlay();
        super.dispose();
    }

    @SuppressWarnings("UnstableApiUsage")
    private void disposeOverlay() {
        if (this.overlay != null) {
            this.overlay.close();
            OverlayStack.close(this.overlay);
            this.overlay = null;
        }
    }
}

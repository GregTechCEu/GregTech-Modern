package com.gregtechceu.gtceu.api.mui.widget.wrapper;

import com.gregtechceu.gtceu.api.mui.base.widget.IFocusedWidget;
import com.gregtechceu.gtceu.api.mui.base.widget.IWidget;
import com.gregtechceu.gtceu.api.mui.widget.EmptyWidget;
import com.gregtechceu.gtceu.client.mui.screen.ModularScreen;
import com.gregtechceu.gtceu.client.mui.screen.viewport.ModularGuiContext;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.TabOrderedElement;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@OnlyIn(Dist.CLIENT)
public class WidgetWrapper extends AbstractWidget {

    private NarratableEntry lastNarratable = null;

    private final IWidget wrapped;
    private final List<WidgetWrapper> children = new ArrayList<>();
    private final ModularScreen screen;

    public WidgetWrapper(IWidget wrapped) {
        super(0, 0, 0, 0, CommonComponents.EMPTY);
        this.wrapped = wrapped;
        this.screen = wrapped.getScreen();
        for (IWidget widget : this.wrapped.getChildren()) {
            this.children.add(new WidgetWrapper(widget));
        }
    }

    public int getX() {
        return this.wrapped.getArea().getX();
    }

    public void setX(int x) {
        this.wrapped.getArea().setX(x);
    }

    public int getY() {
        return this.wrapped.getArea().getY();
    }

    public void setY(int y) {
        this.wrapped.getArea().setY(y);
    }

    @Override
    public void visitWidgets(@NotNull Consumer<AbstractWidget> consumer) {
        if (this.wrapped instanceof EmptyWidget) return;
        if (!this.children.isEmpty()) {
            for (WidgetWrapper child : this.children) {
                child.visitWidgets(consumer);
            }
        } else {
            super.visitWidgets(consumer);
        }
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        ModularGuiContext context = this.screen.getContext();
        GuiGraphics lastGraphics = context.getGraphics();
        context.setGraphics(graphics);

        this.wrapped.draw(context, this.wrapped.getWidgetTheme(context.getTheme()));

        context.setGraphics(lastGraphics);
    }

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput output) {
        output.add(NarratedElementType.TITLE, Component.translatable(this.wrapped.getTranslationId()));
        if (!this.wrapped.isEnabled()) return;
        if (this.children.isEmpty()) {
            if (this.wrapped instanceof IFocusedWidget focusable && focusable.isFocused()) {
                output.add(NarratedElementType.USAGE, Component.translatable("narration.button.usage.focused"));
            } else if (this.wrapped.isHovering()) {
                output.add(NarratedElementType.USAGE, Component.translatable("narration.button.usage.hovered"));
            }
            return;
        }

        Stream<WidgetWrapper> entries = this.children.stream();
        updateNarrations(this.children.stream(), output, lastNarratable, entry -> lastNarratable = entry);
    }

    public static <T extends NarratableEntry> void updateNarrations(Stream<T> unsorted, NarrationElementOutput output,
                                                                    NarratableEntry lastNarratable,
                                                                    Consumer<NarratableEntry> setter) {
        List<NarratableEntry> entries = unsorted.filter(NarratableEntry::isActive)
                .sorted(Comparator.comparingInt(TabOrderedElement::getTabOrderGroup))
                .collect(Collectors.toList());
        Screen.NarratableSearchResult result = Screen.findNarratableWidget(entries, lastNarratable);
        if (result != null) {
            if (result.priority.isTerminal()) {
                setter.accept(result.entry);
            }

            if (entries.size() > 1) {
                output.add(NarratedElementType.POSITION,
                        Component.translatable("narrator.position.screen", result.index + 1, entries.size()));
                if (result.priority == NarrationPriority.FOCUSED) {
                    output.add(NarratedElementType.USAGE, Component.translatable("narration.component_list.usage"));
                }
            }

            result.entry.updateNarration(output.nest());
        }
    }
}

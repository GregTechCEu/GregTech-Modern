package brachy.modularui.utils;

import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.api.drawable.Text;
import brachy.modularui.drawable.ClientTooltipComponentIcon;
import brachy.modularui.drawable.text.FontRenderHelper;
import brachy.modularui.drawable.text.TextIcon;

import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import com.mojang.datafixers.util.Either;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

/**
 * A list that lazily parses a list of text-like and drawable types into a vanilla compatible types.
 */
public class TooltipLines extends AbstractList<Either<FormattedText, TooltipComponent>> {

    private final List<Object> elements;
    private final List<Line> lines = new ArrayList<>(8);
    private int lastElementIndex = 0;

    public TooltipLines(List<Object> elements) {
        this.elements = elements;
    }

    public void clearCache() {
        this.lines.clear();
        this.lastElementIndex = 0;
    }

    private void buildUntil(int index) {
        while (index >= lines.size()) {
            Line line = parseNext();
            if (line == null) break;
            lines.add(line);
        }
    }

    private Line parseNext() {
        if (this.lastElementIndex >= elements.size()) return null;
        List<Component> currentLine = new ArrayList<>();
        int currentLength = 0;
        for (int i = this.lastElementIndex; i < this.elements.size(); i++) {
            Object o = elements.get(i);
            currentLength++;
            if (Text.LINE_FEED.equals(o)) {
                if (currentLength == 1 && i > 0 && !Text.LINE_FEED.equals(this.elements.get(i - 1))) {
                    this.lastElementIndex++;
                    continue;
                }
                Line line = new Line(collapse(currentLine), this.lastElementIndex, currentLength);
                this.lastElementIndex += currentLength;
                return line;
            }
            Component c = null;
            if (o instanceof Component txt) {
                c = txt;
            } else if (o instanceof String str) {
                c = Component.literal(str);
            } else if (o instanceof TextIcon ti) {
                c = ti.getText();
            }
            if (c != null && !FontRenderHelper.isEmpty(c)) {
                currentLine.add(c);
                continue;
            }

            if (o instanceof IDrawable drawable && !(o instanceof TooltipComponent)) {
                o = drawable.asIcon();
            }
            if (o instanceof TooltipComponent tc) {
                Line line;
                if (currentLine.isEmpty()) {
                    line = new Line(tc, this.lastElementIndex, currentLength);
                    this.lastElementIndex += currentLength;
                } else {
                    line = new Line(collapse(currentLine), this.lastElementIndex, currentLength);
                    this.lastElementIndex += currentLength - 1;
                }
                return line;
            }
        }
        if (currentLength > 0) {
            Line line = new Line(collapse(currentLine), this.lastElementIndex, currentLength);
            this.lastElementIndex += currentLength;
            return line;
        }
        return null;
    }

    @Override
    public Either<FormattedText, TooltipComponent> get(int index) {
        buildUntil(index);
        return lines.get(index).text;
    }

    @Override
    public int size() {
        buildUntil(Integer.MAX_VALUE);
        return lines.size();
    }

    @Override
    public Either<FormattedText, TooltipComponent> remove(int index) {
        buildUntil(index);
        Line line = lines.remove(index);

        if (line.length == 1) {
            this.elements.remove(line.index);
        } else {
            this.elements.subList(line.index, line.index + line.length).clear();
        }
        for (int i = index; i < lines.size(); i++) {
            lines.get(i).index -= line.length;
        }
        this.lastElementIndex -= line.length;

        return line.text;
    }

    @Override
    public void add(int index, Either<FormattedText, TooltipComponent> s) {
        buildUntil(index);
        int elementIndex = index >= this.lines.size() ? this.lastElementIndex : this.lines.get(index).index;
        lines.add(index, new Line(s, elementIndex, 1));
        for (int i = index + 1; i < this.lines.size(); i++) {
            lines.get(i).index++;
        }
        s.ifLeft(ft -> {
            if (!(ft instanceof Component)) {
                throw new IllegalArgumentException("Tooltip text must be components");
            }
            this.elements.add(elementIndex, ft);
            this.lastElementIndex++;
        });
        // TODO support tooltip component
    }

    public void add(int index, Component s) {
        add(index, Either.left(s));
    }

    public void add(Component s) {
        add(size(), Either.left(s));
    }

    @Override
    public Either<FormattedText, TooltipComponent> set(int index, Either<FormattedText, TooltipComponent> element) {
        Line line = lines.get(index);
        element.ifLeft(ft -> {
            if (!(ft instanceof Component)) {
                throw new IllegalArgumentException("Tooltip text must be components");
            }
        });
        if (line.length == 1) {
            this.elements.set(line.index, element);
            this.lines.set(index, new Line(element, line.index, line.length));
        } else {
            remove(index);
            add(index, element);
        }
        return line.text;
    }

    @Override
    public void clear() {
        this.elements.clear();
        this.lines.clear();
        this.lastElementIndex = 0;
    }

    public List<ClientTooltipComponent> toClientTooltipComponents() {
        buildUntil(Integer.MAX_VALUE);
        return stream()
                .map(either -> either.map(TooltipLines::textToCTC, TooltipLines::tooltipComponentToCTC))
                .toList();
    }

    public static ClientTooltipComponent textToCTC(FormattedText text) {
        if (text instanceof ClientTooltipComponent ctc) return ctc;
        if (text instanceof Component component) {
            return ClientTooltipComponent.create(component.getVisualOrderText());
        }
        return ClientTooltipComponent.create(Language.getInstance().getVisualOrder(text));
    }

    public static ClientTooltipComponent tooltipComponentToCTC(TooltipComponent comp) {
        if (comp instanceof ClientTooltipComponent ctc) return ctc;
        if (comp instanceof ClientTooltipComponentIcon icon) return icon.getClientTooltipComponent();
        return ClientTooltipComponent.create(comp);
    }

    private static Component collapse(List<Component> components) {
        if (components.isEmpty()) return Text.EMPTY;
        else if (components.size() == 1) return components.getFirst();
        else return Text.comp(components.toArray(Component[]::new));
    }

    private static class Line {

        private final Either<FormattedText, TooltipComponent> text;
        private final int length;
        private int index;

        private Line(Either<FormattedText, TooltipComponent> text, int index, int length) {
            this.text = text;
            this.index = index;
            this.length = length;
        }

        private Line(FormattedText text, int index, int length) {
            this(Either.left(text), index, length);
        }

        private Line(TooltipComponent text, int index, int length) {
            this(Either.right(text), index, length);
        }
    }
}

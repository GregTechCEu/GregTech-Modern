package brachy.modularui.drawable.text;

import brachy.modularui.ModularUI;
import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.api.drawable.IIcon;
import brachy.modularui.api.drawable.IRichTextBuilder;
import brachy.modularui.api.drawable.ITextLine;
import brachy.modularui.api.drawable.Text;
import brachy.modularui.api.layout.IViewportStack;
import brachy.modularui.screen.viewport.GuiContext;
import brachy.modularui.theme.WidgetTheme;
import brachy.modularui.utils.Alignment;
import brachy.modularui.utils.TooltipLines;
import brachy.modularui.utils.serialization.codec.CodecUtil;
import brachy.modularui.utils.serialization.codec.MutableObjectCodec;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.ExtraCodecs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

public class RichText implements IDrawable, IRichTextBuilder<RichText> {

    private static final TextRenderer renderer = new TextRenderer();

    private static final Decoder<Object> RICH_ELEMENT_DECODER = CodecUtil.optionsDecoder(
            ModularComponent.CODEC.codec(), ComponentSerialization.CODEC,
            Spacer.CODEC_MAP.codec(), Codec.STRING, IDrawable.CODEC);
    private static final Encoder<Object> RICH_ELEMENT_ENCODER = new Encoder<>() {
        @Override
        public <T> DataResult<T> encode(Object input, DynamicOps<T> ops, T prefix) {
            if (input == null) return DataResult.success(ops.empty());
            if (input instanceof ModularComponent v) {
                if (v == Text.LINE_FEED) {
                    return DataResult.success(ops.createString("\\n"));
                }
                return Text.CODEC.codec().encode(v, ops, prefix);
            }
            if (input instanceof Component v) return ComponentSerialization.CODEC.encode(v, ops, prefix);
            if (input instanceof IDrawable v) return IDrawable.CODEC.encode(v, ops, prefix);
            if (input instanceof Spacer v) return Spacer.CODEC_MAP.codec().encode(v, ops, prefix);
            return DataResult.error(() -> "RichText is currently unable to encode objects of type " + input.getClass().getSimpleName());
        }
    };
    public static final Codec<Object> RICH_ELEMENT_CODEC = Codec.of(RICH_ELEMENT_ENCODER, RICH_ELEMENT_DECODER);

    public static final MutableObjectCodec<RichText> CODEC = MutableObjectCodec.drawableBuilder(RichText::new)
            .addOpt("alignment", RichText::alignment, RichText::getAlignment, Alignment.CODEC, Alignment.CenterLeft)
            .addOpt("scale", RichText::scale, RichText::getScale, Codec.FLOAT, 1f)
            .addOpt("color", RichText::textColor, RichText::getColor, CodecUtil.wrapNullsafe(Codec.INT), null)
            .addOpt("shadow", RichText::textShadow, RichText::getShadow, CodecUtil.wrapNullsafe(Codec.BOOL), null)
            .add("elements", RichText::setElements, RichText::getElementsForCodec, RICH_ELEMENT_CODEC.listOf())
            .build();

    private final List<Object> elements = new ArrayList<>();
    private TooltipLines componentList;
    @Getter private Alignment alignment = Alignment.CenterLeft;
    @Getter private float scale = 1f;
    @Getter private Integer color = null;
    @Getter private Boolean shadow = null;

    private int cursor = 0;
    private boolean cursorLocked = false;
    private List<ITextLine> cachedText;

    private boolean verified = false;

    public boolean isEmpty() {
        return this.elements.isEmpty();
    }

    public TooltipLines getAsText() {
        if (this.componentList == null) {
            this.componentList = new TooltipLines(this.elements);
        }
        return this.componentList;
    }

    private void setElements(List<Object> elements) {
        clearText();
        elements.stream().filter(Objects::nonNull).map(o -> {
            if (o instanceof String s) {
                if ("\\n".equals(s)) return Text.LINE_FEED;
                return Text.str(s);
            }
            if (o instanceof IDrawable d && !(d instanceof IIcon)) {
                return d.asIcon();
            }
            return o;
        }).forEach(this::addElement);
        verifyListElements(this.elements);
    }

    private List<Object> getElementsForCodec() {
        return this.elements;
    }

    private void clearComponents() {
        if (this.componentList != null) {
            this.componentList.clearCache();
        }
    }

    public int getMinWidth() {
        int minWidth = 12;
        for (Object o : this.elements) {
            if (o instanceof IIcon icon) {
                minWidth = Math.max(minWidth, icon.getWidth());
            }
        }
        return minWidth;
    }

    private void addElement(Object o) {
        this.elements.add(this.cursor, o);
        if (!this.cursorLocked) {
            this.cursor++;
        }
    }

    @Override
    public RichText getThis() {
        return this;
    }

    @Override
    public IRichTextBuilder<?> getRichText() {
        return this;
    }

    @Override
    public RichText reset() {
        this.elements.clear();
        this.componentList = null;
        this.alignment = Alignment.CenterLeft;
        this.scale = 1f;
        this.color = null;
        this.shadow = null;
        this.cursor = 0;
        this.cursorLocked = false;
        this.cachedText = null;
        return this;
    }

    @Override
    public RichText add(Component c) {
        addElement(c);
        clearComponents();
        return this;
    }

    @Override
    public RichText add(String s) {
        addElement(Text.str(s));
        clearComponents();
        return this;
    }

    @Override
    public RichText addDrawable(IDrawable drawable) {
        Object o = drawable;
        if (!(o instanceof Text) && !(o instanceof IIcon)) o = drawable.asIcon();
        addElement(o);
        clearComponents();
        return this;
    }

    @Override
    public RichText addLine(ITextLine line) {
        addElement(line);
        clearComponents();
        return this;
    }

    @Override
    public RichText clearText() {
        this.elements.clear();
        this.cursor = 0;
        clearComponents();
        return this;
    }

    public RichText addAll(RichText other) {
        newLine();
        this.elements.addAll(this.cursor, other.elements);
        if (!this.cursorLocked) {
            this.cursor += other.elements.size();
        }
        clearComponents();
        return this;
    }

    @Override
    public RichText alignment(Alignment alignment) {
        this.alignment = alignment;
        return this;
    }

    @Override
    public RichText textColor(int color) {
        this.color = color;
        return this;
    }

    public RichText textColor(Integer color) {
        this.color = color;
        return this;
    }

    @Override
    public RichText scale(float scale) {
        this.scale = scale;
        return this;
    }

    @Override
    public RichText textShadow(boolean shadow) {
        this.shadow = shadow;
        return this;
    }

    public RichText textShadow(Boolean shadow) {
        this.shadow = shadow;
        return this;
    }

    @Override
    public RichText moveCursorAfterElement(Pattern regex) {
        int i = findNextText(this.cursor, true, s -> regex.matcher(s).find());
        if (i < 0) i = this.elements.size();
        this.cursor = i;
        return this;
    }

    @Override
    public RichText replace(Pattern regex, UnaryOperator<Text> function) {
        int i = findNextText(this.cursor, true, s -> regex.matcher(s).find());
        if (i >= 0) {
            this.cursor = i;
            Object o = this.elements.get(i);
            Text text;
            if (o instanceof Text text1) text = text1;
            else if (o instanceof String s) text = Text.str(s);
            else if (o instanceof Component component) text = component.asModular();
            else return this;
            text = function.apply(text);
            if (text == null) {
                this.elements.remove(i);
                this.cursor--;
            } else {
                this.elements.set(i, text);
            }
        }
        return this;
    }

    @Override
    public RichText moveCursorToStart() {
        this.cursor = 0;
        return this;
    }

    @Override
    public RichText moveCursorToEnd() {
        this.cursor = this.elements.size();
        return this;
    }

    @Override
    public RichText moveCursorForward(int by) {
        this.cursor = Math.min(this.cursor + by, this.elements.size());
        return this;
    }

    @Override
    public RichText moveCursorBackward(int by) {
        this.cursor = Math.max(0, this.cursor - by);
        return this;
    }

    @Override
    public RichText lockCursor() {
        this.cursorLocked = true;
        return this;
    }

    @Override
    public RichText unlockCursor() {
        this.cursorLocked = false;
        return this;
    }

    @Override
    public RichText moveCursorToNextLine() {
        if (this.cursor < this.elements.size()) {
            this.cursor = findNextLine(this.cursor) + 1;
        }
        return this;
    }

    private int findNextLine(int current) {
        for (int i = current; i < this.elements.size(); i++) {
            Object o = this.elements.get(i);
            if (o == Text.LINE_FEED) return i;
            if (o instanceof Component key && key.getString().trim().endsWith("\n")) return i;
            if (o instanceof String string && string.trim().endsWith("\n")) return i;
            if (o instanceof ITextLine) return i;
        }
        return this.elements.size() - 1;
    }

    private int findNextText(int current, boolean wrapAround, Predicate<String> test) {
        int i = current;
        int lim = this.elements.size();
        while (i < lim) {
            Object o = this.elements.get(i);
            if (o instanceof Component key && test.test(key.getString())) return i;
            if (o instanceof String string && test.test(string)) return i;
            if (++i == lim && wrapAround) {
                i = 0;
                lim = current;
                wrapAround = false;
            }
        }
        return -1;
    }

    public RichText insertTitleMargin(int margin) {
        List<Object> objects = this.elements;
        for (int i = 0; i < objects.size(); i++) {
            Object o = objects.get(i);
            if (o == Text.LINE_FEED) {
                if (i == objects.size() - 1) return this;
                if (objects.get(i + 1) instanceof Spacer spacer) {
                    if (spacer.getSpace() == margin) return this;
                    objects.set(i + 1, Spacer.of(margin));
                } else {
                    objects.add(i + 1, Spacer.of(margin));
                }
                clearComponents();
                return this;
            }
        }
        return this;
    }

    @Override
    public void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {
        draw(context, x, y, width, height, widgetTheme.getTextColor(), widgetTheme.isTextShadow());
    }

    public void draw(GuiContext context, int x, int y, int width, int height, int color, boolean shadow) {
        draw(renderer, context, x, y, width, height, color, shadow);
    }

    public void draw(TextRenderer renderer, GuiContext context, int x, int y, int width, int height, int color, boolean shadow) {
        if (ModularUI.isDev() && !this.verified) {
            verifyListElements(this.elements);
            this.verified = true;
        }
        renderer.setSimulate(false);
        setupRenderer(renderer, x, y, width, height, color, shadow);
        this.cachedText = renderer.compileAndDraw(context, this.elements);
    }

    public int getLastWidth() {
        return (int) renderer.getLastWidth();
    }

    public int getLastHeight() {
        return (int) renderer.getLastHeight();
    }

    public void setupRenderer(TextRenderer renderer, int x, int y, float width, float height, int color, boolean shadow) {
        renderer.setPos(x, y);
        renderer.setScale(this.scale);
        renderer.setColor(this.color != null ? this.color : color);
        renderer.setShadow(this.shadow != null ? this.shadow : shadow);
        renderer.setAlignment(this.alignment, width, height);
    }

    public List<ITextLine> compileAndDraw(TextRenderer renderer, GuiContext context, boolean simulate) {
        renderer.setSimulate(simulate);
        this.cachedText = renderer.compileAndDraw(context, this.elements);
        renderer.setSimulate(false);
        return this.cachedText;
    }

    /**
     * Returns the currently hovered element of this rich text or {@code null} if none is hovered.
     * Note that this method assumes, that the {@link IViewportStack}
     * is transformed to 0,0 of this {@link IDrawable}.
     *
     * @param context the viewport stack with transformation to this widget
     * @return hovered element or null
     */
    public Object getHoveringElement(GuiContext context) {
        return getHoveringElement(context.getFont(), context.getMouseX(), context.getMouseY());
    }

    public Object getHoveringElement(Font fr, int x, int y) {
        if (this.cachedText == null) return null;

        for (ITextLine line : this.cachedText) {
            Object o = line.getHoveringElement(fr, x, y);
            if (o == null) continue;
            if (o == Boolean.FALSE) return null;
            return o;
        }
        return null;
    }

    public RichText copy() {
        RichText copy = new RichText();
        copy.copyPropertiesOf(this);
        return copy;
    }

    public void copyPropertiesOf(RichText richText) {
        this.elements.addAll(richText.elements);
        this.alignment = richText.alignment;
        this.scale = richText.scale;
        this.color = richText.color;
        this.shadow = richText.shadow;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        RichText text = (RichText) o;
        return Float.compare(scale, text.scale) == 0 &&
                Objects.equals(elements, text.elements) &&
                Objects.equals(alignment, text.alignment) &&
                Objects.equals(color, text.color) &&
                Objects.equals(shadow, text.shadow);
    }

    @Override
    public int hashCode() {
        return Objects.hash(elements, alignment, scale, color, shadow);
    }

    public static void verifyListElements(List<Object> elements) {
        List<String> errors = new ArrayList<>();
        Set<Class<?>> errored = new ObjectOpenHashSet<>();
        for (Object o : elements) {
            if (o == null) {
                if (!errored.contains(null)) {
                    errors.add("Null elements are not allowed");
                    errored.add(null);
                }
                continue;
            }
            if (o instanceof FormattedText) continue;
            if (o instanceof IIcon) continue;
            if (o instanceof ITextLine) continue;

            if (o instanceof ClientTooltipComponent && !errored.contains(ClientTooltipComponent.class)) {
                errors.add("ClientTooltipComponent must be wrapped in ClientTooltipComponentIcon");
                errored.add(ClientTooltipComponent.class);
                continue;
            }
            if (!errored.contains(o.getClass())) {
                errors.add("Element of type '" + o.getClass() + "' is not valid for RichText. Elements must implement one of these interfaces: [FormattedText, IIcon, ITextLine]");
                errored.add(o.getClass());
            }
        }
        if (!errors.isEmpty()) {
            String msg = "Found " + errored.size() + " invalid types in raw RichText.";
            ModularUI.LOGGER.error(" {} Invalid Types:", msg);
            for (String e : errors) {
                ModularUI.LOGGER.error("  - {}", e);
            }
            throw new IllegalArgumentException(msg);
        }
    }
}

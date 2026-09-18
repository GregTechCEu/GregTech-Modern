package brachy.modularui.drawable.text;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.screen.viewport.GuiContext;
import brachy.modularui.theme.WidgetTheme;
import brachy.modularui.utils.Alignment;
import brachy.modularui.utils.serialization.codec.MutableObjectCodec;
import brachy.modularui.widgets.TextWidget;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.contents.data.DataSource;
import net.minecraft.network.chat.contents.KeybindContents;
import net.minecraft.network.chat.contents.NbtContents;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.network.chat.contents.ScoreContents;
import net.minecraft.network.chat.contents.SelectorContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import com.mojang.serialization.Codec;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.IntSupplier;
import java.util.function.UnaryOperator;

public class ModularComponent extends MutableComponent implements Text {

    public static final MutableObjectCodec<ModularComponent> CODEC = MutableObjectCodec.drawableBuilder(ModularComponent.class, "Text")
            .wrapped(ComponentSerialization.CODEC.xmap(ModularComponent::of, mc -> mc))
            .addOpt("alignment", ModularComponent::alignment, ModularComponent::getAlignment, Alignment.CODEC, Alignment.Center)
            .addOpt("scale", ModularComponent::scale, ModularComponent::getScale, Codec.FLOAT, 1f)
            .addOpt("shadow", ModularComponent::shadow, ModularComponent::getShadow, Codec.BOOL, null)
            .addUnencodable("dynamicColor", ModularComponent::color, ModularComponent::getDynamicColor)
            .build();

    public static ModularComponent literal(String text) {
        return ModularComponent.create(PlainTextContents.create(text));
    }

    public static ModularComponent translatable(String key) {
        return ModularComponent.create(new TranslatableContents(key, null, TranslatableContents.NO_ARGS));
    }

    public static ModularComponent translatable(String key, Object... args) {
        return ModularComponent.create(new TranslatableContents(key, null, args));
    }

    public static ModularComponent translatableWithFallback(String key, @Nullable String fallback) {
        return ModularComponent.create(new TranslatableContents(key, fallback, TranslatableContents.NO_ARGS));
    }

    public static ModularComponent translatableWithFallback(String key, @Nullable String fallback, Object... args) {
        return ModularComponent.create(new TranslatableContents(key, fallback, args));
    }

    public static ModularComponent empty() {
        return ModularComponent.create(PlainTextContents.EMPTY);
    }

    public static ModularComponent keybind(String name) {
        return ModularComponent.create(new KeybindContents(name));
    }

    public static ModularComponent nbt(String nbtPathPattern, boolean interpreting, Optional<Component> separator, DataSource dataSource) {
        return ModularComponent.create(new NbtContents(nbtPathPattern, interpreting, separator, dataSource));
    }

    public static ModularComponent score(String name, String objective) {
        return ModularComponent.create(new ScoreContents(name, objective));
    }

    public static ModularComponent selector(String pattern, Optional<Component> separator) {
        return ModularComponent.create(new SelectorContents(pattern, separator));
    }

    public static ModularComponent create(@NotNull ComponentContents contents) {
        return new ModularComponent(contents, new ArrayList<>(), Style.EMPTY);
    }

    public static ModularComponent of(Component component) {
        if (component instanceof ModularComponent mc) return mc;
        return new ModularComponent(component.getContents(), new ArrayList<>(component.getSiblings()), component.getStyle());
    }

    @Getter private Alignment alignment = Alignment.CENTER;
    @Getter private float scale = 1f;
    @Getter private Boolean shadow;
    @Getter private IntSupplier dynamicColor;

    protected ModularComponent(ComponentContents contents, List<Component> siblings, Style style) {
        super(contents, siblings, style);
    }

    @Override
    public TextWidget<?> asWidget() {
        return new TextWidget<>(this);
    }

    @Override
    public ModularComponent style(@Nullable ChatFormatting formatting) {
        if (formatting == null) {
            setStyle(getStyle().withColor((TextColor) null));
        } else {
            withStyle(formatting);
        }
        return this;
    }

    @Override
    public ModularComponent removeStyle() {
        setStyle(Style.EMPTY);
        return this;
    }

    @Override
    public ModularComponent get() {
        return this;
    }

    @Override
    public void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {
        renderer.setAlignment(this.alignment, width, height);
        renderer.setColor(this.dynamicColor != null ? this.dynamicColor.getAsInt() : widgetTheme.getTextColor());
        renderer.setScale(this.scale);
        renderer.setPos(x, y);
        renderer.setShadow(this.shadow != null ? this.shadow : widgetTheme.isTextShadow());
        renderer.draw(context.getGraphics(), getFormatted());
    }

    @Override
    public @NotNull ModularComponent plainCopy() {
        return ModularComponent.create(getContents());
    }

    @Override
    public @NotNull ModularComponent copy() {
        return new ModularComponent(getContents(), new ArrayList<>(getSiblings()), getStyle())
                .alignment(this.alignment)
                .scale(this.scale)
                .color(this.dynamicColor);
    }

    @Override
    public @Nullable String tryCollapseToString() {
        return null;
    }

    @Override
    public ModularComponent withStyle() {
        return this;
    }

    @Override
    public ModularComponent alignment(Alignment alignment) {
        this.alignment = alignment;
        return this;
    }

    @Override
    public ModularComponent scale(float scale) {
        this.scale = scale;
        return this;
    }

    @Override
    public @NotNull ModularComponent color(int color) {
        withStyle(getStyle().withColor(color));
        return this;
    }

    public ModularComponent color(Integer color) {
        return color != null ? color((int) color) : color((IntSupplier) null);
    }

    @Override
    public ModularComponent color(@Nullable IntSupplier color) {
        this.dynamicColor = color;
        return this;
    }

    @Override
    public ModularComponent shadow(@Nullable Boolean shadow) {
        this.shadow = shadow;
        return this;
    }

    @Override
    public ModularComponent asModular() {
        return this;
    }

    @Override
    public @NotNull ModularComponent append(@NotNull String string) {
        return (ModularComponent) super.append(string);
    }

    @Override
    public @NotNull ModularComponent append(@NotNull Component sibling) {
        return (ModularComponent) super.append(sibling);
    }

    @Override
    public @NotNull ModularComponent withStyle(ChatFormatting @NotNull ... formats) {
        return (ModularComponent) super.withStyle(formats);
    }

    @Override
    public @NotNull ModularComponent withStyle(@NotNull Style style) {
        return (ModularComponent) super.withStyle(style);
    }

    @Override
    public @NotNull ModularComponent withStyle(@NotNull ChatFormatting format) {
        return (ModularComponent) super.withStyle(format);
    }

    @Override
    public @NotNull ModularComponent withStyle(@NotNull UnaryOperator<Style> modifyFunc) {
        return (ModularComponent) super.withStyle(modifyFunc);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ModularComponent other)) return false;
        return super.equals(other) && this.alignment.equals(other.alignment) && Float.compare(this.scale, other.scale) == 0 &&
                Objects.equals(this.shadow, other.shadow) && Objects.equals(this.dynamicColor, other.dynamicColor);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + Objects.hashCode(this.alignment);
        result = 31 * result + Float.hashCode(this.scale);
        result = 31 * result + Objects.hashCode(this.shadow);
        result = 31 * result + Objects.hashCode(getDynamicColor());
        return result;
    }
}

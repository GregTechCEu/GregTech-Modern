package com.gregtechceu.gtceu.api.mui.base.drawable;

import com.gregtechceu.gtceu.api.mui.base.IJsonSerializable;
import com.gregtechceu.gtceu.api.mui.drawable.text.*;
import com.gregtechceu.gtceu.api.mui.theme.WidgetTheme;
import com.gregtechceu.gtceu.api.mui.utils.Alignment;
import com.gregtechceu.gtceu.api.mui.widgets.TextWidget;
import com.gregtechceu.gtceu.client.mui.screen.viewport.GuiContext;
import com.gregtechceu.gtceu.utils.serialization.json.JsonHelper;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

/**
 * This represents a piece of text in a GUI.
 */
public interface IKey extends IDrawable, IJsonSerializable<IKey> {

    int TEXT_COLOR = 0xFF404040;

    TextRenderer renderer = new TextRenderer();

    IKey EMPTY = str("");
    IKey LINE_FEED = str("\n");
    IKey SPACE = str(" ");

    // Formatting for convenience
    ChatFormatting BLACK = ChatFormatting.BLACK;
    ChatFormatting DARK_BLUE = ChatFormatting.DARK_BLUE;
    ChatFormatting DARK_GREEN = ChatFormatting.DARK_GREEN;
    ChatFormatting DARK_AQUA = ChatFormatting.DARK_AQUA;
    ChatFormatting DARK_RED = ChatFormatting.DARK_RED;
    ChatFormatting DARK_PURPLE = ChatFormatting.DARK_PURPLE;
    ChatFormatting GOLD = ChatFormatting.GOLD;
    ChatFormatting GRAY = ChatFormatting.GRAY;
    ChatFormatting DARK_GRAY = ChatFormatting.DARK_GRAY;
    ChatFormatting BLUE = ChatFormatting.BLUE;
    ChatFormatting GREEN = ChatFormatting.GREEN;
    ChatFormatting AQUA = ChatFormatting.AQUA;
    ChatFormatting RED = ChatFormatting.RED;
    ChatFormatting LIGHT_PURPLE = ChatFormatting.LIGHT_PURPLE;
    ChatFormatting YELLOW = ChatFormatting.YELLOW;
    ChatFormatting WHITE = ChatFormatting.WHITE;
    ChatFormatting OBFUSCATED = ChatFormatting.OBFUSCATED;
    ChatFormatting BOLD = ChatFormatting.BOLD;
    ChatFormatting STRIKETHROUGH = ChatFormatting.STRIKETHROUGH;
    ChatFormatting UNDERLINE = ChatFormatting.UNDERLINE;
    ChatFormatting ITALIC = ChatFormatting.ITALIC;
    ChatFormatting RESET = ChatFormatting.RESET;

    /**
     * Creates a translated text.
     *
     * @param key translation key
     * @return text key
     */
    static IKey lang(@NotNull String key) {
        return new LangKey(key);
    }

    /**
     * Creates a translated text.
     *
     * @param component translation component
     * @return text key
     */
    static IKey lang(@NotNull Component component) {
        return new LangKey(component);
    }

    /**
     * Creates a translated text with arguments. The arguments can change.
     *
     * @param key  translation key
     * @param args translation arguments
     * @return text key
     */
    static IKey lang(@NotNull String key, @Nullable Object... args) {
        return new LangKey(key, args);
    }

    /**
     * Creates a translated text with arguments supplier.
     *
     * @param key          translation key
     * @param argsSupplier translation arguments supplier
     * @return text key
     */
    static IKey lang(@NotNull String key, @NotNull Supplier<Object[]> argsSupplier) {
        return new LangKey(key, argsSupplier);
    }

    /**
     * Creates a translated text.
     *
     * @param keySupplier translation key supplier
     * @return text key
     */
    static IKey lang(@NotNull Supplier<String> keySupplier) {
        return new LangKey(keySupplier);
    }

    /**
     * Creates a translated text with arguments supplier.
     *
     * @param keySupplier  translation key supplier
     * @param argsSupplier translation arguments supplier
     * @return text key
     */
    static IKey lang(@NotNull Supplier<String> keySupplier, @NotNull Supplier<Object[]> argsSupplier) {
        return new LangKey(keySupplier, argsSupplier);
    }

    /**
     * Creates a string literal text.
     *
     * @param key string
     * @return text key
     */
    static IKey str(@NotNull String key) {
        return new StringKey(key);
    }

    /**
     * Creates a formatted string literal text with arguments. The arguments can be dynamic.
     * The string is formatted using {@link String#format(String, Object...)}.
     *
     * @param key  string
     * @param args arguments
     * @return text key
     */
    static IKey str(@NotNull String key, @Nullable Object... args) {
        return new StringKey(key, args);
    }

    /**
     * Creates a composed text key.
     *
     * @param keys text keys
     * @return composed text key.
     */
    static IKey comp(@NotNull IKey... keys) {
        return new CompoundKey(keys);
    }

    /**
     * Creates a dynamic text key.
     *
     * @param getter string supplier
     * @return dynamic text key
     */
    static IKey dynamic(@NotNull Supplier<@NotNull Component> getter) {
        return new DynamicKey(getter);
    }

    /**
     * @return the current unformatted string
     */
    MutableComponent get();

    /**
     * @param parentFormatting formatting of the parent in case of composite keys
     * @return the current formatted string
     */
    default MutableComponent getFormatted(@Nullable FormattingState parentFormatting) {
        return get();
    }

    /**
     * @return the current formatted string
     */
    default MutableComponent getFormatted() {
        return getFormatted(null);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    default void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {
        renderer.setColor(widgetTheme.getTextColor());
        renderer.setShadow(widgetTheme.getTextShadow());
        renderer.setAlignment(Alignment.Center, width, height);
        renderer.setScale(1f);
        renderer.setPos(x, y);
        renderer.draw(context.getGraphics(), getFormatted());
    }

    @Override
    default TextWidget asWidget() {
        return new TextWidget(this);
    }

    default StyledText withStyle() {
        return new StyledText(this);
    }

    default AnimatedText withAnimation() {
        return new AnimatedText(this);
    }

    /**
     * @return a formatting state of this key
     */
    default @Nullable FormattingState getFormatting() {
        return null;
    }

    /**
     * Set text formatting to this key. If {@link IKey#RESET} is used, then that's applied first and then all other
     * formatting of this key.
     * With {@code null}, you can remove a color formatting. No matter the parents color, the default color will be
     * used.
     *
     * @param formatting a formatting rule
     * @return this
     */
    IKey style(@Nullable ChatFormatting formatting);

    default IKey style(ChatFormatting... formatting) {
        for (ChatFormatting cf : formatting) style(cf);
        return this;
    }

    default IKey removeFormatColor() {
        return style((ChatFormatting) null);
    }

    IKey removeStyle();

    default StyledText alignment(Alignment alignment) {
        return withStyle().alignment(alignment);
    }

    default StyledText color(@Nullable Integer color) {
        return withStyle().color(color);
    }

    default StyledText scale(float scale) {
        return withStyle().scale(scale);
    }

    default StyledText shadow(@Nullable Boolean shadow) {
        return withStyle().shadow(shadow);
    }

    default KeyIcon asTextIcon() {
        return new KeyIcon(this);
    }

    @Override
    default void loadFromJson(JsonObject json) {
        if (json.has("color") || json.has("shadow") || json.has("align") || json.has("alignment") ||
                json.has("scale")) {
            StyledText styledText = this instanceof StyledText styledText1 ? styledText1 : withStyle();
            if (json.has("color")) {
                styledText.color(JsonHelper.getInt(json, 0, "color"));
            }
            styledText.shadow(JsonHelper.getBoolean(json, false, "shadow"));
            styledText.alignment(
                    JsonHelper.deserialize(json, Alignment.class, styledText.getAlignment(), "align", "alignment"));
            styledText.scale(JsonHelper.getFloat(json, 1, "scale"));
        }
    }
}

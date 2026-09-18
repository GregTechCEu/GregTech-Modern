package brachy.modularui.api.drawable;

import brachy.modularui.ModularUI;
import brachy.modularui.drawable.text.DynamicComponent;
import brachy.modularui.drawable.text.KeyIcon;
import brachy.modularui.drawable.text.ModularComponent;
import brachy.modularui.drawable.text.TextRenderer;
import brachy.modularui.screen.viewport.GuiContext;
import brachy.modularui.theme.WidgetTheme;
import brachy.modularui.utils.Alignment;
import brachy.modularui.utils.serialization.codec.MutableObjectCodec;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.IntSupplier;
import java.util.function.Supplier;

/**
 * This represents a piece of text in a GUI.
 */
public interface Text extends IDrawable {

    MutableObjectCodec<ModularComponent> CODEC = ModularComponent.CODEC;

    int TEXT_COLOR = 0xFF404040;

    TextRenderer renderer = new TextRenderer();

    Component EMPTY = CommonComponents.EMPTY;
    Component LINE_FEED = CommonComponents.NEW_LINE;
    Component SPACE = CommonComponents.SPACE;

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

    static ModularComponent of(Component component) {
        return ModularComponent.of(component);
    }

    /**
     * Creates a translated text.
     *
     * @param key translation key
     * @return text key
     */
    static ModularComponent lang(@NotNull String key) {
        return ModularComponent.translatable(key);
    }

    /**
     * Creates a translated text with arguments. The arguments can change.
     *
     * @param key  translation key
     * @param args translation arguments
     * @return text key
     */
    static ModularComponent lang(@NotNull String key, @Nullable Object... args) {
        return ModularComponent.translatable(key, args);
    }

    /**
     * Creates a string literal text.
     *
     * @param key string
     * @return text key
     */
    static ModularComponent str(@NotNull String key) {
        return ModularComponent.literal(key);
    }

    /**
     * Creates a formatted string literal text with arguments. The arguments can be dynamic.
     * The string is formatted using {@link String#format(String, Object...)}.
     *
     * @param key  string
     * @param args arguments
     * @return text key
     */
    static ModularComponent str(@NotNull String key, @Nullable Object... args) {
        return ModularComponent.translatableWithFallback(key, key, args);
    }

    /**
     * Creates a composed text key.
     *
     * @param keys text keys
     * @return composed text key.
     */
    static ModularComponent comp(@NotNull Component... keys) {
        if (keys.length == 0) {
            return ModularComponent.empty();
        }
        ModularComponent main = keys[0].asModular();
        for (int i = 1; i < keys.length; i++) {
            main.append(keys[i]);
        }
        return main;
    }

    /**
     * Creates a dynamic text key.
     *
     * @param supplier string supplier
     * @return dynamic text key
     */
    static DynamicComponent dynamic(@NotNull Supplier<@NotNull Component> supplier) {
        return new DynamicComponent(supplier);
    }

    /**
     * @return the current unformatted string
     */
    ModularComponent get();

    /**
     * @return the current formatted string
     */
    default MutableComponent getFormatted() {
        return get();
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    default void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {
        drawAligned(context, x, y, width, height, widgetTheme, Alignment.CENTER);
    }

    @OnlyIn(Dist.CLIENT)
    default void drawAligned(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme, Alignment alignment) {
        renderer.setColor(widgetTheme.getTextColor());
        renderer.setShadow(widgetTheme.isTextShadow());
        renderer.setAlignment(alignment, width, height);
        renderer.setScale(getScale());
        renderer.setPos(x, y);
        renderer.draw(context.getGraphics(), getFormatted());
    }

    @Override
    default boolean canApplyTheme() {
        return true;
    }

    @Override
    default int getDefaultWidth() {
        if (!ModularUI.isClientSide()) return 18;
        renderer.setAlignment(Alignment.TopLeft, -1, -1);
        renderer.setScale(getScale());
        renderer.setPos(0, 0);
        renderer.setSimulate(true);
        renderer.draw(null, getFormatted());
        renderer.setSimulate(false);
        return (int) renderer.getLastWidth();
    }

    @Override
    default int getDefaultHeight() {
        if (!ModularUI.isClientSide()) return 18;
        renderer.setAlignment(Alignment.TopLeft, -1, -1);
        renderer.setScale(getScale());
        renderer.setPos(0, 0);
        renderer.setSimulate(true);
        renderer.draw(null, getFormatted());
        renderer.setSimulate(false);
        return (int) renderer.getLastWidth();
    }

    default float getScale() {
        return 1f;
    }

    ModularComponent withStyle();

    /*default AnimatedText withAnimation() {
        return new AnimatedText(this);
    }*/

    /**
     * Set text formatting to this key. If {@link Text#RESET} is used, then that's applied first and then all other
     * formatting of this key.
     * With {@code null}, you can remove a color formatting. No matter the parents color, the default color will be
     * used.
     *
     * @param formatting a formatting rule
     * @return this
     */
    ModularComponent style(@Nullable ChatFormatting formatting);

    default ModularComponent style(ChatFormatting... formatting) {
        for (ChatFormatting cf : formatting) style(cf);
        return get();
    }

    default ModularComponent removeFormatColor() {
        return style((ChatFormatting) null);
    }

    Text removeStyle();

    ModularComponent alignment(Alignment alignment);

    ModularComponent color(int color);

    ModularComponent color(@Nullable IntSupplier color);

    ModularComponent scale(float scale);

    ModularComponent shadow(@Nullable Boolean shadow);

    default KeyIcon asTextIcon() {
        return new KeyIcon(this);
    }

    @Override
    default String getTypeName() {
        return "text";
    }
}

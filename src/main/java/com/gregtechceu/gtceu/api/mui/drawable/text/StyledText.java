package com.gregtechceu.gtceu.api.mui.drawable.text;

import com.gregtechceu.gtceu.api.mui.base.drawable.IKey;
import com.gregtechceu.gtceu.api.mui.theme.WidgetTheme;
import com.gregtechceu.gtceu.api.mui.utils.Alignment;
import com.gregtechceu.gtceu.api.mui.widgets.TextWidget;
import com.gregtechceu.gtceu.client.mui.screen.viewport.GuiContext;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.IntSupplier;

@Accessors(fluent = true, chain = true)
public class StyledText extends BaseKey {

    private final IKey key;
    @Getter
    @Setter
    private Alignment alignment = Alignment.Center;
    @Getter
    @Setter
    private IntSupplier color = null;
    @Setter
    private @Nullable Boolean shadow = null;
    @Getter
    @Setter
    private float scale = 1f;

    public StyledText(IKey key) {
        this.key = key;
    }

    @Override
    public MutableComponent get() {
        return this.key.get();
    }

    @Override
    public MutableComponent getFormatted() {
        return this.key.getFormatted();
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {
        renderer.setAlignment(this.alignment, width, height);
        renderer.setColor(this.color != null ? this.color.getAsInt() : widgetTheme.getTextColor());
        renderer.setScale(this.scale);
        renderer.setPos(x, y);
        renderer.setShadow(this.shadow != null ? this.shadow : widgetTheme.isTextShadow());
        renderer.draw(context.getGraphics(), getFormatted());
    }

    public @Nullable Boolean isShadow() {
        return this.shadow;
    }

    @Override
    public @NotNull BaseKey style(ChatFormatting formatting) {
        this.key.style(formatting);
        return this;
    }

    @Override
    public TextWidget<?> asWidget() {
        return new TextWidget<>(this.key)
                .alignment(this.alignment)
                .color(this.color)
                .scale(this.scale)
                .shadow(this.shadow);
    }

    @Override
    public AnimatedText withAnimation() {
        return new AnimatedText(this.key)
                .alignment(this.alignment)
                .color(this.color)
                .scale(this.scale)
                .shadow(this.shadow);
    }
}

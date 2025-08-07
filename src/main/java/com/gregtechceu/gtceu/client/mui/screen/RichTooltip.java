package com.gregtechceu.gtceu.client.mui.screen;

import com.gregtechceu.gtceu.api.mui.base.GuiAxis;
import com.gregtechceu.gtceu.api.mui.base.MCHelper;
import com.gregtechceu.gtceu.api.mui.base.drawable.IRichTextBuilder;
import com.gregtechceu.gtceu.api.mui.base.widget.IWidget;
import com.gregtechceu.gtceu.api.mui.drawable.GuiDraw;
import com.gregtechceu.gtceu.api.mui.drawable.text.RichText;
import com.gregtechceu.gtceu.api.mui.drawable.text.TextRenderer;
import com.gregtechceu.gtceu.api.mui.utils.Color;
import com.gregtechceu.gtceu.api.mui.utils.Rectangle;
import com.gregtechceu.gtceu.api.mui.widget.sizer.Area;
import com.gregtechceu.gtceu.client.mui.screen.viewport.GuiContext;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.common.MinecraftForge;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Either;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Accessors(chain = true)
public class RichTooltip implements IRichTextBuilder<RichTooltip> {

    private static final Area HOLDER = new Area();

    private final Consumer<Area> parent;
    private final RichText text = new RichText();
    private Pos pos = null;
    private Consumer<RichTooltip> tooltipBuilder;
    @Getter
    @Setter
    private int showUpTimer = 0;
    @Getter
    @Setter
    private boolean autoUpdate = false;
    private int titleMargin = 0;
    private boolean appliedMargin = true;

    private int x = 0, y = 0;
    private int maxWidth = Integer.MAX_VALUE;

    private boolean dirty;

    public RichTooltip(IWidget parent) {
        this(area -> {
            area.setSize(parent.getArea());
            area.setPos(0, 0);
        });
    }

    public RichTooltip(Area parent) {
        this(area -> area.set(parent));
    }

    public RichTooltip(Supplier<Area> parent) {
        this(area -> area.set(parent.get()));
    }

    public RichTooltip(Consumer<Area> parent) {
        this.parent = parent;
    }

    public void buildTooltip() {
        this.dirty = false;
        if (this.tooltipBuilder != null) {
            this.text.clearText();
            this.tooltipBuilder.accept(this);
            this.appliedMargin = false;
        }
    }

    public void draw(GuiContext context) {
        draw(context, ItemStack.EMPTY);
    }

    public void draw(GuiContext context, @Nullable ItemStack stack) {
        if (this.autoUpdate) markDirty();
        if (isEmpty()) return;

        if (this.maxWidth <= 0) {
            this.maxWidth = Integer.MAX_VALUE;
        }
        if (stack == null) stack = ItemStack.EMPTY;
        if (!this.appliedMargin) {
            if (this.titleMargin > 0) {
                this.text.insertTitleMargin(this.titleMargin);
            }
            this.appliedMargin = true;
        }
        Area screen = context.getScreenArea();
        this.maxWidth = Math.min(this.maxWidth, screen.width);
        int mouseX = context.getMouseX(), mouseY = context.getMouseY();
        TextRenderer renderer = TextRenderer.SHARED;
        // this only turns the text and not any drawables into strings
        List<Either<FormattedText, TooltipComponent>> textLines = this.text.getStringRepresentation().stream()
                .<Either<FormattedText, TooltipComponent>>map(Either::left)
                .collect(Collectors.toList());

        var gatherEvent = new RenderTooltipEvent.GatherComponents(stack, screen.width, screen.height, textLines,
                this.maxWidth);
        if (MinecraftForge.EVENT_BUS.post(gatherEvent)) return; // canceled
        this.maxWidth = gatherEvent.getMaxWidth();
        textLines = gatherEvent.getTooltipElements();
        List<ClientTooltipComponent> components = textLines.stream()
                .map(either -> either.map(
                        text -> ClientTooltipComponent.create(text instanceof Component ?
                                ((Component) text).getVisualOrderText() : Language.getInstance().getVisualOrder(text)),
                        ClientTooltipComponent::create))
                .toList();

        RenderTooltipEvent.Pre event = new RenderTooltipEvent.Pre(stack, context.getGraphics(),
                mouseX, mouseY, screen.width, screen.height,
                TextRenderer.getFont(), components, DefaultTooltipPositioner.INSTANCE);
        if (MinecraftForge.EVENT_BUS.post(event)) return; // canceled
        // we are supposed to now use the strings of the event, but we can't properly determine where to put them
        mouseX = event.getX();
        mouseY = event.getY();
        int screenWidth = event.getScreenWidth(), screenHeight = event.getScreenHeight();

        // simulate to figure how big this tooltip is without any restrictions
        this.text.setupRenderer(renderer, 0, 0, this.maxWidth, -1, Color.WHITE.main, false);
        this.text.compileAndDraw(renderer, context, true);

        Rectangle area = determineTooltipArea(context, renderer, screenWidth, screenHeight, mouseX, mouseY);

        Lighting.setupForFlatItems();
        RenderSystem.disableDepthTest();
        RenderSystem.disableBlend();

        GuiDraw.drawTooltipBackground(context.getGraphics(), stack, components, area.x, area.y, area.width,
                area.height);

        // MinecraftForge.EVENT_BUS.post(new RenderTooltipEvent.PostBackground(stack, textLines, area.x, area.y,
        // TextRenderer.getFont(), area.width, area.height));

        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        renderer.setPos(area.x, area.y);
        this.text.compileAndDraw(renderer, context, false);

        // MinecraftForge.EVENT_BUS.post(new RenderTooltipEvent.PostText(stack, textLines, area.x, area.y,
        // TextRenderer.getFont(), area.width, area.height));
    }

    public Rectangle determineTooltipArea(GuiContext context, TextRenderer renderer, int screenWidth, int screenHeight,
                                          int mouseX, int mouseY) {
        int width = (int) renderer.getLastWidth();
        int height = (int) renderer.getLastHeight();

        Pos pos = this.pos;
        if (pos == null) {
            pos = context.isMuiContext() ?
                    context.getMuiContext().getScreen().getCurrentTheme().getTooltipPosOverride() : null;
            if (pos == null) pos = ConfigHolder.INSTANCE.client.ui.tooltipPos;
        }
        if (pos == Pos.FIXED) {
            return new Rectangle(this.x, this.y, width, height);
        }

        if (pos == Pos.NEXT_TO_MOUSE) {
            // vanilla style, tooltip floats next to mouse
            // note that this behaves slightly different from vanilla (better imo)
            final int padding = 8;
            // magic number to place tooltip nicer. Look at Screen#L237
            final int mouseOffset = 12;
            int x = mouseX + mouseOffset, y = mouseY - mouseOffset;
            if (x < padding) {
                x = padding; // this cant happen mathematically since mouse is always positive
            } else if (x + width + padding > screenWidth) {
                // doesn't fit on the right side of the screen
                if (screenWidth - mouseX < mouseX) { // check if left side has more space
                    x -= mouseOffset * 2 + width; // flip side of cursor if other side has more space
                    if (x < padding) {
                        x = padding; // went off-screen
                    }
                    width = mouseX - mouseOffset - x; // max space on left side
                } else {
                    width = screenWidth - padding - x; // max space on right side
                }
                // recalculate width and height
                renderer.setPos(x, y);
                renderer.setAlignment(this.text.getAlignment(), width, -1);
                this.text.compileAndDraw(renderer, context, true);
                width = (int) renderer.getLastWidth();
                height = (int) renderer.getLastHeight();
            }
            y = Mth.clamp(y, padding, screenHeight - padding - height);
            return new Rectangle(x, y, width, height);
        }

        // the rest of the cases will put the tooltip next a given area
        if (this.parent == null) {
            throw new IllegalStateException("Tooltip pos is " + pos.name() + ", but no widget parent is set!");
        }

        int minWidth = this.text.getMinWidth();

        int shiftAmount = 10;
        int padding = 7;

        Area area = HOLDER;
        this.parent.accept(area);
        area.transformAndRectanglerize(context);
        int x = 0, y = 0;
        if (pos.axis.isVertical()) { // above or below
            if (width < area.width) {
                x = area.x + shiftAmount;
            } else {
                x = area.x - shiftAmount;
                if (x < padding) {
                    x = padding;
                } else if (x + width > screenWidth - padding) {
                    int maxWidth = Math.max(minWidth, screenWidth - x - padding);
                    renderer.setAlignment(this.text.getAlignment(), maxWidth);
                    this.text.compileAndDraw(renderer, context, true);
                    width = (int) renderer.getLastWidth();
                    height = (int) renderer.getLastHeight();
                }
            }

            if (pos == Pos.VERTICAL) {
                int bottomSpace = screenHeight - area.ey();
                pos = bottomSpace < height + padding && bottomSpace < area.y ? Pos.ABOVE : Pos.BELOW;
            }

            if (pos == Pos.BELOW) {
                y = area.ey() + padding;
            } else if (pos == Pos.ABOVE) {
                y = area.y - height - padding;
            }
        } else if (pos.axis.isHorizontal()) {
            boolean usedMoreSpaceSide = false;
            Pos oPos = pos;
            if (oPos == Pos.HORIZONTAL) {
                if (area.x > screenWidth - area.ex()) {
                    pos = Pos.LEFT;
                    // x = 0;
                } else {
                    pos = Pos.RIGHT;
                    x = screenWidth - area.ex() + padding;
                }
            }

            if (height < area.height) {
                y = area.y + shiftAmount;
            } else {
                y = area.y - shiftAmount;
                if (y < padding) {
                    y = padding;
                }
            }

            if (x + width > screenWidth - padding) {
                int maxWidth;
                if (pos == Pos.LEFT) {
                    maxWidth = Math.max(minWidth, area.x - padding * 2);
                } else {
                    maxWidth = Math.max(minWidth, screenWidth - area.ex() - padding * 2);
                }
                usedMoreSpaceSide = true;
                renderer.setAlignment(this.text.getAlignment(), maxWidth);
                this.text.compileAndDraw(renderer, context, true);
                width = (int) renderer.getLastWidth();
                height = (int) renderer.getLastHeight();
            }

            if (oPos == Pos.HORIZONTAL && !usedMoreSpaceSide) {
                int rightSpace = screenWidth - area.ex();
                pos = rightSpace < width + padding && rightSpace < area.x ? Pos.LEFT : Pos.RIGHT;
            }

            if (pos == Pos.RIGHT) {
                x = area.ex() + padding;
            } else if (pos == Pos.LEFT) {
                x = area.x - width - padding;
            }
        }
        return new Rectangle(x, y, width, height);
    }

    public boolean isEmpty() {
        if (this.dirty) buildTooltip();
        return this.text.isEmpty();
    }

    public void markDirty() {
        this.dirty = true;
    }

    public RichTooltip pos(Pos pos) {
        this.pos = pos;
        return this;
    }

    public RichTooltip pos(int x, int y) {
        this.pos = Pos.FIXED;
        this.x = x;
        this.y = y;
        return this;
    }

    @Override
    public RichTooltip getThis() {
        return this;
    }

    @Override
    public IRichTextBuilder<?> getRichText() {
        return text;
    }

    public RichTooltip showUpTimer(int showUpTimer) {
        return this.setShowUpTimer(showUpTimer);
    }

    public RichTooltip tooltipBuilder(Consumer<RichTooltip> tooltipBuilder) {
        Consumer<RichTooltip> existingBuilder = this.tooltipBuilder;
        if (existingBuilder != null) {
            this.tooltipBuilder = tooltip -> {
                existingBuilder.accept(this);
                tooltipBuilder.accept(this);
            };
        } else {
            this.tooltipBuilder = tooltipBuilder;
        }
        markDirty();
        return this;
    }

    public RichTooltip addFromItem(ItemStack item) {
        List<Component> lines = MCHelper.getItemToolTip(item);
        add(lines.get(0)).spaceLine(2);
        for (int i = 1, n = lines.size(); i < n; i++) {
            add(lines.get(i)).newLine();
        }
        return this;
    }

    public RichTooltip titleMargin() {
        return titleMargin(0);
    }

    public RichTooltip titleMargin(int margin) {
        this.titleMargin = margin;
        this.appliedMargin = false;
        return this;
    }

    public enum Pos {

        ABOVE(GuiAxis.Y),
        BELOW(GuiAxis.Y),
        LEFT(GuiAxis.X),
        RIGHT(GuiAxis.X),
        VERTICAL(GuiAxis.Y),
        HORIZONTAL(GuiAxis.X),
        NEXT_TO_MOUSE(null),
        FIXED(null);

        public final GuiAxis axis;

        Pos(GuiAxis axis) {
            this.axis = axis;
        }
    }
}

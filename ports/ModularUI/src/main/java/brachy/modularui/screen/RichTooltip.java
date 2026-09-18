package brachy.modularui.screen;

import brachy.modularui.ModularUI;
import brachy.modularui.ModularUIConfig;
import brachy.modularui.api.GuiAxis;
import brachy.modularui.api.MCHelper;
import brachy.modularui.api.drawable.IRichTextBuilder;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.drawable.GuiDraw;
import brachy.modularui.drawable.text.RichText;
import brachy.modularui.drawable.text.TextRenderer;
import brachy.modularui.screen.event.RichTooltipEvent;
import brachy.modularui.screen.viewport.GuiContext;
import brachy.modularui.utils.Color;
import brachy.modularui.utils.Rectangle;
import brachy.modularui.utils.TooltipLines;
import brachy.modularui.utils.serialization.codec.CodecUtil;
import brachy.modularui.utils.serialization.codec.MutableObjectCodec;
import brachy.modularui.widget.sizer.Area;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.serialization.Codec;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;
import net.neoforged.neoforge.common.NeoForge;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.Tolerate;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Accessors(fluent = true, chain = true)
public class RichTooltip implements IRichTextBuilder<RichTooltip> {

    private static final Area HOLDER = new Area();

    public static final MutableObjectCodec<RichTooltip> CODEC = MutableObjectCodec.builder(RichTooltip::new)
            .addOpt("posType", RichTooltip::pos, RichTooltip::pos, CodecUtil.wrapNullsafe(Pos.CODEC), null)
            .addOpt("showUpTimer", RichTooltip::showUpTimer, RichTooltip::showUpTimer, Codec.INT, 0)
            .addOpt("titleMargin", RichTooltip::titleMargin, RichTooltip::titleMargin, Codec.INT, 0)
            .addFieldsOf(RichText.CODEC, tooltip -> tooltip.text)
            .build();

    private final RichText text = new RichText();
    @Setter
    private Consumer<Area> parent;
    @Setter
    @Getter
    private Pos pos = null;
    private Consumer<RichTooltip> tooltipBuilder;
    @Getter
    @Setter
    private int showUpTimer = 0;
    @Getter
    @Setter
    private boolean autoUpdate = false;
    @Getter private int titleMargin = 0;
    private boolean appliedMargin = true;

    private int x = 0, y = 0;
    @Getter private int maxWidth = Integer.MAX_VALUE;

    private boolean dirty;

    public RichTooltip() {
        parent(Area.ZERO);
    }

    @Override
    public RichTooltip reset() {
        clearText();
        this.pos = null;
        this.tooltipBuilder = null;
        this.showUpTimer = 0;
        this.autoUpdate = false;
        this.titleMargin = 0;
        this.appliedMargin = true;
        this.x = 0;
        this.y = 0;
        this.maxWidth = Integer.MAX_VALUE;
        return this;
    }

    @Tolerate
    public RichTooltip parent(Area parent) {
        return parent(area -> area.set(parent));
    }

    @Tolerate
    public RichTooltip parent(Supplier<Area> parent) {
        return parent(area -> area.set(parent.get()));
    }

    @Tolerate
    public RichTooltip parent(IWidget parent) {
        return parent(area -> {
            area.setPos(0, 0);
            area.setSize(parent.getArea());
        });
    }

    public void buildTooltip() {
        this.dirty = false;
        if (this.tooltipBuilder != null) {
            this.text.clearText();
            this.tooltipBuilder.accept(this);
            this.appliedMargin = false;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void draw(GuiContext context) {
        draw(context, ItemStack.EMPTY);
    }

    @OnlyIn(Dist.CLIENT)
    public void draw(GuiContext context, ItemStack stack) {
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
        // Correct the mouse pos with the calculated screen offset.
        // See GuiContext#updateState(int,int,float).
        int mouseX = context.getAbsMouseX() + screen.x, mouseY = context.getAbsMouseY() + screen.y;
        TextRenderer renderer = TextRenderer.SHARED;

        RichText copy = this.text.copy();
        // rich event to gather additional tooltip
        // this does not trigger vanilla event listeners
        var richGatherEventPre = new RichTooltipEvent.Gather.Pre(copy, stack, context, mouseX, mouseY, screen.width, screen.height, this.maxWidth);
        if (NeoForge.EVENT_BUS.post(richGatherEventPre).isCanceled()) return;
        this.maxWidth = richGatherEventPre.getMaxWidth();

        // vanilla event to gather additional tooltip
        TooltipLines textLines = copy.getAsText();
        // noinspection UnstableApiUsage
        var vanillaGatherEvent = new RenderTooltipEvent.GatherComponents(stack, screen.width, screen.height, textLines, this.maxWidth);
        if (NeoForge.EVENT_BUS.post(vanillaGatherEvent).isCanceled()) return;
        this.maxWidth = vanillaGatherEvent.getMaxWidth();

        // rich event to gather additional tooltip
        // this does not trigger vanilla event listeners
        var richGatherEventPost = new RichTooltipEvent.Gather.Post(copy, stack, context, mouseX, mouseY, screen.width, screen.height, this.maxWidth);
        if (NeoForge.EVENT_BUS.post(richGatherEventPost).isCanceled()) return;
        this.maxWidth = richGatherEventPost.getMaxWidth();

        // triggers vanilla event listeners
        List<ClientTooltipComponent> components = textLines.toClientTooltipComponents();
        RichTooltipEvent.Pre event = new RichTooltipEvent.Pre(stack, context.getGraphics(),
                mouseX, mouseY, screen.width, screen.height,
                context.getFont(), components, DefaultTooltipPositioner.INSTANCE, copy);
        if (NeoForge.EVENT_BUS.post(event).isCanceled()) return;

        // we are supposed to now use the strings of the event, but we can't properly determine where to put them
        mouseX = event.getX();
        mouseY = event.getY();
        int screenWidth = event.getScreenWidth(), screenHeight = event.getScreenHeight();

        context.setOverrideFont(event.getFont());

        // simulate to figure how big this tooltip is without any restrictions
        copy.setupRenderer(renderer, 0, 0, this.maxWidth, -1, Color.WHITE.main, false);
        copy.compileAndDraw(renderer, context, true);

        Rectangle area = determineTooltipArea(copy, context, renderer, screenWidth, screenHeight, mouseX, mouseY);

        Lighting.setupForFlatItems();
        RenderSystem.disableDepthTest();
        RenderSystem.disableBlend();

        context.getGraphics().pose().pushPose();
        // Since we applied an offset to the mouse pos earlier, we need to correct it back, but only visually.
        context.getGraphics().pose().translate(-screen.x, -screen.y, 400);
        GuiDraw.drawTooltipBackground(context, stack, components, area.x, area.y, area.width, area.height, copy);

        // NeoForge.EVENT_BUS.post(new RenderTooltipEvent.PostBackground(stack, textLines, area.x, area.y,
        // TextRenderer.getFont(), area.width, area.height));

        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        renderer.setPos(area.x, area.y);
        copy.compileAndDraw(renderer, context, false);
        context.getGraphics().pose().popPose();

        context.setOverrideFont(null);
    }

    public Rectangle determineTooltipArea(RichText text, GuiContext context, TextRenderer renderer,
                                          int screenWidth, int screenHeight, int mouseX, int mouseY) {
        int width = (int) renderer.getLastWidth();
        int height = (int) renderer.getLastHeight();
        if (width > screenWidth - 14) {
            width = screenWidth - 14;
        }

        Pos pos = this.pos;
        if (pos == null) {
            pos = ModularUIConfig.tooltipPos();
        }
        if (pos == Pos.FIXED) {
            return new Rectangle(this.x, this.y, width, height);
        }

        Area area = HOLDER;
        this.parent.accept(area);
        if (area.x == 0 && area.y == 0 && area.width == 0 && area.height == 0) {
            pos = Pos.NEXT_TO_MOUSE;
        }

        if (pos == Pos.NEXT_TO_MOUSE) {
            // vanilla style, tooltip floats next to mouse
            // note that this behaves slightly different from vanilla (better imo)
            final int padding = 8;
            // magic number to place tooltip nicer. Look at Screen#L237
            final int mouseOffset = 12;
            int x = mouseX + mouseOffset, y = mouseY - mouseOffset;
            if (x < padding) {
                x = padding; // this can't happen mathematically since mouse is always positive
            } else if (x + width + padding > screenWidth) {
                // doesn't fit on the right side of the screen
                if (screenWidth - mouseX < mouseX) { // check if left side has more space
                    x -= mouseOffset * 2 + width; // flip side of cursor if other side has more space
                    if (x < padding) {
                        x = padding; // went off-screen
                    }
                    width = mouseX - mouseOffset - padding; // max space on left side
                } else {
                    width = screenWidth - padding - x; // max space on right side
                }
                // recalculate width and height
                renderer.setPos(x, y);
                renderer.setAlignment(text.getAlignment(), width, -1);
                text.compileAndDraw(renderer, context, true);
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

        int minWidth = text.getMinWidth();

        int shiftAmount = 10;
        int padding = 7;

        area.transformAndRectanglerize(context);
        int x = 0, y = 0;
        if (pos.axis.isVertical()) { // above or below
            x = area.x + (width < area.width ? shiftAmount : -shiftAmount);
            x = Mth.clamp(x, padding, screenWidth - padding - width);

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
                if (x < padding) {
                    x = padding;
                    width = area.x - x - padding;
                }
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

    public RichTooltip tooltipBuilder(Consumer<RichTooltip> tooltipBuilder) {
        Consumer<RichTooltip> existingBuilder = this.tooltipBuilder;
        if (existingBuilder != null && tooltipBuilder != null) {
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
        if (!ModularUI.isClientSide()) return this;
        List<Component> lines = MCHelper.getItemToolTip(item);
        add(lines.getFirst());
        if (lines.size() > 1) {
            spaceLine();
            for (int i = 1, n = lines.size(); i < n; i++) {
                add(lines.get(i)).newLine();
            }
        }
        return this;
    }

    public RichTooltip titleMargin(int margin) {
        this.titleMargin = margin;
        this.appliedMargin = false;
        return this;
    }

    public static void findIngredientArea(Area area, int x, int y) {
        Screen screen = MCHelper.getCurrentScreen();
        if (screen instanceof AbstractContainerScreen<?> containerScreen) {
            Slot slot = containerScreen.getSlotUnderMouse();
            if (slot != null) {
                int sx = slot.x + containerScreen.getGuiLeft();
                int sy = slot.y + containerScreen.getGuiTop();
                if (sx >= 0 && sy >= 0) {
                    area.set(sx - 1, sy - 1, 18, 18);
                    return;
                }
            }
        }
        /*
         * TODO fix this JEI compat thing
         * if (ModularUI.Mods.isJEILoaded()) {
         * IShowsRecipeFocuses overlay = (IShowsRecipeFocuses)
         * ModularUIJeiPlugin.getRuntime().getIngredientListOverlay();
         * IClickedIngredient<?> ingredient = overlay.getIngredientUnderMouse(x, y);
         * if (ingredient == null || ingredient.getArea() == null) {
         * overlay = (IShowsRecipeFocuses) ModularUIJeiPlugin.getRuntime().getBookmarkOverlay();
         * ingredient = overlay.getIngredientUnderMouse(x, y);
         * }
         * if (ingredient != null && ingredient.getArea() != null) {
         * Rectangle slot = ingredient.getArea();
         * area.set(slot.x - 1, slot.y - 1, 18, 18);
         * return;
         * }
         * }
         */
        area.set(Area.ZERO);
    }

    public RichTooltip copy() {
        RichTooltip tooltip = new RichTooltip();
        tooltip.text.copyPropertiesOf(this.text);
        tooltip.parent = this.parent;
        tooltip.pos = this.pos;
        tooltip.tooltipBuilder = this.tooltipBuilder;
        tooltip.showUpTimer = this.showUpTimer;
        tooltip.autoUpdate = this.autoUpdate;
        tooltip.titleMargin = this.titleMargin;
        tooltip.appliedMargin = this.appliedMargin;
        tooltip.x = this.x;
        tooltip.y = this.y;
        tooltip.maxWidth = this.maxWidth;
        tooltip.dirty = this.dirty;
        return tooltip;
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof RichTooltip that)) return false;

        return showUpTimer == that.showUpTimer && autoUpdate == that.autoUpdate &&
                titleMargin == that.titleMargin && appliedMargin == that.appliedMargin
                && x == that.x && y == that.y && maxWidth == that.maxWidth &&
                text.equals(that.text) && Objects.equals(parent, that.parent) && pos == that.pos &&
                Objects.equals(tooltipBuilder, that.tooltipBuilder);
    }

    @Override
    public int hashCode() {
        int result = text.hashCode();
        result = 31 * result + Objects.hashCode(parent);
        result = 31 * result + pos.hashCode();
        result = 31 * result + Objects.hashCode(tooltipBuilder);
        result = 31 * result + showUpTimer;
        result = 31 * result + Boolean.hashCode(autoUpdate);
        result = 31 * result + titleMargin;
        result = 31 * result + Boolean.hashCode(appliedMargin);
        result = 31 * result + x;
        result = 31 * result + y;
        result = 31 * result + maxWidth;
        return result;
    }

    public enum Pos implements StringRepresentable {

        ABOVE(GuiAxis.Y),
        BELOW(GuiAxis.Y),
        LEFT(GuiAxis.X),
        RIGHT(GuiAxis.X),
        VERTICAL(GuiAxis.Y),
        HORIZONTAL(GuiAxis.X),
        NEXT_TO_MOUSE(null),
        FIXED(null);

        public static final Codec<Pos> CODEC = StringRepresentable.fromEnum(Pos::values);

        public final GuiAxis axis;
        public final String name;

        Pos(GuiAxis axis) {
            this.axis = axis;
            this.name = name().toLowerCase(Locale.ENGLISH);
        }

        @Override
        public @NotNull String getSerializedName() {
            return this.name;
        }
    }
}

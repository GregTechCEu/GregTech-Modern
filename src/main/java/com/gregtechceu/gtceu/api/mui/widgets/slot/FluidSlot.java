package com.gregtechceu.gtceu.api.mui.widgets.slot;

import com.gregtechceu.gtceu.api.mui.base.ITheme;
import com.gregtechceu.gtceu.api.mui.base.drawable.IDrawable;
import com.gregtechceu.gtceu.api.mui.base.drawable.IKey;
import com.gregtechceu.gtceu.api.mui.base.widget.Interactable;
import com.gregtechceu.gtceu.api.mui.drawable.GuiDraw;
import com.gregtechceu.gtceu.api.mui.drawable.text.TextRenderer;
import com.gregtechceu.gtceu.api.mui.theme.WidgetSlotTheme;
import com.gregtechceu.gtceu.api.mui.theme.WidgetTheme;
import com.gregtechceu.gtceu.api.mui.utils.Alignment;
import com.gregtechceu.gtceu.api.mui.utils.Color;
import com.gregtechceu.gtceu.api.mui.utils.MouseData;
import com.gregtechceu.gtceu.api.mui.value.sync.FluidSlotSyncHandler;
import com.gregtechceu.gtceu.api.mui.value.sync.SyncHandler;
import com.gregtechceu.gtceu.api.mui.widget.Widget;
import com.gregtechceu.gtceu.client.mui.screen.RichTooltip;
import com.gregtechceu.gtceu.client.mui.screen.viewport.ModularGuiContext;
import com.gregtechceu.gtceu.integration.xei.entry.EntryList;
import com.gregtechceu.gtceu.integration.xei.entry.fluid.FluidStackList;
import com.gregtechceu.gtceu.integration.xei.handlers.GhostIngredientSlot;
import com.gregtechceu.gtceu.integration.xei.handlers.IngredientProvider;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;

public class FluidSlot extends Widget<FluidSlot>
                       implements Interactable, GhostIngredientSlot<FluidStack>, IngredientProvider<FluidStack> {

    public static final int DEFAULT_SIZE = 18;
    public static final String UNIT_BUCKET = "B";
    public static final String UNIT_LITER = "L";
    private static final DecimalFormat TOOLTIP_FORMAT = new DecimalFormat("#.##");
    private static final IFluidTank EMPTY = new FluidTank(0);

    static {
        TOOLTIP_FORMAT.setGroupingUsed(true);
        TOOLTIP_FORMAT.setGroupingSize(3);
    }

    private final TextRenderer textRenderer = new TextRenderer();
    private FluidSlotSyncHandler syncHandler;
    private int contentOffsetX = 1, contentOffsetY = 1;
    private boolean alwaysShowFull = true;
    @Nullable
    private IDrawable overlayTexture = null;

    public FluidSlot() {
        size(DEFAULT_SIZE);
        tooltip().autoUpdate(true);// .setHasTitleMargin(true);
        tooltipBuilder(tooltip -> {
            IFluidTank fluidTank = getFluidTank();
            FluidStack fluid = this.syncHandler.getValue();
            if (fluid != null) {
                tooltip.addLine(IKey.lang(fluid.getDisplayName())).spaceLine(2);
            }
            if (this.syncHandler.phantom()) {
                if (fluid != null) {
                    if (this.syncHandler.controlsAmount()) {
                        tooltip.addLine(IKey.lang("modularui.fluid.phantom.amount",
                                formatFluidTooltipAmount(fluid.getAmount()), getBaseUnit()));
                    }
                } else {
                    tooltip.addLine(IKey.lang("modularui.fluid.empty"));
                }
                if (this.syncHandler.controlsAmount()) {
                    tooltip.addLine(IKey.lang("modularui.fluid.phantom.control"));
                }
            } else {
                if (fluid != null) {
                    tooltip.addLine(IKey.lang("modularui.fluid.amount", formatFluidTooltipAmount(fluid.getAmount()),
                            formatFluidTooltipAmount(fluidTank.getCapacity()), getBaseUnit()));
                    addAdditionalFluidInfo(tooltip, fluid);
                } else {
                    tooltip.addLine(IKey.lang("modularui.fluid.empty"));
                }
                if (this.syncHandler.canFillSlot() || this.syncHandler.canDrainSlot()) {
                    tooltip.addLine(IKey.EMPTY); // Add an empty line to separate from the bottom material tooltips
                    if (Interactable.hasShiftDown()) {
                        if (this.syncHandler.canFillSlot() && this.syncHandler.canDrainSlot()) {
                            tooltip.addLine(IKey.lang("modularui.fluid.click_combined"));
                        } else if (this.syncHandler.canDrainSlot()) {
                            tooltip.addLine(IKey.lang("modularui.fluid.click_to_fill"));
                        } else if (this.syncHandler.canFillSlot()) {
                            tooltip.addLine(IKey.lang("modularui.fluid.click_to_empty"));
                        }
                    } else {
                        tooltip.addLine(IKey.lang("modularui.tooltip.shift"));
                    }
                }
            }
        });
    }

    public void addAdditionalFluidInfo(RichTooltip tooltip, FluidStack fluidStack) {}

    public String formatFluidTooltipAmount(double amount) {
        // the tooltip show the full number
        return TOOLTIP_FORMAT.format(amount) + " " + getBaseUnitBaseSuffix();
    }

    protected double getBaseUnitAmount(double amount) {
        return amount / 1000;
    }

    protected String getBaseUnit() {
        return UNIT_BUCKET;
    }

    protected String getBaseUnitBaseSuffix() {
        return "m";
    }

    @Override
    public void onInit() {
        this.textRenderer.setShadow(true);
        this.textRenderer.setScale(0.5f);
        this.textRenderer.setColor(Color.WHITE.main);
        getContext().getXeiSettings().addGhostIngredientSlot(this);
    }

    @Override
    public boolean isValidSyncHandler(SyncHandler syncHandler) {
        this.syncHandler = castIfTypeElseNull(syncHandler, FluidSlotSyncHandler.class);
        return this.syncHandler != null;
    }

    @Override
    public void draw(ModularGuiContext context, WidgetTheme widgetTheme) {
        IFluidTank fluidTank = getFluidTank();
        FluidStack content = this.syncHandler.getValue();
        if (content != null && !content.isEmpty()) {
            float y = this.contentOffsetY;
            float height = getArea().height - y * 2;
            if (!this.alwaysShowFull) {
                float newHeight = height * content.getAmount() * 1f / fluidTank.getCapacity();
                y += height - newHeight;
                height = newHeight;
            }
            GuiDraw.drawFluidTexture(context.getGraphics(), content,
                    this.contentOffsetX, y, getArea().width - this.contentOffsetX * 2, height, 0);
        }
        if (this.overlayTexture != null) {
            this.overlayTexture.drawAtZero(context, getArea(), widgetTheme);
        }
        if (content != null && this.syncHandler.controlsAmount()) {
            String s = FormattingUtil.formatNumberReadable2F(content.getAmount(), true) + getBaseUnit();
            this.textRenderer.setAlignment(Alignment.CenterRight, getArea().width - this.contentOffsetX - 1f);
            this.textRenderer.setPos((int) (this.contentOffsetX + 0.5f), (int) (getArea().height - 5.5f));
            this.textRenderer.draw(context.getGraphics(), Component.literal(s));
        }
    }

    @Override
    public void drawOverlay(ModularGuiContext context, WidgetTheme widgetTheme) {
        if (isHovering()) {
            RenderSystem.colorMask(true, true, true, false);
            GuiDraw.drawRect(context.getGraphics(), 1, 1, getArea().w() - 2, getArea().h() - 2, getSlotHoverColor());
            RenderSystem.colorMask(true, true, true, true);
        }
    }

    @Override
    public WidgetSlotTheme getWidgetThemeInternal(ITheme theme) {
        return theme.getFluidSlotTheme();
    }

    public int getSlotHoverColor() {
        WidgetTheme theme = getWidgetTheme(getContext().getTheme());
        if (theme instanceof WidgetSlotTheme slotTheme) {
            return slotTheme.getSlotHoverColor();
        }
        return ITheme.getDefault().getFluidSlotTheme().getSlotHoverColor();
    }

    @NotNull
    @Override
    public Result onMousePressed(double mouseX, double mouseY, int button) {
        if (!this.syncHandler.canFillSlot() && !this.syncHandler.canDrainSlot()) {
            return Result.ACCEPT;
        }
        ItemStack cursorStack = Minecraft.getInstance().player.containerMenu.getCarried();
        if (this.syncHandler.phantom() ||
                (!cursorStack.isEmpty() &&
                        cursorStack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM, null).isPresent())) {
            MouseData mouseData = MouseData.create(button);
            this.syncHandler.syncToServer(FluidSlotSyncHandler.SYNC_CLICK, mouseData::writeToPacket);
        }
        return Result.SUCCESS;
    }

    @Override
    public boolean onMouseScrolled(double mouseX, double mouseY, double delta) {
        if (this.syncHandler.phantom()) {
            if ((delta > 0 && !this.syncHandler.canFillSlot()) || (delta < 0 && !this.syncHandler.canDrainSlot())) {
                return false;
            }
            MouseData mouseData = MouseData.create(delta > 0 ? 1 : -1);
            this.syncHandler.syncToServer(FluidSlotSyncHandler.SYNC_SCROLL, mouseData::writeToPacket);
            return true;
        }
        return false;
    }

    @Override
    public @NotNull Result onKeyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == InputConstants.KEY_LSHIFT || keyCode == InputConstants.KEY_RSHIFT) {
            markTooltipDirty();
        }
        return Interactable.super.onKeyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean onKeyReleased(int keyCode, int scanCode, int modifiers) {
        if (keyCode == InputConstants.KEY_LSHIFT || keyCode == InputConstants.KEY_RSHIFT) {
            markTooltipDirty();
        }
        return Interactable.super.onKeyReleased(keyCode, scanCode, modifiers);
    }

    @Nullable
    public FluidStack getFluidStack() {
        return this.syncHandler == null ? null : this.syncHandler.getValue();
    }

    public IFluidTank getFluidTank() {
        return this.syncHandler == null ? EMPTY : this.syncHandler.fluidTank();
    }

    /**
     * Set the offset in x and y (on both sides) at which the fluid should be rendered.
     * Default is 1 for both.
     *
     * @param x x offset
     * @param y y offset
     */
    public FluidSlot contentOffset(int x, int y) {
        this.contentOffsetX = x;
        this.contentOffsetY = y;
        return this;
    }

    /**
     * @param alwaysShowFull if the fluid should be rendered as full or as the partial amount.
     */
    public FluidSlot alwaysShowFull(boolean alwaysShowFull) {
        this.alwaysShowFull = alwaysShowFull;
        return this;
    }

    /**
     * @param overlayTexture texture that is rendered on top of the fluid
     */
    public FluidSlot overlayTexture(@Nullable IDrawable overlayTexture) {
        this.overlayTexture = overlayTexture;
        return this;
    }

    public FluidSlot syncHandler(IFluidTank fluidTank) {
        return syncHandler(new FluidSlotSyncHandler(fluidTank));
    }

    public FluidSlot syncHandler(FluidSlotSyncHandler syncHandler) {
        setSyncHandler(syncHandler);
        this.syncHandler = syncHandler;
        return this;
    }

    /* === Jei ghost slot === */

    @Override
    public void setGhostIngredient(@NotNull FluidStack ingredient) {
        if (this.syncHandler.phantom()) {
            this.syncHandler.setValue(ingredient);
        }
    }

    @Override
    public @Nullable FluidStack castGhostIngredientIfValid(@NotNull Object ingredient) {
        return areAncestorsEnabled() && this.syncHandler.phantom() && ingredient instanceof FluidStack fluidStack ?
                fluidStack : null;
    }

    @Override
    public @NotNull Class<FluidStack> ingredientClass() {
        return FluidStack.class;
    }

    @Override
    public EntryList<FluidStack> getIngredients() {
        return FluidStackList.of(getFluidStack());
    }
}

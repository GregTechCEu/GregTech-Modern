package com.gregtechceu.gtceu.api.mui.widgets.slot;

import com.gregtechceu.gtceu.api.mui.base.ITheme;
import com.gregtechceu.gtceu.api.mui.base.drawable.IDrawable;
import com.gregtechceu.gtceu.api.mui.base.drawable.IKey;
import com.gregtechceu.gtceu.api.mui.base.value.ISyncOrValue;
import com.gregtechceu.gtceu.api.mui.base.widget.Interactable;
import com.gregtechceu.gtceu.api.mui.drawable.GuiDraw;
import com.gregtechceu.gtceu.api.mui.theme.SlotTheme;
import com.gregtechceu.gtceu.api.mui.theme.WidgetThemeEntry;
import com.gregtechceu.gtceu.api.mui.utils.MouseData;
import com.gregtechceu.gtceu.api.mui.value.sync.FluidSlotSyncHandler;
import com.gregtechceu.gtceu.api.mui.widgets.AbstractFluidDisplayWidget;
import com.gregtechceu.gtceu.client.mui.screen.RichTooltip;
import com.gregtechceu.gtceu.client.mui.screen.viewport.ModularGuiContext;
import com.gregtechceu.gtceu.integration.xei.handlers.GhostIngredientSlot;
import com.gregtechceu.gtceu.integration.xei.handlers.IngredientProvider;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.fml.ModList;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;

public class FluidSlot extends AbstractFluidDisplayWidget<FluidSlot>
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

    private FluidSlotSyncHandler syncHandler;
    private boolean alwaysShowFull = true;
    private boolean displayAmount = true;

    public FluidSlot() {
        tooltip().autoUpdate(true);
        tooltipBuilder(this::addTooltip);
    }

    protected void addTooltip(RichTooltip tooltip) {
        IFluidTank fluidTank = getFluidTank();
        FluidStack fluid = this.syncHandler.getValue();
        if (fluid != null && !fluid.isEmpty()) {
            tooltip.addLine(IKey.lang(fluid.getDisplayName())).spaceLine(2);
        }
        if (this.syncHandler.phantom()) {
            if (fluid != null) {
                if (this.syncHandler.controlsAmount()) {
                    tooltip.addLine(IKey.lang("modularui.fluid.phantom.amount",
                            formatFluidTooltipAmount(fluid.getAmount()), getUnit()));
                }
            } else {
                tooltip.addLine(IKey.lang("gtceu.fluid.empty"));
                tooltip.addLine(
                        IKey.lang("gtceu.fluid_pipe.capacity", formatFluidTooltipAmount(fluidTank.getCapacity()),
                                getUnit()));
            }
            if (this.syncHandler.controlsAmount()) {
                tooltip.addLine(IKey.lang("modularui.fluid.phantom.control"));
            }
        } else {
            if (fluid != null) {
                tooltip.addLine(IKey.lang("gtceu.fluid.amount", formatFluidTooltipAmount(fluid.getAmount()),
                        formatFluidTooltipAmount(fluidTank.getCapacity()), getUnit()));
                addAdditionalFluidInfo(tooltip, fluid);
            } else {
                tooltip.addLine(IKey.lang("gtceu.fluid.empty"));
            }
            if (this.syncHandler.canFillSlot() || this.syncHandler.canDrainSlot()) {
                tooltip.addLine(IKey.EMPTY); // Add an empty line to separate from the bottom material tooltips
                if (Interactable.hasShiftDown()) {
                    if (this.syncHandler.canFillSlot() && this.syncHandler.canDrainSlot()) {
                        tooltip.addLine(IKey.lang("gtceu.fluid.click_combined"));
                    } else if (this.syncHandler.canDrainSlot()) {
                        tooltip.addLine(IKey.lang("gtceu.fluid.click_to_fill"));
                    } else if (this.syncHandler.canFillSlot()) {
                        tooltip.addLine(IKey.lang("gtceu.fluid.click_to_empty"));
                    }
                } else {
                    tooltip.addLine(IKey.lang("gtceu.tooltip.hold_shift"));
                }
            }
        }
        if (fluid != null && !fluid.isEmpty()) {
            tooltip.add(getFluidModName(fluid));
        }
    }

    private Component getFluidModName(FluidStack fluidStack) {
        String modID = getFluidModID(fluidStack.getFluid());
        var container = ModList.get().getModContainerById(modID);
        if (container.isPresent()) {
            return Component.literal(container.get().getModInfo().getDisplayName()).withStyle(ChatFormatting.BLUE,
                    ChatFormatting.ITALIC);
        }
        return Component.literal(modID).withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC);
    }

    public static String getFluidModID(Fluid fluid) {
        ResourceLocation modName = BuiltInRegistries.FLUID.getKey(fluid);
        return modName.getNamespace();
    }

    public void addAdditionalFluidInfo(RichTooltip tooltip, FluidStack fluidStack) {}

    public String formatFluidTooltipAmount(double amount) {
        // the tooltip show the full number
        return TOOLTIP_FORMAT.format(amount);
    }

    @Override
    public void onInit() {
        getContext().getXeiSettings().addGhostIngredientSlot(this);
    }

    @Override
    public boolean isValidSyncOrValue(@NotNull ISyncOrValue syncOrValue) {
        return syncOrValue.isTypeOrEmpty(FluidSlotSyncHandler.class);
    }

    @Override
    protected void setSyncOrValue(@NotNull ISyncOrValue syncOrValue) {
        super.setSyncOrValue(syncOrValue);
        this.syncHandler = syncOrValue.castNullable(FluidSlotSyncHandler.class);
    }

    @Override
    protected boolean displayAmountText() {
        return this.syncHandler == null || this.syncHandler.controlsAmount();
    }

    @Override
    public void drawOverlay(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        if (isHovering()) {
            RenderSystem.colorMask(true, true, true, false);
            GuiDraw.drawRect(context.getGraphics(), 1, 1, getArea().w() - 2, getArea().h() - 2, getSlotHoverColor());
            RenderSystem.colorMask(true, true, true, true);
        }
    }

    @Override
    public WidgetThemeEntry<?> getWidgetThemeInternal(ITheme theme) {
        return theme.getFluidSlotTheme();
    }

    public int getSlotHoverColor() {
        WidgetThemeEntry<SlotTheme> theme = getWidgetTheme(getContext().getTheme(), SlotTheme.class);
        return theme.getTheme().getSlotHoverColor();
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

    @Override
    protected int getCapacity() {
        return this.alwaysShowFull ? 0 : getFluidTank().getCapacity();
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
    @ApiStatus.ScheduledForRemoval(inVersion = "3.2.0")
    @Deprecated
    public FluidSlot contentOffset(int x, int y) {
        return contentPaddingLeft(x).contentPaddingTop(y);
    }

    public FluidSlot displayAmount(boolean displayAmount) {
        this.displayAmount = displayAmount;
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
    @ApiStatus.ScheduledForRemoval(inVersion = "3.2.0")
    @Deprecated
    public FluidSlot overlayTexture(@Nullable IDrawable overlayTexture) {
        return overlay(overlayTexture);
    }

    public FluidSlot syncHandler(IFluidTank fluidTank) {
        return syncHandler(new FluidSlotSyncHandler(fluidTank));
    }

    public FluidSlot syncHandler(FluidSlotSyncHandler syncHandler) {
        setSyncOrValue(ISyncOrValue.orEmpty(syncHandler));
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
}

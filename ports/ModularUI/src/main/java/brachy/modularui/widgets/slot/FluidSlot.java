package brachy.modularui.widgets.slot;

import brachy.modularui.api.ITheme;
import brachy.modularui.api.MCHelper;
import brachy.modularui.api.drawable.Text;
import brachy.modularui.api.value.ISyncOrValue;
import brachy.modularui.api.widget.Interactable;
import brachy.modularui.drawable.GuiDraw;
import brachy.modularui.integration.recipeviewer.handlers.GhostIngredientSlot;
import brachy.modularui.integration.recipeviewer.handlers.IngredientProvider;
import brachy.modularui.screen.RichTooltip;
import brachy.modularui.screen.viewport.ModularGuiContext;
import brachy.modularui.theme.SlotTheme;
import brachy.modularui.theme.WidgetThemeEntry;
import brachy.modularui.utils.IMultiFluidTankHandler;
import brachy.modularui.utils.LangUtil;
import brachy.modularui.utils.MouseData;
import brachy.modularui.value.sync.FluidSlotSyncHandler;
import brachy.modularui.widgets.AbstractFluidDisplayWidget;

import net.minecraft.world.item.ItemStack;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.IFluidTank;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

import lombok.Getter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;

@Accessors(fluent = true, chain = true)
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

    @Getter private boolean alwaysShowFull = true;

    public FluidSlot() {
        tooltip().autoUpdate(true);
        tooltipBuilder(this::addTooltip);
    }

    protected void addTooltip(RichTooltip tooltip) {
        IFluidTank fluidTank = getFluidTank();
        FluidStack fluid = this.syncHandler.getValue();
        boolean phantom = this.syncHandler.phantom();
        if (fluid != null && !fluid.isEmpty()) {
            tooltip.addLine(fluid.getHoverName()).spaceLine(2);
        }
        if (phantom) {
            if (fluid != null) {
                if (this.syncHandler.controlsAmount()) {
                    tooltip.addLine(Text.lang("modularui.fluid.phantom.amount",
                            formatFluidTooltipAmount(fluid.getAmount()), getUnit()));
                }
            } else {
                tooltip.addLine(Text.lang("modularui.fluid.empty"));
                tooltip.addLine(
                        Text.lang("modularui.fluid.capacity", formatFluidTooltipAmount(fluidTank.getCapacity()),
                                getUnit()));
            }
        } else {
            if (fluid != null) {
                tooltip.addLine(Text.lang("modularui.fluid.amount", formatFluidTooltipAmount(fluid.getAmount()),
                        formatFluidTooltipAmount(fluidTank.getCapacity()), getUnit()));
                addAdditionalFluidInfo(tooltip, fluid);
            } else {
                tooltip.addLine(Text.lang("modularui.fluid.empty"));
            }
        }
        boolean fills = this.syncHandler.canFillSlot(), drains = this.syncHandler.canDrainSlot();
        if (fills || drains) {
            tooltip.addLine(Text.EMPTY); // Add an empty line to separate from the bottom material tooltips
            if (Interactable.hasShiftDown()) {
                String phantom_suffix = phantom ? "_phantom" : "";
                if (fills && drains) {
                    tooltip.addLine(Text.lang("modularui.fluid.click_combined" + phantom_suffix));
                } else if (drains) {
                    tooltip.addLine(Text.lang("modularui.fluid.click_to_fill" + phantom_suffix));
                } else {
                    tooltip.addLine(Text.lang("modularui.fluid.click_to_empty" + phantom_suffix));
                }
                if (!phantom || this.syncHandler.controlsAmount()) {
                    tooltip.addLine(Text.lang("modularui.fluid.scroll"));
                }
            } else {
                tooltip.addLine(Text.lang("modularui.fluid.controls_info"));
            }
        }
        if (fluid != null && !fluid.isEmpty()) {
            tooltip.add(LangUtil.getFluidModName(fluid));
        }
    }

    public void addAdditionalFluidInfo(RichTooltip tooltip, FluidStack fluidStack) {}

    public String formatFluidTooltipAmount(double amount) {
        // the tooltip show the full number
        return TOOLTIP_FORMAT.format(amount);
    }

    @Override
    public void onInit() {
        getContext().getRecipeViewerSettings().addGhostIngredientSlot(this);
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
    public @NotNull FluidSlotSyncHandler getSyncHandler() {
        if (this.syncHandler == null) {
            throw new IllegalStateException("Widget is not initialised or not synced!");
        }
        return syncHandler;
    }

    @Override
    protected boolean displayAmountText() {
        return this.syncHandler == null || this.syncHandler.controlsAmount();
    }

    @Override
    public void drawOverlay(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        super.drawOverlay(context, widgetTheme);
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
        WidgetThemeEntry<SlotTheme> theme = getWidgetTheme(getPanel().getTheme(), SlotTheme.class);
        return theme.theme().getSlotHoverColor();
    }

    @NotNull
    @Override
    public Result onMousePressed(int button) {
        if (!this.syncHandler.canFillSlot() && !this.syncHandler.canDrainSlot()) {
            return Result.ACCEPT;
        }
        ItemStack cursorStack = MCHelper.getPlayer().containerMenu.getCarried();
        if (this.syncHandler.phantom() ||
                (!cursorStack.isEmpty() && cursorStack.getCapability(Capabilities.FluidHandler.ITEM) != null)) {
            MouseData mouseData = MouseData.create(button);
            this.syncHandler.syncToServer(FluidSlotSyncHandler.SYNC_CLICK, mouseData::writeToPacket);
        }
        return Result.SUCCESS;
    }

    @Override
    public boolean onMouseScrolled(double scrollX, double scrollY) {
        if ((scrollY > 0 && !this.syncHandler.canFillSlot()) || (scrollY < 0 && !this.syncHandler.canDrainSlot())) {
            return false;
        }
        MouseData mouseData = MouseData.create(scrollY > 0 ? 1 : -1);
        this.syncHandler.syncToServer(FluidSlotSyncHandler.SYNC_SCROLL, mouseData::writeToPacket);
        return true;
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
    public int getCapacity() {
        return this.alwaysShowFull ? 0 : getFluidTank().getCapacity();
    }

    public FluidStack getFluidStack() {
        return this.syncHandler == null ? FluidStack.EMPTY : this.syncHandler.getOrDefault(FluidStack.EMPTY);
    }

    public IFluidTank getFluidTank() {
        return this.syncHandler == null ? EMPTY : this.syncHandler.fluidTank();
    }

    /**
     * @param alwaysShowFull if the fluid should be rendered as full or as the partial amount.
     */
    public FluidSlot alwaysShowFull(boolean alwaysShowFull) {
        this.alwaysShowFull = alwaysShowFull;
        return this;
    }

    public FluidSlot syncHandler(IFluidTank fluidTank) {
        return syncHandler(new FluidSlotSyncHandler(fluidTank));
    }

    public FluidSlot syncHandler(IMultiFluidTankHandler fluidTank, int index) {
        return syncHandler(fluidTank.getFluidTank(index));
    }

    public FluidSlot syncHandler(FluidSlotSyncHandler syncHandler) {
        setSyncOrValue(ISyncOrValue.orEmpty(syncHandler));
        return this;
    }

    public FluidSlot tank(IFluidTank fluidTank) {
        return syncHandler(fluidTank);
    }

    public FluidSlot tank(IMultiFluidTankHandler fluidTank, int index) {
        return syncHandler(fluidTank, index);
    }

    /* === recipe viewer ghost slot === */

    @Override
    public void setGhostIngredient(@NotNull FluidStack ingredient) {
        if (this.syncHandler.phantom()) {
            if (ingredient.getFluid() != Fluids.EMPTY) {
                ingredient.setAmount(this.syncHandler.controlsAmount() ? 1000 : 1);
                this.syncHandler.playSound(MCHelper.getPlayer(), ingredient, SoundActions.BUCKET_FILL);
            }
            this.syncHandler.setValue(ingredient);
        }
    }

    @Override
    public @Nullable FluidStack castGhostIngredientIfValid(@NotNull Object ingredient) {
        return areAncestorsEnabled() && this.syncHandler.phantom() && ingredient instanceof FluidStack fluidStack ?
                fluidStack : null;
    }
}

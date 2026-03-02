package com.gregtechceu.gtceu.integration.ae2.mui;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.mui.base.drawable.IKey;
import com.gregtechceu.gtceu.api.mui.base.widget.Interactable;
import com.gregtechceu.gtceu.api.mui.theme.WidgetThemeEntry;
import com.gregtechceu.gtceu.api.mui.value.StringValue;
import com.gregtechceu.gtceu.api.mui.value.sync.PanelSyncManager;
import com.gregtechceu.gtceu.api.mui.widget.Widget;
import com.gregtechceu.gtceu.api.mui.widgets.ButtonWidget;
import com.gregtechceu.gtceu.api.mui.widgets.layout.Row;
import com.gregtechceu.gtceu.api.mui.widgets.textfield.TextFieldWidget;
import com.gregtechceu.gtceu.client.mui.screen.ModularPanel;
import com.gregtechceu.gtceu.client.mui.screen.SecondaryPanel;
import com.gregtechceu.gtceu.client.mui.screen.viewport.ModularGuiContext;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.integration.ae2.slot.ExportOnlyAEFluidList;
import com.gregtechceu.gtceu.integration.ae2.slot.ExportOnlyAEItemList;
import com.gregtechceu.gtceu.integration.ae2.slot.IConfigurableSlotList;
import com.gregtechceu.gtceu.integration.emi.EmiStackConverter;
import com.gregtechceu.gtceu.integration.recipeviewer.handlers.GhostIngredientSlot;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import com.mojang.blaze3d.platform.InputConstants;
import dev.emi.emi.api.stack.EmiStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AEConfigWidget extends Widget<AEConfigWidget>
                            implements Interactable, GhostIngredientSlot<ItemStack> {

    private static final int CELL_SIZE = 18;
    private static final int PAIR_HEIGHT = CELL_SIZE * 2 + 2;

    private final IConfigurableSlotList slotList;
    private final int slotCount;
    private final boolean isFluid;
    private final int columns;
    private PanelSyncManager syncManager;
    private AEConfigSyncHandler configSyncHandler;

    @OnlyIn(Dist.CLIENT)
    private float lastMouseX;
    @OnlyIn(Dist.CLIENT)
    private float lastMouseY;
    @OnlyIn(Dist.CLIENT)
    private int editingSlotIndex;
    @OnlyIn(Dist.CLIENT)
    private String pendingAmount;
    @OnlyIn(Dist.CLIENT)
    private SecondaryPanel amountEditorPanel;
    @OnlyIn(Dist.CLIENT)
    private TextFieldWidget amountField;

    public AEConfigWidget(IConfigurableSlotList slotList, int slotCount, boolean isFluid) {
        this.slotList = slotList;
        this.slotCount = slotCount;
        this.isFluid = isFluid;
        this.columns = 8;
    }

    public AEConfigWidget syncManager(PanelSyncManager syncManager) {
        this.syncManager = syncManager;
        this.configSyncHandler = new AEConfigSyncHandler(slotList, slotCount);
        syncManager.syncValue("ae_config_display", configSyncHandler);
        return this;
    }

    @Override
    public void onInit() {
        super.onInit();
        getContext().getRecipeViewerSettings().addGhostIngredientSlot(this);
        editingSlotIndex = -1;
        pendingAmount = "0";
        amountEditorPanel = new SecondaryPanel(getPanel(), this::buildAmountEditor, true);
    }

    @OnlyIn(Dist.CLIENT)
    private ModularPanel buildAmountEditor(ModularPanel parent, net.minecraft.world.entity.player.Player player) {
        amountField = new TextFieldWidget() {

            @Override
            public @NotNull Interactable.Result onKeyPressed(int keyCode, int scanCode, int modifiers) {
                if (isFocused() && (keyCode == InputConstants.KEY_RETURN ||
                        keyCode == InputConstants.KEY_NUMPADENTER)) {
                    confirmAmountEdit();
                    return Interactable.Result.SUCCESS;
                }
                return super.onKeyPressed(keyCode, scanCode, modifiers);
            }
        };
        amountField.expanded().heightRel(1f)
                .value(new StringValue.Dynamic(() -> pendingAmount, val -> pendingAmount = val));

        return new ModularPanel("ae_amount_editor")
                .size(120, 36)
                .alignX(0.5f).alignY(0.35f)
                .background(GTGuiTextures.BACKGROUND)
                .child(ButtonWidget.panelCloseButton())
                .child(IKey.str("Amount").asWidget().pos(4, 4))
                .child(new Row()
                        .left(4).right(4).bottom(4).height(18)
                        .child(amountField)
                        .child(new ButtonWidget<>()
                                .size(18, 18)
                                .overlay(IKey.str("✓"))
                                .onMousePressed((mouseX, mouseY, button) -> {
                                    if (button == 0) {
                                        confirmAmountEdit();
                                        return true;
                                    }
                                    return false;
                                })));
    }

    @OnlyIn(Dist.CLIENT)
    private void confirmAmountEdit() {
        if (editingSlotIndex < 0 || syncManager == null) return;
        String text = amountField != null ? amountField.getText() : pendingAmount;
        long amount = AEGuiHelper.parseAmount(text);
        if (amount > 0) {
            String resolved = String.valueOf(amount);
            pendingAmount = resolved;
            if (amountField != null) {
                amountField.setText(resolved);
            }
            int slot = editingSlotIndex;
            syncManager.callSyncedAction("ae_config_amount", buf -> {
                buf.writeVarInt(slot);
                buf.writeVarLong(amount);
            });
        }
        editingSlotIndex = -1;
        amountEditorPanel.closePanel();
    }

    @OnlyIn(Dist.CLIENT)
    private void openAmountEditor(int slotIndex) {
        GenericStack config = configSyncHandler != null ? configSyncHandler.getClientConfig(slotIndex) : null;
        if (config == null) return;
        editingSlotIndex = slotIndex;
        pendingAmount = String.valueOf(config.amount());
        if (amountEditorPanel.isPanelOpen()) {
            if (amountField != null) {
                amountField.setText(pendingAmount);
            }
            return;
        }
        amountEditorPanel.deleteCachedPanel();
        amountEditorPanel.openPanel();
    }

    private boolean isAutoPull() {
        return slotList instanceof ExportOnlyAEItemList itemList && itemList.isAutoPull() ||
                slotList instanceof ExportOnlyAEFluidList fluidList && fluidList.isAutoPull();
    }

    private boolean isStocking() {
        return slotList instanceof ExportOnlyAEItemList itemList && itemList.isStocking() ||
                slotList instanceof ExportOnlyAEFluidList fluidList && fluidList.isStocking();
    }

    private int slotX(int index) {
        return (index % columns) * CELL_SIZE;
    }

    private int slotY(int index) {
        return (index / columns) * PAIR_HEIGHT;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        GuiGraphics graphics = context.getGraphics();
        boolean autoPull = isAutoPull();
        boolean stocking = isStocking();
        lastMouseX = context.getMouseX();
        lastMouseY = context.getMouseY();

        for (int i = 0; i < slotCount; i++) {
            int x = slotX(i);
            int y = slotY(i);
            drawSlotBackground(graphics, x, y, autoPull);

            GenericStack config = configSyncHandler != null ? configSyncHandler.getClientConfig(i) : null;
            GenericStack stock = configSyncHandler != null ? configSyncHandler.getClientStock(i) : null;

            if (config != null) {
                drawStack(graphics, config, x + 1, y + 1);
                if (!stocking) AEGuiHelper.drawAmountOverlay(graphics, config.amount(), x + 1, y + 1);
            }

            if (stock != null) {
                drawStack(graphics, stock, x + 1, y + 19);
                AEGuiHelper.drawAmountOverlay(graphics, stock.amount(), x + 1, y + 19);
            }

            float mouseX = context.getMouseX();
            float mouseY = context.getMouseY();
            if (mouseX >= x && mouseX < x + CELL_SIZE && mouseY >= y && mouseY < y + CELL_SIZE) {
                AEGuiHelper.drawSelectionOverlay(graphics, x + 1, y + 1, 16, 16);
            } else if (mouseX >= x && mouseX < x + CELL_SIZE && mouseY >= y + CELL_SIZE && mouseY < y + CELL_SIZE * 2) {
                AEGuiHelper.drawSelectionOverlay(graphics, x + 1, y + 19, 16, 16);
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void drawForeground(ModularGuiContext context) {
        float mouseX = context.getMouseX();
        float mouseY = context.getMouseY();
        GuiGraphics graphics = context.getGraphics();

        for (int i = 0; i < slotCount; i++) {
            int x = slotX(i);
            int y = slotY(i);

            if (mouseX >= x && mouseX < x + CELL_SIZE && mouseY >= y && mouseY < y + CELL_SIZE * 2) {
                GenericStack tooltipStack = mouseY < y + CELL_SIZE ?
                        (configSyncHandler != null ? configSyncHandler.getClientConfig(i) : null) :
                        (configSyncHandler != null ? configSyncHandler.getClientStock(i) : null);
                if (tooltipStack != null) {
                    ItemStack wrapped = GenericStack.wrapInItemStack(tooltipStack);
                    graphics.renderTooltip(Minecraft.getInstance().font, wrapped,
                            (int) context.getAbsMouseX(), (int) context.getAbsMouseY());
                }
            }
        }
    }

    @Override
    public Result onMousePressed(double mouseX, double mouseY, int button) {
        double localX = mouseX - getArea().x;
        double localY = mouseY - getArea().y;

        int slotIndex = getSlotAtLocal(localX, localY);
        if (slotIndex < 0) return Result.IGNORE;

        int y = slotY(slotIndex);
        boolean overConfig = localY < y + CELL_SIZE;
        boolean overStock = localY >= y + CELL_SIZE && localY < y + CELL_SIZE * 2;

        if (overConfig && !isAutoPull() && syncManager != null) {
            if (button == 1) {
                syncManager.callSyncedAction("ae_config_clear", buf -> buf.writeVarInt(slotIndex));
                return Result.SUCCESS;
            } else if (button == 0) {
                GenericStack config = configSyncHandler != null ? configSyncHandler.getClientConfig(slotIndex) : null;
                boolean holdingItem = !Minecraft.getInstance().player.containerMenu.getCarried().isEmpty();
                if (config != null && !holdingItem) {
                    openAmountEditor(slotIndex);
                } else {
                    syncManager.callSyncedAction("ae_config_set", buf -> buf.writeVarInt(slotIndex));
                }
                return Result.SUCCESS;
            }
        }

        if (overStock && !isStocking() && !isFluid && button == 0 && syncManager != null) {
            syncManager.callSyncedAction("ae_stock_pickup", buf -> buf.writeVarInt(slotIndex));
            return Result.SUCCESS;
        }

        return Result.IGNORE;
    }

    @Override
    public boolean onMouseScrolled(double mouseX, double mouseY, double delta) {
        if (isStocking()) return false;

        double localX = mouseX - getArea().x;
        double localY = mouseY - getArea().y;
        int slotIndex = getSlotAtLocal(localX, localY);
        if (slotIndex < 0) return false;

        if (localY >= slotY(slotIndex) + CELL_SIZE) return false;

        GenericStack config = configSyncHandler != null ? configSyncHandler.getClientConfig(slotIndex) : null;
        if (config == null || delta == 0) return false;

        long current = config.amount();
        long next = Interactable.hasControlDown() ?
                (delta > 0 ? current * 2L : current / 2L) :
                (delta > 0 ? current + 1L : current - 1L);

        if (next > 0 && next < Integer.MAX_VALUE + 1L && syncManager != null) {
            syncManager.callSyncedAction("ae_config_amount", buf -> {
                buf.writeVarInt(slotIndex);
                buf.writeVarLong(next);
            });
            return true;
        }
        return false;
    }

    // --- GhostIngredientSlot ---

    @Override
    public void setGhostIngredient(@NotNull ItemStack ingredient) {
        if (isAutoPull() || syncManager == null) return;
        int slot = findTargetSlot();
        if (slot < 0) return;

        if (isFluid) {
            FluidUtil.getFluidContained(ingredient).ifPresent(fluid -> {
                if (!fluid.isEmpty()) {
                    syncManager.callSyncedAction("ae_config_set_ghost", buf -> {
                        buf.writeVarInt(findTargetSlot());
                        buf.writeBoolean(true);
                        fluid.writeToPacket(buf);
                    });
                }
            });
        } else {
            syncManager.callSyncedAction("ae_config_set_ghost", buf -> {
                buf.writeVarInt(slot);
                buf.writeBoolean(false);
                buf.writeItem(ingredient);
            });
        }
    }

    @Override
    public boolean ingredientHandlingOverride(Object ingredient) {
        if (isAutoPull() || syncManager == null) return false;
        if (!(ingredient instanceof EmiStack emiStack)) return false;

        if (isFluid) {
            FluidStack fluidStack = EmiStackConverter.FLUID.convertFrom(emiStack);
            if (fluidStack == null) return false;
            if (fluidStack.getAmount() <= 0) fluidStack.setAmount(1000);
            int slot = findTargetSlot();
            if (slot < 0) return false;
            FluidStack toSend = fluidStack;
            syncManager.callSyncedAction("ae_config_set_ghost", buf -> {
                buf.writeVarInt(slot);
                buf.writeBoolean(true);
                toSend.writeToPacket(buf);
            });
            return true;
        } else {
            ItemStack itemStack = EmiStackConverter.ITEM.convertFrom(emiStack);
            if (itemStack == null) return false;
            if (itemStack.getCount() <= 0) itemStack.setCount(1);
            int slot = findTargetSlot();
            if (slot < 0) return false;
            syncManager.callSyncedAction("ae_config_set_ghost", buf -> {
                buf.writeVarInt(slot);
                buf.writeBoolean(false);
                buf.writeItem(itemStack);
            });
            return true;
        }
    }

    @Override
    public @Nullable ItemStack castGhostIngredientIfValid(@NotNull Object ingredient) {
        if (isAutoPull() || !areAncestorsEnabled()) return null;
        return !isFluid && ingredient instanceof ItemStack itemStack ? itemStack : null;
    }

    @Override
    public @NotNull Class<ItemStack> ingredientClass() {
        return ItemStack.class;
    }

    // --- Internals ---

    private int findTargetSlot() {
        int hovered = getSlotAtLocal(lastMouseX, lastMouseY);
        if (hovered >= 0) return hovered;
        for (int i = 0; i < slotCount; i++) {
            if (configSyncHandler == null || configSyncHandler.getClientConfig(i) == null) return i;
        }
        return 0;
    }

    private int getSlotAtLocal(double localX, double localY) {
        for (int i = 0; i < slotCount; i++) {
            int x = slotX(i);
            int y = slotY(i);
            if (localX >= x && localX < x + CELL_SIZE && localY >= y && localY < y + CELL_SIZE * 2) {
                return i;
            }
        }
        return -1;
    }

    @OnlyIn(Dist.CLIENT)
    private void drawStack(GuiGraphics graphics, GenericStack stack, int x, int y) {
        if (isFluid) {
            AEGuiHelper.drawFluid(graphics, stack, x, y);
        } else if (stack.what() instanceof AEItemKey key) {
            ItemStack displayStack = new ItemStack(key.getItem());
            if (key.hasTag()) displayStack.setTag(key.getTag().copy());
            graphics.renderItem(displayStack, x, y);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void drawSlotBackground(GuiGraphics graphics, int x, int y, boolean autoPull) {
        if (autoPull) {
            GuiTextures.SLOT_DARK.draw(graphics, 0, 0, x, y, 18, 18);
            GuiTextures.CONFIG_ARROW.draw(graphics, 0, 0, x, y, 18, 18);
        } else {
            (isFluid ? GuiTextures.FLUID_SLOT : GuiTextures.SLOT).draw(graphics, 0, 0, x, y, 18, 18);
            GuiTextures.CONFIG_ARROW_DARK.draw(graphics, 0, 0, x, y, 18, 18);
        }
        GuiTextures.SLOT_DARK.draw(graphics, 0, 0, x, y + 18, 18, 18);
    }
}

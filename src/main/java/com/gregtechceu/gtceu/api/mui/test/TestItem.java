package com.gregtechceu.gtceu.api.mui.test;

import com.gregtechceu.gtceu.api.mui.base.IPanelHandler;
import com.gregtechceu.gtceu.api.mui.base.IUIHolder;
import com.gregtechceu.gtceu.api.mui.base.drawable.IDrawable;
import com.gregtechceu.gtceu.api.mui.base.drawable.IKey;
import com.gregtechceu.gtceu.api.mui.drawable.GuiDraw;
import com.gregtechceu.gtceu.api.mui.drawable.Rectangle;
import com.gregtechceu.gtceu.api.mui.factory.PlayerInventoryGuiData;
import com.gregtechceu.gtceu.api.mui.factory.inventory.InventoryTypes;
import com.gregtechceu.gtceu.api.mui.utils.Alignment;
import com.gregtechceu.gtceu.api.mui.utils.Color;
import com.gregtechceu.gtceu.api.mui.utils.ColorShade;
import com.gregtechceu.gtceu.api.mui.value.sync.PanelSyncManager;
import com.gregtechceu.gtceu.api.mui.value.sync.SyncHandlers;
import com.gregtechceu.gtceu.api.mui.widget.ParentWidget;
import com.gregtechceu.gtceu.api.mui.widgets.ButtonWidget;
import com.gregtechceu.gtceu.api.mui.widgets.ColorPickerDialog;
import com.gregtechceu.gtceu.api.mui.widgets.CycleButtonWidget;
import com.gregtechceu.gtceu.api.mui.widgets.SlotGroupWidget;
import com.gregtechceu.gtceu.api.mui.widgets.layout.Column;
import com.gregtechceu.gtceu.api.mui.widgets.layout.Flow;
import com.gregtechceu.gtceu.api.mui.widgets.slot.ItemSlot;
import com.gregtechceu.gtceu.api.mui.widgets.slot.ModularSlot;
import com.gregtechceu.gtceu.client.mui.screen.ModularPanel;
import com.gregtechceu.gtceu.client.mui.screen.UISettings;
import com.gregtechceu.gtceu.client.mui.screen.viewport.ModularGuiContext;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.ArrayList;
import java.util.List;

import static net.minecraftforge.common.capabilities.ForgeCapabilities.ITEM_HANDLER;

public class TestItem extends Item implements ICurioItem, IUIHolder<PlayerInventoryGuiData<?>> {

    public TestItem(Properties properties) {
        super(properties);
        CuriosApi.registerCurio(this, this);
    }

    @Override
    public ModularPanel buildUI(PlayerInventoryGuiData<?> data, PanelSyncManager syncManager, UISettings settings) {
        var cap = data.getUsedItemStack().getCapability(ITEM_HANDLER);
        if (!cap.isPresent() || cap.resolve().isEmpty()) return null;
        IItemHandler itemHandler = cap.resolve().get();
        syncManager.registerSlotGroup("mixer_items", 2);
        if (!(itemHandler instanceof IItemHandlerModifiable ihm)) return null;

        // if the player slot is the slot with this item, then disallow any interaction
        // if the item is not in the player inventory (bauble for example), then this items slot is not on the screen,
        // and we don't need to limit accessibility
        if (data.getInventoryType() == InventoryTypes.PLAYER) {
            syncManager.bindPlayerInventory(data.getPlayer(), (inv, index) -> index == data.getSlotIndex() ?
                    new ModularSlot(inv, index).accessibility(false, false) :
                    new ModularSlot(inv, index));
        }
        ModularPanel panel = ModularPanel.defaultPanel("knapping_gui").resizeableOnDrag(true);
        panel.child(new Column().margin(7)
                .child(new ParentWidget<>().widthRel(1f).expanded()
                        .child(SlotGroupWidget.builder()
                                .row("I I")
                                .row("  I")
                                .row("   ")
                                .row(" I ")
                                .key('I', index -> new ItemSlot().slot(SyncHandlers.itemSlot(ihm, index)
                                        .ignoreMaxStackSize(true)
                                        .slotGroup("mixer_items")
                                        // do not allow putting items which can hold other items into the item
                                        // some mods don't do this on their backpacks, so it won't catch those cases
                                        .filter(stack -> !stack.getCapability(ITEM_HANDLER).isPresent())))
                                .build()
                                .align(Alignment.TopLeft)))
                .child(SlotGroupWidget.playerInventory(false)));

        return panel;
    }

    public @NotNull ModularPanel buildColorUI(ModularGuiContext context) {
        List<Pair<Integer, Float>> colors = new ArrayList<>();
        for (ColorShade shade : ColorShade.getAll()) {
            for (int c : shade) {
                colors.add(Pair.of(c, Color.getLuminance(c)));
            }
        }
        colors.sort((a, b) -> Float.compare(a.getRight(), b.getRight()));

        IDrawable luminanceSortedColors = (context1, x, y, width, height, widgetTheme) -> {
            float w = (float) width / colors.size();
            float x0 = x;
            for (Pair<Integer, Float> c : colors) {
                GuiDraw.drawRect(context.getGraphics(), x0, y, w, height, c.getLeft());
                x0 += w;
            }
        };

        Rectangle color1 = new Rectangle().setColor(Color.BLACK.main);
        Rectangle color2 = new Rectangle().setColor(Color.WHITE.main);

        IDrawable gradient = (context1, x, y, width, height, widgetTheme) -> GuiDraw.drawHorizontalGradientRect(
                context1.getGraphics(), x, y, width, height, color1.getColor(), color2.getColor());
        IDrawable correctedGradient = (context1, x, y, width, height, widgetTheme) -> {
            int points = 500;
            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder buffer = tesselator.getBuilder();

            buffer.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
            float x0 = x;
            float w = (float) width / points;
            for (int i = 0; i < points; i++) {
                int color = Color.lerp(color1.getColor(), color2.getColor(), (float) i / points);
                int r = Color.getRed(color), g = Color.getGreen(color), b = Color.getBlue(color), a = 0xFF;
                buffer.vertex(x0, y, 0).color(r, g, b, a).endVertex();
                buffer.vertex(x0, y + height, 0).color(r, g, b, a).endVertex();
                x0 += w;
            }
            tesselator.end();
        };

        ModularPanel panel = new ModularPanel("colors").width(300).coverChildrenHeight().padding(7);

        IPanelHandler colorPicker1 = IPanelHandler.simple(panel,
                (mainPanel, player) -> new ColorPickerDialog("color_picker1", color1::setColor, color1.getColor(), true)
                        .setDraggable(true)
                        .relative(panel)
                        .top(0)
                        .rightRel(1f),
                true);
        IPanelHandler colorPicker2 = IPanelHandler.simple(panel,
                (mainPanel, player) -> new ColorPickerDialog("color_picker2", color2::setColor, color2.getColor(), true)
                        .setDraggable(true)
                        .relative(panel)
                        .top(0)
                        .leftRel(1f),
                true);

        return panel
                .child(Flow.column()
                        .coverChildrenHeight()
                        .child(IKey.str("Colors sorted by luminance").asWidget().margin(1))
                        .child(luminanceSortedColors.asWidget().widthRel(1f).height(10))
                        .child(IKey.str("Blending color").asWidget().margin(1).marginTop(2))
                        .child(Flow.row()
                                .coverChildrenHeight()
                                .mainAxisAlignment(Alignment.MainAxis.SPACE_BETWEEN)
                                .child(new ButtonWidget<>()
                                        .name("color picker button 1")
                                        .background(color1)
                                        .disableHoverBackground()
                                        .onMousePressed((mouseX, mouseY, mouseButton) -> {
                                            colorPicker1.openPanel();
                                            return true;
                                        }))
                                .child(new CycleButtonWidget())
                                .child(new ButtonWidget<>()
                                        .name("color picker button 2")
                                        .background(color2)
                                        .disableHoverBackground()
                                        .onMousePressed((mouseX, mouseY, mouseButton) -> {
                                            colorPicker2.openPanel();
                                            return true;
                                        })))
                        .child(IKey.str("OpenGL color gradient").asWidget().margin(1))
                        .child(gradient.asWidget().widthRel(1f).height(10))
                        .child(IKey.str("Gamma corrected gradient").asWidget().margin(1))
                        .child(correctedGradient.asWidget().widthRel(1f).height(10)));
    }

    @Override
    public @Nullable ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new ICapabilityProvider() {

            @Override
            public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
                if (cap == ITEM_HANDLER) {
                    var handler = new ItemStackHandler(4);
                    return LazyOptional.of(() -> handler).cast();
                }
                return LazyOptional.empty();
            }
        };
    }

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        return true;
    }
}

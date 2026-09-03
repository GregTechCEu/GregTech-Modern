package com.gregtechceu.gtceu.common.item.tool.behavior;

import brachy.modularui.drawable.Icon;
import com.gregtechceu.gtceu.api.item.datacomponents.AoESymmetrical;
import com.gregtechceu.gtceu.api.item.tool.behavior.IToolUIBehavior;
import com.gregtechceu.gtceu.api.item.tool.behavior.ToolBehaviorType;
import com.gregtechceu.gtceu.common.data.GTToolBehaviors;
import com.gregtechceu.gtceu.common.data.item.GTDataComponents;

import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.drawable.GuiTextures;
import brachy.modularui.factory.PlayerInventoryGuiData;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.InteractionSyncHandler;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widgets.ButtonWidget;
import brachy.modularui.widgets.layout.Flow;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

import static com.gregtechceu.gtceu.api.item.tool.ToolHelper.getAoEStateMutable;

public class AOEConfigUIBehavior implements IToolUIBehavior<AOEConfigUIBehavior> {

    public static final AOEConfigUIBehavior INSTANCE = new AOEConfigUIBehavior();
    public static final Codec<AOEConfigUIBehavior> CODEC = Codec.unit(INSTANCE);
    public static final StreamCodec<ByteBuf, AOEConfigUIBehavior> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public boolean shouldOpenUI(@NotNull Player player, @NotNull InteractionHand hand) {
        return player.isShiftKeyDown() &&
                !player.getItemInHand(hand).getOrDefault(GTDataComponents.MAX_AOE, AoESymmetrical.ZERO).isZero();
    }

    @Override
    public ModularPanel<?> buildUI(PlayerInventoryGuiData<?> data, PanelSyncManager syncManager, UISettings settings) {
        ItemStack held = data.getUsedItemStack();
        final AoESymmetrical.Mutable definition = getAoEStateMutable(held);

        // spotless:off
        InteractionSyncHandler minusCols = new InteractionSyncHandler();
        minusCols.setOnMousePressed(data1 -> held.set(GTDataComponents.AOE, definition.decreaseColumn().toImmutable()));
        InteractionSyncHandler plusCols = new InteractionSyncHandler();
        plusCols.setOnMousePressed(data1 -> held.set(GTDataComponents.AOE, definition.increaseColumn().toImmutable()));
        InteractionSyncHandler minusRows = new InteractionSyncHandler();
        minusRows.setOnMousePressed(data1 -> held.set(GTDataComponents.AOE, definition.decreaseRow().toImmutable()));
        InteractionSyncHandler plusRows = new InteractionSyncHandler();
        plusRows.setOnMousePressed(data1 -> held.set(GTDataComponents.AOE, definition.increaseRow().toImmutable()));
        InteractionSyncHandler minusLayers = new InteractionSyncHandler();
        minusLayers.setOnMousePressed(data1 -> held.set(GTDataComponents.AOE, definition.decreaseLayer().toImmutable()));
        InteractionSyncHandler plusLayers = new InteractionSyncHandler();
        plusLayers.setOnMousePressed(data1 -> held.set(GTDataComponents.AOE, definition.increaseLayer().toImmutable()));

        final Icon addOverlay = GuiTextures.ADD.asIcon().size(10);
        final Icon removeOverlay = GuiTextures.REMOVE.asIcon().size(10);

        return new ModularPanel<>("aoe_config")
                .coverChildren()
                .child(Flow.row()
                        .coverChildren()
                        .childPadding(5)
                        .verticalCenter()
                        .margin(20, 5)
                        .child(Flow.column()
                                .childPadding(6)
                                .coverChildren()
                                .child(Text.lang("item.gtceu.tool.aoe.columns").asWidget())
                                .child(Text.lang("item.gtceu.tool.aoe.rows").asWidget())
                                .child(Text.lang("item.gtceu.tool.aoe.layers").asWidget()))
                        .child(Flow.column()
                                .childPadding(2)
                                .coverChildren()
                                .child(Flow.row()
                                        .coverChildren()
                                        .childPadding(2)
                                        .child(new ButtonWidget<>()
                                                .size(12)
                                                .background(GuiTextures.MC_BUTTON, removeOverlay)
                                                .hoverBackground(GuiTextures.MC_BUTTON_HOVERED, removeOverlay)
                                                .syncHandler(minusCols))
                                        .child(Text.dynamic(() -> Component.literal(Integer.toString(2 * definition.column() + 1)))
                                                .asWidget())
                                        .child(new ButtonWidget<>()
                                                .size(12)
                                                .background(GuiTextures.MC_BUTTON, addOverlay)
                                                .hoverBackground(GuiTextures.MC_BUTTON_HOVERED, addOverlay)
                                                .syncHandler(plusCols)))
                                .child(Flow.row()
                                        .coverChildren()
                                        .childPadding(2)
                                        .child(new ButtonWidget<>()
                                                .size(12)
                                                .background(GuiTextures.MC_BUTTON, removeOverlay)
                                                .hoverBackground(GuiTextures.MC_BUTTON_HOVERED, removeOverlay)
                                                .syncHandler(minusRows))
                                        .child(Text.dynamic(() -> Component.literal(Integer.toString(2 * definition.row + 1)))
                                                .asWidget())
                                        .child(new ButtonWidget<>()
                                                .size(12)
                                                .background(GuiTextures.MC_BUTTON, addOverlay)
                                                .hoverBackground(GuiTextures.MC_BUTTON_HOVERED, addOverlay)
                                                .syncHandler(plusRows)))
                                .child(Flow.row()
                                        .coverChildren()
                                        .childPadding(2)
                                        .child(new ButtonWidget<>()
                                                .size(12)
                                                .background(GuiTextures.MC_BUTTON, removeOverlay)
                                                .hoverBackground(GuiTextures.MC_BUTTON_HOVERED, removeOverlay)
                                                .syncHandler(minusLayers))
                                        .child(Text.dynamic(() -> Component.literal(Integer.toString(2 * definition.layer + 1)))
                                                .asWidget())
                                        .child(new ButtonWidget<>()
                                                .size(12)
                                                .background(GuiTextures.MC_BUTTON, addOverlay)
                                                .hoverBackground(GuiTextures.MC_BUTTON_HOVERED, addOverlay)
                                                .syncHandler(plusLayers)))));
        // spotless:on
    }

    @Override
    public ToolBehaviorType<AOEConfigUIBehavior> getType() {
        return GTToolBehaviors.AOE_CONFIG_UI;
    }
}

package com.gregtechceu.gtceu.common.item.tool.behavior;

import com.gregtechceu.gtceu.api.item.tool.aoe.AoESymmetrical;
import com.gregtechceu.gtceu.api.item.tool.behavior.IToolUIBehavior;

import com.gregtechceu.gtceu.api.mui.base.drawable.IKey;
import com.gregtechceu.gtceu.api.mui.factory.PlayerInventoryGuiData;
import com.gregtechceu.gtceu.api.mui.value.sync.InteractionSyncHandler;
import com.gregtechceu.gtceu.api.mui.value.sync.PanelSyncManager;
import com.gregtechceu.gtceu.api.mui.widgets.ButtonWidget;
import com.gregtechceu.gtceu.api.mui.widgets.TextWidget;
import com.gregtechceu.gtceu.api.mui.widgets.layout.Flow;
import com.gregtechceu.gtceu.client.mui.screen.ModularPanel;
import com.gregtechceu.gtceu.client.mui.screen.UISettings;

import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;

import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import static com.gregtechceu.gtceu.api.item.tool.ToolHelper.getBehaviorsTag;
import static com.gregtechceu.gtceu.api.item.tool.ToolHelper.getMaxAoEDefinition;

public class AOEConfigUIBehavior implements IToolUIBehavior {

    public static final AOEConfigUIBehavior INSTANCE = new AOEConfigUIBehavior();

//    @Override
//    public boolean openUI(@NotNull Player player, @NotNull InteractionHand hand) {
//        return player.isShiftKeyDown() && !getMaxAoEDefinition(player.getItemInHand(hand)).isZero();
//    }
//
//    @Override
//    public ModularUI createUI(Player player, HeldItemUIFactory.HeldItemHolder holder) {
//        var tag = getBehaviorsTag(holder.getHeld());
//        var defaultDefinition = getMaxAoEDefinition(holder.getHeld());
//        return new ModularUI(120, 80, holder, player).background(GuiTextures.BACKGROUND)
//                .widget(new LabelWidget(6, 10, "item.gtceu.tool.aoe.columns"))
//                .widget(new LabelWidget(49, 10, "item.gtceu.tool.aoe.rows"))
//                .widget(new LabelWidget(79, 10, "item.gtceu.tool.aoe.layers"))
//                .widget(new ButtonWidget(15, 24, 20, 20, new TextTexture("+"), (data) -> {
//                    AoESymmetrical.increaseColumn(tag, defaultDefinition);
//                    holder.markAsDirty();
//                }))
//                .widget(new ButtonWidget(15, 44, 20, 20, new TextTexture("-"), (data) -> {
//                    AoESymmetrical.decreaseColumn(tag, defaultDefinition);
//                    holder.markAsDirty();
//                }))
//                .widget(new ButtonWidget(50, 24, 20, 20, new TextTexture("+"), (data) -> {
//                    AoESymmetrical.increaseRow(tag, defaultDefinition);
//                    holder.markAsDirty();
//                }))
//                .widget(new ButtonWidget(50, 44, 20, 20, new TextTexture("-"), (data) -> {
//                    AoESymmetrical.decreaseRow(tag, defaultDefinition);
//                    holder.markAsDirty();
//                }))
//                .widget(new ButtonWidget(85, 24, 20, 20, new TextTexture("+"), (data) -> {
//                    AoESymmetrical.increaseLayer(tag, defaultDefinition);
//                    holder.markAsDirty();
//                }))
//                .widget(new ButtonWidget(85, 44, 20, 20, new TextTexture("-"), (data) -> {
//                    AoESymmetrical.decreaseLayer(tag, defaultDefinition);
//                    holder.markAsDirty();
//                }))
//                .widget(new LabelWidget(23, 65,
//                        () -> Integer.toString(1 +
//                                2 * AoESymmetrical.getColumn(getBehaviorsTag(holder.getHeld()), defaultDefinition))))
//                .widget(new LabelWidget(58, 65,
//                        () -> Integer.toString(
//                                1 + 2 * AoESymmetrical.getRow(getBehaviorsTag(holder.getHeld()), defaultDefinition))))
//                .widget(new LabelWidget(93, 65, () -> Integer
//                        .toString(1 + AoESymmetrical.getLayer(getBehaviorsTag(holder.getHeld()), defaultDefinition))));
//    }


    @Override
    public boolean shouldOpenUI(@NotNull Player player, @NotNull InteractionHand hand) {
        return player.isShiftKeyDown() && !getMaxAoEDefinition(player.getItemInHand(hand)).isZero();
    }

    @Override
    public ModularPanel buildUI(PlayerInventoryGuiData<?> data, PanelSyncManager syncManager, UISettings settings) {
        ItemStack held = data.getUsedItemStack();
        CompoundTag tag = getBehaviorsTag(held);
        AoESymmetrical defaultDefinition = getMaxAoEDefinition(held);
        InteractionSyncHandler minusCols = new InteractionSyncHandler();
        minusCols.setOnMousePressed(data1 -> AoESymmetrical.decreaseColumn(tag, defaultDefinition));
        InteractionSyncHandler plusCols = new InteractionSyncHandler();
        plusCols.setOnMousePressed(data1 -> AoESymmetrical.increaseColumn(tag, defaultDefinition));
        InteractionSyncHandler minusRows = new InteractionSyncHandler();
        minusRows.setOnMousePressed(data1 -> AoESymmetrical.decreaseRow(tag, defaultDefinition));
        InteractionSyncHandler plusRows = new InteractionSyncHandler();
        plusRows.setOnMousePressed(data1 -> AoESymmetrical.increaseRow(tag, defaultDefinition));
        InteractionSyncHandler minusLayers = new InteractionSyncHandler();
        minusLayers.setOnMousePressed(data1 -> AoESymmetrical.decreaseLayer(tag, defaultDefinition));
        InteractionSyncHandler plusLayers = new InteractionSyncHandler();
        plusLayers.setOnMousePressed(data1 -> AoESymmetrical.increaseLayer(tag, defaultDefinition));
        return new ModularPanel("aoe_config")
                .child(Flow.column()
                        .margin(5)
                        .childPadding(2)
                        .child(Flow.row()
                                .childPadding(2)
                                .coverChildren()
                                .child(new TextWidget<>(IKey.lang("item.gtceu.tool.aoe.columns")))
                                .child(new ButtonWidget<>()
                                        .background(GTGuiTextures.MC_BUTTON, IKey.str("-"))
                                        .hoverBackground(GTGuiTextures.MC_BUTTON_HOVERED, IKey.str("-"))
                                        .syncHandler(minusCols))
                                .child(new TextWidget<>(IKey.dynamic(() -> Component.literal(Integer.toString(
                                        AoESymmetrical.getColumn(tag, defaultDefinition)
                                )))))
                                .child(new ButtonWidget<>()
                                        .background(GTGuiTextures.MC_BUTTON, IKey.str("+"))
                                        .hoverBackground(GTGuiTextures.MC_BUTTON_HOVERED, IKey.str("+"))
                                        .syncHandler(plusCols)))
                        .child(Flow.row()
                                .childPadding(2)
                                .coverChildren()
                                .child(new TextWidget<>(IKey.lang("item.gtceu.tool.aoe.rows")))
                                .child(new ButtonWidget<>()
                                        .background(GTGuiTextures.MC_BUTTON, IKey.str("-"))
                                        .hoverBackground(GTGuiTextures.MC_BUTTON_HOVERED, IKey.str("-"))
                                        .syncHandler(minusRows))
                                .child(new TextWidget<>(IKey.dynamic(() -> Component.literal(Integer.toString(
                                        AoESymmetrical.getRow(tag, defaultDefinition)
                                )))))
                                .child(new ButtonWidget<>()
                                        .background(GTGuiTextures.MC_BUTTON, IKey.str("+"))
                                        .hoverBackground(GTGuiTextures.MC_BUTTON_HOVERED, IKey.str("+"))
                                        .syncHandler(plusRows)))
                        .child(Flow.row()
                                .childPadding(2)
                                .coverChildren()
                                .child(new TextWidget<>(IKey.lang("item.gtceu.tool.aoe.layers")))
                                .child(new ButtonWidget<>()
                                        .background(GTGuiTextures.MC_BUTTON, IKey.str("-"))
                                        .hoverBackground(GTGuiTextures.MC_BUTTON_HOVERED, IKey.str("-"))
                                        .syncHandler(minusLayers))
                                .child(new TextWidget<>(IKey.dynamic(() -> Component.literal(Integer.toString(
                                        AoESymmetrical.getLayer(tag, defaultDefinition)
                                )))))
                                .child(new ButtonWidget<>()
                                        .background(GTGuiTextures.MC_BUTTON, IKey.str("+"))
                                        .hoverBackground(GTGuiTextures.MC_BUTTON_HOVERED, IKey.str("+"))
                                        .syncHandler(plusLayers))));
    }
}

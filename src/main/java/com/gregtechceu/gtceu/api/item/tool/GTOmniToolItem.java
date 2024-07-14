package com.gregtechceu.gtceu.api.item.tool;

import appeng.util.Platform;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.item.IGTTool;
import com.gregtechceu.gtceu.api.item.tool.aoe.AoESymmetrical;
import com.gregtechceu.gtceu.client.renderer.item.ToolItemRenderer;
import com.gregtechceu.gtceu.common.item.tool.behavior.ToolModeSwitchBehavior;
import com.gregtechceu.gtceu.utils.input.IKeyPressedListener;
import com.gregtechceu.gtceu.utils.input.KeyBind;
import com.lowdragmc.lowdraglib.LDLib;
import com.lowdragmc.lowdraglib.gui.factory.HeldItemUIFactory;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import lombok.Getter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

import static com.gregtechceu.gtceu.api.item.tool.ToolHelper.getBehaviorsTag;
import static com.gregtechceu.gtceu.api.item.tool.ToolHelper.getMaxAoEDefinition;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class GTOmniToolItem extends GTToolItem implements IGTTool, IKeyPressedListener {
    @Getter
    private final GTToolType toolType;
    @Getter
    private final Material material;
    @Getter
    private final int electricTier;
    @Getter
    private final IGTToolDefinition omniToolStats;
    private static boolean onInit = false;
    protected GTOmniToolItem(GTToolType toolType, MaterialToolTier tier, Material material, IGTToolDefinition toolStats, Properties properties) {
        super(toolType, tier, material ,toolStats, properties);
        this.material = material;
        this.toolType = toolType;
        this.electricTier = toolType.electricTier;
        this.omniToolStats = toolStats;
        if (Platform.isClient()){
            ToolItemRenderer.create(this,toolType);
        }
        definition$init();
        if(LDLib.isClient()){
            ItemProperties.register(this, GTCEu.id("omnitool"), (itemStack, c, l, i) -> {
                if (ToolHelper.hasBehaviorsTag(itemStack)) {
                    CompoundTag behaviors = ToolHelper.getBehaviorsTag(itemStack);
                    return (float) behaviors.getByte("OmniToolMode");
                }
                return 0;
            });
        }
    }
    public static GTOmniToolItem create(GTToolType toolType, MaterialToolTier tier, Material material, IGTToolDefinition toolStats, Item.Properties properties){
        return new GTOmniToolItem(toolType, tier, material, toolStats, properties);
    }
    @Override
    public void onKeyPressed(ServerPlayer player, KeyBind keyPressed) {
        if (keyPressed.isPressed()){
            var stackHand = player.getMainHandItem();
            CompoundTag data = stackHand.getOrCreateTag();
            var tagCompound = getBehaviorsTag(stackHand);
            tagCompound.putByte("OmniToolMode", (byte) ((tagCompound.getByte("OmniToolMode") + 1) % ToolModeSwitchBehavior.OmniModeType.values().length));
            player.displayClientMessage(Component.translatable("metaitem.machine_configuration.mode", ToolModeSwitchBehavior.OmniModeType.values()[tagCompound.getByte("OmniToolMode")].getName()), true);
            setLastCraftingSoundTime(stackHand);
        }
    }
    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if (!onInit){
            //Registers the listener then stops after the first tick.
            KeyBind.QUARK_TOOL_MODE_SWITCH.registerListener((ServerPlayer) entity,this);
            onInit = true;
        }

    }

    @Override
    public ItemStack getDefaultInstance() {
        return get();
    }
    @Override
    public @Nullable ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return definition$initCapabilities(stack, nbt);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        return super.use(level, player, usedHand);
    }
    @Override
    public ModularUI createUI(Player entityPlayer, HeldItemUIFactory.HeldItemHolder holder) {
        CompoundTag tag = getBehaviorsTag(holder.getHeld());
        AoESymmetrical defaultDefinition = getMaxAoEDefinition(holder.getHeld());
        return new ModularUI(140, 120, holder, entityPlayer).background(GuiTextures.BACKGROUND)
            .widget(new LabelWidget(6, 10, "item.gtceu.tool.aoe.columns"))
            .widget(new LabelWidget(49, 10, "item.gtceu.tool.aoe.rows"))
            .widget(new LabelWidget(79, 10, "item.gtceu.tool.aoe.layers"))
            .widget(new LabelWidget(6, 85, "item.gtceu.tool.treefeller.mode"))
            .widget(new LabelWidget(6, 105, "item.gtceu.tool.scythe.mode"))
            .widget(new ButtonWidget(15, 24, 20, 20, new TextTexture("+"), (data) -> {
                AoESymmetrical.increaseColumn(tag, defaultDefinition);
                holder.markAsDirty();
            }))
            .widget(new ButtonWidget(15, 44, 20, 20, new TextTexture("-"), (data) -> {
                AoESymmetrical.decreaseColumn(tag, defaultDefinition);
                holder.markAsDirty();
            }))
            .widget(new ButtonWidget(50, 24, 20, 20, new TextTexture("+"), (data) -> {
                AoESymmetrical.increaseRow(tag, defaultDefinition);
                holder.markAsDirty();
            }))
            .widget(new ButtonWidget(50, 44, 20, 20, new TextTexture("-"), (data) -> {
                AoESymmetrical.decreaseRow(tag, defaultDefinition);
                holder.markAsDirty();
            }))
            .widget(new ButtonWidget(85, 24, 20, 20, new TextTexture("+"), (data) -> {
                AoESymmetrical.increaseLayer(tag, defaultDefinition);
                holder.markAsDirty();
            }))
            .widget(new ButtonWidget(85, 44, 20, 20, new TextTexture("-"), (data) -> {
                AoESymmetrical.decreaseLayer(tag, defaultDefinition);
                holder.markAsDirty();
            }))
            .widget(new LabelWidget(23, 65, () ->
                Integer.toString(1 + 2 * AoESymmetrical.getColumn(getBehaviorsTag(holder.getHeld()), defaultDefinition))))
            .widget(new LabelWidget(58, 65, () ->
                Integer.toString(1 + 2 * AoESymmetrical.getRow(getBehaviorsTag(holder.getHeld()), defaultDefinition))))
            .widget(new LabelWidget(93, 65, () ->
                Integer.toString(1 + AoESymmetrical.getLayer(getBehaviorsTag(holder.getHeld()), defaultDefinition))));
    }


}

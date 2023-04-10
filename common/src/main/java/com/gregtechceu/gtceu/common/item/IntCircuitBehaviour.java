package com.gregtechceu.gtceu.common.item;

import com.gregtechceu.gtceu.api.item.component.IItemUIFactory;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.item.component.IAddInformation;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.lowdragmc.lowdraglib.gui.factory.HeldItemUIFactory;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author KilaBash
 * @date 2023/2/23
 * @implNote IntCircuitBehaviour
 */
public class IntCircuitBehaviour implements IItemUIFactory, IAddInformation {
    public static final int CIRCUIT_MAX = 32;

    public static ItemStack stack(int configuration) {
        var stack = GTItems.INTEGRATED_CIRCUIT.asStack();
        setCircuitConfiguration(stack, configuration);
        return stack;
    }

    public static void setCircuitConfiguration(ItemStack itemStack, int configuration) {
        if (configuration < 0 || configuration > CIRCUIT_MAX)
            throw new IllegalArgumentException("Given configuration number is out of range!");
        var tagCompound = itemStack.getTag();
        if (tagCompound == null) {
            tagCompound = new CompoundTag();
            itemStack.setTag(tagCompound);
        }
        tagCompound.putInt("Configuration", configuration);
    }

    public static int getCircuitConfiguration(ItemStack itemStack) {
        if (!isIntegratedCircuit(itemStack)) return 0;
        var tagCompound = itemStack.getTag();
        if (tagCompound != null) {
            return tagCompound.getInt("Configuration");
        }
        return 0;
    }

    public static boolean isIntegratedCircuit(ItemStack itemStack) {
        boolean isCircuit = GTItems.INTEGRATED_CIRCUIT.isIn(itemStack);
        if (isCircuit && !itemStack.hasTag()) {
            var compound = new CompoundTag();
            compound.putInt("Configuration", 0);
            itemStack.setTag(compound);
        }
        return isCircuit;
    }

    public static void adjustConfiguration(HeldItemUIFactory.HeldItemHolder holder, int amount) {
        adjustConfiguration(holder.getHeld(), amount);
        holder.markAsDirty();
    }

    public static void adjustConfiguration(ItemStack stack, int amount) {
        if (!isIntegratedCircuit(stack)) return;
        int configuration = getCircuitConfiguration(stack);
        configuration += amount;
        configuration = Mth.clamp(configuration, 0, CIRCUIT_MAX);
        setCircuitConfiguration(stack, configuration);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        int configuration = getCircuitConfiguration(stack);
        tooltipComponents.add(Component.translatable("metaitem.int_circuit.configuration", configuration));
    }

//    saving this one for later use
//    @Override
//    public ModularUI createUI(HeldItemUIFactory.HeldItemHolder holder, Player entityPlayer) {
//        var modular = new ModularUI(176, 60, holder, entityPlayer)
//                .widget(new LabelWidget(9, 8, "metaitem.circuit.integrated.gui"))
//                .widget(new LabelWidget(82, 30, () -> Integer.toString(getCircuitConfiguration(holder.getHeld()))))
//                .widget(new ButtonWidget(15, 24, 20, 20, new GuiTextureGroup(GuiTextures.VANILLA_BUTTON, new TextTexture("-5")), data -> adjustConfiguration(holder, -5)))
//                .widget(new ButtonWidget(50, 24, 20, 20, new GuiTextureGroup(GuiTextures.VANILLA_BUTTON, new TextTexture("-1")), data -> adjustConfiguration(holder, -1)))
//                .widget(new ButtonWidget(104, 24, 20, 20, new GuiTextureGroup(GuiTextures.VANILLA_BUTTON, new TextTexture("+1")), data -> adjustConfiguration(holder, +1)))
//                .widget(new ButtonWidget(141, 24, 20, 20, new GuiTextureGroup(GuiTextures.VANILLA_BUTTON, new TextTexture("+5")), data -> adjustConfiguration(holder, +5)));
//        modular.mainGroup.setBackground(GuiTextures.BACKGROUND);
//        return modular;
//    }

    @Override
    public ModularUI createUI(HeldItemUIFactory.HeldItemHolder holder, Player entityPlayer) {
        int configuration = getCircuitConfiguration(holder);
        var modular = new ModularUI(176, 200, holder, entityPlayer)
                .widget(new LabelWidget(9, 8, "metaitem.circuit.integrated.gui"))
                .widget(new ButtonWidget(15, 24, 20, 20, new GuiTextureGroup(GuiTextures.VANILLA_BUTTON, new TextTexture("0")), data -> adjustConfiguration(holder, (configuration - 0) * -1)))
                .widget(new ButtonWidget(35, 24, 20, 20, new GuiTextureGroup(GuiTextures.VANILLA_BUTTON, new TextTexture("1")), data -> adjustConfiguration(holder, (configuration - 1) * -1)))
                .widget(new ButtonWidget(55, 24, 20, 20, new GuiTextureGroup(GuiTextures.VANILLA_BUTTON, new TextTexture("2")), data -> adjustConfiguration(holder, (configuration - 2) * -1)))
                .widget(new ButtonWidget(75, 24, 20, 20, new GuiTextureGroup(GuiTextures.VANILLA_BUTTON, new TextTexture("3")), data -> adjustConfiguration(holder, (configuration - 3) * -1)))
                .widget(new ButtonWidget(95, 24, 20, 20, new GuiTextureGroup(GuiTextures.VANILLA_BUTTON, new TextTexture("4")), data -> adjustConfiguration(holder, (configuration - 4) * -1)))
                .widget(new ButtonWidget(115, 24, 20, 20, new GuiTextureGroup(GuiTextures.VANILLA_BUTTON, new TextTexture("5")), data -> adjustConfiguration(holder, (configuration - 5) * -1)))
                .widget(new ButtonWidget(15, 44, 20, 20, new GuiTextureGroup(GuiTextures.VANILLA_BUTTON, new TextTexture("6")), data -> adjustConfiguration(holder, (configuration - 6) * -1)))
                .widget(new ButtonWidget(35, 24, 20, 20, new GuiTextureGroup(GuiTextures.VANILLA_BUTTON, new TextTexture("7")), data -> adjustConfiguration(holder, (configuration - 7) * -1)))
        modular.mainGroup.setBackground(GuiTextures.BACKGROUND);
        return modular;
    }
}

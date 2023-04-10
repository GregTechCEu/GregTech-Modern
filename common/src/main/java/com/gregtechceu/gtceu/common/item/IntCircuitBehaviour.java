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

    public static void setCircuitConfiguration(HeldItemUIFactory.HeldItemHolder holder, int configuration) {
        setCircuitConfiguration(holder.getHeld(), configuration);
        holder.markAsDirty();
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
        var modular = new ModularUI(176, 200, holder, entityPlayer)
                .widget(new LabelWidget(9, 8, "metaitem.circuit.integrated.gui"))
                .widget(new ButtonWidget(15, 24, 20, 20, new GuiTextureGroup(GuiTextures.VANILLA_BUTTON, new TextTexture("0")), data -> setCircuitConfiguration(holder, 0)))
                .widget(new ButtonWidget(35, 24, 20, 20, new GuiTextureGroup(GuiTextures.VANILLA_BUTTON, new TextTexture("1")), data -> setCircuitConfiguration(holder, 1)))
                .widget(new ButtonWidget(55, 24, 20, 20, new GuiTextureGroup(GuiTextures.VANILLA_BUTTON, new TextTexture("2")), data -> setCircuitConfiguration(holder, 2)))
                .widget(new ButtonWidget(75, 24, 20, 20, new GuiTextureGroup(GuiTextures.VANILLA_BUTTON, new TextTexture("3")), data -> setCircuitConfiguration(holder, 3)))
                .widget(new ButtonWidget(95, 24, 20, 20, new GuiTextureGroup(GuiTextures.VANILLA_BUTTON, new TextTexture("4")), data -> setCircuitConfiguration(holder, 4)))
                .widget(new ButtonWidget(115, 24, 20, 20, new GuiTextureGroup(GuiTextures.VANILLA_BUTTON, new TextTexture("5")), data -> setCircuitConfiguration(holder, 5)))
                .widget(new ButtonWidget(15, 44, 20, 20, new GuiTextureGroup(GuiTextures.VANILLA_BUTTON, new TextTexture("6")), data -> setCircuitConfiguration(holder, 6)))
                .widget(new ButtonWidget(35, 44, 20, 20, new GuiTextureGroup(GuiTextures.VANILLA_BUTTON, new TextTexture("7")), data -> setCircuitConfiguration(holder, 7)))
                .widget(new ButtonWidget(55, 44, 20, 20, new GuiTextureGroup(GuiTextures.VANILLA_BUTTON, new TextTexture("8")), data -> setCircuitConfiguration(holder, 8)))
                .widget(new ButtonWidget(75, 44, 20, 20, new GuiTextureGroup(GuiTextures.VANILLA_BUTTON, new TextTexture("9")), data -> setCircuitConfiguration(holder, 9)))
                .widget(new ButtonWidget(95, 44, 20, 20, new GuiTextureGroup(GuiTextures.VANILLA_BUTTON, new TextTexture("10")), data -> setCircuitConfiguration(holder, 10)))
                .widget(new ButtonWidget(115, 44, 20, 20, new GuiTextureGroup(GuiTextures.VANILLA_BUTTON, new TextTexture("11")), data -> setCircuitConfiguration(holder, 11)))
                .widget(new ButtonWidget(15, 64, 20, 20, new GuiTextureGroup(GuiTextures.VANILLA_BUTTON, new TextTexture("12")), data -> setCircuitConfiguration(holder, 12)))
                .widget(new ButtonWidget(35, 64, 20, 20, new GuiTextureGroup(GuiTextures.VANILLA_BUTTON, new TextTexture("13")), data -> setCircuitConfiguration(holder, 13)))
                .widget(new ButtonWidget(55, 64, 20, 20, new GuiTextureGroup(GuiTextures.VANILLA_BUTTON, new TextTexture("14")), data -> setCircuitConfiguration(holder, 14)))
                .widget(new ButtonWidget(75, 64, 20, 20, new GuiTextureGroup(GuiTextures.VANILLA_BUTTON, new TextTexture("15")), data -> setCircuitConfiguration(holder, 15)))
                .widget(new ButtonWidget(95, 64, 20, 20, new GuiTextureGroup(GuiTextures.VANILLA_BUTTON, new TextTexture("16")), data -> setCircuitConfiguration(holder, 16)))
                .widget(new ButtonWidget(115, 64, 20, 20, new GuiTextureGroup(GuiTextures.VANILLA_BUTTON, new TextTexture("17")), data -> setCircuitConfiguration(holder, 17)))
                .widget(new ButtonWidget(15, 84, 20, 20, new GuiTextureGroup(GuiTextures.VANILLA_BUTTON, new TextTexture("18")), data -> setCircuitConfiguration(holder, 18)))
                .widget(new ButtonWidget(35, 84, 20, 20, new GuiTextureGroup(GuiTextures.VANILLA_BUTTON, new TextTexture("19")), data -> setCircuitConfiguration(holder, 19)))
                .widget(new ButtonWidget(55, 84, 20, 20, new GuiTextureGroup(GuiTextures.VANILLA_BUTTON, new TextTexture("20")), data -> setCircuitConfiguration(holder, 20)))
                .widget(new ButtonWidget(75, 84, 20, 20, new GuiTextureGroup(GuiTextures.VANILLA_BUTTON, new TextTexture("21")), data -> setCircuitConfiguration(holder, 21)))
                .widget(new ButtonWidget(95, 84, 20, 20, new GuiTextureGroup(GuiTextures.VANILLA_BUTTON, new TextTexture("22")), data -> setCircuitConfiguration(holder, 22)))
                .widget(new ButtonWidget(115, 84, 20, 20, new GuiTextureGroup(GuiTextures.VANILLA_BUTTON, new TextTexture("23")), data -> setCircuitConfiguration(holder, 23)))
                .widget(new ButtonWidget(15, 104, 20, 20, new GuiTextureGroup(GuiTextures.VANILLA_BUTTON, new TextTexture("24")), data -> setCircuitConfiguration(holder, 24))):

        modular.mainGroup.setBackground(GuiTextures.BACKGROUND);
        return modular;
    }
}

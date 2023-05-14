package com.gregtechceu.gtceu.common.item;

import com.gregtechceu.gtceu.api.item.component.IItemUIFactory;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.item.component.IAddInformation;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtlib.gui.factory.HeldItemUIFactory;
import com.gregtechceu.gtlib.gui.modular.ModularUI;
import com.gregtechceu.gtlib.gui.texture.GuiTextureGroup;
import com.gregtechceu.gtlib.gui.texture.TextTexture;
import com.gregtechceu.gtlib.gui.widget.ButtonWidget;
import com.gregtechceu.gtlib.gui.widget.LabelWidget;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
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

    // deprecated, not needed (for now)
    @Deprecated
    public static void adjustConfiguration(HeldItemUIFactory.HeldItemHolder holder, int amount) {
        adjustConfiguration(holder.getHeld(), amount);
        holder.markAsDirty();
    }

    // deprecated, not needed (for now)
    @Deprecated
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

    @Override
    public ModularUI createUI(HeldItemUIFactory.HeldItemHolder holder, Player entityPlayer) {
        var modular = new ModularUI(178, 133, holder, entityPlayer)
                .widget(new LabelWidget(9, 8, "metaitem.circuit.integrated.gui"));
        int idx = 0;
        //TODO: fix the motherfucking circuit textures
        for(int x = 0; x <= 3; x++) {
            for(int y = 0; y <= 7; y++) {
                int finalIdx = idx;
                modular.widget(new ButtonWidget(10 + (18 * y), 24 + (18 * x), 18, 18, new GuiTextureGroup(GuiTextures.SLOT, new ResourceTexture(new ResourceLocation("gtceu:textures/item/circuit.integrated/" + (idx + 1) + ".png"), 0.2f, 0.2f, 1.2f, 1.2f)), data -> setCircuitConfiguration(holder, finalIdx)));
                idx++;
            }
        }
        modular.widget(new ButtonWidget(10, 96, 18, 18, new GuiTextureGroup(GuiTextures.SLOT, new ResourceTexture(new ResourceLocation("gtceu:textures/item/circuit.integrated/33.png"), 0.0f, 0.0f, 1.0f, 1.0f)), data -> setCircuitConfiguration(holder, 32)));
        modular.mainGroup.setBackground(GuiTextures.BACKGROUND);
        return modular;
    }
}

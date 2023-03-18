package com.gregtechceu.gtceu.api.item.tool;

import com.gregtechceu.gtceu.common.libs.GTItems;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.item.GTToolItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

/**
 * @author KilaBash
 * @date 2023/2/23
 * @implNote ToolHelper
 */
public class ToolHelper {

    public static ItemStack get(GTToolType toolType, Material material) {
        if (material.hasProperty(PropertyKey.TOOL)) {
            var entry = GTItems.TOOL_ITEMS.get(material.getToolTier(), toolType);
            if (entry != null) {
                return entry.asStack();
            }
        }
        return ItemStack.EMPTY;
    }

    public static boolean is(ItemStack stack, GTToolType toolType) {
        if (stack.getItem() instanceof GTToolItem item) {
            return item.getToolType() == toolType;
        }
        return false;
    }

    public static boolean canUse(ItemStack stack) {
        return stack.getDamageValue() < stack.getMaxDamage();
    }

    public static void damageItem(@Nonnull ItemStack stack, RandomSource random, @Nullable ServerPlayer user) {
        if (canUse(stack)) {
            stack.hurt(1, random, user);
        } else {
            stack.shrink(1);
        }
    }

    public static void playToolSound(GTToolType toolType, ServerPlayer player) {
        if (toolType.soundEntry != null) {
            toolType.soundEntry.playOnServer(player.getLevel(), player.blockPosition());
        }
    }

}

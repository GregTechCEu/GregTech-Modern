package com.gregtechceu.gtceu.utils;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.client.util.TooltipHelper;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;

import com.demonwav.mcdev.annotations.Translatable;

public class ComponentUtil {

    /**
     * Formats an EUt value as {@code (amps) A @ (voltage)}
     * 
     * @param color If color formatting should also be applied
     */
    public static MutableComponent formattedEUt(float amps, long voltage, boolean color) {
        var tier = GTUtil.getTierByVoltage(voltage);

        MutableComponent amperage = ComponentUtil.prepend("common.gtceu.amperage",
                FormattingUtil.formatNumber2Places(amps));
        if (color) amperage = amperage.withStyle(ChatFormatting.RED);

        MutableComponent text = amperage.append(color ? Component.literal(" @ ").withStyle(ChatFormatting.GREEN) :
                Component.literal(" @ "));

        if (tier < GTValues.TIER_COUNT) {
            MutableComponent voltageComponent = Component.literal(GTValues.VNF[tier]);
            if (color) voltageComponent = voltageComponent.withStyle(style -> style.withColor(GTValues.VC[tier]));
            text = voltageComponent.append(voltageComponent);
        } else {
            int oc = Mth.clamp(tier - GTValues.TIER_COUNT - 1, 0, GTValues.TIER_COUNT);
            MutableComponent maxComponent = Component.literal("MAX");
            if (color) maxComponent = maxComponent.withStyle(style -> style.withColor(TooltipHelper.rainbowColor(oc)));

            MutableComponent countComponent = Component.literal("+").append(String.valueOf(oc));
            if (color) countComponent = countComponent.withStyle(style -> style.withColor(GTValues.VC[oc]));
            text.append(maxComponent).append(countComponent);
        }

        return text;
    }

    /**
     * Adds a given string to the beginning and end of a component.
     * 
     * @param component The component to append/preprend to.
     * @param str       The string to append/prepend.
     */
    public static MutableComponent wrap(Component component, String str) {
        return Component.literal(str).append(component).append(str);
    }

    /**
     * Adds a given string to the beginning and end of a component.
     * 
     * @param component The component to append/preprend to.
     * @param prefix    The string to preprend.
     * @param postfix   The string to append.
     */
    public static MutableComponent wrap(Component component, String prefix, String postfix) {
        return Component.literal(prefix).append(component).append(postfix);
    }

    /**
     * Preprends a value to the start of a component.<br>
     * e.g. {@code prepend(Component.translatable("common.gtceu.mb_per_tick"), 100)} returns {@code "100 mB/t"}
     */
    public static MutableComponent prepend(Component component, Object val) {
        return Component.literal(val.toString() + " ").append(component);
    }

    /**
     * Preprends a value to the start of a component.<br>
     * e.g. {@code prepend("common.gtceu.mb_per_tick", 100)} returns {@code "100 mB/t"}
     */
    public static MutableComponent prepend(@Translatable String langKey, Object val) {
        return Component.literal(val.toString() + " ").append(Component.translatable(langKey));
    }
}

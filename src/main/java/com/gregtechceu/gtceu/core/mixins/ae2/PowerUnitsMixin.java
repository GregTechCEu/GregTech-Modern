package com.gregtechceu.gtceu.core.mixins.ae2;

import appeng.api.config.PowerUnits;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Arrays;

// Reasoning: The Power Unit enum is used to store
// the different power units and their lang/conversion
// factors in AE2. We want to add EU to this.
@Debug(export = true)
@Mixin(value = PowerUnits.class, remap = false)
public class PowerUnitsMixin {

    @Unique
    private static PowerUnits gtceu$EU_UNIT;

    @Invoker(value = "<init>")
    private static PowerUnits gtceu$invokeConstructor(String internalName, int ordinal, String unlocalizedName,
                                                      String textRepresentation) {
        throw new AssertionError();
    }

    @ModifyReturnValue(method = "values", at = @At("RETURN"))
    private static PowerUnits[] gtceu$addEUToValues(PowerUnits[] original) {
        if (gtceu$EU_UNIT == null) {
            gtceu$EU_UNIT = gtceu$invokeConstructor("EU", original.length, "gui.ae2.units.eu", "EU");
        }

        for (PowerUnits unit : original) {
            if (unit == gtceu$EU_UNIT) {
                return original;
            }
        }

        PowerUnits[] newArray = Arrays.copyOf(original, original.length + 1);
        newArray[original.length] = gtceu$EU_UNIT;

        return newArray;
    }
}

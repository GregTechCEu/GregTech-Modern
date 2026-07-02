package com.gregtechceu.gtceu.core.mixins.dev.datagen;

import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.settings.KeyMappingLookup;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.platform.InputConstants;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.EnumMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This mixin is only loaded in datagen contexts
 */
@Mixin(value = KeyMappingLookup.class, remap = false)
public class FixParallelKeyMappingRegistrationMixin {

    /**
     * @author screret
     * @reason Fix keybind registration in datagen.<br>
     *         <p>
     *         The map isn't thread safe and some mods register their keybinds in places they probably shouldn't.<br>
     *         (IDK why it only crashes in datagen, though)
     */
    @WrapOperation(method = "<clinit>",
                   at = @At(value = "INVOKE",
                            target = "Ljava/util/EnumMap;put(Ljava/lang/Enum;Ljava/lang/Object;)Ljava/lang/Object;"))
    private static <K extends Enum<K>, V> V gtceu$makeKeyMappingMapsConcurrent(EnumMap<K, V> instance, K key, V value,
                                                                               Operation<V> original) {
        // replace `value`, which is a non-threadsafe HashMap, with a ConcurrentHashMap
        return original.call(instance, key, new ConcurrentHashMap<InputConstants.Key, List<KeyMapping>>());
    }
}

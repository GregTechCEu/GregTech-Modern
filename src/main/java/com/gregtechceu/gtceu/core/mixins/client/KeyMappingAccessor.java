package com.gregtechceu.gtceu.core.mixins.client;

import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.settings.KeyMappingLookup;
import org.apache.commons.lang3.NotImplementedException;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(KeyMapping.class)
public interface KeyMappingAccessor {

    @Accessor
    static KeyMappingLookup getMAP() {
        throw new NotImplementedException("KeyBindingMap getMAP()");
    }
}

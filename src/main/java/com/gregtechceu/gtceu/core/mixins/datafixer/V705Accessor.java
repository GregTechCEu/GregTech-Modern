package com.gregtechceu.gtceu.core.mixins.datafixer;

import com.mojang.datafixers.types.templates.Hook;
import net.minecraft.util.datafix.schemas.V705;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(V705.class)
public interface V705Accessor {

    @Accessor("ADD_NAMES")
    static Hook.HookFunction gtceu$getAddNames() {
        throw new AssertionError("Mixin didn't apply");
    }
}

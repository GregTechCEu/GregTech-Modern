package brachy.modularui.core.mixins.common;

import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.wrapper.CombinedInvWrapper;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = CombinedInvWrapper.class, remap = false)
public interface CombinedInvWrapperAccessor {

    @Accessor
    IItemHandlerModifiable[] getItemHandler();
}

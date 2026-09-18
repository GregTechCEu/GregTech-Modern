package brachy.modularui.core.mixins.common;

import brachy.modularui.drawable.text.ModularComponent;
import brachy.modularui.drawable.text.ToModularComponent;

import net.minecraft.network.chat.Component;

import org.spongepowered.asm.mixin.Mixin;

@Mixin(Component.class)
public interface ComponentMixin extends ToModularComponent {

    @Override
    default ModularComponent asModular() {
        return ModularComponent.of((Component) this);
    }
}

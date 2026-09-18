package brachy.modularui.core.mixins.common;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Accessor interface for accessing protected methods from {@link Slot}.
 */
@Mixin(Slot.class)
public interface SlotAccessor {

    @Accessor("x")
    @Mutable
    void setX(int x);

    @Accessor("y")
    @Mutable
    void setY(int y);

    @Accessor("container")
    @Mutable
    void setContainer(Container container);
}

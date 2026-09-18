package brachy.modularui.core.mixins.client;

import net.minecraft.client.StringSplitter;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(StringSplitter.class)
public interface StringSplitterAccessor {

    @Accessor
    StringSplitter.WidthProvider getWidthProvider();
}

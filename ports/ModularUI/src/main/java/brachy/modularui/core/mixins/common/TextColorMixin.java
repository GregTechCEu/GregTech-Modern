package brachy.modularui.core.mixins.common;

import net.minecraft.network.chat.TextColor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(TextColor.class)
public class TextColorMixin {

    /**
     * @reason Minecraft uses Integer.parseInt which fails when alpha is specified because of signed vs. unsigned
     */
    @Redirect(method = "parseColor", at = @At(value = "INVOKE", target = "Ljava/lang/Integer;parseInt(Ljava/lang/String;I)I"))
    private static int fixDecode(String s, int radix) {
        return (int) Long.parseLong(s, radix);
    }
}

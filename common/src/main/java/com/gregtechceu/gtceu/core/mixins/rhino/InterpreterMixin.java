package com.gregtechceu.gtceu.core.mixins.rhino;

import dev.latvian.mods.rhino.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Interpreter.class, remap = false)
public class InterpreterMixin {

    /**
     * {@code @Coerce}'d because the InterpretedFunction class is package-private. It "works".
     * What a bodge for an issue that should never even happen, looking at the code.
     * God I hate Rhino.
     */
    @SuppressWarnings("InvalidInjectorMethodSignature")
    @Inject(method = "interpret", at = @At(value = "INVOKE", target = "Ldev/latvian/mods/rhino/Kit;codeBug()Ljava/lang/RuntimeException;"), remap = false, cancellable = true)
    private static void gtceu$doNotCrashPls(@Coerce NativeFunction ifun, Context cx, Scriptable scope, Scriptable thisObj, Object[] args, CallbackInfoReturnable<Object> cir) {
        cir.setReturnValue(ScriptRuntime.doTopCall(cx, scope, ifun, thisObj, args, cx.isStrictMode()));
    }
}

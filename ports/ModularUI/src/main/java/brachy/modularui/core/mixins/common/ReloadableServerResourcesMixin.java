package brachy.modularui.core.mixins.common;

import brachy.modularui.utils.RegistryAccessContainer;

import net.minecraft.commands.Commands;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.tags.TagManager;
import net.minecraft.world.flag.FeatureFlagSet;
import net.neoforged.neoforge.common.conditions.ICondition;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ReloadableServerResources.class)
public abstract class ReloadableServerResourcesMixin {

    @Shadow
    @Final
    private TagManager tagManager;

    @Shadow
    public abstract ICondition.IContext getConditionContext();

    @Inject(method = "<init>", at = @At("TAIL"))
    private void mui$hookResourceLoad(RegistryAccess.Frozen registryAccess, FeatureFlagSet enabledFeatures,
                                      Commands.CommandSelection commandSelection, int functionCompilationLevel,
                                      CallbackInfo ci) {
        RegistryAccessContainer.update(registryAccess, getConditionContext());
    }
}

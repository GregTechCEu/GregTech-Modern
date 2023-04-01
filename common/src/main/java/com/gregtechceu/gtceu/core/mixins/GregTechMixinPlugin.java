package com.gregtechceu.gtceu.core.mixins;

import com.lowdragmc.lowdraglib.core.mixins.MixinPluginShared;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

/**
 * @author KilaBash
 * @date 2023/2/11
 * @implNote LDLibMixinPlugin
 */
public class GregTechMixinPlugin implements IMixinConfigPlugin {
    @Override
    public void onLoad(String mixinPackage) {

    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (mixinClassName.contains("com.gregtechceu.gtceu.core.mixins.kjs")) {
            return MixinPluginShared.isClassFound("dev.latvian.mods.kubejs.KubeJSPlugin");
        } else if (mixinClassName.contains("com.gregtechceu.gtceu.core.mixins.create")) {
            return MixinPluginShared.isClassFound("com.simibubi.create.compat.Mods");
        }
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }
}

package com.gregtechceu.gtceu.core.mixins;

import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.LoadingModList;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GTMixinPlugin implements IMixinConfigPlugin {

    @Override
    public void onLoad(String mixinPackage) {}

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    private static final String MIXIN_PACKAGE = "com.gregtechceu.gtceu.core.mixins.";
    private static final Map<String, String> MOD_COMPAT_MIXINS = new HashMap<>();

    private static final String DEV_PACKAGE = "dev.";
    private static final String DATAGEN_PACKAGE = "datagen.";

    static {
        MOD_COMPAT_MIXINS.put("roughlyenoughitems", "rei.");
        addModCompatMixin("emi");
        addModCompatMixin("jei");
        addModCompatMixin("top");
        addModCompatMixin("ftbchunks");
        addModCompatMixin("xaerominimap");
        addModCompatMixin("xaeroworldmap");
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (!mixinClassName.startsWith(MIXIN_PACKAGE)) {
            // skip checking mixins that aren't in our package
            // this should never happen, but better safe than sorry
            return true;
        }
        mixinClassName = mixinClassName.substring(MIXIN_PACKAGE.length());

        if (mixinClassName.startsWith(DEV_PACKAGE)) {
            if (FMLLoader.isProduction()) {
                // don't load dev-only mixins in prod
                return false;
            }
            mixinClassName = mixinClassName.substring(DEV_PACKAGE.length());
            if (mixinClassName.startsWith(DATAGEN_PACKAGE)) {
                // only load datagen mixins in datagen
                return FMLLoader.getLaunchHandler().isData();
            }
            return true;
        }
        for (var compatMod : MOD_COMPAT_MIXINS.entrySet()) {
            if (mixinClassName.startsWith(compatMod.getValue())) {
                return isModLoaded(compatMod.getKey());
            }
        }
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

    private static void addModCompatMixin(String modId) {
        MOD_COMPAT_MIXINS.put(modId, modId + ".");
    }

    private static boolean isModLoaded(String modId) {
        if (ModList.get() == null) {
            return LoadingModList.get().getModFileById(modId) != null;
        }
        return ModList.get().isLoaded(modId);
    }
}

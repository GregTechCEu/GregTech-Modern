package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLLoader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GTMixinPlugin implements IMixinConfigPlugin {

    public static final Logger LOGGER = LogManager.getLogger("GregTechCEu");

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

    private static final String CLIENT_PACKAGE = "client.";
    private static final String BLOOM_PACKAGE = "bloom.",
            BLOOM_NORMAL_PACKAGE = "normal.",
            BLOOM_SAFEMODE_PACKAGE = "safemode.";

    static {
        MOD_COMPAT_MIXINS.put("roughlyenoughitems", "rei.");
        addModCompatMixin("emi");
        addModCompatMixin("jei");
        addModCompatMixin("top");
        addModCompatMixin("embeddium");
        // MOD_COMPAT_MIXINS.put("sodium", "embeddium.");
        addModCompatMixin("oculus");
        // MOD_COMPAT_MIXINS.put("iris", "oculus.");
        addModCompatMixin("ftbchunks");
        addModCompatMixin("xaerominimap");
        addModCompatMixin("xaeroworldmap");
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (!FMLLoader.getLoadingModList().getErrors().isEmpty()) {
            return false;
        }

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
        if (mixinClassName.startsWith(CLIENT_PACKAGE)) {
            mixinClassName = mixinClassName.substring(CLIENT_PACKAGE.length());

            if (FMLLoader.getDist() != Dist.CLIENT) {
                // make extra sure client mixins are only loaded on clients
                return false;
            }

            if (mixinClassName.startsWith(BLOOM_PACKAGE)) {
                mixinClassName = mixinClassName.substring(BLOOM_PACKAGE.length());

                String[] mutable = temp;
                mutable[0] = mixinClassName;

                if (!filterBloomMixins(mutable)) return false;

                mixinClassName = mutable[0];
            }
        }

        for (var compatMod : MOD_COMPAT_MIXINS.entrySet()) {
            if (mixinClassName.startsWith(compatMod.getValue())) {
                return isModLoaded(compatMod.getKey());
            }
        }
        return true;
    }

    /// ensure only the appropriate set of bloom-related mixins are loaded
    private static boolean filterBloomMixins(String[] mixinClassName) {
        boolean safeModeConfigEnabled = ConfigHolder.getInstance().client.bloom.safeMode;

        if (mixinClassName[0].startsWith(BLOOM_NORMAL_PACKAGE)) {
            if (safeModeConfigEnabled) return false;

            // trim off the load type package so mod loaded checks also function
            mixinClassName[0] = mixinClassName[0].substring(BLOOM_NORMAL_PACKAGE.length());
        } else if (mixinClassName[0].startsWith(BLOOM_SAFEMODE_PACKAGE)) {
            if (!safeModeConfigEnabled) return false;

            // trim off the load type package so mod loaded checks also function
            mixinClassName[0] = mixinClassName[0].substring(BLOOM_SAFEMODE_PACKAGE.length());
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
        if (modId.equals("optifine")) {
            return OPTIFINE_PRESENT;
        } else {
            return FMLLoader.getLoadingModList().getModFileById(modId) != null;
        }
    }

    public static final boolean OPTIFINE_PRESENT;

    static {
        boolean hasOfClass = false;
        try {
            Class.forName("optifine.OptiFineTransformationService");
            hasOfClass = true;
        } catch (Throwable ignored) {}

        OPTIFINE_PRESENT = hasOfClass;
    }

    private static final String[] temp = new String[1];
}

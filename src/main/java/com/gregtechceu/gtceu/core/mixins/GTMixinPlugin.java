package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.core.config.GTEarlyConfig;
import com.gregtechceu.gtceu.core.config.Option;

import net.minecraftforge.fml.loading.FMLLoader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.UnknownNullability;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.io.File;
import java.util.*;

import static com.gregtechceu.gtceu.core.config.GTEarlyConfig.OPTIFINE_PRESENT;

public class GTMixinPlugin implements IMixinConfigPlugin {

    public static final Logger LOGGER = LogManager.getLogger("GregTechCEu");

    public static final String MIXIN_PACKAGE_ROOT = "com.gregtechceu.gtceu.core.mixins.";

    public static @UnknownNullability GTEarlyConfig CONFIG = null;

    public GTMixinPlugin() {
        if (CONFIG != null) {
            return;
        }

        try {
            CONFIG = GTEarlyConfig.load(new File("./config/gtceu-early-config.properties"));
        } catch (Exception e) {
            throw new RuntimeException("Could not load mixin configuration file for GTCEu", e);
        }

        CONFIG.getOptionMap().values().forEach(option -> {
            if (option.isOverridden()) {
                String source = "[unknown]";

                if (option.isUserDefined()) {
                    source = "user configuration";
                } else if (!FMLLoader.getLoadingModList().getErrors().isEmpty()) {
                    source = "load error";
                } else if (option.isModDefined()) {
                    source = "mods [" + String.join(", ", option.getDefiningMods()) + "]";
                }
                LOGGER.warn("Option '{}' overridden (by {}) to '{}'", option.getName(), source, option.isEnabled());
            }
        });

        if (OPTIFINE_PRESENT) {
            LOGGER.fatal(
                    "OptiFine detected. Use of GTCEu with OptiFine is not supported due to its breakage of Forge features.");
        }
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (!mixinClassName.startsWith(MIXIN_PACKAGE_ROOT)) {
            LOGGER.error("Expected mixin '{}' to start with package root '{}', treating as foreign and disabling!",
                    mixinClassName, MIXIN_PACKAGE_ROOT);

            return false;
        }

        String mixin = mixinClassName.substring(MIXIN_PACKAGE_ROOT.length());

        if (!isOptionEnabled(mixin)) {
            return false;
        }

        return true;
    }

    public static boolean isOptionEnabled(String mixin) {
        Option option = CONFIG.getEffectiveOptionForMixin(mixin);

        if (option == null) {
            String msg = "No rules matched mixin '{}', treating as foreign and disabling!";
            if (!FMLLoader.isProduction()) {
                LOGGER.error(msg, mixin);
            } else {
                LOGGER.debug(msg, mixin);
            }

            return false;
        }

        return option.isEnabled();
    }

    @Override
    public void onLoad(String mixinPackage) {}

    @Override
    public String getRefMapperConfig() {
        return null;
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
}

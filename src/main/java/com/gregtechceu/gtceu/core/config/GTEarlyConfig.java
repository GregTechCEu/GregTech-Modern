package com.gregtechceu.gtceu.core.config;

import net.minecraftforge.fml.loading.FMLLoader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.*;

/**
 * An early (e.g. mixin) config handler based on the one from <a
 * href=
 * "https://github.com/embeddedt/ModernFix/blob/4e3ecf9b6d7ab3ebecbc0604db916cd4922689fc/src/main/java/org/embeddedt/modernfix/core/config/ModernFixEarlyConfig.java">ModernFix</a>
 */
public class GTEarlyConfig {

    public static final String SAFE_MODE = "client.bloom.safe_mode.";

    private static final Logger LOGGER = LogManager.getLogger("GTEarlyConfig");

    private final Map<String, Option> options = new HashMap<>();
    private final File configFile;

    private GTEarlyConfig(File file) {
        this.configFile = file;

        // Defines the default rules which can be configured by the user or other mods.
        // You must manually add a rule for any new mixins not covered by an existing package rule.

        Option option = addMixinRule(SAFE_MODE, false);
        option.addComment(
                "Whether to use a 'safe mode' for bloom rendering",
                "NOTE: considerably slower than the normal logic, but likely fixes compatibility issues with other mods.",
                "Requires restarting the client to take effect.");

        addDelegateRule("client.bloom.safemode", SAFE_MODE, false);
        addDelegateRule("client.bloom.normal", SAFE_MODE, true);

        // hidden rules for dev-only mixins
        addHiddenRule("dev", !FMLLoader.isProduction());
        addHiddenRule("dev.datagen", FMLLoader.getLaunchHandler().isData());

        // hidden rules for mod dependencies
        enableIfModPresent("emi", "emi");
        enableIfModPresent("jei", "jei");
        enableIfModPresent("rei", "roughlyenoughitems");

        final String[] EMBEDDIUM_MOD_IDS = { "embeddium", "sodium" };
        final String[] OCULUS_MOD_IDS = { "oculus", "iris" };
        enableIfModPresent("embeddium", EMBEDDIUM_MOD_IDS);
        enableIfModPresent("oculus", OCULUS_MOD_IDS);
        enableIfModPresent("client.bloom.normal.embeddium", EMBEDDIUM_MOD_IDS);
        enableIfModPresent("client.bloom.normal.oculus", OCULUS_MOD_IDS);
        enableIfModPresent("client.bloom.safemode.embeddium", EMBEDDIUM_MOD_IDS);

        enableIfModPresent("top", "top");

        enableIfModPresent("ftbchunks", "ftbchunks");
        enableIfModPresent("xaerominimap", "xaerominimap");
        enableIfModPresent("xaeroworldmap", "xaeroworldmap");

        // bind non-empty parents
        for (Map.Entry<String, Option> entry : this.options.entrySet()) {
            if (entry.getValue().getParent() != null) continue;

            int idx = entry.getKey().lastIndexOf('.');
            if (idx <= 0) continue;

            String potentialParentKey = entry.getKey().substring(0, idx);
            Option potentialParent = this.options.get(potentialParentKey);
            if (potentialParent != null) {
                entry.getValue().setParent(potentialParent);
            }
        }
    }

    private void disableIfModPresent(String configName, String... ids) {
        Option option = this.options.get(configName);
        if (option == null) {
            option = addMixinRule(configName, true);
        }

        for (String id : ids) {
            if (isModLoaded(id)) {
                option.addModOverride(false, id);
            }
        }
    }

    // opposite of disableIfModPresent
    private void enableIfModPresent(String configName, String... ids) {
        Option option = this.options.get(configName);
        if (option == null) {
            option = addMixinRule(configName, false);
        }
        option.setHidden(true);

        for (String id : ids) {
            if (isModLoaded(id)) {
                option.addModOverride(true, id);
            }
        }
    }

    /**
     * Defines a Mixin rule which can be configured by users and other mods.
     *
     * @param configName The name of the mixin package which will be controlled by this rule
     * @param enabled    True if the rule will be enabled by default, otherwise false
     *
     * @throws IllegalStateException If a rule with that name already exists
     */
    private Option addMixinRule(String configName, boolean enabled) {
        if (configName.endsWith(".")) configName = configName.substring(0, configName.length() - 1);

        Option option = new Option(configName, enabled, false);
        if (this.options.putIfAbsent(configName, option) != null) {
            throw new IllegalStateException("Mixin rule already defined: " + configName);
        }
        return option;
    }

    /**
     * Defines a Mixin rule which can be configured by users and other mods.
     *
     * @param configName The name of the mixin package which will be controlled by this rule
     * @param enabled    True if the rule will be enabled by default, otherwise false
     *
     * @throws IllegalStateException If a rule with that name already exists
     */
    private Option addHiddenRule(String configName, boolean enabled) {
        Option option = addMixinRule(configName, enabled);
        option.setHidden(true);
        return option;
    }

    /**
     * Defines a Mixin rule which directly relegates to another (existing) mixin rule.
     *
     * @param configName   The name of the mixin package which will be controlled by this rule
     * @param delegateName The name of the (existing) rule which this rule delegates to
     * @param invert       Whether to invert {@code delegateName}'s enabled state
     *
     * @throws IllegalStateException    If a rule with that name already exists
     * @throws IllegalArgumentException If a rule named {@code delegateName} doesn't already exist
     */
    private Option addDelegateRule(String configName, String delegateName, boolean invert) {
        if (delegateName.endsWith(".")) delegateName = delegateName.substring(0, delegateName.length() - 1);

        Option delegateOption = this.options.get(delegateName);
        if (delegateOption == null) {
            throw new IllegalArgumentException("Delegate rule not defined: " + delegateName);
        }

        Option option = addHiddenRule(configName, delegateOption.isEnabled() != invert);
        option.setParent(delegateOption);
        option.setParentValueInverted(invert);

        return option;
    }

    private void readProperties(Properties props) {
        for (Map.Entry<Object, Object> entry : props.entrySet()) {
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();

            Option option = this.options.get(key);
            if (option == null) {
                LOGGER.warn("No configuration key exists with name '{}', ignoring", key);
                continue;
            }
            if (option.isHidden()) {
                continue;
            }

            boolean enabled;
            if (value.equalsIgnoreCase("true")) {
                enabled = true;
            } else if (value.equalsIgnoreCase("false")) {
                enabled = false;
            } else {
                LOGGER.warn("Invalid value '{}' encountered for configuration key '{}', ignoring", value, key);
                continue;
            }

            if (!option.isModDefined()) {
                option.setEnabled(enabled, true);
            } else {
                LOGGER.warn("Option '{}' already disabled by a mod. Ignoring user configuration", key);
            }
        }
    }

    /**
     * Returns the effective option for the specified class name. This traverses the package path of the given mixin
     * and checks each root for configuration rules. If a configuration rule disables a package, all mixins located in
     * that package and its children will be disabled. The effective option is that of the highest-priority rule, either
     * an enable rule at the end of the chain or a disable rule at the earliest point in the chain.
     *
     * @return Null if no options matched the given mixin name, otherwise the effective option for this Mixin
     */
    public @Nullable Option getEffectiveOptionForMixin(String mixinClassName) {
        int lastSplit = 0;
        int nextSplit;

        Option rule = null;

        while ((nextSplit = mixinClassName.indexOf('.', lastSplit)) != -1) {
            String key = mixinClassName.substring(0, nextSplit);

            Option candidate = this.options.get(key);

            if (candidate != null) {
                rule = candidate;

                if (!rule.isEnabled()) {
                    return rule;
                }
            }

            lastSplit = nextSplit + 1;
        }

        return rule;
    }

    /**
     * Loads the configuration file from the specified location. If it does not exist, a new configuration file will be
     * created. The file on disk will then be updated to include any new options.
     */
    public static GTEarlyConfig load(File file) {
        GTEarlyConfig config = new GTEarlyConfig(file);
        Properties props = new Properties();
        if (file.exists()) {
            try (FileInputStream fin = new FileInputStream(file)) {
                props.load(fin);
            } catch (IOException e) {
                throw new RuntimeException("Could not load config file", e);
            }
            config.readProperties(props);
        }

        try {
            config.save();
        } catch (IOException e) {
            LOGGER.warn("Could not write configuration file", e);
        }

        return config;
    }

    public void save() throws IOException {
        File dir = configFile.getParentFile();

        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                throw new IOException("Could not create parent directories");
            }
        } else if (!dir.isDirectory()) {
            throw new IOException("The parent file is not a directory");
        }

        try (Writer writer = new FileWriter(configFile)) {
            writer.write("# This is the early configuration file for GregTech CEu Modern.\n");
            writer.write("# The following options can be enabled or disabled if there is a compatibility issue.\n");
            writer.write(
                    "# Add a line with your option name and =true or =false at the bottom of the file to enable\n");
            writer.write("# or disable a rule. For example:\n");
            writer.write("#   client.bloom.safe_mode=true\n");
            writer.write("# Do not include the #. You may reset to defaults by deleting this file.\n");
            writer.write("#\n");
            writer.write("# Available options:\n");
            var entries = this.options.entrySet().stream()
                    .filter(entry -> !entry.getValue().isHidden())
                    .sorted()
                    .toList();

            for (var entry : entries) {
                String line = entry.getKey();
                Option option = entry.getValue();

                String extraContext;
                if (!option.isUserDefined()) {
                    extraContext = "=" + option.isEnabled() + " # " +
                            (option.isModDefined() ? "(overridden for mod compat)" : "(default)");
                } else {
                    extraContext = "=" + option.isDefaultEnabled() + " # (default)";
                }

                writer.write("#\n");
                if (option.getComment() != null) {
                    for (String commentLine : option.getComment()) {
                        writer.write("#  # " + commentLine + "\n");
                    }
                }
                writer.write("#  " + line + extraContext + "\n");
            }

            writer.write("#\n");
            writer.write("#\n");
            writer.write("# User overrides go here.\n");

            for (var entry : entries) {
                Option option = entry.getValue();
                if (option.isUserDefined()) {
                    writer.write(entry.getKey() + "=" + option.isEnabled() + "\n");
                }
            }
        }
    }

    public int getOptionCount() {
        return this.options.size();
    }

    public int getOptionOverrideCount() {
        return (int) this.options.values()
                .stream()
                .filter(option -> !option.isHidden())
                .filter(Option::isOverridden)
                .count();
    }

    public Map<String, Option> getOptionMap() {
        return Collections.unmodifiableMap(this.options);
    }

    public static boolean isModLoaded(String modId) {
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
}

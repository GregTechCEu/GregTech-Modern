package com.gregtechceu.gtceu.integration.kjs;

import net.minecraft.resources.Identifier;

import dev.latvian.mods.kubejs.script.ConsoleJS;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

@FunctionalInterface
public interface Validator {

    enum ValidationResult {
        SUCCESS,
        SILENT,
        WARNING,
        ERROR
    }

    ValidationResult run(Identifier id);

    static void validate(Identifier id, Validator... validators) {
        long errors = Arrays.stream(validators)
                .map(validator -> validator.run(id))
                .filter(result -> result == ValidationResult.ERROR)
                .count(); // Ensure all errors are caught here

        if (errors > 0) {
            ConsoleJS.getCurrent(null).error("VALIDATION FAILED! Cannot build %s".formatted(id));

            throw new IllegalStateException(
                    "Validation failed for %s. Please check your KubeJS logs for details.".formatted(id));
        }
    }

    ////////////////////////////////////////
    // ********* VALIDATORS *********//
    ////////////////////////////////////////

    static Validator onlySetDefault(@Nullable Object value, Runnable defaultSetter) {
        return id -> {
            if (value != null)
                return ValidationResult.SUCCESS;

            defaultSetter.run();
            return ValidationResult.SILENT;
        };
    }

    static Validator warnRecommendedIfNull(@Nullable Object value, String name) {
        return warnRecommendedIfNull(value, name, () -> {});
    }

    static Validator warnRecommendedIfNull(@Nullable Object value, String name, Runnable defaultSetter) {
        return id -> {
            if (value != null)
                return ValidationResult.SUCCESS;

            ConsoleJS.getCurrent(null)
                    .warn("Value %s is not defined in %s. It is recommended to set a value.".formatted(name, id));
            defaultSetter.run();

            return ValidationResult.WARNING;
        };
    }

    static Validator warnDefaultIfNull(@Nullable Object value, String name, Runnable defaultSetter) {
        return warnDefaultIfNull(value, name, null, defaultSetter);
    }

    static Validator warnDefaultIfNull(@Nullable Object value, String name,
                                       @Nullable String defaultExplanation, Runnable defaultSetter) {
        return id -> {
            if (value != null)
                return ValidationResult.SUCCESS;

            var explanation = defaultExplanation != null ? defaultExplanation : "Using a default value.";

            ConsoleJS.getCurrent(null).warn("Value %s is not defined in %s. %s".formatted(name, id, explanation));
            defaultSetter.run();

            return ValidationResult.WARNING;
        };
    }

    static Validator errorIfNull(@Nullable Object value, String name) {
        return id -> {
            if (value != null)
                return ValidationResult.SUCCESS;

            ConsoleJS.getCurrent(null).error("Cannot build %s: %s is not set.".formatted(id, name));
            return ValidationResult.ERROR;
        };
    }

    static Validator errorIfOutOfRange(int value, String name, int min, int max) {
        return id -> {
            if (value >= min && value <= max) {
                return ValidationResult.SUCCESS;
            }

            var message = "Cannot build %s, %s is not in range [%d, %d].".formatted(id, name, min, max);

            ConsoleJS.getCurrent(null).error(message);
            return ValidationResult.ERROR;
        };
    }
}

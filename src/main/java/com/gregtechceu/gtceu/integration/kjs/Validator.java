package com.gregtechceu.gtceu.integration.kjs;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;

import dev.latvian.mods.kubejs.util.ConsoleJS;
import dev.latvian.mods.rhino.Context;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
@FunctionalInterface
public interface Validator {

    enum ValidationResult {
        SUCCESS,
        SILENT,
        WARNING,
        ERROR
    }

    ValidationResult run(ResourceLocation id);

    static void validate(ResourceLocation id, Validator... validators) {
        long errors = Arrays.stream(validators)
                .map(validator -> validator.run(id))
                .filter(result -> result == ValidationResult.ERROR)
                .count(); // Ensure all errors are caught here

        if (errors > 0) {
            ConsoleJS.getCurrent((Context) null).error("VALIDATION FAILED! Cannot build %s".formatted(id));

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

            ConsoleJS.getCurrent((Context) null)
                    .warn("Value %s is not defined in %s. It is recommended to set a value.".formatted(name, id));
            defaultSetter.run();

            return ValidationResult.WARNING;
        };
    }

    static Validator warnDefaultIfNull(@Nullable Object value, String name, Runnable defaultSetter) {
        return warnDefaultIfNull(value, name, null, defaultSetter);
    }

    static Validator warnDefaultIfNull(@Nullable Object value, String name, @Nullable String defaultExplanation,
                                       Runnable defaultSetter) {
        return id -> {
            if (value != null)
                return ValidationResult.SUCCESS;

            var explanation = defaultExplanation != null ? defaultExplanation : "Using a default value.";

            ConsoleJS.getCurrent((Context) null)
                    .warn("Value %s is not defined in %s. %s".formatted(name, id, explanation));
            defaultSetter.run();

            return ValidationResult.WARNING;
        };
    }

    static Validator errorIfNull(@Nullable Object value, String name) {
        return id -> {
            if (value != null)
                return ValidationResult.SUCCESS;

            var message = "Cannot build %s: %s is not set.".formatted(id, name);

            ConsoleJS.getCurrent((Context) null).error(message);
            return ValidationResult.ERROR;
        };
    }

    static Validator errorIfOutOfRange(int value, String name, int min, int max) {
        return id -> {
            if (value >= min && value <= max) {
                return ValidationResult.SUCCESS;
            }

            var message = "Cannot build %s, %s is not in range [%d, %d].".formatted(id, name, min, max);

            ConsoleJS.getCurrent((Context) null).error(message);
            return ValidationResult.ERROR;
        };
    }
}

package com.gregtechceu.gtceu.integration.kjs.recipe.components;

import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.recipe.content.Content;

import dev.latvian.mods.kubejs.recipe.InputReplacement;
import dev.latvian.mods.kubejs.recipe.OutputReplacement;
import dev.latvian.mods.kubejs.recipe.RecipeJS;
import dev.latvian.mods.kubejs.recipe.ReplacementMatch;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.ArrayUtils;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@NoArgsConstructor
public class CapabilityMap extends IdentityHashMap<RecipeCapability<?>, Content[]>
                           implements InputReplacement, OutputReplacement {

    public CapabilityMap(Map<RecipeCapability<?>, Content[]> m) {
        super(m);
    }

    public void add(RecipeCapability<?> capability, Content value) {
        this.put(capability, ArrayUtils.add(this.get(capability), value));
    }

    public boolean isInput(RecipeJS recipe, ReplacementMatch match) {
        AtomicBoolean returnValue = new AtomicBoolean(false);
        this.forEach((key, values) -> {
            var pair = GTRecipeComponents.VALID_CAPS.get(key);
            if (!pair.getFirst().isOutput()) {
                for (Content value : values) {
                    if (pair.getFirst().isInput(recipe, value, match)) {
                        returnValue.set(true);
                        return;
                    }
                }
            } else if (!pair.getSecond().isOutput()) {
                for (Content value : values) {
                    if (pair.getSecond().isInput(recipe, value, match)) {
                        returnValue.set(true);
                        return;
                    }
                }
            }
        });
        return returnValue.get();
    }

    public boolean isOutput(RecipeJS recipe, ReplacementMatch match) {
        AtomicBoolean returnValue = new AtomicBoolean(false);
        this.forEach((key, values) -> {
            var pair = GTRecipeComponents.VALID_CAPS.get(key);
            if (pair.getFirst().isOutput()) {
                for (Content value : values) {
                    if (pair.getFirst().isOutput(recipe, value, match)) {
                        returnValue.set(true);
                        return;
                    }
                }
            } else if (pair.getSecond().isOutput()) {
                for (Content value : values) {
                    if (pair.getSecond().isOutput(recipe, value, match)) {
                        returnValue.set(true);
                        return;
                    }
                }
            }
        });
        return returnValue.get();
    }

    @Override
    public CapabilityMap replaceInput(RecipeJS recipe, ReplacementMatch match, InputReplacement with) {
        AtomicBoolean changed = new AtomicBoolean(false);
        this.forEach((key, values) -> {
            var pair = GTRecipeComponents.VALID_CAPS.get(key);
            if (!pair.getFirst().isOutput()) {
                for (int i = 0; i < values.length; ++i) {
                    Content value = values[i];
                    Content result = pair.getFirst().replaceInput(recipe, value, match, with);
                    if (result != value) {
                        changed.set(true);
                        values[i] = result;
                    }
                }
            } else if (!pair.getSecond().isOutput()) {
                for (int i = 0; i < values.length; ++i) {
                    Content value = values[i];
                    Content result = pair.getSecond().replaceInput(recipe, value, match, with);
                    if (result != value) {
                        changed.set(true);
                        values[i] = result;
                    }
                }
            }
        });
        return changed.get() ? new CapabilityMap(this) : this;
    }

    @Override
    public CapabilityMap replaceOutput(RecipeJS recipe, ReplacementMatch match, OutputReplacement with) {
        AtomicBoolean changed = new AtomicBoolean(false);
        this.forEach((key, values) -> {
            var pair = GTRecipeComponents.VALID_CAPS.get(key);
            if (pair.getFirst().isOutput()) {
                for (int i = 0; i < values.length; ++i) {
                    Content value = values[i];
                    Content result = pair.getFirst().replaceOutput(recipe, value, match, with);
                    if (result != value) {
                        changed.set(true);
                        values[i] = result;
                    }
                }
            } else if (pair.getSecond().isOutput()) {
                for (int i = 0; i < values.length; ++i) {
                    Content value = values[i];
                    Content result = pair.getSecond().replaceOutput(recipe, value, match, with);
                    if (result != value) {
                        changed.set(true);
                        values[i] = result;
                    }
                }
            }
        });
        return changed.get() ? new CapabilityMap(this) : this;
    }
}

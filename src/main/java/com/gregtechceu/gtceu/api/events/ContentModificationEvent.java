package com.gregtechceu.gtceu.api.events;

import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MachineInstanceFactory;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.category.GTRecipeCategory;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.registry.GTRegistry;
import com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.event.IModBusEvent;

import lombok.Getter;

import java.util.function.Consumer;

/**
 * Events fired to modify base GT content and other addon content.<br>
 *
 */
public abstract class ContentModificationEvent<T> extends Event implements IModBusEvent {

    /**
     * The ID of the content.
     */
    @Getter
    private final ResourceLocation id;
    /**
     * The registry it is being registered into.
     */
    @Getter
    private final GTRegistry<?, T> registry;

    protected ContentModificationEvent(ResourceLocation id, GTRegistry<?, T> registry) {
        this.id = id;
        this.registry = registry;
    }

    public static class Machine extends ContentModificationEvent<MachineDefinition> {

        @Getter
        private final MachineBuilder<?, ?, ?> builder;

        public Machine(MachineBuilder<?, ?, ?> builder) {
            super(builder.id, GTRegistries.MACHINES);
            this.builder = builder;
        }

        /**
         * Add a callback to be run after the machine is created, useful for adding traits or performing other modification.
         * 
         * @param creationCallback The callback to run
         */
        @SuppressWarnings({ "unchecked", "rawtypes" })
        public void afterCreation(Consumer<? extends MetaMachine> creationCallback) {
            MachineInstanceFactory current = builder.instanceFactory();
            builder.instanceFactory(current.andThen(creationCallback));
        }
    }

    public static class TagPrefix extends ContentModificationEvent<com.gregtechceu.gtceu.api.data.tag.TagPrefix> {

        @Getter
        private final com.gregtechceu.gtceu.api.data.tag.TagPrefix tagPrefix;

        public TagPrefix(com.gregtechceu.gtceu.api.data.tag.TagPrefix tagPrefix) {
            super(tagPrefix.id, GTRegistries.TAG_PREFIXES);
            this.tagPrefix = tagPrefix;
        }
    }

    public static class RecipeType extends ContentModificationEvent<GTRecipeType> {

        @Getter
        private final GTRecipeType recipeType;

        public RecipeType(GTRecipeType recipeType) {
            super(recipeType.registryName, GTRegistries.RECIPE_TYPES);
            this.recipeType = recipeType;
        }
    }

    public static class RecipeCategory extends ContentModificationEvent<GTRecipeCategory> {

        @Getter
        private final GTRecipeCategory recipeCategory;

        public RecipeCategory(GTRecipeCategory recipeCategory) {
            super(recipeCategory.registryKey, GTRegistries.RECIPE_CATEGORIES);
            this.recipeCategory = recipeCategory;
        }
    }
}

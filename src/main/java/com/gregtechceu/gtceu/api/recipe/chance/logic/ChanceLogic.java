package com.gregtechceu.gtceu.api.recipe.chance.logic;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.recipe.chance.boost.ChanceBoostFunction;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

import net.minecraftforge.fml.ModLoader;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collections;
import java.util.List;

/**
 * Logic for determining which chanced outputs should be produced from a list
 */
public abstract class ChanceLogic {

    static {
        GTRegistries.CHANCE_LOGICS.unfreeze();
    }

    /**
     * Chanced Output Logic where any ingredients succeeding their roll will be produced
     */
    public static final ChanceLogic OR = new ChanceLogic("or") {

        @Override
        public @Nullable @Unmodifiable List<@NotNull Content> roll(@NotNull @Unmodifiable List<@NotNull Content> chancedEntries,
                                                                   @NotNull ChanceBoostFunction boostFunction,
                                                                   int baseTier, int machineTier,
                                                                   @Nullable Object2IntMap<?> cache) {
            ImmutableList.Builder<Content> builder = ImmutableList.builder();
            for (Content entry : chancedEntries) {
                int cached = getCachedChance(entry, cache);

                int chance = getChance(entry, boostFunction, baseTier, machineTier) + cached;
                int maxChance = entry.maxChance;
                if (passesChance(chance, maxChance)) {
                    do {
                        builder.add(entry);
                        chance -= maxChance;
                    } while (passesChance(chance, maxChance));
                }
                updateCachedChance(entry.content, cache, chance);
            }

            List<Content> list = builder.build();
            return list.isEmpty() ? null : list;
        }

        @Override
        public @NotNull String getTranslationKey() {
            return "gtceu.chance_logic.or";
        }

        @Override
        public String toString() {
            return "ChanceLogic{OR}";
        }
    };

    /**
     * Chanced Output Logic where all ingredients must succeed their roll in order for any to be produced
     */
    public static final ChanceLogic AND = new ChanceLogic("and") {

        @Override
        public @Nullable @Unmodifiable List<@NotNull Content> roll(@NotNull @Unmodifiable List<@NotNull Content> chancedEntries,
                                                                   @NotNull ChanceBoostFunction boostFunction,
                                                                   int baseTier, int machineTier,
                                                                   @Nullable Object2IntMap<?> cache) {
            boolean failed = false;
            for (Content entry : chancedEntries) {
                int cached = getCachedChance(entry, cache);

                int chance = getChance(entry, boostFunction, baseTier, machineTier) + cached;
                if (!passesChance(chance, entry.maxChance)) {
                    failed = true;
                }
                updateCachedChance(entry.content, cache, chance);
            }
            return failed ? null : ImmutableList.copyOf(chancedEntries);
        }

        @Override
        public @NotNull String getTranslationKey() {
            return "gtceu.chance_logic.and";
        }

        @Override
        public String toString() {
            return "ChanceLogic{AND}";
        }
    };

    /**
     * Chanced Output Logic where only the first ingredient succeeding its roll will be produced
     */
    public static final ChanceLogic XOR = new ChanceLogic("xor") {

        @Override
        public @Nullable @Unmodifiable List<@NotNull Content> roll(@NotNull @Unmodifiable List<@NotNull Content> chancedEntries,
                                                                   @NotNull ChanceBoostFunction boostFunction,
                                                                   int baseTier, int machineTier,
                                                                   @Nullable Object2IntMap<?> cache) {
            Content selected = null;
            for (Content entry : chancedEntries) {
                int cached = getCachedChance(entry, cache);

                int chance = getChance(entry, boostFunction, baseTier, machineTier) + cached;
                if (passesChance(chance, entry.maxChance) && selected == null) {
                    selected = entry;
                }
                updateCachedChance(entry.content, cache, chance);
            }
            return selected == null ? null : Collections.singletonList(selected);
        }

        @Override
        public @NotNull String getTranslationKey() {
            return "gtceu.chance_logic.xor";
        }

        @Override
        public String toString() {
            return "ChanceLogic{XOR}";
        }
    };

    /**
     * Chanced Output Logic where nothing is produced
     */
    public static final ChanceLogic NONE = new ChanceLogic("none") {

        @Override
        public @Nullable @Unmodifiable List<@NotNull Content> roll(@NotNull @Unmodifiable List<@NotNull Content> chancedEntries,
                                                                   @NotNull ChanceBoostFunction boostFunction,
                                                                   int baseTier, int machineTier,
                                                                   @Nullable Object2IntMap<?> cache) {
            return null;
        }

        @Override
        public @NotNull String getTranslationKey() {
            return "gtceu.chance_logic.none";
        }

        @Override
        public String toString() {
            return "ChanceLogic{NONE}";
        }
    };

    public ChanceLogic(String id) {
        GTRegistries.CHANCE_LOGICS.register(id, this);
    }

    /**
     * @param entry         the entry to get the complete chance for
     * @param boostFunction the function boosting the entry's chance
     * @param baseTier      the base tier of the recipe
     * @param machineTier   the tier the recipe is run at
     * @return the total chance for the entry
     */
    static int getChance(@NotNull Content entry, @NotNull ChanceBoostFunction boostFunction, int baseTier,
                         int machineTier) {
        return boostFunction.getBoostedChance(entry, baseTier, machineTier);
    }

    /**
     * @param chance the chance to check
     * @return if the roll with the chance is successful
     */
    static boolean passesChance(int chance, int maxChance) {
        return chance >= maxChance;
    }

    /**
     * @return the upper bound for rolling chances
     */
    public static int getMaxChancedValue() {
        return 10_000;
    }

    /**
     * @param entry the current entry
     * @param cache the cache of previously rolled chances, can be null
     * @return the cached chance, otherwise a random initial chance
     *         between 0 and {@link Content#maxChance} (exclusive)
     */
    static int getCachedChance(Content entry, @Nullable Object2IntMap<?> cache) {
        if (cache == null || !cache.containsKey(entry.content))
            return GTValues.RNG.nextInt(entry.maxChance);

        return cache.getInt(entry.content);
    }

    /**
     * @param ingredient the key used for the cache
     * @param cache      the cache of previously rolled chances, can be null
     * @param chance     the chance to update the cache with
     */
    static void updateCachedChance(Object ingredient, @Nullable Object2IntMap<?> cache, int chance) {
        if (cache == null) return;
        // noinspection unchecked,rawtypes
        ((Object2IntMap) cache).put(ingredient, chance);
    }

    /**
     * Roll the chance and attempt to produce the output
     *
     * @param chancedEntries the list of entries to roll
     * @param boostFunction  the function to boost the entries' chances
     * @param baseTier       the base tier of the recipe
     * @param machineTier    the tier the recipe is run at
     * @param cache          the cache of previously rolled chances, can be null
     * @return a list of the produced outputs, or null if failed
     */
    public abstract @Nullable @Unmodifiable List<@NotNull Content> roll(
                                                                        @NotNull @Unmodifiable List<@NotNull Content> chancedEntries,
                                                                        @NotNull ChanceBoostFunction boostFunction,
                                                                        int baseTier, int machineTier,
                                                                        @Nullable Object2IntMap<?> cache);

    /**
     * Roll the chance and attempt to produce the output
     *
     * @param chancedEntries the list of entries to roll
     * @param boostFunction  the function to boost the entries' chances
     * @param baseTier       the base tier of the recipe
     * @param machineTier    the tier the recipe is run at
     * @return a list of the produced outputs
     */
    @Nullable
    @Unmodifiable
    public List<@NotNull Content> roll(
                                       @NotNull @Unmodifiable List<@NotNull Content> chancedEntries,
                                       @NotNull ChanceBoostFunction boostFunction,
                                       int baseTier, int machineTier) {
        return roll(chancedEntries, boostFunction, baseTier, machineTier, null);
    }

    @NotNull
    public abstract String getTranslationKey();

    @ApiStatus.Internal
    public static void init() {
        ModLoader.get().postEvent(new GTCEuAPI.RegisterEvent<>(GTRegistries.CHANCE_LOGICS, ChanceLogic.class));
        GTRegistries.CHANCE_LOGICS.freeze();
    }
}

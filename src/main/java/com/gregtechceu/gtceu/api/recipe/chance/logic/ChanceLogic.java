package com.gregtechceu.gtceu.api.recipe.chance.logic;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.recipe.chance.boost.ChanceBoostFunction;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

import net.minecraft.network.chat.Component;
import net.minecraftforge.fml.ModLoader;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
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
        public @Unmodifiable List<@NotNull Content> roll(@NotNull @Unmodifiable List<@NotNull Content> chancedEntries,
                                                         @NotNull ChanceBoostFunction boostFunction,
                                                         int recipeTier, int chanceTier,
                                                         @Nullable Object2IntMap<?> cache, int times) {
            ImmutableList.Builder<Content> builder = ImmutableList.builder();
            for (Content entry : chancedEntries) {
                int maxChance = entry.maxChance;

                // OR Chanced outputs are deterministic
                // If a large batch is being done we can calculate how many we expect to get.
                // Add the guaranteed part of that to the list, then roll for the remaining chanced part.
                int newChance = getChance(entry, boostFunction, recipeTier, chanceTier);
                int totalChance = times * newChance;
                int guaranteed = totalChance / maxChance;
                if (guaranteed > 0) builder.addAll(Collections.nCopies(guaranteed, entry));
                newChance = totalChance % maxChance;

                int cached = getCachedChance(entry, cache);
                int chance = newChance + cached;
                while (passesChance(chance, maxChance)) {
                    builder.add(entry);
                    chance -= maxChance;
                    newChance -= maxChance;
                }
                updateCachedChance(entry.content, cache, newChance / 2 + cached);
            }
            return builder.build();
        }

        @Override
        public @NotNull Component getTranslation() {
            return Component.translatable("gtceu.chance_logic.or");
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
        public @Unmodifiable List<@NotNull Content> roll(@NotNull @Unmodifiable List<@NotNull Content> chancedEntries,
                                                         @NotNull ChanceBoostFunction boostFunction,
                                                         int recipeTier, int chanceTier,
                                                         @Nullable Object2IntMap<?> cache, int times) {
            ImmutableList.Builder<Content> builder = ImmutableList.builder();
            for (int i = 0; i < times; ++i) {
                boolean failed = false;
                for (Content entry : chancedEntries) {
                    int newChance = getChance(entry, boostFunction, recipeTier, chanceTier);
                    int cached = getCachedChance(entry, cache);
                    int chance = newChance + cached;
                    if (passesChance(chance, entry.maxChance)) newChance -= entry.maxChance;
                    else failed = true;
                    updateCachedChance(entry.content, cache, newChance / 2 + cached);
                    if (failed) break;
                }
                if (!failed) builder.addAll(chancedEntries);
            }
            return builder.build();
        }

        @Override
        public @NotNull Component getTranslation() {
            return Component.translatable("gtceu.chance_logic.and");
        }

        @Override
        public String toString() {
            return "ChanceLogic{AND}";
        }
    };

    /**
     * Chanced Output Logic where only the first ingredient succeeding its roll will be produced
     * Deprecated following the rewrite of XOR
     */
    @Deprecated
    public static final ChanceLogic FIRST = new ChanceLogic("first") {

        @Override
        public @Unmodifiable List<@NotNull Content> roll(@NotNull @Unmodifiable List<@NotNull Content> chancedEntries,
                                                         @NotNull ChanceBoostFunction boostFunction,
                                                         int recipeTier, int chanceTier,
                                                         @Nullable Object2IntMap<?> cache, int times) {
            ImmutableList.Builder<Content> builder = ImmutableList.builder();
            for (int i = 0; i < times; ++i) {
                Content selected = null;
                for (Content entry : chancedEntries) {
                    int newChance = getChance(entry, boostFunction, recipeTier, chanceTier);
                    int cached = getCachedChance(entry, cache);
                    int chance = newChance + cached;
                    if (passesChance(chance, entry.maxChance)) {
                        selected = entry;
                        newChance -= entry.maxChance;
                    }
                    updateCachedChance(entry.content, cache, newChance / 2 + cached);
                    if (selected != null) break;
                }
                if (selected != null) builder.add(selected);
            }
            return builder.build();
        }

        @Override
        public @NotNull Component getTranslation() {
            return Component.translatable("gtceu.chance_logic.first");
        }

        @Override
        public String toString() {
            return "ChanceLogic{FIRST}";
        }
    };

    /**
     * Chanced Output Logic where only one of the ingredients will be output, in a manner weighted to the input chances
     */
    public static final ChanceLogic XOR = new ChanceLogic("xor") {

        @Override
        public @Unmodifiable List<@NotNull Content> roll(@NotNull @Unmodifiable List<@NotNull Content> chancedEntries,
                                                         @NotNull ChanceBoostFunction boostFunction,
                                                         int recipeTier, int chanceTier,
                                                         @Nullable Object2IntMap<?> cache, int times) {
            // Have to set up a system where all chances are set to be out of 10000
            IntList chancesOutOfTenThousand = new IntArrayList();

            for (Content orig : chancedEntries) {
                if (orig.maxChance == getMaxChancedValue()) {
                    chancesOutOfTenThousand.add(orig.chance);
                } else {
                    chancesOutOfTenThousand.add((int) ((orig.chance / (float) orig.maxChance) * getMaxChancedValue()));
                }
            }

            int chanceTotal = 0;
            for (int chance : chancesOutOfTenThousand) {
                chanceTotal += chance;
            }

            // Here, if the newly calculated chances don't add up to 10000, they're renormalized
            if (chanceTotal != getMaxChancedValue()) {
                int chanceTotalDecremented = getMaxChancedValue();
                for (int i = 0; i < chancesOutOfTenThousand.size(); i++) {
                    int newChance = (int) (chancesOutOfTenThousand.getInt(i) *
                            ((float) getMaxChancedValue() / (float) chanceTotal));
                    // last chance ends up being set to the remainder in case things don't line up
                    if (i == chancesOutOfTenThousand.size() - 1) {
                        chancesOutOfTenThousand.set(i, chanceTotalDecremented);
                    } else {
                        chancesOutOfTenThousand.set(i, newChance);
                    }
                    chanceTotalDecremented -= newChance;
                }
            }

            // Finally, generate a new Content list with the changes
            List<Content> normalizedEntries = new ArrayList<>();
            for (int i = 0; i < chancesOutOfTenThousand.size(); i++) {
                normalizedEntries.add(new Content(chancedEntries.get(i).content, chancesOutOfTenThousand.getInt(i),
                        getMaxChancedValue(), chancedEntries.get(i).tierChanceBoost));
            }

            // Use the new, normalized list for the logic
            ImmutableList.Builder<Content> builder = ImmutableList.builder();
            for (int i = 0; i < times; ++i) {
                Content selected = null;
                int maxChance = getMaxChancedValue();
                for (Content entry : normalizedEntries) {
                    int newChance = getChance(entry, boostFunction, recipeTier, chanceTier);
                    int cached = getCachedChance(entry, cache);
                    int chance = newChance + cached;
                    if (passesChance(chance, maxChance)) {
                        selected = entry;
                        newChance -= maxChance;
                    }
                    updateCachedChance(entry.content, cache, newChance / 2 + cached);
                    if (selected != null) break;
                    maxChance -= newChance;
                }
                if (selected != null) builder.add(selected);
            }
            return builder.build();
        }

        @Override
        public @NotNull Component getTranslation() {
            return Component.translatable("gtceu.chance_logic.xor");
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
        public @Unmodifiable List<@NotNull Content> roll(@NotNull @Unmodifiable List<@NotNull Content> chancedEntries,
                                                         @NotNull ChanceBoostFunction boostFunction,
                                                         int recipeTier, int chanceTier,
                                                         @Nullable Object2IntMap<?> cache, int times) {
            return Collections.emptyList();
        }

        @Override
        public @NotNull Component getTranslation() {
            return Component.translatable("gtceu.chance_logic.none");
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
     * @param recipeTier    the base tier of the recipe
     * @param chanceTier    the tier the recipe is run at
     * @return the total chance for the entry
     */
    static int getChance(@NotNull Content entry, @NotNull ChanceBoostFunction boostFunction, int recipeTier,
                         int chanceTier) {
        return boostFunction.getBoostedChance(entry, recipeTier, chanceTier);
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
     * @param recipeTier     the base tier of the recipe
     * @param chanceTier     the tier the recipe is run at
     * @param cache          the cache of previously rolled chances, can be null
     * @param times          the number of times to roll
     * @return a list of the produced outputs, empty if roll fails
     */
    public abstract @Unmodifiable List<@NotNull Content> roll(@NotNull @Unmodifiable List<@NotNull Content> chancedEntries,
                                                              @NotNull ChanceBoostFunction boostFunction,
                                                              int recipeTier,
                                                              int chanceTier, @Nullable Object2IntMap<?> cache,
                                                              int times);

    /**
     * Roll the chance and attempt to produce the output
     *
     * @param chancedEntries the list of entries to roll
     * @param boostFunction  the function to boost the entries' chances
     * @param recipeTier     the base tier of the recipe
     * @param chanceTier     the tier the recipe is run at
     * @param times          the number of times to roll
     * @return a list of the produced outputs
     */
    @Unmodifiable
    public List<@NotNull Content> roll(@NotNull @Unmodifiable List<@NotNull Content> chancedEntries,
                                       @NotNull ChanceBoostFunction boostFunction, int recipeTier, int chanceTier,
                                       int times) {
        return roll(chancedEntries, boostFunction, recipeTier, chanceTier, null, times);
    }

    @NotNull
    public abstract Component getTranslation();

    @ApiStatus.Internal
    public static void init() {
        ModLoader.get().postEvent(new GTCEuAPI.RegisterEvent<>(GTRegistries.CHANCE_LOGICS, ChanceLogic.class));
        GTRegistries.CHANCE_LOGICS.freeze();
    }
}

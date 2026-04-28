package com.gregtechceu.gtceu.data.loot;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.core.mixins.LootPoolAccessor;
import com.gregtechceu.gtceu.utils.GTMath;
import com.gregtechceu.gtceu.utils.ItemStackHashStrategy;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.LootTableLoadEvent;

import com.google.common.base.Preconditions;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public final class ChestGenHooks {

    private static final Map<Identifier, List<GTLootEntryItem>> lootEntryItems = new Object2ObjectOpenHashMap<>();
    private static final Map<Identifier, NumberProvider> rollValues = new Object2ObjectOpenHashMap<>();

    private static final List<LootItemCondition> NO_CONDITIONS = List.of();

    private ChestGenHooks() {}

    public static void init() {
        NeoForge.EVENT_BUS.register(ChestGenHooks.class);
        RandomWeightLootFunction.init();
    }

    @SubscribeEvent
    public static void onWorldLoad(@NotNull LootTableLoadEvent event) {
        LootPool mainPool = event.getTable().getPool("main");
        if (mainPool == null) return;

        Identifier name = event.getName();
        if (lootEntryItems.containsKey(name)) {
            List<GTLootEntryItem> entryItems = lootEntryItems.get(name);
            for (GTLootEntryItem entry : entryItems) {
                if (ConfigHolder.INSTANCE.dev.debug) {
                    GTCEu.LOGGER.info("adding {} to lootTable {}", entry, name);
                }

                try {
                    List<LootPoolEntryContainer> entries = new ArrayList<>(((LootPoolAccessor) mainPool).getEntries());
                    entries.add(entry);
                    ((LootPoolAccessor) mainPool).setEntries(entries);
                } catch (RuntimeException e) {
                    GTCEu.LOGGER.error("Couldn't add {} to lootTable {}: {}", entry, name, e.getMessage());
                }
            }
        }

        if (rollValues.containsKey(event.getName())) {
            NumberProvider rangeAdd = rollValues.get(event.getName());
            NumberProvider range = mainPool.getRolls();
            // mainPool.setRolls(UniformGenerator.between(range.getMin() + rangeAdd.getMin(), range.getMax() +
            // rangeAdd.getMax())); TODO additional rolls
        }
    }

    public static void addItem(@NotNull ResourceKey<LootTable> lootTable, @NotNull ItemStack stack, int minAmount,
                               int maxAmount, int weight) {
        RandomWeightLootFunction lootFunction = new RandomWeightLootFunction(stack, minAmount, maxAmount);
        String modid = Objects.requireNonNull(BuiltInRegistries.ITEM.getKey(stack.getItem())).getNamespace();
        String entryName = createEntryName(stack, modid, weight, lootFunction);
        GTLootEntryItem itemEntry = new GTLootEntryItem(stack, weight, lootFunction, entryName);
        lootEntryItems.computeIfAbsent(lootTable.identifier(), $ -> new ArrayList<>()).add(itemEntry);
    }

    public static void addRolls(ResourceKey<LootTable> lootTable, int minAdd, int maxAdd) {
        rollValues.put(lootTable.identifier(), UniformGenerator.between(minAdd, maxAdd));
    }

    private static final ItemStackHashStrategy HASH_STRATEGY = ItemStackHashStrategy.comparingAllButCount();

    private static @NotNull String createEntryName(@NotNull ItemStack stack, @NotNull String modid, int weight,
                                                   @NotNull RandomWeightLootFunction function) {
        int hashCode = GTMath.hashInts(HASH_STRATEGY.hashCode(stack), modid.hashCode(), weight, function.getMinAmount(),
                function.getMaxAmount());
        return String.format("#%s:loot_%s", modid, hashCode);
    }

    private static class GTLootEntryItem extends LootItem {

        private final ItemStack stack;
        private final String entryName;

        public GTLootEntryItem(@NotNull ItemStack stack, int weight, LootItemFunction lootFunction,
                               @NotNull String entryName) {
            super(stack.typeHolder(), weight, 1, NO_CONDITIONS, List.of(lootFunction));
            this.stack = stack;
            this.entryName = entryName;
        }

        public void createItemStack(Consumer<ItemStack> stackConsumer, @NotNull LootContext lootContext) {
            stackConsumer.accept(this.stack.copy());
        }

        @Override
        public @NotNull String toString() {
            return "GTLootEntryItem{name=" + entryName + ", stack=" + stack.toString() + '}';
        }
    }

    public static class RandomWeightLootFunction extends LootItemConditionalFunction implements LootItemFunction {

        public static final MapCodec<RandomWeightLootFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
                .group(
                        ItemStack.CODEC.fieldOf("stack").forGetter(val -> val.stack),
                        ExtraCodecs.NON_NEGATIVE_INT.fieldOf("min").forGetter(val -> val.minAmount),
                        ExtraCodecs.NON_NEGATIVE_INT.fieldOf("max").forGetter(val -> val.maxAmount))
                .apply(instance, RandomWeightLootFunction::new));

        public static final MapCodec<RandomWeightLootFunction> TYPE = GTRegistries.register(
                BuiltInRegistries.LOOT_FUNCTION_TYPE, GTCEu.id("random_weight"), CODEC);

        private final ItemStack stack;
        @Getter
        private final int minAmount;
        @Getter
        private final int maxAmount;

        public RandomWeightLootFunction(@NotNull ItemStack stack, int minAmount, int maxAmount) {
            super(NO_CONDITIONS);
            Preconditions.checkArgument(minAmount <= maxAmount, "minAmount must be <= maxAmount");
            this.stack = stack;
            this.minAmount = minAmount;
            this.maxAmount = maxAmount;
        }

        public static void init() {}

        @Override
        public @NotNull MapCodec<? extends LootItemConditionalFunction> codec() {
            return TYPE;
        }

        @Override
        protected @NotNull ItemStack run(@NotNull ItemStack itemStack, @NotNull LootContext context) {
            if (stack.getDamageValue() != 0) {
                itemStack.setDamageValue(stack.getDamageValue());
            }
            if (!stack.isComponentsPatchEmpty()) {
                itemStack.applyComponents(stack.getComponentsPatch());
            }

            if (minAmount == maxAmount) {
                itemStack.setCount(minAmount);
                return itemStack;
            }

            int count = Math.min(minAmount + context.getRandom().nextInt(maxAmount - minAmount + 1),
                    stack.getMaxStackSize());
            itemStack.setCount(count);
            return itemStack;
        }
    }
}

package com.gregtechceu.gtceu.data.loot;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.core.mixins.LootPoolAccessor;
import com.gregtechceu.gtceu.utils.GTMath;
import com.gregtechceu.gtceu.utils.ItemStackHashStrategy;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

import com.google.common.base.Preconditions;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Getter;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public final class ChestGenHooks {

    private static final Map<ResourceLocation, List<GTLootEntryItem>> lootEntryItems = new Object2ObjectOpenHashMap<>();
    private static final Map<ResourceLocation, NumberProvider> rollValues = new Object2ObjectOpenHashMap<>();

    private static final LootItemCondition[] NO_CONDITIONS = new LootItemCondition[0];

    private ChestGenHooks() {}

    public static void init() {
        MinecraftForge.EVENT_BUS.register(ChestGenHooks.class);
    }

    @SubscribeEvent
    public static void onWorldLoad(@NotNull LootTableLoadEvent event) {
        LootPool mainPool = event.getTable().getPool("main");
        if (mainPool == null) return;

        ResourceLocation name = event.getName();
        if (lootEntryItems.containsKey(name)) {
            List<GTLootEntryItem> entryItems = lootEntryItems.get(name);
            for (GTLootEntryItem entry : entryItems) {
                if (ConfigHolder.INSTANCE.dev.debug) {
                    GTCEu.LOGGER.info("adding {} to lootTable {}", entry, name);
                }

                try {
                    LootPoolEntryContainer[] entries = ((LootPoolAccessor) mainPool).getEntries();
                    entries = ArrayUtils.add(entries, entry);
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

    public static void addItem(@NotNull ResourceLocation lootTable, @NotNull ItemStack stack, int minAmount,
                               int maxAmount, int weight) {
        RandomWeightLootFunction lootFunction = new RandomWeightLootFunction(stack, minAmount, maxAmount);
        String modid = Objects.requireNonNull(BuiltInRegistries.ITEM.getKey(stack.getItem())).getNamespace();
        String entryName = createEntryName(stack, modid, weight, lootFunction);
        GTLootEntryItem itemEntry = new GTLootEntryItem(stack, weight, lootFunction, entryName);
        lootEntryItems.computeIfAbsent(lootTable, $ -> new ArrayList<>()).add(itemEntry);
    }

    public static void addRolls(ResourceLocation tableLocation, int minAdd, int maxAdd) {
        rollValues.put(tableLocation, UniformGenerator.between(minAdd, maxAdd));
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
            super(stack.getItem(), weight, 1, NO_CONDITIONS, new LootItemFunction[] { lootFunction });
            this.stack = stack;
            this.entryName = entryName;
        }

        public void createItemStack(Consumer<ItemStack> stackConsumer, LootContext lootContext) {
            stackConsumer.accept(this.stack.copy());
        }

        @Override
        public @NotNull String toString() {
            return "GTLootEntryItem{name=" + entryName + ", stack=" + stack.toString() + '}';
        }
    }

    public static class RandomWeightLootFunction extends LootItemConditionalFunction implements LootItemFunction {

        public static final LootItemFunctionType TYPE = GTRegistries.register(BuiltInRegistries.LOOT_FUNCTION_TYPE,
                GTCEu.id("random_weight"), new LootItemFunctionType(new Serializer()));

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

        public static void init() {
            // Do nothing here. This just ensures that TYPE is being set immediately when called.
        }

        @Override
        public LootItemFunctionType getType() {
            return TYPE;
        }

        @Override
        protected ItemStack run(ItemStack itemStack, LootContext context) {
            if (stack.getDamageValue() != 0) {
                itemStack.setDamageValue(stack.getDamageValue());
            }
            CompoundTag tagCompound = stack.getTag();
            if (tagCompound != null) {
                itemStack.setTag(tagCompound.copy());
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

        public static class Serializer extends LootItemConditionalFunction.Serializer<RandomWeightLootFunction> {

            /**
             * Serialize the {@link SetItemCountFunction} by putting its data into the JsonObject.
             */
            public void serialize(JsonObject json, RandomWeightLootFunction setItemCountFunction,
                                  JsonSerializationContext serializationContext) {
                super.serialize(json, setItemCountFunction, serializationContext);
                json.add("min", serializationContext.serialize(setItemCountFunction.minAmount));
                json.add("max", serializationContext.serialize(setItemCountFunction.maxAmount));
                JsonObject stack = new JsonObject();
                stack.addProperty("item",
                        ForgeRegistries.ITEMS.getKey(setItemCountFunction.stack.getItem()).toString());
                stack.addProperty("count", setItemCountFunction.stack.getCount());
                if (setItemCountFunction.stack.hasTag())
                    stack.addProperty("nbt", setItemCountFunction.stack.getTag().toString());
                json.add("stack", stack);
            }

            public RandomWeightLootFunction deserialize(JsonObject object,
                                                        JsonDeserializationContext deserializationContext,
                                                        LootItemCondition[] conditions) {
                ItemStack stack = CraftingHelper.getItemStack(object.getAsJsonObject("stack"), true);
                int min = GsonHelper.getAsInt(object, "min");
                int max = GsonHelper.getAsInt(object, "max");
                return new RandomWeightLootFunction(stack, min, max);
            }
        }
    }
}

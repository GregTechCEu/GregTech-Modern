package com.gregtechceu.gtceu.api.sync_system.data_transformers.gtceu;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.sync_system.data_transformers.ValueTransformer;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import org.jetbrains.annotations.Nullable;

import java.util.IdentityHashMap;

public class ChanceCacheTransformer implements
                                    ValueTransformer<IdentityHashMap<RecipeCapability<?>, Object2IntMap<?>>> {

    @Override
    public Tag serializeNBT(IdentityHashMap<RecipeCapability<?>, Object2IntMap<?>> value,
                            TransformerContext<IdentityHashMap<RecipeCapability<?>, Object2IntMap<?>>> context) {
        CompoundTag chanceCache = new CompoundTag();

        value.forEach((cap, cache) -> {
            ListTag cacheTag = new ListTag();
            cache.forEach((content, chance) -> {
                CompoundTag compoundTag = new CompoundTag();
                var obj = cap.contentToNbt(content);
                compoundTag.put("entry", obj);
                compoundTag.putInt("cached_chance", chance);
                cacheTag.add(compoundTag);
            });
            chanceCache.put(cap.id.toString(), cacheTag);
        });

        return chanceCache;
    }

    @Override
    public @Nullable IdentityHashMap<RecipeCapability<?>, Object2IntMap<?>> deserializeNBT(Tag tag,
                                                                                           TransformerContext<IdentityHashMap<RecipeCapability<?>, Object2IntMap<?>>> context) {
        CompoundTag chanceCache = ValueTransformer.assertTagType(CompoundTag.class, tag,
                context);

        if (context.currentValue() == null) return null;
        for (String key : chanceCache.getAllKeys()) {
            RecipeCapability<?> cap = GTRegistries.RECIPE_CAPABILITIES.get(GTCEu.id(key));
            if (cap == null) continue;
            // noinspection rawtypes
            Object2IntMap map = context.currentValue().computeIfAbsent(cap,
                    RecipeCapability::makeChanceCache);

            ListTag chanceTag = chanceCache.getList(key, Tag.TAG_COMPOUND);
            for (int i = 0; i < chanceTag.size(); ++i) {
                CompoundTag chanceKey = chanceTag.getCompound(i);
                var entry = cap.serializer.fromNbt(chanceKey.get("entry"));
                int value = chanceKey.getInt("cached_chance");
                // noinspection unchecked
                map.put(entry, value);
            }
        }
        return context.currentValue();
    }

    @Override
    public void writeToPacket(FriendlyByteBuf buf, IdentityHashMap<RecipeCapability<?>, Object2IntMap<?>> value,
                              TransformerContext<IdentityHashMap<RecipeCapability<?>, Object2IntMap<?>>> context) {}

    @Override
    public @Nullable IdentityHashMap<RecipeCapability<?>, Object2IntMap<?>> readFromPacket(FriendlyByteBuf buf,
                                                                                           TransformerContext<IdentityHashMap<RecipeCapability<?>, Object2IntMap<?>>> context) {
        // no-op, client sync not required
        return null;
    }
}

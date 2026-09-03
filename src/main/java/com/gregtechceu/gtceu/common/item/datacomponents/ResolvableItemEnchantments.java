package com.gregtechceu.gtceu.common.item.datacomponents;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.item.EitherHolder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class ResolvableItemEnchantments implements TooltipProvider {

    // spotless:off
    public static final ResolvableItemEnchantments EMPTY = new ResolvableItemEnchantments(createLevelMap(), true);

    private static final Codec<Integer> LEVEL_CODEC = Codec.intRange(0, 255);
    private static final Codec<EitherHolder<Enchantment>> ENCHANTMENT_CODEC = EitherHolder.codec(Registries.ENCHANTMENT, Enchantment.CODEC);
    private static final Codec<Object2IntOpenCustomHashMap<EitherHolder<Enchantment>>> LEVELS_CODEC = Codec.unboundedMap(ENCHANTMENT_CODEC, LEVEL_CODEC)
            .xmap(ResolvableItemEnchantments::createLevelMap, Function.identity());
    private static final Codec<ResolvableItemEnchantments> FULL_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            LEVELS_CODEC.fieldOf("levels").forGetter(enchantments -> enchantments.enchantments),
            Codec.BOOL.optionalFieldOf("show_in_tooltip", true).forGetter(enchantments -> enchantments.showInTooltip)
    ).apply(instance, ResolvableItemEnchantments::new));
    public static final Codec<ResolvableItemEnchantments> CODEC = Codec.withAlternative(FULL_CODEC, LEVELS_CODEC, enchants -> new ResolvableItemEnchantments(enchants, true));

    private static final StreamCodec<RegistryFriendlyByteBuf, Object2IntOpenCustomHashMap<EitherHolder<Enchantment>>> LEVELS_STREAM_CODEC = ByteBufCodecs.map(
            ResolvableItemEnchantments::createLevelMap,
            EitherHolder.streamCodec(Registries.ENCHANTMENT, Enchantment.STREAM_CODEC),
            ByteBufCodecs.VAR_INT
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, ResolvableItemEnchantments> STREAM_CODEC = StreamCodec.composite(
            LEVELS_STREAM_CODEC, enchantments -> enchantments.enchantments,
            ByteBufCodecs.BOOL, enchantments -> enchantments.showInTooltip,
            ResolvableItemEnchantments::new
    );
    // spotless:on

    private final Object2IntOpenCustomHashMap<EitherHolder<Enchantment>> enchantments;
    private final boolean showInTooltip;

    ResolvableItemEnchantments(Object2IntOpenCustomHashMap<EitherHolder<Enchantment>> enchantments, boolean showInTooltip) {
        this.enchantments = enchantments;
        this.showInTooltip = showInTooltip;

        for (Object2IntMap.Entry<EitherHolder<Enchantment>> entry : enchantments.object2IntEntrySet()) {
            int i = entry.getIntValue();
            if (i < 0 || i > 255) {
                throw new IllegalArgumentException("Enchantment " + entry.getKey() + " has invalid level " + i);
            }
        }
    }

    public int getLevel(Holder<Enchantment> enchantment) {
        return this.enchantments.getInt(new EitherHolder<>(enchantment));
    }

    public int getLevel(ResourceKey<Enchantment> enchantment) {
        return this.enchantments.getInt(new EitherHolder<>(enchantment));
    }

    @Override
    public void addToTooltip(Item.TooltipContext context, Consumer<Component> tooltipAdder, TooltipFlag tooltipFlag) {
        if (!this.showInTooltip) {
            return;
        }
        HolderLookup.Provider registries = context.registries();
        HolderSet<Enchantment> tooltipOrder = getTooltipOrderOrEmpty(registries);

        for (Holder<Enchantment> enchantment : tooltipOrder) {
            int i = this.enchantments.getInt(new EitherHolder<>(enchantment));
            if (i > 0) {
                tooltipAdder.accept(Enchantment.getFullname(enchantment, i));
            }
        }

        for (Object2IntMap.Entry<EitherHolder<Enchantment>> entry : this.enchantments.object2IntEntrySet()) {
            EitherHolder<Enchantment> eitherHolder = entry.getKey();
            var optional = registries != null ? eitherHolder.unwrap(registries) : eitherHolder.holder();
            if (optional.isEmpty()) {
                continue;
            }
            Holder<Enchantment> enchantment = optional.get();

            if (!tooltipOrder.contains(enchantment)) {
                tooltipAdder.accept(Enchantment.getFullname(enchantment, entry.getIntValue()));
            }
        }
    }

    private static HolderSet<Enchantment> getTooltipOrderOrEmpty(@Nullable HolderLookup.Provider registries) {
        if (registries != null) {
            var tag = registries.lookupOrThrow(Registries.ENCHANTMENT).get(EnchantmentTags.TOOLTIP_ORDER);
            if (tag.isPresent()) return tag.get();
        }
        return HolderSet.direct();
    }

    public ResolvableItemEnchantments withTooltip(boolean showInTooltip) {
        return new ResolvableItemEnchantments(this.enchantments, showInTooltip);
    }

    public int size() {
        return this.enchantments.size();
    }

    public boolean isEmpty() {
        return this.enchantments.isEmpty();
    }

    public ItemEnchantments resolve(HolderLookup.Provider registries) {
        return resolve(registries.lookupOrThrow(Registries.ENCHANTMENT));
    }

    public ItemEnchantments resolve(HolderLookup.RegistryLookup<Enchantment> registry) {
        ItemEnchantments.Mutable resolvedEnchantments = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        Object2IntMaps.fastForEach(this.enchantments, entry -> {
            var enchantmentOptional = entry.getKey().holder().or(() -> registry.get(entry.getKey().key()));
            if (enchantmentOptional.isEmpty()) return;

            Holder<Enchantment> enchantment = enchantmentOptional.get();
            int level = entry.getIntValue();
            resolvedEnchantments.set(enchantment, level);
        });

        return resolvedEnchantments.toImmutable();
    }

    private Object2IntOpenHashMap<Holder<Enchantment>> resolveToMap(@Nullable HolderLookup.Provider registries) {
        Object2IntOpenHashMap<Holder<Enchantment>> resolvedEnchantments = new Object2IntOpenHashMap<>();
        Object2IntMaps.fastForEach(this.enchantments, entry -> {
            var enchantmentOptional = entry.getKey().holder()
                    .or(() -> Optional.ofNullable(registries).flatMap(reg -> reg.holder(entry.getKey().key())));
            if (enchantmentOptional.isEmpty()) return;

            Holder<Enchantment> enchantment = enchantmentOptional.get();
            int level = entry.getIntValue();
            resolvedEnchantments.put(enchantment, level);
        });

        return resolvedEnchantments;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;

        return other instanceof ResolvableItemEnchantments resolvableEnchantments &&
                this.showInTooltip == resolvableEnchantments.showInTooltip &&
                this.enchantments.equals(resolvableEnchantments.enchantments);
    }

    @Override
    public int hashCode() {
        int i = this.enchantments.hashCode();
        return 31 * i + Boolean.hashCode(this.showInTooltip);
    }

    @Override
    public String toString() {
        return "ResolvableItemEnchantments{enchantments=" + this.enchantments + ", showInTooltip=" + this.showInTooltip + "}";
    }

    public static class Mutable {
        private final Object2IntOpenCustomHashMap<EitherHolder<Enchantment>> enchantments = createLevelMap();
        private final boolean showInTooltip;

        public Mutable(ResolvableItemEnchantments enchantments) {
            this.enchantments.putAll(enchantments.enchantments);
            this.showInTooltip = enchantments.showInTooltip;
        }

        public Mutable() {
            this(ResolvableItemEnchantments.EMPTY);
        }

        public void set(Holder<Enchantment> enchantment, int level) {
            set(new EitherHolder<>(enchantment), level);
        }

        public void set(ResourceKey<Enchantment> enchantment, int level) {
            set(new EitherHolder<>(enchantment), level);
        }

        public void set(EitherHolder<Enchantment> enchantment, int level) {
            if (level <= 0) {
                this.enchantments.removeInt(enchantment);
            } else {
                this.enchantments.put(enchantment, Math.min(level, 255));
            }
        }

        public void upgrade(Holder<Enchantment> enchantment, int level) {
            upgrade(new EitherHolder<>(enchantment), level);
        }

        public void upgrade(ResourceKey<Enchantment> enchantment, int level) {
            upgrade(new EitherHolder<>(enchantment), level);
        }

        public void upgrade(EitherHolder<Enchantment> enchantment, int level) {
            if (level > 0) {
                this.enchantments.merge(enchantment, Math.min(level, 255), Integer::max);
            }
        }

        public void removeIf(Predicate<EitherHolder<Enchantment>> predicate) {
            this.enchantments.keySet().removeIf(predicate);
        }

        public int getLevel(Holder<Enchantment> enchantment) {
            return getLevel(new EitherHolder<>(enchantment));
        }

        public int getLevel(ResourceKey<Enchantment> enchantment) {
            return getLevel(new EitherHolder<>(enchantment));
        }

        public int getLevel(EitherHolder<Enchantment> enchantment) {
            return this.enchantments.getOrDefault(enchantment, 0);
        }

        public Set<EitherHolder<Enchantment>> keySet() {
            return this.enchantments.keySet();
        }

        public ResolvableItemEnchantments toImmutable() {
            return new ResolvableItemEnchantments(this.enchantments, this.showInTooltip);
        }
    }

    private static Object2IntOpenCustomHashMap<EitherHolder<Enchantment>> createLevelMap() {
        return new Object2IntOpenCustomHashMap<>(HashStrategy.INSTANCE);
    }

    private static Object2IntOpenCustomHashMap<EitherHolder<Enchantment>> createLevelMap(int size) {
        return new Object2IntOpenCustomHashMap<>(size, HashStrategy.INSTANCE);
    }

    private static Object2IntOpenCustomHashMap<EitherHolder<Enchantment>> createLevelMap(Map<EitherHolder<Enchantment>, Integer> map) {
        return new Object2IntOpenCustomHashMap<>(map, HashStrategy.INSTANCE);
    }

    private enum HashStrategy implements Hash.Strategy<EitherHolder<Enchantment>> {
        INSTANCE;

        @Override
        public int hashCode(@Nullable EitherHolder<Enchantment> o) {
            if (o == null) return 0;
            return o.key().hashCode();
        }

        @Override
        public boolean equals(@Nullable EitherHolder<Enchantment> a, @Nullable EitherHolder<Enchantment> b) {
            if (a == b) return true;
            if (a == null) return false;
            if (b == null) return false;

            return a.key().equals(b.key());
        }
    }
}

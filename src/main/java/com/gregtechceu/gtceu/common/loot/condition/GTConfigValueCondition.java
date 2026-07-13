package com.gregtechceu.gtceu.common.loot.condition;

import com.gregtechceu.gtceu.common.data.loot.GTLootConditions;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.toma.configuration.config.value.IConfigValue;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

import java.util.Optional;
import java.util.function.BooleanSupplier;

@Accessors(fluent = true)
public class GTConfigValueCondition implements LootItemCondition {

    // spotless:off
    public static final MapCodec<GTConfigValueCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("config_field").forGetter(GTConfigValueCondition::configField)
    ).apply(instance, GTConfigValueCondition::new));
    // spotless:on

    @Getter
    private final String configField;
    private final BooleanSupplier configValueGetter;

    protected GTConfigValueCondition(String configField) {
        this.configField = configField;

        Optional<IConfigValue<Boolean>> configEntry = ConfigHolder.INTERNAL_INSTANCE
                .getConfigValue(configField, boolean.class);
        this.configValueGetter = () -> configEntry.map(IConfigValue::get).orElse(false);
    }

    @Override
    public boolean test(LootContext lootContext) {
        return configValueGetter.getAsBoolean();
    }

    @Override
    public LootItemConditionType getType() {
        return GTLootConditions.CONFIG_VALUE.get();
    }

    public static LootItemCondition.Builder configEnabled(String configField) {
        return () -> new GTConfigValueCondition(configField);
    }

    /**
     * {@return Config value condition that checks the {@code worldgen.addLoot} config}
     */
    public static LootItemCondition.Builder addLootConfigEnabled() {
        return configEnabled("worldgen.addLoot");
    }

    /**
     * {@return Config value condition that checks the {@code worldgen.increaseDungeonLoot} config}
     */
    public static LootItemCondition.Builder increaseDungeonLootConfigEnabled() {
        return configEnabled("worldgen.increaseDungeonLoot");
    }
}

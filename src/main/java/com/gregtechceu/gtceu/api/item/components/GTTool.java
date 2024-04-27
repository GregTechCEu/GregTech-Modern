package com.gregtechceu.gtceu.api.item.components;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Optional;

public record GTTool(
    Optional<Float> toolSpeed,
    Optional<Float> attackDamage,
    Optional<Integer> enchantability,
    Optional<Integer> harvestLevel,
    Optional<Integer> lastCraftingUse) {

    public static final Codec<GTTool> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.FLOAT.optionalFieldOf("tool_speed").forGetter(GTTool::toolSpeed),
        Codec.FLOAT.optionalFieldOf("attack_damage").forGetter(GTTool::attackDamage),
        Codec.INT.optionalFieldOf("enchantability").forGetter(GTTool::enchantability),
        Codec.INT.optionalFieldOf("harvest_level").forGetter(GTTool::harvestLevel),
        Codec.INT.optionalFieldOf("last_crafting_use").forGetter(GTTool::lastCraftingUse)
    ).apply(instance, GTTool::new));
    public static final StreamCodec<ByteBuf, GTTool> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.optional(ByteBufCodecs.FLOAT), GTTool::toolSpeed,
        ByteBufCodecs.optional(ByteBufCodecs.FLOAT), GTTool::attackDamage,
        ByteBufCodecs.optional(ByteBufCodecs.INT), GTTool::enchantability,
        ByteBufCodecs.optional(ByteBufCodecs.INT), GTTool::harvestLevel,
        ByteBufCodecs.optional(ByteBufCodecs.INT), GTTool::lastCraftingUse,
        GTTool::new
    );

    public GTTool() {
        this(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
    }

    public GTTool setToolSpeed(float toolSpeed) {
        return new GTTool(Optional.of(toolSpeed), this.attackDamage, this.enchantability, this.harvestLevel, this.lastCraftingUse);
    }

    public GTTool setAttackDamage(float attackDamage) {
        return new GTTool(this.toolSpeed, Optional.of(attackDamage), this.enchantability, this.harvestLevel, this.lastCraftingUse);
    }

    public GTTool setEnchantability(int enchantability) {
        return new GTTool(this.toolSpeed, this.attackDamage, Optional.of(enchantability), this.harvestLevel, this.lastCraftingUse);
    }

    public GTTool setHarvestLevel(int harvestLevel) {
        return new GTTool(this.toolSpeed, this.attackDamage, this.enchantability, Optional.of(harvestLevel), this.lastCraftingUse);
    }

    public GTTool setLastCraftingUse(int lastCraftingUse) {
        return new GTTool(this.toolSpeed, this.attackDamage, this.enchantability, this.harvestLevel, Optional.of(lastCraftingUse));
    }
}

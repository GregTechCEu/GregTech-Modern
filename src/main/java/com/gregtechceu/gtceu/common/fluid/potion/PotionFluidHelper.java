package com.gregtechceu.gtceu.common.fluid.potion;

import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.common.data.GTFluids;
import com.gregtechceu.gtceu.core.mixins.forge.StrictNBTIngredientAccessor;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.GTMath;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Tuple;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class PotionFluidHelper {

    public static final int BOTTLE_AMOUNT = FluidType.BUCKET_VOLUME / 4;
    public static final int MB_PER_RECIPE = BOTTLE_AMOUNT * 3;

    public static Pair<FluidStack, ItemStack> emptyPotion(ItemStack stack, boolean simulate) {
        FluidStack fluid = getFluidFromPotionItem(stack, BOTTLE_AMOUNT);
        if (!simulate)
            stack.shrink(1);
        return Pair.of(fluid, new ItemStack(Items.GLASS_BOTTLE));
    }

    public static FluidIngredient potionIngredient(Potion potion, int amount) {
        FluidStack stack = PotionFluidHelper
                .getFluidFromPotionItem(PotionUtils.setPotion(new ItemStack(Items.POTION), potion), amount);
        stack.setAmount(amount);
        return FluidIngredient.of(stack);
    }

    public static FluidIngredient getPotionFluidIngredientFrom(Ingredient potion, int amount) {
        if (potion instanceof StrictNBTIngredientAccessor strict) {
            return FluidIngredient.fromValue(new FluidIngredient.FluidValue(GTFluids.POTION.get()),
                    amount, strict.getStack().getTag());
        }

        List<FluidStack> fluids = new ArrayList<>();
        for (ItemStack stack : potion.getItems()) {
            FluidStack fluidStack = getFluidFromPotionItem(stack, amount);
            if (!fluidStack.isEmpty()) {
                fluids.add(fluidStack);
            }
        }
        return FluidIngredient.of(fluids);
    }

    public static FluidStack getFluidFromPotionItem(ItemStack stack, int amount) {
        Potion potion = PotionUtils.getPotion(stack);
        if (potion == Potions.EMPTY) {
            return FluidStack.EMPTY;
        }
        List<MobEffectInstance> list = PotionUtils.getCustomEffects(stack);
        if (potion == Potions.WATER && list.isEmpty())
            return new FluidStack(Fluids.WATER, amount);
        return PotionFluid.withEffects(amount, potion, list);
    }

    public static FluidStack getFluidFromPotion(Potion potion, int amount) {
        if (potion == Potions.WATER)
            return new FluidStack(Fluids.WATER, amount);
        return PotionFluid.of(amount, potion);
    }

    public static ItemStack fillBottle(ItemStack stack, FluidStack availableFluid) {
        CompoundTag tag = availableFluid.getOrCreateTag();
        if (stack.is(Items.GLASS_BOTTLE)) {
            int count = stack.getCount();
            CompoundTag stackTag = stack.getTag();
            stack = new ItemStack(Items.POTION);
            stack.setCount(count);
            stack.setTag(stackTag);
        }
        PotionUtils.setPotion(stack, PotionUtils.getPotion(tag));
        PotionUtils.setCustomEffects(stack, PotionUtils.getCustomEffects(tag));
        return stack;
    }

    // Modified version of PotionUtils#addPotionTooltip
    @OnlyIn(Dist.CLIENT)
    public static void addPotionTooltip(FluidStack fs, Consumer<Component> tooltip) {
        List<MobEffectInstance> list = PotionUtils.getAllEffects(fs.getOrCreateTag());
        List<Tuple<String, AttributeModifier>> modifiers = Lists.newArrayList();
        if (list.isEmpty()) {
            tooltip.accept(Component.translatable("effect.none").withStyle(ChatFormatting.GRAY));
        } else {
            for (MobEffectInstance effectInstance : list) {
                MutableComponent name = Component.translatable(effectInstance.getDescriptionId());
                MobEffect effect = effectInstance.getEffect();
                Map<Attribute, AttributeModifier> map = effect.getAttributeModifiers();
                if (!map.isEmpty()) {
                    for (Map.Entry<Attribute, AttributeModifier> entry : map.entrySet()) {
                        AttributeModifier modifier = entry.getValue();
                        AttributeModifier mutated = new AttributeModifier(modifier.getName(),
                                effect.getAttributeModifierValue(effectInstance.getAmplifier(), modifier),
                                modifier.getOperation());
                        modifiers.add(new Tuple<>(
                                entry.getKey().getDescriptionId(),
                                mutated));
                    }
                }

                if (effectInstance.getAmplifier() > 0) {
                    name = Component.translatable("potion.withAmplifier", name,
                            Component.translatable("potion.potency." + effectInstance.getAmplifier()));;
                }

                if (!effectInstance.endsWithin(20)) {
                    name = Component.translatable("potion.withDuration", name,
                            MobEffectUtil.formatDuration(effectInstance, 1.0f));

                }

                tooltip.accept(name.withStyle(effect.getCategory()
                        .getTooltipFormatting()));
            }
        }

        if (!modifiers.isEmpty()) {
            tooltip.accept(Component.empty());
            tooltip.accept(Component.translatable("potion.whenDrank").withStyle(ChatFormatting.DARK_PURPLE));

            for (Tuple<String, AttributeModifier> tuple : modifiers) {
                AttributeModifier modifier2 = tuple.getB();
                double d0 = modifier2.getAmount();
                double d1;
                if (modifier2.getOperation() != AttributeModifier.Operation.MULTIPLY_BASE &&
                        modifier2.getOperation() != AttributeModifier.Operation.MULTIPLY_TOTAL) {
                    d1 = modifier2.getAmount();
                } else {
                    d1 = modifier2.getAmount() * 100.0D;
                }

                if (d0 > 0.0D) {
                    tooltip.accept(Component.translatable(
                            "attribute.modifier.plus." + modifier2.getOperation()
                                    .toValue(),
                            ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(d1),
                            Component.translatable(tuple.getA()))
                            .withStyle(ChatFormatting.BLUE));
                } else if (d0 < 0.0D) {
                    d1 = d1 * -1.0D;
                    tooltip.accept(Component.translatable(
                            "attribute.modifier.take." + modifier2.getOperation()
                                    .toValue(),
                            ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(d1),
                            Component.translatable(tuple.getA()))
                            .withStyle(ChatFormatting.RED));
                }
            }
        }
    }

    public static Component formatDuration(MobEffectInstance effect) {
        if (effect.isInfiniteDuration()) {
            return Component.translatable("effect.duration.infinite");
        } else {
            int time = effect.getDuration();
            Instant start = Instant.now();
            Instant max = Instant.now().plusSeconds(time / 20);
            Duration durationMax = Duration.between(start, max);

            Component unit;

            if (durationMax.getSeconds() <= 60) {
                time = GTMath.saturatedCast(durationMax.getSeconds());
                unit = Component.translatable("item.gtceu.battery.charge_unit.second");
            } else if (durationMax.toMinutes() <= 60) {
                time = GTMath.saturatedCast(durationMax.toMinutes());
                unit = Component.translatable("item.gtceu.battery.charge_unit.minute");
            } else {
                time = GTMath.saturatedCast(durationMax.toHours());
                unit = Component.translatable("item.gtceu.battery.charge_unit.hour");
            }

            return Component.literal(FormattingUtil.formatNumbers(time)).append(unit);
        }
    }
}

package com.gregtechceu.gtceu.common.fluid.potion;

import com.gregtechceu.gtceu.common.data.GTFluids;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.GTMath;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.crafting.DataComponentIngredient;
import net.neoforged.neoforge.common.util.AttributeUtil;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.crafting.DataComponentFluidIngredient;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class PotionFluidHelper {

    private static final Component NO_EFFECT = Component.translatable("effect.none").withStyle(ChatFormatting.GRAY);

    public static final int BOTTLE_AMOUNT = FluidType.BUCKET_VOLUME / 4;
    public static final int MB_PER_RECIPE = BOTTLE_AMOUNT * 3;

    public static Pair<FluidStack, ItemStack> emptyPotion(ItemStack stack, boolean simulate) {
        FluidStack fluid = getFluidFromPotionItem(stack, BOTTLE_AMOUNT);
        if (!simulate)
            stack.shrink(1);
        return Pair.of(fluid, new ItemStack(Items.GLASS_BOTTLE));
    }

    public static FluidIngredient potionIngredient(Holder<Potion> potion, int amount) {
        FluidStack stack = PotionFluidHelper
                .getFluidFromPotionItem(PotionContents.createItemStack(Items.POTION, potion), amount);
        stack.setAmount(amount);
        return FluidIngredient.of(stack);
    }

    public static SizedFluidIngredient getPotionFluidIngredientFrom(Ingredient potion, int amount) {
        if (potion.getCustomIngredient() instanceof DataComponentIngredient component && component.isStrict()) {
            return new SizedFluidIngredient(DataComponentFluidIngredient.of(false,
                    DataComponents.POTION_CONTENTS,
                    component.getItems().findFirst().orElse(ItemStack.EMPTY)
                            .getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY),
                    GTFluids.POTION.get()), amount);
        }

        List<FluidStack> fluids = new ArrayList<>();
        for (ItemStack stack : potion.getItems()) {
            FluidStack fluidStack = getFluidFromPotionItem(stack, amount);
            if (!fluidStack.isEmpty()) {
                fluids.add(fluidStack);
            }
        }
        return new SizedFluidIngredient(FluidIngredient.of(fluids.toArray(FluidStack[]::new)), amount);
    }

    public static FluidStack getFluidFromPotionItem(ItemStack stack, int amount) {
        PotionContents potion = stack.get(DataComponents.POTION_CONTENTS);
        if (potion == null || potion == PotionContents.EMPTY) {
            return FluidStack.EMPTY;
        }
        List<MobEffectInstance> list = potion.customEffects();
        if (potion.is(Potions.WATER) && list.isEmpty())
            return new FluidStack(Fluids.WATER, amount);
        return PotionFluid.of(amount, potion);
    }

    public static FluidStack getFluidFromPotion(Holder<Potion> potion, int amount) {
        if (potion == Potions.WATER)
            return new FluidStack(Fluids.WATER, amount);
        return PotionFluid.of(amount, potion);
    }

    public static ItemStack fillBottle(ItemStack stack, FluidStack availableFluid) {
        var contents = availableFluid.get(DataComponents.POTION_CONTENTS);
        if (stack.is(Items.GLASS_BOTTLE)) {
            int count = stack.getCount();
            var componentsPatch = stack.getComponentsPatch();
            stack = new ItemStack(Items.POTION);
            stack.setCount(count);
            stack.applyComponents(componentsPatch);
        }
        stack.set(DataComponents.POTION_CONTENTS, contents);
        return stack;
    }

    // Modified version of PotionUtils#addPotionTooltip
    @OnlyIn(Dist.CLIENT)
    public static void addPotionTooltip(FluidStack fs, Consumer<Component> tooltipAdder, Item.TooltipContext context) {
        var contents = fs.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        Iterable<MobEffectInstance> effects = contents.getAllEffects();

        List<Pair<Holder<Attribute>, AttributeModifier>> list = Lists.newArrayList();
        boolean noEffects = true;
        for (MobEffectInstance mobeffectinstance : effects) {
            noEffects = false;
            MutableComponent mutablecomponent = Component.translatable(mobeffectinstance.getDescriptionId());
            Holder<MobEffect> holder = mobeffectinstance.getEffect();
            holder.value().createModifiers(mobeffectinstance.getAmplifier(),
                    (p_331556_, p_330860_) -> list.add(new Pair<>(p_331556_, p_330860_)));
            if (mobeffectinstance.getAmplifier() > 0) {
                mutablecomponent = Component.translatable(
                        "potion.withAmplifier", mutablecomponent,
                        Component.translatable("potion.potency." + mobeffectinstance.getAmplifier()));
            }

            if (!mobeffectinstance.endsWithin(20)) {
                mutablecomponent = Component.translatable(
                        "potion.withDuration", mutablecomponent,
                        MobEffectUtil.formatDuration(mobeffectinstance, 1.0f, context.tickRate()));
            }

            tooltipAdder.accept(mutablecomponent.withStyle(holder.value().getCategory().getTooltipFormatting()));
        }

        if (noEffects) {
            tooltipAdder.accept(NO_EFFECT);
        }

        if (!list.isEmpty()) {
            tooltipAdder.accept(CommonComponents.EMPTY);
            tooltipAdder.accept(Component.translatable("potion.whenDrank").withStyle(ChatFormatting.DARK_PURPLE));

            // Neo: Override handling of potion attribute tooltips to support IAttributeExtension
            AttributeUtil.addPotionTooltip(list, tooltipAdder);
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

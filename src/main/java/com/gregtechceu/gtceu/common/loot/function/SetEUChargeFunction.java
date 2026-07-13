package com.gregtechceu.gtceu.common.loot.function;

import com.google.gson.*;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.common.data.loot.GTLootFunctions;
import com.gregtechceu.gtceu.utils.codec.GTCodecUtils;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.*;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.LootModifier;

public class SetEUChargeFunction extends LootItemConditionalFunction {

   // spotless:off
   public static final MapCodec<SetEUChargeFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
           LootModifier.LOOT_CONDITIONS_CODEC.fieldOf("conditions").forGetter(fn -> fn.predicates),
           GTCodecUtils.NON_NEGATIVE_LONG.fieldOf("charge").forGetter(fn -> fn.charge)
   ).apply(instance, SetEUChargeFunction::new));
   // spotless:on

   protected final long charge;

   protected SetEUChargeFunction(LootItemCondition[] conditions, long charge) {
      super(conditions);
      this.charge = charge;
   }

   @Override
   public LootItemFunctionType getType() {
      return GTLootFunctions.SET_EU_CHARGE.get();
   }

   @Override
   public ItemStack run(ItemStack stack, LootContext context) {
      IElectricItem electricItem = GTCapabilityHelper.getElectricItem(stack);
      if (electricItem == null) {
         return stack;
      }
      electricItem.charge(this.charge, Integer.MAX_VALUE, true, false);
      return stack;
   }

   public static LootItemConditionalFunction.Builder<?> setCharge(long charge) {
      return simpleBuilder(conditions -> new SetEUChargeFunction(conditions, charge));
   }
}

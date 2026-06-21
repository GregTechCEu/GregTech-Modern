package com.gregtechceu.gtceu.integration.forestry.items;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import forestry.api.apiculture.IBeeHousing;
import forestry.api.apiculture.IBeeModifier;
import forestry.api.apiculture.genetics.IBee;
import forestry.api.apiculture.genetics.IBeeSpecies;
import forestry.api.apiculture.hives.IHiveFrame;
import forestry.api.genetics.IGenome;
import forestry.api.genetics.IMutation;
import forestry.core.items.ItemForestry;

import java.text.DecimalFormat;
import java.util.List;


//todo use JB nullability
import javax.annotation.Nullable;

// CREDIT GOES TO: christopherwalkerml |
// https://github.com/christopherwalkerml/MoBees/blob/main/src/main/java/com/noodlepfp/mobees/item/MoreBeesItemHiveFrame.java
public class GTHiveFrameItem extends ItemForestry implements IHiveFrame {

    private final Modifier beeModifier;


    //todo make the other fields/setters' names more reasonable while at it.
    public GTHiveFrameItem(GTItemHiveFrameBuilder builder) {
        super((new Item.Properties()).durability(builder.durability));
        this.beeModifier = new Modifier(builder.ageMult,
                builder.speedMult,
                builder.pollinationMult,
                builder.decayMult,
                builder.mutationMult,
                builder.isRainproof,
                builder.isAlwaysSunny,
                builder.isHellish);
    }

    @Override
    public ItemStack frameUsed(IBeeHousing housing, ItemStack frame, IBee queen, int wear) {
        return frame.hurt(wear, housing.getWorldObj().getRandom(), null) ? ItemStack.EMPTY : frame;
    }

    @Override
    public void appendHoverText(ItemStack stack, @org.jetbrains.annotations.Nullable Level world,
                                List<Component> tooltip, TooltipFlag advanced) {
        super.appendHoverText(stack, world, tooltip, advanced);
        DecimalFormat FORMAT = new DecimalFormat("#.##");

        if (beeModifier.speedMult != 1) {
            tooltip.add(
                    Component.translatable("item.gtceu.bee.modifier.speed_multiplier").withStyle(ChatFormatting.GRAY)
                            .append(": ")
                            .append(Component.literal(FORMAT.format(beeModifier.speedMult) + "x")
                                    .withStyle(beeModifier.speedMult > 1 ? ChatFormatting.GREEN : ChatFormatting.RED)));
        }
        if (beeModifier.decayMult != 1) {
            tooltip.add(
                    Component.translatable("item.gtceu.bee.modifier.decay_multiplier").withStyle(ChatFormatting.GRAY)
                            .append(": ")
                            .append(Component.literal(FORMAT.format(beeModifier.decayMult) + "x")
                                    .withStyle(beeModifier.decayMult > 1 ? ChatFormatting.RED : ChatFormatting.GREEN)));
        }
        if (beeModifier.pollinationMult != 1) {
            tooltip.add(Component.translatable("item.gtceu.bee.modifier.pollination_multiplier")
                    .withStyle(ChatFormatting.GRAY)
                    .append(": ")
                    .append(Component.literal(FORMAT.format(beeModifier.pollinationMult) + "x")
                            .withStyle(beeModifier.pollinationMult > 1 ? ChatFormatting.GREEN : ChatFormatting.RED)));
        }
        if (beeModifier.mutationMult != 1) {
            tooltip.add(Component.translatable("item.gtceu.bee.modifier.mutation_multiplier")
                    .withStyle(ChatFormatting.GRAY)
                    .append(": ")
                    .append(Component.literal(FORMAT.format(beeModifier.mutationMult) + "x")
                            .withStyle(beeModifier.mutationMult > 1 ? ChatFormatting.GREEN : ChatFormatting.RED)));
        }
        if (beeModifier.ageMult != 1) {
            tooltip.add(
                    Component.translatable("item.gtceu.bee.modifier.aging_multiplier").withStyle(ChatFormatting.GRAY)
                            .append(": ")
                            .append(Component.literal(FORMAT.format(beeModifier.ageMult) + "x")
                                    .withStyle(beeModifier.ageMult > 1 ? ChatFormatting.RED : ChatFormatting.GREEN)));
        }


        //todo Don't hardcode a literal "true" string in the tooltip pls
        if (beeModifier.isRainproof) {
            tooltip.add(Component.translatable("item.gtceu.bee.modifier.is_rainproof").withStyle(ChatFormatting.GRAY)
                    .append(": ").append(Component.literal("true").withStyle(ChatFormatting.GREEN)));
        }
        if (beeModifier.isAlwaysSunny) {
            tooltip.add(Component.translatable("item.gtceu.bee.modifier.is_always_sunny").withStyle(ChatFormatting.GRAY)
                    .append(": ").append(Component.literal("true").withStyle(ChatFormatting.GREEN)));
        }
        if (beeModifier.isHellish) {
            tooltip.add(Component.translatable("item.gtceu.bee.modifier.is_hellish").withStyle(ChatFormatting.GRAY)
                    .append(": ").append(Component.literal("true").withStyle(ChatFormatting.GREEN)));
        }
        if (!stack.isDamaged()) {
            tooltip.add(Component.translatable("item.forestry.durability", stack.getMaxDamage()));
        }
    }

    public IBeeModifier getBeeModifier(ItemStack frame) {
        return this.beeModifier;
    }

    private class Modifier implements IBeeModifier {

        private final float ageMult;
        private final float speedMult;
        private final float pollinationMult;
        private final float decayMult;
        private final float mutationMult;
        private final boolean isRainproof;
        private final boolean isAlwaysSunny;
        private final boolean isHellish;

        public Modifier(float ageMult, float speedMult, float pollinationMult, float decayMult, float mutationMult,
                        boolean isRainproof, boolean isAlwaysSunny, boolean isHellish) {
            this.ageMult = ageMult;
            this.speedMult = speedMult;
            this.pollinationMult = pollinationMult;
            this.decayMult = decayMult;
            this.mutationMult = mutationMult;
            this.isRainproof = isRainproof;
            this.isAlwaysSunny = isAlwaysSunny;
            this.isHellish = isHellish;
        }

        @Override
        public float modifyMutationChance(IGenome genome, IGenome mate, IMutation<IBeeSpecies> mutation,
                                          float currentChance) {
            // mult cap is the base mutation chance to the power of 3. ie. 0.06 -> 0.09 -> 0.135 -> 0.203 -> 0.304,
            // capped at 0.5
            float multCap = Math.min((float) (mutation.getChance() * (Math.pow(1.5, 4))), 0.5f);
            return Math.min(currentChance * mutationMult, multCap);
        }

        @Override
        public float modifyAging(IGenome genome, @Nullable IGenome mate, float currentAging) {
            return currentAging * ageMult;
        }

        @Override
        public float modifyProductionSpeed(IGenome genome, float currentSpeed) {
            return currentSpeed * speedMult;
        }

        @Override
        public float modifyPollination(IGenome genome, float currentPollination) {
            return currentPollination * pollinationMult;
        }

        @Override
        public float modifyGeneticDecay(IGenome genome, float currentDecay) {
            return currentDecay * decayMult;
        }

        @Override
        public boolean isSealed() {
            return isRainproof;
        }

        @Override
        public boolean isSunlightSimulated() {
            return isAlwaysSunny;
        }

        @Override
        public boolean isHellish() {
            return isHellish;
        }
    }

    public static class GTItemHiveFrameBuilder {

        // required params
        private int durability = 64;

        // optional params
        private float ageMult = 1;
        private float speedMult = 1;
        private float pollinationMult = 1;
        private float decayMult = 1;
        private float mutationMult = 1;
        private boolean isRainproof = false;
        private boolean isAlwaysSunny = false;
        private boolean isHellish = false;

        public GTItemHiveFrameBuilder(int maxDmg) {
            this.durability = maxDmg;
        }

        public GTItemHiveFrameBuilder setAgeMult(float ageMult) {
            this.ageMult = ageMult;
            return this;
        }

        public GTItemHiveFrameBuilder setSpeedMult(float speedMult) {
            this.speedMult = speedMult;
            return this;
        }

        public GTItemHiveFrameBuilder setPollinationMult(float pollinationMult) {
            this.pollinationMult = pollinationMult;
            return this;
        }

        public GTItemHiveFrameBuilder setDecayMult(float decayMult) {
            this.decayMult = decayMult;
            return this;
        }

        public GTItemHiveFrameBuilder setMutationMult(float mutationMult) {
            this.mutationMult = mutationMult;
            return this;
        }

        public GTItemHiveFrameBuilder setIsRainproof(boolean isRainproof) {
            this.isRainproof = isRainproof;
            return this;
        }

        public GTItemHiveFrameBuilder setIsAlwaysSunny(boolean isAlwaysSunny) {
            this.isAlwaysSunny = isAlwaysSunny;
            return this;
        }

        public GTItemHiveFrameBuilder setIsHellish(boolean isHellish) {
            this.isHellish = isHellish;
            return this;
        }

        public GTHiveFrameItem build() {
            return new GTHiveFrameItem(this);
        }
    }
}

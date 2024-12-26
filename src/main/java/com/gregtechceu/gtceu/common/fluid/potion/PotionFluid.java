package com.gregtechceu.gtceu.common.fluid.potion;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.common.data.GTFluids;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public class PotionFluid extends ForgeFlowingFluid {

    public PotionFluid(Properties properties) {
        super(properties
                .bucket(() -> Items.AIR)
                .block(() -> (LiquidBlock) Blocks.WATER));
        registerDefaultState(getStateDefinition().any().setValue(LEVEL, 7));
    }

    @Override
    protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder) {
        super.createFluidStateDefinition(builder);
        builder.add(LEVEL);
    }

    public static FluidStack of(int amount, Potion potion) {
        FluidStack fluidStack = new FluidStack(GTFluids.POTION.get()
                .getSource(), amount);
        addPotionToFluidStack(fluidStack, potion);
        return fluidStack;
    }

    public static FluidStack withEffects(int amount, Potion potion, List<MobEffectInstance> customEffects) {
        FluidStack fluidStack = of(amount, potion);
        appendEffects(fluidStack, customEffects);
        return fluidStack;
    }

    public static FluidStack addPotionToFluidStack(FluidStack fluidStack, Potion potion) {
        ResourceLocation resourcelocation = BuiltInRegistries.POTION.getKey(potion);
        if (potion == Potions.EMPTY) {
            fluidStack.removeChildTag("Potion");
            return fluidStack;
        }
        fluidStack.getOrCreateTag()
                .putString("Potion", resourcelocation.toString());
        return fluidStack;
    }

    public static FluidStack appendEffects(FluidStack fluidStack, Collection<MobEffectInstance> customEffects) {
        if (customEffects.isEmpty())
            return fluidStack;
        CompoundTag tag = fluidStack.getOrCreateTag();
        ListTag effects = tag.getList("CustomPotionEffects", 9);
        for (MobEffectInstance effect : customEffects)
            effects.add(effect.save(new CompoundTag()));
        tag.put("CustomPotionEffects", effects);
        return fluidStack;
    }

    @Override
    public boolean isSource(FluidState state) {
        return this == GTFluids.POTION.get().getSource();
    }

    @Override
    public int getAmount(FluidState state) {
        return state.getValue(LEVEL);
    }

    public static class PotionFluidType extends FluidType {

        private static final ResourceLocation texture = GTCEu.id("block/fluids/fluid.potion");

        /**
         * Default constructor.
         *
         * @param properties the general properties of the fluid type
         */
        public PotionFluidType(Properties properties, ResourceLocation still, ResourceLocation flow) {
            super(properties);
        }

        @Override
        public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
            consumer.accept(new IClientFluidTypeExtensions() {

                @Override
                public ResourceLocation getStillTexture() {
                    return texture;
                }

                @Override
                public ResourceLocation getFlowingTexture() {
                    return texture;
                }

                @Override
                public int getTintColor(FluidStack stack) {
                    CompoundTag tag = stack.getOrCreateTag();
                    return PotionUtils.getColor(PotionUtils.getAllEffects(tag)) | 0xff000000;
                }
            });
        }

        @Override
        public String getDescriptionId(FluidStack stack) {
            CompoundTag tag = stack.getOrCreateTag();
            return PotionUtils.getPotion(tag)
                    .getName(Items.POTION.getDescriptionId() + ".effect.");
        }
    }
}

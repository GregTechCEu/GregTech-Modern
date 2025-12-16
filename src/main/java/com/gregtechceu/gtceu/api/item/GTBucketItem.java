package com.gregtechceu.gtceu.api.item;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.fluids.GTFluid;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKey;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.wrappers.FluidBucketWrapper;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Supplier;

import javax.annotation.Nullable;

public class GTBucketItem extends BucketItem {

    final Material material;
    final String langKey;

    public GTBucketItem(Supplier<? extends Fluid> fluid, Properties properties, Material material, String langKey) {
        super(fluid, properties);
        this.material = material;
        this.langKey = langKey;
    }

    public static int color(ItemStack itemStack, int index) {
        if (itemStack.getItem() instanceof GTBucketItem item) {
            if (index == 1) {
                return IClientFluidTypeExtensions.of(item.getFluid()).getTintColor();
            }
        }
        return -1;
    }

    public ICapabilityProvider initCapabilities(@NotNull ItemStack stack, @Nullable CompoundTag nbt) {
        return this.getClass() == GTBucketItem.class ? new FluidBucketWrapper(stack) :
                super.initCapabilities(stack, nbt);
    }

    @Override
    public String getDescriptionId() {
        return "item.gtceu.bucket";
    }

    @Override
    public Component getDescription() {
        Component materialName = material.getLocalizedName();
        return Component.translatable("item.gtceu.bucket", Component.translatable(this.langKey, materialName));
    }

    @Override
    public Component getName(ItemStack stack) {
        return this.getDescription();
    }

    @Override
    public int getBurnTime(ItemStack itemStack, @Nullable RecipeType<?> recipeType) {
        var property = material.getProperty(PropertyKey.FLUID);
        if (property != null) {
            var fluid = material.getFluid();
            if (fluid instanceof GTFluid gtFluid) {
                return gtFluid.getBurnTime();
            }
        }
        return -1;
    }

    @Override
    public boolean emptyContents(@Nullable Player player, Level level, BlockPos pos,
                                 @Nullable BlockHitResult result,
                                 @Nullable ItemStack container) {
        if (!(this.getFluid() instanceof FlowingFluid)) return false;

        BlockState blockstate = level.getBlockState(pos);
        Block block = blockstate.getBlock();
        boolean canReplace = blockstate.canBeReplaced(this.getFluid());
        boolean canPlace = blockstate.isAir() || canReplace ||
                block instanceof LiquidBlockContainer lbc &&
                        lbc.canPlaceLiquid(level, pos, blockstate, this.getFluid());

        if (!canPlace) {
            return result != null && this.emptyContents(player, level,
                    result.getBlockPos().relative(result.getDirection()), null, container);
        }

        var fluidType = this.getFluid().getFluidType();
        Optional<FluidStack> containedFluidStack = Optional.ofNullable(container).flatMap(FluidUtil::getFluidContained);
        if (containedFluidStack.isPresent() &&
                fluidType.isVaporizedOnPlacement(level, pos, containedFluidStack.get())) {
            fluidType.onVaporize(player, level, pos, containedFluidStack.get());
            return true;
        }

        if (doesFluidVaporize(material, level)) {
            int i = pos.getX();
            int j = pos.getY();
            int k = pos.getZ();
            level.playSound(player, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F,
                    2.6F + (level.random.nextFloat() - level.random.nextFloat()) * 0.8F);

            for (int l = 0; l < 8; ++l) {
                double xi = i + GTValues.RNG.nextDouble();
                double xj = j + GTValues.RNG.nextDouble();
                double xk = k + GTValues.RNG.nextDouble();
                level.addParticle(ParticleTypes.LARGE_SMOKE, xi, xj, xk, 0.0D, 0.0D, 0.0D);
            }
            return true;
        }

        if (block instanceof LiquidBlockContainer blockContainer &&
                blockContainer.canPlaceLiquid(level, pos, blockstate, getFluid())) {
            var flowingFluid = ((FlowingFluid) this.getFluid());
            blockContainer.placeLiquid(level, pos, blockstate, flowingFluid.getSource(false));
            this.playEmptySound(player, level, pos);
            return true;
        } else {
            if (!level.isClientSide && canReplace && !blockstate.liquid()) {
                level.destroyBlock(pos, true);
            }

            var fluidBlockState = material.getFluid().defaultFluidState().createLegacyBlock();
            if (hasFluidBlock(material) && level.setBlock(pos, fluidBlockState, Block.UPDATE_ALL_IMMEDIATE) &&
                    fluidBlockState.getFluidState().isSource()) {
                this.playEmptySound(player, level, pos);
                return true;
            }
        }
        return false;
    }

    private static boolean hasFluidBlock(Material mat) {
        var fluidStorage = mat.getProperty(PropertyKey.FLUID).getStorage();

        for (var key : FluidStorageKey.allKeys()) {
            var fluidEntry = fluidStorage.getEntry(key);
            if (fluidEntry != null) {
                var fluidBuilder = fluidEntry.getBuilder();
                if (fluidBuilder != null && fluidBuilder.hasFluidBlock()) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean doesFluidVaporize(Material mat, Level level) {
        // water in nether behavior
        if (level.dimensionType().ultraWarm() && this.getFluid().defaultFluidState().is(FluidTags.WATER)) {
            return true;
        }
        var fluidStorage = mat.getProperty(PropertyKey.FLUID).getStorage();
        var plasmaEntry = fluidStorage.getEntry(FluidStorageKeys.PLASMA);
        var gasEntry = fluidStorage.getEntry(FluidStorageKeys.GAS);
        if (plasmaEntry != null) {
            var plasmaBuilder = plasmaEntry.getBuilder();
            return plasmaBuilder != null && plasmaBuilder.hasFluidBlock();
        } else if (gasEntry != null) {
            var gasBuilder = gasEntry.getBuilder();
            return gasBuilder != null && gasBuilder.hasFluidBlock();
        }
        return false;
    }
}

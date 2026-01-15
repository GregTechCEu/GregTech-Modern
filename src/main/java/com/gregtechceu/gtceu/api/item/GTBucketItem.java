package com.gregtechceu.gtceu.api.item;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.fluid.GTFluid;
import com.gregtechceu.gtceu.api.fluid.store.FluidStorageKey;
import com.gregtechceu.gtceu.api.fluid.store.FluidStorageKeys;
import com.gregtechceu.gtceu.api.material.material.Material;
import com.gregtechceu.gtceu.api.material.material.properties.PropertyKey;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
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
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;

import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class GTBucketItem extends BucketItem {

    final Material material;
    final String langKey;

    public GTBucketItem(Fluid fluid, Properties properties, Material material, String langKey) {
        super(fluid, properties);
        this.material = material;
        this.langKey = langKey;
    }

    public static int color(ItemStack itemStack, int index) {
        if (itemStack.getItem() instanceof GTBucketItem item) {
            if (index == 1) {
                return IClientFluidTypeExtensions.of(item.content).getTintColor();
            }
        }
        return -1;
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
        if (!(material.getFluid() instanceof FlowingFluid)) return false;

        BlockState blockstate = level.getBlockState(pos);
        Block block = blockstate.getBlock();
        boolean canReplace = blockstate.canBeReplaced(material.getFluid());
        boolean canPlace = blockstate.isAir() || canReplace ||
                block instanceof LiquidBlockContainer lbc &&
                        lbc.canPlaceLiquid(player, level, pos, blockstate, material.getFluid());

        if (!canPlace) {
            return result != null && this.emptyContents(player, level,
                    result.getBlockPos().relative(result.getDirection()), null, container);
        }

        var fluidType = material.getFluid().getFluidType();
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
                blockContainer.canPlaceLiquid(player, level, pos, blockstate, material.getFluid())) {
            var flowingFluid = ((FlowingFluid) material.getFluid());
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
        if (level.dimensionType().ultraWarm() && material.getFluid().defaultFluidState().is(FluidTags.WATER)) {
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

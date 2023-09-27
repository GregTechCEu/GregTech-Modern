package com.gregtechceu.gtceu.api.fluids;


import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.fluids.attribute.FluidAttribute;
import com.gregtechceu.gtceu.api.fluids.attribute.IAttributedFluid;
import dev.architectury.injectables.annotations.ExpectPlatform;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import lombok.Getter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.function.Supplier;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class GTFluid extends FlowingFluid implements IAttributedFluid {

    private final Collection<FluidAttribute> attributes = new ObjectLinkedOpenHashSet<>();
    private final FluidState state;
    private final Supplier<? extends Item> bucketItem;
    private final Supplier<? extends Fluid> flowingFluid;
    private final Supplier<? extends LiquidBlock> block;
    @Getter
    private final int burnTime;

    public GTFluid(@NotNull ResourceLocation fluidName, @NotNull FluidState state, Supplier<? extends Fluid> flowingFluid, Supplier<? extends LiquidBlock> block, Supplier<? extends Item> bucket, int burnTime) {
        super();
        this.state = state;
        this.flowingFluid = flowingFluid;
        this.block = block;
        this.bucketItem = bucket;
        this.burnTime = burnTime;
        registerDefaultState(getStateDefinition().any().setValue(LEVEL, 8));
    }

    @Override
    protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, net.minecraft.world.level.material.FluidState> builder) {
        super.createFluidStateDefinition(builder);
        builder.add(LEVEL);
    }

    @Override
    public @NotNull FluidState getState() {
        return state;
    }

    @Override
    public @NotNull @Unmodifiable Collection<FluidAttribute> getAttributes() {
        return attributes;
    }

    @Override
    public void addAttribute(@NotNull FluidAttribute attribute) {
        attributes.add(attribute);
    }

    @Override
    public Item getBucket() {
        return bucketItem != null ? bucketItem.get() : Items.AIR;
    }

    @Override
    protected boolean canBeReplacedWith(net.minecraft.world.level.material.FluidState state, BlockGetter level, BlockPos pos, Fluid fluid, Direction direction) {
        return direction == Direction.DOWN && !isSame(fluid);
    }

    @Override
    public int getTickDelay(LevelReader level) {
        return 5;
    }

    @Override
    protected float getExplosionResistance() {
        return 1;
    }

    @Override
    protected BlockState createLegacyBlock(net.minecraft.world.level.material.FluidState state) {
        if (block != null)
            return block.get().defaultBlockState().setValue(LiquidBlock.LEVEL, getLegacyLevel(state));
        return Blocks.AIR.defaultBlockState();
    }

    @Override
    public boolean isSource(net.minecraft.world.level.material.FluidState state) {
        return state.is(this);
    }

    @Override
    public Fluid getFlowing() {
        return flowingFluid != null ? flowingFluid.get() : Fluids.EMPTY;
    }

    @Override
    public Fluid getSource() {
        return this;
    }

    @Override
    protected boolean canConvertToSource(Level world) {
        return false;
    }

    @Override
    protected void beforeDestroyingBlock(LevelAccessor level, BlockPos pos, BlockState state) {
        BlockEntity blockEntity = state.hasBlockEntity() ? level.getBlockEntity(pos) : null;
        Block.dropResources(state, level, pos, blockEntity);
    }

    @Override
    protected int getSlopeFindDistance(LevelReader level) {
        return 4;
    }

    @Override
    protected int getDropOff(LevelReader level) {
        return 1;
    }

    @Override
    public int getAmount(net.minecraft.world.level.material.FluidState state) {
        return state.getValue(LEVEL);
    }


    public static class GTMaterialFluid extends GTFluid {

        private final Material material;
        private final String translationKey;

        public GTMaterialFluid(@NotNull ResourceLocation fluidName, @NotNull FluidState state, 
                               Supplier<? extends Fluid> flowingFluid, Supplier<? extends LiquidBlock> block, Supplier<? extends Item> bucket, 
                               @Nullable String translationKey, @NotNull Material material, int burnTime) {
            super(fluidName, state, flowingFluid, block, bucket, burnTime);
            this.material = material;
            this.translationKey = translationKey;
        }

        public @NotNull Material getMaterial() {
            return this.material;
        }

        public String getTranslationKey() {
            return translationKey;
        }

        /*
        @Override
        public String getLocalizedName(FluidStack stack) {
            String localizedName;
            String customMaterialTranslation = "fluid." + material.getUnlocalizedName();

            if (LocalizationUtils.exist(customMaterialTranslation)) {
                localizedName = I18n.format(customMaterialTranslation);
            } else {
                localizedName = I18n.format(material.getUnlocalizedName());
            }

            if (translationKey != null) {
                return I18n.format(translationKey, localizedName);
            }
            return localizedName;
        }
         */
    }
}


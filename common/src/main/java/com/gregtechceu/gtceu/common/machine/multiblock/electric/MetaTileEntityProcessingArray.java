package com.gregtechceu.gtceu.common.machine.multiblock.electric;

import gregtech.api.GTValues;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.impl.AbstractRecipeLogic;
import gregtech.api.capability.impl.MultiblockRecipeLogic;
import gregtech.api.metatileentity.IMachineHatchMultiblock;
import gregtech.api.metatileentity.ITieredMetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.*;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.renderer.texture.cube.OrientedOverlayRenderer;
import gregtech.common.ConfigHolder;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import gregtech.core.sound.GTSoundEvents;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.apache.commons.lang3.ArrayUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import static gregtech.api.GTValues.ULV;

public class MetaTileEntityProcessingArray extends RecipeMapMultiblockController implements IMachineHatchMultiblock {

    private final int tier;
    private boolean machineChanged;

    public MetaTileEntityProcessingArray(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId, null);
        this.tier = tier;
        this.recipeMapWorkable = new ProcessingArrayWorkable(this);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityProcessingArray(metaTileEntityId, tier);
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        ((ProcessingArrayWorkable) this.recipeMapWorkable).findMachineStack();
    }

    @Override
    public int getMachineLimit() {
        return tier == 0 ? 16 : 64;
    }

    @Nonnull
    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("XXX", "XXX", "XXX")
                .aisle("XXX", "X#X", "XXX")
                .aisle("XXX", "XSX", "XXX")
                .where('L', states(getCasingState()))
                .where('S', selfPredicate())
                .where('X', states(getCasingState())
                        .setMinGlobalLimited(tier == 0 ? 11 : 4)
                        .or(autoAbilities(false, true, true, true, true, true, true))
                        .or(abilities(MultiblockAbility.INPUT_ENERGY).setMinGlobalLimited(1)) // no energy hatch maximum
                        .or(abilities(MultiblockAbility.MACHINE_HATCH).setExactLimit(1)))
                .where('#', air())
                .build();
    }

    public IBlockState getCasingState() {
        return tier == 0
                ? MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.TUNGSTENSTEEL_ROBUST)
                : MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.HSSE_STURDY);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return tier == 0
                ? Textures.ROBUST_TUNGSTENSTEEL_CASING
                : Textures.STURDY_HSSE_CASING;
    }

    @Override
    protected void addDisplayText(List<ITextComponent> textList) {
        super.addDisplayText(textList);
        if (this.isActive()) {
            textList.add(new TextComponentTranslation("gregtech.machine.machine_hatch.locked").setStyle(new Style().setColor(TextFormatting.RED)));
        }
    }

    @Nonnull
    @Override
    protected OrientedOverlayRenderer getFrontOverlay() {
        return tier == 0
                ? Textures.PROCESSING_ARRAY_OVERLAY
                : Textures.ADVANCED_PROCESSING_ARRAY_OVERLAY;
    }

    @Override
    public boolean canBeDistinct() {
        return true;
    }

    @Override
    public void notifyMachineChanged() {
        machineChanged = true;
    }

    @Override
    public String[] getBlacklist() {
        return ConfigHolder.machines.processingArrayBlacklist;
    }

    @Override
    public SoundEvent getBreakdownSound() {
        return GTSoundEvents.BREAKDOWN_MECHANICAL;
    }

    @Override
    public SoundEvent getSound() {
        return GTSoundEvents.ARC;
    }

    @Override
    public TraceabilityPredicate autoAbilities(boolean checkEnergyIn, boolean checkMaintenance, boolean checkItemIn, boolean checkItemOut, boolean checkFluidIn, boolean checkFluidOut, boolean checkMuffler) {
        TraceabilityPredicate predicate = super.autoAbilities(checkMaintenance, checkMuffler)
                .or(checkEnergyIn ? abilities(MultiblockAbility.INPUT_ENERGY).setMinGlobalLimited(1).setMaxGlobalLimited(4).setPreviewCount(1) : new TraceabilityPredicate());

        predicate = predicate.or(abilities(MultiblockAbility.IMPORT_ITEMS).setPreviewCount(1));

        predicate = predicate.or(abilities(MultiblockAbility.EXPORT_ITEMS).setPreviewCount(1));

        predicate = predicate.or(abilities(MultiblockAbility.IMPORT_FLUIDS).setPreviewCount(1));

        predicate = predicate.or(abilities(MultiblockAbility.EXPORT_FLUIDS).setPreviewCount(1));

        return predicate;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("gregtech.universal.tooltip.parallel", getMachineLimit()));
    }

    @Override
    public int getItemOutputLimit() {
        ItemStack machineStack = ((ProcessingArrayWorkable) this.recipeMapWorkable).getMachineStack();
        MetaTileEntity mte = GTUtility.getMetaTileEntity(machineStack);
        return mte == null ? 0 : mte.getItemOutputLimit();

    }

    @SuppressWarnings("InnerClassMayBeStatic")
    protected class ProcessingArrayWorkable extends MultiblockRecipeLogic {

        private static final ICleanroomProvider DUMMY_CLEANROOM = DummyCleanroom.createForAllTypes();

        ItemStack currentMachineStack = ItemStack.EMPTY;
        MetaTileEntity mte = null;
        //The Voltage Tier of the machines the PA is operating upon, from GTValues.V
        private int machineTier;
        //The maximum Voltage of the machines the PA is operating upon
        private long machineVoltage;
        //The Recipe Map of the machines the PA is operating upon
        private RecipeMap<?> activeRecipeMap;

        public ProcessingArrayWorkable(RecipeMapMultiblockController tileEntity) {
            super(tileEntity);
        }

        @Override
        public void invalidate() {
            super.invalidate();

            // invalidate mte's cleanroom reference
            if (mte != null && mte instanceof ICleanroomReceiver){
                ((ICleanroomReceiver) mte).setCleanroom(null);
            }

            // Reset locally cached variables upon invalidation
            currentMachineStack = ItemStack.EMPTY;
            mte = null;
            machineChanged = true;
            machineTier = 0;
            machineVoltage = 0L;
            activeRecipeMap = null;
        }

        /**
         * Checks if a provided Recipe Map is valid to be used in the processing array
         * Will filter out anything in the config blacklist, and also any non-singleblock machines
         *
         * @param recipeMap The recipeMap to check
         * @return {@code true} if the provided recipeMap is valid for use
         */
        @Override
        public boolean isRecipeMapValid(@Nonnull RecipeMap<?> recipeMap) {
            if (ArrayUtils.contains(((IMachineHatchMultiblock) metaTileEntity).getBlacklist(), recipeMap.getUnlocalizedName())) {
                return false;
            }

            return GTUtility.isMachineValidForMachineHatch(currentMachineStack, ((IMachineHatchMultiblock) metaTileEntity).getBlacklist());
        }

        @Override
        protected boolean shouldSearchForRecipes() {
            return canWorkWithMachines() && super.shouldSearchForRecipes();
        }

        public boolean canWorkWithMachines() {
            if (machineChanged) {
                findMachineStack();
                machineChanged = false;
                previousRecipe = null;
                if (isDistinct()) {
                    invalidatedInputList.clear();
                } else {
                    invalidInputsForRecipes = false;
                }
            }
            return (!currentMachineStack.isEmpty() && this.activeRecipeMap != null);
        }

        @Nullable
        @Override
        public RecipeMap<?> getRecipeMap() {
            return activeRecipeMap;
        }

        public void findMachineStack() {
            RecipeMapMultiblockController controller = (RecipeMapMultiblockController) this.metaTileEntity;

            //The Processing Array is limited to 1 Machine Interface per multiblock, and only has 1 slot
            ItemStack machine = controller.getAbilities(MultiblockAbility.MACHINE_HATCH).get(0).getStackInSlot(0);


            mte = GTUtility.getMetaTileEntity(machine);

            if (mte == null) {
                this.activeRecipeMap = null;
            } else {
                this.activeRecipeMap = mte.getRecipeMap();
                // Set the world for MTEs, as some need it for checking their recipes
                MetaTileEntityHolder holder = new MetaTileEntityHolder();
                mte = holder.setMetaTileEntity(mte);
                holder.setWorld(this.metaTileEntity.getWorld());

                // Set the cleanroom of the MTEs to the PA's cleanroom reference
                if (mte instanceof ICleanroomReceiver receiver) {
                    if (ConfigHolder.machines.cleanMultiblocks) {
                        receiver.setCleanroom(DUMMY_CLEANROOM);
                    } else {
                        ICleanroomProvider provider = controller.getCleanroom();
                        if (provider != null) receiver.setCleanroom(provider);
                    }
                }
            }

            //Find the voltage tier of the machine.
            this.machineTier = mte instanceof ITieredMetaTileEntity ? ((ITieredMetaTileEntity) mte).getTier() : 0;

            this.machineVoltage = GTValues.V[this.machineTier];

            this.currentMachineStack = machine;
        }

        @Override
        public boolean checkRecipe(@Nonnull Recipe recipe) {
            if (mte == null) return false;

            AbstractRecipeLogic arl = mte.getRecipeLogic();
            if (arl == null) return false;

            return arl.checkRecipe(recipe) && super.checkRecipe(recipe);
        }

        @Override
        protected int getOverclockForTier(long voltage) {
            return super.getOverclockForTier(Math.min(machineVoltage, getMaximumOverclockVoltage()));
        }

        @Override
        public int getParallelLimit() {
            return (currentMachineStack == null || currentMachineStack.isEmpty()) ? getMachineLimit() : Math.min(currentMachineStack.getCount(), getMachineLimit());
        }

        @Override
        protected Recipe findRecipe(long maxVoltage, IItemHandlerModifiable inputs, IMultipleTankHandler fluidInputs) {
            return super.findRecipe(Math.min(super.getMaxVoltage(), this.machineVoltage), inputs, fluidInputs);
        }

        @Override
        protected int getNumberOfOCs(int recipeEUt) {
            if (!isAllowOverclocking()) return 0;

            int recipeTier = Math.max(0, GTUtility.getTierByVoltage(recipeEUt / Math.max(1, this.parallelRecipesPerformed)));
            int maximumTier = Math.min(this.machineTier, GTUtility.getTierByVoltage(getMaxVoltage()));

            // The maximum number of overclocks is determined by the difference between the tier the recipe is running at,
            // and the maximum tier that the machine can overclock to.
            int numberOfOCs = maximumTier - recipeTier;
            if (recipeTier == ULV) numberOfOCs--; // no ULV overclocking

            return numberOfOCs;
        }

        private ItemStack getMachineStack() {
            return currentMachineStack;
        }
    }
}

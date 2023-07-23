package com.gregtechceu.gtceu.common.machine.multiblock.electric;

import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import gregtech.api.GTValues;
import gregtech.api.capability.IObjectHolder;
import com.gregtechceu.gtceu.api.machine.trait.optical.IOpticalComputationHatch;
import com.gregtechceu.gtceu.api.machine.trait.optical.IOpticalComputationHatchder;
import com.gregtechceu.gtceu.api.machine.trait.optical.IOpticalComputationHatchver;
import gregtech.api.capability.impl.ComputationRecipeLogic;
import gregtech.api.capability.impl.ItemHandlerList;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.MultiblockShapeInfo;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMaps;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.ConfigHolder;
import gregtech.common.blocks.BlockComputerCasing;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.metatileentities.MetaTileEntities;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.*;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class ResearchStationMachine extends WorkableElectricMultiblockMachine implements IOpticalComputationReceiver {

    private IOpticalComputationProvider computationProvider;
    private IObjectHolder objectHolder;

    public ResearchStationMachine(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, RecipeMaps.RESEARCH_STATION_RECIPES);
        this.recipeMapWorkable = new ResearchStationRecipeLogic(this);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new ResearchStationMachine(metaTileEntityId);
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        List<IOpticalComputationHatch> providers = getAbilities(MultiblockAbility.COMPUTATION_DATA_RECEPTION);
        if (providers != null && providers.size() >= 1) {
            computationProvider = providers.get(0);
        }
        List<IObjectHolder> holders = getAbilities(MultiblockAbility.OBJECT_HOLDER);
        if (holders != null && holders.size() >= 1) {
            objectHolder = holders.get(0);
            // cannot set in initializeAbilities since super() calls it before setting the objectHolder field here
            this.inputInventory = new ItemHandlerList(Collections.singletonList(objectHolder.getAsHandler()));
        }

        // should never happen, but would rather do this than have an obscure NPE
        if (computationProvider == null || objectHolder == null) {
            invalidateStructure();
        }
    }

    // force object holder to be facing the controller
    @Override
    public void checkStructurePattern() {
        super.checkStructurePattern();
        if (isStructureFormed() && objectHolder.getFrontFacing() != getFrontFacing().getOpposite()) {
            invalidateStructure();
        }
    }

    @Override
    public ComputationRecipeLogic getRecipeMapWorkable() {
        return (ComputationRecipeLogic) recipeMapWorkable;
    }

    @Override
    public void invalidateStructure() {
        computationProvider = null;
        // recheck the ability to make sure it wasn't the one broken
        List<IObjectHolder> holders = getAbilities(MultiblockAbility.OBJECT_HOLDER);
        if (holders != null && holders.size() >= 1 && holders.get(0) == objectHolder) {
            objectHolder.setLocked(false);
        }
        objectHolder = null;
        super.invalidateStructure();
    }

    @Override
    public IOpticalComputationProvider getComputationProvider() {
        return computationProvider;
    }

    public IObjectHolder getObjectHolder() {
        return objectHolder;
    }

    @NotNull
    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("XXX", "VVV", "PPP", "PPP", "PPP", "VVV", "XXX")
                .aisle("XXX", "VAV", "AAA", "AAA", "AAA", "VAV", "XXX")
                .aisle("XXX", "VAV", "XAX", "XSX", "XAX", "VAV", "XXX")
                .aisle("XXX", "XAX", "---", "---", "---", "XAX", "XXX")
                .aisle(" X ", "XAX", "---", "---", "---", "XAX", " X ")
                .aisle(" X ", "XAX", "-A-", "-H-", "-A-", "XAX", " X ")
                .aisle("   ", "XXX", "---", "---", "---", "XXX", "   ")
                .where('S', selfPredicate())
                .where('X', states(getCasingState()))
                .where(' ', any())
                .where('-', air())
                .where('V', states(getVentState()))
                .where('A', states(getAdvancedState()))
                .where('P', states(getCasingState())
                        .or(abilities(MultiblockAbility.INPUT_ENERGY).setMinGlobalLimited(1))
                        .or(abilities(MultiblockAbility.MAINTENANCE_HATCH).setExactLimit(1))
                        .or(abilities(MultiblockAbility.COMPUTATION_DATA_RECEPTION).setExactLimit(1)))
                .where('H', abilities(MultiblockAbility.OBJECT_HOLDER))
                .build();
    }

    @Override
    public List<MultiblockShapeInfo> getMatchingShapes() {
        return Collections.singletonList(MultiblockShapeInfo.builder()
                .aisle("XXX", "VVV", "POP", "PEP", "PMP", "VVV", "XXX")
                .aisle("XXX", "VAV", "AAA", "AAA", "AAA", "VAV", "XXX")
                .aisle("XXX", "VAV", "XAX", "XSX", "XAX", "VAV", "XXX")
                .aisle("XXX", "XAX", "---", "---", "---", "XAX", "XXX")
                .aisle("-X-", "XAX", "---", "---", "---", "XAX", "-X-")
                .aisle("-X-", "XAX", "-A-", "-H-", "-A-", "XAX", "-X-")
                .aisle("---", "XXX", "---", "---", "---", "XXX", "---")
                .where('S', MetaTileEntities.RESEARCH_STATION, EnumFacing.SOUTH)
                .where('X', getCasingState())
                .where('-', Blocks.AIR.getDefaultState())
                .where('V', getVentState())
                .where('A', getAdvancedState())
                .where('P', getCasingState())
                .where('O', MetaTileEntities.COMPUTATION_HATCH_RECEIVER, EnumFacing.NORTH)
                .where('E', MetaTileEntities.ENERGY_INPUT_HATCH[GTValues.LuV], EnumFacing.NORTH)
                .where('M', () -> ConfigHolder.machines.enableMaintenance ? MetaTileEntities.MAINTENANCE_HATCH : getCasingState(), EnumFacing.NORTH)
                .where('H', MetaTileEntities.OBJECT_HOLDER, EnumFacing.NORTH)
                .build());
    }

    @NotNull
    private static IBlockState getVentState() {
        return MetaBlocks.COMPUTER_CASING.getState(BlockComputerCasing.CasingType.COMPUTER_HEAT_VENT);
    }

    @NotNull
    private static IBlockState getAdvancedState() {
        return MetaBlocks.COMPUTER_CASING.getState(BlockComputerCasing.CasingType.ADVANCED_COMPUTER_CASING);
    }

    @NotNull
    private static IBlockState getCasingState() {
        return MetaBlocks.COMPUTER_CASING.getState(BlockComputerCasing.CasingType.COMPUTER_CASING);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        if (sourcePart == null || sourcePart instanceof IObjectHolder) {
            return Textures.ADVANCED_COMPUTER_CASING;
        }
        return Textures.COMPUTER_CASING;
    }

    @NotNull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return Textures.RESEARCH_STATION_OVERLAY;
    }

    @Override
    protected boolean shouldShowVoidingModeButton() {
        return false;
    }

    // let it think it can "void" since we replace an input item with the finished
    // item on completion, instead of outputting into a dedicated output slot.
    @Override
    public boolean canVoidRecipeItemOutputs() {
        return true;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip, boolean advanced) {
        super.addInformation(stack, world, tooltip, advanced);
        tooltip.add(I18n.format("gregtech.machine.research_station.tooltip.1"));
        tooltip.add(I18n.format("gregtech.machine.research_station.tooltip.2"));
        tooltip.add(I18n.format("gregtech.machine.research_station.tooltip.3"));
    }

    @Override
    protected void addDisplayText(List<ITextComponent> textList) {
        super.addDisplayText(textList);
        if (isStructureFormed() && isActive()) {
            var recipeLogic = getRecipeMapWorkable();
            textList.add(new TextComponentTranslation("gregtech.multiblock.computation.usage", recipeLogic.getRecipeCWUt()));
            if (recipeLogic.isHasNotEnoughComputation()) {
                textList.add(new TextComponentTranslation("gregtech.multiblock.computation.not_enough_computation")
                        .setStyle(new Style().setColor(TextFormatting.RED)));
            }
        }
    }

    @Override
    protected void addWarningText(List<ITextComponent> textList) {
        super.addWarningText(textList);
        if (isStructureFormed() && isActive() && getRecipeMapWorkable().isHasNotEnoughComputation()) {
            textList.add(new TextComponentTranslation("gregtech.multiblock.computation.not_enough_computation")
                    .setStyle(new Style().setColor(TextFormatting.RED)));
        }
    }

    private static class ResearchStationRecipeLogic extends ComputationRecipeLogic {

        public ResearchStationRecipeLogic(ResearchStationMachine metaTileEntity) {
            super(metaTileEntity, ComputationType.SPORADIC);
        }

        @NotNull
        @Override
        public ResearchStationMachine getMetaTileEntity() {
            return (ResearchStationMachine) super.getMetaTileEntity();
        }

        @Override
        public boolean isAllowOverclocking() {
            return false;
        }

        @Override
        protected boolean setupAndConsumeRecipeInputs(@NotNull Recipe recipe, @NotNull IItemHandlerModifiable importInventory) {
            // this machine cannot overclock, so don't bother calling it
            this.overclockResults = new int[]{recipe.getEUt(), recipe.getDuration()};
            if (!hasEnoughPower(overclockResults)) {
                return false;
            }

            // skip "can fit" checks, it can always fit

            // do not consume inputs here, consume them on completion
            if (recipe.matches(false, importInventory, getInputTank())) {
                this.metaTileEntity.addNotifiedInput(importInventory);
                return true;
            }
            return false;
        }

        // lock the object holder on recipe start
        @Override
        protected void setupRecipe(Recipe recipe) {
            IObjectHolder holder = getMetaTileEntity().getObjectHolder();
            holder.setLocked(true);
            super.setupRecipe(recipe);
        }

        // "replace" the items in the slots rather than outputting elsewhere
        // unlock the object holder
        @Override
        protected void outputRecipeOutputs() {
            IObjectHolder holder = getMetaTileEntity().getObjectHolder();
            holder.setHeldItem(ItemStack.EMPTY);

            ItemStack outputItem = ItemStack.EMPTY;
            if (itemOutputs != null && itemOutputs.size() >= 1) {
                outputItem = itemOutputs.get(0);
            }
            holder.setDataItem(outputItem);
            holder.setLocked(false);
        }
    }
}

package com.gregtechceu.gtceu.common.machine.multiblock.part;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.IDataAccessHatch;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.UITemplate;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredIOPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.utils.AssemblyLineManager;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.SlotWidget;
import com.lowdragmc.lowdraglib.side.item.IItemTransfer;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.ArrayUtils;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

public class DataAccessHatchPartMachine extends TieredIOPartMachine implements IDataAccessHatch {
    private final Set<GTRecipe> recipes;
    private final boolean isCreative;

    public DataAccessHatchPartMachine(IMachineBlockEntity holder, int tier, boolean isCreative) {
        super(holder, tier, IO.IN);
        this.isCreative = isCreative;
        this.recipes = isCreative ? Collections.emptySet() : new ObjectOpenHashSet<>();
        rebuildData();
    }

    protected IItemTransfer createImportItemHandler() {
        return new NotifiableItemStackHandler(this, getInventorySize(getTier()), IO.IN) {
            @Override
            public void onChanged() {
                super.onChanged();
                rebuildData();
            }

            @Nonnull
            @Override
            public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
                var controller = MetaTileEntityDataAccessHatch.this.getController();
                boolean isDataBank = controller instanceof MetaTileEntityDataBank;
                if (AssemblyLineManager.isStackDataItem(stack, isDataBank) && AssemblyLineManager.hasResearchTag(stack)) {
                    return super.insertItem(slot, stack, simulate);
                }
                return stack;
            }
        };
    }

    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        if (getController() instanceof MetaTileEntityAssemblyLine && getController().isStructureFormed()) {
            IVertexOperation colourMultiplier = new ColourMultiplier(GTUtility.convertRGBtoOpaqueRGBA_CL(getPaintingColorForRendering()));
            for (EnumFacing facing : EnumFacing.VALUES) {
                // render grate texture on all sides but from if formed
                if (facing == getFrontFacing()) {
                    getBaseTexture().renderSided(facing, renderState, translation, ArrayUtils.add(pipeline, colourMultiplier));
                } else {
                    Textures.GRATE_CASING.renderSided(facing, renderState, translation, ArrayUtils.add(pipeline, colourMultiplier));
                }
            }
        } else {
            super.renderMetaTileEntity(renderState, translation, pipeline);
        }

        if (shouldRenderOverlay()) {
            if (isCreative) {
                Textures.CREATIVE_DATA_ACCESS_HATCH.renderSided(getFrontFacing(), renderState, translation, pipeline);
            } else {
                Textures.DATA_ACCESS_HATCH.renderSided(getFrontFacing(), renderState, translation, pipeline);
            }
        }
    }

    @Override
    public ModularUI createUI(Player entityPlayer) {
        if (isCreative) return null;
        int rowSize = (int) Math.sqrt(getInventorySize(getTier()));
        ModularUI builder = new ModularUI(176, 18 + 18 * rowSize + 94, this, entityPlayer)
                .background(GuiTextures.BACKGROUND);
        builder.widget(new LabelWidget(6, 6, this.getDefinition().getDescriptionId()));
        
        for (int y = 0; y < rowSize; y++) {
            for (int x = 0; x < rowSize; x++) {
                int index = y * rowSize + x;
                builder.widget(new SlotWidget(io == IO.OUT ? exportItems : importItems, index,
                        88 - rowSize * 9 + x * 18, 18 + y * 18, true, io == IO.IN)
                        .setBackgroundTexture(GuiTextures.SLOT));
            }
        }
        builder.widget(UITemplate.bindPlayerInventory(entityPlayer.getInventory(), GuiTextures.SLOT, 7, 18 + 18 * rowSize + 12, true));
        return builder;
    }

    @Override
    protected boolean openGUIOnRightClick() {
        return !this.isCreative;
    }

    public static int getInventorySize(int tier) {
        return tier == GTValues.LuV ? 16 : 9;
    }

    private void rebuildData() {
        if (isCreative) return;
        recipes.clear();
        boolean isDataBank = getControllers() instanceof MetaTileEntityDataBank;
        for (int i = 0; i < this.importItems.getSlots(); i++) {
            ItemStack stack = this.importItems.getStackInSlot(i);
            String researchId = AssemblyLineManager.readResearchId(stack);
            boolean isValid = AssemblyLineManager.isStackDataItem(stack, isDataBank);
            if (researchId != null && isValid) {
                Collection<GTRecipe> collection = (GTRecipeTypes.ASSEMBLY_LINE_RECIPES).getDataStickEntry(researchId);
                if (collection != null) {
                    recipes.addAll(collection);
                }
            }
        }
    }

    @Override
    public boolean isRecipeAvailable(@Nonnull GTRecipe recipe, @Nonnull Collection<IDataAccessHatch> seen) {
        seen.add(this);
        return recipes.contains(recipe);
    }

    @Override
    public boolean isCreative() {
        return this.isCreative;
    }

    @Override
    public void addToMultiBlock(MultiblockControllerBase controllerBase) {
        super.addToMultiBlock(controllerBase);
        if (!(controllerBase instanceof MetaTileEntityDataBank)) {
            rebuildData();
        }
    }
}

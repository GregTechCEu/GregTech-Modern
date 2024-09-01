package com.gregtechceu.gtceu.common.machine.multiblock.part;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.IDataAccessHatch;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IDataInfoProvider;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.api.machine.feature.IMachineModifyDrops;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.common.item.PortableScannerBehavior;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.research.DataBankMachine;
import com.gregtechceu.gtceu.common.recipe.ResearchCondition;
import com.gregtechceu.gtceu.utils.ItemStackHashStrategy;
import com.gregtechceu.gtceu.utils.ResearchManager;

import com.lowdragmc.lowdraglib.gui.widget.SlotWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class DataAccessHatchMachine extends TieredPartMachine
                                    implements IMachineLife, IDataAccessHatch, IDataInfoProvider {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            DataAccessHatchMachine.class, MultiblockPartMachine.MANAGED_FIELD_HOLDER);

    private final Set<GTRecipe> recipes;
    @Getter
    private final boolean isCreative;
    @Persisted
    public final NotifiableItemStackHandler importItems;

    public DataAccessHatchMachine(IMachineBlockEntity holder, int tier, boolean isCreative) {
        super(holder, tier);
        this.isCreative = isCreative;
        this.recipes = isCreative ? Collections.emptySet() : new ObjectOpenHashSet<>();
        this.importItems = createImportItemHandler();
    }

    protected NotifiableItemStackHandler createImportItemHandler() {
        if (isCreative) return new NotifiableItemStackHandler(this, 0, IO.BOTH);
        return new NotifiableItemStackHandler(this, getInventorySize(), IO.BOTH) {

            @Override
            public void onContentsChanged() {
                super.onContentsChanged();
                rebuildData(!getControllers().isEmpty() && getControllers().get(0) instanceof DataBankMachine);
            }

            @NotNull
            @Override
            public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
                var controller = DataAccessHatchMachine.this.getControllers().isEmpty() ? null :
                        DataAccessHatchMachine.this.getControllers().get(0);
                boolean isDataBank = controller instanceof DataBankMachine;
                if (ResearchManager.isStackDataItem(stack, isDataBank) &&
                        ResearchManager.hasResearchTag(stack)) {
                    return super.insertItem(slot, stack, simulate);
                }
                return stack;
            }
        };
    }

    @Override
    public Widget createUIWidget() {
        int rowSize = (int) Math.sqrt(getInventorySize());
        int xOffset = 18 * rowSize / 2;
        WidgetGroup group = new WidgetGroup(0, 0, 18 * rowSize, 18 * rowSize);

        for (int y = 0; y < rowSize; y++) {
            for (int x = 0; x < rowSize; x++) {
                int index = y * rowSize + x;
                group.addWidget(new SlotWidget(importItems, index,
                        rowSize * 9 + x * 18 - xOffset, y * 18, true, true)
                        .setBackgroundTexture(GuiTextures.SLOT));
            }
        }
        return group;
    }

    @Override
    public boolean shouldOpenUI(Player player, InteractionHand hand, BlockHitResult hit) {
        return !this.isCreative;
    }

    protected int getInventorySize() {
        return getTier() == GTValues.LuV ? 16 : 9;
    }

    @Override
    public void onMachineRemoved() {
        clearInventory(importItems.storage);
    }

    private void rebuildData(boolean isDataBank) {
        if (isCreative || getLevel() == null || getLevel().isClientSide) return;
        recipes.clear();
        for (int i = 0; i < this.importItems.getSlots(); i++) {
            ItemStack stack = this.importItems.getStackInSlot(i);
            Pair<GTRecipeType, String> researchData = ResearchManager.readResearchId(stack);
            boolean isValid = ResearchManager.isStackDataItem(stack, isDataBank);
            if (researchData != null && isValid) {
                Collection<GTRecipe> collection = researchData.getFirst().getDataStickEntry(researchData.getSecond());
                if (collection != null) {
                    recipes.addAll(collection);
                }
            }
        }
    }

    @Override
    public boolean isRecipeAvailable(@NotNull GTRecipe recipe, @NotNull Collection<IDataAccessHatch> seen) {
        seen.add(this);
        return recipe.conditions.stream().noneMatch(ResearchCondition.class::isInstance) || recipes.contains(recipe);
    }

    @NotNull
    @Override
    public List<Component> getDataInfo(PortableScannerBehavior.DisplayMode mode) {
        if (mode == PortableScannerBehavior.DisplayMode.SHOW_ALL ||
                mode == PortableScannerBehavior.DisplayMode.SHOW_RECIPE_INFO) {
            if (recipes.isEmpty())
                return Collections.emptyList();
            List<Component> list = new ArrayList<>();

            list.add(Component.translatable("behavior.data_item.assemblyline.title"));
            list.add(Component.empty());
            Collection<ItemStack> itemsAdded = new ObjectOpenCustomHashSet<>(ItemStackHashStrategy.comparingAll());
            for (GTRecipe recipe : recipes) {
                ItemStack stack = ItemRecipeCapability.CAP
                        .of(recipe.getOutputContents(ItemRecipeCapability.CAP).get(0).content).getItems()[0];
                if (!itemsAdded.contains(stack)) {
                    itemsAdded.add(stack);
                    list.add(Component.translatable("behavior.data_item.assemblyline.data", stack.getDisplayName()));
                }
            }
            return list;
        }
        return new ArrayList<>();
    }

    @Override
    public boolean canShared() {
        return isCreative;
    }

    @Override
    public void addedToController(IMultiController controller) {
        rebuildData(controller instanceof DataBankMachine);
        super.addedToController(controller);
    }

    @Override
    public GTRecipe modifyRecipe(GTRecipe recipe) {
        return IDataAccessHatch.super.modifyRecipe(recipe);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }
}

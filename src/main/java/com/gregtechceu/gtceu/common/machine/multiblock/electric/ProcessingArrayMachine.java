package com.gregtechceu.gtceu.common.machine.multiblock.electric;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.item.MetaMachineItem;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IMachineModifyDrops;
import com.gregtechceu.gtceu.api.machine.multiblock.TieredWorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.utils.GTUtil;
import com.lowdragmc.lowdraglib.gui.widget.SlotWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.misc.ItemStackTransfer;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author KilaBash
 * @date 2023/7/23
 * @implNote ProcessingArrayMachine
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ProcessingArrayMachine extends TieredWorkableElectricMultiblockMachine implements IMachineModifyDrops {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(ProcessingArrayMachine.class, TieredWorkableElectricMultiblockMachine.MANAGED_FIELD_HOLDER);

    @Persisted @DescSynced
    public final NotifiableItemStackHandler machineStorage;
    //runtime
    @Nullable
    private GTRecipeType[] recipeTypeCache;

    public ProcessingArrayMachine(IMachineBlockEntity holder, int tier, Object... args) {
        super(holder, tier, args);
        this.machineStorage = createMachineStorage(args);
    }

    //////////////////////////////////////
    //*****     Initialization    ******//
    //////////////////////////////////////
    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    protected NotifiableItemStackHandler createMachineStorage(Object... args) {
        var storage = new NotifiableItemStackHandler(this, 1, IO.NONE, IO.NONE, slots -> new ItemStackTransfer(1) {
            @Override
            public int getSlotLimit(int slot) {
                return getMachineLimit(getDefinition().getTier());
            }
        });
        storage.setFilter(this::isMachineStack);
        return storage;
    }

    protected boolean isMachineStack(ItemStack itemStack) {
        if (itemStack.getItem() instanceof MetaMachineItem metaMachineItem) {
            var recipeTypes = metaMachineItem.getDefinition().getRecipeTypes();
            if(recipeTypes == null){
                return false;
            }
            for(GTRecipeType type : recipeTypes){
                if(type != GTRecipeTypes.DUMMY_RECIPES){
                    return true;
                }
            }
        }
        return false;
    }

    @Nullable
    public MachineDefinition getMachineDefinition() {
        if (machineStorage.storage.getStackInSlot(0).getItem() instanceof MetaMachineItem metaMachineItem) {
            return metaMachineItem.getDefinition();
        }
        return null;
    }

    @Override
    @Nonnull
    public GTRecipeType[] getRecipeTypes() {
        if (recipeTypeCache == null) {
            var definition = getMachineDefinition();
            recipeTypeCache = definition == null ? null : definition.getRecipeTypes();
        }
        if (recipeTypeCache == null) {
            recipeTypeCache = new GTRecipeType[]{GTRecipeTypes.DUMMY_RECIPES};
        }
        return recipeTypeCache;
    }

    @NotNull
    @Override
    public GTRecipeType getRecipeType() {
        return getRecipeTypes()[getActiveRecipeType()];
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!isRemote()) {
            machineStorage.addChangedListener(this::onMachineChanged);
        }
    }

    protected void onMachineChanged() {
        recipeTypeCache = null;
        if (isFormed) {
            if (getRecipeLogic().getLastRecipe() != null) {
                getRecipeLogic().markLastRecipeDirty();
            }
            getRecipeLogic().updateTickSubscription();
        }
    }

    @Override
    public void onDrops(List<ItemStack> drops, Player entity) {
        clearInventory(drops, machineStorage.storage);
    }

    //////////////////////////////////////
    //*******    Recipe Logic    *******//
    //////////////////////////////////////

    /**
     * For available recipe tier, decided by the held machine.
     */
    @Override
    public int getTier() {
        var definition = getMachineDefinition();
        return definition == null ? 0 : definition.getTier();
    }

    @Override
    public int getOverclockTier() {
        MachineDefinition machineDefinition = getMachineDefinition();
        int machineTier = machineDefinition == null ? getDefinition().getTier() : Math.min(getDefinition().getTier(), machineDefinition.getTier());
        return Math.min(machineTier, GTUtil.getTierByVoltage(getMaxVoltage()));
    }

    @Override
    public int getMinOverclockTier() {
        return getOverclockTier();
    }

    @Override
    public int getMaxOverclockTier() {
        return getOverclockTier();
    }

    @Override
    public long getMaxVoltage() {
        return getMaxHatchVoltage();
    }

    @Nullable
    public static GTRecipe recipeModifier(MetaMachine machine, @Nonnull GTRecipe recipe) {
        if (machine instanceof ProcessingArrayMachine processingArray && processingArray.machineStorage.storage.getStackInSlot(0).getCount() > 0) {
            if (RecipeHelper.getRecipeEUtTier(recipe) > processingArray.getTier()) {
                return null;
            }

            var parallelLimit = processingArray.machineStorage.storage.getStackInSlot(0).getCount();

            // apply parallel first
            var parallel = Objects.requireNonNull(GTRecipeModifiers.accurateParallel(
                machine, recipe, Math.min(parallelLimit, getMachineLimit(machine.getDefinition().getTier())), false
            ));
            int parallelCount = parallel.getB();
            recipe = parallel.getA();

            // apply overclock afterwards
            long maxVoltage = processingArray.getOverclockVoltage() * parallelCount;
            recipe = RecipeHelper.applyOverclock(OverclockingLogic.NON_PERFECT_OVERCLOCK, recipe, maxVoltage);

            return recipe;
        }
        return null;
    }

    @Override
    public Map<RecipeCapability<?>, Integer> getOutputLimits() {
        if (getMachineDefinition() != null) {
            return getMachineDefinition().getRecipeOutputLimits();
        }
        return GTRegistries.RECIPE_CAPABILITIES.values().stream().map(key -> Map.entry(key, 0)).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    //////////////////////////////////////
    //********        Gui       ********//
    //////////////////////////////////////

    @Override
    public void addDisplayText(List<Component> textList) {
        super.addDisplayText(textList);
        if (isActive()) {
            textList.add(Component.translatable("gtceu.machine.machine_hatch.locked").withStyle(Style.EMPTY.withColor(ChatFormatting.RED)));
        }
    }

    @Override
    public Widget createUIWidget() {
        var widget =  super.createUIWidget();
        if (widget instanceof WidgetGroup group) {
            var size = group.getSize();
            group.addWidget(new SlotWidget(machineStorage.storage, 0, size.width - 30, size.height - 30, true, true)
                    .setBackground(GuiTextures.SLOT));
        }
        return widget;
    }

    //////////////////////////////////////
    //********     Structure    ********//
    //////////////////////////////////////
    public static Block getCasingState(int tier) {
        if (tier <= GTValues.IV) {
            return GTBlocks.CASING_TUNGSTENSTEEL_ROBUST.get();
        } else {
            return GTBlocks.CASING_HSSE_STURDY.get();
        }
    }

    public static int getMachineLimit(Integer tier) {
        return tier <= GTValues.IV ? 16 : 64;
    }

}

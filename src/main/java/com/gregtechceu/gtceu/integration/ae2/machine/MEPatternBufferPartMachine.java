package com.gregtechceu.gtceu.integration.ae2.machine;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IFilteredHandler;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.fancyconfigurator.ButtonConfigurator;
import com.gregtechceu.gtceu.api.machine.fancyconfigurator.CircuitFancyConfigurator;
import com.gregtechceu.gtceu.api.machine.fancyconfigurator.FancyInvConfigurator;
import com.gregtechceu.gtceu.api.machine.fancyconfigurator.FancyTankConfigurator;
import com.gregtechceu.gtceu.api.machine.feature.IDataStickInteractable;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.trait.IRecipeHandlerTrait;
import com.gregtechceu.gtceu.api.machine.trait.MachineTraitType;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableRecipeHandlerTrait;
import com.gregtechceu.gtceu.api.machine.trait.RecipeHandlerGroupDistinctness;
import com.gregtechceu.gtceu.api.machine.trait.RecipeHandlerList;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredient;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gregtechceu.gtceu.common.data.machines.GTAEMachines;
import com.gregtechceu.gtceu.common.item.behavior.IntCircuitBehaviour;
import com.gregtechceu.gtceu.integration.ae2.gui.widget.AETextInputButtonWidget;
import com.gregtechceu.gtceu.integration.ae2.gui.widget.slot.AEPatternViewSlotWidget;
import com.gregtechceu.gtceu.utils.GTMath;
import com.gregtechceu.gtceu.utils.ItemStackHashStrategy;

import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.util.ClickData;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;

import appeng.api.crafting.IPatternDetails;
import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.implementations.blockentities.PatternContainerGroup;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.stacks.*;
import appeng.api.storage.MEStorage;
import appeng.api.storage.StorageHelper;
import appeng.crafting.pattern.EncodedPatternItem;
import appeng.crafting.pattern.ProcessingPatternItem;
import appeng.helpers.patternprovider.PatternContainer;
import it.unimi.dsi.fastutil.objects.*;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MEPatternBufferPartMachine extends MEBusPartMachine
                                        implements ICraftingProvider, PatternContainer, IDataStickInteractable {

    protected static final int MAX_PATTERN_COUNT = 27;
    private final InternalInventory internalPatternInventory = new InternalInventory() {

        @Override
        public int size() {
            return MAX_PATTERN_COUNT;
        }

        @Override
        public ItemStack getStackInSlot(int slotIndex) {
            return patternInventory.getStackInSlot(slotIndex);
        }

        @Override
        public void setItemDirect(int slotIndex, ItemStack stack) {
            patternInventory.setStackInSlot(slotIndex, stack);
            patternInventory.onContentsChanged(slotIndex);
            onPatternChange(slotIndex);
        }
    };

    @Getter
    @SaveField
    @SyncToClient
    // Maybe an Expansion Option in the future? a bit redundant for rn. Maybe Packdevs want to add their own
    // version.
    private final CustomItemStackHandler patternInventory = new CustomItemStackHandler(MAX_PATTERN_COUNT);

    @Getter
    @SaveField
    protected final NotifiableItemStackHandler shareInventory;

    @Getter
    @SaveField
    protected final NotifiableFluidTank shareTank;

    @SaveField
    protected final InternalSlot[] internalInventory = new InternalSlot[MAX_PATTERN_COUNT];

    private final @Nullable IPatternDetails[] patternSlotDetails = new IPatternDetails[MAX_PATTERN_COUNT];

    @SyncToClient
    @SaveField
    private String customName = "";

    private boolean needPatternSync;

    @SaveField
    private final Set<BlockPos> proxies = new ObjectOpenHashSet<>();
    private final Set<MEPatternBufferProxyPartMachine> proxyMachines = new ReferenceOpenHashSet<>();

    @Nullable
    protected TickableSubscription updateSubs;

    private final List<InternalSlot> workerSlots = new ArrayList<>();
    private final List<@Nullable IPatternDetails> workerPatterns = new ArrayList<>();
    private final List<WorkerItemHandler> workerItemHandlers = new ArrayList<>();
    private final List<WorkerFluidHandler> workerFluidHandlers = new ArrayList<>();
    private final List<RecipeHandlerList> workerHandlerLists = new ArrayList<>();
    private final @UnmodifiableView List<RecipeHandlerList> workerHandlersView = Collections.unmodifiableList(workerHandlerLists);

    public MEPatternBufferPartMachine(BlockEntityCreationInfo info) {
        super(info, IO.IN);
        patternInventory.setOnContentsChanged(() -> getSyncDataHolder().markClientSyncFieldDirty("patternInventory"));
        this.patternInventory.setFilter(stack -> stack.getItem() instanceof ProcessingPatternItem);

        for (int i = 0; i < this.internalInventory.length; i++) {
            this.internalInventory[i] = new InternalSlot();
        }

        getMainNode().addService(ICraftingProvider.class, this);

        this.shareInventory = attachTrait(new NotifiableItemStackHandler(9, IO.IN, IO.NONE));
        this.shareTank = attachTrait(new NotifiableFluidTank(9, 8 * FluidType.BUCKET_VOLUME, IO.IN, IO.NONE));
        addWorkerSlot();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!isRemote()) {
            for (int i = 0; i < patternInventory.getSlots(); i++) {
                patternSlotDetails[i] = PatternDetailsHelper.decodePattern(patternInventory.getStackInSlot(i),
                        getLevel());
            }
            needPatternSync = true;
        }
    }

    private void addWorkerSlot() {
        int idx = workerSlots.size();
        if (idx >= MAX_PATTERN_COUNT) {
            return;
        }

        InternalSlot slot = internalInventory[idx];
        workerSlots.add(slot);
        workerPatterns.add(null);

        WorkerItemHandler itemH = attachTrait(new WorkerItemHandler(slot, idx));
        WorkerFluidHandler fluidH = attachTrait(new WorkerFluidHandler(slot, idx));
        workerItemHandlers.add(itemH);
        workerFluidHandlers.add(fluidH);

        slot.setOnContentsChanged(() -> {
            itemH.notifyListeners();
            fluidH.notifyListeners();
        });

        workerHandlerLists.add(new WorkerRHL(itemH, fluidH));
    }

    private void removeLastWorkerSlot() {
        if (workerSlots.size() <= 1) {
            return;
        }

        int last = workerSlots.size() - 1;
        InternalSlot slot = workerSlots.get(last);

        slot.refund();
        slot.setOnContentsChanged(() -> {});

        workerSlots.remove(last);
        workerPatterns.remove(last);
        workerItemHandlers.remove(last);
        workerFluidHandlers.remove(last);
        workerHandlerLists.remove(last);
        workerHandlersView = null;
    }

    public int getWorkerSlotCount() {
        return workerSlots.size();
    }

    public IRecipeHandlerTrait<Ingredient> getWorkerItemHandler(int idx) {
        return workerItemHandlers.get(idx);
    }

    public IRecipeHandlerTrait<FluidIngredient> getWorkerFluidHandler(int idx) {
        return workerFluidHandlers.get(idx);
    }

    static boolean couldSlotMatchContents(InternalSlot slot, Map<RecipeCapability<?>, List<Object>> contents) {
        List<Object> itemContents = contents.get(ItemRecipeCapability.CAP);
        if (itemContents != null && !slot.isItemEmpty()) {
            Set<Item> itemTypes = slot.getItemTypes();
            for (Object obj : itemContents) {
                if (!(obj instanceof Ingredient ing) || ing.isEmpty()) {
                    continue;
                }
                for (ItemStack stack : ing.getItems()) {
                    if (itemTypes.contains(stack.getItem())) {
                        return true;
                    }
                }
            }
        }
        List<Object> fluidContents = contents.get(FluidRecipeCapability.CAP);
        if (fluidContents != null && !slot.isFluidEmpty()) {
            Set<Fluid> fluidTypes = slot.getFluidTypes();
            for (Object obj : fluidContents) {
                if (!(obj instanceof FluidIngredient ing) || ing.isEmpty()) {
                    continue;
                }
                for (FluidStack stack : ing.getStacks()) {
                    if (fluidTypes.contains(stack.getFluid())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    class WorkerRHL extends RecipeHandlerList {

        private final int workerIdx;

        WorkerRHL(WorkerItemHandler itemH, WorkerFluidHandler fluidH) {
            super(IO.IN);
            this.workerIdx = itemH.workerIdx;
            addHandlers(getCircuitInventory(), getShareInventory(), getShareTank(), itemH, fluidH);
            setGroup(RecipeHandlerGroupDistinctness.BUS_DISTINCT);
        }

        @Override
        public Map<RecipeCapability<?>, List<Object>> handleRecipe(IO io, GTRecipe recipe,
                                                                   Map<RecipeCapability<?>, List<Object>> contents,
                                                                   boolean simulate) {
            if (workerIdx >= workerSlots.size()) {
                return contents;
            }

            InternalSlot slot = workerSlots.get(workerIdx);
            if (slot.isItemEmpty() && slot.isFluidEmpty()) {
                return contents;
            }

            if (!couldSlotMatchContents(slot, contents)) {
                return contents;
            }

            return super.handleRecipe(io, recipe, contents, simulate);
        }

        @Override
        public boolean isDistinct() {
            return true;
        }

        @Override
        public void setDistinct(boolean ignored, boolean notify) {}
    }

    @Getter
    class WorkerItemHandler extends NotifiableRecipeHandlerTrait<Ingredient> {

        static final MachineTraitType<WorkerItemHandler> TYPE = new MachineTraitType<>(WorkerItemHandler.class);

        @Override
        public MachineTraitType<WorkerItemHandler> getTraitType() {
            return TYPE;
        }

        private final InternalSlot slot;
        final int workerIdx;
        private final int priority;
        private final int size = 64;
        private final RecipeCapability<Ingredient> capability = ItemRecipeCapability.CAP;
        private final IO handlerIO = IO.IN;
        private final boolean isDistinct = true;

        WorkerItemHandler(InternalSlot slot, int idx) {
            super();
            this.slot = slot;
            this.workerIdx = idx;
            this.priority = IFilteredHandler.HIGH + idx + 1;
        }

        @Override
        public @Nullable List<Ingredient> handleRecipeInner(IO io, GTRecipe recipe, List<Ingredient> left,
                                                            boolean simulate) {
            if (io != IO.IN || slot.isItemEmpty()) {
                return left;
            }
            return slot.handleItemInternal(left, simulate);
        }

        @Override
        public List<Object> getContents() {
            return new ArrayList<>(slot.getItems());
        }

        @Override
        public double getTotalContentAmount() {
            return slot.getItems().stream().mapToLong(ItemStack::getCount).sum();
        }
    }

    @Getter
    class WorkerFluidHandler extends NotifiableRecipeHandlerTrait<FluidIngredient> {

        static final MachineTraitType<WorkerFluidHandler> TYPE = new MachineTraitType<>(WorkerFluidHandler.class);

        @Override
        public MachineTraitType<WorkerFluidHandler> getTraitType() {
            return TYPE;
        }

        private final InternalSlot slot;
        final int workerIdx;
        private final int priority;
        private final int size = 64;
        private final RecipeCapability<FluidIngredient> capability = FluidRecipeCapability.CAP;
        private final IO handlerIO = IO.IN;
        private final boolean isDistinct = true;

        WorkerFluidHandler(InternalSlot slot, int idx) {
            super();
            this.slot = slot;
            this.workerIdx = idx;
            this.priority = IFilteredHandler.HIGH + idx + 1;
        }

        @Override
        public @Nullable List<FluidIngredient> handleRecipeInner(IO io, GTRecipe recipe, List<FluidIngredient> left,
                                                                 boolean simulate) {
            if (io != IO.IN || slot.isFluidEmpty()) {
                return left;
            }
            return slot.handleFluidInternal(left, simulate);
        }

        @Override
        public List<Object> getContents() {
            return new ArrayList<>(slot.getFluids());
        }

        @Override
        public double getTotalContentAmount() {
            return slot.getFluids().stream().mapToLong(FluidStack::getAmount).sum();
        }
    }

    @Override
    public List<RecipeHandlerList> getRecipeHandlers() {
        if (workerHandlersView == null)
            workerHandlersView = Collections.unmodifiableList(workerHandlerLists);
        return workerHandlersView;
    }

    @Override
    public boolean isWorkingEnabled() {
        return true;
    }

    public void setCustomName(String newName) {
        customName = newName;
        syncDataHolder.markClientSyncFieldDirty("customName");
        markAsDirty();
    }

    @Override
    public void setWorkingEnabled(boolean ignored) {}

    @Override
    public boolean isDistinct() {
        return true;
    }

    @Override
    public void setDistinct(boolean ignored) {}

    @Override
    public void onMainNodeStateChanged(IGridNodeListener.State reason) {
        super.onMainNodeStateChanged(reason);
        this.updateSubscription();
    }

    protected void updateSubscription() {
        if (getMainNode().isOnline()) {
            updateSubs = subscribeServerTick(updateSubs, this::update);
        } else if (updateSubs != null) {
            updateSubs.unsubscribe();
            updateSubs = null;
        }
    }

    protected void update() {
        if (needPatternSync) {
            ICraftingProvider.requestUpdate(getMainNode());
            this.needPatternSync = false;
        }
    }

    public void addProxy(MEPatternBufferProxyPartMachine proxy) {
        proxies.add(proxy.getBlockPos());
        proxyMachines.add(proxy);
        addWorkerSlot();
    }

    public void removeProxy(MEPatternBufferProxyPartMachine proxy) {
        proxies.remove(proxy.getBlockPos());
        proxyMachines.remove(proxy);
        removeLastWorkerSlot();
    }

    @UnmodifiableView
    public Set<MEPatternBufferProxyPartMachine> getProxies() {
        if (proxyMachines.size() != proxies.size()) {
            proxyMachines.clear();
            for (BlockPos pos : proxies) {
                if (MetaMachine.getMachine(getLevel(), pos) instanceof MEPatternBufferProxyPartMachine proxy) {
                    proxyMachines.add(proxy);
                }
            }
        }
        return Collections.unmodifiableSet(proxyMachines);
    }

    private void refundAll(ClickData clickData) {
        if (!clickData.isRemote) {
            for (InternalSlot slot : workerSlots) {
                slot.refund();
            }
        }
    }

    @VisibleForTesting
    public void onPatternChange(int index) {
        if (isRemote()) return;

        IPatternDetails oldPattern = patternSlotDetails[index];
        IPatternDetails newPatternDetails = PatternDetailsHelper.decodePattern(patternInventory.getStackInSlot(index),
                getLevel());
        patternSlotDetails[index] = newPatternDetails;
        if (oldPattern != null && !oldPattern.equals(newPatternDetails)) {
            for (int i = 0; i < workerPatterns.size(); i++) {
                if (oldPattern.equals(workerPatterns.get(i))) {
                    workerSlots.get(i).refund();
                    workerPatterns.set(i, null);
                    break;
                }
            }
        }

        needPatternSync = true;
    }

    //////////////////////////////////////
    // ********** GUI ***********//
    //////////////////////////////////////
    @Override
    public void attachConfigurators(ConfiguratorPanel configuratorPanel) {
        configuratorPanel.attachConfigurators(new ButtonConfigurator(
                new GuiTextureGroup(GuiTextures.BUTTON, GuiTextures.REFUND_OVERLAY), this::refundAll)
                .setTooltips(List.of(Component.translatable("gui.gtceu.refund_all.desc"))));
        if (isHasCircuitSlot() && isCircuitSlotEnabled()) {
            configuratorPanel.attachConfigurators(new CircuitFancyConfigurator(circuitInventory.storage));
        }
        configuratorPanel.attachConfigurators(new FancyInvConfigurator(
                shareInventory.storage, Component.translatable("gui.gtceu.share_inventory.title"))
                .setTooltips(List.of(
                        Component.translatable("gui.gtceu.share_inventory.desc.0"),
                        Component.translatable("gui.gtceu.share_inventory.desc.1"))));
        configuratorPanel.attachConfigurators(new FancyTankConfigurator(
                shareTank.getStorages(), Component.translatable("gui.gtceu.share_tank.title"))
                .setTooltips(List.of(
                        Component.translatable("gui.gtceu.share_tank.desc.0"),
                        Component.translatable("gui.gtceu.share_inventory.desc.1"))));
    }

    @Override
    public Widget createUIWidget() {
        int rowSize = 9;
        int colSize = 3;
        var group = new WidgetGroup(0, 0, 18 * rowSize + 16, 18 * colSize + 16);
        int index = 0;
        for (int y = 0; y < colSize; ++y) {
            for (int x = 0; x < rowSize; ++x) {
                int finalI = index;
                var slot = new AEPatternViewSlotWidget(patternInventory, index++, 8 + x * 18, 14 + y * 18)
                        .setOccupiedTexture(GuiTextures.SLOT)
                        .setItemHook(stack -> {
                            if (!stack.isEmpty() && stack.getItem() instanceof EncodedPatternItem iep) {
                                final ItemStack out = iep.getOutput(stack);
                                if (!out.isEmpty()) {
                                    return out;
                                }
                            }
                            return stack;
                        })
                        .setChangeListener(() -> onPatternChange(finalI))
                        .setBackground(GuiTextures.SLOT, GuiTextures.PATTERN_OVERLAY);
                group.addWidget(slot);
            }
        }
        // ME Network status
        group.addWidget(new LabelWidget(
                8,
                2,
                () -> this.isOnline ? "gtceu.gui.me_network.online" : "gtceu.gui.me_network.offline"));

        group.addWidget(new AETextInputButtonWidget(18 * rowSize + 8 - 70, 2, 70, 10)
                .setText(customName)
                .setOnConfirm(this::setCustomName)
                .setButtonTooltips(Component.translatable("gui.gtceu.rename.desc")));

        return group;
    }

    @Override
    public List<IPatternDetails> getAvailablePatterns() {
        ArrayList<IPatternDetails> result = new ArrayList<>(MAX_PATTERN_COUNT);
        for (IPatternDetails p : patternSlotDetails) {
            if (p != null) result.add(p);
        }
        return result;
    }

    @Override
    public boolean pushPattern(IPatternDetails patternDetails, KeyCounter[] inputHolder) {
        if (!isFormed() || !getMainNode().isActive() || !checkInput(inputHolder)) {
            return false;
        }
        boolean knownPattern = false;
        for (IPatternDetails p : patternSlotDetails) {
            if (patternDetails.equals(p)) {
                knownPattern = true;
                break;
            }
        }
        if (!knownPattern) return false;

        for (int i = 0; i < workerSlots.size(); i++) {
            InternalSlot slot = workerSlots.get(i);
            boolean empty = slot.isItemEmpty() && slot.isFluidEmpty();
            if (empty) workerPatterns.set(i, null);
            IPatternDetails currentPattern = workerPatterns.get(i);

            if (currentPattern == null) {
                if (!empty) continue;
                workerPatterns.set(i, patternDetails);
                slot.pushPattern(patternDetails, inputHolder);
                return true;
            }
            if (currentPattern.equals(patternDetails)) {
                slot.pushPattern(patternDetails, inputHolder);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isBusy() {
        return false;
    }

    private boolean checkInput(KeyCounter[] inputHolder) {
        for (KeyCounter input : inputHolder) {
            var illegal = input.keySet().stream()
                    .map(AEKey::getType)
                    .map(AEKeyType::getId)
                    .anyMatch(id -> !id.equals(AEKeyType.items().getId()) && !id.equals(AEKeyType.fluids().getId()));
            if (illegal) return false;
        }
        return true;
    }

    @Override
    public @Nullable IGrid getGrid() {
        return getMainNode().getGrid();
    }

    @Override
    public InternalInventory getTerminalPatternInventory() {
        return internalPatternInventory;
    }

    @Override
    public PatternContainerGroup getTerminalGroup() {
        // Has controller
        if (isFormed()) {
            MultiblockControllerMachine controller = getControllers().first();
            MultiblockMachineDefinition controllerDefinition = controller.getDefinition();
            // has customName
            if (!customName.isEmpty()) {
                return new PatternContainerGroup(
                        AEItemKey.of(controllerDefinition.asStack()),
                        Component.literal(customName),
                        Collections.emptyList());
            } else {
                ItemStack circuitStack = isHasCircuitSlot() ? circuitInventory.storage.getStackInSlot(0) :
                        ItemStack.EMPTY;
                int circuitConfiguration = circuitStack.isEmpty() ? -1 :
                        IntCircuitBehaviour.getCircuitConfiguration(circuitStack);

                Component groupName = circuitConfiguration != -1 ?
                        Component.translatable(controllerDefinition.getDescriptionId())
                                .append(" - " + circuitConfiguration) :
                        Component.translatable(controllerDefinition.getDescriptionId());

                return new PatternContainerGroup(
                        AEItemKey.of(controllerDefinition.asStack()), groupName, Collections.emptyList());
            }
        } else {
            if (!customName.isEmpty()) {
                return new PatternContainerGroup(
                        AEItemKey.of(GTAEMachines.ME_PATTERN_BUFFER.getItem()),
                        Component.literal(customName),
                        Collections.emptyList());
            } else {
                return new PatternContainerGroup(
                        AEItemKey.of(GTAEMachines.ME_PATTERN_BUFFER.getItem()),
                        GTAEMachines.ME_PATTERN_BUFFER.get().getDefinition().getItem().getDescription(),
                        Collections.emptyList());
            }
        }
    }

    @Override
    public void onMachineDestroyed() {
        patternInventory.dropInventoryInWorld(getLevel(), getBlockPos());
        shareInventory.dropInventoryInWorld();
    }

    @Override
    public InteractionResult onDataStickShiftUse(Player player, ItemStack dataStick) {
        dataStick.getOrCreateTag().putIntArray("pos",
                new int[] { getBlockPos().getX(), getBlockPos().getY(), getBlockPos().getZ() });
        return InteractionResult.SUCCESS;
    }

    public record BufferData(Object2LongMap<ItemStack> items, Object2LongMap<FluidStack> fluids) {}

    public BufferData mergeInternalSlots() {
        var items = new Object2LongOpenCustomHashMap<>(ItemStackHashStrategy.comparingAllButCount());
        var fluids = new Object2LongOpenHashMap<FluidStack>();
        for (InternalSlot slot : workerSlots) {
            slot.itemInventory.object2LongEntrySet().fastForEach(e -> items.addTo(e.getKey(), e.getLongValue()));
            slot.fluidInventory.object2LongEntrySet().fastForEach(e -> fluids.addTo(e.getKey(), e.getLongValue()));
        }
        return new BufferData(items, fluids);
    }

    public class InternalSlot implements INBTSerializable<CompoundTag> {

        @Getter
        @Setter
        private Runnable onContentsChanged = () -> {};

        private final Object2LongOpenCustomHashMap<ItemStack> itemInventory = new Object2LongOpenCustomHashMap<>(
                ItemStackHashStrategy.comparingAllButCount());
        private final Object2LongOpenHashMap<FluidStack> fluidInventory = new Object2LongOpenHashMap<>();
        private @Nullable List<ItemStack> itemStacks = null;
        private @Nullable List<FluidStack> fluidStacks = null;
        private @Nullable Set<Item> cachedItemTypes = null;
        private @Nullable Set<Fluid> cachedFluidTypes = null;

        public InternalSlot() {}

        public boolean isItemEmpty() {
            return itemInventory.isEmpty();
        }

        public boolean isFluidEmpty() {
            return fluidInventory.isEmpty();
        }

        public Set<Item> getItemTypes() {
            if (cachedItemTypes == null) {
                cachedItemTypes = new ReferenceOpenHashSet<>(itemInventory.size());
                itemInventory.keySet().forEach(s -> cachedItemTypes.add(s.getItem()));
            }
            return cachedItemTypes;
        }

        public Set<Fluid> getFluidTypes() {
            if (cachedFluidTypes == null) {
                cachedFluidTypes = new ReferenceOpenHashSet<>(fluidInventory.size());
                fluidInventory.keySet().forEach(s -> cachedFluidTypes.add(s.getFluid()));
            }
            return cachedFluidTypes;
        }

        public void onContentsChanged() {
            itemStacks = null;
            fluidStacks = null;
            cachedItemTypes = null;
            cachedFluidTypes = null;
            onContentsChanged.run();
        }

        private void add(AEKey what, long amount) {
            if (amount <= 0L) return;
            if (what instanceof AEItemKey itemKey) {
                var stack = itemKey.toStack();
                itemInventory.addTo(stack, amount);
            } else if (what instanceof AEFluidKey fluidKey) {
                var stack = fluidKey.toStack(1);
                fluidInventory.addTo(stack, amount);
            }
        }

        public List<ItemStack> getItems() {
            if (itemStacks == null) {
                itemStacks = new ArrayList<>();
                itemInventory.object2LongEntrySet().stream()
                        .map(e -> GTMath.splitStacks(e.getKey(), e.getLongValue()))
                        .forEach(itemStacks::addAll);
            }
            return itemStacks;
        }

        public List<FluidStack> getFluids() {
            if (fluidStacks == null) {
                fluidStacks = new ArrayList<>();
                fluidInventory.object2LongEntrySet().stream()
                        .map(e -> GTMath.splitFluidStacks(e.getKey(), e.getLongValue()))
                        .forEach(fluidStacks::addAll);
            }
            return fluidStacks;
        }

        public void refund() {
            var network = getMainNode().getGrid();
            if (network != null) {
                MEStorage networkInv = network.getStorageService().getInventory();
                var energy = network.getEnergyService();

                for (var it = itemInventory.object2LongEntrySet().iterator(); it.hasNext();) {
                    var entry = it.next();
                    var stack = entry.getKey();
                    var count = entry.getLongValue();
                    if (stack.isEmpty() || count == 0) {
                        it.remove();
                        continue;
                    }

                    var key = AEItemKey.of(stack);
                    if (key == null) continue;

                    long inserted = StorageHelper.poweredInsert(energy, networkInv, key, count, actionSource);
                    if (inserted > 0) {
                        count -= inserted;
                        if (count == 0) it.remove();
                        else entry.setValue(count);
                    }
                }

                for (var it = fluidInventory.object2LongEntrySet().iterator(); it.hasNext();) {
                    var entry = it.next();
                    var stack = entry.getKey();
                    var amount = entry.getLongValue();
                    if (stack.isEmpty() || amount == 0) {
                        it.remove();
                        continue;
                    }

                    var key = AEFluidKey.of(stack);
                    if (key == null) continue;

                    long inserted = StorageHelper.poweredInsert(energy, networkInv, key, amount, actionSource);
                    if (inserted > 0) {
                        amount -= inserted;
                        if (amount == 0) it.remove();
                        else entry.setValue(amount);
                    }
                }
                onContentsChanged();
            }
        }

        public void pushPattern(IPatternDetails patternDetails, KeyCounter[] inputHolder) {
            patternDetails.pushInputsToExternalInventory(inputHolder, this::add);
            onContentsChanged();
        }

        public @Nullable List<Ingredient> handleItemInternal(List<Ingredient> left, boolean simulate) {
            boolean changed = false;
            for (var it = left.listIterator(); it.hasNext();) {
                var ingredient = it.next();
                if (ingredient.isEmpty()) {
                    it.remove();
                    continue;
                }

                var items = ingredient.getItems();
                if (items.length == 0 || items[0].isEmpty()) {
                    it.remove();
                    continue;
                }

                int amount = items[0].getCount();
                for (var it2 = itemInventory.object2LongEntrySet().iterator(); it2.hasNext();) {
                    var entry = it2.next();
                    var stack = entry.getKey();
                    var count = entry.getLongValue();
                    if (stack.isEmpty() || count == 0) {
                        it2.remove();
                        continue;
                    }
                    if (!ingredient.test(stack)) continue;
                    int extracted = Math.min(GTMath.saturatedCast(count), amount);
                    if (!simulate && extracted > 0) {
                        changed = true;
                        count -= extracted;
                        if (count == 0) it2.remove();
                        else entry.setValue(count);
                    }
                    amount -= extracted;

                    if (amount <= 0) {
                        it.remove();
                        break;
                    }
                }

                if (amount > 0) {
                    if (ingredient instanceof SizedIngredient si) {
                        si.setAmount(amount);
                    } else {
                        items[0].setCount(amount);
                    }
                }
            }
            if (changed) onContentsChanged();
            return left.isEmpty() ? null : left;
        }

        public @Nullable List<FluidIngredient> handleFluidInternal(List<FluidIngredient> left, boolean simulate) {
            boolean changed = false;
            for (var it = left.listIterator(); it.hasNext();) {
                var ingredient = it.next();
                if (ingredient.isEmpty()) {
                    it.remove();
                    continue;
                }

                var fluids = ingredient.getStacks();
                if (fluids.length == 0 || fluids[0].isEmpty()) {
                    it.remove();
                    continue;
                }

                int amount = fluids[0].getAmount();
                for (var it2 = fluidInventory.object2LongEntrySet().iterator(); it2.hasNext();) {
                    var entry = it2.next();
                    var stack = entry.getKey();
                    var count = entry.getLongValue();
                    if (stack.isEmpty() || count == 0) {
                        it2.remove();
                        continue;
                    }
                    if (!ingredient.test(stack)) continue;
                    int extracted = Math.min(GTMath.saturatedCast(count), amount);
                    if (!simulate && extracted > 0) {
                        changed = true;
                        count -= extracted;
                        if (count == 0) it2.remove();
                        else entry.setValue(count);
                    }
                    amount -= extracted;

                    if (amount <= 0) {
                        it.remove();
                        break;
                    }
                }

                if (amount > 0) {
                    ingredient.setAmount(amount);
                }
            }

            if (changed) onContentsChanged();
            return left.isEmpty() ? null : left;
        }

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag tag = new CompoundTag();

            ListTag itemsTag = new ListTag();
            for (var entry : itemInventory.object2LongEntrySet()) {
                var ct = entry.getKey().serializeNBT();
                ct.putLong("real", entry.getLongValue());
                itemsTag.add(ct);
            }
            if (!itemsTag.isEmpty()) tag.put("inventory", itemsTag);

            ListTag fluidsTag = new ListTag();
            for (var entry : fluidInventory.object2LongEntrySet()) {
                var ct = entry.getKey().writeToNBT(new CompoundTag());
                ct.putLong("real", entry.getLongValue());
                fluidsTag.add(ct);
            }
            if (!fluidsTag.isEmpty()) tag.put("fluidInventory", fluidsTag);

            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {
            ListTag items = tag.getList("inventory", Tag.TAG_COMPOUND);
            for (Tag t : items) {
                if (!(t instanceof CompoundTag ct)) continue;
                var stack = ItemStack.of(ct);
                var count = ct.getLong("real");
                if (!stack.isEmpty() && count > 0) {
                    itemInventory.put(stack, count);
                }
            }

            ListTag fluids = tag.getList("fluidInventory", Tag.TAG_COMPOUND);
            for (Tag t : fluids) {
                if (!(t instanceof CompoundTag ct)) continue;
                var stack = FluidStack.loadFluidStackFromNBT(ct);
                var amount = ct.getLong("real");
                if (!stack.isEmpty() && amount > 0) {
                    fluidInventory.put(stack, amount);
                }
            }
        }
    }
}

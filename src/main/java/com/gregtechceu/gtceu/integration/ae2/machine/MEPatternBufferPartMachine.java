package com.gregtechceu.gtceu.integration.ae2.machine;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.fancyconfigurator.ButtonConfigurator;
import com.gregtechceu.gtceu.api.machine.fancyconfigurator.CircuitFancyConfigurator;
import com.gregtechceu.gtceu.api.machine.fancyconfigurator.FancyInvConfigurator;
import com.gregtechceu.gtceu.api.machine.fancyconfigurator.FancyTankConfigurator;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gregtechceu.gtceu.common.data.machines.GTAEMachines;
import com.gregtechceu.gtceu.common.item.IntCircuitBehaviour;
import com.gregtechceu.gtceu.integration.ae2.gui.widget.AETextInputButtonWidget;
import com.gregtechceu.gtceu.integration.ae2.gui.widget.slot.AEPatternViewSlotWidget;
import com.gregtechceu.gtceu.integration.ae2.machine.trait.MEPatternBufferRecipeHandler;
import com.gregtechceu.gtceu.integration.ae2.utils.AEUtil;
import com.gregtechceu.gtceu.utils.GTMath;
import com.gregtechceu.gtceu.utils.GTUtil;

import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.util.ClickData;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.IContentChangeAware;
import com.lowdragmc.lowdraglib.syncdata.ITagSerializable;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
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
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MEPatternBufferPartMachine extends MEBusPartMachine
                                        implements ICraftingProvider, PatternContainer {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            MEPatternBufferPartMachine.class, MEBusPartMachine.MANAGED_FIELD_HOLDER);
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
    @Persisted
    @DescSynced // Maybe an Expansion Option in the future? a bit redundant for rn. Maybe Packdevs want to add their own
                // version.
    private final CustomItemStackHandler patternInventory = new CustomItemStackHandler(MAX_PATTERN_COUNT);
    // DO NOT remove this and use a default circuitInventory. It will cause the circuit inventory to vanish entirely and
    // crash clients as well as cause unintended behaviors.
    @Getter
    @Persisted
    protected final NotifiableItemStackHandler circuitInventorySimulated;

    @Getter
    @Persisted
    protected final NotifiableItemStackHandler shareInventory;

    @Getter
    @Persisted
    protected final NotifiableFluidTank shareTank;

    @Getter
    @Persisted
    protected final InternalSlot[] internalInventory = new InternalSlot[MAX_PATTERN_COUNT];

    private final BiMap<IPatternDetails, InternalSlot> detailsSlotMap = HashBiMap.create(MAX_PATTERN_COUNT);

    @DescSynced
    @Persisted
    @Setter
    private String customName = "";

    private boolean needPatternSync;

    @Persisted
    private HashSet<BlockPos> proxies = new HashSet<>();

    protected final MEPatternBufferRecipeHandler recipeHandler = new MEPatternBufferRecipeHandler(this);

    @Nullable
    protected TickableSubscription updateSubs;

    @Override
    public boolean isWorkingEnabled() {
        return true;
    }

    @Override
    public void setWorkingEnabled(boolean ignored) {}

    public MEPatternBufferPartMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, IO.IN, args);
        this.patternInventory.setFilter(stack -> stack.getItem() instanceof ProcessingPatternItem);
        for (int i = 0; i < this.internalInventory.length; i++) {
            this.internalInventory[i] = new InternalSlot();
        }
        getMainNode().addService(ICraftingProvider.class, this);
        this.circuitInventorySimulated = new NotifiableItemStackHandler(this, 1, IO.IN, IO.NONE)
                .setFilter(IntCircuitBehaviour::isIntegratedCircuit);
        this.shareInventory = new NotifiableItemStackHandler(this, 9, IO.IN, IO.NONE);
        this.shareTank = new NotifiableFluidTank(this, 9, 8 * FluidType.BUCKET_VOLUME, IO.IN, IO.NONE);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (getLevel() instanceof ServerLevel serverLevel) {
            serverLevel.getServer().tell(new TickTask(1, () -> {
                for (int i = 0; i < patternInventory.getSlots(); i++) {
                    var pattern = patternInventory.getStackInSlot(i);
                    var patternDetails = PatternDetailsHelper.decodePattern(pattern, getLevel());
                    if (patternDetails != null) {
                        this.detailsSlotMap.put(patternDetails, this.internalInventory[i]);
                    }
                }
            }));
        }
        getRecipeHandlers().forEach(handler -> handler.addChangedListener(() -> getProxies().forEach(proxy -> {
            if (handler.getCapability() == ItemRecipeCapability.CAP) {
                proxy.itemProxyHandler.notifyListeners();
            } else {
                proxy.fluidProxyHandler.notifyListeners();
            }
        })));
    }

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
        proxies.add(proxy.getPos());
    }

    public void removeProxy(MEPatternBufferProxyPartMachine proxy) {
        proxies.remove(proxy.getPos());
    }

    public Set<MEPatternBufferProxyPartMachine> getProxies() {
        Set<MEPatternBufferProxyPartMachine> proxies1 = new HashSet<>();
        for (var pos : proxies) {
            if (MetaMachine.getMachine(getLevel(), pos) instanceof MEPatternBufferProxyPartMachine p) {
                proxies1.add(p);
            }
        }
        return proxies1;
    }

    private void refundAll(ClickData clickData) {
        if (!clickData.isRemote) {
            for (InternalSlot internalSlot : internalInventory) {
                internalSlot.refund();
            }
        }
    }

    private void onPatternChange(int index) {
        if (isRemote()) return;

        // remove old if applicable
        var internalInv = internalInventory[index];
        var newPattern = patternInventory.getStackInSlot(index);
        var newPatternDetails = PatternDetailsHelper.decodePattern(newPattern, getLevel());
        var oldPatternDetails = detailsSlotMap.inverse().get(internalInv);
        detailsSlotMap.forcePut(newPatternDetails, internalInv);
        if (oldPatternDetails != null && !oldPatternDetails.equals(newPatternDetails)) {
            internalInv.refund();
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
        configuratorPanel.attachConfigurators(new CircuitFancyConfigurator(circuitInventorySimulated.storage));
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
        return detailsSlotMap.keySet().stream().filter(Objects::nonNull).toList();
    }

    @Override
    public boolean pushPattern(IPatternDetails patternDetails, KeyCounter[] inputHolder) {
        if (!getMainNode().isActive() || !detailsSlotMap.containsKey(patternDetails) || !checkInput(inputHolder)) {
            return false;
        }

        var slot = detailsSlotMap.get(patternDetails);
        if (slot != null) {
            slot.pushPattern(patternDetails, inputHolder);
            recipeHandler.onChanged();
            return true;
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
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
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
        List<IMultiController> controllers = getControllers();
        // has controller
        if (!controllers.isEmpty()) {
            IMultiController controller = controllers.get(0);
            MultiblockMachineDefinition controllerDefinition = controller.self().getDefinition();
            // has customName
            if (!customName.isEmpty()) {
                return new PatternContainerGroup(
                        AEItemKey.of(controllerDefinition.asStack()),
                        Component.literal(customName),
                        Collections.emptyList());
            } else {
                ItemStack circuitStack = circuitInventorySimulated.storage.getStackInSlot(0);
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
    public void onMachineRemoved() {
        clearInventory(patternInventory);
        clearInventory(shareInventory);
    }

    public class InternalSlot implements ITagSerializable<CompoundTag>, IContentChangeAware {

        @Getter
        @Setter
        protected Runnable onContentsChanged = () -> {
            /**/
        };

        private final Set<ItemStack> itemInventory;
        private final Set<FluidStack> fluidInventory;

        public InternalSlot() {
            this.itemInventory = new HashSet<>();
            this.fluidInventory = new HashSet<>();
        }

        public boolean isItemEmpty() {
            return itemInventory.isEmpty();
        }

        public boolean isFluidEmpty() {
            return fluidInventory.isEmpty();
        }

        private void addItem(AEItemKey key, long amount) {
            if (amount <= 0L) return;
            for (ItemStack item : itemInventory) {
                if (key.matches(item)) {
                    long sum = item.getCount() + amount;
                    if (sum <= Integer.MAX_VALUE) {
                        item.grow((int) amount);
                    } else {
                        itemInventory.remove(item);
                        itemInventory.addAll(List.of(AEUtil.toItemStacks(key, sum)));
                    }
                    return;
                }
            }
            itemInventory.addAll(List.of(AEUtil.toItemStacks(key, amount)));
            recipeHandler.getItemInputHandler().notifyListeners();
        }

        private void addFluid(AEFluidKey key, int amount) {
            if (amount <= 0L) return;
            for (FluidStack fluid : fluidInventory) {
                if (key.matches(fluid)) {
                    int free = Integer.MAX_VALUE - fluid.getAmount();
                    if (amount <= free) {
                        fluid.grow(amount);
                    } else {
                        fluid.setAmount(Integer.MAX_VALUE);
                        fluidInventory.add(AEUtil.toFluidStack(key, amount - free));
                    }
                    return;
                }
            }
            fluidInventory.add(AEUtil.toFluidStack(key, amount));
            recipeHandler.getFluidInputHandler().notifyListeners();
        }

        public ItemStack[] getItemInputs() {
            return ArrayUtils.addAll(itemInventory.toArray(new ItemStack[0]));
        }

        public FluidStack[] getFluidInputs() {
            return ArrayUtils.addAll(fluidInventory.toArray(new FluidStack[0]));
        }

        public void refund() {
            var network = getMainNode().getGrid();
            if (network != null) {
                MEStorage networkInv = network.getStorageService().getInventory();
                var energy = network.getEnergyService();
                for (ItemStack stack : itemInventory) {
                    if (stack == null) continue;

                    var key = AEItemKey.of(stack);
                    if (key == null) continue;

                    long inserted = StorageHelper.poweredInsert(energy, networkInv, key, stack.getCount(),
                            actionSource);
                    if (inserted > 0) {
                        stack.shrink((int) inserted);
                        if (stack.isEmpty()) {
                            itemInventory.remove(stack);
                        }
                    }
                }

                for (FluidStack stack : fluidInventory) {
                    if (stack == null || stack.isEmpty()) continue;

                    int inserted = GTMath.saturatedCast(StorageHelper.poweredInsert(
                            energy,
                            networkInv,
                            AEFluidKey.of(stack.getFluid(), stack.getTag()),
                            stack.getAmount(),
                            actionSource));
                    if (inserted > 0) {
                        stack.shrink(inserted);
                        if (stack.isEmpty()) {
                            fluidInventory.remove(stack);
                        }
                    }
                }
                onContentsChanged.run();
            }
        }

        public void pushPattern(IPatternDetails patternDetails, KeyCounter[] inputHolder) {
            patternDetails.pushInputsToExternalInventory(inputHolder, (what, amount) -> {
                if (what instanceof AEFluidKey key) {
                    addFluid(key, GTMath.saturatedCast(amount));
                }

                if (what instanceof AEItemKey key) {
                    addItem(key, amount);
                }
            });
            onContentsChanged.run();
        }

        public @Nullable List<Ingredient> handleItemInternal(List<Ingredient> left, boolean simulate) {
            Iterator<Ingredient> iterator = left.iterator();
            while (iterator.hasNext()) {
                Ingredient ingredient = iterator.next();
                SLOT_LOOKUP:
                for (ItemStack stack : itemInventory) {
                    if (ingredient.test(stack)) {
                        ItemStack[] ingredientStacks = ingredient.getItems();
                        for (ItemStack ingredientStack : ingredientStacks) {
                            if (ingredientStack.is(stack.getItem())) {
                                int extracted = Math.min(ingredientStack.getCount(), stack.getCount());
                                if (!simulate) {
                                    stack.shrink(extracted);
                                    if (stack.isEmpty()) {
                                        itemInventory.remove(stack);
                                    }
                                    onContentsChanged.run();
                                }
                                ingredientStack.shrink(extracted);
                                if (ingredientStack.isEmpty()) {
                                    iterator.remove();
                                    break SLOT_LOOKUP;
                                }
                            }
                        }
                    }
                }
            }
            return left.isEmpty() ? null : left;
        }

        public @Nullable List<FluidIngredient> handleFluidInternal(List<FluidIngredient> left, boolean simulate) {
            Iterator<FluidIngredient> iterator = left.iterator();
            while (iterator.hasNext()) {
                FluidIngredient fluidStack = iterator.next();
                if (fluidStack.isEmpty()) {
                    iterator.remove();
                    continue;
                }
                boolean found = false;
                FluidStack foundStack = null;
                for (FluidStack stack : fluidInventory) {
                    if (!fluidStack.test(stack)) {
                        continue;
                    }
                    found = true;
                    foundStack = stack;
                }
                if (!found) continue;
                int drained = Math.min(foundStack.getAmount(), fluidStack.getAmount());
                if (!simulate) {
                    foundStack.shrink(drained);
                    if (foundStack.isEmpty()) {
                        fluidInventory.remove(foundStack);
                    }
                    onContentsChanged.run();
                }

                fluidStack.setAmount(fluidStack.getAmount() - drained);
                if (fluidStack.getAmount() <= 0) {
                    iterator.remove();
                }
            }
            return left.isEmpty() ? null : left;
        }

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag tag = new CompoundTag();

            ListTag itemInventoryTag = new ListTag();
            for (ItemStack itemStack : this.itemInventory) {
                itemInventoryTag.add(GTUtil.saveItemStack(itemStack, new CompoundTag()));
            }
            tag.put("inventory", itemInventoryTag);

            ListTag fluidInventoryTag = new ListTag();
            for (FluidStack fluidStack : fluidInventory) {
                fluidInventoryTag.add(fluidStack.writeToNBT(new CompoundTag()));
            }
            tag.put("fluidInventory", fluidInventoryTag);

            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {
            ListTag inv = tag.getList("inventory", Tag.TAG_COMPOUND);
            for (int i = 0; i < inv.size(); i++) {
                CompoundTag tagItemStack = inv.getCompound(i);
                var item = GTUtil.loadItemStack(tagItemStack);
                if (item != null) {
                    if (!item.isEmpty()) {
                        itemInventory.add(item);
                    }
                } else {
                    GTCEu.LOGGER
                            .warn(
                                    "An error occurred while loading contents of ME Crafting Input Bus. This item has been voided: " +
                                            tagItemStack);
                }
            }
            ListTag fluidInv = tag.getList("fluidInventory", Tag.TAG_COMPOUND);
            for (int i = 0; i < fluidInv.size(); i++) {
                CompoundTag tagFluidStack = fluidInv.getCompound(i);
                var fluid = FluidStack.loadFluidStackFromNBT(tagFluidStack);
                if (fluid != null) {
                    if (!fluid.isEmpty()) {
                        fluidInventory.add(fluid);
                    }
                } else {
                    GTCEu.LOGGER
                            .warn(
                                    "An error occurred while loading contents of ME Crafting Input Bus. This fluid has been voided: " +
                                            tagFluidStack);
                }
            }
        }
    }
}

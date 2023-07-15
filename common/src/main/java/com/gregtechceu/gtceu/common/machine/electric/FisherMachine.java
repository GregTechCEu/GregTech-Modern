package com.gregtechceu.gtceu.common.machine.electric;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.WidgetUtils;
import com.gregtechceu.gtceu.api.gui.editor.EditableMachineUI;
import com.gregtechceu.gtceu.api.gui.editor.EditableUI;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.TieredEnergyMachine;
import com.gregtechceu.gtceu.api.machine.feature.IAutoOutputItem;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.syncdata.RequireRerender;
import com.gregtechceu.gtceu.data.lang.LangHandler;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.gui.widget.*;
import com.lowdragmc.lowdraglib.misc.ItemStackTransfer;
import com.lowdragmc.lowdraglib.side.item.ItemTransferHelper;
import com.lowdragmc.lowdraglib.syncdata.ISubscription;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.DropSaved;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import com.lowdragmc.lowdraglib.utils.Position;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.BiFunction;

/**
 * @author h3tr
 * @date 2023/7/13
 * @implNote FisherMachine
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FisherMachine extends TieredEnergyMachine implements IAutoOutputItem, IFancyUIMachine {
    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(FisherMachine.class, TieredEnergyMachine.MANAGED_FIELD_HOLDER);


    @Getter @Persisted @DescSynced @RequireRerender
    protected Direction outputFacingItems;
    @Getter @Persisted @DescSynced @RequireRerender
    protected boolean autoOutputItems;
    @Persisted @DropSaved
    protected final NotifiableItemStackHandler cache;
    @Getter @Setter @Persisted
    protected boolean allowInputFromOutputSideItems;
    @Persisted @DropSaved
    protected final NotifiableItemStackHandler baitHandler;

    @Getter  @Persisted
    protected final ItemStackTransfer chargerInventory;
    @Nullable
    protected TickableSubscription autoOutputSubs, batterySubs;
    @Nullable
    protected ISubscription exportItemSubs, energySubs;
    private final long energyPerTick;

    private final int inventorySize;
    public final long fishingTicks;
    public static final int WATER_CHECK_SIZE = 5;
    private static final ItemStack fishingRod = new ItemStack(Items.FISHING_ROD);

    public FisherMachine(IMachineBlockEntity holder, int tier, Object... args) {
        super(holder, tier);
        this.inventorySize = (tier + 1) * (tier + 1);
        this.fishingTicks = 1000 - tier * 200L;
        this.energyPerTick = GTValues.V[tier - 1];
        this.cache = createCacheItemHandler(args);
        this.baitHandler = createBaitItemHandler(args);
        this.chargerInventory = createChargerItemHandler();
        setOutputFacingItems(getFrontFacing());




    }

    //////////////////////////////////////
    //*****     Initialization     *****//
    //////////////////////////////////////

    protected ItemStackTransfer createChargerItemHandler(Object... args) {
        var transfer = new ItemStackTransfer();
        transfer.setFilter(item -> GTCapabilityHelper.getElectricItem(item) != null);
        return transfer;
    }
    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    protected NotifiableItemStackHandler createCacheItemHandler(Object... args) {
        return new NotifiableItemStackHandler(this, inventorySize, IO.BOTH, IO.OUT);
    }

    protected NotifiableItemStackHandler createBaitItemHandler(Object... args) {
        return new NotifiableItemStackHandler(this, 1, IO.BOTH, IO.BOTH);
    }
    @Override
    public void onLoad() {
        super.onLoad();
        subscribeServerTick(this::update);
        exportItemSubs = cache.addChangedListener(this::updateAutoOutputSubscription);
        energySubs = energyContainer.addChangedListener(this::updateBatterySubscription);
        chargerInventory.setOnContentsChanged(this::updateBatterySubscription);

    }

    @Override
    public void onUnload() {
        super.onUnload();
        if (energySubs != null) {
            energySubs.unsubscribe();
            energySubs = null;
        }
        if (exportItemSubs != null) {
            exportItemSubs.unsubscribe();
            exportItemSubs = null;
        }
    }

    @Override
    public boolean shouldWeatherOrTerrainExplosion() {
        return false;
    }

    //////////////////////////////////////
    //*********     Logic     **********//
    //////////////////////////////////////



    public void update() {
        this.cache.exportToNearby(getOutputFacingItems());

        //do not do anything if requirements are not met
        if (this.energyContainer.getEnergyStored() < GTValues.V[getTier()] || !this.baitHandler.getStackInSlot(0).is(Items.STRING))
            return;

        drainEnergy(false);

        if(this.getOffsetTimer() % this.fishingTicks != 0)
            return;


        int waterCount = 0;
        int edgeSize = WATER_CHECK_SIZE;
        for (int x = 0; x < edgeSize; x++) {
            for (int z = 0; z < edgeSize; z++) {
                BlockPos waterCheckPos = getPos().below().offset(x - edgeSize / 2, 0, z - edgeSize / 2);
                if (getLevel().getBlockState(waterCheckPos).getBlock().equals(Blocks.WATER) &&
                        getLevel().getBlockState(waterCheckPos).getMaterial() == Material.WATER) {
                    waterCount++;
                }
            }
        }
        if(waterCount<WATER_CHECK_SIZE*WATER_CHECK_SIZE)
            return;

        LootTable lootTable = getLevel().getServer().getLootTables().get(BuiltInLootTables.FISHING);

        FishingHook simulatedHook = new FishingHook(EntityType.FISHING_BOBBER, getLevel()) {
            public boolean isOpenWaterFishing() {
                return true;
            }
        };

        LootContext lootContext = new LootContext.Builder((ServerLevel) getLevel())
                .withOptionalParameter(LootContextParams.THIS_ENTITY,simulatedHook)
                .withParameter(LootContextParams.TOOL, fishingRod)
                .withParameter(LootContextParams.ORIGIN,new Vec3(getPos().getX(),getPos().getY(),getPos().getZ()))
                .create(LootContextParamSets.FISHING);


        NonNullList<ItemStack> generatedLoot = NonNullList.create();
        generatedLoot.addAll(lootTable.getRandomItems(lootContext));

        boolean useBait = false;
        for (ItemStack itemStack : generatedLoot)
            useBait |= tryFillCache(itemStack);


        if(useBait)
            this.baitHandler.extractItem(0,1,false);

    }

    private boolean tryFillCache(ItemStack stack){
        for(int i = 0; i < cache.getSlots(); i++)
        {
            if(cache.insertItemInternal(i,stack,true).getCount()==stack.getCount())
                continue;
            cache.insertItemInternal(i,stack,false);
            return true;
        }
        return false;
    }


    @Override
    public void setAutoOutputItems(boolean allow) {
        this.autoOutputItems = allow;
        updateAutoOutputSubscription();
    }

    @Override
    public void setOutputFacingItems(@Nullable Direction outputFacing) {
        this.outputFacingItems = outputFacing;
        updateAutoOutputSubscription();
    }

    protected void updateBatterySubscription() {
        if (energyContainer.dischargeOrRechargeEnergyContainers(chargerInventory, 0, true))
            batterySubs = subscribeServerTick(batterySubs, this::chargeBattery);
        else if (batterySubs != null) {
            batterySubs.unsubscribe();
            batterySubs = null;
        }
    }

    protected void updateAutoOutputSubscription() {
        var outputFacing = getOutputFacingItems();
        if ((isAutoOutputItems() && !cache.isEmpty()) && outputFacing != null
                && ItemTransferHelper.getItemTransfer(getLevel(), getPos().relative(outputFacing), outputFacing.getOpposite()) != null)
            autoOutputSubs = subscribeServerTick(autoOutputSubs, this::checkAutoOutput);
        else if (autoOutputSubs != null) {
            autoOutputSubs.unsubscribe();
            autoOutputSubs = null;
        }
    }

    protected void checkAutoOutput() {
        if (getOffsetTimer() % 5 == 0) {
            if (isAutoOutputItems() && getOutputFacingItems() != null)
                cache.exportToNearby(getOutputFacingItems());
            updateAutoOutputSubscription();
        }
    }

    protected void chargeBattery() {
        if (!energyContainer.dischargeOrRechargeEnergyContainers(chargerInventory, 0, false))
            updateBatterySubscription();
    }

    public boolean drainEnergy(boolean simulate) {
        long resultEnergy = energyContainer.getEnergyStored() - energyPerTick;
        if (resultEnergy >= 0L && resultEnergy <= energyContainer.getEnergyCapacity()) {
            if (!simulate)
                energyContainer.removeEnergy(energyPerTick);
            return true;
        }
        return false;
    }


    //////////////////////////////////////
    //**********     Gui     ***********//
    //////////////////////////////////////

    public static BiFunction<ResourceLocation, Integer, EditableMachineUI> EDITABLE_UI_CREATOR = Util.memoize((path, inventorySize)-> new EditableMachineUI("misc", path, () -> {
        var template =  createTemplate(inventorySize).createDefault();
        var energyBar = createEnergyBar().createDefault();
        var batterySlot = createBatterySlot().createDefault();
        var energyGroup = new WidgetGroup(0, 0, energyBar.getSize().width, energyBar.getSize().height + 20);
        batterySlot.setSelfPosition(new Position((energyBar.getSize().width - 18) / 2, energyBar.getSize().height + 1));
        energyGroup.addWidget(energyBar);
        energyGroup.addWidget(batterySlot);
        var group = new WidgetGroup(0, 0,
                Math.max(energyGroup.getSize().width + template.getSize().width + 12, 110),
                Math.max(template.getSize().height + 8, energyGroup.getSize().height + 8));
        var size = group.getSize();
        energyGroup.setSelfPosition(new Position(3, (size.height - energyGroup.getSize().height) / 2));

        template.setSelfPosition(new Position(
                energyGroup.getSize().width + 4,
                (size.height - template.getSize().height) / 2));

        group.addWidget(energyGroup);
        group.addWidget(template);
        return group;
    }, (template, machine) -> {
        if (machine instanceof FisherMachine fisherMachine) {
            createTemplate(inventorySize).setupUI(template, fisherMachine);
            createEnergyBar().setupUI(template, fisherMachine);
            createBatterySlot().setupUI(template, fisherMachine);
        }
    }));

    protected static EditableUI<SlotWidget, FisherMachine> createBatterySlot() {
        return new EditableUI<>("battery_slot", SlotWidget.class, () -> {
            var slotWidget = new SlotWidget();
            slotWidget.setBackground(GuiTextures.SLOT, GuiTextures.CHARGER_OVERLAY);
            return slotWidget;
        }, (slotWidget, machine) -> {
            slotWidget.setHandlerSlot(machine.chargerInventory, 0);
            slotWidget.setCanPutItems(true);
            slotWidget.setCanTakeItems(true);
            slotWidget.setHoverTooltips(LangHandler.getMultiLang("gtceu.gui.charger_slot.tooltip", GTValues.VNF[machine.getTier()], GTValues.VNF[machine.getTier()]).toArray(new MutableComponent[0]));
        });
    }


    protected static EditableUI<WidgetGroup, FisherMachine> createTemplate(int inventorySize) {
        return new EditableUI<>("functional_container", WidgetGroup.class, () -> {
            int rowSize = (int) Math.sqrt(inventorySize);
            int width = rowSize * 18 + 55;
            int height = rowSize>4?95:80;
            WidgetGroup main = new WidgetGroup(0, 0, width, height);


            WidgetGroup slots = new WidgetGroup(55,4,rowSize,rowSize);
            for (int y = 0; y < rowSize; y++)
                for (int x = 0; x < rowSize; x++) {
                    int index = y * rowSize + x;
                    SlotWidget slotWidget = new SlotWidget();
                    slotWidget.initTemplate();
                    slotWidget.setSelfPosition(new Position(x * 18, y * 18));
                    slotWidget.setBackground(GuiTextures.SLOT);
                    slotWidget.setId("slot_" + index);
                    slots.addWidget(slotWidget);
                }
            main.addWidget(slots);

            SlotWidget baitSlotWidget = new SlotWidget();
            baitSlotWidget.initTemplate();
            baitSlotWidget.setSelfPosition(new Position(10,4));
            baitSlotWidget.setBackground(GuiTextures.SLOT);
            baitSlotWidget.setOverlay(GuiTextures.STRING_SLOT_OVERLAY);
            baitSlotWidget.setId("bait_slot");
            main.addWidget(baitSlotWidget);

            return main;
        }, (group, machine) -> {
            WidgetUtils.widgetByIdForEach(group, "^slot_[0-9]+$", SlotWidget.class, slot -> {
                var index = WidgetUtils.widgetIdIndex(slot);
                if (index >= 0 && index < machine.cache.getSlots()) {
                    slot.setHandlerSlot(machine.cache, index);
                    slot.setCanTakeItems(true);
                    slot.setCanPutItems(false);
                }
            });
            SlotWidget baitSlot = (SlotWidget) WidgetUtils.getFirstWidgetById(group,"bait_slot");
            assert baitSlot != null;
            baitSlot.setHandlerSlot(machine.baitHandler, 0);
        });
    }

    //////////////////////////////////////
    //*******     Rendering     ********//
    //////////////////////////////////////
    @Override
    public ResourceTexture sideTips(Player player, GTToolType toolType, Direction side) {
        if (toolType == GTToolType.WRENCH && player.isCrouching() &&hasFrontFacing() && side != this.getFrontFacing() && isFacingValid(side))
            return GuiTextures.TOOL_IO_FACING_ROTATION;
        else if (toolType == GTToolType.SCREWDRIVER && side == getOutputFacingItems())
            return GuiTextures.TOOL_ALLOW_INPUT;

        return super.sideTips(player, toolType, side);
    }

    //////////////////////////////////////
    //*******    Interactions   ********//
    //////////////////////////////////////

    @Override
    protected InteractionResult onScrewdriverClick(Player playerIn, InteractionHand hand, Direction gridSide, BlockHitResult hitResult) {
        if (isRemote())
            return super.onScrewdriverClick(playerIn, hand, gridSide, hitResult);

        if (gridSide != getOutputFacingItems())
            return InteractionResult.SUCCESS;
        boolean IfromOItems = isAllowInputFromOutputSideItems();

        playerIn.sendSystemMessage(Component.translatable("gtceu.machine.basic.input_from_output_side." + (IfromOItems ? "allow" : "disallow"))
                .append(Component.translatable("gtceu.creative.chest.item")));
        setAllowInputFromOutputSideItems(!IfromOItems);

        return InteractionResult.SUCCESS;


    }
}

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
import com.lowdragmc.lowdraglib.gui.widget.SlotWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.misc.ItemStackTransfer;
import com.lowdragmc.lowdraglib.side.item.ItemTransferHelper;
import com.lowdragmc.lowdraglib.syncdata.ISubscription;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.DropSaved;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import com.lowdragmc.lowdraglib.utils.Position;
import lombok.Getter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.function.BiFunction;
/**
 * @author h3tr
 * @date 2023/7/15
 * @implNote BlockBreakerMachine
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class BlockBreakerMachine extends TieredEnergyMachine implements IAutoOutputItem, IFancyUIMachine {
    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(BlockBreakerMachine.class, TieredEnergyMachine.MANAGED_FIELD_HOLDER);


    @Getter @Persisted @DescSynced @RequireRerender
    protected Direction outputFacingItems;
    @Getter @Persisted @DescSynced @RequireRerender
    protected boolean autoOutputItems;
    @Persisted @DropSaved
    protected final NotifiableItemStackHandler cache;
    @Getter  @Persisted
    protected final ItemStackTransfer chargerInventory;
    @Nullable
    protected TickableSubscription autoOutputSubs, batterySubs;
    @Nullable
    protected ISubscription exportItemSubs, energySubs;
    private final int inventorySize;

    private int blockBreakProgress = 0;
    private float currentHardness;
    private int blockBreakTime = 0;

    private final long energyPerTick;

    public final float efficiencyMultiplier;



    public BlockBreakerMachine(IMachineBlockEntity holder, int tier, Object... args) {
        super(holder, tier);
        this.inventorySize = (tier + 1) * (tier + 1);
        this.cache = createCacheItemHandler(args);
        this.chargerInventory = createChargerItemHandler();
        this.energyPerTick = GTValues.V[tier - 1];
        setOutputFacingItems(getFrontFacing().getOpposite());
        this.efficiencyMultiplier = 1.0f - getEfficiencyMultiplier(tier);

    }

    public static float getEfficiencyMultiplier(int tier){
        float efficiencyMultiplier = 1.0f - 0.2f * (tier - 1.0f);
        //Clamp efficiencyMultiplier
        if(efficiencyMultiplier>1.0f)
            efficiencyMultiplier = 1.0f;
        else if (efficiencyMultiplier<.1f)
            efficiencyMultiplier = .1f;
        efficiencyMultiplier = 1.0f - efficiencyMultiplier;
        return efficiencyMultiplier;
    }

    //////////////////////////////////////
    //*****     Initialization     *****//
    //////////////////////////////////////

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    protected ItemStackTransfer createChargerItemHandler(Object... args) {
        var transfer = new ItemStackTransfer();
        transfer.setFilter(item -> GTCapabilityHelper.getElectricItem(item) != null);
        return transfer;
    }

    protected NotifiableItemStackHandler createCacheItemHandler(Object... args) {
        return new NotifiableItemStackHandler(this, inventorySize, IO.BOTH, IO.OUT);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        subscribeServerTick(this::update);
        exportItemSubs = cache.addChangedListener(this::updateAutoOutputSubscription);
        energySubs = energyContainer.addChangedListener(this::updateBatterySubscription);

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

    //////////////////////////////////////
    //*********     Logic     **********//
    //////////////////////////////////////



    public void update() {
        if (this.energyContainer.getEnergyStored() < GTValues.V[getTier()])
            return;

        if(this.blockBreakProgress>0) {
            --this.blockBreakProgress;
            drainEnergy(false);
            BlockPos pos = getPos().offset(getFrontFacing().getNormal());
            BlockState blockState = getLevel().getBlockState(pos);
            getLevel().addDestroyBlockEffect(pos,blockState);
            if(blockBreakProgress==0){
                float hardness = blockState.getBlock().defaultDestroyTime();
                if(hardness>=0.0f && Math.abs(hardness - currentHardness)<.5f){
                    List<ItemStack> drops = tryDestroyBlockAndGetDrops(pos);
                    double itemSpawnX = getPos().getX() + 0.5 + getOutputFacingItems().getStepX();
                    double itemSpawnY = getPos().getY() + 0.5 + getOutputFacingItems().getStepY();
                    double itemSpawnZ = getPos().getZ() + 0.5 + getOutputFacingItems().getStepZ();
                    for (ItemStack drop: drops) {
                        ItemStack remainder = tryFillCache(drop);
                        if(!remainder.isEmpty()){
                            ItemEntity itemEntity = new ItemEntity(getLevel(),itemSpawnX,itemSpawnY,itemSpawnZ,remainder);
                            itemEntity.setDefaultPickUpDelay();
                            getLevel().addFreshEntity(itemEntity);
                        }

                    }
                }
                this.blockBreakProgress = 0;
                this.currentHardness = 0f;
            }
        }
        if(blockBreakProgress==0&&getLevel().hasNeighborSignal(getPos())){
            BlockPos pos = getPos().offset(getFrontFacing().getNormal());
            BlockState blockState = getLevel().getBlockState(pos);
            float hardness = blockState.getBlock().defaultDestroyTime();
            boolean skipBlock = blockState.getMaterial() == Material.AIR;
            if(hardness>=0f&&!skipBlock){
                int ticksPerOneDurability = 5;
                int totalTicksPerBlock = (int) Math.ceil(ticksPerOneDurability * hardness);

                this.blockBreakTime = (int) Math.ceil(totalTicksPerBlock * this.efficiencyMultiplier);
                this.blockBreakProgress = this.blockBreakTime;
                this.currentHardness = hardness;
            }
        }
    }




    private List<ItemStack> tryDestroyBlockAndGetDrops(BlockPos pos){
        List<ItemStack> drops = Block.getDrops(getLevel().getBlockState(pos), (ServerLevel) getLevel(), pos, null,null, ItemStack.EMPTY);
        getLevel().destroyBlock(pos,false);
        return drops;
    }

    private ItemStack tryFillCache(ItemStack stack){
        for(int i = 0; i < cache.getSlots(); i++)
        {
            if(cache.insertItemInternal(i,stack,true).getCount()==stack.getCount())
                continue;
            return tryFillCache(cache.insertItemInternal(i,stack,false));
        }
        return stack;
    }

    @Override
    public void setAutoOutputItems(boolean allow) {
        this.autoOutputItems = allow;
        updateAutoOutputSubscription();
    }

    @Override
    public boolean isAllowInputFromOutputSideItems() {
        return false;
    }

    @Override
    public void setAllowInputFromOutputSideItems(boolean allow) {}

    @Override
    public void setOutputFacingItems(@Nullable Direction outputFacing) {
        this.outputFacingItems = outputFacing;
        updateAutoOutputSubscription();
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

    protected void updateBatterySubscription() {
        if (energyContainer.dischargeOrRechargeEnergyContainers(chargerInventory, 0, true))
            batterySubs = subscribeServerTick(batterySubs, this::chargeBattery);
        else if (batterySubs != null) {
            batterySubs.unsubscribe();
            batterySubs = null;
        }
    }

    protected void chargeBattery() {
        if (!energyContainer.dischargeOrRechargeEnergyContainers(chargerInventory, 0, false))
            updateBatterySubscription();
    }

    @Override
    public boolean shouldWeatherOrTerrainExplosion() {
        return false;
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
        if (machine instanceof BlockBreakerMachine blockBreakerMachine) {
            createTemplate(inventorySize).setupUI(template, blockBreakerMachine);
            createEnergyBar().setupUI(template, blockBreakerMachine);
            createBatterySlot().setupUI(template, blockBreakerMachine);
        }
    }));

    protected static EditableUI<SlotWidget, BlockBreakerMachine> createBatterySlot() {
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


    protected static EditableUI<WidgetGroup, BlockBreakerMachine> createTemplate(int inventorySize) {
        return new EditableUI<>("functional_container", WidgetGroup.class, () -> {
            int rowSize = (int) Math.sqrt(inventorySize);
            int width = rowSize * 18 + 20;
            int height = rowSize>4?95:80;
            WidgetGroup main = new WidgetGroup(0, 0, width, height);


            WidgetGroup slots = new WidgetGroup(20,4,rowSize,rowSize);
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
        });
    }

    //////////////////////////////////////
    //*******     Rendering     ********//
    //////////////////////////////////////
    @Override
    public ResourceTexture sideTips(Player player, GTToolType toolType, Direction side) {
        if (toolType == GTToolType.WRENCH && player.isCrouching() &&hasFrontFacing() && side != this.getFrontFacing() && isFacingValid(side))
            return GuiTextures.TOOL_IO_FACING_ROTATION;

        return super.sideTips(player, toolType, side);
    }

}

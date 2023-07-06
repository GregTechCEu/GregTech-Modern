package com.gregtechceu.gtceu.common.machine.electric;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IControllable;
import com.gregtechceu.gtceu.api.capability.IMiner;
import com.gregtechceu.gtceu.api.capability.impl.MinerLogic;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.WorkableTieredMachine;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.data.lang.LangHandler;
import com.lowdragmc.lowdraglib.gui.widget.*;
import com.lowdragmc.lowdraglib.misc.ItemStackTransfer;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import com.lowdragmc.lowdraglib.utils.Position;
import lombok.Getter;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class MinerMachine extends WorkableTieredMachine implements IMiner, IControllable, IFancyUIMachine {
    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(MinerMachine.class, WorkableTieredMachine.MANAGED_FIELD_HOLDER);


    @Getter
    @Persisted
    protected final ItemStackTransfer chargerInventory;

    private final int inventorySize;
    private final long energyPerTick;
    private boolean isInventoryFull = false;
    @Nullable
    protected TickableSubscription subscription;

    public MinerMachine(IMachineBlockEntity holder, int tier, int speed, int maximumRadius, int fortune, Object... args) {
        super(holder, tier, GTMachines.defaultTankSizeFunction, args, (tier + 1) * (tier + 1), fortune, speed, maximumRadius);
        this.inventorySize = (tier + 1) * (tier + 1);
        this.energyPerTick = GTValues.V[tier - 1];
        this.chargerInventory = createChargerItemHandler();
    }

    protected ItemStackTransfer createChargerItemHandler(Object... args) {
        var transfer = new ItemStackTransfer();
        transfer.setFilter(item -> GTCapabilityHelper.getElectricItem(item) != null);
        return transfer;
    }

    @Override
    protected NotifiableItemStackHandler createImportItemHandler(Object... args) {
        return new NotifiableItemStackHandler(this, 0, IO.NONE);
    }

    @Override
    protected NotifiableItemStackHandler createExportItemHandler(Object... args) {
        if (args.length > 3 && args[args.length - 4] instanceof Integer invSize) {
            return new NotifiableItemStackHandler(this, invSize, IO.OUT, IO.BOTH);
        }
        throw new IllegalArgumentException("MinerMachine need args [inventorySize, fortune, speed, maximumRadius] for initialization");
    }

    @Override
    protected RecipeLogic createRecipeLogic(Object... args) {
        if (args.length > 2 && args[args.length - 3] instanceof Integer fortune && args[args.length - 2] instanceof Integer speed && args[args.length - 1] instanceof Integer maxRadius) {
            return new MinerLogic(this, fortune, speed, maxRadius);
        }
        throw new IllegalArgumentException("MinerMachine need args [inventorySize, fortune, speed, maximumRadius] for initialization");
    }

    @Override
    public Widget createUIWidget() {
        int rowSize = (int) Math.sqrt(inventorySize);
        WidgetGroup group = new WidgetGroup(0, 0, 180, 84);

        WidgetGroup slots = new WidgetGroup(160 - rowSize * 9, 18, rowSize * 18, rowSize * 18);
        for (int y = 0; y < rowSize; y++) {
            for (int x = 0; x < rowSize; x++) {
                int index = y * rowSize + x;
                slots.addWidget(new SlotWidget(exportItems, index, x * 18, y * 18, true, false)
                        .setBackgroundTexture(GuiTextures.SLOT));
            }
        }

        var energyBar = createEnergyBar();
        var batterySlot = createBatterySlot();
        var energyGroup = new WidgetGroup(2, 3, energyBar.getSize().width, energyBar.getSize().height + 20);
        batterySlot.setSelfPosition(new Position((energyBar.getSize().width - 18) / 2, energyBar.getSize().height + 1));
        energyGroup.addWidget(energyBar);
        energyGroup.addWidget(batterySlot);

        WidgetGroup text = new WidgetGroup(32, 6, 108, 70);
        text.addWidget(new ImageWidget(0, 0, 108, 70, GuiTextures.DISPLAY));

        text.addWidget(new ComponentPanelWidget(4, 7, this::addDisplayText));
        text.addWidget(new ComponentPanelWidget(64, 7, this::addDisplayText2));

        group.addWidget(slots);
        group.addWidget(text);
        group.addWidget(energyGroup);
        return group;
    }

    /**
     * Create an energy bar widget.
     */
    protected Widget createBatterySlot() {
        return new SlotWidget(chargerInventory, 0, 0, 0, true, true)
                .setBackground(GuiTextures.SLOT, GuiTextures.CHARGER_OVERLAY)
                .setHoverTooltips(LangHandler.getMultiLang("gtceu.gui.charger_slot.tooltip", GTValues.VNF[getTier()], GTValues.VNF[getTier()]).toArray(new MutableComponent[0]));
    }

    private void addDisplayText(@Nonnull List<Component> textList) {
        MinerLogic minerLogic = (MinerLogic) this.recipeLogic;
        int workingArea = IMiner.getWorkingArea(minerLogic.getCurrentRadius());
        textList.add(Component.translatable("gtceu.machine.miner.startx", minerLogic.getX()));
        textList.add(Component.translatable("gtceu.machine.miner.starty", minerLogic.getY()));
        textList.add(Component.translatable("gtceu.machine.miner.startz", minerLogic.getZ()));
        textList.add(Component.translatable("gtceu.universal.tooltip.working_area", workingArea, workingArea));
        if (minerLogic.isDone())
            textList.add(Component.translatable("gtceu.multiblock.large_miner.done").setStyle(Style.EMPTY.withColor(ChatFormatting.GREEN)));
        else if (minerLogic.isWorking())
            textList.add(Component.translatable("gtceu.multiblock.large_miner.working").setStyle(Style.EMPTY.withColor(ChatFormatting.GOLD)));
        else if (!this.isWorkingEnabled())
            textList.add(Component.translatable("gtceu.multiblock.work_paused"));
        if (isInventoryFull)
            textList.add(Component.translatable("gtceu.multiblock.large_miner.invfull").setStyle(Style.EMPTY.withColor(ChatFormatting.RED)));
        if (!drainEnergy(true))
            textList.add(Component.translatable("gtceu.multiblock.large_miner.needspower").setStyle(Style.EMPTY.withColor(ChatFormatting.RED)));
    }

    private void addDisplayText2(@Nonnull List<Component> textList) {
        MinerLogic minerLogic = (MinerLogic) this.recipeLogic;
        textList.add(Component.translatable("gtceu.machine.miner.minex", minerLogic.getMineX()));
        textList.add(Component.translatable("gtceu.machine.miner.miney", minerLogic.getMineY()));
        textList.add(Component.translatable("gtceu.machine.miner.minez", minerLogic.getMineZ()));
    }

    @Override
    public boolean drainEnergy(boolean simulate) {
        long resultEnergy = energyContainer.getEnergyStored() - energyPerTick;
        if (resultEnergy >= 0L && resultEnergy <= energyContainer.getEnergyCapacity()) {
            if (!simulate)
                energyContainer.removeEnergy(energyPerTick);
            if (recipeLogic.isWaiting()) recipeLogic.setStatus(RecipeLogic.Status.WORKING);
            return true;
        }
        recipeLogic.setWaiting(Component.translatable("gtceu.recipe_logic.insufficient_in").append(": ").append(EURecipeCapability.CAP.getTraslateComponent()));
        return false;
    }

    public void update() {
        if (!isRemote()) {
            if (energyContainer.dischargeOrRechargeEnergyContainers(chargerInventory, 0, true)) {
                energyContainer.dischargeOrRechargeEnergyContainers(chargerInventory, 0, false);
            }

            if (getOffsetTimer() % 5 == 0)
                exportItems.exportToNearby(getFrontFacing());
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!isRemote()) {
            subscription = subscribeServerTick(subscription, this::update);
        }
    }

    @Override
    public void onUnload() {
        super.onUnload();
        if (subscription != null) {
            subscription.unsubscribe();
            subscription = null;
        }
    }

    @Override
    protected InteractionResult onScrewdriverClick(Player playerIn, InteractionHand hand, Direction gridSide, BlockHitResult hitResult) {
        if (isRemote()) return InteractionResult.SUCCESS;

        MinerLogic minerLogic = (MinerLogic) this.recipeLogic;
        if (!this.isActive()) {
            int currentRadius = minerLogic.getCurrentRadius();
            if (currentRadius == 1)
                minerLogic.setCurrentRadius(minerLogic.getMaximumRadius());
            else if (playerIn.isCrouching())
                minerLogic.setCurrentRadius(Math.max(1, Math.round(currentRadius / 2.0f)));
            else
                minerLogic.setCurrentRadius(Math.max(1, currentRadius - 1));

            minerLogic.resetArea();

            int workingArea = IMiner.getWorkingArea(minerLogic.getCurrentRadius());
            playerIn.sendSystemMessage(Component.translatable("gtceu.universal.tooltip.working_area", workingArea, workingArea));
        } else {
            playerIn.sendSystemMessage(Component.translatable("gtceu.multiblock.large_miner.errorradius"));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void onDrops(List<ItemStack> drops, Player entity) {
        clearInventory(drops, exportItems.storage);
        clearInventory(drops, chargerInventory);
    }

    @Override
    public boolean isInventoryFull() {
        return isInventoryFull;
    }

    @Override
    public void setInventoryFull(boolean isFull) {
        this.isInventoryFull = isFull;
    }

    @Override
    public boolean shouldWorkingPlaySound() {
        return super.shouldWorkingPlaySound();
    }

    //    @Nonnull
//    @Override
//    public List<Component> getDataInfo() {
//        int workingArea = getWorkingArea(minerLogic.getCurrentRadius());
//        return Collections.singletonList(Component.translatable("gtceu.machine.miner.working_area", workingArea, workingArea));
//    }
}

package com.gregtechceu.gtceu.common.machine.steam;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.IControllable;
import com.gregtechceu.gtceu.api.capability.IMiner;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.UITemplate;
import com.gregtechceu.gtceu.api.gui.widget.PredicatedImageWidget;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.IExhaustVentMachine;
import com.gregtechceu.gtceu.api.machine.feature.IUIMachine;
import com.gregtechceu.gtceu.api.machine.steam.SteamWorkableMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.client.renderer.block.TextureOverrideRenderer;
import com.gregtechceu.gtceu.client.renderer.machine.MinerRenderer;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.machine.trait.miner.SteamMinerLogic;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.widget.ComponentPanelWidget;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.SlotWidget;
import com.lowdragmc.lowdraglib.side.fluid.FluidHelper;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public class SteamMinerMachine extends SteamWorkableMachine implements IMiner, IControllable, IExhaustVentMachine, IUIMachine {

    @Getter
    @Setter
    private GTRecipeType recipeType;

    @Setter
    @Persisted @DescSynced
    private boolean needsVenting;
    @Persisted
    public final NotifiableItemStackHandler importItems;
    @Persisted
    public final NotifiableItemStackHandler exportItems;

    private final int inventorySize;
    private final int energyPerTick;
    private boolean isInventoryFull = false;
    @Nullable
    protected TickableSubscription itemExportSubs;

    public SteamMinerMachine(IMachineBlockEntity holder, int speed, int maximumRadius, int fortune) {
        super(holder, false, fortune, speed, maximumRadius);
        this.recipeType = getDefinition().getRecipeType();
        this.inventorySize = 4;
        this.energyPerTick = 16;
        this.importItems = createImportItemHandler();
        this.exportItems = createExportItemHandler();
    }

    @Override
    protected @NotNull RecipeLogic createRecipeLogic(Object... args) {
        if (args.length > 2 && args[args.length - 3] instanceof Integer fortune && args[args.length - 2] instanceof Integer speed && args[args.length - 1] instanceof Integer maxRadius) {
            return new SteamMinerLogic(this, fortune, speed, maxRadius,  new TextureOverrideRenderer(MinerRenderer.PIPE_MODEL, Map.of("all", GTCEu.id("block/casings/solid/machine_casing_bronze_plated_bricks"))));
        }
        throw new IllegalArgumentException("MinerMachine need args [inventorySize, fortune, speed, maximumRadius] for initialization");
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!isRemote()) {
            itemExportSubs = subscribeServerTick(itemExportSubs, this::update);
        }
    }

    @Override
    public void onUnload() {
        super.onUnload();
        if (itemExportSubs != null) {
            itemExportSubs.unsubscribe();
            itemExportSubs = null;
        }
    }

    @Override
    public SteamMinerLogic getRecipeLogic() {
        return (SteamMinerLogic) super.getRecipeLogic();
    }

    @Override
    protected NotifiableFluidTank createSteamTank(Object... args) {
        return new NotifiableFluidTank(this, 1, 16 * FluidHelper.getBucket(), IO.IN).setFilter(fluidStack -> fluidStack.getFluid().isSame(GTMaterials.Steam.getFluid()));
    }

    protected NotifiableItemStackHandler createImportItemHandler(@SuppressWarnings("unused") Object... args) {
        return new NotifiableItemStackHandler(this, 0, IO.IN);
    }

    protected NotifiableItemStackHandler createExportItemHandler(@SuppressWarnings("unused") Object... args) {
        return new NotifiableItemStackHandler(this, inventorySize, IO.OUT);
    }

    @Override
    public ModularUI createUI(Player entityPlayer) {
        int rowSize = (int) Math.sqrt(inventorySize);

        ModularUI builder = new ModularUI(175, 176, this, entityPlayer)
                .background(GuiTextures.BACKGROUND_STEAM.get(false));
        builder.widget(UITemplate.bindPlayerInventory(entityPlayer.getInventory(), GuiTextures.SLOT_STEAM.get(false), 7, 94, true));

        for (int y = 0; y < rowSize; y++) {
            for (int x = 0; x < rowSize; x++) {
                int index = y * rowSize + x;
                builder.widget(new SlotWidget(exportItems, index, 142 - rowSize * 9 + x * 18, 18 + y * 18, true, false)
                        .setBackgroundTexture(GuiTextures.SLOT_STEAM.get(false)));
            }
        }

        builder.widget(new LabelWidget(5, 5, getBlockState().getBlock().getDescriptionId()));
        builder.widget(new PredicatedImageWidget(79, 42, 18, 18, GuiTextures.INDICATOR_NO_STEAM.get(isHighPressure))
                .setPredicate(recipeLogic::isHasNotEnoughEnergy));
        builder.widget(new ImageWidget(7, 16, 105, 75, GuiTextures.DISPLAY_STEAM.get(false)));
        builder.widget(new ComponentPanelWidget(10, 19, this::addDisplayText)
                .setMaxWidthLimit(84));
        builder.widget(new ComponentPanelWidget(70, 19, this::addDisplayText2)
                .setMaxWidthLimit(84));


        return builder;
    }

    void addDisplayText(List<Component> textList) {
        int workingArea = IMiner.getWorkingArea(getRecipeLogic().getCurrentRadius());
        textList.add(Component.translatable("gtceu.machine.miner.startx", this.getRecipeLogic().getX()));
        textList.add(Component.translatable("gtceu.machine.miner.starty", this.getRecipeLogic().getY()));
        textList.add(Component.translatable("gtceu.machine.miner.startz", this.getRecipeLogic().getZ()));
        textList.add(Component.translatable("gtceu.universal.tooltip.working_area", workingArea, workingArea));
        if (this.getRecipeLogic().isDone())
            textList.add(Component.translatable("gtceu.multiblock.large_miner.done").setStyle(Style.EMPTY.withColor(ChatFormatting.GREEN)));
        else if (this.getRecipeLogic().isWorking())
            textList.add(Component.translatable("gtceu.multiblock.large_miner.working").setStyle(Style.EMPTY.withColor(ChatFormatting.GOLD)));
        else if (!this.isWorkingEnabled())
            textList.add(Component.translatable("gtceu.multiblock.work_paused"));
        if (this.isInventoryFull)
            textList.add(Component.translatable("gtceu.multiblock.large_miner.invfull").setStyle(Style.EMPTY.withColor(ChatFormatting.RED)));
    }

    void addDisplayText2(List<Component> textList) {
        textList.add(Component.translatable("gtceu.machine.miner.minex", this.getRecipeLogic().getMineX()));
        textList.add(Component.translatable("gtceu.machine.miner.miney", this.getRecipeLogic().getMineY()));
        textList.add(Component.translatable("gtceu.machine.miner.minez", this.getRecipeLogic().getMineZ()));
    }

    public boolean drainEnergy(boolean simulate) {
        long resultSteam = steamTank.getFluidInTank(0).getAmount() - energyPerTick;
        if (!this.isVentingBlocked() && resultSteam >= 0L && resultSteam <= steamTank.getTankCapacity(0)) {
            if (!simulate)
                steamTank.drain(energyPerTick, true);
            return true;
        }
        return false;
    }

    public void update() {
        if (!isRemote()) {
            if (getOffsetTimer() % 5 == 0)
                exportItems.exportToNearby(getFrontFacing());
        }
    }

    @Override
    public @NotNull Direction getVentingDirection() {
        return Direction.UP;
    }

    @Override
    public boolean needsVenting() {
        return needsVenting;
    }

    @Override
    public void markVentingComplete() {

    }

    @Override
    public float getVentingDamage() {
        return 0;
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
    public boolean isWorkingEnabled() {
        return getRecipeLogic().isWorkingEnabled();
    }

    @Override
    public void setWorkingEnabled(boolean isActivationAllowed) {
        getRecipeLogic().setWorkingEnabled(isActivationAllowed);
    }

//    @Nonnull
//    @Override
//    public List<Component> getDataInfo() {
//        int workingArea = getWorkingArea(this.getRecipeLogic().getCurrentRadius());
//        return Collections.singletonList(Component.translatable("gtceu.universal.tooltip.working_area", workingArea, workingArea));
//    }
}

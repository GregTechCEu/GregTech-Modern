package com.gregtechceu.gtceu.common.cover.detector;

import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.cover.IMuiCover;
import com.gregtechceu.gtceu.api.cover.filter.FilterHandler;
import com.gregtechceu.gtceu.api.cover.filter.FilterHandlers;
import com.gregtechceu.gtceu.api.cover.filter.FluidFilter;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.common.mui.GTMuiWidgets;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import brachy.modularui.api.drawable.IKey;
import brachy.modularui.factory.SidedPosGuiData;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.BooleanSyncValue;
import brachy.modularui.value.sync.IntSyncValue;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widgets.ToggleButton;
import brachy.modularui.widgets.layout.Flow;
import lombok.Getter;

import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

import static com.gregtechceu.gtceu.utils.RedstoneUtil.computeLatchedRedstoneBetweenValues;
import static com.gregtechceu.gtceu.utils.RedstoneUtil.computeRedstoneBetweenValues;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class AdvancedFluidDetectorCover extends FluidDetectorCover implements IMuiCover {

    private static final int DEFAULT_MIN = 64;
    private static final int DEFAULT_MAX = 512;
    @SaveField
    @Getter
    private int minValue, maxValue;

    @SaveField
    @SyncToClient
    @Getter
    private boolean isLatched;
    @SaveField
    @SyncToClient
    @Getter
    protected final FilterHandler<FluidStack, FluidFilter> filterHandler;

    public AdvancedFluidDetectorCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide) {
        super(definition, coverHolder, attachedSide);

        this.minValue = DEFAULT_MIN;
        this.maxValue = DEFAULT_MAX;

        filterHandler = FilterHandlers.fluid(this);
    }

    public void setLatched(boolean latched) {
        isLatched = latched;
        syncDataHolder.markClientSyncFieldDirty("isLatched");
    }

    @Override
    public List<ItemStack> getAdditionalDrops() {
        var list = super.getAdditionalDrops();
        if (!filterHandler.getFilterItem().isEmpty()) {
            list.add(filterHandler.getFilterItem());
        }
        return list;
    }

    @Override
    protected void update() {
        if (this.coverHolder.getOffsetTimer() % 20 != 0)
            return;

        FluidFilter filter = filterHandler.getFilter();
        IFluidHandler fluidHandler = getFluidHandler();
        if (fluidHandler == null)
            return;

        long storedFluid = 0;

        for (int tank = 0; tank < fluidHandler.getTanks(); tank++) {
            FluidStack content = fluidHandler.getFluidInTank(tank);

            if (!content.isEmpty() && filter.test(content))
                storedFluid += content.getAmount();
        }

        if (isLatched) {
            setRedstoneSignalOutput(computeLatchedRedstoneBetweenValues(storedFluid, maxValue, minValue,
                    isInverted(), redstoneSignalOutput));
        } else {
            setRedstoneSignalOutput(computeRedstoneBetweenValues(storedFluid, maxValue, minValue, isInverted()));
        }
    }

    public void setMinValue(int minValue) {
        this.minValue = Mth.clamp(minValue, 0, maxValue - 1);
        if (this.minValue < 0) this.minValue = 0;
    }

    public void setMaxValue(int maxValue) {
        this.maxValue = Math.max(maxValue, 0);
        setMinValue(this.getMinValue());
    }

    //////////////////////////////////////
    // *********** GUI ***********//
    //////////////////////////////////////
    ///
    @Override
    public void createCoverUIRows(Flow column, SidedPosGuiData data, PanelSyncManager syncManager,
                                  UISettings settings) {
        var minValueSync = new IntSyncValue(this::getMinValue, this::setMinValue);
        var maxValueSync = new IntSyncValue(this::getMaxValue, this::setMaxValue);

        syncManager.syncValue("minValue", minValueSync);
        syncManager.syncValue("maxValue", maxValueSync);

        var buttonRow = coverUIRow()
                .child(new ToggleButton().value(new BooleanSyncValue(this::isInverted, this::setInverted))
                        .overlay(false, GTGuiTextures.OVERLAY_REDSTONE_OFF)
                        .overlay(true, GTGuiTextures.OVERLAY_REDSTONE_ON)
                        .tooltip(false, t -> t.add("cover.advanced_fluid_detector.invert.disabled"))
                        .tooltip(true, t -> t.add("cover.advanced_fluid_detector.invert.enabled")))
                .child(new ToggleButton().value(new BooleanSyncValue(this::isLatched, this::setLatched))
                        .overlay(false, GTGuiTextures.BUTTON_LOCK)
                        .overlay(true, GTGuiTextures.BUTTON_LOCK)
                        .tooltip(false, t -> t.add("cover.advanced_detector.latch.disabled"))
                        .tooltip(true, t -> t.add("cover.advanced_detector.latch.enabled")));

        GTMuiWidgets.createFilterRow(buttonRow, filterHandler, data, syncManager, settings);

        column.child(coverUIRow().child(IKey.lang("cover.advanced_fluid_detector.min").asWidget().width(50))
                .child(GTMuiWidgets.createIntInputWithButtons(minValueSync, () -> 0, this::getMaxValue).width(110)))
                .child(coverUIRow().child(IKey.lang("cover.advanced_fluid_detector.max").asWidget().width(50))
                        .child(GTMuiWidgets.createIntInputWithButtons(maxValueSync, () -> 0, () -> Integer.MAX_VALUE)
                                .width(110)))
                .child(buttonRow);
    }

    @Override
    public CompoundTag copyConfig(CompoundTag tag) {
        tag.putInt("min", minValue);
        tag.putInt("max", maxValue);
        tag.putBoolean("latched", isLatched);
        tag.put("filter", filterHandler.getFilterItem().serializeNBT());
        return super.copyConfig(tag);
    }

    @Override
    public void pasteConfig(ServerPlayer player, CompoundTag tag) {
        setMinValue(tag.getInt("min"));
        setMaxValue(tag.getInt("max"));
        setLatched(tag.getBoolean("latched"));
        filterHandler.setFilterItem(ItemStack.of(tag.getCompound("filter")));
        super.pasteConfig(player, tag);
    }
}

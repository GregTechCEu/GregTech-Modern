package com.gregtechceu.gtceu.common.cover.detector;

import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.cover.IUICover;
import com.gregtechceu.gtceu.api.cover.filter.FilterHandler;
import com.gregtechceu.gtceu.api.cover.filter.FilterHandlers;
import com.gregtechceu.gtceu.api.cover.filter.FluidFilter;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;

import com.lowdragmc.lowdraglib.gui.widget.Widget;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import lombok.Getter;

import java.util.List;

import static com.gregtechceu.gtceu.utils.RedstoneUtil.computeLatchedRedstoneBetweenValues;
import static com.gregtechceu.gtceu.utils.RedstoneUtil.computeRedstoneBetweenValues;

public class AdvancedFluidDetectorCover extends FluidDetectorCover implements IUICover {

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
    }

    public void setMaxValue(int maxValue) {
        this.maxValue = Math.max(maxValue, 0);
    }

    //////////////////////////////////////
    // *********** GUI ***********//
    //////////////////////////////////////

    @Override
    public Widget createUIWidget() {
        return AdvancedFluidDetectorCoverUI.createUIWidget(this);
    }

    @Override
    public CompoundTag copyConfig(CompoundTag tag) {
        tag.putInt("min", minValue);
        tag.putInt("max", maxValue);
        tag.putBoolean("latched", isLatched);
        tag.put("filter", ItemStack.OPTIONAL_CODEC
                .encodeStart(coverHolder.getLevel().registryAccess().createSerializationContext(NbtOps.INSTANCE),
                        filterHandler.getFilterItem())
                .getOrThrow());
        return super.copyConfig(tag);
    }

    @Override
    public void pasteConfig(ServerPlayer player, CompoundTag tag) {
        setMinValue(tag.getIntOr("min", 0));
        setMaxValue(tag.getIntOr("max", 0));
        setLatched(tag.getBooleanOr("latched", false));
        filterHandler.setFilterItem(ItemStack.OPTIONAL_CODEC
                .parse(coverHolder.getLevel().registryAccess().createSerializationContext(NbtOps.INSTANCE),
                        tag.getCompoundOrEmpty("filter"))
                .result()
                .orElse(ItemStack.EMPTY));
        super.pasteConfig(player, tag);
    }
}

package com.gregtechceu.gtceu.common.cover.detector;

import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.cover.IUICover;
import com.gregtechceu.gtceu.api.cover.filter.FilterHandler;
import com.gregtechceu.gtceu.api.cover.filter.FilterHandlers;
import com.gregtechceu.gtceu.api.cover.filter.ItemFilter;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.utils.RedstoneUtil;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

import lombok.Getter;

import java.util.List;

public class AdvancedItemDetectorCover extends ItemDetectorCover implements IUICover {

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
    protected final FilterHandler<ItemStack, ItemFilter> filterHandler;

    public AdvancedItemDetectorCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide) {
        super(definition, coverHolder, attachedSide);

        this.minValue = DEFAULT_MIN;
        this.maxValue = DEFAULT_MAX;

        filterHandler = FilterHandlers.item(this);
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

        ItemFilter filter = filterHandler.getFilter();
        IItemHandler handler = getItemHandler();
        if (handler == null)
            return;

        int storedItems = 0;

        for (int i = 0; i < handler.getSlots(); i++) {
            if (filter.test(handler.getStackInSlot(i)))
                storedItems += handler.getStackInSlot(i).getCount();
        }

        if (isLatched) {
            setRedstoneSignalOutput(RedstoneUtil.computeLatchedRedstoneBetweenValues(storedItems, maxValue, minValue,
                    isInverted(), redstoneSignalOutput));
        } else {
            setRedstoneSignalOutput(
                    RedstoneUtil.computeRedstoneBetweenValues(storedItems, maxValue, minValue, isInverted()));
        }
    }

    public void setMinValue(int minValue) {
        this.minValue = Mth.clamp(minValue, 0, maxValue - 1);
    }

    public void setMaxValue(int maxValue) {
        this.maxValue = Math.max(maxValue, 0);
    }

    public void setLatched(boolean latched) {
        isLatched = latched;
        syncDataHolder.markClientSyncFieldDirty("isLatched");
    }

    //////////////////////////////////////
    // *********** GUI ***********//
    //////////////////////////////////////

    @Override
    public Object createUIWidget() {
        return AdvancedItemDetectorCoverUI.createUIWidget(this);
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

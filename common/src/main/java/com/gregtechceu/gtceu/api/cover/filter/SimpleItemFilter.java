package com.gregtechceu.gtceu.api.cover.filter;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.ToggleButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.PhantomSlotWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.jei.IngredientIO;
import com.lowdragmc.lowdraglib.misc.ItemStackTransfer;
import com.lowdragmc.lowdraglib.side.item.ItemTransferHelper;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.Observable;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;
import java.util.function.Consumer;

/**
 * @author KilaBash
 * @date 2023/3/13
 * @implNote ItemFilterHandler
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SimpleItemFilter implements ItemFilter {
    @Getter
    protected boolean isBlackList;
    @Getter
    protected boolean ignoreNbt;
    @Getter
    protected ItemStack[] matches = new ItemStack[9];
    @Setter
    protected Consumer<ItemFilter> onUpdated;

    @Getter
    protected int maxStackSize;

    protected PhantomSlotWidget[] filterSlots = new PhantomSlotWidget[9];

    protected SimpleItemFilter() {
        Arrays.fill(matches, ItemStack.EMPTY);
        maxStackSize = 1;
    }

    public static SimpleItemFilter loadFilter(ItemStack itemStack) {
        return loadFilter(itemStack.getOrCreateTag(), filter -> itemStack.setTag(filter.saveFilter()));
    }

    public static SimpleItemFilter loadFilter(CompoundTag tag, Consumer<ItemFilter> onUpdated) {
        var handler = new SimpleItemFilter();
        handler.setOnUpdated(onUpdated);
        handler.isBlackList = tag.getBoolean("isBlackList");
        handler.ignoreNbt = tag.getBoolean("matchNbt");
        var list = tag.getList("matches", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            handler.matches[i] = ItemStack.of((CompoundTag) list.get(i));
        }
        return handler;
    }

    public CompoundTag saveFilter() {
        var tag = new CompoundTag();
        tag.putBoolean("isBlackList", isBlackList);
        tag.putBoolean("matchNbt", ignoreNbt);
        var list = new ListTag();
        for (var match : matches) {
            list.add(match.save(new CompoundTag()));
        }
        tag.put("matches", list);
        return tag;
    }


    public void setBlackList(boolean blackList) {
        isBlackList = blackList;
        onUpdated.accept(this);
    }

    public void setIgnoreNbt(boolean ingoreNbt) {
        this.ignoreNbt = ingoreNbt;
        onUpdated.accept(this);
    }

    public WidgetGroup openConfigurator(int x, int y) {
        WidgetGroup group = new WidgetGroup(x, y, 18 * 3 + 25, 18 * 3); // 80 55
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                final int index = i * 3 + j;
                var handler = new ItemStackTransfer(matches[index]);
                var slot = new PhantomSlotWidget(handler, 0, i * 18, j * 18);
                slot.setMaxStackSize(this.maxStackSize);
                slot.setChangeListener(() -> {
                    matches[index] = handler.getStackInSlot(0);
                    onUpdated.accept(this);
                }).setBackground(GuiTextures.SLOT);
                this.filterSlots[index] = slot; // Used to update max stack size on change
                group.addWidget(slot);
            }
        }
        group.addWidget(new ToggleButtonWidget(18 * 3 + 2, (18 * 0) + 6, 18, 18,
                GuiTextures.BUTTON_BLACKLIST, this::isBlackList, this::setBlackList));
        group.addWidget(new ToggleButtonWidget(18 * 3 + 2, (18 * 1) + 6, 18, 18,
                GuiTextures.BUTTON_FILTER_NBT, this::isIgnoreNbt, this::setIgnoreNbt));
        return group;
    }

    @Override
    public boolean test(ItemStack itemStack) {
        boolean found = false;
        for (var match : matches) {
            if (ignoreNbt) {
                found = match.sameItem(itemStack);
            } else {
                found = ItemTransferHelper.canItemStacksStack(match, itemStack);
            }
            if (found) {
                break;
            }
        }
        return isBlackList != found;
    }

    public void setMaxStackSize(int maxStackSize) {
        this.maxStackSize = maxStackSize;

        for (PhantomSlotWidget filterSlot : filterSlots) {
            if (null == filterSlot)
                continue;

            filterSlot.setMaxStackSize(maxStackSize);
        }

        for (ItemStack match : matches) {
            match.setCount(Math.min(match.getCount(), maxStackSize));
        }
    }
}

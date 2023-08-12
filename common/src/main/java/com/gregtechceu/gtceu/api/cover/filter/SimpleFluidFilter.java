package com.gregtechceu.gtceu.api.cover.filter;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.ScrollablePhantomFluidWidget;
import com.gregtechceu.gtceu.api.gui.widget.ToggleButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.misc.FluidStorage;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import lombok.Getter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.function.Consumer;

/**
 * @author KilaBash
 * @date 2023/3/13
 * @implNote ItemFilterHandler
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SimpleFluidFilter implements FluidFilter {
    @Getter
    protected boolean isBlackList;
    @Getter
    protected boolean ignoreNbt;
    @Getter
    protected FluidStack[] matches = new FluidStack[9];

    protected Consumer<FluidFilter> itemWriter = filter -> {};
    protected Consumer<FluidFilter> onUpdated = filter -> itemWriter.accept(filter);

    @Getter
    protected long maxStackSize = 1L;

    private FluidStorage[] fluidStorageSlots = new FluidStorage[9];

    protected SimpleFluidFilter() {
        Arrays.fill(matches, FluidStack.empty());
    }

    public static SimpleFluidFilter loadFilter(ItemStack itemStack) {
        return loadFilter(itemStack.getOrCreateTag(), filter -> itemStack.setTag(filter.saveFilter()));
    }

    private static SimpleFluidFilter loadFilter(CompoundTag tag, Consumer<FluidFilter> itemWriter) {
        var handler = new SimpleFluidFilter();
        handler.itemWriter = itemWriter;
        handler.isBlackList = tag.getBoolean("isBlackList");
        handler.ignoreNbt = tag.getBoolean("matchNbt");
        var list = tag.getList("matches", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            handler.matches[i] = FluidStack.loadFromTag((CompoundTag) list.get(i));
        }
        return handler;
    }

    @Override
    public void setOnUpdated(Consumer<FluidFilter> onUpdated) {
        this.onUpdated = filter -> {
            this.itemWriter.accept(filter);
            onUpdated.accept(filter);
        };
    }

    public CompoundTag saveFilter() {
        var tag = new CompoundTag();
        tag.putBoolean("isBlackList", isBlackList);
        tag.putBoolean("matchNbt", ignoreNbt);
        var list = new ListTag();
        for (var match : matches) {
            list.add(match.saveToTag(new CompoundTag()));
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
        fluidStorageSlots = new FluidStorage[9];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                final int index = i * 3 + j;

                fluidStorageSlots[index] = new FluidStorage(maxStackSize);
                fluidStorageSlots[index].setFluid(matches[index]);

                var tank = new ScrollablePhantomFluidWidget(fluidStorageSlots[index], i * 18, j * 18) {
                    @Override
                    public void updateScreen() {
                        super.updateScreen();
                        setShowAmount(maxStackSize > 1L);
                    }

                    @Override
                    public void detectAndSendChanges() {
                        super.detectAndSendChanges();
                        setShowAmount(maxStackSize > 1L);
                    }
                };

                tank.setChangeListener(() -> {
                    matches[index] = fluidStorageSlots[index].getFluidInTank(0);
                    onUpdated.accept(this);
                }).setBackground(GuiTextures.SLOT);

                group.addWidget(tank);
            }
        }
        group.addWidget(new ToggleButtonWidget(18 * 3 + 5, 0, 20, 20,
                GuiTextures.BUTTON_BLACKLIST, this::isBlackList, this::setBlackList));
        group.addWidget(new ToggleButtonWidget(18 * 3 + 5, 20, 20, 20,
                GuiTextures.BUTTON_FILTER_NBT, this::isIgnoreNbt, this::setIgnoreNbt));
        return group;
    }

    @Override
    public boolean test(FluidStack other) {
        return testFluidAmount(other) > 0L;
    }

    @Override
    public long testFluidAmount(FluidStack fluidStack) {
        long totalFluidAmount = getTotalConfiguredFluidAmount(fluidStack);

        if (isBlackList) {
            return (totalFluidAmount > 0L) ? 0L : Long.MAX_VALUE;
        }

        return totalFluidAmount;
    }

    public long getTotalConfiguredFluidAmount(FluidStack fluidStack) {
        long totalAmount = 0L;

        for (var candidate : matches) {
            if (ignoreNbt && candidate.getFluid() == fluidStack.getFluid()) {
                totalAmount += candidate.getAmount();
            } else if (candidate.isFluidEqual(fluidStack)) {
                totalAmount += candidate.getAmount();
            }
        }

        return totalAmount;
    }

    public void setMaxStackSize(long maxStackSize) {
        this.maxStackSize = maxStackSize;

        for (FluidStorage slot : fluidStorageSlots) {
            if (slot != null)
                slot.setCapacity(maxStackSize);
        }

        for (FluidStack match : matches) {
            if (!match.isEmpty())
                match.setAmount(Math.min(match.getAmount(), maxStackSize));
        }
    }
}

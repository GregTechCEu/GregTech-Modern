package com.lowdragmc.gtceu.api.cover.filter;

import com.lowdragmc.gtceu.api.gui.GuiTextures;
import com.lowdragmc.gtceu.api.gui.widget.ToggleButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.PhantomFluidWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.msic.FluidStorage;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import lombok.Getter;
import lombok.Setter;
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
    @Setter
    protected Consumer<FluidFilter> onUpdated;

    protected SimpleFluidFilter() {
        Arrays.fill(matches, FluidStack.empty());
    }

    public static SimpleFluidFilter loadFilter(ItemStack itemStack) {
        return loadFilter(itemStack.getOrCreateTag(), filter -> itemStack.setTag(filter.saveFilter()));
    }

    public static SimpleFluidFilter loadFilter(CompoundTag tag, Consumer<FluidFilter> onUpdated) {
        var handler = new SimpleFluidFilter();
        handler.setOnUpdated(onUpdated);
        handler.isBlackList = tag.getBoolean("isBlackList");
        handler.ignoreNbt = tag.getBoolean("matchNbt");
        var list = tag.getList("matches", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            handler.matches[i] = FluidStack.loadFromTag((CompoundTag) list.get(i));
        }
        return handler;
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
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                final int index = i * 3 + j;
                var handler = new FluidStorage(1000);
                handler.setFluid(matches[index]);
                var tank = new PhantomFluidWidget(handler, i * 18, j * 18);
                tank.setShowAmount(false);
                tank.setChangeListener(() -> {
                    matches[index] = handler.getFluidInTank(0);
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
        boolean found = false;
        for (var match : matches) {
            if (ignoreNbt) {
                found = match.getFluid() == other.getFluid();
            } else {
                found = match.isFluidEqual(other);
            }
            if (found) {
                break;
            }
        }
        return isBlackList != found;
    }
}

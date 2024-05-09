package com.gregtechceu.gtceu.api.covers.filter;

import com.gregtechceu.gtceu.api.guis.GuiTextures;
import com.gregtechceu.gtceu.api.guis.widget.ScrollablePhantomFluidWidget;
import com.gregtechceu.gtceu.api.guis.widget.ToggleButtonWidget;
import com.gregtechceu.gtceu.api.transfer.fluid.CustomFluidTank;
import com.gregtechceu.gtceu.data.GTDataComponents;
import com.lowdragmc.lowdraglib.Platform;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author KilaBash
 * @date 2023/3/13
 * @implNote ItemFilterHandler
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SimpleFluidFilter implements FluidFilter {
    public static final Codec<SimpleFluidFilter> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.BOOL.fieldOf("is_blacklist").forGetter(val -> val.isBlackList),
        Codec.BOOL.fieldOf("ignore_components").forGetter(val -> val.ignoreNbt),
        FluidStack.OPTIONAL_CODEC.listOf().fieldOf("matches").forGetter(val -> Arrays.stream(val.matches).toList())
    ).apply(instance, SimpleFluidFilter::new));
    @Getter
    protected boolean isBlackList;
    @Getter
    protected boolean ignoreNbt;
    @Getter
    protected FluidStack[] matches = new FluidStack[9];

    protected Consumer<FluidFilter> itemWriter = filter -> {};
    protected Consumer<FluidFilter> onUpdated = filter -> itemWriter.accept(filter);

    @Getter
    protected int maxStackSize = 1;

    private CustomFluidTank[] fluidStorageSlots = new CustomFluidTank[9];

    protected SimpleFluidFilter() {
        Arrays.fill(matches, FluidStack.EMPTY);
    }

    protected SimpleFluidFilter(boolean isBlackList, boolean ignoreNbt, List<FluidStack> matches) {
        this.isBlackList = isBlackList;
        this.ignoreNbt = ignoreNbt;
        this.matches = matches.toArray(FluidStack[]::new);
    }

    public static SimpleFluidFilter loadFilter(ItemStack itemStack) {
        //handler.itemWriter = itemWriter; TODO fix
        return itemStack.get(GTDataComponents.SIMPLE_FLUID_FILTER);
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
            list.add(FluidStack.OPTIONAL_CODEC.encodeStart(Platform.getFrozenRegistry().createSerializationContext(NbtOps.INSTANCE), match).getOrThrow());
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
        fluidStorageSlots = new CustomFluidTank[9];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                final int index = i * 3 + j;

                fluidStorageSlots[index] = new CustomFluidTank(maxStackSize);
                fluidStorageSlots[index].setFluid(matches[index]);

                var tank = new ScrollablePhantomFluidWidget(fluidStorageSlots[index], 0, i * 18, j * 18, 18, 18, () -> fluidStorageSlots[index].getFluid(), (fluid) -> fluidStorageSlots[index].setFluid(fluid)) {
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
    public int testFluidAmount(FluidStack fluidStack) {
        int totalFluidAmount = getTotalConfiguredFluidAmount(fluidStack);

        if (isBlackList) {
            return (totalFluidAmount > 0) ? 0 : Integer.MAX_VALUE;
        }

        return totalFluidAmount;
    }

    public int getTotalConfiguredFluidAmount(FluidStack fluidStack) {
        int totalAmount = 0;

        for (var candidate : matches) {
            if (ignoreNbt && candidate.getFluid() == fluidStack.getFluid()) {
                totalAmount += candidate.getAmount();
            } else if (FluidStack.isSameFluidSameComponents(candidate, fluidStack)) {
                totalAmount += candidate.getAmount();
            }
        }

        return totalAmount;
    }

    public void setMaxStackSize(int maxStackSize) {
        this.maxStackSize = maxStackSize;

        for (CustomFluidTank slot : fluidStorageSlots) {
            if (slot != null)
                slot.setCapacity(maxStackSize);
        }

        for (FluidStack match : matches) {
            if (!match.isEmpty())
                match.setAmount(Math.min(match.getAmount(), maxStackSize));
        }
    }
}

package com.gregtechceu.gtceu.api.gui.widget;

import com.gregtechceu.gtceu.utils.GTUtil;

import com.lowdragmc.lowdraglib.gui.widget.PhantomFluidWidget;
import com.lowdragmc.lowdraglib.side.fluid.FluidHelper;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import com.lowdragmc.lowdraglib.side.fluid.IFluidTransfer;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class ScrollablePhantomFluidWidget extends PhantomFluidWidget {

    private static final int SCROLL_ACTION_ID = 0x0001_0001;
    private static final long MILLIBUCKETS = FluidHelper.getBucket() / 1000;

    public ScrollablePhantomFluidWidget(@Nullable IFluidTransfer fluidTank, int tank, int x, int y, int width,
                                        int height, Supplier<FluidStack> phantomFluidGetter,
                                        Consumer<FluidStack> phantomFluidSetter) {
        super(fluidTank, tank, x, y, width, height, phantomFluidGetter, phantomFluidSetter);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean mouseWheelMove(double mouseX, double mouseY, double wheelDelta) {
        if (!isMouseOverElement(mouseX, mouseY))
            return false;

        var delta = getModifiedChangeAmount((wheelDelta > 0) ? 1 : -1) * MILLIBUCKETS;
        writeClientAction(SCROLL_ACTION_ID, buf -> buf.writeLong(delta));

        return true;
    }

    private long getModifiedChangeAmount(int amount) {
        if (GTUtil.isShiftDown())
            amount *= 10;

        if (GTUtil.isCtrlDown())
            amount *= 100;

        if (!GTUtil.isAltDown())
            amount *= 1000;

        return amount;
    }

    @Override
    public void handleClientAction(int id, FriendlyByteBuf buffer) {
        switch (id) {
            case SCROLL_ACTION_ID -> handleScrollAction(buffer.readLong());
            default -> super.handleClientAction(id, buffer);
        }

        detectAndSendChanges();
    }

    private void handleScrollAction(long delta) {
        IFluidTransfer fluidTank = getFluidTank();
        if (fluidTank == null)
            return;

        FluidStack fluid = fluidTank.getFluidInTank(tank);
        if (fluid.isEmpty())
            return;

        if (fluid.isEmpty())
            return;

        fluid.setAmount(Math.min(Math.max(fluid.getAmount() + delta, 0L), fluidTank.getTankCapacity(tank)));
        if (fluid.getAmount() <= 0L) {
            fluidTank.setFluidInTank(tank, FluidStack.empty());
        }
    }
}

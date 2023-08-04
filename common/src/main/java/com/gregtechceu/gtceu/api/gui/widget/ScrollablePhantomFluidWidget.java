package com.gregtechceu.gtceu.api.gui.widget;

import com.gregtechceu.gtceu.utils.GTUtil;
import com.lowdragmc.lowdraglib.gui.widget.PhantomFluidWidget;
import com.lowdragmc.lowdraglib.side.fluid.FluidHelper;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import com.lowdragmc.lowdraglib.side.fluid.IFluidStorage;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

public class ScrollablePhantomFluidWidget extends PhantomFluidWidget {
    private static final int SCROLL_ACTION_ID = 0x0001_0001;
    private static final long MILLIBUCKETS = FluidHelper.getBucket() / 1000;


    public ScrollablePhantomFluidWidget() {
    }

    public ScrollablePhantomFluidWidget(IFluidStorage fluidTank, int x, int y) {
        super(fluidTank, x, y);
    }

    public ScrollablePhantomFluidWidget(@Nullable IFluidStorage fluidTank, int x, int y, int width, int height) {
        super(fluidTank, x, y, width, height);
    }

    @Override
    @Environment(EnvType.CLIENT)
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
        IFluidStorage tank = getFluidTank();
        if (tank == null)
            return;

        FluidStack fluid = tank.getFluid();
        if (fluid.isEmpty())
            return;

        if (fluid.isEmpty())
            return;

        fluid.setAmount(Mth.clamp(fluid.getAmount() + delta, 0L, tank.getCapacity()));

        if (fluid.getAmount() <= 0L) {
            tank.setFluid(FluidStack.empty());
        }
    }
}

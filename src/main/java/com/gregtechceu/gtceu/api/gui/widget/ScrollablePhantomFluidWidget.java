package com.gregtechceu.gtceu.api.gui.widget;

import com.gregtechceu.gtceu.utils.GTUtil;
import com.lowdragmc.lowdraglib.gui.widget.PhantomFluidWidget;
import com.lowdragmc.lowdraglib.side.fluid.FluidHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.Nullable;

public class ScrollablePhantomFluidWidget extends PhantomFluidWidget {
    private static final int SCROLL_ACTION_ID = 0x0001_0001;
    private static final int MILLIBUCKETS = FluidHelper.getBucket() / 1000;


    public ScrollablePhantomFluidWidget() {
    }

    public ScrollablePhantomFluidWidget(IFluidHandler fluidTank, int x, int y) {
        super(fluidTank, x, y);
    }

    public ScrollablePhantomFluidWidget(@Nullable IFluidHandler fluidTank, int x, int y, int width, int height) {
        super(fluidTank, x, y, width, height);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean mouseWheelMove(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (!isMouseOverElement(mouseX, mouseY))
            return false;

        int delta = getModifiedChangeAmount((scrollY > 0) ? 1 : -1) * MILLIBUCKETS;
        writeClientAction(SCROLL_ACTION_ID, buf -> buf.writeVarInt(delta));

        return true;
    }

    private int getModifiedChangeAmount(int amount) {
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
            case SCROLL_ACTION_ID -> handleScrollAction(buffer.readVarInt());
            default -> super.handleClientAction(id, buffer);
        }

        detectAndSendChanges();
    }

    private void handleScrollAction(int delta) {
        IFluidHandlerModifiable fluidTank = getFluidTank();
        if (fluidTank == null)
            return;

        FluidStack fluid = fluidTank.getFluidInTank(tank);
        if (fluid.isEmpty())
            return;

        fluid.setAmount(Math.min(Math.max(fluid.getAmount() + delta, 0), fluidTank.getTankCapacity(tank)));
        if (fluid.getAmount() <= 0L) {
            fluidTank.setFluidInTank(tank, FluidStack.EMPTY);
        }
    }
}

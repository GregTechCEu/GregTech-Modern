package com.gregtechceu.gtceu.common.cover.ender;

import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.cover.filter.FilterHandler;
import com.gregtechceu.gtceu.api.cover.filter.FilterHandlers;
import com.gregtechceu.gtceu.api.cover.filter.FluidFilter;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.TankWidget;
import com.gregtechceu.gtceu.api.misc.virtualregistry.EntryTypes;
import com.gregtechceu.gtceu.api.misc.virtualregistry.VirtualEnderRegistry;
import com.gregtechceu.gtceu.api.misc.virtualregistry.entries.VirtualTank;
import com.gregtechceu.gtceu.api.misc.virtualregistry.entries.VisualTank;
import com.gregtechceu.gtceu.api.transfer.fluid.IFluidHandlerModifiable;
import com.gregtechceu.gtceu.utils.GTTransferUtils;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.LazyManaged;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class EnderFluidLinkCover extends CoverAbstractEnderLink<VirtualTank> {
    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(EnderFluidLinkCover.class,
            CoverAbstractEnderLink.MANAGED_FIELD_HOLDER);
    public static final int TRANSFER_RATE = 8000; // mB/t

    @Persisted
    @DescSynced
    @LazyManaged
    protected final VisualTank visualTank = new VisualTank();
    @Persisted
    @DescSynced
    protected final FilterHandler<FluidStack, FluidFilter> filterHandler;
    protected int mBLeftToTransferLastSecond;

    public EnderFluidLinkCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide) {
        super(definition, coverHolder, attachedSide);
        this.mBLeftToTransferLastSecond = TRANSFER_RATE * 20;
        filterHandler = FilterHandlers.fluid(this);
    }

    @Override
    public boolean canAttach() {
        return FluidUtil.getFluidHandler(coverHolder.getLevel(), coverHolder.getPos(), attachedSide).isPresent();
    }

    @Override
    protected EntryTypes<VirtualTank> getEntryType() {
        return EntryTypes.ENDER_FLUID;
    }

    @Override
    protected void updateEntry() {
        var reg = VirtualEnderRegistry.getInstance();
        if (reg == null) return;
        var tank = reg.getOrCreateEntry(getOwner(), EntryTypes.ENDER_FLUID, this.channelName);
        this.visualTank.setVirtualTank(tank);
        this.visualTank.getVirtualTank().setColor(this.channelName);
        markAsDirty();
        markDirty("visualTank");
        this.visualTank.setFluid(this.visualTank.getVirtualTank().getFluid());
    }

    @Override
    protected void transfer() {
        long timer = coverHolder.getOffsetTimer();
        if (mBLeftToTransferLastSecond > 0) {
            int platformTransferredFluid = doTransferFluids(mBLeftToTransferLastSecond);
            this.mBLeftToTransferLastSecond -= platformTransferredFluid;
        }

        if (timer % 20 == 0) {
            this.mBLeftToTransferLastSecond = TRANSFER_RATE * 20;
        }
    }

    protected @Nullable IFluidHandlerModifiable getOwnFluidHandler() {
        return coverHolder.getFluidHandlerCap(attachedSide, false);
    }

    private int doTransferFluids(int platformTransferLimit) {
        var ownFluidHandler = getOwnFluidHandler();

        if (ownFluidHandler != null) {
            return switch (io) {
                case IN -> GTTransferUtils.transferFluidsFiltered(ownFluidHandler, visualTank.getVirtualTank(),
                        filterHandler.getFilter(), platformTransferLimit);
                case OUT -> GTTransferUtils.transferFluidsFiltered(visualTank.getVirtualTank(), ownFluidHandler,
                        filterHandler.getFilter(), platformTransferLimit);
                default -> 0;
            };

        }
        return 0;
    }

    @Override
    public void onAttached(ItemStack itemStack, ServerPlayer player) {
        super.onAttached(itemStack, player);
        playerUUID = player.getUUID();
    }

    @Override
    public @NotNull ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    //////////////////////////////////////
    //   *********** GUI ************   //

    /// ///////////////////////////////////

    @Override
    protected void buildAdditionalUI(WidgetGroup group) {
        group.addWidget(new TankWidget(visualTank, 0, 146, 20, 20, 20,
                true, true).setBackground(GuiTextures.FLUID_SLOT));

        group.addWidget(filterHandler.createFilterSlotUI(117, 108));
        group.addWidget(filterHandler.createFilterConfigUI(10, 72, 156, 60));
    }

    @NotNull
    @Override
    protected String getUITitle() {
        return "cover.ender_fluid_link.title";
    }

//    damn I can't make the list panel work in the server side
//    private SelectableWidgetGroup createVisualTankWidget(VisualTank tank, int y) {
//        final int TOTAL_WIDTH = 116;
//        final int BUTTON_SIZE = 20;
//        final int MARGIN = 2;
//
//        int currentX = 0;
//        int availableWidth = TOTAL_WIDTH - BUTTON_SIZE + MARGIN;
//
//        SelectableWidgetGroup channelGroup = new SelectableWidgetGroup(0, y, TOTAL_WIDTH, BUTTON_SIZE){
//            @Override
//            public void drawInForeground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
//                super.drawInForeground(graphics, mouseX, mouseY, partialTicks);
//                if (super.isSelected)
//                    DrawerHelper.drawBorder(graphics, getPositionX(), getPositionY(), TOTAL_WIDTH, BUTTON_SIZE, 0xFFFFFFFF, 1);
//            }
//        };
//        var name = tank.getVirtualTank().getColorStr();
//
//        // Color block
//        channelGroup.addWidget(new ColorBlockWidget(currentX, 0, BUTTON_SIZE, BUTTON_SIZE)
//                .setCurrentColor(tank.getVirtualTank().getColor()));
//        currentX += BUTTON_SIZE + MARGIN;
//
//        // Text box
//        int textBoxWidth = availableWidth;
//        textBoxWidth -= BUTTON_SIZE + MARGIN;
//        if (tank.getFluidAmount() == 0) {
//            textBoxWidth -= BUTTON_SIZE + MARGIN;
//        }
//        channelGroup.addWidget(
//                new TextBoxWidget(currentX, 6, textBoxWidth, List.of(name)).setCenter(true));
//        currentX += textBoxWidth + MARGIN;
//
//        // Tank slot
//        channelGroup.addWidget(new TankWidget(tank, currentX, 0,
//                BUTTON_SIZE, BUTTON_SIZE, false, false)
//                .setBackground(GuiTextures.FLUID_SLOT));
//        currentX += BUTTON_SIZE + MARGIN;
//
//        // Remove button (if tank is empty)
//        if (tank.getFluidAmount() == 0) {
//            channelGroup.addWidget(new ButtonWidget(currentX, 0, BUTTON_SIZE, BUTTON_SIZE,
//                    GuiTextures.BUTTON_INT_CIRCUIT_MINUS, cd -> removeChannel(tank)));
//        }
//        return channelGroup;
//    }
}

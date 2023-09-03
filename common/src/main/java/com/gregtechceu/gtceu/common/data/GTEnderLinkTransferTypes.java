package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.misc.ProxiedTransferWrapper;
import com.gregtechceu.gtceu.api.pipenet.enderlink.ITransferType;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.lowdragmc.lowdraglib.side.fluid.FluidTransferHelper;
import com.lowdragmc.lowdraglib.side.fluid.IFluidTransfer;
import com.lowdragmc.lowdraglib.side.item.IItemTransfer;
import com.lowdragmc.lowdraglib.side.item.ItemTransferHelper;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;


@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class GTEnderLinkTransferTypes {
    public static final ITransferType<IItemTransfer> ITEM = register(GTCEu.id("item"), new ITransferType<>() {
        @Override
        public Class<IItemTransfer> getTransferClass() {
            return IItemTransfer.class;
        }

        @Override
        public long transferAll(IItemTransfer input, IItemTransfer output) {
            long transferred = 0;

            for (int srcIndex = 0; srcIndex < input.getSlots(); srcIndex++) {
                ItemStack sourceStack = input.extractItem(srcIndex, Integer.MAX_VALUE, true);
                if (sourceStack.isEmpty())
                    continue;

                ItemStack remainder = ItemTransferHelper.insertItem(output, sourceStack, true);
                int amountToInsert = sourceStack.getCount() - remainder.getCount();
                if (amountToInsert <= 0)
                    continue;

                sourceStack = input.extractItem(srcIndex, amountToInsert, false);
                if (sourceStack.isEmpty())
                    continue;

                ItemTransferHelper.insertItem(output, sourceStack, false);
                transferred += sourceStack.getCount();
            }

            return transferred;
        }

        @Override
        public ProxiedTransferWrapper<IItemTransfer> createTransferWrapper() {
            return new ProxiedTransferWrapper.Item();
        }
    });


    public static final ITransferType<IFluidTransfer> FLUID = register(GTCEu.id("fluid"), new ITransferType<>() {
        @Override
        public Class<IFluidTransfer> getTransferClass() {
            return IFluidTransfer.class;
        }

        @Override
        public long transferAll(IFluidTransfer input, IFluidTransfer output) {
            return FluidTransferHelper.transferFluids(input, output, Long.MAX_VALUE, f -> true);
        }

        @Override
        public ProxiedTransferWrapper<IFluidTransfer> createTransferWrapper() {
            return new ProxiedTransferWrapper.Fluid();
        }
    });


    public static final ITransferType<Void> CONTROLLER = register(GTCEu.id("controller"), new ITransferType<>() {
        @Override
        public Class<Void> getTransferClass() {
            return Void.class;
        }

        @Override
        public long transferAll(Void input, Void output) {
            return 0L;
        }

        @Override
        public ProxiedTransferWrapper<Void> createTransferWrapper() {
            return new VoidTransferWrapper();
        }

        private static class VoidTransferWrapper extends ProxiedTransferWrapper<Void> {
            @Override
            protected Void createTransferProxy(IO io, Collection<Void> transfers) {
                return null;
            }
        }
    });


    ///////////////////////////////////////////////////////////////////////////////////////////


    private static <T> ITransferType<T> register(ResourceLocation id, ITransferType<T> transferType) {
        GTRegistries.ENDER_LINK_TRANSFER_TYPES.register(id, transferType);
        return transferType;
    }

    public static void init() {

    }
}

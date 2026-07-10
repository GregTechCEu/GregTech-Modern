package com.gregtechceu.gtceu.integration.ae2;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.MachineTrait;
import com.gregtechceu.gtceu.common.item.ColorSprayBehaviour;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import appeng.api.implementations.blockentities.IColorableBlockEntity;
import appeng.api.networking.IInWorldGridNodeHost;
import appeng.api.networking.pathing.ChannelMode;
import appeng.api.util.AEColor;
import appeng.blockentity.networking.CableBusBlockEntity;
import appeng.capabilities.Capabilities;
import appeng.core.AEConfig;

import java.util.List;
import java.util.Set;

public final class AE2Compat {

    private AE2Compat() {}

    public static MachineTrait createGridNodeHost(MetaMachine machine) {
        return new GridNodeHost(machine);
    }

    public static LazyOptional<?> getGridNodeHostCapability(Capability<?> cap, MetaMachine machine, Direction side) {
        if (cap != Capabilities.IN_WORLD_GRID_NODE_HOST) {
            return LazyOptional.empty();
        }
        if (machine instanceof IInWorldGridNodeHost nodeHost) {
            return Capabilities.IN_WORLD_GRID_NODE_HOST.orEmpty(cap, LazyOptional.of(() -> nodeHost));
        }
        List<IInWorldGridNodeHost> hosts = MetaMachineBlockEntity.getCapabilitiesFromTraits(
                machine.getTraits(), side, IInWorldGridNodeHost.class);
        if (hosts.isEmpty()) {
            return LazyOptional.empty();
        }
        return Capabilities.IN_WORLD_GRID_NODE_HOST.orEmpty(cap, LazyOptional.of(() -> hosts.get(0)));
    }

    public static boolean recolor(ColorSprayBehaviour spray, BlockEntity first, int limit, UseOnContext context) {
        if (!(first instanceof IColorableBlockEntity colorable)) {
            return false;
        }
        Player player = context.getPlayer();
        if (player == null) {
            return false;
        }

        Set<? extends IColorableBlockEntity> collected = first instanceof CableBusBlockEntity cableBus ?
                collect(cableBus, limit) : Set.of(colorable);
        DyeColor color = spray.getPaintColor();
        AEColor ae2Color = color == null ? AEColor.TRANSPARENT : AEColor.values()[color.ordinal()];
        for (IColorableBlockEntity blockEntity : collected) {
            if (blockEntity.getColor() == ae2Color) {
                continue;
            }
            blockEntity.recolourBlock(context.getClickedFace(), ae2Color, player);
            if (!spray.useItemDurability(player, context.getHand(), context.getItemInHand(), ItemStack.EMPTY)) {
                break;
            }
        }
        return true;
    }

    public static boolean hasInfiniteChannels() {
        return AEConfig.instance().getChannelMode() == ChannelMode.INFINITE;
    }

    private static Set<CableBusBlockEntity> collect(CableBusBlockEntity first, int limit) {
        return com.gregtechceu.gtceu.utils.BreadthFirstBlockSearch.conditionalBlockEntitySearch(
                CableBusBlockEntity.class, first, AE2Compat::areConnectedCables, limit, limit * 6);
    }

    private static boolean areConnectedCables(CableBusBlockEntity parent, CableBusBlockEntity child,
                                              Direction direction) {
        if (parent == null) {
            return true;
        }
        Direction childDirection = direction.getOpposite();
        return parent.getPart(direction) == null && parent.getCableConnectionType(direction).isValid() &&
                child.getPart(childDirection) == null && child.getCableConnectionType(childDirection).isValid() &&
                parent.getColor() == child.getColor();
    }
}

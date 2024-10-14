package com.gregtechceu.gtceu.api.block;

import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.common.machine.owner.ArgonautsOwner;
import com.gregtechceu.gtceu.common.machine.owner.FTBOwner;
import com.gregtechceu.gtceu.common.machine.owner.IMachineOwner;
import com.gregtechceu.gtceu.common.machine.owner.PlayerOwner;

import com.lowdragmc.lowdraglib.client.renderer.IBlockRendererProvider;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import dev.ftb.mods.ftbteams.FTBTeamsAPIImpl;
import dev.ftb.mods.ftbteams.api.Team;
import earth.terrarium.argonauts.api.guild.Guild;
import earth.terrarium.argonauts.common.handlers.guild.GuildHandler;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * @author KilaBash
 * @date 2023/3/31
 * @implNote IMachineBlock
 */
public interface IMachineBlock extends IBlockRendererProvider, EntityBlock {

    default Block self() {
        return (Block) this;
    }

    MachineDefinition getDefinition();

    RotationState getRotationState();

    static int colorTinted(BlockState blockState, @Nullable BlockAndTintGetter level, @Nullable BlockPos pos,
                           int index) {
        if (level != null && pos != null) {
            var machine = MetaMachine.getMachine(level, pos);
            if (machine != null) {
                return machine.tintColor(index);
            }
        }
        return -1;
    }

    @Nullable
    @Override
    default BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return getDefinition().getBlockEntityType().create(pos, state);
    }

    @Nullable
    @Override
    default <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                   BlockEntityType<T> blockEntityType) {
        if (blockEntityType == getDefinition().getBlockEntityType()) {
            if (state.getValue(BlockProperties.SERVER_TICK) && !level.isClientSide) {
                return (pLevel, pPos, pState, pTile) -> {
                    if (pTile instanceof IMachineBlockEntity metaMachine) {
                        metaMachine.getMetaMachine().serverTick();
                    }
                };
            }
            if (level.isClientSide) {
                return (pLevel, pPos, pState, pTile) -> {
                    if (pTile instanceof IMachineBlockEntity metaMachine) {
                        metaMachine.getMetaMachine().clientTick();
                    }
                };
            }
        }
        return null;
    }

    default void setMachineOwner(MetaMachine machine, ServerPlayer player) {
        if (IMachineOwner.MachineOwnerType.FTB.isAvailable()) {
            Optional<Team> team = FTBTeamsAPIImpl.INSTANCE.getManager().getTeamForPlayerID(player.getUUID());
            if (team.isPresent()) {
                machine.holder.setOwner(new FTBOwner(team.get(), player.getUUID()));
                return;
            }
        }
        if (IMachineOwner.MachineOwnerType.ARGONAUTS.isAvailable()) {
            Guild guild = GuildHandler.read(player.server).get(player);
            if (guild != null) {
                machine.holder.setOwner(new ArgonautsOwner(guild, player.getUUID()));
                return;
            }
        }
        machine.holder.setOwner(new PlayerOwner(player.getUUID()));
    }
}

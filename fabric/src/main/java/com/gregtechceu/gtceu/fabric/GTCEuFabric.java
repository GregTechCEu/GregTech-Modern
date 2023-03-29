package com.gregtechceu.gtceu.fabric;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.machine.feature.IInteractedMachine;
import com.gregtechceu.gtceu.api.recipe.ingredient.fabric.SizedIngredientImpl;
import com.gregtechceu.gtceu.common.ServerCommands;
import com.gregtechceu.gtceu.common.fabric.CommonProxyImpl;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;
import net.minecraft.world.InteractionResult;

public class GTCEuFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        GTCEu.init();
        CommonProxyImpl.init();
        CustomIngredientSerializer.register(SizedIngredientImpl.Serializer.INSTANCE);
        AttackBlockCallback.EVENT.register(((player, world, hand, pos, direction) -> {
            var blockState = world.getBlockState(pos);
            if (blockState.hasBlockEntity() && blockState.getBlock() instanceof MetaMachineBlock block
                    && block.getMachine(world, pos) instanceof IInteractedMachine machine) {
                if (machine.onLeftClick(player, world, hand, pos, direction)) {
                    return InteractionResult.SUCCESS;
                }
            }
            return InteractionResult.PASS;
        }));
        // register server commands
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> ServerCommands.createServerCommands().forEach(dispatcher::register));
    }

}

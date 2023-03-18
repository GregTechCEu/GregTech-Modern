package com.lowdragmc.gtceu.fabric;

import com.lowdragmc.gtceu.GTCEu;
import com.lowdragmc.gtceu.api.block.MetaMachineBlock;
import com.lowdragmc.gtceu.api.machine.feature.IInteractedMachine;
import com.lowdragmc.gtceu.api.recipe.ingredient.fabric.SizedIngredientImpl;
import com.lowdragmc.gtceu.api.registry.GTRegistries;
import com.lowdragmc.gtceu.common.CommonProxy;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;
import net.minecraft.world.InteractionResult;

public class GTCEuFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        GTCEu.init();
        CommonProxy.init();
        GTRegistries.REGISTRATE.registerRegistrate();
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
    }

}

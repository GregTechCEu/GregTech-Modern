package com.gregtechceu.gtceu.common.blockentity.fabric;

import com.gregtechceu.gtceu.api.blockentity.fabric.MetaMachineBlockEntityImpl;
import com.gregtechceu.gtceu.common.blockentity.KineticMachineBlockEntity;
import com.jozufozu.flywheel.api.MaterialManager;
import com.jozufozu.flywheel.backend.instancing.InstancedRenderRegistry;
import com.jozufozu.flywheel.backend.instancing.blockentity.BlockEntityInstance;
import com.lowdragmc.lowdraglib.LDLib;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;

/**
 * @author KilaBash
 * @date 2023/3/31
 * @implNote KineticMachineBlockEntityImpl
 */
public class KineticMachineBlockEntityImpl extends KineticMachineBlockEntity{

    protected KineticMachineBlockEntityImpl(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
    }

    public static KineticMachineBlockEntity create(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        return new KineticMachineBlockEntityImpl(typeIn, pos, state);
    }

    public static void onBlockEntityRegister(BlockEntityType blockEntityType,
                                             @Nullable NonNullSupplier<BiFunction<MaterialManager, KineticMachineBlockEntity, BlockEntityInstance<? super KineticMachineBlockEntity>>> instanceFactory,
                                             boolean renderNormally) {
        MetaMachineBlockEntityImpl.onBlockEntityRegister(blockEntityType);
        if (instanceFactory != null && LDLib.isClient()) {
            InstancedRenderRegistry.configure(blockEntityType)
                    .factory(instanceFactory.get())
                    .skipRender(be -> !renderNormally)
                    .apply();
        }
    }
}

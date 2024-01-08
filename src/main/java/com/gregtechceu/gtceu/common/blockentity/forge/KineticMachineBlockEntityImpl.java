package com.gregtechceu.gtceu.common.blockentity.forge;

import com.gregtechceu.gtceu.api.blockentity.forge.MetaMachineBlockEntityImpl;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.blockentity.KineticMachineBlockEntity;
import com.jozufozu.flywheel.api.MaterialManager;
import com.jozufozu.flywheel.backend.instancing.InstancedRenderRegistry;
import com.jozufozu.flywheel.backend.instancing.blockentity.BlockEntityInstance;
import com.lowdragmc.lowdraglib.LDLib;
import com.tterrag.registrate.util.OneTimeEventReceiver;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;

/**
 * @author KilaBash
 * @date 2023/3/31
 * @implNote KineticMachineBlockEntityImpl
 */
public class KineticMachineBlockEntityImpl extends KineticMachineBlockEntity {
    protected KineticMachineBlockEntityImpl(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
    }

    public static KineticMachineBlockEntity create(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        return new KineticMachineBlockEntityImpl(typeIn, pos, state);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        var result = MetaMachineBlockEntityImpl.getCapability(getMetaMachine(), cap, side);
        return result == null ? super.getCapability(cap, side) : result;
    }

    public static void onBlockEntityRegister(BlockEntityType blockEntityType, NonNullSupplier<BiFunction<MaterialManager, KineticMachineBlockEntity, BlockEntityInstance<? super KineticMachineBlockEntity>>> instanceFactory, boolean renderNormally) {
        if (instanceFactory != null && LDLib.isClient()) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                    OneTimeEventReceiver.addModListener(GTRegistries.REGISTRATE, FMLClientSetupEvent.class,
                            ($) -> InstancedRenderRegistry.configure(blockEntityType)
                                    .factory(instanceFactory.get())
                                    .skipRender((be) -> !renderNormally)
                                    .apply()));
        }
    }
}

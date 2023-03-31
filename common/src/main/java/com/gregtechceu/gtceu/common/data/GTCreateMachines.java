package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.block.IMachineBlock;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.item.MetaMachineItem;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder;
import com.gregtechceu.gtceu.client.renderer.machine.BatteryBufferRenderer;
import com.gregtechceu.gtceu.common.block.KineticMachineBlock;
import com.gregtechceu.gtceu.common.blockentity.KineticMachineBlockEntity;
import com.gregtechceu.gtceu.common.machine.KineticMachineDefinition;
import com.gregtechceu.gtceu.common.machine.kinetic.ElectricGearBoxMachine;
import com.simibubi.create.foundation.block.BlockStressValues;
import com.simibubi.create.foundation.utility.Couple;
import it.unimi.dsi.fastutil.ints.Int2FloatFunction;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;
import java.util.function.Function;

import static com.gregtechceu.gtceu.api.registry.GTRegistries.REGISTRATE;
import static com.gregtechceu.gtceu.common.data.GTMachines.*;

/**
 * @author KilaBash
 * @date 2023/3/31
 * @implNote GTCreateMachines
 */
public class GTCreateMachines {

    public static final KineticMachineDefinition[] ELECTRIC_GEAR_BOX_2A = registerElectricGearBox("electric_gear_box", 2, LOW_TIERS);
    public static final KineticMachineDefinition[] ELECTRIC_GEAR_BOX_8A = registerElectricGearBox("electric_gear_box", 8, LOW_TIERS);
    public static final KineticMachineDefinition[] ELECTRIC_GEAR_BOX_16A = registerElectricGearBox("electric_gear_box", 16, LOW_TIERS);

    public static KineticMachineDefinition[] registerElectricGearBox(String name, int maxAmps, int... tiers) {
        return registerTieredMachines(name + "." + maxAmps, true,
                tier -> GTValues.V[tier], (holder, tier) -> new ElectricGearBoxMachine(holder, tier, maxAmps), (tier, builder) -> builder
                        .rotationState(RotationState.ALL)
                        .renderer(() -> new BatteryBufferRenderer(tier, 4))
                        .tooltips(explosion())
                        .register()
                , tiers);
    }

    public static MachineBuilder<KineticMachineDefinition> registerMachines(String name, boolean isSource, float torque, Function<IMachineBlockEntity, MetaMachine> factory) {
        return REGISTRATE.machine(name,
                id -> new KineticMachineDefinition(id, isSource, torque),
                factory,
                KineticMachineBlock::new,
                MetaMachineItem::new,
                KineticMachineBlockEntity::create).onBlockEntityRegister(KineticMachineBlockEntity::onBlockEntityRegister);
    }

    public static KineticMachineDefinition[] registerTieredMachines(String name, boolean isSource,
                                                                    Int2FloatFunction torque,
                                                                    BiFunction<IMachineBlockEntity, Integer, MetaMachine> factory,
                                                                    BiFunction<Integer, MachineBuilder<KineticMachineDefinition>, KineticMachineDefinition> builder,
                                                                    int... tiers) {
        KineticMachineDefinition[] definitions = new KineticMachineDefinition[tiers.length];
        for (int i = 0; i < tiers.length; i++) {
            int tier = tiers[i];
            var register =  REGISTRATE.machine(name + "." + GTValues.VN[tier].toLowerCase(),
                            id -> new KineticMachineDefinition(id, isSource, torque.apply(tier)),
                            holder -> factory.apply(holder, tier), 
                            KineticMachineBlock::new, 
                            MetaMachineItem::new, 
                            KineticMachineBlockEntity::create)
                    .tier(tier).onBlockEntityRegister(KineticMachineBlockEntity::onBlockEntityRegister);
            definitions[i] = builder.apply(tier, register);
        }
        return definitions;
    }

    public GTCreateMachines() {
        BlockStressValues.registerProvider(GTCEu.MOD_ID, new BlockStressValues.IStressValueProvider() {
            @Override
            public double getImpact(Block block) {
                if (block instanceof IMachineBlock machineBlock && machineBlock.getDefinition() instanceof KineticMachineDefinition definition) {
                    if (!definition.isSource()) {
                        return definition.getTorque();
                    }
                }
                return 0;
            }

            @Override
            public double getCapacity(Block block) {
                if (block instanceof IMachineBlock machineBlock && machineBlock.getDefinition() instanceof KineticMachineDefinition definition) {
                    if (definition.isSource()) {
                        return definition.getTorque();
                    }
                }
                return 0;
            }

            @Override
            public boolean hasImpact(Block block) {
                if (block instanceof IMachineBlock machineBlock && machineBlock.getDefinition() instanceof KineticMachineDefinition definition) {
                    return !definition.isSource();
                }
                return false;
            }

            @Override
            public boolean hasCapacity(Block block) {
                if (block instanceof IMachineBlock machineBlock && machineBlock.getDefinition() instanceof KineticMachineDefinition definition) {
                    return definition.isSource();
                }
                return false;
            }

            @Nullable
            @Override
            public Couple<Integer> getGeneratedRPM(Block block) {
                return null;
            }
        });
    }
}

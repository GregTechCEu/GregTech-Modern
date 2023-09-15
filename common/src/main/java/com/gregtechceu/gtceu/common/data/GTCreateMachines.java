package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.block.IMachineBlock;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.item.MetaMachineItem;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.SimpleTieredMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder;
import com.gregtechceu.gtceu.client.instance.SplitShaftInstance;
import com.gregtechceu.gtceu.client.renderer.machine.KineticWorkableTieredHullMachineRenderer;
import com.gregtechceu.gtceu.client.renderer.machine.SplitShaftTieredHullMachineRenderer;
import com.gregtechceu.gtceu.common.block.KineticMachineBlock;
import com.gregtechceu.gtceu.common.blockentity.KineticMachineBlockEntity;
import com.gregtechceu.gtceu.common.machine.KineticMachineDefinition;
import com.gregtechceu.gtceu.common.machine.kinetic.ElectricGearBoxMachine;
import com.gregtechceu.gtceu.common.machine.kinetic.SimpleKineticElectricWorkableMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.KineticPartMachine;
import com.jozufozu.flywheel.api.MaterialManager;
import com.jozufozu.flywheel.backend.instancing.blockentity.BlockEntityInstance;
import com.simibubi.create.content.kinetics.BlockStressValues;
import com.simibubi.create.foundation.utility.Couple;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.gregtechceu.gtceu.api.GTValues.VLVH;
import static com.gregtechceu.gtceu.api.GTValues.VLVT;
import static com.gregtechceu.gtceu.api.registry.GTRegistries.REGISTRATE;
import static com.gregtechceu.gtceu.common.data.GTMachines.*;
import static com.gregtechceu.gtceu.utils.FormattingUtil.toEnglishName;

/**
 * @author KilaBash
 * @date 2023/3/31
 * @implNote GTCreateMachines
 */
public class GTCreateMachines {

    public static final KineticMachineDefinition[] ELECTRIC_GEAR_BOX_2A = registerElectricGearBox(2, LOW_TIERS);
    public static final KineticMachineDefinition[] ELECTRIC_GEAR_BOX_8A = registerElectricGearBox(8, LOW_TIERS);
    public static final KineticMachineDefinition[] ELECTRIC_GEAR_BOX_16A = registerElectricGearBox(16, LOW_TIERS);
    public static final KineticMachineDefinition[] ELECTRIC_GEAR_BOX_32A = registerElectricGearBox(32, LOW_TIERS);
    public static final KineticMachineDefinition[] KINETIC_MIXER = registerSimpleKineticElectricMachine("kinetic_mixer", GTRecipeTypes.CREATE_MIXER_RECIPES, LOW_TIERS);
    public static final KineticMachineDefinition[] KINETIC_INPUT_BOX = registerTieredMachines("kinetic_input_box", (tier, id) -> new KineticMachineDefinition(id, false, GTValues.V[tier]).setFrontRotation(true),
            (holder, tier) -> new KineticPartMachine(holder, tier, IO.IN), (tier, builder) -> builder
                    .langValue("%s %s %s".formatted(VLVH[tier], toEnglishName("kinetic_input_box"), VLVT[tier]))
                    .rotationState(RotationState.ALL)
                    .blockProp(BlockBehaviour.Properties::dynamicShape)
                    .blockProp(BlockBehaviour.Properties::noOcclusion)
                    .abilities(PartAbility.INPUT_KINETIC)
                    .renderer(() -> new SplitShaftTieredHullMachineRenderer(tier, GTCEu.id("block/machine/part/kinetic_input_box")))
                    .register()
            , () -> SplitShaftInstance::new, false, ELECTRIC_TIERS);
    public static final KineticMachineDefinition[] KINETIC_OUTPUT_BOX = registerTieredMachines("kinetic_output_box", (tier, id) -> new KineticMachineDefinition(id, true, GTValues.V[tier]).setFrontRotation(true),
            (holder, tier) -> new KineticPartMachine(holder, tier, IO.OUT), (tier, builder) -> builder
                    .langValue("%s %s %s".formatted(VLVH[tier], toEnglishName("kinetic_output_box"), VLVT[tier]))
                    .rotationState(RotationState.ALL)
                    .blockProp(BlockBehaviour.Properties::dynamicShape)
                    .blockProp(BlockBehaviour.Properties::noOcclusion)
                    .abilities(PartAbility.OUTPUT_KINETIC)
                    .renderer(() -> new SplitShaftTieredHullMachineRenderer(tier, GTCEu.id("block/machine/part/kinetic_output_box")))
                    .register()
            , () -> SplitShaftInstance::new, false, ELECTRIC_TIERS);

    @SuppressWarnings("unchecked")
    public static KineticMachineDefinition[] registerElectricGearBox(int maxAmps, int... tiers) {
        return registerTieredMachines("electric_gear_box_%sa".formatted(maxAmps), (tier, id) -> new KineticMachineDefinition(id, true, GTValues.V[tier]).setFrontRotation(true),
                (holder, tier) -> new ElectricGearBoxMachine(holder, tier, maxAmps), (tier, builder) -> builder
                        .langValue("%s %s %s".formatted(VLVH[tier], "Electric Gearbox %dA".formatted(maxAmps), VLVT[tier]))
                        .rotationState(RotationState.ALL)
                        .blockProp(BlockBehaviour.Properties::dynamicShape)
                        .blockProp(BlockBehaviour.Properties::noOcclusion)
                        .renderer(() -> new SplitShaftTieredHullMachineRenderer(tier, GTCEu.id("block/machine/electric_gear_box_%sa".formatted(maxAmps))))
                        .tooltips(explosion())
                        .register()
                , () -> SplitShaftInstance::new, false, tiers);
    }

    public static KineticMachineDefinition[] registerSimpleKineticElectricMachine(String name, GTRecipeType recipeType, int... tiers) {
        return registerTieredMachines(name, (tier, id) -> new KineticMachineDefinition(id, false, GTValues.V[tier]),
                (holder, tier) -> new SimpleKineticElectricWorkableMachine(holder, tier, defaultTankSizeFunction), (tier, builder) -> builder
                        .langValue("%s %s %s".formatted(VLVH[tier], toEnglishName(name), VLVT[tier]))
                        .rotationState(RotationState.NON_Y_AXIS)
                        .editableUI(SimpleTieredMachine.EDITABLE_UI_CREATOR.apply(GTCEu.id(name), recipeType))
                        .blockProp(BlockBehaviour.Properties::dynamicShape)
                        .blockProp(BlockBehaviour.Properties::noOcclusion)
                        .recipeType(recipeType)
                        .recipeModifier(GTRecipeModifiers.ELECTRIC_OVERCLOCK.apply(OverclockingLogic.NON_PERFECT_OVERCLOCK))
                        .renderer(() -> new KineticWorkableTieredHullMachineRenderer(tier, GTCEu.id("block/machine/kinetic_electric_machine"), GTCEu.id("block/machines/" + name)))
                        .tooltips(explosion())
                        .tooltips(workableTiered(tier, GTValues.V[tier], GTValues.V[tier] * 64, recipeType, defaultTankSizeFunction.apply(tier), true))
                        .register()
                , () -> SplitShaftInstance::new, false, tiers);
    }

    public static MachineBuilder<KineticMachineDefinition> registerMachines(String name, Function<ResourceLocation, KineticMachineDefinition> definitionFactory, Function<IMachineBlockEntity, MetaMachine> factory,
                                                                            @Nullable NonNullSupplier<BiFunction<MaterialManager, KineticMachineBlockEntity, BlockEntityInstance<? super KineticMachineBlockEntity>>> instanceFactory,
                                                                            boolean renderNormally) {
        return REGISTRATE.machine(name, definitionFactory, factory, KineticMachineBlock::new, MetaMachineItem::new, KineticMachineBlockEntity::create)
                .hasTESR(instanceFactory != null)
                .onBlockEntityRegister(type -> KineticMachineBlockEntity.onBlockEntityRegister(type, instanceFactory, renderNormally));
    }

    public static KineticMachineDefinition[] registerTieredMachines(String name,
                                                                    BiFunction<Integer, ResourceLocation, KineticMachineDefinition> definitionFactory,
                                                                    BiFunction<IMachineBlockEntity, Integer, MetaMachine> factory,
                                                                    BiFunction<Integer, MachineBuilder<KineticMachineDefinition>, KineticMachineDefinition> builder,
                                                                    @Nullable NonNullSupplier<BiFunction<MaterialManager, KineticMachineBlockEntity, BlockEntityInstance<? super KineticMachineBlockEntity>>> instanceFactory,
                                                                    boolean renderNormally,
                                                                    int... tiers) {
        KineticMachineDefinition[] definitions = new KineticMachineDefinition[GTValues.TIER_COUNT];
        for (int tier : tiers) {
            var register = REGISTRATE.machine(GTValues.VN[tier].toLowerCase(Locale.ROOT) + "_" + name,
                            id -> definitionFactory.apply(tier, id),
                            holder -> factory.apply(holder, tier),
                            KineticMachineBlock::new,
                            MetaMachineItem::new,
                            KineticMachineBlockEntity::create)
                    .tier(tier)
                    .hasTESR(instanceFactory != null)
                    .onBlockEntityRegister(type -> KineticMachineBlockEntity.onBlockEntityRegister(type, instanceFactory, renderNormally));
            definitions[tier] = builder.apply(tier, register);
        }
        return definitions;
    }

    public static void init() {
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

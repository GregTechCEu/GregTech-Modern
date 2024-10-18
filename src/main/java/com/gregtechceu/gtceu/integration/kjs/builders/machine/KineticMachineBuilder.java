package com.gregtechceu.gtceu.integration.kjs.builders.machine;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.item.MetaMachineItem;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder;
import com.gregtechceu.gtceu.client.instance.SplitShaftInstance;
import com.gregtechceu.gtceu.client.renderer.machine.KineticWorkableTieredHullMachineRenderer;
import com.gregtechceu.gtceu.common.block.KineticMachineBlock;
import com.gregtechceu.gtceu.common.blockentity.KineticMachineBlockEntity;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;
import com.gregtechceu.gtceu.common.machine.KineticMachineDefinition;
import com.gregtechceu.gtceu.common.machine.kinetic.SimpleKineticElectricWorkableMachine;
import com.gregtechceu.gtceu.common.registry.GTRegistration;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import com.tterrag.registrate.util.nullness.NonNullConsumer;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;

import java.util.Locale;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static com.gregtechceu.gtceu.api.GTValues.VLVH;
import static com.gregtechceu.gtceu.api.GTValues.VLVT;
import static com.gregtechceu.gtceu.common.data.GTMachines.explosion;
import static com.gregtechceu.gtceu.utils.FormattingUtil.toEnglishName;

/**
 * @author Screret
 * @date 2023/4/01
 * @implNote MachineBuilder
 */
public class KineticMachineBuilder extends MachineBuilder<KineticMachineDefinition> {

    public transient Int2IntFunction tankScalingFunction; // reflected in MachineFunctionPresets. DO NOT CHANGE!
    private final Object[] passedArguments;

    public KineticMachineBuilder(String name, boolean isSource, int tier, Object... args) {
        super(GTRegistration.REGISTRATE, name, (id) -> new KineticMachineDefinition(id, isSource, GTValues.V[tier]),
                (holder) -> new SimpleKineticElectricWorkableMachine(holder, tier, GTMachines.defaultTankSizeFunction,
                        args),
                KineticMachineBlock::new, MetaMachineItem::new, KineticMachineBlockEntity::create);
        this.tankScalingFunction = GTMachines.defaultTankSizeFunction;
        this.passedArguments = args;
    }

    public KineticMachineBuilder isSource(boolean isSource) {
        this.definitionFactory((id) -> new KineticMachineDefinition(id, isSource, GTValues.V[this.tier()]));
        return this;
    }

    @SuppressWarnings("unused") // Accessed via reflection
    public KineticMachineBuilder tankScalingFunction(Function<Object, Double> tankScalingFunction) {
        this.tankScalingFunction = tier -> tankScalingFunction.apply(tier).intValue();
        this.metaMachine((holder) -> new SimpleKineticElectricWorkableMachine(holder, tier(), this.tankScalingFunction,
                passedArguments));
        return this;
    }

    @Override
    public KineticMachineBuilder tier(int tier) {
        return (KineticMachineBuilder) super.tier(tier);
    }

    @Override
    public KineticMachineBuilder hasTESR(boolean hasTESR) {
        return (KineticMachineBuilder) super.hasTESR(hasTESR);
    }

    @Override
    public KineticMachineBuilder onBlockEntityRegister(NonNullConsumer<BlockEntityType<BlockEntity>> onBlockEntityRegister) {
        return (KineticMachineBuilder) super.onBlockEntityRegister(onBlockEntityRegister);
    }

    private static KineticMachineBuilder[] tieredMachines(String name,
                                                          BiConsumer<MachineBuilder<KineticMachineDefinition>, Integer> builderConsumer,
                                                          Integer... tiers) {
        KineticMachineBuilder[] builders = new KineticMachineBuilder[GTValues.TIER_COUNT];
        for (int tier : tiers) {
            var register = new KineticMachineBuilder(GTValues.VN[tier].toLowerCase(Locale.ROOT) + "_" + name, false,
                    tier)
                    .tier(tier)
                    .hasTESR(true)
                    .onBlockEntityRegister(type -> KineticMachineBlockEntity.onBlockEntityRegister(type,
                            () -> SplitShaftInstance::new, false));
            builderConsumer.accept(register, tier);
            builders[tier] = register;
        }
        return builders;
    }

    private static void simple(MachineBuilder<KineticMachineDefinition> builder, int tier) {
        builder.langValue("%s %s %s".formatted(VLVH[tier], toEnglishName(builder.id.getPath()), VLVT[tier]))
                .rotationState(RotationState.NON_Y_AXIS)
                // .editableUI(SimpleTieredMachine.EDITABLE_UI_CREATOR.apply(GTCEu.id(builder.id.getPath()),
                // recipeType))
                .blockProp(BlockBehaviour.Properties::dynamicShape)
                .blockProp(BlockBehaviour.Properties::noOcclusion)
                .recipeModifier(GTRecipeModifiers.ELECTRIC_OVERCLOCK.apply(OverclockingLogic.NON_PERFECT_OVERCLOCK))
                .renderer(() -> new KineticWorkableTieredHullMachineRenderer(tier,
                        GTCEu.id("block/machine/kinetic_electric_machine"),
                        GTCEu.id("block/machines/" + builder.id.getPath())))
                .tooltips(explosion());
        // .tooltips(workableTiered(tier, GTValues.V[tier], GTValues.V[tier] * 64, recipeType,
        // defaultTankSizeFunction.apply(tier), true));
    }

    public static MachineBuilder<KineticMachineDefinition> createAll(String name, Object... args) {
        KineticMachineBuilder[] builders = tieredMachines(name, KineticMachineBuilder::simple,
                MachineFunctionPresets.mapTierArray(args));
        return MachineFunctionPresets.builder(name, builders, KineticMachineBuilder.class,
                (id) -> new KineticMachineDefinition(id, false, 0), KineticMachineBlock::new,
                KineticMachineBlockEntity::create);
    }
}

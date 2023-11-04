package com.gregtechceu.gtceu.integration.kjs.builders.machine;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.SimpleGeneratorMachine;
import com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder;
import com.gregtechceu.gtceu.client.renderer.machine.SimpleGeneratorMachineRenderer;

import java.util.Arrays;
import java.util.Locale;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static com.gregtechceu.gtceu.api.GTValues.VLVH;
import static com.gregtechceu.gtceu.api.GTValues.VLVT;
import static com.gregtechceu.gtceu.common.data.GTMachines.defaultTankSizeFunction;
import static com.gregtechceu.gtceu.common.data.GTMachines.explosion;
import static com.gregtechceu.gtceu.utils.FormattingUtil.toEnglishName;

/**
 * @author Screret
 * @date 2023/4/01
 * @implNote MachineBuilder
 */
public class GeneratorBuilder extends SimpleMachineBuilder {
    public GeneratorBuilder(String name, Function<IMachineBlockEntity, MetaMachine> machineConstructor) {
        super(name, machineConstructor);
    }

    @Override
    public GeneratorBuilder tier(int tier) {
        return (GeneratorBuilder) super.tier(tier);
    }

    private static GeneratorBuilder[] tieredMachines(String name,
                                                     BiConsumer<GeneratorBuilder, Integer> builderConsumer,
                                                     Integer... tiers) {
        GeneratorBuilder[] builders = new GeneratorBuilder[GTValues.TIER_COUNT];
        for (int tier : tiers) {
            var register = new GeneratorBuilder(GTValues.VN[tier].toLowerCase(Locale.ROOT) + "_" + name, holder -> new SimpleGeneratorMachine(holder, tier, defaultTankSizeFunction)).tier(tier);
            builderConsumer.accept(register, tier);
            builders[tier] = register;
        }
        return builders;
    }

    private static void simple(GeneratorBuilder builder, int tier) {
        builder.langValue("%s %s Generator %s".formatted(VLVH[tier], toEnglishName(builder.id.getPath()), VLVT[tier]))
                //.editableUI(SimpleGeneratorMachine.EDITABLE_UI_CREATOR.apply(GTCEu.id(builder.id.getPath()), recipeType))
                .rotationState(RotationState.NON_Y_AXIS)
                //.recipeType(recipeType)
                .recipeModifier(SimpleGeneratorMachine::recipeModifier, true)
                .renderer(() -> new SimpleGeneratorMachineRenderer(tier, GTCEu.id("block/generators/" + builder.id.getPath())))
                .tooltips(explosion());
                //.tooltips(workableTiered(tier, GTValues.V[tier], GTValues.V[tier] * 64, recipeType, tankScalingFunction.apply(tier), false))
    }

    public static MachineBuilder<MachineDefinition> createAll(String name, Object... args) {
        GeneratorBuilder[] builders = tieredMachines(name, GeneratorBuilder::simple, MachineFunctionPresets.mapTierArray(args));
        return MachineFunctionPresets.builder(name, builders, GeneratorBuilder.class, MachineDefinition::createDefinition, MetaMachineBlock::new, MetaMachineBlockEntity::createBlockEntity);
    }
}

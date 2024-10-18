package com.gregtechceu.gtceu.integration.kjs.builders.machine;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.item.MetaMachineItem;
import com.gregtechceu.gtceu.api.machine.*;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;
import com.gregtechceu.gtceu.common.registry.GTRegistration;

import it.unimi.dsi.fastutil.ints.Int2IntFunction;

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
public class SimpleMachineBuilder extends MachineBuilder<MachineDefinition> {

    public transient Int2IntFunction tankScalingFunction; // reflected in MachineFunctionPresets. DO NOT CHANGE!

    public SimpleMachineBuilder(String name, Function<IMachineBlockEntity, MetaMachine> machineConstructor) {
        super(GTRegistration.REGISTRATE, name, MachineDefinition::createDefinition, machineConstructor,
                MetaMachineBlock::new, MetaMachineItem::new, MetaMachineBlockEntity::createBlockEntity);
        this.tankScalingFunction = GTMachines.defaultTankSizeFunction;
    }

    @SuppressWarnings("unused") // Accessed via reflection
    public SimpleMachineBuilder tankScalingFunction(Function<Integer, Double> tankScalingFunction) {
        this.tankScalingFunction = tier -> tankScalingFunction.apply(tier).intValue();
        this.metaMachine((holder) -> new SimpleTieredMachine(holder, tier(), this.tankScalingFunction));
        return this;
    }

    @Override
    public SimpleMachineBuilder tier(int tier) {
        return (SimpleMachineBuilder) super.tier(tier);
    }

    private static SimpleMachineBuilder[] simpleMachines(String name,
                                                         BiConsumer<SimpleMachineBuilder, Integer> builderConsumer,
                                                         Integer... tiers) {
        SimpleMachineBuilder[] builders = new SimpleMachineBuilder[GTValues.TIER_COUNT];
        for (int tier : tiers) {
            SimpleMachineBuilder register = new SimpleMachineBuilder(
                    GTValues.VN[tier].toLowerCase(Locale.ROOT) + "_" + name,
                    holder -> new SimpleTieredMachine(holder, tier, defaultTankSizeFunction)).tier(tier);
            builderConsumer.accept(register, tier);
            builders[tier] = register;
        }
        return builders;
    }

    public static void simple(SimpleMachineBuilder builder, int tier) {
        builder.tier(tier)
                .langValue("%s %s %s".formatted(VLVH[tier], toEnglishName(builder.name), VLVT[tier]))
                // .editableUI(SimpleTieredMachine.EDITABLE_UI_CREATOR.apply(GTCEu.id(builder.id.getPath()),
                // recipeType))
                .rotationState(RotationState.NON_Y_AXIS)
                // .recipeType(recipeType)
                .recipeModifier(GTRecipeModifiers.ELECTRIC_OVERCLOCK.apply(OverclockingLogic.NON_PERFECT_OVERCLOCK))
                .workableTieredHullRenderer(GTCEu.id("block/machines/" + builder.name))
                .tooltips(explosion());
        // .tooltips(workableTiered(tier, GTValues.V[tier], GTValues.V[tier] * 64, recipeType,
        // tankScalingFunction.apply(tier), true))
    }

    public static MachineBuilder<MachineDefinition> create(String name, Object... args) {
        SimpleMachineBuilder[] builders = simpleMachines(name, SimpleMachineBuilder::simple,
                MachineFunctionPresets.mapTierArray(args));
        return MachineFunctionPresets.builder(name, builders, SimpleMachineBuilder.class,
                MachineDefinition::createDefinition, MetaMachineBlock::new, MetaMachineBlockEntity::createBlockEntity);
    }
}

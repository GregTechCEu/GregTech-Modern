package com.gregtechceu.gtceu.integration.kjs.builders.machine;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.item.MetaMachineItem;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.steam.SimpleSteamMachine;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder;
import com.gregtechceu.gtceu.client.renderer.machine.WorkableSteamMachineRenderer;

import static com.gregtechceu.gtceu.api.GTValues.VLVH;
import static com.gregtechceu.gtceu.api.GTValues.VLVT;
import static com.gregtechceu.gtceu.utils.FormattingUtil.toEnglishName;

/**
 * @author Screret
 * @date 2023/4/01
 * @implNote MachineBuilder
 */
public class SteamMachineBuilder extends MachineBuilder<MachineDefinition> {

    public SteamMachineBuilder(String id, boolean isHighPressure, Object... args) {
        super(GTRegistries.REGISTRATE, id, MachineDefinition::createDefinition, holder -> new SimpleSteamMachine(holder, isHighPressure, args), MetaMachineBlock::new, MetaMachineItem::new, MetaMachineBlockEntity::createBlockEntity);
    }

    public static <T extends MachineBuilder<? extends MachineDefinition>> void simple(T builder, int tier) {
        builder.langValue("%s %s %s".formatted(VLVH[tier], toEnglishName(builder.id.getPath()), VLVT[tier]))
                .rotationState(RotationState.NON_Y_AXIS)
                //.recipeType(recipeType)
                .recipeModifier(SimpleSteamMachine::recipeModifier)
                .renderer(() -> new WorkableSteamMachineRenderer(tier > 0, GTCEu.id("block/machines/" + builder.id.getPath())));
    }

    private static SteamMachineBuilder[] createBuilder(String name, boolean hasHighPressure, Object... args) {
        SteamMachineBuilder lp = new SteamMachineBuilder("lp_" + name, false, args);
        return hasHighPressure ? new SteamMachineBuilder[] { lp, new SteamMachineBuilder("hp_" + name, true, args) } : new SteamMachineBuilder[] { lp };
    }

    public static MachineBuilder<MachineDefinition> createBoth(String name, Object... args) {
        SteamMachineBuilder[] builders;
        if (args.length > 0) {
            Object[] argsCopy;
            boolean hasHighPressure;
            if (args[0] instanceof Boolean hasHP) {
                argsCopy = MachineFunctionPresets.copyArgs(args, 1);
                hasHighPressure = hasHP;
            } else {
                argsCopy = args;
                hasHighPressure = true;
            }
            builders = createBuilder(name, hasHighPressure, argsCopy);
        } else {
            builders = createBuilder(name, true, args);
        }

        return MachineFunctionPresets.builder(name, builders, SteamMachineBuilder.class, MachineDefinition::createDefinition, MetaMachineBlock::new, MetaMachineBlockEntity::createBlockEntity);
    }
}

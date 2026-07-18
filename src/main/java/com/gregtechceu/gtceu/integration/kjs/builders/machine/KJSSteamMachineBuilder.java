package com.gregtechceu.gtceu.integration.kjs.builders.machine;

import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MachineInstanceFactory;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties;
import com.gregtechceu.gtceu.api.machine.steam.SimpleSteamMachine;
import com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.registry.registrate.builder.MachineBuilder;
import com.gregtechceu.gtceu.integration.kjs.GregTechKubeJSPlugin;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.resources.ResourceLocation;

import dev.latvian.mods.kubejs.registry.AdditionalObjectRegistry;
import dev.latvian.mods.kubejs.registry.BuilderBase;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(fluent = true, chain = true)
public class KJSSteamMachineBuilder extends BuilderBase<MachineDefinition> {

    @Setter
    public transient boolean hasLowPressure = true, hasHighPressure = true;
    @Setter
    public transient MachineInstanceFactory.Steam<? extends MetaMachine> machine = SimpleSteamMachine::new;
    @Setter
    public transient SteamDefinitionFunction definition = (isHP, def) -> def.tier(isHP ? 1 : 0);

    public KJSSteamMachineBuilder(ResourceLocation id) {
        super(id);

        this.dummyBuilder = true;
    }

    @SuppressWarnings("DataFlowIssue")
    @Override
    public MachineDefinition createObject() {
        // this method is never called on dummy builders (which this class is)
        return null;
    }

    @Override
    public void createAdditionalObjects(AdditionalObjectRegistry registry) {
        if (hasLowPressure) {
            var lowPressureBuilder = GregTechKubeJSPlugin.KUBEJS_DUMMY_REGISTRATE
                    .machine(String.format("lp_%s", this.id.getPath()),
                            holder -> machine.buildMachine(holder, false))
                    .langValue("Low Pressure " + FormattingUtil.toEnglishName(this.id.getPath()))
                    .tier(0)
                    .recipeModifier(SimpleSteamMachine::recipeModifier)
                    .modelProperty(GTMachineModelProperties.VENT_DIRECTION, RelativeDirection.BACK)
                    .workableSteamHullModel(false, id.withPrefix("block/machines/"));

            definition.apply(false, lowPressureBuilder);
            registry.add(GTRegistries.Keys.MACHINE, new MachineBuilderWrapper<>(lowPressureBuilder));
        }

        if (hasHighPressure) {
            var highPressureBuilder = GregTechKubeJSPlugin.KUBEJS_DUMMY_REGISTRATE
                    .machine(String.format("hp_%s", this.id.getPath()),
                            holder -> machine.buildMachine(holder, true))
                    .langValue("High Pressure " + FormattingUtil.toEnglishName(this.id.getPath()))
                    .tier(1)
                    .recipeModifier(SimpleSteamMachine::recipeModifier)
                    .modelProperty(GTMachineModelProperties.VENT_DIRECTION, RelativeDirection.BACK)
                    .workableSteamHullModel(true, id.withPrefix("block/machines/"));

            definition.apply(true, highPressureBuilder);
            registry.add(GTRegistries.Keys.MACHINE, new MachineBuilderWrapper<>(highPressureBuilder));
        }
    }

    @FunctionalInterface
    public interface SteamDefinitionFunction {

        void apply(boolean isHighPressure, MachineBuilder<?, ?, ?, ?> builder);
    }
}

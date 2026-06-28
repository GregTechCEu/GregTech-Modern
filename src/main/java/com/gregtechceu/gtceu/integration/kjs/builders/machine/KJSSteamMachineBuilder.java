package com.gregtechceu.gtceu.integration.kjs.builders.machine;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties;
import com.gregtechceu.gtceu.api.machine.steam.SimpleSteamMachine;
import com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder;
import com.gregtechceu.gtceu.integration.kjs.GTRegistryInfo;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.resources.ResourceLocation;

import dev.latvian.mods.kubejs.registry.BuilderBase;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(fluent = true, chain = true)
public class KJSSteamMachineBuilder extends BuilderBase<MachineDefinition> {

    @Setter
    public transient boolean hasLowPressure = true, hasHighPressure = true;
    @Setter
    public transient SteamCreationFunction machine = SimpleSteamMachine::new;
    @Setter
    public transient SteamDefinitionFunction definition = (isHP, def) -> def.tier(isHP ? 1 : 0);

    public KJSSteamMachineBuilder(ResourceLocation id) {
        super(id);

        this.dummyBuilder = true;
    }

    @Override
    public RegistryInfo<MachineDefinition> getRegistryType() {
        return GTRegistryInfo.MACHINE;
    }

    @SuppressWarnings("DataFlowIssue")
    @Override
    public MachineDefinition createObject() {
        // this method is never called on dummy builders (which this class is)
        return null;
    }

    @Override
    public void createAdditionalObjects() {
        if (hasLowPressure) {
            var lowPressureBuilder = GTRegistrate.create(id.getNamespace(), false)
                    .machine(String.format("lp_%s", this.id.getPath()),
                            holder -> machine.create(holder, false))
                    .langValue("Low Pressure " + FormattingUtil.toEnglishName(this.id.getPath()))
                    .tier(0)
                    .recipeModifier(SimpleSteamMachine::recipeModifier)
                    .modelProperty(GTMachineModelProperties.VENT_DIRECTION, RelativeDirection.BACK)
                    .workableSteamHullModel(false, id.withPrefix("block/machines/"));

            definition.apply(false, lowPressureBuilder);
            GTRegistryInfo.MACHINE.addBuilder(new MachineBuilderWrapper<>(lowPressureBuilder));
        }

        if (hasHighPressure) {
            var highPressureBuilder = GTRegistrate.create(id.getNamespace(), false)
                    .machine(String.format("hp_%s", this.id.getPath()),
                            holder -> machine.create(holder, true))
                    .langValue("High Pressure " + FormattingUtil.toEnglishName(this.id.getPath()))
                    .tier(1)
                    .recipeModifier(SimpleSteamMachine::recipeModifier)
                    .modelProperty(GTMachineModelProperties.VENT_DIRECTION, RelativeDirection.BACK)
                    .workableSteamHullModel(true, id.withPrefix("block/machines/"));

            definition.apply(true, highPressureBuilder);
            GTRegistryInfo.MACHINE.addBuilder(new MachineBuilderWrapper<>(highPressureBuilder));
        }
    }

    @FunctionalInterface
    public interface SteamCreationFunction {

        MetaMachine create(BlockEntityCreationInfo info, boolean isHighPressure);
    }

    @FunctionalInterface
    public interface SteamDefinitionFunction {

        void apply(boolean isHighPressure, MachineBuilder<?, ?, ?, ?> builder);
    }
}

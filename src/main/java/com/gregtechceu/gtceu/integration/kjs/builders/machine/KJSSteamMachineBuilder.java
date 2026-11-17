package com.gregtechceu.gtceu.integration.kjs.builders.machine;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties;
import com.gregtechceu.gtceu.api.machine.steam.SimpleSteamMachine;
import com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.resources.ResourceLocation;

import dev.latvian.mods.kubejs.registry.AdditionalObjectRegistry;
import dev.latvian.mods.kubejs.registry.BuilderBase;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;

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
    public void createAdditionalObjects(AdditionalObjectRegistry registry) {
        var registrate = GTRegistrate.createIgnoringListenerErrors(this.id.getNamespace());

        if (hasLowPressure) {
            var internalLowPressure = registrate
                    .machine(String.format("lp_%s", this.id.getPath()),
                            holder -> machine.create(holder, false));

            var lowPressureBuilder = new MachineBuilderJS<>(id, internalLowPressure)
                    .langValue("Low Pressure " + FormattingUtil.toEnglishName(this.id.getPath()))
                    .tier(0)
                    .recipeModifier(SimpleSteamMachine::recipeModifier)
                    .modelProperty(GTMachineModelProperties.VENT_DIRECTION, RelativeDirection.BACK)
                    .workableSteamHullModel(false, id.withPrefix("block/machines/"));

            definition.apply(false, lowPressureBuilder);

            registry.add(GTRegistries.Keys.MACHINE, lowPressureBuilder);
        }

        if (hasHighPressure) {

            var internalHighPressure = registrate
                    .machine(String.format("hp_%s", this.id.getPath()),
                            holder -> machine.create(holder, true));

            var highPressureBuilder = new MachineBuilderJS<>(id, internalHighPressure)
                    .langValue("High Pressure " + FormattingUtil.toEnglishName(this.id.getPath()))
                    .tier(1)
                    .recipeModifier(SimpleSteamMachine::recipeModifier)
                    .modelProperty(GTMachineModelProperties.VENT_DIRECTION, RelativeDirection.BACK)
                    .workableSteamHullModel(true, id.withPrefix("block/machines/"));

            definition.apply(true, highPressureBuilder);

            registry.add(GTRegistries.Keys.MACHINE, highPressureBuilder);
        }
    }

    @Override
    public @Nullable MachineDefinition createObject() {
        return null;
    }

    @FunctionalInterface
    public interface SteamCreationFunction {

        MetaMachine create(BlockEntityCreationInfo info, boolean isHighPressure);
    }

    @FunctionalInterface
    public interface SteamDefinitionFunction {

        void apply(boolean isHighPressure, MachineBuilderJS<?> builder);
    }
}

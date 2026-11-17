package com.gregtechceu.gtceu.integration.kjs.builders.machine;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties;
import com.gregtechceu.gtceu.api.machine.steam.SimpleSteamMachine;
import com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.resources.ResourceLocation;

import dev.latvian.mods.kubejs.client.LangKubeEvent;
import dev.latvian.mods.kubejs.generator.KubeAssetGenerator;
import dev.latvian.mods.kubejs.registry.BuilderBase;
import dev.latvian.mods.rhino.util.HideFromJS;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;

@Accessors(fluent = true, chain = true)
public class KJSSteamMachineBuilder extends BuilderBase<MachineDefinition> implements IMachineBuilderKJS {

    @Setter
    public transient boolean hasLowPressure = true, hasHighPressure = true;
    @Setter
    public transient SteamCreationFunction machine = SimpleSteamMachine::new;
    @Setter
    public transient SteamDefinitionFunction definition = (isHP, def) -> def.tier(isHP ? 1 : 0);

    @HideFromJS
    private @Nullable MachineBuilderJS<?> lowPressureBuilder = null, highPressureBuilder = null;

    public KJSSteamMachineBuilder(ResourceLocation id) {
        super(id);
        this.dummyBuilder = true;
    }

    @Override
    public @Nullable MachineDefinition createObject() {
        if (hasLowPressure) {

            var internalLowPressure = GTRegistrate.createIgnoringListenerErrors(this.id.getNamespace())
                    .machine(String.format("lp_%s", this.id.getPath()),
                            holder -> machine.create(holder, false));

            this.lowPressureBuilder = new MachineBuilderJS<>(id, internalLowPressure)
                    .langValue("Low Pressure " + FormattingUtil.toEnglishName(this.id.getPath()))
                    .tier(0)
                    .recipeModifier(SimpleSteamMachine::recipeModifier)
                    .modelProperty(GTMachineModelProperties.VENT_DIRECTION, RelativeDirection.BACK)
                    .workableSteamHullModel(false, id.withPrefix("block/machines/"));

            definition.apply(false, lowPressureBuilder);
        }

        if (hasHighPressure) {

            var internalHighPressure = GTRegistrate.createIgnoringListenerErrors(this.id.getNamespace())
                    .machine(String.format("hp_%s", this.id.getPath()),
                            holder -> machine.create(holder, true));

            this.highPressureBuilder = new MachineBuilderJS<>(id, internalHighPressure)
                    .langValue("High Pressure " + FormattingUtil.toEnglishName(this.id.getPath()))
                    .tier(1)
                    .recipeModifier(SimpleSteamMachine::recipeModifier)
                    .modelProperty(GTMachineModelProperties.VENT_DIRECTION, RelativeDirection.BACK)
                    .workableSteamHullModel(true, id.withPrefix("block/machines/"));

            definition.apply(true, highPressureBuilder);
        }

        return null;
    }

    @Override
    public void generateMachineModels() {
        if (lowPressureBuilder != null) lowPressureBuilder.generateMachineModels();
        if (highPressureBuilder != null) highPressureBuilder.generateMachineModels();
    }

    @Override
    public void generateAssets(KubeAssetGenerator generator) {
        if (lowPressureBuilder != null) lowPressureBuilder.generateAssets(generator);
        if (highPressureBuilder != null) highPressureBuilder.generateAssets(generator);
    }

    @Override
    public String getTranslationKeyGroup() {
        return "block";
    }

    @Override
    public void generateLang(LangKubeEvent lang) {
        if (lowPressureBuilder != null) lowPressureBuilder.generateLang(lang);
        if (highPressureBuilder != null) highPressureBuilder.generateLang(lang);
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

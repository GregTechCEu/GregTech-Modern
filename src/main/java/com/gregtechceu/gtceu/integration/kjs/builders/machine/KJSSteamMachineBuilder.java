package com.gregtechceu.gtceu.integration.kjs.builders.machine;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties;
import com.gregtechceu.gtceu.api.machine.steam.SimpleSteamMachine;
import com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection;
import com.gregtechceu.gtceu.api.registry.registrate.BuilderBase;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.resources.ResourceLocation;

import dev.latvian.mods.kubejs.client.LangEventJS;
import dev.latvian.mods.kubejs.generator.AssetJsonGenerator;
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

    private transient MachineBuilder<?, ?, ?> lowPressureBuilder = null, highPressureBuilder = null;

    public KJSSteamMachineBuilder(ResourceLocation id) {
        super(id);
    }

    @Override
    public MachineDefinition register() {
        var registrate = GTRegistrate.createIgnoringListenerErrors(this.id.getNamespace());

        if (hasLowPressure) {
            this.lowPressureBuilder = registrate.machine(
                    String.format("lp_%s", this.id.getPath()),
                    holder -> machine.create(holder, false));
            lowPressureBuilder.langValue("Low Pressure " + FormattingUtil.toEnglishName(this.id.getPath()))
                    .tier(0)
                    .recipeModifier(SimpleSteamMachine::recipeModifier)
                    .modelProperty(GTMachineModelProperties.VENT_DIRECTION, RelativeDirection.BACK)
                    .workableSteamHullModel(false, id.withPrefix("block/machines/"));
            definition.apply(false, lowPressureBuilder);
            value = lowPressureBuilder.register();
        }

        if (hasHighPressure) {
            this.highPressureBuilder = registrate.machine(
                    String.format("hp_%s", this.id.getPath()),
                    holder -> machine.create(holder, true));
            highPressureBuilder.langValue("High Pressure " + FormattingUtil.toEnglishName(this.id.getPath()))
                    .tier(1)
                    .recipeModifier(SimpleSteamMachine::recipeModifier)
                    .modelProperty(GTMachineModelProperties.VENT_DIRECTION, RelativeDirection.BACK)
                    .workableSteamHullModel(true, id.withPrefix("block/machines/"));
            definition.apply(true, highPressureBuilder);
            value = highPressureBuilder.register();
        }

        return value;
    }

    @Override
    public void generateAssetJsons(@Nullable AssetJsonGenerator generator) {
        super.generateAssetJsons(generator);
        if (this.lowPressureBuilder != null) {
            this.lowPressureBuilder.generateAssetJsons(generator);
        }
        if (this.highPressureBuilder != null) {
            this.highPressureBuilder.generateAssetJsons(generator);
        }
    }

    @Override
    public void generateLang(LangEventJS lang) {
        super.generateLang(lang);
        lang.add(GTCEu.MOD_ID, value.getDescriptionId(), value.getLangValue());
    }

    @Override
    public MachineDefinition get() {
        return value;
    }

    @FunctionalInterface
    public interface SteamCreationFunction {

        MetaMachine create(BlockEntityCreationInfo info, boolean isHighPressure);
    }

    @FunctionalInterface
    public interface SteamDefinitionFunction {

        void apply(boolean isHighPressure, MachineBuilder<?, ?, ?> builder);
    }
}

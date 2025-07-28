package com.gregtechceu.gtceu.integration.kjs.builders.machine;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties;
import com.gregtechceu.gtceu.api.machine.steam.SimpleSteamMachine;
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;
import com.gregtechceu.gtceu.api.registry.registrate.BuilderBase;
import com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder;
import com.gregtechceu.gtceu.common.registry.GTRegistration;
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
    public volatile boolean hasLowPressure = true, hasHighPressure = true;
    @Setter
    public volatile SteamCreationFunction machine = SimpleSteamMachine::new;
    @Setter
    public volatile SteamDefinitionFunction definition = (isHP, def) -> def.tier(isHP ? 1 : 0);

    private volatile MachineBuilder<?> lowPressureBuilder = null, highPressureBuilder = null;
    private volatile MachineDefinition hpValue = null;

    public KJSSteamMachineBuilder(ResourceLocation id) {
        super(id);
    }

    @Override
    public MachineDefinition register() {
        if (hasLowPressure) {
            this.lowPressureBuilder = GTRegistration.REGISTRATE.machine(
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
            this.highPressureBuilder = GTRegistration.REGISTRATE.machine(
                    String.format("hp_%s", this.id.getPath()),
                    holder -> machine.create(holder, true));
            highPressureBuilder.langValue("High Pressure " + FormattingUtil.toEnglishName(this.id.getPath()))
                    .tier(1)
                    .recipeModifier(SimpleSteamMachine::recipeModifier)
                    .modelProperty(GTMachineModelProperties.VENT_DIRECTION, RelativeDirection.BACK)
                    .workableSteamHullModel(true, id.withPrefix("block/machines/"));
            definition.apply(true, highPressureBuilder);
            hpValue = highPressureBuilder.register();
        }

        return value != null ? value : hpValue;
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
        if (value != null) {
            lang.add(GTCEu.MOD_ID, value.getDescriptionId(), value.getLangValue());
        }
        if (hpValue != null) {
            lang.add(GTCEu.MOD_ID, hpValue.getDescriptionId(), hpValue.getLangValue());
        }
    }

    @Override
    public MachineDefinition get() {
        return value != null ? value : hpValue;
    }

    @FunctionalInterface
    public interface SteamCreationFunction {

        MetaMachine create(IMachineBlockEntity holder, boolean isHighPressure);
    }

    @FunctionalInterface
    public interface SteamDefinitionFunction {

        void apply(boolean isHighPressure, MachineBuilder<?> builder);
    }
}

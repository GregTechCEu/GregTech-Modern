package com.gregtechceu.gtceu.integration.kjs.builders.machine;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.steam.SimpleSteamMachine;
import com.gregtechceu.gtceu.api.registry.registrate.BuilderBase;
import com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder;
import com.gregtechceu.gtceu.client.renderer.machine.WorkableSteamMachineRenderer;
import com.gregtechceu.gtceu.common.registry.GTRegistration;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.resources.ResourceLocation;

import dev.latvian.mods.kubejs.client.LangEventJS;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(fluent = true, chain = true)
public class KJSSteamMachineBuilder extends BuilderBase<MachineDefinition> {

    @Setter
    public volatile boolean hasHighPressure = true;
    @Setter
    public volatile SteamCreationFunction machine = SimpleSteamMachine::new;
    @Setter
    public volatile SteamDefinitionFunction definition = (isHP, def) -> def.tier(isHP ? 1 : 0);
    private volatile MachineDefinition hp = null;

    public KJSSteamMachineBuilder(ResourceLocation id) {
        super(id);
    }

    @Override
    public MachineDefinition register() {
        MachineBuilder<?> lowPressureBuilder = GTRegistration.REGISTRATE.machine(
                String.format("lp_%s", this.id.getPath()),
                holder -> machine.create(holder, false));
        lowPressureBuilder.langValue("Low Pressure " + FormattingUtil.toEnglishName(this.id.getPath()))
                .tier(0)
                .recipeModifier(SimpleSteamMachine::recipeModifier)
                .renderer(() -> new WorkableSteamMachineRenderer(false, id.withPrefix("block/machines/")));
        definition.apply(false, lowPressureBuilder);
        var lowPressure = lowPressureBuilder.register();

        if (hasHighPressure) {
            MachineBuilder<?> highPressureBuilder = GTRegistration.REGISTRATE.machine(
                    String.format("hp_%s", this.id.getPath()),
                    holder -> machine.create(holder, true));
            highPressureBuilder.langValue("High Pressure " + FormattingUtil.toEnglishName(this.id.getPath()))
                    .tier(1)
                    .recipeModifier(SimpleSteamMachine::recipeModifier)
                    .renderer(() -> new WorkableSteamMachineRenderer(true, id.withPrefix("block/machines/")));
            definition.apply(true, highPressureBuilder);
            hp = highPressureBuilder.register();
        }

        return value = lowPressure;
    }

    @Override
    public void generateLang(LangEventJS lang) {
        super.generateLang(lang);
        lang.add(value.getDescriptionId(), value.getLangValue());
        if (hp != null) {
            lang.add(GTCEu.MOD_ID, hp.getDescriptionId(), hp.getLangValue());
        }
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

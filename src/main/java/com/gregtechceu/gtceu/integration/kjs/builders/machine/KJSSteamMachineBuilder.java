package com.gregtechceu.gtceu.integration.kjs.builders.machine;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties;
import com.gregtechceu.gtceu.api.machine.steam.SimpleSteamMachine;
import com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder;
import com.gregtechceu.gtceu.integration.kjs.GTRegistryInfo;
import com.gregtechceu.gtceu.integration.kjs.helpers.IGTDummyBuilder;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.resources.ResourceLocation;

import dev.latvian.mods.kubejs.client.LangEventJS;
import dev.latvian.mods.kubejs.generator.AssetJsonGenerator;
import dev.latvian.mods.kubejs.registry.BuilderBase;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;

@Accessors(fluent = true, chain = true)
public class KJSSteamMachineBuilder extends BuilderBase<MachineDefinition>
                                    implements IMachineBuilderKJS, IGTDummyBuilder<MachineDefinition> {

    @Setter
    public transient boolean hasLowPressure = true, hasHighPressure = true;
    @Setter
    public transient SteamCreationFunction machine = SimpleSteamMachine::new;
    @Setter
    public transient SteamDefinitionFunction definition = (isHP, def) -> def.tier(isHP ? 1 : 0);

    private @Nullable MachineBuilder<?, ?, ?> lowPressureBuilder = null, highPressureBuilder = null;
    @Nullable
    private MachineDefinition lpObject = null, hpObject = null;

    public KJSSteamMachineBuilder(ResourceLocation id) {
        super(id);
    }

    @Override
    public RegistryInfo<MachineDefinition> getRegistryType() {
        return GTRegistryInfo.MACHINE;
    }

    @Override
    public MachineDefinition createObject() {
        MachineDefinition value = null;
        if (hasLowPressure) {
            this.lowPressureBuilder = GTRegistrate.createIgnoringListenerErrors(this.id.getNamespace())
                    .machine(String.format("lp_%s", this.id.getPath()),
                            holder -> machine.create(holder, false))
                    .langValue("Low Pressure " + FormattingUtil.toEnglishName(this.id.getPath()))
                    .tier(0)
                    .recipeModifier(SimpleSteamMachine::recipeModifier)
                    .modelProperty(GTMachineModelProperties.VENT_DIRECTION, RelativeDirection.BACK)
                    .workableSteamHullModel(false, id.withPrefix("block/machines/"));

            definition.apply(false, lowPressureBuilder);
            this.lpObject = lowPressureBuilder.register();
            value = lpObject;
        }

        if (hasHighPressure) {
            this.highPressureBuilder = GTRegistrate.createIgnoringListenerErrors(this.id.getNamespace())
                    .machine(String.format("hp_%s", this.id.getPath()),
                            holder -> machine.create(holder, true))
                    .langValue("High Pressure " + FormattingUtil.toEnglishName(this.id.getPath()))
                    .tier(1)
                    .recipeModifier(SimpleSteamMachine::recipeModifier)
                    .modelProperty(GTMachineModelProperties.VENT_DIRECTION, RelativeDirection.BACK)
                    .workableSteamHullModel(true, id.withPrefix("block/machines/"));

            definition.apply(true, highPressureBuilder);
            this.hpObject = highPressureBuilder.register();
            if (value == null) value = hpObject;
        }

        return value;
    }

    @Override
    public void generateMachineModels() {
        generateMachineModel(lowPressureBuilder, lpObject);
        generateMachineModel(highPressureBuilder, hpObject);
    }

    @Override
    public void generateAssetJsons(AssetJsonGenerator generator) {
        if (this.lowPressureBuilder != null) {
            generator.itemModel(id, gen -> gen.parent(id.withPrefix("block/machine/").toString()));
        }
        if (this.highPressureBuilder != null) {
            generator.itemModel(id, gen -> gen.parent(id.withPrefix("block/machine/").toString()));
        }
    }

    @Override
    public String getTranslationKeyGroup() {
        return "block";
    }

    @Override
    public void generateLang(LangEventJS lang) {
        if (lpObject != null) {
            lang.add(GTCEu.MOD_ID, lpObject.getDescriptionId(), lpObject.getLangValue());
        }
        if (hpObject != null) {
            lang.add(GTCEu.MOD_ID, hpObject.getDescriptionId(), hpObject.getLangValue());
        }
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

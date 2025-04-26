package com.gregtechceu.gtceu.data.damagesource;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.medicalcondition.MedicalCondition;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageEffects;
import net.minecraft.world.damagesource.DamageScaling;
import net.minecraft.world.damagesource.DamageType;

public class GTDamageTypes {

    public static final ResourceKey<DamageType> HEAT = create("heat");
    public static final ResourceKey<DamageType> CHEMICAL = create("chemical");
    public static final ResourceKey<DamageType> ELECTRIC = create("electric");
    public static final ResourceKey<DamageType> RADIATION = create("radiation");
    public static final ResourceKey<DamageType> TURBINE = create("turbine");

    public static void init() {}

    private static ResourceKey<DamageType> create(String path) {
        return ResourceKey.create(Registries.DAMAGE_TYPE, GTCEu.id(path));
    }

    public static void bootstrap(BootstrapContext<DamageType> ctx) {
        ctx.register(HEAT, new DamageType("gtceu.heat", 0, DamageEffects.BURNING));
        ctx.register(CHEMICAL, new DamageType("gtceu.chemical", 0));
        ctx.register(ELECTRIC, new DamageType("gtceu.electric", 0));
        ctx.register(RADIATION, new DamageType("gtceu.radiation", 0));
        ctx.register(TURBINE, new DamageType("gtceu.turbine", 0));

        for (var entry : MedicalCondition.CONDITIONS.entrySet()) {
            String name = entry.getKey();
            MedicalCondition condition = entry.getValue();
            ctx.register(condition.getDamageType(),
                    new DamageType("gtceu.medical_condition." + name, DamageScaling.NEVER, 0));
        }
    }
}

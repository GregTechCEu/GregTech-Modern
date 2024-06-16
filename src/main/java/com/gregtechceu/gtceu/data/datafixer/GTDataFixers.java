package com.gregtechceu.gtceu.data.datafixer;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.datafixer.DataFixesInternals;
import com.gregtechceu.gtceu.common.datafixer.GTItemStackComponentizationFix;

import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mojang.datafixers.DataFixerBuilder;
import com.mojang.datafixers.schemas.Schema;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiFunction;

import static com.gregtechceu.gtceu.api.datafixer.DataFixesInternals.BASE_SCHEMA;

public class GTDataFixers {

    private static final BiFunction<Integer, Schema, Schema> SAME = Schema::new;
    private static final BiFunction<Integer, Schema, Schema> SAME_NAMESPACED = NamespacedSchema::new;

    public static void register() {
        GTCEu.LOGGER.info("Registering data fixers");

        DataFixesInternals api = DataFixesInternals.get();

        DataFixerBuilder builder = new DataFixerBuilder(GTCEuAPI.GT_DATA_VERSION);
        addFixers(builder);

        ExecutorService executor = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder()
                .setNameFormat("GTCEuM Datafixer Bootstrap").setDaemon(true).setPriority(1).build());
        DataFixerBuilder.Result result = builder.build();
        result.optimize(DataFixTypes.TYPES_FOR_LEVEL_LIST, executor);
        api.registerFixer(GTCEuAPI.GT_DATA_VERSION, result.fixer());
    }

    public static void addFixers(DataFixerBuilder builder) {
        builder.addSchema(0, BASE_SCHEMA);

        Schema schema = builder.addSchema(1, SAME_NAMESPACED);
        builder.addFixer(new GTItemStackComponentizationFix(schema));
    }
}

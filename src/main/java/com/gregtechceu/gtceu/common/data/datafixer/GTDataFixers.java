package com.gregtechceu.gtceu.common.data.datafixer;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.datafixer.DataFixHelper;
import com.gregtechceu.gtceu.api.datafixer.LazyDataFixer;
import com.gregtechceu.gtceu.api.datafixer.fixes.MaterialRenameFix;
import com.gregtechceu.gtceu.common.datafixer.fixes.*;
import com.gregtechceu.gtceu.common.datafixer.schemas.*;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.SharedConstants;
import net.minecraft.util.datafix.fixes.*;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.DataFixerBuilder;
import com.mojang.datafixers.schemas.Schema;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.BiFunction;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

/**
 * Register new datafixers in {@link #addFixers(DataFixerBuilder)}. Remember to bump {@link GTCEu#GT_DATA_VERSION} in
 * every PR that changes existing save data and add a schema & fixer for it here.
 *
 * <p>
 * For changes that do not change named types or add new (block) entities, use {@link #SAME_NAMESPACED} for the schema.
 * Register machines (and other block entities) in a schema overriding {@link Schema#registerBlockEntities}.
 *
 * <p>
 * <h3>DO NOT ADD NEW MACHINES TO {@link V0}!! THEY SHOULD BE IN NEW, CORRECTLY VERSIONED, SCHEMAS!!</h3>
 * Reference vanilla schemas for more information on that, examples linked below. Also check how they're used in
 * vanilla's {@link net.minecraft.util.datafix.DataFixers DataFixers}.<br>
 * {@link net.minecraft.util.datafix.schemas.V1460 V1460}, {@link net.minecraft.util.datafix.schemas.V1906 V1906}<br>
 * Also, always add a {@link AddNewChoices} fixer of the appropriate type(s) when adding new types to a schema.
 *
 * <p>
 * Note how only fields that use other registered (named) types (such as item/fluid stacks) are defined. The same should
 * be done for all types.<br>
 * If the type doesn't depend on any named types, register it with {@link Schema#register} like this:
 * 
 * <pre>{@code
 * 
 * public Map<String, Supplier<TypeTemplate>> registerBlockEntities(Schema schema) {
 *     Map<String, Supplier<TypeTemplate>> map = super.registerBlockEntities(schema);
 *     // ...
 *     schema.register(map, "my_id_here", () -> traitHolder(schema));
 *     // ...
 *     return map;
 * }
 * }</pre>
 * 
 * We don't use {@link Schema#registerSimple} because we need to register the trait holder type for all machines.
 */
@SuppressWarnings("SameParameterValue")
public class GTDataFixers {

    private static final BiFunction<Integer, Schema, Schema> SAME_NAMESPACED = NamespacedSchema::new;

    @Getter
    private static final @Nullable DataFixer dataFixer = createFixer(SharedConstants.DATA_FIX_TYPES_TO_OPTIMIZE);

    public static void init() {}

    private static @Nullable DataFixer createFixer(final Set<DSL.TypeReference> typesToOptimize) {
        if (!ConfigHolder.INSTANCE.compat.doDataFixers) {
            return null;
        }

        DataFixHelper.LOGGER.info("Registering data fixers");

        return new LazyDataFixer(() -> {
            try {
                DataFixerBuilder builder = new DataFixerBuilder(GTCEu.GT_DATA_VERSION);
                addFixers(builder);

                if (typesToOptimize.isEmpty()) {
                    return builder.buildUnoptimized();
                } else {
                    Executor executor = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder()
                            .setNameFormat("GTCEu Datafixer Bootstrap").setDaemon(true).setPriority(1).build());
                    return builder.buildOptimized(typesToOptimize, executor);
                }
            } catch (Exception ex) {
                DataFixHelper.LOGGER.warn(
                        "Failed to initialize! Either someone stopped DFU from initializing, or this Minecraft build is hosed.");
                DataFixHelper.LOGGER.warn("Using no-op implementation.");
                DataFixHelper.LOGGER.warn("Full error: ", ex);

                return null;
            }
        });
    }

    public static void addFixers(DataFixerBuilder builder) {
        builder.addSchema(new V0(DataFixUtils.makeKey(0), DataFixHelper.getLatestVanillaSchema()));

        Schema v1 = builder.addSchema(1, V1::new);
        createBlockItemRenameFix(builder, v1, "Rename RTM Alloy Coil Block",
                createRenamer("tungstensteel_coil_block", "rtm_alloy_coil_block"));

        builder.addFixer(ItemRenameFix.create(v1, "Fix Advanced Nanomuscle Chestplate name",
                createRenamer("gtceu:avanced_nanomuscle_chestplate", "gtceu:advanced_nanomuscle_chestplate")));

        createBlockItemRenameFix(builder, v1, "Rename Palladium Substation Casing",
                createRenamer("gtceu:palladium_substation", "gtceu:palladium_substation_casing"));

        builder.addFixer(ItemRenameFix.create(v1, "Rename Tungstensteel Fluid Cell",
                createRenamer("gtceu:tungstensteel_fluid_cell", "gtceu:tungsten_steel_fluid_cell")));

        createBlockItemEntityRenameFix(builder, v1, "Rename Low Pressure Steam Miner",
                createRenamer("gtceu:steam_miner", "gtceu:lp_steam_miner"));

        builder.addFixer(ItemRenameFix.create(v1, "Rename electric wire cutters",
                createRenamer(Pattern.compile("([lhi])v_([a-z0-9/._-]+)_wirecutter"), "$1v_$2_wire_cutter")));

        // separator

        Schema v2 = builder.addSchema(2, SAME_NAMESPACED);
        builder.addFixer(new LDLibPayloadWrapperRemovalFix(v2));
        builder.addFixer(new AutoOutputTraitFix(v2));

        // separator

        Schema v3 = builder.addSchema(3, SAME_NAMESPACED);
        builder.addFixer(new UpwardsFacingPropertyFix(v3));

        // separator

        Schema v4 = builder.addSchema(4, V4::new);
        builder.addFixer(new TraitHolderificationFix(v4));

        // separator

        // Schema v10 = builder.addSchema(10, SAME_NAMESPACED);
        // createMaterialRenameFix(builder, v10, "U238",
        // createRenamer(Pattern.compile("gtceu:(.*)uranium_"), "gtceu:$1uranium_238_"));
        // createMaterialRenameFix(builder, v10, "Pu239",
        // createRenamer(Pattern.compile("gtceu:(.*)plutonium_"), "gtceu:$1plutonium_239_"));
        // createMaterialRenameFix(builder, v10, "Red Granite",
        // createRenamer(Pattern.compile("gtceu:(.*)granite_red"), "gtceu:$1red_granite"));
        //
        // createMaterialRenameFix(builder, v10, "Oil Variants",
        // createRenamer(OilVariantsRenameFix.RENAMED_ITEM_IDS));
    }

    private static void createBlockItemRenameFix(DataFixerBuilder builder, Schema schema, String name,
                                                 UnaryOperator<String> renamer) {
        builder.addFixer(ItemRenameFix.create(schema, name, renamer));
        builder.addFixer(BlockRenameFix.create(schema, name, renamer));
    }

    private static void createMaterialRenameFix(DataFixerBuilder builder, Schema schema, String name,
                                                UnaryOperator<String> renamer) {
        createBlockItemRenameFix(builder, schema, name, renamer);
        builder.addFixer(MaterialRenameFix.create(schema, name, renamer));
    }

    private static void createBlockItemEntityRenameFix(DataFixerBuilder builder, Schema schema, String name,
                                                       UnaryOperator<String> renamer) {
        createBlockItemRenameFix(builder, schema, name, renamer);
        builder.addFixer(BlockEntityRenameFix.create(schema, name, renamer));
    }

    private static UnaryOperator<String> createRenamer(String oldName, String newName) {
        return id -> Objects.equals(NamespacedSchema.ensureNamespaced(id), oldName) ? newName : id;
    }

    private static UnaryOperator<String> createRenamer(Map<String, String> renameMap) {
        return id -> renameMap.getOrDefault(NamespacedSchema.ensureNamespaced(id), id);
    }

    private static UnaryOperator<String> createRenamer(Pattern check, String replaceWith) {
        return id -> check.matcher(NamespacedSchema.ensureNamespaced(id)).replaceAll(replaceWith);
    }
}

package com.gregtechceu.gtceu.common.data.datafixer;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.datafixer.DataFixHelper;
import com.gregtechceu.gtceu.api.datafixer.FixedDataFixerBuilder;
import com.gregtechceu.gtceu.api.datafixer.LazyDataFixer;
import com.gregtechceu.gtceu.api.datafixer.fixes.FluidRenameFix;
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
 * Register new datafixers in {@link #addFixers(FixedDataFixerBuilder)}. Remember to bump {@link GTCEu#GT_DATA_VERSION} in
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
                FixedDataFixerBuilder builder = new FixedDataFixerBuilder(GTCEu.GT_DATA_VERSION);
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

    public static void addFixers(FixedDataFixerBuilder builder) {
        builder.addSchema(new V0(DataFixUtils.makeKey(0), DataFixHelper.getLatestVanillaSchema()));

        Schema v1 = builder.addSchema(1, V1::new);
        builder.addFixer(new AddNewChoices(v1, "Added High Pressure Steam Miner", References.BLOCK_ENTITY));
        createBlockItemRenameFix(builder, v1, "Rename RTM Alloy Coil Block",
                createRenamer("tungstensteel_coil_block", "rtm_alloy_coil_block"));

        builder.addFixer(ItemRenameFix.create(v1, "Fix Advanced Nanomuscle Chestplate name",
                createRenamer("gtceu:avanced_nanomuscle_chestplate", "gtceu:advanced_nanomuscle_chestplate")));

        builder.addFixer(ItemRenameFix.create(v1, "Rename Tungstensteel Fluid Cell",
                createRenamer("gtceu:tungstensteel_fluid_cell", "gtceu:tungsten_steel_fluid_cell")));

        createBlockItemEntityRenameFix(builder, v1, "Rename Low Pressure Steam Miner",
                createRenamer("gtceu:steam_miner", "gtceu:lp_steam_miner"));

        builder.addFixer(ItemRenameFix.create(v1, "Rename electric wire cutters",
                createRenamer(Pattern.compile("([lhi])v_([a-z0-9/._-]+)_wirecutter"), "$1v_$2_wire_cutter")));

        // for whatever reason forge registers the milk fluid under MC's namespace
        builder.addFixer(FluidRenameFix.create(v1, "Remove GT milk fluid",
                createRenamer(Map.of("gtceu:milk", "minecraft:milk", "gtceu:flowing_milk", "minecraft:flowing_milk"))));
        builder.addFixer(ItemRenameFix.create(v1, "Remove GT milk bucket",
                createRenamer("gtceu:milk_bucket", "minecraft:milk_bucket")));

        // separator

        Schema v2_0 = builder.addSchema(2, SAME_NAMESPACED);
        builder.addFixer(new LDLibPayloadWrapperRemovalFix(v2_0));
        builder.addFixer(new AutoOutputTraitFix(v2_0));

        // separator

        Schema v2_1 = builder.addSchema(2, 1, SAME_NAMESPACED);
        builder.addFixer(new UpwardsFacingPropertyFix(v2_1));

        // separator

        Schema v2_2 = builder.addSchema(2, 2, V2_2::new);
        builder.addFixer(new TraitHolderificationFix(v2_2));

        // separator

        Schema v2_3 = builder.addSchema(2, 3, V2_3::new);
        builder.addFixer(new AddNewChoices(v2_3, "Added Welder and Spooler", References.BLOCK_ENTITY));
        builder.addFixer(new ExtruderCastingMoldRemapFix(v2_3));
        // Migrate GT's Molten fluids, ignore addons to allow them to fail loudly
        builder.addFixer(BlockRenameFix.create(v2_3, "Remove molten fluid blocks",
                createRenamer(RemoveMoltenFluidsFix.REMOVED_BLOCKS)));
        builder.addFixer(ItemRenameFix.create(v2_3, "Remove molten fluid buckets",
                createRenamer(RemoveMoltenFluidsFix.REMOVED_ITEMS)));
        builder.addFixer(FluidRenameFix.create(v2_3, "Remove molten fluids",
                createRenamer(RemoveMoltenFluidsFix.REMOVED_FLUIDS)));
        // version that doesn't ignore addons:
        // createBlockItemFluidRenameFix(builder, v2_3, "Remove molten fluids",
        // createRenamer(Pattern.compile("(flowing_)?molten_"), "$1"));

        // separator

        Schema v3_0 = builder.addSchema(3, 0, V3::new);
        // createMaterialRenameFix(builder, v3_0, "Rename uranium to uranium_238",
        // createRenamer(Pattern.compile("gtceu:(.*)?uranium"), "gtceu:$1uranium_238"));
        // createMaterialRenameFix(builder, v3_0, "Rename uranium_235 to uranium",
        // createRenamer(Pattern.compile("gtceu:(.*)uranium_235"), "gtceu:$1uranium"));
        createMaterialRenameFix(builder, v3_0, "gtceu:granite_red", "gtceu:red_granite");
        createMaterialRenameFix(builder, v3_0, "gtceu:oil_heavy", "gtceu:heavy_oil");
        createMaterialRenameFix(builder, v3_0, "gtceu:oil_light", "gtceu:light_oil");
        createMaterialRenameFix(builder, v3_0, "gtceu:oil_medium", "gtceu:raw_oil");
        createMaterialRenameFix(builder, v3_0, "gtceu:palladium_raw", "gtceu:raw_palladium");

        createBlockItemRenameFix(builder, v3_0, "Fix Palladium Substation Casing name",
                createRenamer("gtceu:palladium_substation", "gtceu:palladium_substation_casing"));
        createBlockItemRenameFix(builder, v3_0, "Fix Assembly Control Casing name",
                createRenamer("gtceu:assembly_line_casing", "gtceu:assembly_control_casing"));
        createBlockItemRenameFix(builder, v3_0, "Fix Magnalium Turbine Casing name",
                createRenamer("gtceu:steel_turbine_casing", "gtceu:magnalium_turbine_casing"));
        createBlockItemRenameFix(builder, v3_0, "Fix Bricked Bronze Casing name",
                createRenamer("gtceu:bronze_brick_casing", "gtceu:bricked_bronze_casing"));
        createBlockItemRenameFix(builder, v3_0, "Fix Bricked Wrought Iron Casing name",
                createRenamer("gtceu:steel_brick_casing", "gtceu:bricked_wrought_iron_casing"));

        builder.addFixer(new ItemStackTurbineRotorFix(v3_0));

        // separator

        Schema v3_1 = builder.addSchema(3, 1, V3_1::new);
        createBlockItemEntityRenameFix(builder, v3_1, "Fix Rotary Hearth Furnace name",
                createRenamer("gtceu:mega_blast_furnace", "gtceu:rotary_hearth_furnace"));
        createBlockItemEntityRenameFix(builder, v3_1, "Fix Bulk Blast Chiller name",
                createRenamer("gtceu:mega_vacuum_freezer", "gtceu:bulk_blast_chiller"));

    }

    private static void createBlockItemRenameFix(FixedDataFixerBuilder builder, Schema schema, String name,
                                                 UnaryOperator<String> renamer) {
        builder.addFixer(ItemRenameFix.create(schema, name + " for item", renamer));
        // BlockRenameFix adds the " for block" and " for block_state" parts
        builder.addFixer(BlockRenameFix.create(schema, name, renamer));
    }

    private static void createBlockItemFluidRenameFix(FixedDataFixerBuilder builder, Schema schema, String name,
                                                      UnaryOperator<String> renamer) {
        createBlockItemRenameFix(builder, schema, name, renamer);
        builder.addFixer(FluidRenameFix.create(schema, name + " for fluid", renamer));
    }

    /**
     * create and add all required fixes to rename a material.
     * 
     * @param builder the DFU builder instance
     * @param schema  the current version's schema
     * @param oldName the material's old name INCLUDING the namespace
     * @param newName the material's new name INCLUDING the namespace
     */
    private static void createMaterialRenameFix(FixedDataFixerBuilder builder, Schema schema,
                                                String oldName, String newName) {
        final UnaryOperator<String> renamer = MaterialRenameHelper.createRenamer(oldName, newName);
        final String dataFixName = "Fix " + newName + " name";

        createBlockItemFluidRenameFix(builder, schema, dataFixName, renamer);
        builder.addFixer(MaterialRenameFix.create(schema, dataFixName + " for material", renamer));
    }

    private static void createBlockItemEntityRenameFix(FixedDataFixerBuilder builder, Schema schema, String name,
                                                       UnaryOperator<String> renamer) {
        createBlockItemRenameFix(builder, schema, name, renamer);
        builder.addFixer(BlockEntityRenameFix.create(schema, name + " for block_entity", renamer));
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

package com.gregtechceu.gtceu.integration.jsonthings.parsers;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.block.BlockProperties;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.gui.editor.EditableMachineUI;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.SimpleGeneratorMachine;
import com.gregtechceu.gtceu.api.machine.SimpleTieredMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.*;
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.client.renderer.GTRendererProvider;
import com.gregtechceu.gtceu.integration.jsonthings.JsonThingsCompat;
import com.gregtechceu.gtceu.integration.jsonthings.builders.MachineBuilder;
import com.gregtechceu.gtceu.utils.SupplierMemoizer;

import com.lowdragmc.lowdraglib.Platform;

import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.RegisterEvent;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import com.tterrag.registrate.util.OneTimeEventReceiver;
import dev.gigaherz.jsonthings.JsonThings;
import dev.gigaherz.jsonthings.things.builders.BaseBuilder;
import dev.gigaherz.jsonthings.things.parsers.ThingParseException;
import dev.gigaherz.jsonthings.things.parsers.ThingParser;
import dev.gigaherz.jsonthings.things.shapes.DynamicShape;
import dev.gigaherz.jsonthings.util.parse.JParse;
import dev.gigaherz.jsonthings.util.parse.value.Any;
import dev.gigaherz.jsonthings.util.parse.value.ArrayValue;
import dev.gigaherz.jsonthings.util.parse.value.ObjValue;
import dev.gigaherz.jsonthings.util.parse.value.StringValue;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class MachineParser extends ThingParser<MachineBuilder> {

    public MachineParser(IEventBus bus) {
        super(GSON, "gtceu/machine");
        bus.addGenericListener(MachineDefinition.class, this::registerMachines);
        bus.addListener((RegisterEvent e) -> registerBlocks(e, bus));
    }

    public void registerMachines(GTCEuAPI.RegisterEvent<ResourceLocation, MachineDefinition> event) {}

    public void registerBlocks(RegisterEvent event, IEventBus modBus) {
        event.register(Registries.BLOCK, helper -> {
            // unfreeze registry because gt registration event happens before JsonThings loads
            GTRegistries.MACHINES.unfreeze();
            LOGGER.info("Started registering Machine things, errors about unexpected registry domains are harmless...");

            processAndConsumeErrors(getThingType(), getBuilders(), (thing) -> {
                ResourceLocation location = thing.getRegistryName();
                LOGGER.info("loading machine {}", location);
                GTRegistries.MACHINES.register(thing.getRegistryName(), thing.get());
            }, BaseBuilder::getRegistryName);

            GTRegistries.MACHINES.freeze();
            LOGGER.info("Done processing thingpack Machines.");

            LOGGER.info("Started registering Block things, errors about unexpected registry domains are harmless...");
            processAndConsumeErrors(getThingType(), getBuilders(),
                    thing -> {
                        RotationState.set(thing.getRotationState());
                        MachineDefinition.setBuilt(thing.get());

                        helper.register(thing.getBlockBuilder().getRegistryName(),
                                thing.getBlockBuilder().get().self());

                        Arrays.stream(thing.getAbilities())
                                .forEach(a -> a.register(thing.getTier(), thing.getBlockBuilder().get().self()));
                        RotationState.clear();
                        MachineDefinition.clearBuilt();
                    },
                    BaseBuilder::getRegistryName);
            LOGGER.info("Done processing thingpack Blocks.");
        });
        event.register(Registries.BLOCK_ENTITY_TYPE, helper -> {
            processAndConsumeErrors(getThingType(), getBuilders(),
                    thing -> {
                        if (Platform.isClient() && thing.isHasTESR()) {
                            OneTimeEventReceiver.addListener(modBus, FMLClientSetupEvent.class, $ -> {
                                BlockEntityRenderers.register(thing.getBlockEntityTypeSupplier().get(),
                                        GTRendererProvider::getOrCreate);
                            });
                        }
                        helper.register(thing.getRegistryName(), thing.getBlockEntityTypeSupplier().get());
                    },
                    BaseBuilder::getRegistryName);
        });
    }

    @Override
    protected @NotNull MachineBuilder processThing(ResourceLocation key, JsonObject data,
                                                   Consumer<MachineBuilder> builderModification) {
        final MachineBuilder builder = MachineBuilder.begin(this, key);

        MutableObject<RotationState> rotationState = new MutableObject<>(RotationState.ALL);

        final Map<String, Property<?>> propertiesByName = new HashMap<>();
        propertiesByName.put("server_tick", BlockProperties.SERVER_TICK);

        MutableObject<String> uiTemplateType = new MutableObject<>(null);

        JParse.begin(data)
                .ifKey("parent", val -> val.string().map(ResourceLocation::new).handle(builder::setParent))
                .ifKey("type", val -> val.string().handle(builder::setType))
                .ifKey("block", val -> parseBlock(builder, val))
                .ifKey("allow_extended_facing", val -> val.bool().handle(value -> {
                    builder.setAllowExtendedFacing(value);
                    propertiesByName.put("upwards_facing", BlockProperties.UPWARDS_FACING_PROPERTY);
                }))
                .ifKey("allow_flip", val -> val.bool().handle(builder::setAllowFlip))
                .ifKey("rotation_state", val -> val.string().map(MachineParser::parseRotationState).handle(state -> {
                    rotationState.setValue(state);
                    propertiesByName.put("facing", state.property);
                }))
                .ifKey("shape",
                        val -> val.raw(obj -> builder.setShape(
                                DynamicShape.parseShape(obj, rotationState.getValue().property, propertiesByName))))
                .ifKey("has_tesr", val -> val.bool().handle(builder::setHasTESR))
                .ifKey("render_multiblock_world_preview",
                        val -> val.bool().handle(builder::setRenderMultiblockWorldPreview))
                .ifKey("render_multiblock_xei_preview",
                        val -> val.bool().handle(builder::setRenderMultiblockXEIPreview))
                .ifKey("ui_template_type", val -> val.string().handle(uiTemplateType::setValue))
                // DOCS: remember to put the recipe type with the most slots first so the UI gets enough slots for all
                // of them
                .ifKey("recipe_types", val -> val.array()
                        .strings()
                        .flatten(StringValue::getAsString, String[]::new)
                        .map(strings -> Arrays.stream(strings)
                                .map(name -> GTRegistries.RECIPE_TYPES.get(GTCEu.appendId(name)))
                                .toArray(GTRecipeType[]::new))
                        .handle(types -> {
                            if (uiTemplateType.getValue() != null) {
                                EditableMachineUI ui = parseEditableUI(uiTemplateType.getValue(), key, types[0]);
                                builder.setEditableUI(ui);
                            }
                            builder.setRecipeTypes(types);
                        }))
                .ifKey("tier", val -> val.intValue().handle(builder::setTier))
                .ifKey("recipe_output_limits", val -> val.obj().raw(json -> {
                    Object2IntMap<RecipeCapability<?>> limits = new Object2IntOpenHashMap<>();
                    for (String type : json.keySet()) {
                        RecipeCapability<?> cap = GTRegistries.RECIPE_CAPABILITIES.get(type);
                        if (cap == null) {
                            throw new ThingParseException("Recipe capability with name " + type + " does not exist.");
                        }
                        limits.put(cap, GsonHelper.getAsInt(json, type));
                        builder.setRecipeOutputLimits(limits);
                    }
                }))
                .ifKey("default_painting_color", val -> val
                        .ifInteger(intValue -> intValue.handle(builder::setPaintingColor))
                        .ifString(stringValue -> stringValue
                                .handle(str -> builder.setPaintingColor(Long.decode(str).intValue()))))
                .ifKey("part_abilities", val -> val.array()
                        .strings()
                        .flatten(StringValue::getAsString, String[]::new)
                        .map(strings -> Arrays.stream(strings).map(MachineParser::parseAbility)
                                .toArray(PartAbility[]::new))
                        .handle(builder::setAbilities))
                .ifKey("tooltips",
                        val -> val.array().unwrapRaw(MachineParser::parseTooltips).handle(builder::setTooltips))
                // .ifKey("recipe_modifier", val -> )
                // .ifKey("always_try_modify_recipe", val -> val.bool().handle(builder::setAlwaysTryModifyRecipe))
                // .ifKey("appearance", val -> )
                .ifKey("is_generator", val -> val.bool().handle(builder::setGenerator))
                .ifKey("pattern", val -> val.obj().map(MachineParser::parsePattern).handle(builder::setPattern))
                .ifKey("shape_infos", val -> val.map(MachineParser::parseShapeInfos).handle(builder::setShapeInfo))
                .ifKey("recovery_items", val -> val.array().mapWhole(MachineParser::parseRecoveryItems)
                        .handle(builder::setRecoveryItems))
                // .ifKey("part_sorter", val -> )
                .ifKey("events", val -> val.obj().map(this::parseEvents).handle(builder::setEventMap));

        builderModification.accept(builder);

        builder.setFactory(builder.getType().getFactory(data));

        return builder;
    }

    private static RotationState parseRotationState(String value) {
        for (RotationState state : RotationState.values()) {
            if (state.name().equalsIgnoreCase(value)) {
                return state;
            }
        }
        throw new ThingParseException("Rotation state with name " + value +
                " does not exist. Valid values are: ALL, NONE, Y_AXIS, NON_Y_AXIS");
    }

    private static List<Component> parseTooltips(JsonArray lines) {
        var lore = new ArrayList<Component>();
        for (JsonElement e : lines) {
            lore.add(Component.Serializer.fromJson(e));
        }
        return lore;
    }

    private static EditableMachineUI parseEditableUI(String type, ResourceLocation id, GTRecipeType recipeType) {
        if (type.equalsIgnoreCase("simple")) {
            return SimpleTieredMachine.EDITABLE_UI_CREATOR.apply(id, recipeType);
        } else if (type.equalsIgnoreCase("generator")) {
            return SimpleGeneratorMachine.EDITABLE_UI_CREATOR.apply(id, recipeType);
        }
        return null;
    }

    private static PartAbility parseAbility(String type) {
        if (!PartAbility.PART_ABILITIES.containsKey(type)) {
            throw new ThingParseException("Part ability with name " + type + "does not exist.");
        }
        return PartAbility.PART_ABILITIES.get(type);
    }

    private static Supplier<BlockPattern> parsePattern(ObjValue objValue) {
        return () -> {
            MutableObject<RelativeDirection[]> directions = new MutableObject<>(new RelativeDirection[] {
                    RelativeDirection.LEFT,
                    RelativeDirection.UP,
                    RelativeDirection.FRONT
            });
            objValue.ifKey("directions", val -> val.array()
                    .strings()
                    .flatten(StringValue::getAsString, String[]::new)
                    .map(strings -> Arrays.stream(strings)
                            .map(MachineParser::parseRelativeDirection)
                            .toArray(RelativeDirection[]::new))
                    .handle(directions::setValue));
            RelativeDirection[] parsed = directions.getValue();
            FactoryBlockPattern patternFactory = FactoryBlockPattern.start(parsed[0], parsed[1], parsed[2]);

            objValue.key("aisles", val -> val.array().raw(array -> {
                for (JsonElement e : array.asList()) {
                    String[] aisle;
                    int minRepeat = 1;
                    int maxRepeat = 1;
                    if (e.isJsonArray()) {
                        aisle = e.getAsJsonArray().asList()
                                .stream()
                                .map(JsonElement::getAsString)
                                .toArray(String[]::new);
                    } else if (e.isJsonObject()) {
                        JsonObject obj = e.getAsJsonObject();
                        aisle = obj.getAsJsonArray("aisle").asList()
                                .stream()
                                .map(JsonElement::getAsString)
                                .toArray(String[]::new);
                        minRepeat = GsonHelper.getAsInt(obj, "min_repeat");
                        // default to minRepeat repetitions if max_repeat doesn't exist.
                        maxRepeat = GsonHelper.getAsInt(obj, "max_repeat", minRepeat);
                    } else {
                        throw new ThingParseException("Invalid object type as aisle! wanted array or object, got " + e);
                    }
                    patternFactory.aisleRepeatable(minRepeat, maxRepeat, aisle);
                }
            }))
                    .key("symbol_map", val -> val.obj().forEach((key, value) -> {
                        if (key.length() != 1) {
                            throw new ThingParseException("Pattern keys need to be 1 (one) character long, got " +
                                    key.length() + " characters");
                        }
                        patternFactory.where(key.charAt(0), parseTraceabilityPredicate(value));
                    }));

            return patternFactory.build();
        };
    }

    private static RelativeDirection parseRelativeDirection(String value) {
        for (RelativeDirection direction : RelativeDirection.values()) {
            if (direction.name().equalsIgnoreCase(value)) {
                return direction;
            }
        }
        throw new ThingParseException("Relative direction with name " + value + " does not exist." +
                "Valid values are: UP, DOWN, LEFT, RIGHT, FRONT, BACK");
    }

    private static TraceabilityPredicate parseTraceabilityPredicate(Any value) {
        MutableObject<TraceabilityPredicate> object = new MutableObject<>(new TraceabilityPredicate());
        value.ifString(val -> {
            String string = val.getAsString();
            if (string.equalsIgnoreCase("heating_coils")) {
                object.setValue(Predicates.heatingCoils());
            } else if (string.equalsIgnoreCase("cleanroom_filters")) {
                object.setValue(Predicates.cleanroomFilters());
            } else if (string.equalsIgnoreCase("pss_batteries")) {
                object.setValue(Predicates.powerSubstationBatteries());
            } else {
                object.setValue(Predicates.blocks(BuiltInRegistries.BLOCK.get(new ResourceLocation(string))));
            }
        }).ifArray(val -> {
            val.strings()
                    .flatten(StringValue::getAsString, String[]::new)
                    .map(strings -> Arrays.stream(strings)
                            .map(string -> BuiltInRegistries.BLOCK.get(new ResourceLocation(string))))
                    .handle(blocks -> {
                        object.setValue(Predicates.blocks(blocks.toArray(Block[]::new)));
                    });
        })
                .ifObj(objVal -> {
                    objVal.ifKey("blocks", val -> val.array()
                            .strings()
                            .flatten(StringValue::getAsString, String[]::new)
                            .map(strings -> Arrays.stream(strings)
                                    .map(string -> BuiltInRegistries.BLOCK.get(new ResourceLocation(string))))
                            .handle(blocks -> {
                                object.setValue(object.getValue().or(Predicates.blocks(blocks.toArray(Block[]::new))));
                            }))
                            .ifKey("block_tag", val -> val.string()
                                    .map(string -> TagKey.create(Registries.BLOCK, new ResourceLocation(string)))
                                    .handle(tags -> object.setValue(object.getValue().or(Predicates.blockTag(tags)))))
                            .ifKey("fluids", val -> val.array()
                                    .strings()
                                    .flatten(StringValue::getAsString, String[]::new)
                                    .map(strings -> Arrays.stream(strings)
                                            .map(string -> BuiltInRegistries.FLUID.get(new ResourceLocation(string))))
                                    .handle(fluids -> {
                                        object.setValue(
                                                object.getValue().or(Predicates.fluids(fluids.toArray(Fluid[]::new))));
                                    }))
                            .ifKey("fluid_tag", val -> val.string()
                                    .map(string -> TagKey.create(Registries.FLUID, new ResourceLocation(string)))
                                    .handle(tags -> object.setValue(object.getValue().or(Predicates.fluidTag(tags)))))
                            .ifKey("state", val -> val.array().raw(json -> {
                                List<BlockState> states = new ArrayList<>();
                                for (var e : json.asList()) {
                                    states.add(BlockState.CODEC.parse(JsonOps.INSTANCE, e)
                                            .getOrThrow(false, GTCEu.LOGGER::error));
                                }
                                object.setValue(
                                        object.getValue().or(Predicates.states(states.toArray(BlockState[]::new))));
                            }))
                            .ifKey("any", val -> object.setValue(object.getValue().or(Predicates.any())))
                            .ifKey("air", val -> object.setValue(object.getValue().or(Predicates.air())))
                            .ifKey("controller", val -> object.setValue(Predicates.controller(object.getValue())))
                            .ifKey("abilities", val -> val.array()
                                    .strings()
                                    .flatten(StringValue::getAsString, String[]::new)
                                    .handle(strings -> {
                                        List<PartAbility> abilities = new ArrayList<>();
                                        for (String str : strings) {
                                            abilities.add(MachineParser.parseAbility(str));
                                        }
                                        object.setValue(object.getValue()
                                                .or(Predicates.abilities(abilities.toArray(PartAbility[]::new))));
                                    }))

                            .ifKey("or",
                                    orVal -> object.setValue(object.getValue().or(parseTraceabilityPredicate(orVal))))
                            .ifKey("tooltips", val -> val.array()
                                    .unwrapRaw(MachineParser::parseTooltips)
                                    .handle(tooltips -> object.getValue()
                                            .addTooltips(tooltips.toArray(Component[]::new))))
                            .ifKey("global_min_count",
                                    val -> val.intValue().handle(count -> object.getValue().setMinGlobalLimited(count)))
                            .ifKey("global_max_count",
                                    val -> val.intValue().handle(count -> object.getValue().setMaxGlobalLimited(count)))
                            .ifKey("layer_min_count",
                                    val -> val.intValue().handle(count -> object.getValue().setMinLayerLimited(count)))
                            .ifKey("layer_max_count",
                                    val -> val.intValue().handle(count -> object.getValue().setMaxLayerLimited(count)))
                            .ifKey("preview_count",
                                    val -> val.intValue().handle(count -> object.getValue().setPreviewCount(count)))
                            .ifKey("xei_render_disabled", val -> val.bool().handle(disabled -> {
                                if (disabled) {
                                    object.getValue().disableRenderFormed();
                                }
                            }))
                            .ifKey("io",
                                    val -> val.string().map(MachineParser::parseIo)
                                            .handle(io -> object.getValue().setIO(io)));
                });
        return object.getValue();
    }

    @Nullable
    private static IO parseIo(String value) {
        for (IO io : IO.values()) {
            if (io.name().equalsIgnoreCase(value)) {
                return io;
            }
        }
        return null;
    }

    private static Supplier<List<MultiblockShapeInfo>> parseShapeInfos(Any value) {
        MutableObject<Supplier<List<MultiblockShapeInfo>>> shapeInfos = new MutableObject<>();

        value.ifArray(val -> shapeInfos.setValue(() -> {
            List<MultiblockShapeInfo> list = new ArrayList<>();
            var elements = val.getAsJsonArray().asList();
            for (int i = 0; i < elements.size(); i++) {
                list.add(parseShapeInfo(GsonHelper.convertToJsonObject(elements.get(i), "element " + i)));
            }
            return list;
        }))
                .ifObj(val -> shapeInfos.setValue(() -> List.of(parseShapeInfo(val.getAsJsonObject()))));

        return shapeInfos.getValue();
    }

    public static MultiblockShapeInfo parseShapeInfo(JsonObject infoElement) {
        MultiblockShapeInfo.ShapeInfoBuilder builder = MultiblockShapeInfo.builder();

        JsonArray aisles = GsonHelper.getAsJsonArray(infoElement, "aisles");
        for (JsonElement aisleElement : aisles.asList()) {
            builder.aisle(aisleElement.getAsJsonArray().asList()
                    .stream()
                    .map(JsonElement::getAsString)
                    .toArray(String[]::new));
        }
        JsonObject symbolMap = GsonHelper.getAsJsonObject(infoElement, "symbol_map");
        for (String key : symbolMap.keySet()) {
            if (key.length() != 1) {
                throw new ThingParseException(
                        "Shape info keys need to be 1 (one) character long, got " + key.length() + " characters");
            }

            JsonElement symbol = symbolMap.get(key);
            if (symbol.isJsonPrimitive() && symbol.getAsJsonPrimitive().isString()) {
                String symbolString = symbol.getAsString();
                builder.where(key.charAt(0), BuiltInRegistries.BLOCK.get(new ResourceLocation(symbolString)));
            } else {
                BlockState state = BlockState.CODEC.parse(JsonOps.INSTANCE, symbol)
                        .getOrThrow(false, GTCEu.LOGGER::error);
                builder.where(key.charAt(0), state);
            }
        }

        return builder.build();
    }

    private static Supplier<ItemStack[]> parseRecoveryItems(ArrayValue value) {
        return SupplierMemoizer.memoize(() -> {
            List<ItemStack> stacks = new ArrayList<>();
            value.forEach((i, val) -> {
                stacks.add(ItemStack.CODEC.parse(JsonOps.INSTANCE, val.get())
                        .getOrThrow(false, GTCEu.LOGGER::error));
            });
            return stacks.toArray(ItemStack[]::new);
        });
    }

    public static void parseBlock(MachineBuilder builder, Any val) {
        val
                .ifBool(v -> v.handle(b -> {
                    if (b) createBlock(builder, new JsonObject());
                }))
                .ifObj(obj -> obj.raw((JsonObject block) -> createBlock(builder, block)))
                .typeError();
    }

    private static void createBlock(MachineBuilder builder, JsonObject obj) {
        try {
            var blockBuilder = JsonThings.blockParser.processThing(builder.getRegistryName(), obj, b -> {
                if (!b.hasBlockType())
                    b.setBlockType(JsonThingsCompat.META_MACHINE);
            });
            if (blockBuilder != null)
                builder.setBlockBuilder(blockBuilder);
        } catch (Exception e) {
            throw new ThingParseException("Exception while parsing nested block in " + builder.getRegistryName(), e);
        }
    }
}

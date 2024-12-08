package com.gregtechceu.gtceu.integration.kjs.builders.machine;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.block.IMachineBlock;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.gui.editor.EditableMachineUI;
import com.gregtechceu.gtceu.api.item.MetaMachineItem;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.SimpleTieredMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;
import com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.registry.GTRegistration;

import com.lowdragmc.lowdraglib.client.renderer.IRenderer;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.builders.ItemBuilder;
import com.tterrag.registrate.util.nullness.NonNullConsumer;
import com.tterrag.registrate.util.nullness.NonNullUnaryOperator;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import org.apache.commons.lang3.function.TriFunction;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.function.*;
import java.util.stream.Stream;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
@SuppressWarnings({ "unused" })
public class MachineFunctionPresets {

    public static Integer[] mapTierArray(Object[] tiers) {
        return Arrays.stream(tiers)
                .flatMap(
                        object -> object.getClass().isArray() ?
                                object.getClass().componentType().isPrimitive() ?
                                        Arrays.stream((int[]) object).boxed() : Arrays.stream((Object[]) object) :
                                Stream.of(object))
                .filter(Number.class::isInstance)
                .map(Number.class::cast)
                .map(Number::intValue)
                .toArray(Integer[]::new);
    }

    public static Object[] copyArgs(Object[] original, int offset) {
        Object[] copy = new Object[original.length - offset];
        System.arraycopy(original, offset, copy, 0, original.length - offset);
        return copy;
    }

    public static <D extends MachineDefinition,
            B extends MachineBuilder<D>> MachineBuilder<D> builder(String name, B[] builders, Class<B> builderClass,
                                                                   Function<ResourceLocation, D> definitionFactory,
                                                                   BiFunction<BlockBehaviour.Properties, D, IMachineBlock> blockFactory,
                                                                   TriFunction<BlockEntityType<?>, BlockPos, BlockState, IMachineBlockEntity> blockEntityFactory) {
        return new MachineBuilder<D>(GTRegistration.REGISTRATE, name, definitionFactory, holder -> null, blockFactory,
                MetaMachineItem::new, blockEntityFactory) {

            public MachineBuilder<D> renderer(@Nullable Supplier<IRenderer> renderer) {
                for (var builder : builders) {
                    if (builder == null) continue;
                    builder.renderer(renderer);
                }
                return this;
            }

            @Override
            public MachineBuilder<D> shape(VoxelShape shape) {
                for (var builder : builders) {
                    if (builder == null) continue;
                    builder.shape(shape);
                }
                return this;
            }

            @Override
            public MachineBuilder<D> rotationState(RotationState rotationState) {
                for (var builder : builders) {
                    if (builder == null) continue;
                    builder.rotationState(rotationState);
                }
                return this;
            }

            @Override
            public MachineBuilder<D> hasTESR(boolean hasTESR) {
                for (var builder : builders) {
                    if (builder == null) continue;
                    builder.hasTESR(hasTESR);
                }
                return this;
            }

            @Override
            public MachineBuilder<D> blockProp(NonNullUnaryOperator<BlockBehaviour.Properties> blockProp) {
                for (var builder : builders) {
                    if (builder == null) continue;
                    builder.blockProp(blockProp);
                }
                return this;
            }

            @Override
            public MachineBuilder<D> itemProp(NonNullUnaryOperator<Item.Properties> itemProp) {
                for (var builder : builders) {
                    if (builder == null) continue;
                    builder.itemProp(itemProp);
                }
                return this;
            }

            @Override
            public MachineBuilder<D> blockBuilder(Consumer<BlockBuilder<? extends Block, ?>> blockBuilder) {
                for (var builder : builders) {
                    if (builder == null) continue;
                    builder.blockBuilder(blockBuilder);
                }
                return this;
            }

            @Override
            public MachineBuilder<D> itemBuilder(Consumer<ItemBuilder<? extends MetaMachineItem, ?>> itemBuilder) {
                for (var builder : builders) {
                    if (builder == null) continue;
                    builder.itemBuilder(itemBuilder);
                }
                return this;
            }

            @Override
            public MachineBuilder<D> onBlockEntityRegister(NonNullConsumer<BlockEntityType<BlockEntity>> onBlockEntityRegister) {
                for (var builder : builders) {
                    if (builder == null) continue;
                    builder.onBlockEntityRegister(onBlockEntityRegister);
                }
                return this;
            }

            @Override
            public MachineBuilder<D> recipeType(GTRecipeType type) {
                for (var builder : builders) {
                    if (builder == null) continue;
                    builder.recipeType(type);
                }
                return this;
            }

            @Override
            public MachineBuilder<D> recipeTypes(GTRecipeType... recipeTypes) {
                for (var builder : builders) {
                    if (builder == null) continue;
                    builder.recipeTypes(recipeTypes);
                }
                return this;
            }

            @Override
            public MachineBuilder<D> tier(int tier) {
                return this;
            }

            public MachineBuilder<D> recipeOutputLimits(Object2IntMap<RecipeCapability<?>> map) {
                for (var builder : builders) {
                    if (builder == null) continue;
                    builder.recipeOutputLimits(map);
                }
                return this;
            }

            @Override
            public MachineBuilder<D> addOutputLimit(RecipeCapability<?> capability, int limit) {
                for (var builder : builders) {
                    if (builder == null) continue;
                    builder.addOutputLimit(capability, limit);
                }
                return this;
            }

            @Override
            public MachineBuilder<D> paintingColor(int paintingColor) {
                for (var builder : builders) {
                    if (builder == null) continue;
                    builder.paintingColor(paintingColor);
                }
                return this;
            }

            @Override
            public MachineBuilder<D> itemColor(BiFunction<ItemStack, Integer, Integer> itemColor) {
                for (var builder : builders) {
                    if (builder == null) continue;
                    builder.itemColor(itemColor);
                }
                return this;
            }

            @Override
            public MachineBuilder<D> abilities(PartAbility... abilities) {
                for (var builder : builders) {
                    if (builder == null) continue;
                    builder.abilities(abilities);
                }
                return this;
            }

            public MachineBuilder<D> noRecipeModifier() {
                for (var builder : builders) {
                    if (builder == null) continue;
                    builder.noRecipeModifier();
                }
                return this;
            }

            public MachineBuilder<D> tooltips(Component... tooltips) {
                for (var builder : builders) {
                    if (builder == null) continue;
                    builder.tooltips(tooltips);
                }
                return this;
            }

            @Override
            public MachineBuilder<D> tooltipBuilder(BiConsumer<ItemStack, List<Component>> tooltipBuilder) {
                for (var builder : builders) {
                    if (builder == null) continue;
                    builder.tooltipBuilder(tooltipBuilder);
                }
                return this;
            }

            @Override
            public MachineBuilder<D> recipeModifier(RecipeModifier recipeModifier) {
                for (var builder : builders) {
                    if (builder == null) continue;
                    builder.recipeModifier(recipeModifier);
                }
                return this;
            }

            @Override
            public MachineBuilder<D> recipeModifiers(RecipeModifier... recipeModifiers) {
                for (var builder : builders) {
                    if (builder == null) continue;
                    builder.recipeModifiers(recipeModifiers);
                }
                return this;
            }

            @Override
            public MachineBuilder<D> alwaysTryModifyRecipe(boolean alwaysTryModifyRecipe) {
                for (var builder : builders) {
                    if (builder == null) continue;
                    builder.alwaysTryModifyRecipe(alwaysTryModifyRecipe);
                }
                return this;
            }

            @Override
            public MachineBuilder<D> appearance(Supplier<BlockState> appearance) {
                for (var builder : builders) {
                    if (builder == null) continue;
                    builder.appearance(appearance);
                }
                return this;
            }

            @Override
            public MachineBuilder<D> editableUI(@Nullable EditableMachineUI editableUI) {
                for (var builder : builders) {
                    if (builder == null) continue;
                    builder.editableUI(editableUI);
                }
                return this;
            }

            @Override
            public MachineBuilder<D> langValue(String langValue) {
                for (var builder : builders) {
                    if (builder == null) continue;
                    builder.langValue(langValue);
                }
                return this;
            }

            @Override
            public MachineBuilder<D> modelRenderer(Supplier<ResourceLocation> model) {
                for (var builder : builders) {
                    if (builder == null) continue;
                    builder.modelRenderer(model);
                }
                return this;
            }

            @Override
            public MachineBuilder<D> defaultModelRenderer() {
                return modelRenderer(() -> new ResourceLocation(registrate.getModid(), "block/" + name));
            }

            @Override
            public MachineBuilder<D> tieredHullRenderer(ResourceLocation model) {
                for (var builder : builders) {
                    if (builder == null) continue;
                    builder.tieredHullRenderer(model);
                }
                return this;
            }

            @Override
            public MachineBuilder<D> overlayTieredHullRenderer(String name) {
                for (var builder : builders) {
                    if (builder == null) continue;
                    builder.overlayTieredHullRenderer(name);
                }
                return this;
            }

            @Override
            public MachineBuilder<D> overlaySteamHullRenderer(String name) {
                for (var builder : builders) {
                    if (builder == null) continue;
                    builder.overlaySteamHullRenderer(name);
                }
                return this;
            }

            @Override
            public MachineBuilder<D> workableTieredHullRenderer(ResourceLocation workableModel) {
                for (var builder : builders) {
                    if (builder == null) continue;
                    builder.workableTieredHullRenderer(workableModel);
                }
                return this;
            }

            @Override
            public MachineBuilder<D> workableSteamHullRenderer(boolean isHighPressure, ResourceLocation workableModel) {
                for (var builder : builders) {
                    if (builder == null) continue;
                    builder.workableSteamHullRenderer(isHighPressure, workableModel);
                }
                return this;
            }

            @Override
            public MachineBuilder<D> workableCasingRenderer(ResourceLocation baseCasing,
                                                            ResourceLocation workableModel) {
                for (var builder : builders) {
                    if (builder == null) continue;
                    builder.workableCasingRenderer(baseCasing, workableModel);
                }
                return this;
            }

            @Override
            public MachineBuilder<D> workableCasingRenderer(ResourceLocation baseCasing, ResourceLocation workableModel,
                                                            boolean tint) {
                for (var builder : builders) {
                    if (builder == null) continue;
                    builder.workableCasingRenderer(baseCasing, workableModel, tint);
                }
                return this;
            }

            @Override
            public MachineBuilder<D> sidedWorkableCasingRenderer(String basePath, ResourceLocation overlayModel,
                                                                 boolean tint) {
                for (var builder : builders) {
                    if (builder == null) continue;
                    builder.sidedWorkableCasingRenderer(basePath, overlayModel, tint);
                }
                return this;
            }

            @Override
            public MachineBuilder<D> sidedWorkableCasingRenderer(String basePath, ResourceLocation overlayModel) {
                for (var builder : builders) {
                    if (builder == null) continue;
                    builder.sidedWorkableCasingRenderer(basePath, overlayModel);
                }
                return this;
            }

            public MachineBuilder<D> tier(int tier, BuilderConsumer<D> consumer) {
                for (var builder : builders) {
                    if (builder == null) continue;
                    if (builder.tier() == tier) {
                        consumer.accept(builder);
                    }
                }
                return this;
            }

            public MachineBuilder<D> allTiers(TieredBuilderConsumer<D> consumer) {
                for (var builder : builders) {
                    if (builder == null) continue;
                    consumer.accept(builder.tier(), builder);
                }
                return this;
            }

            public MachineBuilder<D> recipeModifier(RecipeModifier recipeModifier, boolean alwaysTryModifyRecipe) {
                recipeModifier(recipeModifier);
                alwaysTryModifyRecipe(alwaysTryModifyRecipe);
                return this;
            }

            // reflect the tankScalingFunction method because I'm a little bitch teehee (and because it's not a common
            // method, but in both SimpleMachineBuilder and KineticMachineBuilder, which can't inherit from each other)
            // does nothing if not found, or errors otherwise
            public MachineBuilder<D> tankScalingFunction(Function<Object, Double> tankScalingFunction) {
                try {
                    Method method = builderClass.getDeclaredMethod("tankScalingFunction", Function.class);

                    for (var builder : builders) {
                        if (builder == null) continue;
                        method.invoke(builder, tankScalingFunction);
                    }
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {

                }

                return this;
            }

            @SuppressWarnings("unchecked")
            @Nullable
            public Function<Integer, Integer> getTankScalingFunction(B builder) {
                try {
                    Field field = builderClass.getField("tankScalingFunction");

                    return (Function<Integer, Integer>) field.get(builder);
                } catch (NoSuchFieldException | IllegalAccessException exception) {
                    return null;
                }
            }

            public MachineBuilder<D> workableTooltip(GTRecipeType recipeType) {
                for (var builder : builders) {
                    if (builder == null) continue;
                    int tier = builder.tier();
                    Function<Integer, Integer> tankScalingFunction = getTankScalingFunction(builder);
                    builder.tooltips(GTMachines.workableTiered(
                            tier, GTValues.V[tier], GTValues.V[tier] * 64, recipeType, tankScalingFunction != null ?
                                    tankScalingFunction.apply(tier) : GTMachines.defaultTankSizeFunction.apply(tier),
                            true));
                }
                return this;
            }

            public MachineBuilder<D> recipeType(GTRecipeType recipeType, boolean applyWorkableTooltip) {
                this.recipeType(recipeType);
                if (applyWorkableTooltip) {
                    workableTooltip(recipeType);
                }
                return this;
            }

            public MachineBuilder<D> recipeType(GTRecipeType recipeType, boolean applyWorkableTooltip,
                                                boolean applyDefaultGUIFunction) {
                this.recipeType(recipeType);
                if (applyWorkableTooltip) {
                    workableTooltip(recipeType);
                }
                if (applyDefaultGUIFunction) {
                    editableUI(SimpleTieredMachine.EDITABLE_UI_CREATOR.apply(this.id, recipeType));
                }
                return this;
            }

            public MachineBuilder<D> isSource(boolean isSource) {
                if (KineticMachineBuilder.class.isAssignableFrom(builderClass)) {
                    for (var builder : builders) {
                        if (builder == null) continue;
                        ((KineticMachineBuilder) builder).isSource(isSource);
                    }
                }
                return this;
            }

            @Override
            public D register() {
                for (var builder : builders) {
                    if (builder == null) continue;
                    value = builder.register();
                }
                return value;
            }
        };
    }

    @FunctionalInterface
    public interface BuilderConsumer<D extends MachineDefinition> extends Consumer<MachineBuilder<D>> {

        void accept(MachineBuilder<D> builder);
    }

    @FunctionalInterface
    public interface TieredBuilderConsumer<D extends MachineDefinition> {

        void accept(int tier, MachineBuilder<D> builder);
    }
}

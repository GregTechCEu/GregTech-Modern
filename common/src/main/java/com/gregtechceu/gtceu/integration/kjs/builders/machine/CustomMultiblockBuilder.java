package com.gregtechceu.gtceu.integration.kjs.builders.machine;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.gui.editor.EditableMachineUI;
import com.gregtechceu.gtceu.api.item.MetaMachineItem;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.TieredWorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.pattern.MultiblockShapeInfo;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder;
import com.gregtechceu.gtceu.api.registry.registrate.MultiblockMachineBuilder;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.machine.multiblock.primitive.PrimitiveFancyUIWorkableMachine;
import com.lowdragmc.lowdraglib.client.renderer.IRenderer;
import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.builders.ItemBuilder;
import com.tterrag.registrate.util.nullness.NonNullConsumer;
import com.tterrag.registrate.util.nullness.NonNullUnaryOperator;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.commons.lang3.function.TriFunction;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.*;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
@SuppressWarnings({"unused"})
public class CustomMultiblockBuilder extends MultiblockMachineBuilder {
    protected CustomMultiblockBuilder(String name, Function<IMachineBlockEntity, ? extends MultiblockControllerMachine> metaMachine) {
        super(GTRegistries.REGISTRATE, name, metaMachine, MetaMachineBlock::new, MetaMachineItem::new, MetaMachineBlockEntity::createBlockEntity);
    }

    public static CustomMultiblockBuilder[] tieredMultis(String name,
                                                         BiFunction<IMachineBlockEntity, Integer, MultiblockControllerMachine> factory,
                                                         Integer... tiers) {
        CustomMultiblockBuilder[] builders = new CustomMultiblockBuilder[GTValues.TIER_COUNT];
        for (int tier : tiers) {
            var builder = new CustomMultiblockBuilder(GTValues.VN[tier].toLowerCase(Locale.ROOT) + "_" + name, holder -> factory.apply(holder, tier))
                    .tier(tier);
            builders[tier] = builder;
        }
        return builders;
    }

    @Override
    public CustomMultiblockBuilder tier(int tier) {
        return (CustomMultiblockBuilder) super.tier(tier);
    }


    @SuppressWarnings("unchecked")
    public static MachineBuilder<MultiblockMachineDefinition> createMultiblock(String name, Object... args) {
        CustomMultiblockBuilder[] builders;
        int start = 0;
        while (start < args.length && (!(args[start] instanceof Number) || !(args[start] instanceof Number[]) || !(args[start] instanceof int[]))) {
            ++start;
        }
        Object[] tierObjects = MachineFunctionPresets.copyArgs(args, start);
        Integer[] tiers = MachineFunctionPresets.mapTierArray(tierObjects);
        if (tiers.length > 0) {
            if (args.length > 0 && args[0] instanceof BiFunction<?,?,?> machineFunction) {
                builders = tieredMultis(name, (BiFunction<IMachineBlockEntity, Integer, MultiblockControllerMachine>) machineFunction, tiers);
            } else {
                builders = tieredMultis(name, TieredWorkableElectricMultiblockMachine::new, tiers);
            }
        } else {
            if (args.length > 0 && args[0] instanceof Function<?,?> machineFunction) {
                return new CustomMultiblockBuilder(name, (Function<IMachineBlockEntity, MultiblockControllerMachine>)machineFunction);
            } else {
                return new CustomMultiblockBuilder(name, WorkableElectricMultiblockMachine::new);
            }
        }
        return tieredBuilder(name, builders);
    }

    public static MachineBuilder<MultiblockMachineDefinition> createPrimitiveMultiblock(String name, Object... args) {
        return new CustomMultiblockBuilder(name, (holder) -> new PrimitiveFancyUIWorkableMachine(holder, args));
    }

    public static CustomMultiblockBuilder tieredBuilder(String name, CustomMultiblockBuilder[] builders) {
        return new CustomMultiblockBuilder(name, holder -> null) {
            @Override
            public MultiblockMachineBuilder pattern(Function<MultiblockMachineDefinition, BlockPattern> pattern) {
                for (var builder : builders) {
                    if (builder == null) continue;
                    builder.pattern(pattern);
                }
                return this;
            }

            @Override
            public MultiblockMachineBuilder partSorter(Comparator<IMultiPart> partSorter) {
                for (var builder : builders) {
                    if (builder == null) continue;
                    builder.partSorter(partSorter);
                }
                return this;
            }

            @Override
            public MultiblockMachineBuilder partAppearance(TriFunction<IMultiController, IMultiPart, Direction, BlockState> partAppearance) {
                for (var builder : builders) {
                    if (builder == null) continue;
                    builder.partAppearance(partAppearance);
                }
                return this;
            }

            @Override
            public MultiblockMachineBuilder additionalDisplay(BiConsumer<IMultiController, List<Component>> additionalDisplay) {
                for (var builder : builders) {
                    if (builder == null) continue;
                    builder.additionalDisplay(additionalDisplay);
                }
                return this;
            }

            public MultiblockMachineBuilder shapeInfo(Function<MultiblockMachineDefinition, MultiblockShapeInfo> shape) {
                for (var builder : builders) {
                    if (builder == null) continue;
                    builder.shapeInfo(shape);
                }
                return this;
            }

            public MultiblockMachineBuilder shapeInfos(Function<MultiblockMachineDefinition, List<MultiblockShapeInfo>> shapes) {
                for (var builder : builders) {
                    if (builder == null) continue;
                    builder.shapeInfos(shapes);
                }
                return this;
            }

            public MultiblockMachineBuilder recoveryItems(Supplier<ItemLike[]> items) {
                for (var builder : builders) {
                    if (builder == null) continue;
                    builder.recoveryItems(items);
                }
                return this;
            }

            public MultiblockMachineBuilder recoveryStacks(Supplier<ItemStack[]> stacks) {
                for (var builder : builders) {
                    if (builder == null) continue;
                    builder.recoveryStacks(stacks);
                }
                return this;
            }

            public CustomMultiblockBuilder renderer(@Nullable Supplier<IRenderer> renderer) {
                for (var builder : builders) {
                    if (builder == null) continue;
                    builder.renderer(renderer);
                }
                return this;
            }

            @Override
            public CustomMultiblockBuilder shape(VoxelShape shape) {
                for (var builder : builders) {
                    if (builder == null) continue;
                    builder.shape(shape);
                }
                return this;
            }

            @Override
            public CustomMultiblockBuilder rotationState(RotationState rotationState) {
                for (var builder : builders) {
                    if (builder == null) continue;
                    builder.rotationState(rotationState);
                }
                return this;
            }

            @Override
            public CustomMultiblockBuilder hasTESR(boolean hasTESR) {
                for (var builder : builders) {
                    if (builder == null) continue;
                    builder.hasTESR(hasTESR);
                }
                return this;
            }

            @Override
            public CustomMultiblockBuilder blockProp(NonNullUnaryOperator<BlockBehaviour.Properties> blockProp) {
                for (var builder : builders) {
                    if (builder == null) continue;
                    builder.blockProp(blockProp);
                }
                return this;
            }

            @Override
            public CustomMultiblockBuilder itemProp(NonNullUnaryOperator<Item.Properties> itemProp) {
                for (var builder : builders) {
                    if (builder == null) continue;
                    builder.itemProp(itemProp);
                }
                return this;
            }

            @Override
            public CustomMultiblockBuilder blockBuilder(Consumer<BlockBuilder<? extends Block, ?>> blockBuilder) {
                for (var builder : builders) {
                    if (builder == null) continue;
                    builder.blockBuilder(blockBuilder);
                }
                return this;
            }

            @Override
            public CustomMultiblockBuilder itemBuilder(Consumer<ItemBuilder<? extends MetaMachineItem, ?>> itemBuilder) {
                for (var builder : builders) {
                    if (builder == null) continue;
                    builder.itemBuilder(itemBuilder);
                }
                return this;
            }

            @Override
            public CustomMultiblockBuilder onBlockEntityRegister(NonNullConsumer<BlockEntityType<BlockEntity>> onBlockEntityRegister) {
                for (var builder : builders) {
                    if (builder == null) continue;
                    builder.onBlockEntityRegister(onBlockEntityRegister);
                }
                return this;
            }

            @Override
            public CustomMultiblockBuilder recipeTypes(GTRecipeType... recipeTypes) {
                for (var builder : builders) {
                    if (builder == null) continue;
                    builder.recipeTypes(recipeTypes);
                }
                return this;
            }

            @Override
            public CustomMultiblockBuilder recipeType(GTRecipeType recipeType) {
                return recipeTypes(recipeType);
            }

            @Override
            public CustomMultiblockBuilder tier(int tier) {
                return this;
            }

            @Override
            public CustomMultiblockBuilder paintingColor(int paintingColor) {
                for (var builder : builders) {
                    if (builder == null) continue;
                    builder.paintingColor(paintingColor);
                }
                return this;
            }

            @Override
            public CustomMultiblockBuilder itemColor(BiFunction<ItemStack, Integer, Integer> itemColor) {
                for (var builder : builders) {
                    if (builder == null) continue;
                    builder.itemColor(itemColor);
                }
                return this;
            }

            @Override
            public CustomMultiblockBuilder abilities(PartAbility... abilities) {
                for (var builder : builders) {
                    if (builder == null) continue;
                    builder.abilities(abilities);
                }
                return this;
            }

            public CustomMultiblockBuilder tooltips(Component... tooltips) {
                for (var builder : builders) {
                    if (builder == null) continue;
                    builder.tooltips(tooltips);
                }
                return this;
            }

            @Override
            public CustomMultiblockBuilder tooltipBuilder(BiConsumer<ItemStack, List<Component>> tooltipBuilder) {
                for (var builder : builders) {
                    if (builder == null) continue;
                    builder.tooltipBuilder(tooltipBuilder);
                }
                return this;
            }

            @Override
            public CustomMultiblockBuilder recipeModifier(BiFunction<MetaMachine, GTRecipe, GTRecipe> recipeModifier) {
                for (var builder : builders) {
                    if (builder == null) continue;
                    builder.recipeModifier(recipeModifier);
                }
                return this;
            }

            @Override
            public CustomMultiblockBuilder alwaysTryModifyRecipe(boolean alwaysTryModifyRecipe) {
                for (var builder : builders) {
                    if (builder == null) continue;
                    builder.alwaysTryModifyRecipe(alwaysTryModifyRecipe);
                }
                return this;
            }

            @Override
            public CustomMultiblockBuilder appearance(Supplier<BlockState> appearance) {
                for (var builder : builders) {
                    if (builder == null) continue;
                    builder.appearance(appearance);
                }
                return this;
            }

            @Override
            public CustomMultiblockBuilder editableUI(@Nullable EditableMachineUI editableUI) {
                for (var builder : builders) {
                    if (builder == null) continue;
                    builder.editableUI(editableUI);
                }
                return this;
            }

            @Override
            public CustomMultiblockBuilder langValue(String langValue) {
                for (var builder : builders) {
                    if (builder == null) continue;
                    builder.langValue(langValue);
                }
                return this;
            }

            public CustomMultiblockBuilder recipeModifier(BiFunction<MetaMachine, GTRecipe, GTRecipe> recipeModifier, boolean alwaysTryModifyRecipe) {
                recipeModifier(recipeModifier);
                alwaysTryModifyRecipe(alwaysTryModifyRecipe);
                return this;
            }

            public CustomMultiblockBuilder tier(int tier, BuilderConsumer consumer) {
                for (var builder : builders) {
                    if (builder == null) continue;
                    if (builder.tier() == tier) {
                        consumer.accept(builder);
                    }
                }
                return this;
            }

            public CustomMultiblockBuilder allTiers(TieredBuilderConsumer consumer) {
                for (var builder : builders) {
                    if (builder == null) continue;
                    consumer.accept(builder.tier(), builder);
                }
                return this;
            }

            @Override
            public MultiblockMachineDefinition register() {
                for (var builder : builders) {
                    if (builder == null) continue;
                    value = builder.register();
                }
                return value;
            }
        };
    }

    @FunctionalInterface
    public interface BuilderConsumer extends Consumer<CustomMultiblockBuilder> {
        void accept(CustomMultiblockBuilder builder);
    }

    @FunctionalInterface
    public interface TieredBuilderConsumer {
        void accept(int tier, CustomMultiblockBuilder builder);
    }
}

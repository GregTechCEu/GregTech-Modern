package com.gregtechceu.gtceu.api.machine;

import com.gregtechceu.gtceu.api.block.IMachineBlock;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.gui.editor.EditableMachineUI;
import com.gregtechceu.gtceu.api.item.MetaMachineItem;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;
import com.gregtechceu.gtceu.client.model.machine.MachineRenderState;

import com.lowdragmc.lowdraglib.utils.ShapeUtils;

import net.minecraft.core.Direction;
import net.minecraft.core.IdMapper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.*;

/**
 * Representing basic information of a machine.
 */
public class MachineDefinition implements Supplier<IMachineBlock> {

    public static final IdMapper<MachineRenderState> RENDER_STATE_REGISTRY = new IdMapper<>(512);

    @Getter
    private final ResourceLocation id;
    // This is only stored here for KJS use.
    @Getter
    @Setter
    private String langValue;
    @Setter
    private Supplier<? extends Block> blockSupplier;
    @Setter
    private Supplier<? extends MetaMachineItem> itemSupplier;
    @Setter
    private Supplier<BlockEntityType<? extends BlockEntity>> blockEntityTypeSupplier;
    @Setter
    private Function<IMachineBlockEntity, MetaMachine> machineSupplier;
    @Getter
    @Setter
    private @NotNull GTRecipeType @NotNull [] recipeTypes;
    @Getter
    @Setter
    private int tier;
    @Getter
    @Setter
    private int defaultPaintingColor;
    @Getter
    @Setter
    private RecipeModifier recipeModifier;
    @Getter
    @Setter
    private boolean alwaysTryModifyRecipe;
    @NotNull
    @Getter
    @Setter
    private BiPredicate<IRecipeLogicMachine, GTRecipe> beforeWorking = (machine, recipe) -> true;
    @NotNull
    @Getter
    @Setter
    private Predicate<IRecipeLogicMachine> onWorking = (machine) -> true;
    @NotNull
    @Getter
    @Setter
    private Consumer<IRecipeLogicMachine> onWaiting = (machine) -> {};
    @NotNull
    @Getter
    @Setter
    private Consumer<IRecipeLogicMachine> afterWorking = (machine) -> {};
    @Getter
    @Setter
    private boolean regressWhenWaiting = true;
    /** Whether this machine can be rotated or face upwards. */
    @Getter
    @Setter
    private boolean allowExtendedFacing;

    @Getter
    @Setter
    private RotationState rotationState;
    @Setter
    private VoxelShape shape;
    @Getter
    @Setter
    private boolean renderWorldPreview;
    @Getter
    @Setter
    private boolean renderXEIPreview;
    private final Map<Direction, VoxelShape> cache = new EnumMap<>(Direction.class);
    @Getter
    @Setter
    private BiConsumer<ItemStack, List<Component>> tooltipBuilder;
    @Getter
    @Setter
    private Supplier<BlockState> appearance;
    @Getter
    @Setter
    private boolean allowCoverOnFront;
    @Nullable
    @Getter
    @Setter
    private EditableMachineUI editableUI;
    @Getter
    @Setter
    private Reference2IntMap<RecipeCapability<?>> recipeOutputLimits = new Reference2IntOpenHashMap<>();

    @Getter
    @Setter(onMethod_ = @ApiStatus.Internal)
    private StateDefinition<MachineDefinition, MachineRenderState> stateDefinition;
    @Accessors(fluent = true)
    @Getter
    private MachineRenderState defaultRenderState;

    public MachineDefinition(ResourceLocation id) {
        this.id = id;
    }

    public final void registerDefaultState(MachineRenderState state) {
        this.defaultRenderState = state;
    }

    public Block getBlock() {
        return blockSupplier.get();
    }

    public MetaMachineItem getItem() {
        return itemSupplier.get();
    }

    public BlockEntityType<? extends BlockEntity> getBlockEntityType() {
        return blockEntityTypeSupplier.get();
    }

    public MetaMachine createMetaMachine(IMachineBlockEntity blockEntity) {
        return machineSupplier.apply(blockEntity);
    }

    public ItemStack asStack() {
        return new ItemStack(getItem());
    }

    public ItemStack asStack(int count) {
        return new ItemStack(getItem(), count);
    }

    public VoxelShape getShape(Direction direction) {
        if (shape.isEmpty() || shape == Shapes.block() || direction == Direction.NORTH) return shape;
        return this.cache.computeIfAbsent(direction, dir -> ShapeUtils.rotate(shape, dir));
    }

    @Override
    public IMachineBlock get() {
        return (IMachineBlock) blockSupplier.get();
    }

    public String getName() {
        return id.getPath();
    }

    @Override
    public String toString() {
        return id.toString();
    }

    public String getDescriptionId() {
        return getBlock().getDescriptionId();
    }

    public BlockState defaultBlockState() {
        return getBlock().defaultBlockState();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MachineDefinition that = (MachineDefinition) o;

        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    static final ThreadLocal<MachineDefinition> STATE = new ThreadLocal<>();

    public static MachineDefinition getBuilt() {
        return STATE.get();
    }

    public static void setBuilt(MachineDefinition state) {
        STATE.set(state);
    }

    public static void clearBuilt() {
        STATE.remove();
    }
}

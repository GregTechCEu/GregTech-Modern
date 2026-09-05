package com.gregtechceu.gtceu.api.machine;

import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.item.MetaMachineItem;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.mui.factory.PanelFactory;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifierList;
import com.gregtechceu.gtceu.api.registry.registrate.builder.MachineBuilder;
import com.gregtechceu.gtceu.api.registry.registrate.provider.GTBlockstateProvider;
import com.gregtechceu.gtceu.client.model.machine.MachineRenderState;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.core.Direction;
import net.minecraft.core.IdMapper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.registries.DeferredHolder;

import brachy.modularui.theme.ThemeAPI;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.*;

/**
 * Representing basic information of a machine.
 */
public class MachineDefinition {

    public static final IdMapper<MachineRenderState> RENDER_STATE_REGISTRY = new IdMapper<>(512);

    @Getter
    private final ResourceLocation id;
    // This is only stored here for KJS use.
    @Getter
    @Nullable
    private final String langValue;
    private final DeferredHolder<Block, ? extends MetaMachineBlock> blockHolder;
    private final DeferredHolder<Item, ? extends MetaMachineItem> itemHolder;
    private final Supplier<BlockEntityType<? extends MetaMachine>> blockEntityTypeSupplier;
    @Getter
    private final GTRecipeType[] recipeTypes;
    @Getter
    private final int tier;
    @Getter
    private final int defaultPaintingColor;
    @Getter
    private final RecipeModifier recipeModifier;
    @Getter
    private final boolean alwaysTryModifyRecipe;
    @Getter
    private final BiPredicate<IRecipeLogicMachine, @Nullable GTRecipe> beforeWorking;
    @Getter
    private final Predicate<IRecipeLogicMachine> onWorking;
    @Getter
    private final Consumer<IRecipeLogicMachine> onWaiting;
    @Getter
    private final Consumer<IRecipeLogicMachine> afterWorking;
    @Getter
    private final boolean regressWhenWaiting;
    /** Whether this machine can be rotated or face upwards. */
    @Getter
    private final boolean allowExtendedFacing;
    @Getter
    private final RotationState rotationState;
    private final VoxelShape shape;
    private final Map<Direction, VoxelShape> cache = new EnumMap<>(Direction.class);
    @Getter
    private final BiConsumer<ItemStack, List<Component>> tooltipBuilder;
    @Getter
    private final Supplier<BlockState> appearance;
    @Getter
    private final boolean allowCoverOnFront;
    @Getter
    @Nullable
    private final PanelFactory UI;
    @Getter
    private final String themeId;
    @Getter
    private final Reference2IntMap<RecipeCapability<?>> recipeOutputLimits;
    @Getter
    private final StateDefinition<MachineDefinition, MachineRenderState> stateDefinition;
    @Accessors(fluent = true)
    @Getter
    private final MachineRenderState defaultRenderState;

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public MachineDefinition(ResourceLocation id, Properties properties) {
        this.id = id;

        this.rotationState = properties.rotationState();
        this.langValue = properties.langValue();
        this.UI = properties.ui();
        this.themeId = properties.themeId();
        this.recipeTypes = properties.recipeTypes();
        this.blockHolder = properties.blockHolder();
        this.itemHolder = properties.itemHolder();
        this.tier = properties.tier();
        this.recipeOutputLimits = properties.recipeOutputLimits();
        this.blockEntityTypeSupplier = properties.blockEntityTypeSupplier();
        this.tooltipBuilder = (itemStack, components) -> {
            components.addAll(properties.tooltips());
            if (properties.tooltipBuilder() != null) properties.tooltipBuilder().accept(itemStack, components);
        };
        this.recipeModifier = properties.recipeModifier();
        this.alwaysTryModifyRecipe = properties.alwaysTryModifyRecipe();
        this.beforeWorking = properties.beforeWorking();
        this.onWorking = properties.onWorking();
        this.onWaiting = properties.onWaiting();
        this.afterWorking = properties.afterWorking();
        this.regressWhenWaiting = properties.regressWhenWaiting();
        this.allowCoverOnFront = properties.allowCoverOnFront();

        for (GTRecipeType type : recipeTypes) {
            if (type.getIconSupplier() == null) {
                type.setIconSupplier(this::asStack);
            }
        }
        if (properties.appearance() == null) {
            properties.appearance(() -> blockHolder.value().defaultBlockState());
        }
        this.appearance = properties.appearance();
        this.allowExtendedFacing = properties.allowExtendedFacing();
        this.shape = properties.shape();
        this.defaultPaintingColor = properties.paintingColor();

        // Initialise render state

        StateDefinition.Builder<MachineDefinition, MachineRenderState> builder = new StateDefinition.Builder<>(
                this);
        properties.modelProperties().keySet().forEach(builder::add);
        stateDefinition = builder.create(MachineDefinition::defaultRenderState, MachineRenderState::new);

        MachineRenderState defaultState = getStateDefinition().any();
        for (var entry : properties.modelProperties().entrySet()) {
            if (entry.getValue() == null) continue;
            defaultState = defaultState.setValue((Property) entry.getKey(), (Comparable) entry.getValue());
        }
        this.defaultRenderState = defaultState;
    }

    public boolean isTiered() {
        return tier != -1;
    }

    public MetaMachineBlock getBlock() {
        return blockHolder.get();
    }

    public MetaMachineItem getItem() {
        return itemHolder.get();
    }

    public BlockEntityType<? extends MetaMachine> getBlockEntityType() {
        return blockEntityTypeSupplier.get();
    }

    public ItemStack asStack() {
        return new ItemStack(getItem());
    }

    public ItemStack asStack(int count) {
        return new ItemStack(getItem(), count);
    }

    public VoxelShape getShape(Direction direction) {
        if (shape.isEmpty() || shape == Shapes.block() || direction == Direction.NORTH) return shape;
        return this.cache.computeIfAbsent(direction, dir -> GTUtil.rotateVoxelShape(shape, dir));
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

    static final ThreadLocal<@Nullable MachineDefinition> STATE = new ThreadLocal<>();

    public static MachineDefinition getBuilt() {
        return Objects.requireNonNull(STATE.get());
    }

    public static void setBuilt(MachineDefinition state) {
        STATE.set(state);
    }

    public static void clearBuilt() {
        STATE.remove();
    }

    /**
     * Gets the input size for a machine for the capability with the machine having the specified recipe types
     */
    public int getInputSize(RecipeCapability<?> cap, GTRecipeType... recipeTypes) {
        int recipeTypeInputSize = 0;
        for (var recipeType : recipeTypes) {
            recipeTypeInputSize = Math.max(recipeType.getMaxInputs(cap), recipeTypeInputSize);
        }
        return recipeTypeInputSize;
    }

    /**
     * Gets the output size for a machine for the capability with the machine having the specified recipe types
     */
    public int getOutputSize(RecipeCapability<?> cap, GTRecipeType... recipeTypes) {
        int recipeTypeOutputSize = 0;
        for (var recipeType : recipeTypes) {
            recipeTypeOutputSize = Math.max(recipeType.getMaxOutputs(cap), recipeTypeOutputSize);
        }
        int machineTypeOutputLimit = this.getRecipeOutputLimits().getOrDefault(cap, recipeTypeOutputSize);
        return Math.min(recipeTypeOutputSize, machineTypeOutputLimit);
    }

    @Accessors(fluent = true)
    @Getter
    @Setter
    public static class Properties {

        private DeferredHolder<Block, ? extends MetaMachineBlock> blockHolder;
        private DeferredHolder<Item, ? extends MetaMachineItem> itemHolder;
        private Supplier<BlockEntityType<? extends MetaMachine>> blockEntityTypeSupplier;

        @Nullable
        private MachineBuilder.ModelInitializer model = null;
        private @Nullable NonNullBiConsumer<DataGenContext<Block, ? extends Block>, GTBlockstateProvider> blockModel = null;
        protected final Map<Property<?>, @Nullable Comparable<?>> modelProperties = new IdentityHashMap<>();
        private VoxelShape shape = Shapes.block();
        private RotationState rotationState = RotationState.NON_Y_AXIS;
        private boolean allowExtendedFacing = false;
        private boolean hasBER = ConfigHolder.INSTANCE.client.machinesHaveBERsByDefault;
        private GTRecipeType[] recipeTypes = new GTRecipeType[0];
        private int tier = -1;
        private Reference2IntMap<RecipeCapability<?>> recipeOutputLimits = new Reference2IntOpenHashMap<>();
        private int paintingColor = ConfigHolder.INSTANCE.client.getDefaultPaintingColor();
        private PartAbility[] abilities = new PartAbility[0];
        private final List<Component> tooltips = new ArrayList<>();
        private @Nullable BiConsumer<ItemStack, List<Component>> tooltipBuilder;
        private RecipeModifier recipeModifier = new RecipeModifierList(GTRecipeModifiers.OC_NON_PERFECT);
        private boolean alwaysTryModifyRecipe;
        private BiPredicate<IRecipeLogicMachine, GTRecipe> beforeWorking = (machine, recipe) -> true;
        private Predicate<IRecipeLogicMachine> onWorking = (machine) -> true;
        private Consumer<IRecipeLogicMachine> onWaiting = (machine) -> {};
        private Consumer<IRecipeLogicMachine> afterWorking = (machine) -> {};
        private boolean regressWhenWaiting = true;
        private boolean allowCoverOnFront = false;
        @Nullable
        private PanelFactory ui = null;
        private String themeId = ThemeAPI.DEFAULT_ID;
        private @Nullable Supplier<BlockState> appearance;
        @Nullable
        private String langValue = null;
    }
}

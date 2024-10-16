package com.gregtechceu.gtceu.integration.jsonthings.builders;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.gui.editor.EditableMachineUI;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.pattern.MultiblockShapeInfo;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifierList;
import com.gregtechceu.gtceu.client.renderer.GTRendererProvider;
import com.gregtechceu.gtceu.client.renderer.machine.OverlayTieredMachineRenderer;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.integration.jsonthings.JsonThingsCompat;
import com.gregtechceu.gtceu.integration.jsonthings.serializers.IMachineFactory;
import com.gregtechceu.gtceu.integration.jsonthings.serializers.MachineBuilderType;
import com.gregtechceu.gtceu.utils.SupplierMemoizer;

import com.lowdragmc.lowdraglib.LDLib;
import com.lowdragmc.lowdraglib.Platform;
import com.lowdragmc.lowdraglib.client.renderer.IRenderer;

import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import com.tterrag.registrate.util.OneTimeEventReceiver;
import dev.gigaherz.jsonthings.things.builders.BaseBuilder;
import dev.gigaherz.jsonthings.things.builders.BlockBuilder;
import dev.gigaherz.jsonthings.things.parsers.ThingParser;
import dev.gigaherz.jsonthings.things.shapes.DynamicShape;
import dev.gigaherz.jsonthings.util.Utils;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.function.TriFunction;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.*;

public class MachineBuilder extends BaseBuilder<MachineDefinition, MachineBuilder> {

    public static MachineBuilder begin(ThingParser<MachineBuilder> ownerParser, ResourceLocation registryName) {
        return new MachineBuilder(ownerParser, registryName);
    }

    @Getter
    @Setter
    protected BlockBuilder blockBuilder;
    @Getter
    protected Supplier<BlockEntityType<?>> blockEntityTypeSupplier;
    protected TriFunction<BlockEntityType<?>, BlockPos, BlockState, IMachineBlockEntity> blockEntityFactory = MetaMachineBlockEntity::createBlockEntity;
    protected MachineBuilderType<?, ?> type;
    @Setter
    private DynamicShape shape;
    @Setter
    private RotationState rotationState = RotationState.NONE;
    @Setter
    private Boolean hasTESR;
    @Setter
    private Boolean renderMultiblockWorldPreview;
    @Setter
    private Boolean renderMultiblockXEIPreview;
    @Setter
    private GTRecipeType[] recipeTypes;
    @Setter
    private Integer tier;
    @Setter
    private Object2IntMap<RecipeCapability<?>> recipeOutputLimits;
    @Setter
    private Integer paintingColor;
    @Setter
    private PartAbility[] abilities;
    @Setter
    private List<Component> tooltips;
    @Setter
    private RecipeModifier recipeModifier;
    @Setter
    private Boolean alwaysTryModifyRecipe;

    @Setter
    private Supplier<BlockState> appearance;
    @Setter
    @Nullable
    private EditableMachineUI editableUI;

    @Setter
    private Boolean generator;
    @Setter
    private Supplier<BlockPattern> pattern;
    @Setter
    private Supplier<List<MultiblockShapeInfo>> shapeInfo;
    /** Whether this multi can be rotated or face upwards. */
    @Setter
    private Boolean allowExtendedFacing = true;
    /** Set this to false only if your multiblock is set up such that it could have a wall-shared controller. */
    @Setter
    private Boolean allowFlip = true;
    @Setter
    private Supplier<ItemStack[]> recoveryItems;
    @Setter
    private Comparator<IMultiPart> partSorter = (a, b) -> 0;
    @Setter
    private TriFunction<IMultiController, IMultiPart, Direction, BlockState> partAppearance;
    @Setter
    private BiConsumer<IMultiController, List<Component>> additionalDisplay = (m, l) -> {};

    @Setter
    private IMachineFactory<?, ?> factory;

    protected MachineBuilder(ThingParser<MachineBuilder> ownerParser, ResourceLocation registryName) {
        super(ownerParser, registryName);
    }

    public void setType(String typeName) {
        if (this.type != null) throw new RuntimeException("Machine type already set.");
        MachineBuilderType<?, ?> machineType = JsonThingsCompat.MACHINE_BUILDER_TYPES.get(typeName);
        if (machineType == null)
            throw new IllegalStateException("No known machine type with name " + typeName);
        this.type = machineType;
    }

    public void setType(MachineBuilderType<?, ?> type) {
        if (JsonThingsCompat.MACHINE_BUILDER_TYPES.getKey(type) == null)
            throw new IllegalStateException("Machine type not registered!");
        this.type = type;
    }

    public MachineBuilderType<?, ?> getTypeRaw() {
        return getValue(type, MachineBuilder::getTypeRaw);
    }

    public MachineBuilderType<?, ?> getType() {
        return Utils.orElse(getTypeRaw(), MachineBuilderType.PLAIN);
    }

    public DynamicShape getShape() {
        return getValueOrElse(shape, MachineBuilder::getShape, JsonThingsCompat.FULL_BLOCK);
    }

    public RotationState getRotationState() {
        return getValue(rotationState, MachineBuilder::getRotationState);
    }

    public boolean isHasTESR() {
        return getValueOrElse(hasTESR, MachineBuilder::isHasTESR, false);
    }

    public boolean isRenderMultiblockXEIPreview() {
        return getValueOrElse(renderMultiblockXEIPreview, MachineBuilder::isRenderMultiblockXEIPreview, true);
    }

    public boolean isRenderMultiblockWorldPreview() {
        return getValueOrElse(renderMultiblockWorldPreview, MachineBuilder::isRenderMultiblockWorldPreview, true);
    }

    public GTRecipeType[] getRecipeTypes() {
        return getValue(recipeTypes, MachineBuilder::getRecipeTypes);
    }

    /**
     * Gets the tier. defaults to LV (1)
     * 
     * @return the tier of this machine
     */
    public int getTier() {
        return getValueOrElse(tier, MachineBuilder::getTier, 1);
    }

    public Object2IntMap<RecipeCapability<?>> getRecipeOutputLimits() {
        return getValueOrElseGet(recipeOutputLimits, MachineBuilder::getRecipeOutputLimits, Object2IntOpenHashMap::new);
    }

    public Integer getPaintingColor() {
        return getValueOrElse(paintingColor,
                MachineBuilder::getPaintingColor,
                Long.decode(ConfigHolder.INSTANCE.client.defaultPaintingColor).intValue());
    }

    public PartAbility[] getAbilities() {
        return getValueOrElseGet(abilities, MachineBuilder::getAbilities, () -> new PartAbility[0]);
    }

    public List<Component> getTooltips() {
        return getValueOrElseGet(tooltips, MachineBuilder::getTooltips, ArrayList::new);
    }

    public RecipeModifier getRecipeModifier() {
        return getValueOrElseGet(recipeModifier,
                MachineBuilder::getRecipeModifier,
                () -> new RecipeModifierList(GTRecipeModifiers.ELECTRIC_OVERCLOCK
                        .apply(OverclockingLogic.NON_PERFECT_OVERCLOCK)));
    }

    public boolean isAlwaysTryModifyRecipe() {
        return getValueOrElse(alwaysTryModifyRecipe, MachineBuilder::isAlwaysTryModifyRecipe, true);
    }

    public Supplier<BlockState> getAppearance() {
        return getValue(appearance, MachineBuilder::getAppearance);
    }

    public @Nullable EditableMachineUI getEditableUI() {
        return getValue(editableUI, MachineBuilder::getEditableUI);
    }

    public boolean isGenerator() {
        return getValueOrElse(generator, MachineBuilder::isGenerator, false);
    }

    public Supplier<BlockPattern> getPattern() {
        return getValue(pattern, MachineBuilder::getPattern);
    }

    public Supplier<List<MultiblockShapeInfo>> getShapeInfo() {
        return getValue(shapeInfo, MachineBuilder::getShapeInfo);
    }

    public boolean isAllowExtendedFacing() {
        return getValueOrElse(allowExtendedFacing, MachineBuilder::isAllowExtendedFacing, true);
    }

    public boolean isAllowFlip() {
        return getValueOrElse(allowFlip, MachineBuilder::isAllowFlip, true);
    }

    public Supplier<ItemStack[]> getRecoveryItems() {
        return getValue(recoveryItems, MachineBuilder::getRecoveryItems);
    }

    public Comparator<IMultiPart> getPartSorter() {
        return getValue(partSorter, MachineBuilder::getPartSorter);
    }

    public TriFunction<IMultiController, IMultiPart, Direction, BlockState> getPartAppearance() {
        return getValue(partAppearance, MachineBuilder::getPartAppearance);
    }

    public BiConsumer<IMultiController, List<Component>> getAdditionalDisplay() {
        return getValue(additionalDisplay, MachineBuilder::getAdditionalDisplay);
    }

    @Override
    protected String getThingTypeDisplayName() {
        return "Machine";
    }

    @Override
    protected MachineDefinition buildInternal() {
        MachineDefinition definition = factory.construct(this.getRegistryName(), this);
        if (definition instanceof MultiblockMachineDefinition multi) {
            multi.setPatternFactory(SupplierMemoizer.memoize(getPattern()));
            multi.setShapes(SupplierMemoizer.memoize(getShapeInfo()));
            multi.setAllowExtendedFacing(isAllowExtendedFacing());
            multi.setAllowFlip(isAllowFlip());
            if (getRecoveryItems() != null) {
                multi.setRecoveryItems(getRecoveryItems());
            }
            multi.setPartSorter(getPartSorter());
            if (getPartAppearance() == null) {
                partAppearance = (controller, part, side) -> definition.getAppearance().get();
            }
            multi.setPartAppearance(getPartAppearance());
            multi.setAdditionalDisplay(getAdditionalDisplay());
        }

        // noinspection unchecked,rawtypes we will just assume it's the correct generic.
        definition.setMachineSupplier((holder) -> ((IMachineFactory) factory).create(holder, definition));
        definition.setBlockSupplier(() -> blockBuilder.get().self());
        definition.setItemSupplier(() -> blockBuilder.getItemBuilder().get().self());

        blockEntityTypeSupplier = SupplierMemoizer.memoize(() -> {
            RotationState.set(MachineBuilder.this.getRotationState());
            MachineDefinition.setBuilt(definition);
            Block block = blockBuilder.get().self();
            RotationState.clear();
            MachineDefinition.clearBuilt();
            return BlockEntityType.Builder
                    .of((pos, state) -> blockEntityFactory.apply(blockEntityTypeSupplier.get(), pos, state).self(),
                            block)
                    .build(null);
        });
        if (Platform.isClient() && isHasTESR()) {
            OneTimeEventReceiver.addListener(FMLJavaModLoadingContext.get().getModEventBus(), FMLClientSetupEvent.class,
                    $ -> {
                        BlockEntityRenderers.register(getBlockEntityTypeSupplier().get(),
                                GTRendererProvider::getOrCreate);
                    });
        }
        definition.setBlockEntityTypeSupplier(blockEntityTypeSupplier);

        definition.setShape(getShape().getShape(null));
        definition.setRenderXEIPreview(isRenderMultiblockXEIPreview());
        definition.setRenderWorldPreview(isRenderMultiblockWorldPreview());
        definition.setRecipeTypes(getRecipeTypes());
        definition.setTier(getTier());
        definition.setDefaultPaintingColor(getPaintingColor());
        definition.setTooltipBuilder((stack, components) -> components.addAll(getTooltips()));
        definition.setRecipeModifier(getRecipeModifier());
        definition.setRecipeOutputLimits(getRecipeOutputLimits());

        if (getAppearance() == null) {
            setAppearance(() -> blockBuilder.get().self().defaultBlockState());
        }
        definition.setAppearance(getAppearance());

        definition.setRenderer(LDLib.isClient() ?
                new OverlayTieredMachineRenderer(tier,
                        getRegistryName().withPrefix("block/machine/part/")) :
                IRenderer.EMPTY);

        return definition;
    }
}

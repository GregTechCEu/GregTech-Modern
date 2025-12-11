package com.gregtechceu.gtceu.client.model.pipe;

import com.gregtechceu.gtceu.api.block.PipeBlock;
import com.gregtechceu.gtceu.api.block.property.GTBlockStateProperties;
import com.gregtechceu.gtceu.data.model.builder.PipeModelBuilder;

import net.minecraft.data.models.blockstates.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.generators.BlockModelBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

public class ActivablePipeModel extends PipeModel {

    @Setter
    public @Nullable ResourceLocation sideActive, endActive;
    @Setter
    public @Nullable ResourceLocation sideSecondaryActive, endSecondaryActive;
    @Setter
    public @Nullable ResourceLocation sideOverlayActive, endOverlayActive;

    /// Use {@link #getActiveBlockModel()} instead of referencing this field directly.
    @Getter(lazy = true)
    private final BlockModelBuilder activeBlockModel = createActiveBlockModel();
    /// Use {@link #getActiveCenterElement()} instead of referencing this field directly.
    @Getter(lazy = true)
    private final BlockModelBuilder activeCenterElement = createActiveCenterElement();
    /// Use {@link #getActiveConnectionElement()} instead of referencing this field directly.
    @Getter(lazy = true)
    private final BlockModelBuilder activeConnectionElement = createActiveConnectionElement();

    public ActivablePipeModel(PipeBlock<?, ?, ?> block, float thickness, ResourceLocation side, ResourceLocation end,
                              ExistingFileHelper existingFileHelper) {
        super(block, existingFileHelper, thickness, side, end);
    }

    /**
     * @return A mutable collection of all block model builders that are required for this block to exist.
     * @see #createActiveBlockModel()
     * @see #createConnectionElement()
     * @see #createActiveCenterElement()
     * @see #createActiveConnectionElement()
     */
    @Override
    public Collection<BlockModelBuilder> getBlockModels() {
        Collection<BlockModelBuilder> models = super.getBlockModels();
        Collections.addAll(models, this.getActiveCenterElement(), this.getActiveConnectionElement(),
                this.getActiveBlockModel());
        return models;
    }

    /**
     * Override this to change the actual model {@link #block this.block} will use.
     *
     * @return A model builder for the block's actual model.
     * @see #createBlockModel()
     * @see #createActiveCenterElement()
     * @see #createActiveConnectionElement()
     */
    @ApiStatus.OverrideOnly
    protected BlockModelBuilder createActiveBlockModel() {
        // spotless:off
        return new BlockModelBuilder(this.blockId, this.existingFileHelper)
                .parent(this.getActiveCenterElement())
                .customLoader(PipeModelBuilder::begin)
                .connectionModels()
                .modelFile(this.getActiveConnectionElement())
                .addModel()
                .centerModel()
                .modelFile(this.getActiveCenterElement())
                .addModel()
                .end();
        // spotless:on
    }

    /**
     * Override this to change the center element's model for when the pipe is active.
     *
     * @return A model builder for the center element's model.
     * @see #createCenterElement()
     * @see #createActiveConnectionElement()
     */
    @ApiStatus.OverrideOnly
    protected BlockModelBuilder createActiveCenterElement() {
        return makeActiveVariant(this.getCenterElement());
    }

    /**
     * Override this to change the 'connection' element's model for when the pipe is active.<br>
     * By default, this is rotated & used for all connected sides of the pipe.<br>
     * Note that that is not a hard requirement, and that you may set a model per side in {@link #createBlockModel()}.
     *
     * @return A model builder for the connection element's model.
     * @see #createConnectionElement()
     * @see #createActiveCenterElement()
     */
    @ApiStatus.OverrideOnly
    protected BlockModelBuilder createActiveConnectionElement() {
        return makeActiveVariant(this.getConnectionElement());
    }

    protected BlockModelBuilder makeActiveVariant(BlockModelBuilder parentModel) {
        BlockModelBuilder model = new BlockModelBuilder(parentModel.getLocation().withSuffix("_active"),
                this.existingFileHelper)
                .parent(parentModel);
        // override non-null textures, leave unset ones as is
        if (this.sideActive != null) model.texture("side", this.sideActive);
        if (this.endActive != null) model.texture("end", this.endActive);
        if (this.sideSecondaryActive != null) model.texture("side_secondary", this.sideSecondaryActive);
        if (this.endSecondaryActive != null) model.texture("end_secondary", this.endSecondaryActive);
        if (this.sideOverlayActive != null) model.texture("side_overlay", this.sideOverlayActive);
        if (this.endOverlayActive != null) model.texture("end_overlay", this.endOverlayActive);

        return model;
    }

    @Override
    public BlockStateGenerator createBlockState() {
        if (!this.getBlock().defaultBlockState().hasProperty(GTBlockStateProperties.ACTIVE)) {
            return super.createBlockState();
        }

        ResourceLocation baseModelLoc = this.blockId.withPrefix("block/");
        return MultiVariantGenerator.multiVariant(this.getBlock(),
                Variant.variant().with(VariantProperties.MODEL, baseModelLoc))
                .with(PropertyDispatch.property(GTBlockStateProperties.ACTIVE)
                        .select(true, Variant.variant()
                                .with(VariantProperties.MODEL, baseModelLoc.withSuffix("_active"))));
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ActivablePipeModel pipeModel)) return false;
        return super.equals(o) &&
                Objects.equals(sideActive, pipeModel.sideActive) &&
                Objects.equals(endActive, pipeModel.endActive) &&
                Objects.equals(sideSecondaryActive, pipeModel.sideSecondaryActive) &&
                Objects.equals(endSecondaryActive, pipeModel.endSecondaryActive) &&
                Objects.equals(sideOverlayActive, pipeModel.sideOverlayActive) &&
                Objects.equals(endOverlayActive, pipeModel.endOverlayActive);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + Objects.hashCode(sideActive);
        result = 31 * result + Objects.hashCode(endActive);
        result = 31 * result + Objects.hashCode(sideSecondaryActive);
        result = 31 * result + Objects.hashCode(endSecondaryActive);
        result = 31 * result + Objects.hashCode(sideOverlayActive);
        result = 31 * result + Objects.hashCode(endOverlayActive);
        return result;
    }
}

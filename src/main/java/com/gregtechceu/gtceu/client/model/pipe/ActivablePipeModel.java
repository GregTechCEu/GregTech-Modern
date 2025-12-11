package com.gregtechceu.gtceu.client.model.pipe;

import com.gregtechceu.gtceu.api.block.PipeBlock;
import com.gregtechceu.gtceu.api.block.property.GTBlockStateProperties;
import com.gregtechceu.gtceu.api.registry.registrate.provider.GTBlockstateProvider;
import com.gregtechceu.gtceu.data.model.builder.PipeModelBuilder;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.generators.BlockModelBuilder;
import net.minecraftforge.client.model.generators.IGeneratedBlockState;

import lombok.Setter;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class ActivablePipeModel extends PipeModel {

    @Setter
    public @Nullable ResourceLocation sideActive, endActive;
    @Setter
    public @Nullable ResourceLocation sideSecondaryActive, endSecondaryActive;
    @Setter
    public @Nullable ResourceLocation sideOverlayActive, endOverlayActive;

    /// Use {@link #getOrCreateActiveBlockModel()} instead of referencing this field directly.
    private BlockModelBuilder activeBlockModel;
    /// Use {@link #getOrCreateActiveCenterElement()} instead of referencing this field directly.
    private BlockModelBuilder activeCenterElement;
    /// Use {@link #getOrCreateActiveConnectionElement()} instead of referencing this field directly.
    private BlockModelBuilder activeConnectionElement;

    public ActivablePipeModel(PipeBlock<?, ?, ?> block, float thickness, ResourceLocation side, ResourceLocation end,
                              GTBlockstateProvider provider) {
        super(block, provider, thickness, side, end);
    }

    /**
     * @see #getOrCreateActiveBlockModel()
     * @see #getOrCreateConnectionElement()
     * @see #getOrCreateActiveCenterElement()
     * @see #getOrCreateActiveConnectionElement()
     */
    @Override
    public void initModels() {
        getOrCreateActiveCenterElement();
        getOrCreateActiveConnectionElement();
        getOrCreateActiveBlockModel();

        super.initModels();
    }

    /**
     * Override this to change the actual model {@link #block this.block} will use.
     *
     * @return A model builder for the block's actual model.
     * @see #getOrCreateBlockModel()
     * @see #getOrCreateActiveCenterElement()
     * @see #getOrCreateActiveConnectionElement()
     */
    @ApiStatus.OverrideOnly
    protected BlockModelBuilder getOrCreateActiveBlockModel() {
        if (this.activeBlockModel != null) {
            return this.activeBlockModel;
        }
        // spotless:off
        return this.activeBlockModel = this.provider.models().getBuilder(this.blockId.withSuffix("_active").toString())
                .parent(this.getOrCreateActiveCenterElement())
                .customLoader(PipeModelBuilder::begin)
                    .thickness(this.thickness).provider(this.provider)
                    .centerModels(this.getOrCreateActiveCenterElement().getLocation())
                    .connectionModels(this.getOrCreateActiveConnectionElement().getLocation())
                .end();
        // spotless:on
    }

    /**
     * Override this to change the center element's model for when the pipe is active.
     *
     * @return A model builder for the center element's model.
     * @see #getOrCreateCenterElement()
     * @see #getOrCreateActiveConnectionElement()
     */
    @ApiStatus.OverrideOnly
    protected BlockModelBuilder getOrCreateActiveCenterElement() {
        if (this.activeCenterElement != null) {
            return this.activeCenterElement;
        }
        return this.activeCenterElement = makeActiveVariant(this.getOrCreateCenterElement());
    }

    /**
     * Override this to change the 'connection' element's model for when the pipe is active.<br>
     * By default, this is rotated & used for all connected sides of the pipe.<br>
     * Note that that is not a hard requirement, and that you may set a model per side in
     * {@link #getOrCreateBlockModel()}.
     *
     * @return A model builder for the connection element's model.
     * @see #getOrCreateConnectionElement()
     * @see #getOrCreateActiveCenterElement()
     */
    @ApiStatus.OverrideOnly
    protected BlockModelBuilder getOrCreateActiveConnectionElement() {
        if (this.activeConnectionElement != null) {
            return this.activeConnectionElement;
        }
        return this.activeConnectionElement = makeActiveVariant(this.getOrCreateConnectionElement());
    }

    protected BlockModelBuilder makeActiveVariant(BlockModelBuilder parentModel) {
        BlockModelBuilder model = this.provider.models()
                .getBuilder(parentModel.getLocation().withSuffix("_active").toString())
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
    public IGeneratedBlockState createBlockState() {
        if (!this.getBlock().defaultBlockState().hasProperty(GTBlockStateProperties.ACTIVE)) {
            return super.createBlockState();
        }
        // spotless:off
        return this.provider.getVariantBuilder(this.getBlock())
                .partialState()
                    .with(GTBlockStateProperties.ACTIVE, false)
                    .modelForState()
                        .modelFile(this.provider.models().getExistingFile(this.blockId))
                    .addModel()
                .partialState()
                    .with(GTBlockStateProperties.ACTIVE, true)
                    .modelForState()
                        .modelFile(this.provider.models().getExistingFile(this.blockId.withSuffix("_active")))
                    .addModel();
        // spotless:on
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

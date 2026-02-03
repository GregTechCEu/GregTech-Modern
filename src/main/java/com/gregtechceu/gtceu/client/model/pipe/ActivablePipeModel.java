package com.gregtechceu.gtceu.client.model.pipe;

import com.gregtechceu.gtceu.api.block.PipeBlock;
import com.gregtechceu.gtceu.api.block.property.GTBlockStateProperties;
import com.gregtechceu.gtceu.api.registry.registrate.provider.GTBlockstateProvider;
import com.gregtechceu.gtceu.data.model.builder.PipeModelBuilder;

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.generators.BlockModelBuilder;
import net.minecraftforge.client.model.generators.IGeneratedBlockState;
import net.minecraftforge.client.model.generators.ModelBuilder;
import net.minecraftforge.client.model.generators.ModelFile;

import it.unimi.dsi.fastutil.objects.Reference2FloatMap;
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
    @Setter
    public int activeEmissivity = 15;

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
                .customLoader(PipeModelBuilder.begin(this.thickness, this.provider))
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
        return this.activeCenterElement = makeActiveElementModel(
                this.blockId.withPath(path -> "block/pipe/" + path + "/center_active"),
                null, minCoord, minCoord, minCoord, maxCoord, maxCoord, maxCoord);
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
        return this.activeConnectionElement = makeActiveElementModel(
                this.blockId.withPath(path -> "block/pipe/" + path + "/connection_active"),
                Direction.DOWN, minCoord, 0, minCoord, maxCoord, minCoord, maxCoord);
    }

    /**
     * Fills out a model builder with applicable pipe model elements and returns it for further use
     * <hr>
     * This method is a copy of {@linkplain #makeElementModel} with the texture references changed for active variants.
     *
     * @param name    the resulting model's path
     * @param endFace the model face that's being created
     * @param x1      min X coordinate in the range [-16,32]
     * @param y1      min Y coordinate in the range [-16,32]
     * @param z1      min Z coordinate in the range [-16,32]
     * @param x2      max X coordinate in the range [-16,32]
     * @param y2      max Y coordinate in the range [-16,32]
     * @param z2      max Z coordinate in the range [-16,32]
     * @implNote The coordinates must be in the correct order or the resulting model's cubes will be inside out!
     * @see #makeElementModel
     */
    protected BlockModelBuilder makeActiveElementModel(ResourceLocation name, @Nullable Direction endFace,
                                                       final float x1, final float y1, final float z1,
                                                       final float x2, final float y2, final float z2) {
        Reference2FloatMap<Direction> faceEndpoints = makeFaceEndpointMap(x1, y1, z1, x2, y2, z2);

        BlockModelBuilder model = this.provider.models().getBuilder(name.toString())
                .parent(new ModelFile.UncheckedModelFile("block/block"))
                .texture("particle", "#" + (this.side != null ? SIDE_KEY : END_KEY));

        ResourceLocation side = this.sideActive != null ? this.sideActive : this.side;
        ResourceLocation end = this.endActive != null ? this.endActive : this.end;
        ResourceLocation sideSecondary = this.sideSecondaryActive != null ? this.sideSecondaryActive :
                this.sideSecondary;
        ResourceLocation endSecondary = this.endSecondaryActive != null ? this.endSecondaryActive : this.endSecondary;
        ResourceLocation sideOverlay = this.sideOverlayActive != null ? this.sideOverlayActive : this.sideOverlay;
        ResourceLocation endOverlay = this.endOverlayActive != null ? this.endOverlayActive : this.endOverlay;

        makePartModelElement(model, endFace, false, faceEndpoints, 0.0f, 0, 1,
                x1, y1, z1, x2, y2, z2, side, end, SIDE_KEY, END_KEY,
                this.sideActive != null, this.endActive != null);

        makePartModelElement(model, endFace, true, faceEndpoints, 0.001f, 0, 1,
                x1, y1, z1, x2, y2, z2, sideSecondary, endSecondary, SIDE_SECONDARY_KEY, END_SECONDARY_KEY,
                this.sideSecondaryActive != null, this.endSecondaryActive != null);

        makePartModelElement(model, endFace, true, faceEndpoints, 0.002f, 2, 2,
                x1, y1, z1, x2, y2, z2, sideOverlay, endOverlay, SIDE_OVERLAY_KEY, END_OVERLAY_KEY,
                this.sideOverlayActive != null, this.endOverlayActive != null);

        return model;
    }

    protected <T extends ModelBuilder<T>> void makePartModelElement(T model, @Nullable Direction endFace,
                                                                    boolean useEndWithFullCube,
                                                                    Reference2FloatMap<Direction> faceEndpoints,
                                                                    float offset, int sideTintIndex, int endTintIndex,
                                                                    final float x1, final float y1, final float z1,
                                                                    final float x2, final float y2, final float z2,
                                                                    @Nullable ResourceLocation sideTexture,
                                                                    @Nullable ResourceLocation endTexture,
                                                                    String sideKey, String endKey,
                                                                    boolean sideEmissive, boolean endEmissive) {
        this.makePartModelElement(model, endFace, useEndWithFullCube, faceEndpoints, offset,
                sideTintIndex, endTintIndex, x1, y1, z1, x2, y2, z2, sideTexture, endTexture, sideKey, endKey,
                (face, textureKey, builder) -> {
                    if (activeEmissivity == 0) {
                        return;
                    }
                    if (sideEmissive && textureKey.equals(sideKey)) {
                        builder.emissivity(activeEmissivity, activeEmissivity).ao(false);
                    } else if (endEmissive && textureKey.equals(endKey)) {
                        builder.emissivity(activeEmissivity, activeEmissivity).ao(false);
                    }
                });
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

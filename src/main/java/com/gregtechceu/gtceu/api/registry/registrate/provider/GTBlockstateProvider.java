package com.gregtechceu.gtceu.api.registry.registrate.provider;

import com.gregtechceu.gtceu.api.block.property.GTBlockStateProperties;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.client.util.ExtendedBlockModelRotation;

import com.gregtechceu.gtceu.data.GregTechDatagen;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.providers.RegistrateProvider;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.data.models.blockstates.*;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.ExistingFileHelper;

import com.google.gson.JsonPrimitive;
import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class GTBlockstateProvider extends RegistrateBlockstateProvider {

    // spotless:off
    public static final String Z_ROT_PROPERTY_NAME = "gtceu:z";
    private static final VariantProperty<VariantProperties.Rotation> X_ROT = VariantProperties.X_ROT;
    private static final VariantProperty<VariantProperties.Rotation> Y_ROT = VariantProperties.Y_ROT;
    private static final VariantProperty<VariantProperties.Rotation> Z_ROT = new VariantProperty<>(Z_ROT_PROPERTY_NAME, r -> new JsonPrimitive(r.ordinal() * 90));

    public static final ExistingFileHelper.ResourceType TEXTURE = new ExistingFileHelper.ResourceType(PackType.CLIENT_RESOURCES, ".png", "textures");
    public static final ExistingFileHelper.ResourceType MODEL = new ExistingFileHelper.ResourceType(PackType.CLIENT_RESOURCES, ".json", "models");
    public static final ExistingFileHelper.ResourceType MODEL_WITH_EXTENSION = new ExistingFileHelper.ResourceType(PackType.CLIENT_RESOURCES, "", "models");
    // spotless:on


    private final AbstractRegistrate<?> parent;

    public GTBlockstateProvider(AbstractRegistrate<?> parent, PackOutput packOutput, ExistingFileHelper exFileHelper,
                                Map<ProviderType<?>, RegistrateProvider> existing) {
        super(parent, packOutput, exFileHelper);
        this.parent = parent;
        // replace the default blockstate provider with this one
        existing.put(ProviderType.BLOCKSTATE, this);
    }

    @Override
    protected void registerStatesAndModels() {
        super.registerStatesAndModels();
        parent.genData(GregTechDatagen.BLOCKSTATE_PROVIDER, this);
    }

    public ExistingFileHelper getExistingFileHelper() {
        return this.models().existingFileHelper;
    }

    public MultiVariantGenerator multiVariantGenerator(Block block) {
        var multiVariant = MultiVariantGenerator.multiVariant(block);
        registeredBlocks.put(block, () -> multiVariant.get().getAsJsonObject());
        return multiVariant;
    }

    public MultiVariantGenerator multiVariantGenerator(Block block, Variant baseVariant) {
        var multiVariant = MultiVariantGenerator.multiVariant(block, baseVariant);
        registeredBlocks.put(block, () -> multiVariant.get().getAsJsonObject());
        return multiVariant;
    }

    public static @Nullable PropertyDispatch createFacingDispatch(MachineDefinition definition) {
        return createFacingDispatch(definition.getRotationState(), definition.isAllowExtendedFacing());
    }

    public static @Nullable PropertyDispatch createFacingDispatch(RotationState rotationState,
                                                                  boolean allowExtendedFacing) {
        // doesn't have a rotation property.
        if (rotationState == RotationState.NONE) return null;

        PropertyDispatch dispatch;
        if (!allowExtendedFacing) {
            var disp = PropertyDispatch.property(rotationState.property);

            dispatch = disp.generate((front) -> {
                var orientation = ExtendedBlockModelRotation.get(front, Direction.NORTH);
                return applyOrientation(Variant.variant(), orientation);
            });
        } else {
            var disp = PropertyDispatch.properties(rotationState.property, GTBlockStateProperties.UPWARDS_FACING);

            dispatch = disp.generate((front, up) -> {
                var orientation = ExtendedBlockModelRotation.get(front, up);
                return applyOrientation(Variant.variant(), orientation);
            });
        }
        return dispatch;
    }

    public static Variant applyOrientation(Variant variant, ExtendedBlockModelRotation orientation) {
        return applyRotation(variant, orientation.getAngleX(), orientation.getAngleY(), orientation.getAngleZ());
    }

    public static Variant applyRotation(Variant variant, int angleX, int angleY, int angleZ) {
        angleX = normalizeAngle(angleX);
        angleY = normalizeAngle(angleY);
        angleZ = normalizeAngle(angleZ);

        if (angleX != 0) variant = variant.with(X_ROT, rotationByAngle(angleX));
        if (angleY != 0) variant = variant.with(Y_ROT, rotationByAngle(angleY));
        if (angleZ != 0) variant = variant.with(Z_ROT, rotationByAngle(angleZ));
        return variant;
    }

    private static int normalizeAngle(int angle) {
        return angle - (angle / 360) * 360;
    }

    private static VariantProperties.Rotation rotationByAngle(int angle) {
        return switch (angle) {
            case 0 -> VariantProperties.Rotation.R0;
            case 90 -> VariantProperties.Rotation.R90;
            case 180 -> VariantProperties.Rotation.R180;
            case 270 -> VariantProperties.Rotation.R270;
            default -> throw new IllegalArgumentException("Invalid angle: " + angle);
        };
    }
}

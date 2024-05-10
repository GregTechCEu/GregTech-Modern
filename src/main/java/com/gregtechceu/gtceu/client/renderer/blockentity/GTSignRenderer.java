package com.gregtechceu.gtceu.client.renderer.blockentity;

import com.google.common.collect.ImmutableMap;
import com.gregtechceu.gtceu.common.blockentity.GTSignBlockEntity;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.SignRenderer;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Direction;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class GTSignRenderer implements BlockEntityRenderer<GTSignBlockEntity> {
    public static final int MAX_LINE_WIDTH = 90;
    private static final int LINE_HEIGHT = 10;
    private static final String STICK = "stick";
    private static final int BLACK_TEXT_OUTLINE_COLOR = -988212;
    private static final int OUTLINE_RENDER_DISTANCE = Mth.square(16);
    private final Map signModels;
    private final Font font;

    public GTSignRenderer(BlockEntityRendererProvider.Context context) {
        this.signModels = (Map)WoodType.values().collect(ImmutableMap.toImmutableMap((signType) -> {
            return signType;
        }, (signType) -> {
            return new SignRenderer.SignModel(context.bakeLayer(ModelLayers.createSignModelName(signType)));
        }));
        this.font = context.getFont();
    }

    public void render(GTSignBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        BlockState blockState = blockEntity.getBlockState();
        poseStack.pushPose();
        float f = 0.6666667F;
        WoodType woodType = getWoodType(blockState.getBlock());
        SignRenderer.SignModel signModel = (SignRenderer.SignModel)this.signModels.get(woodType);
        float g;
        if (blockState.getBlock() instanceof StandingSignBlock) {
            poseStack.translate(0.5, 0.5, 0.5);
            g = -((float)(blockState.getValue(StandingSignBlock.ROTATION) * 360) / 16.0F);
            poseStack.mulPose(Vector3f.YP.rotationDegrees(g));
            signModel.stick.visible = true;
        } else {
            poseStack.translate(0.5, 0.5, 0.5);
            g = -blockState.getValue(WallSignBlock.FACING).toYRot();
            poseStack.mulPose(Vector3f.YP.rotationDegrees(g));
            poseStack.translate(0.0, -0.3125, -0.4375);
            signModel.stick.visible = false;
        }

        poseStack.pushPose();
        poseStack.scale(0.6666667F, -0.6666667F, -0.6666667F);
        Material material = Sheets.getSignMaterial(woodType);
        Objects.requireNonNull(signModel);
        VertexConsumer vertexConsumer = material.buffer(bufferSource, signModel::renderType);
        signModel.root.render(poseStack, vertexConsumer, packedLight, packedOverlay);
        poseStack.popPose();
        float h = 0.010416667F;
        poseStack.translate(0.0, 0.3333333432674408, 0.046666666865348816);
        poseStack.scale(0.010416667F, -0.010416667F, 0.010416667F);
        int i = getDarkColor(blockEntity);
        FormattedCharSequence[] formattedCharSequences = blockEntity.getRenderMessages(Minecraft.getInstance().isTextFilteringEnabled(), (text) -> {
            List<FormattedCharSequence> list = this.font.split(text, 90);
            return list.isEmpty() ? FormattedCharSequence.EMPTY : (FormattedCharSequence)list.get(0);
        });
        int k;
        boolean bl;
        int l;
        if (blockEntity.hasGlowingText()) {
            k = blockEntity.getColor().getTextColor();
            bl = isOutlineVisible(blockEntity, k);
            l = 15728880;
        } else {
            k = i;
            bl = false;
            l = packedLight;
        }

        for(int m = 0; m < 4; ++m) {
            FormattedCharSequence formattedCharSequence = formattedCharSequences[m];
            float n = (float)(-this.font.width(formattedCharSequence) / 2);
            if (bl) {
                this.font.drawInBatch8xOutline(formattedCharSequence, n, (float)(m * 10 - 20), k, i, poseStack.last().pose(), bufferSource, l);
            } else {
                this.font.drawInBatch(formattedCharSequence, n, (float)(m * 10 - 20), k, false, poseStack.last().pose(), bufferSource, false, 0, l);
            }
        }

        poseStack.popPose();
    }

    private static boolean isOutlineVisible(GTSignBlockEntity blockEntity, int textColor) {
        if (textColor == DyeColor.BLACK.getTextColor()) {
            return true;
        } else {
            Minecraft minecraft = Minecraft.getInstance();
            LocalPlayer localPlayer = minecraft.player;
            if (localPlayer != null && minecraft.options.getCameraType().isFirstPerson() && localPlayer.isScoping()) {
                return true;
            } else {
                Entity entity = minecraft.getCameraEntity();
                return entity != null && entity.distanceToSqr(Vec3.atCenterOf(blockEntity.getBlockPos())) < (double)OUTLINE_RENDER_DISTANCE;
            }
        }
    }

    private static int getDarkColor(GTSignBlockEntity blockEntity) {
        int i = blockEntity.getColor().getTextColor();
        double d = 0.4;
        int j = (int)((double) NativeImage.getR(i) * 0.4);
        int k = (int)((double)NativeImage.getG(i) * 0.4);
        int l = (int)((double)NativeImage.getB(i) * 0.4);
        return i == DyeColor.BLACK.getTextColor() && blockEntity.hasGlowingText() ? -988212 : NativeImage.combine(0, l, k, j);
    }

    public static WoodType getWoodType(Block block) {
        WoodType woodType;
        if (block instanceof SignBlock) {
            woodType = ((SignBlock)block).type();
        } else {
            woodType = WoodType.OAK;
        }

        return woodType;
    }

    public static SignRenderer.SignModel createSignModel(EntityModelSet entityModelSet, WoodType woodType) {
        return new SignRenderer.SignModel(entityModelSet.bakeLayer(ModelLayers.createSignModelName(woodType)));
    }

    public static LayerDefinition createSignLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        partDefinition.addOrReplaceChild("sign", CubeListBuilder.create().texOffs(0, 0).addBox(-12.0F, -14.0F, -1.0F, 24.0F, 12.0F, 2.0F), PartPose.ZERO);
        partDefinition.addOrReplaceChild("stick", CubeListBuilder.create().texOffs(0, 14).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 14.0F, 2.0F), PartPose.ZERO);
        return LayerDefinition.create(meshDefinition, 64, 32);
    }

    @OnlyIn(Dist.CLIENT)
    public static final class SignModel extends Model {
        public final ModelPart root;
        public final ModelPart stick;

        public SignModel(ModelPart root) {
            super(RenderType::entityCutoutNoCull);
            this.root = root;
            this.stick = root.getChild("stick");
        }

        public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
            this.root.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        }
    }
}

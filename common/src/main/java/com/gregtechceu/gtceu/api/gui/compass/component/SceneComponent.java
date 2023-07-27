package com.gregtechceu.gtceu.api.gui.compass.component;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.compass.ILayoutComponent;
import com.gregtechceu.gtceu.api.gui.compass.LayoutPageWidget;
import com.gregtechceu.gtceu.utils.XmlUtils;
import com.lowdragmc.lowdraglib.client.utils.RenderUtils;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.SceneWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.utils.BlockInfo;
import com.lowdragmc.lowdraglib.utils.TrackedDummyWorld;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author KilaBash
 * @date 2022/10/22
 * @implNote SceneComponent
 */
public class SceneComponent extends AbstractComponent {

    protected final List<Map<BlockPos, BlockInfo>> pages = new ArrayList<>();
    protected final List<Map<BlockPos, String>> hoverInfos = new ArrayList<>();
    protected final List<Object2BooleanMap<BlockPos>> itemInfos = new ArrayList<>();
    private boolean draggable = false;
    private boolean scalable = false;
    private boolean ortho = false;
    private float zoom = -1;
    private float yaw = 25;
    private int height = 150;

    @Override
    public ILayoutComponent fromXml(Element element) {
        super.fromXml(element);
        draggable = XmlUtils.getAsBoolean(element, "draggable", draggable);
        scalable = XmlUtils.getAsBoolean(element, "scalable", scalable);
        ortho = !XmlUtils.getAsString(element, "camera", "ortho").equals("perspective");
        zoom = XmlUtils.getAsFloat(element, "zoom", zoom);
        yaw = XmlUtils.getAsFloat(element, "yaw", yaw);
        height = XmlUtils.getAsInt(element, "height", height);
        NodeList nodeList = element.getChildNodes();
        BlockPos offset = BlockPos.ZERO;
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node instanceof Element page && page.getNodeName().equals("page")) {
                addPage(page, offset);
                offset = offset.offset(500, 0, 500);
            }
        }
        return this;
    }

    private void addPage(Element element, BlockPos offset) {
        Map<BlockPos, BlockInfo> blocks = new HashMap<>();
        Map<BlockPos, String> contents = new HashMap<>();
        Object2BooleanMap<BlockPos> items = new Object2BooleanOpenHashMap<>();
        NodeList nodeList = element.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node instanceof Element block && block.getNodeName().equals("block")) {
                BlockPos pos = XmlUtils.getAsBlockPos(block, "pos", BlockPos.ZERO).offset(offset);
                BlockInfo blockInfo = XmlUtils.getBlockInfo(block);
                boolean itemTips = XmlUtils.getAsBoolean(block, "item-tips", false);
                String content = XmlUtils.getContent(block, true);
                blocks.put(pos, blockInfo);
                contents.put(pos, content);
                items.put(pos, itemTips);
            }

        }
        pages.add(blocks);
        hoverInfos.add(contents);
        itemInfos.add(items);
    }

    @Override
    @Environment(EnvType.CLIENT)
    protected LayoutPageWidget addWidgets(LayoutPageWidget currentPage) {
        if (pages.isEmpty()) return currentPage;
        AtomicInteger pageNum = new AtomicInteger(0);
        WidgetGroup group = new WidgetGroup(0, 0, currentPage.getSize().width, height);
        TrackedDummyWorld world = new TrackedDummyWorld();
        for (Map<BlockPos, BlockInfo> blocks : pages) {
            world.addBlocks(blocks);
        }
        SceneWidget sceneWidget = new SceneWidget(0, 0, currentPage.getSize().width, height, world);
        sceneWidget.setOnAddedTooltips((scene, list) -> {
                    var hoverPosFace = scene.getHoverPosFace();
                    if (hoverPosFace == null) {
                        return;
                    }
                    if (!itemInfos.get(pageNum.get()).getBoolean(hoverPosFace.pos)) {
                        list.clear();
                    }
                    String hover = hoverInfos.get(pageNum.get()).getOrDefault(hoverPosFace.pos, "");
                    if (!hover.isEmpty()) {
                        list.add(Component.literal(hover));
                    }
                })
                .setHoverTips(true)
                .useOrtho(ortho)
                .setOrthoRange(0.5f)
                .setScalable(scalable)
                .setDraggable(draggable)
                .setRenderFacing(false)
                .setRenderSelect(false);

        sceneWidget.getRenderer().setFov(30);
        group.addWidget(sceneWidget);
        sceneWidget.setRenderedCore(pages.stream().flatMap(page -> page.keySet().stream()).toList(), null);

        sceneWidget.getRenderer().setBeforeWorldRender(renderer -> {
            PoseStack matrixStack = new PoseStack();
            matrixStack.pushPose();
            RenderUtils.moveToFace(matrixStack, (minX + maxX) / 2f, minY, (minZ + maxZ) / 2f, Direction.DOWN);
            RenderUtils.rotateToFace(matrixStack, Direction.UP, null);
            int width = (maxX - minX) + 3;
            int height = (maxZ - minZ) + 3;
            new ResourceTexture("thaumcraft:textures/gui/parchment.png").draw(matrixStack, 0, 0, width / -2f, height / -2f, width, height);
            matrixStack.popPose();
        });

        setPage(pages.get(0), sceneWidget);

        ButtonWidget left, right;
        group.addWidget(left = (ButtonWidget) new ButtonWidget(20, height - 16, 12, 7, GuiTextures.BUTTON_LEFT, null).setClientSideWidget());
        group.addWidget(right = (ButtonWidget) new ButtonWidget(currentPage.getSize().width - 20 - 12, height - 16, 12, 7, GuiTextures.BUTTON_RIGHT, null).setClientSideWidget());
        left.setVisible(pageNum.get() - 1 >=0);
        right.setVisible(pageNum.get() + 1 < pages.size());
        left.setOnPressCallback(cd -> {
            if (pageNum.get() - 1 >= 0) {
                setPage(pages.get(pageNum.addAndGet(-1)), sceneWidget);
            }
            left.setVisible(pageNum.get() - 1 >=0);
            right.setVisible(pageNum.get() + 1 < pages.size());
        });
        right.setOnPressCallback(cd -> {
            if (pageNum.get() + 1 < pages.size()) {
                setPage(pages.get(pageNum.addAndGet(1)), sceneWidget);
            }
            left.setVisible(pageNum.get() - 1 >=0);
            right.setVisible(pageNum.get() + 1 < pages.size());
        });

        group.addWidget(new ButtonWidget((currentPage.getSize().width - 24) / 2 + 6, height - 19, 12, 12, GuiTextures.TOOL_IO_FACING_ROTATION, cd -> {
            float current = sceneWidget.getRotationPitch();
            sceneWidget.setCameraYawAndPitchAnima(sceneWidget.getRotationYaw(), current + 90, 20);
        }).setClientSideWidget());

        if (this.hoverInfo != null) {
            group.setHoverTooltips(hoverInfo);
        }

        return currentPage.addStreamWidget(group);
    }

    int minX = Integer.MAX_VALUE;
    int minY = Integer.MAX_VALUE;
    int minZ = Integer.MAX_VALUE;
    int maxX = Integer.MIN_VALUE;
    int maxY = Integer.MIN_VALUE;
    int maxZ = Integer.MIN_VALUE;

    private void setPage(Map<BlockPos, BlockInfo> blocks, SceneWidget sceneWidget) {
        minX = Integer.MAX_VALUE;
        minY = Integer.MAX_VALUE;
        minZ = Integer.MAX_VALUE;
        maxX = Integer.MIN_VALUE;
        maxY = Integer.MIN_VALUE;
        maxZ = Integer.MIN_VALUE;
        for (BlockPos vPos : blocks.keySet()) {
            minX = Math.min(minX, vPos.getX());
            minY = Math.min(minY, vPos.getY());
            minZ = Math.min(minZ, vPos.getZ());
            maxX = Math.max(maxX, vPos.getX());
            maxY = Math.max(maxY, vPos.getY());
            maxY = Math.max(maxY, vPos.getY());
            maxZ = Math.max(maxZ, vPos.getZ());
        }
        var center = new Vector3f((minX + maxX) / 2f + 0.5F, (minY + maxY) / 2f + 0.5F, (minZ + maxZ) / 2f + 0.5F);

        if (zoom > 0) {
            sceneWidget.setZoom(zoom);
        } else {
            sceneWidget.setZoom((float) (6 * Math.sqrt(Math.max(Math.max(Math.max(maxX - minX + 1, maxY - minY + 1), maxZ - minZ + 1), 1))));
        }

        sceneWidget.setCenter(center);
        sceneWidget.setCameraYawAndPitch(yaw, sceneWidget.getRotationPitch());
    }

}

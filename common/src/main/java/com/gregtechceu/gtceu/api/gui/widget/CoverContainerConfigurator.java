package com.gregtechceu.gtceu.api.gui.widget;

import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.cover.IUICover;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.gregtechceu.gtceu.api.item.component.IItemComponent;
import com.gregtechceu.gtceu.common.item.CoverPlaceBehavior;
import com.lowdragmc.lowdraglib.client.scene.ISceneRenderHook;
import com.lowdragmc.lowdraglib.gui.animation.Animation;
import com.lowdragmc.lowdraglib.gui.animation.Transform;
import com.lowdragmc.lowdraglib.gui.editor.ColorPattern;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.widget.*;
import com.lowdragmc.lowdraglib.misc.ItemStackTransfer;
import com.lowdragmc.lowdraglib.utils.Position;
import com.lowdragmc.lowdraglib.utils.Size;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.List;

/**
 * @author KilaBash
 * @date 2023/6/29
 * @implNote CoverContainerConfigurator
 */
public class CoverContainerConfigurator extends WidgetGroup {
    protected final ICoverable coverable;
    protected final ItemStackTransfer transfer;
    protected SceneWidget sceneWidget;
    protected SlotWidget slotWidget;
    protected ImageWidget background;
    // runtime
    @Nullable
    protected Direction side;
    @Nullable
    protected CoverBehavior coverBehavior;
    @Nullable
    protected Widget coverConfigurator;
    private boolean needUpdate;

    public CoverContainerConfigurator(ICoverable coverable) {
        super(0, 0, 120, 80);
        this.coverable = coverable;
        this.transfer = new ItemStackTransfer() {
            @Override
            public int getSlotLimit(int slot) {
                return 1;
            }
        };
        this.transfer.setFilter(itemStack -> itemStack.isEmpty() || this.side != null &&
                CoverPlaceBehavior.isCoverBehaviorItem(itemStack, () -> false, coverDef -> ICoverable.canPlaceCover(coverDef, this.coverable)));
    }

    @Override
    public void initWidget() {
        super.initWidget();
        addWidget(background = new ImageWidget(0, 0, 120, 80, GuiTextures.BACKGROUND_INVERSE));
        sceneWidget = new SceneWidget(4, 4, 120 - 8, 80 - 8, coverable.getLevel())
                .setRenderedCore(List.of(coverable.getPos()), null)
                .setRenderSelect(false)
                .setOnSelected(this::onSideSelected);
        if (isRemote()) {
            sceneWidget.getRenderer().addRenderedBlocks(
                    List.of(coverable.getPos().above(),
                            coverable.getPos().below(),
                            coverable.getPos().north(),
                            coverable.getPos().south(),
                            coverable.getPos().east(),
                            coverable.getPos().west()
                    ), new ISceneRenderHook() {
                        @Override
                        @Environment(EnvType.CLIENT)
                        public void apply(boolean isTESR, RenderType layer) {
                            RenderSystem.enableBlend();
                            RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
                        }
                    });

            var playerRotation = gui.entityPlayer.getRotationVector();
            sceneWidget.setCameraYawAndPitch(playerRotation.x, playerRotation.y - 90);
        }
        addWidget(sceneWidget.setBackground(ColorPattern.BLACK.rectTexture()));
        addWidget(slotWidget = new SlotWidget(transfer, 0, 4, 80 - 4 - 18)
                .setChangeListener(this::coverRemoved)
                .setBackgroundTexture(new GuiTextureGroup(GuiTextures.SLOT, GuiTextures.FILTER_SLOT_OVERLAY)));
        slotWidget.setVisible(false);
        slotWidget.setActive(false);
    }

    private void coverRemoved() {
        if (getGui().entityPlayer instanceof ServerPlayer serverPlayer && side != null) {
            var item = transfer.getStackInSlot(0);
            if (coverable.getCoverAtSide(side) != null) {
                coverable.removeCover(false, side, serverPlayer);
            }
            if (!item.isEmpty() && coverable.getCoverAtSide(side) == null) {
                if (item.getItem() instanceof ComponentItem componentItem) {
                    for (IItemComponent component : componentItem.getComponents()) {
                        if (component instanceof CoverPlaceBehavior placeBehavior) {
                            coverable.placeCoverOnSide(side, item, placeBehavior.coverDefinition(), serverPlayer);
                        }
                    }
                }
            }
        }
    }

    private void onSideSelected(BlockPos blockPos, Direction direction) {
        if (blockPos.equals(coverable.getPos())) {
            if (side != direction) {
                side = direction;
                slotWidget.setActive(true);
                slotWidget.setVisible(true);
                checkCoverBehaviour();
            }
        }
    }

    public boolean checkCoverBehaviour() {
        if (side != null) {
            var coverBehaviour = coverable.getCoverAtSide(side);
            if (coverBehaviour != this.coverBehavior) {
                this.coverBehavior = coverBehaviour;
                var attachItem = coverBehaviour == null ? ItemStack.EMPTY : coverBehaviour.getAttachItem();
                transfer.setStackInSlot(0, attachItem);
                transfer.onContentsChanged(0);
                updateCoverConfigurator();
                return true;
            }
        }
        return false;
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if (side != null) {
            if (checkCoverBehaviour()) {
                writeClientAction(-2, buf -> {});
            }
        }
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        if (side != null && needUpdate) {
            if (checkCoverBehaviour()) {
                needUpdate = false;
            }
        }
    }

    @Override
    public void handleClientAction(int id, FriendlyByteBuf buffer) {
        if (id == -2) {
            needUpdate = true;
        } else {
            super.handleClientAction(id, buffer);
        }
    }

    protected void updateCoverConfigurator() {
        if (coverConfigurator != null) {
            removeWidgetAnima(coverConfigurator, new Transform()
                    .offset(-coverConfigurator.getSize().width / 2, -coverConfigurator.getSize().height / 2)
                    .scale(0)
                    .duration(300));
            coverConfigurator = null;
            sceneWidget.animation(new Animation()
                    .duration(300)
                    .size(new Size(120 - 8, 80 - 8)));
            setSize(new Size(120, 80));
            background.animation(new Animation()
                    .duration(300)
                    .size(getSize()));
        }
        if (side != null) {
            if (coverable.getCoverAtSide(side) instanceof IUICover iuiCover) {
                coverConfigurator = iuiCover.createUIWidget();
                coverConfigurator.setBackground(GuiTextures.BACKGROUND);
                coverConfigurator.setSelfPosition(new Position(4, 80 - 4));
                addWidgetAnima(coverConfigurator, new Transform()
                        .offset(-coverConfigurator.getSize().width / 2, -coverConfigurator.getSize().height / 2)
                        .scale(0)
                        .duration(300));
                sceneWidget.animation(new Animation()
                        .duration(300)
                        .size(new Size(Math.max(120, coverConfigurator.getSize().width + 8) - 8, 80 - 8)));
                setSize(new Size(Math.max(120, coverConfigurator.getSize().width + 8), Math.max(80, 80 + coverConfigurator.getSize().height)));
                background.animation(new Animation()
                        .duration(300)
                        .size(getSize()));
            }
        }
    }
}

package com.gregtechceu.gtceu.common.cover;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.cover.IMuiCover;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.IDataStickInteractable;
import com.gregtechceu.gtceu.api.mui.factory.SidedPosGuiData;
import com.gregtechceu.gtceu.api.mui.value.sync.PanelSyncManager;
import com.gregtechceu.gtceu.api.mui.value.sync.SyncHandlers;
import com.gregtechceu.gtceu.api.mui.widget.ParentWidget;
import com.gregtechceu.gtceu.api.placeholder.IPlaceholderInfoProviderCover;
import com.gregtechceu.gtceu.api.placeholder.MultiLineComponent;
import com.gregtechceu.gtceu.api.placeholder.PlaceholderContext;
import com.gregtechceu.gtceu.api.placeholder.PlaceholderHandler;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gregtechceu.gtceu.client.mui.screen.UISettings;
import com.gregtechceu.gtceu.client.renderer.cover.CoverTextRenderer;
import com.gregtechceu.gtceu.client.renderer.cover.IDynamicCoverRenderer;
import com.gregtechceu.gtceu.integration.create.GTCreateIntegration;
import com.gregtechceu.gtceu.utils.GTUtil;

import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.function.Supplier;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ComputerMonitorCover extends CoverBehavior
                                  implements IDataStickInteractable, IPlaceholderInfoProviderCover, IMuiCover {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(ComputerMonitorCover.class,
            CoverBehavior.MANAGED_FIELD_HOLDER);

    private TickableSubscription subscription;

    private final CoverTextRenderer renderer;

    @Persisted
    @Getter
    private final List<String> formatStringArgs = new ArrayList<>(8);

    @Persisted
    @Getter
    private final List<String> formatStringLines = new ArrayList<>(8);

    @Persisted
    @Getter
    @Setter
    private String placeholderText = "";

    @Persisted
    @Getter
    @Setter
    private boolean paused = false;

    @Persisted
    @Getter
    @Setter
    private double scale = 1;

    @Persisted
    @DescSynced
    @Getter
    private List<MutableComponent> text = new ArrayList<>();

    @Persisted
    public final CustomItemStackHandler itemStackHandler = new CustomItemStackHandler(8);

    @Setter
    @Getter
    @Persisted
    private int updateInterval = 100;

    @Persisted
    @Getter
    private final List<MutableComponent> createDisplayTargetBuffer = new ArrayList<>();

    @Persisted
    @Getter
    private final UUID placeholderUUID;

    public ComputerMonitorCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide) {
        super(definition, coverHolder, attachedSide);
        renderer = new CoverTextRenderer(this::getText, this::getScale);
        placeholderUUID = UUID.randomUUID();
        for (int i = 0; i < 100; i++) createDisplayTargetBuffer.add(MutableComponent.create(ComponentContents.EMPTY));
    }

    public List<MutableComponent> getRenderedText() {
        return PlaceholderHandler.processPlaceholders(
                placeholderText,
                getPlaceholderContext());
    }

    public void setDisplayTargetBufferLine(int line, MutableComponent component) {
        createDisplayTargetBuffer.set(line, component);
    }

    @Override
    public boolean canPipePassThrough() {
        return false;
    }

    @Override
    public Supplier<IDynamicCoverRenderer> getDynamicRenderer() {
        return () -> renderer;
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        subscription = coverHolder.subscribeServerTick(subscription, () -> {
            if (!this.paused) this.update();
        });
    }

    @Override
    public long getTicksSincePlaced() {
        return coverHolder.getOffsetTimer();
    }

    private void update() {
        if (coverHolder.getOffsetTimer() % updateInterval == 0) {
            try {
                if (GTCEu.Mods.isCreateLoaded())
                    GTCreateIntegration.TemporaryRedstoneLinkTransmitter.destroyAll();
                setRedstoneSignalOutput(0);
                text = getRenderedText();
            } catch (RuntimeException e) {
                text = GTUtil
                        .list(Component.translatable("gtceu.computer_monitor_cover.error.exception", e.getMessage()));
            }
        }
    }

    @Override
    public void onRemoved() {
        super.onRemoved();
        if (subscription != null) {
            subscription.unsubscribe();
        }
    }

    @Override
    public boolean canConnectRedstone() {
        return true;
    }

    @Override
    public List<ItemStack> getAdditionalDrops() {
        List<ItemStack> drops = super.getAdditionalDrops();
        for (int i = 0; i < 8; i++) {
            if (!itemStackHandler.getStackInSlot(i).isEmpty())
                drops.add(itemStackHandler.getStackInSlot(i));
        }
        return drops;
    }

    @Override
    public InteractionResult onDataStickUse(Player player, ItemStack dataStick) {
        CompoundTag tag = dataStick.getTagElement("computer_monitor_cover_config");
        if (tag == null) return InteractionResult.FAIL;
        placeholderText = tag.getString("code");
        updateInterval = tag.getInt("updateInterval");
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult onDataStickShiftUse(Player player, ItemStack dataStick) {
        CompoundTag tag = dataStick.getOrCreateTagElement("computer_monitor_cover_config");
        tag.putString("code", placeholderText);
        tag.putInt("updateInterval", updateInterval);
        return InteractionResult.SUCCESS;
    }

    @Override
    public ParentWidget<?> createCoverUI(SidedPosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        return PlaceholderHandler.createPlaceholderEditor(
                syncManager,
                getPlaceholderContext(),
                SyncHandlers.string(this::getPlaceholderText, this::setPlaceholderText),
                SyncHandlers.doubleNumber(this::getScale, this::setScale),
                SyncHandlers.intNumber(this::getUpdateInterval, this::setUpdateInterval),
                SyncHandlers.bool(this::isPaused, this::setPaused),
                this::update);
    }

    private PlaceholderContext getPlaceholderContext() {
        return new PlaceholderContext(coverHolder.getLevel(), coverHolder.getPos(), attachedSide, itemStackHandler,
                this, new MultiLineComponent(text), placeholderUUID);
    }
}

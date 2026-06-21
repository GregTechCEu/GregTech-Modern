package com.gregtechceu.gtceu.common.cover;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.cover.IMuiCover;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.IDataStickInteractable;
import com.gregtechceu.gtceu.api.placeholder.IPlaceholderInfoProviderCover;
import com.gregtechceu.gtceu.api.placeholder.MultiLineComponent;
import com.gregtechceu.gtceu.api.placeholder.PlaceholderContext;
import com.gregtechceu.gtceu.api.placeholder.PlaceholderHandler;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gregtechceu.gtceu.client.renderer.cover.CoverTextRenderer;
import com.gregtechceu.gtceu.client.renderer.cover.IDynamicCoverRenderer;
import com.gregtechceu.gtceu.common.mui.GTMuiWidgets;
import com.gregtechceu.gtceu.integration.create.GTCreateIntegration;
import com.gregtechceu.gtceu.utils.GTStringUtils;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import brachy.modularui.api.IPanelHandler;
import brachy.modularui.factory.SidedPosGuiData;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.value.sync.SyncHandlers;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ComputerMonitorCover extends CoverBehavior
                                  implements IDataStickInteractable, IPlaceholderInfoProviderCover, IMuiCover {

    private @Nullable TickableSubscription subscription;
    private final CoverTextRenderer renderer;
    @SaveField
    @Getter
    private List<String> formatStringArgs = new ArrayList<>(8);
    @SaveField
    @Getter
    private List<String> formatStringLines = new ArrayList<>(8);
    @SaveField
    @SyncToClient
    @Getter
    private List<MutableComponent> text = new ArrayList<>();
    @SaveField
    public CustomItemStackHandler itemStackHandler = new CustomItemStackHandler(8);
    @Setter
    @Getter
    @SaveField
    private int updateInterval = 100;
    @Getter
    @SaveField
    private long ticksSincePlaced = 0;
    @SaveField
    @Getter
    private List<MutableComponent> createDisplayTargetBuffer = new ArrayList<>();
    @SaveField
    @Getter
    private List<MutableComponent> computerCraftTextBuffer = new ArrayList<>();
    @SaveField
    @Getter
    private UUID placeholderUUID;

    public ComputerMonitorCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide) {
        super(definition, coverHolder, attachedSide);
        renderer = new CoverTextRenderer(this::getText);
        placeholderUUID = UUID.randomUUID();
        for (int i = 0; i < 100; i++) {
            createDisplayTargetBuffer.add(MutableComponent.create(ComponentContents.EMPTY));
            computerCraftTextBuffer.add(MutableComponent.create(ComponentContents.EMPTY));
        }
    }

    public PlaceholderContext createPlaceholderContext() {
        return new PlaceholderContext(coverHolder.getLevel(), coverHolder.getBlockPos(), attachedSide,
                itemStackHandler,
                this, null, new MultiLineComponent(text), placeholderUUID);
    }

    public String getCode() {
        return formatStringLines.stream().reduce((a, b) -> a + "\n" + b).orElse("");
    }

    public void setCode(String code) {
        formatStringLines.clear();
        formatStringLines.addAll(List.of(code.split("\n")));
    }

    public List<MutableComponent> getRenderedText() {
        List<String> tmp = new ArrayList<>(formatStringArgs);
        tmp = tmp.stream().map(str -> '{' + str + '}').toList();
        return PlaceholderHandler.processPlaceholders(
                GTStringUtils.replace(getCode(), "\\{}", tmp), createPlaceholderContext());
    }

    public void setDisplayTargetBufferLine(int line, MutableComponent component) {
        createDisplayTargetBuffer.set(line, component);
    }

    @Override
    public void setComputerCraftTextBufferLine(int line, MutableComponent component) {
        computerCraftTextBuffer.set(line, component);
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
    public void onLoad() {
        super.onLoad();
        subscription = coverHolder.subscribeServerTick(subscription, this::update);
    }

    private void update() {
        ticksSincePlaced++;
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
            syncDataHolder.markClientSyncFieldDirty("text");
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
        List<String> stringLines = new ArrayList<>();
        ListTag stringLinesTag = tag.getList("lines", Tag.TAG_STRING);
        for (int i = 0; i < stringLinesTag.size(); i++) stringLines.add(stringLinesTag.getString(i));
        formatStringLines.clear();
        formatStringLines.addAll(stringLines);
        List<String> stringArgs = new ArrayList<>();
        ListTag stringArgsTag = tag.getList("args", Tag.TAG_STRING);
        for (int i = 0; i < stringArgsTag.size(); i++) stringArgs.add(stringArgsTag.getString(i));
        formatStringArgs.clear();
        formatStringArgs.addAll(stringArgs);
        updateInterval = tag.getInt("updateInterval");
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult onDataStickShiftUse(Player player, ItemStack dataStick) {
        CompoundTag tag = dataStick.getOrCreateTagElement("computer_monitor_cover_config");
        ListTag stringLinesTag = new ListTag();
        formatStringLines.forEach(line -> stringLinesTag.add(StringTag.valueOf(line)));
        tag.put("lines", stringLinesTag);
        ListTag stringArgsTag = new ListTag();
        formatStringArgs.forEach(line -> stringArgsTag.add(StringTag.valueOf(line)));
        tag.put("args", stringArgsTag);
        tag.putInt("updateInterval", updateInterval);
        return InteractionResult.SUCCESS;
    }

    @Override
    public ModularPanel<?> buildUI(SidedPosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        var codeSync = SyncHandlers.string(this::getCode, this::setCode).allowC2S();
        var intervalSync = SyncHandlers.intNumber(this::getUpdateInterval, this::setUpdateInterval).allowC2S();
        IPanelHandler helpPanel = syncManager.syncedPanel("placeholder_language_help",
                true,
                (syncManager1, panelHandler1) -> PlaceholderHandler.createHelpPanel());
        return PlaceholderHandler.createPlaceholderEditorPanel(
                "main", createPlaceholderContext(),
                codeSync, null, intervalSync, null, helpPanel, null)
                .child(GTMuiWidgets.verticalPlayerInventory((index, slot) -> slot)
                        .verticalCenter()
                        .left(-80));
    }
}

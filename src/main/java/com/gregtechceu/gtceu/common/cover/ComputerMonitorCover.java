package com.gregtechceu.gtceu.common.cover;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.cover.IUICover;
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
import com.gregtechceu.gtceu.common.data.item.GTDataComponents;
import com.gregtechceu.gtceu.common.item.datacomponents.ComputerMonitorConfig;
import com.gregtechceu.gtceu.integration.create.GTCreateIntegration;
import com.gregtechceu.gtceu.utils.GTStringUtils;
import com.gregtechceu.gtceu.utils.GTUtil;

import com.lowdragmc.lowdraglib.gui.widget.Widget;

import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.function.Supplier;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ComputerMonitorCover extends CoverBehavior
                                  implements IUICover, IDataStickInteractable, IPlaceholderInfoProviderCover {

    private TickableSubscription subscription;
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
    private String placeholderSearch = "";
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
            createDisplayTargetBuffer.add(Component.empty());
            computerCraftTextBuffer.add(Component.empty());
        }
    }

    public List<MutableComponent> getRenderedText() {
        String s = formatStringLines.stream().reduce((a, b) -> a + "\n" + b).orElse("");
        List<String> tmp = new ArrayList<>(formatStringArgs);
        tmp = tmp.stream().map(str -> '{' + str + '}').toList();
        return PlaceholderHandler.processPlaceholders(
                GTStringUtils.replace(s, "\\{}", tmp),
                new PlaceholderContext(coverHolder.getLevel(), coverHolder.getBlockPos(), attachedSide,
                        itemStackHandler,
                        this, new MultiLineComponent(text), placeholderUUID));
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
    public Widget createUIWidget() {
        return ComputerMonitorCoverUI.createUIWidget(this);
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
                text = GTUtil.list(
                        Component.translatable("gtceu.computer_monitor_cover.error.exception", e.getMessage()));
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
            if (!itemStackHandler.getStackInSlot(i).isEmpty()) {
                drops.add(itemStackHandler.getStackInSlot(i));
            }
        }
        return drops;
    }

    @Override
    public InteractionResult onDataStickUse(Player player, ItemStack dataStick) {
        ComputerMonitorConfig config = dataStick.get(GTDataComponents.COMPUTER_MONITOR_CONFIG);
        if (config == null) return InteractionResult.FAIL;

        formatStringLines.clear();
        formatStringLines.addAll(config.lines());

        formatStringArgs.clear();
        formatStringArgs.addAll(config.args());
        updateInterval = config.updateInterval();
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult onDataStickShiftUse(Player player, ItemStack dataStick) {
        dataStick.set(GTDataComponents.COMPUTER_MONITOR_CONFIG,
                new ComputerMonitorConfig(formatStringLines, formatStringArgs, updateInterval));
        return InteractionResult.SUCCESS;
    }
}

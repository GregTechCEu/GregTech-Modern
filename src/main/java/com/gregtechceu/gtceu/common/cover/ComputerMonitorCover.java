package com.gregtechceu.gtceu.common.cover;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.cover.IUICover;
import com.gregtechceu.gtceu.api.gui.widget.IntInputWidget;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.IDataStickInteractable;
import com.gregtechceu.gtceu.api.placeholder.IPlaceholderInfoProviderCover;
import com.gregtechceu.gtceu.api.placeholder.MultiLineComponent;
import com.gregtechceu.gtceu.api.placeholder.PlaceholderContext;
import com.gregtechceu.gtceu.api.placeholder.PlaceholderHandler;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gregtechceu.gtceu.client.renderer.cover.CoverTextRenderer;
import com.gregtechceu.gtceu.client.renderer.cover.IDynamicCoverRenderer;
import com.gregtechceu.gtceu.data.lang.LangHandler;
import com.gregtechceu.gtceu.integration.create.GTCreateIntegration;
import com.gregtechceu.gtceu.utils.GTStringUtils;
import com.gregtechceu.gtceu.utils.GTUtil;

import com.lowdragmc.lowdraglib.gui.texture.ResourceBorderTexture;
import com.lowdragmc.lowdraglib.gui.widget.*;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

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

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Supplier;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ComputerMonitorCover extends CoverBehavior
                                  implements IUICover, IDataStickInteractable, IPlaceholderInfoProviderCover {

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
    @DescSynced
    @Getter
    private List<MutableComponent> text = new ArrayList<>();
    @Persisted
    public final CustomItemStackHandler itemStackHandler = new CustomItemStackHandler(8);
    @Setter
    private String placeholderSearch = "";
    @Setter
    @Getter
    @Persisted
    private int updateInterval = 100;
    @Getter
    @Persisted
    private long ticksSincePlaced = 0;
    @Persisted
    @Getter
    private final List<MutableComponent> createDisplayTargetBuffer = new ArrayList<>();
    @Persisted
    @Getter
    private final List<MutableComponent> computerCraftTextBuffer = new ArrayList<>();
    @Persisted
    @Getter
    private final UUID placeholderUUID;

    public ComputerMonitorCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide) {
        super(definition, coverHolder, attachedSide);
        renderer = new CoverTextRenderer(this::getText);
        placeholderUUID = UUID.randomUUID();
        for (int i = 0; i < 100; i++) {
            createDisplayTargetBuffer.add(MutableComponent.create(ComponentContents.EMPTY));
            computerCraftTextBuffer.add(MutableComponent.create(ComponentContents.EMPTY));
        }
    }

    public List<MutableComponent> getRenderedText() {
        String s = formatStringLines.stream().reduce((a, b) -> a + "\n" + b).orElse("");
        List<String> tmp = new ArrayList<>(formatStringArgs);
        tmp = tmp.stream().map(str -> '{' + str + '}').toList();
        return PlaceholderHandler.processPlaceholders(
                GTStringUtils.replace(s, "\\{}", tmp),
                new PlaceholderContext(coverHolder.getLevel(), coverHolder.getPos(), attachedSide, itemStackHandler,
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
    public @NotNull ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public Widget createUIWidget() {
        int textFieldWidth = 160, horizontalPadding = 10, verticalPadding = 2;
        final WidgetGroup group = new WidgetGroup(0, 0, 2 * textFieldWidth + 3 * horizontalPadding, 150);
        final WidgetGroup mainPage = new WidgetGroup(0, 0, 2 * textFieldWidth + 3 * horizontalPadding, 150);
        final WidgetGroup formatStringArgsPage = new WidgetGroup(0, 0, 2 * textFieldWidth + 3 * horizontalPadding, 150);
        for (int i = 0; i < 8; i++) {
            TextFieldWidget formatStringInput = new TextFieldWidget();
            formatStringInput.setSize(textFieldWidth, 15);
            formatStringInput.setSelfPosition(horizontalPadding + textFieldWidth / 2,
                    10 + verticalPadding + i * (15 + verticalPadding));
            formatStringInput.setHoverTooltips(GTStringUtils.toImmutable(
                    LangHandler.getMultiLang("gtceu.gui.computer_monitor_cover.main_textbox_tooltip", i + 1)));
            int finalI = i;
            if (i >= formatStringLines.size()) formatStringLines.add("");
            formatStringInput.setCurrentString(formatStringLines.get(i));
            formatStringInput.setTextResponder((s) -> formatStringLines.set(finalI, s));
            mainPage.addWidget(formatStringInput);
            SlotWidget slot = new com.gregtechceu.gtceu.api.gui.widget.SlotWidget(
                    itemStackHandler,
                    i,
                    horizontalPadding + 50,
                    20 * i);
            slot.setBackgroundTexture(SlotWidget.ITEM_SLOT_TEXTURE);
            slot.setHoverTooltips(GTStringUtils
                    .toImmutable(LangHandler.getMultiLang("gtceu.gui.computer_monitor_cover.slot_tooltip", i + 1)));
            mainPage.addWidget(slot);
        }
        for (int i = 0; i < 8; i++) {
            TextFieldWidget formatStringArgsInput = new TextFieldWidget();
            formatStringArgsInput.setSize(textFieldWidth, 15);
            formatStringArgsInput.setSelfPosition(textFieldWidth / 2 + horizontalPadding,
                    10 + verticalPadding + i * (15 + verticalPadding));
            formatStringArgsInput.setHoverTooltips(GTStringUtils.toImmutable(
                    LangHandler.getMultiLang("gtceu.gui.computer_monitor_cover.second_page_textbox_tooltip",
                            GTStringUtils.getIntOrderingSuffix(i + 1))));

            int finalI = i;
            if (i >= formatStringArgs.size()) formatStringArgs.add("");
            formatStringArgsInput.setCurrentString(formatStringArgs.get(i));
            formatStringArgsInput.setTextResponder((s) -> formatStringArgs.set(finalI, s));
            formatStringArgsPage.addWidget(formatStringArgsInput);
        }
        ButtonWidget switchToFormatStringArgsPageButton = new ButtonWidget(
                horizontalPadding + 50,
                10 * (15 + verticalPadding) + verticalPadding,
                20, 20,
                new ResourceBorderTexture(),
                clickData -> {
                    group.clearAllWidgets();
                    group.addWidget(formatStringArgsPage);
                });
        ButtonWidget switchBack = new ButtonWidget(
                horizontalPadding + 50,
                10 * (15 + verticalPadding) + verticalPadding,
                20, 20,
                new ResourceBorderTexture(),
                clickData -> {
                    group.clearAllWidgets();
                    group.addWidget(mainPage);
                });
        mainPage.addWidget(PlaceholderHandler.getPlaceholderHandlerUI(""));
        // TextFieldWidget searchBox = new TextFieldWidget(280, 0, 80, 15, null, onSearch);
        // searchBox.setHoverTooltips("Search");
        // mainPage.addWidget(searchBox);
        IntInputWidget updateIntervalInput = new IntInputWidget(0, 0, 60, 20, this::getUpdateInterval,
                this::setUpdateInterval);
        updateIntervalInput.setMin(1);
        updateIntervalInput.setMax(60 * 20);
        updateIntervalInput
                .setHoverTooltips(Component.translatable("gtceu.gui.computer_monitor_cover.update_interval"));
        mainPage.addWidget(updateIntervalInput);
        switchToFormatStringArgsPageButton
                .setHoverTooltips(Component.translatable("gtceu.gui.computer_monitor_cover.edit_blank_placeholders"));
        switchBack.setHoverTooltips(Component.translatable("gtceu.gui.computer_monitor_cover.edit_displayed_text"));
        mainPage.addWidget(switchToFormatStringArgsPageButton);
        formatStringArgsPage.addWidget(switchBack);
        group.addWidget(mainPage);
        return group;
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
}

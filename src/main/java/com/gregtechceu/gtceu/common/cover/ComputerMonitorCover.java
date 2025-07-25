package com.gregtechceu.gtceu.common.cover;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.capability.IWorkable;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.cover.IUICover;
import com.gregtechceu.gtceu.api.cover.filter.ItemFilter;
import com.gregtechceu.gtceu.api.gui.widget.IntInputWidget;
import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.gregtechceu.gtceu.api.item.component.IDataItem;
import com.gregtechceu.gtceu.api.item.component.IItemComponent;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMaintenanceMachine;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gregtechceu.gtceu.client.renderer.cover.CoverTextRenderer;
import com.gregtechceu.gtceu.client.renderer.cover.IDynamicCoverRenderer;
import com.gregtechceu.gtceu.common.blockentity.CableBlockEntity;
import com.gregtechceu.gtceu.data.lang.LangHandler;
import com.gregtechceu.gtceu.integration.create.GTCreateIntegration;
import com.gregtechceu.gtceu.utils.GTStringUtils;
import com.gregtechceu.gtceu.utils.GTUtil;

import com.lowdragmc.lowdraglib.gui.texture.ResourceBorderTexture;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.*;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ForgeRegistries;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ComputerMonitorCover extends CoverBehavior implements IUICover {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(ComputerMonitorCover.class,
            CoverBehavior.MANAGED_FIELD_HOLDER);

    private static final Map<String, BiFunction<ComputerMonitorCover, List<List<MutableComponent>>, List<MutableComponent>>> PLACEHOLDERS = new HashMap<>();

    private TickableSubscription subscription;
    private final CoverTextRenderer renderer;
    @Persisted
    private final List<String> formatStringArgs = new ArrayList<>(8);
    @Persisted
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

    public ComputerMonitorCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide) {
        super(definition, coverHolder, attachedSide);
        renderer = new CoverTextRenderer(this::getText);
        for (int i = 0; i < 100; i++) createDisplayTargetBuffer.add(MutableComponent.create(ComponentContents.EMPTY));
    }

    public boolean placeholderExists(List<MutableComponent> placeholder) {
        return PLACEHOLDERS.containsKey(GTStringUtils.componentsToString(placeholder));
    }

    public @Nullable List<MutableComponent> processPlaceholder(List<List<MutableComponent>> placeholder) {
        if (!placeholderExists(placeholder.get(0))) return null;
        return PLACEHOLDERS.get(GTStringUtils.componentsToString(placeholder.get(0))).apply(this,
                placeholder.subList(1, placeholder.size()));
    }

    public List<MutableComponent> getRenderedText() {
        List<MutableComponent> out = GTStringUtils.literalLine("");
        Stack<List<List<MutableComponent>>> incompletePlaceholders = new Stack<>();
        int formatStringArgsIndex = 0;
        StringBuilder formatString = new StringBuilder();
        formatStringLines.forEach((line) -> formatString.append(line).append("\n"));
        StringBuilder tmp = new StringBuilder();
        boolean left_bracket = false;
        boolean escaped = false;
        for (char c : formatString.toString().toCharArray()) {
            if (escaped) {
                tmp.append(c);
                escaped = false;
                left_bracket = false;
            } else if (left_bracket && c == '}') {
                if (formatStringArgsIndex >= formatStringArgs.size()) continue;
                tmp.append('{').append(formatStringArgs.get(formatStringArgsIndex)).append('}');
                formatStringArgsIndex++;
                left_bracket = false;
            } else if (left_bracket && c == '{') tmp.append(c);
            else if (c == '{') left_bracket = true;
            else if (c == '\\') {
                tmp.append(c);
                escaped = true;
            } else {
                if (left_bracket) tmp.append('{');
                tmp.append(c);
                left_bracket = false;
            }
        }
        if (left_bracket) tmp.append('{');
        if (escaped) tmp.append('\\');
        escaped = false;
        for (char c : tmp.toString().toCharArray()) {
            if (c == '\\') {
                if (escaped) {
                    if (incompletePlaceholders.isEmpty()) GTStringUtils.append(out, c);
                    else GTStringUtils.append(GTUtil.getLast(incompletePlaceholders.peek()), c);
                    escaped = false;
                } else escaped = true;
            } else if (escaped) {
                if (c == 'n') {
                    if (incompletePlaceholders.isEmpty()) out.add(MutableComponent.create(ComponentContents.EMPTY));
                    else GTUtil.getLast(incompletePlaceholders.peek())
                            .add(MutableComponent.create(ComponentContents.EMPTY));
                } else if (incompletePlaceholders.isEmpty()) GTStringUtils.append(out, c);
                else GTStringUtils.append(GTUtil.getLast(incompletePlaceholders.peek()), c);
                escaped = false;
            } else if (c == ' ') {
                if (incompletePlaceholders.isEmpty()) GTStringUtils.append(out, c);
                else incompletePlaceholders.peek().add(GTStringUtils.literalLine(""));
            } else if (c == '{') incompletePlaceholders.push(new ArrayList<>(List.of(GTStringUtils.literalLine(""))));
            else if (c == '}') {
                if (incompletePlaceholders.isEmpty())
                    return GTUtil.list(Component.translatable("gtceu.computer_monitor_cover.error.unexpected_bracket"));
                if (incompletePlaceholders.peek().isEmpty()) {
                    incompletePlaceholders.pop();
                } else if (placeholderExists(incompletePlaceholders.peek().get(0))) {
                    List<MutableComponent> placeholderString = processPlaceholder(incompletePlaceholders.pop());
                    if (incompletePlaceholders.isEmpty()) GTStringUtils.append(out, placeholderString);
                    else GTStringUtils.append(GTUtil.getLast(incompletePlaceholders.peek()), placeholderString);
                } else {
                    return GTUtil.list(Component.translatable("gtceu.computer_monitor_cover.error.no_placeholder",
                            GTStringUtils.componentsToString(incompletePlaceholders.peek().get(0))));
                }
            } else if (c == '\n') {
                if (incompletePlaceholders.isEmpty()) out.add(MutableComponent.create(ComponentContents.EMPTY));
                else GTUtil.getLast(incompletePlaceholders.peek())
                        .add(MutableComponent.create(ComponentContents.EMPTY));
            } else {
                if (incompletePlaceholders.isEmpty()) GTStringUtils.append(out, c);
                else GTStringUtils.append(GTUtil.getLast(incompletePlaceholders.peek()), c);
            }
        }
        if (incompletePlaceholders.isEmpty())
            return out;
        return GTUtil.list(Component.translatable("gtceu.computer_monitor_cover.error.unclosed_bracket"));
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
        DraggableScrollableWidgetGroup placeholderReference = new DraggableScrollableWidgetGroup(280, 15, 100, 200);
        Consumer<String> onSearch = (newSearch) -> {
            setPlaceholderSearch(newSearch);
            placeholderReference.clearAllWidgets();
            int y = verticalPadding;
            ArrayList<String> placeholders = new ArrayList<>(PLACEHOLDERS.keySet().stream().toList());
            placeholders.removeIf(s -> s == null || !s.contains(placeholderSearch));
            placeholders.sort(String::compareTo);
            for (String placeholder : placeholders) {
                TextTextureWidget placeholderName = new TextTextureWidget(0, y, 80, 15, placeholder);
                placeholderName.getTextTexture().type = TextTexture.TextType.LEFT;
                placeholderName.setHoverTooltips(GTStringUtils
                        .toImmutable(LangHandler.getSingleOrMultiLang("gtceu.placeholder_info." + placeholder)));
                placeholderReference.addWidget(placeholderName);
                y += 15;
            }
        };
        TextTextureWidget placeholderReferenceLabel = new TextTextureWidget(
                280, 0,
                160, 15,
                GTStringUtils.componentsToString(
                        LangHandler.getMultiLang("gtceu.gui.computer_monitor_cover.placeholder_reference")));
        placeholderReferenceLabel.getTextTexture().type = TextTexture.TextType.LEFT;
        mainPage.addWidget(placeholderReferenceLabel);
        // TextFieldWidget searchBox = new TextFieldWidget(280, 0, 80, 15, null, onSearch);
        // searchBox.setHoverTooltips("Search");
        // mainPage.addWidget(searchBox);
        onSearch.accept("");
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
        mainPage.addWidget(placeholderReference);
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

    private @Nullable IEnergyContainer getEnergyContainer() {
        return GTCapabilityHelper.getEnergyContainer(coverHolder.getLevel(), coverHolder.getPos(), attachedSide);
    }

    private static int countItems(String id, @Nullable IItemHandler itemHandler) {
        if (itemHandler == null) return 0;
        int cnt = 0;
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            ItemStack itemStack = itemHandler.getStackInSlot(i);
            String itemId = "%s:%s".formatted(itemStack.getItem().getCreatorModId(itemStack),
                    itemStack.getItem().toString());
            if (itemId.equals(id)) cnt += itemStack.getCount();
        }
        return cnt;
    }

    private static int countFluids(@Nullable String id, @Nullable IFluidHandler fluidHandler) {
        if (fluidHandler == null) return 0;
        int cnt = 0;
        for (int i = 0; i < fluidHandler.getTanks(); i++) {
            FluidStack fluidStack = fluidHandler.getFluidInTank(i);
            String fluidId = Objects.requireNonNull(ForgeRegistries.FLUIDS.getKey(fluidStack.getFluid())).toString();
            if (id == null || fluidId.equals(id)) cnt += fluidStack.getAmount();
        }
        return cnt;
    }

    private static int countItems(@Nullable ItemFilter filter, @Nullable IItemHandler itemHandler) {
        if (itemHandler == null)
            return -1;
        int cnt = 0;
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            if (filter == null || filter.test(itemHandler.getStackInSlot(i)))
                cnt += itemHandler.getStackInSlot(i).getCount();
        }
        return cnt;
    }

    public static void addPlaceholder(String placeholderName,
                                      BiFunction<ComputerMonitorCover, List<List<MutableComponent>>, List<MutableComponent>> args) {
        PLACEHOLDERS.put(placeholderName, args);
    }

    @Override
    public boolean canConnectRedstone() {
        return true;
    }

    public static void initPlaceholders() {
        addPlaceholder("energy", (cover, args) -> {
            IEnergyContainer energy = cover.getEnergyContainer();
            return GTStringUtils.literalLine(energy != null ? energy.getEnergyStored() : 0);
        });
        addPlaceholder("energyCapacity", (cover, args) -> {
            IEnergyContainer energy = cover.getEnergyContainer();
            return GTStringUtils.literalLine(energy != null ? energy.getEnergyCapacity() : 0);
        });
        addPlaceholder("calc", (cover, args) -> {
            List<String> stringArgs = new ArrayList<>();
            args.forEach((components) -> stringArgs.add(GTStringUtils.componentsToString(components)));
            return GTStringUtils.literalLine(GTStringUtils.calc(stringArgs));
        });
        addPlaceholder("itemCount", (cover, args) -> {
            IItemHandler itemHandler = cover.coverHolder.getItemHandlerCap(cover.attachedSide, false);
            if (args.isEmpty()) return GTStringUtils.literalLine(countItems((ItemFilter) null, itemHandler));
            if (args.size() == 1) return GTStringUtils
                    .literalLine(countItems(GTStringUtils.componentsToString(args.get(0)), itemHandler));
            if (GTStringUtils.equals(args.get(0), "filter")) {
                try {
                    int slot = GTStringUtils.toInt(args.get(1));
                    if (slot > 8 || slot < 1)
                        return GTUtil.list(Component.translatable("gtceu.computer_monitor_cover.error.not_in_range",
                                "slot index", 1, 8, slot));
                    return GTStringUtils.literalLine(
                            countItems(
                                    ItemFilter.loadFilter(cover.itemStackHandler.getStackInSlot(slot - 1)),
                                    itemHandler));
                } catch (NumberFormatException e) {
                    return GTUtil.list(Component.translatable("gtceu.computer_monitor_cover.error.invalid_number",
                            e.getMessage()));
                } catch (NullPointerException e) {
                    return GTUtil.list(Component.translatable("gtceu.computer_monitor_cover.error.missing_item",
                            "filter", GTStringUtils.componentsToString(args.get(1))));
                }
            }
            return GTUtil.list(Component.translatable("gtceu.computer_monitor_cover.error.invalid_args"));
        });
        addPlaceholder("fluidCount", (cover, args) -> {
            IFluidHandler fluidHandler = cover.coverHolder.getFluidHandlerCap(cover.attachedSide, false);
            if (args.isEmpty()) return GTStringUtils.literalLine(countFluids(null, fluidHandler));
            if (args.size() == 1) return GTStringUtils
                    .literalLine(countFluids(GTStringUtils.componentsToString(args.get(0)), fluidHandler));
            return GTUtil.list(
                    Component.translatable("gtceu.computer_monitor_cover.error.wrong_number_of_args", 1, args.size()));
        });
        addPlaceholder("if", (cover, args) -> {
            if (args.size() < 2) return GTUtil.list(
                    Component.translatable("gtceu.computer_monitor_cover.error.wrong_number_of_args", 2, args.size()));
            try {
                if (GTStringUtils.toDouble(args.get(0)) != 0) {
                    return args.get(1);
                } else if (args.size() > 2) return args.get(2);
                else return GTStringUtils.literalLine("");
            } catch (NumberFormatException e) {
                return args.get(1);
            }
        });
        addPlaceholder("color", (cover, args) -> {
            if (args.size() != 2) return GTUtil.list(
                    Component.translatable("gtceu.computer_monitor_cover.error.wrong_number_of_args", 2, args.size()));
            ChatFormatting color = ChatFormatting.getByName(GTStringUtils.componentsToString(args.get(0)));
            if (color == null)
                return GTUtil.list(Component.translatable("gtceu.computer_monitor_cover.error.invalid_args"));
            return args.get(1).stream().map(c -> c.withStyle(color)).toList();
        });
        addPlaceholder("underline", (cover, args) -> {
            if (args.size() != 1) return GTUtil.list(
                    Component.translatable("gtceu.computer_monitor_cover.error.wrong_number_of_args", 1, args.size()));
            return args.get(0).stream().map(c -> c.withStyle(ChatFormatting.UNDERLINE)).toList();
        });
        addPlaceholder("strike", (cover, args) -> {
            if (args.size() != 1) return GTUtil.list(
                    Component.translatable("gtceu.computer_monitor_cover.error.wrong_number_of_args", 1, args.size()));
            return args.get(0).stream().map(c -> c.withStyle(ChatFormatting.STRIKETHROUGH)).toList();
        });
        addPlaceholder("obf", (cover, args) -> {
            if (args.size() != 1) return GTUtil.list(
                    Component.translatable("gtceu.computer_monitor_cover.error.wrong_number_of_args", 1, args.size()));
            return args.get(0).stream().map(c -> c.withStyle(ChatFormatting.OBFUSCATED)).toList();
        });
        addPlaceholder("random", (cover, args) -> {
            if (args.size() != 2) return GTUtil.list(
                    Component.translatable("gtceu.computer_monitor_cover.error.wrong_number_of_args", 2, args.size()));
            try {
                return GTStringUtils.literalLine(GTValues.RNG.nextIntBetweenInclusive(
                        GTStringUtils.toInt(args.get(0)), GTStringUtils.toInt(args.get(1))));
            } catch (NumberFormatException e) {
                return GTUtil.list(
                        Component.translatable("gtceu.computer_monitor_cover.error.invalid_number", e.getMessage()));
            }
        });
        addPlaceholder("repeat", (cover, args) -> {
            if (args.size() != 2) return GTUtil.list(
                    Component.translatable("gtceu.computer_monitor_cover.error.wrong_number_of_args", 2, args.size()));
            try {
                int count = GTStringUtils.toInt(args.get(0));
                List<MutableComponent> out = GTStringUtils.literalLine("");
                for (int i = 0; i < count; i++) GTStringUtils.append(out, args.get(1));
                return out;
            } catch (NumberFormatException e) {
                return GTUtil.list(
                        Component.translatable("gtceu.computer_monitor_cover.error.invalid_number", e.getMessage()));
            }
        });
        addPlaceholder("block", (cover, args) -> {
            if (!args.isEmpty()) return GTUtil.list(
                    Component.translatable("gtceu.computer_monitor_cover.error.wrong_number_of_args", 0, args.size()));
            return GTStringUtils.literalLine("█");
        });
        addPlaceholder("tick", (cover, args) -> {
            if (!args.isEmpty()) return GTUtil.list(
                    Component.translatable("gtceu.computer_monitor_cover.error.wrong_number_of_args", 0, args.size()));
            return GTStringUtils.literalLine(cover.getTicksSincePlaced());
        });
        addPlaceholder("select", (cover, args) -> {
            if (args.isEmpty())
                return GTUtil.list(Component.translatable("gtceu.computer_monitor_cover.error.not_enough_args", 1, 0));
            try {
                int i = GTStringUtils.toInt(args.get(0));
                if (args.size() <= i + 1) return GTUtil.list(Component
                        .translatable("gtceu.computer_monitor_cover.error.not_enough_args", i + 2, args.size()));
                return args.get(i + 1);
            } catch (NumberFormatException e) {
                return GTUtil.list(
                        Component.translatable("gtceu.computer_monitor_cover.error.invalid_number", e.getMessage()));
            }
        });
        addPlaceholder("redstone", (cover, args) -> {
            if (args.size() != 2) return GTUtil.list(
                    Component.translatable("gtceu.computer_monitor_cover.error.wrong_number_of_args", 2, args.size()));
            if (GTStringUtils.equals(args.get(0), "get")) {
                Direction direction = Direction.byName(GTStringUtils.componentsToString(args.get(1)));
                if (direction == null)
                    return GTUtil.list(Component.translatable("gtceu.computer_monitor_cover.error.invalid_args"));
                return GTStringUtils.literalLine(cover.coverHolder.getLevel()
                        .getSignal(cover.coverHolder.getPos().relative(direction), direction));
            } else if (GTStringUtils.equals(args.get(1), "set")) {
                try {
                    int power = GTStringUtils.toInt(args.get(1));
                    if (power < 0 || power > 15)
                        return GTUtil.list(Component.translatable("gtceu.computer_monitor_cover.error.not_in_range",
                                "redstone", 1, 15, power));
                    cover.setRedstoneSignalOutput(power);
                    return GTStringUtils.literalLine("");
                } catch (NumberFormatException e) {
                    return GTUtil.list(Component.translatable("gtceu.computer_monitor_cover.error.invalid_number",
                            e.getMessage()));
                }
            } else return GTUtil.list(Component.translatable("gtceu.computer_monitor_cover.error.invalid_args"));
        });
        addPlaceholder("previousText", (cover, args) -> {
            if (args.size() != 1) return GTUtil.list(
                    Component.translatable("gtceu.computer_monitor_cover.error.wrong_number_of_args", 1, args.size()));
            try {
                int i = GTStringUtils.toInt(args.get(0));
                if (i < 1 || i > cover.text.size())
                    return GTUtil.list(Component.translatable("gtceu.computer_monitor_cover.error.not_in_range",
                            "index", 1, cover.text.size(), i));
                return new ArrayList<>(List.of(cover.text.get(i - 1)));
            } catch (NumberFormatException e) {
                return GTUtil.list(
                        Component.translatable("gtceu.computer_monitor_cover.error.invalid_number", e.getMessage()));
            }
        });
        addPlaceholder("progress", (cover, args) -> {
            if (!args.isEmpty()) return GTUtil.list(
                    Component.translatable("gtceu.computer_monitor_cover.error.wrong_number_of_args", 0, args.size()));
            IWorkable workable = GTCapabilityHelper.getWorkable(cover.coverHolder.getLevel(),
                    cover.coverHolder.getPos(), cover.attachedSide);
            if (workable == null) return GTStringUtils.literalLine(-1);
            return GTStringUtils.literalLine(workable.getProgress());
        });
        addPlaceholder("maxProgress", (cover, args) -> {
            if (!args.isEmpty()) return GTUtil.list(
                    Component.translatable("gtceu.computer_monitor_cover.error.wrong_number_of_args", 0, args.size()));
            IWorkable workable = GTCapabilityHelper.getWorkable(cover.coverHolder.getLevel(),
                    cover.coverHolder.getPos(), cover.attachedSide);
            if (workable == null) return GTStringUtils.literalLine(-1);
            return GTStringUtils.literalLine(workable.getMaxProgress());
        });
        addPlaceholder("maintenance", (cover, args) -> {
            if (!args.isEmpty())
                return GTUtil.list(Component.translatable("gtceu.computer_monitor_cover.error.wrong_number_of_args"));
            IMaintenanceMachine maintenance = GTCapabilityHelper.getMaintenanceMachine(cover.coverHolder.getLevel(),
                    cover.coverHolder.getPos(), cover.attachedSide);
            if (maintenance == null) return GTStringUtils.literalLine(0);
            return GTStringUtils.literalLine(maintenance.hasMaintenanceProblems() ? 1 : 0);
        });
        addPlaceholder("active", (cover, args) -> {
            if (!args.isEmpty()) return GTUtil.list(
                    Component.translatable("gtceu.computer_monitor_cover.error.wrong_number_of_args", 0, args.size()));
            IWorkable workable = GTCapabilityHelper.getWorkable(cover.coverHolder.getLevel(),
                    cover.coverHolder.getPos(), cover.attachedSide);
            if (workable == null) return GTStringUtils.literalLine(0);
            return GTStringUtils.literalLine(workable.isActive() ? 1 : 0);
        });
        addPlaceholder("voltage", (cover, args) -> {
            if (!args.isEmpty()) return GTUtil.list(
                    Component.translatable("gtceu.computer_monitor_cover.error.wrong_number_of_args", 0, args.size()));
            if (cover.coverHolder.getLevel()
                    .getBlockEntity(cover.coverHolder.getPos()) instanceof CableBlockEntity cable) {
                return GTStringUtils.literalLine(cable.getAverageVoltage());
            } else return GTUtil.list(Component.translatable("gtceu.computer_monitor_cover.error.not_cable"));
        });
        addPlaceholder("amperage", (cover, args) -> {
            if (!args.isEmpty()) return GTUtil.list(
                    Component.translatable("gtceu.computer_monitor_cover.error.wrong_number_of_args", 0, args.size()));
            if (cover.coverHolder.getLevel()
                    .getBlockEntity(cover.coverHolder.getPos()) instanceof CableBlockEntity cable) {
                return GTStringUtils.literalLine(cable.getAverageAmperage());
            } else return GTUtil.list(Component.translatable("gtceu.computer_monitor_cover.error.not_cable"));
        });
        addPlaceholder("count", (cover, args) -> {
            if (args.isEmpty())
                return GTUtil.list(Component.translatable("gtceu.computer_monitor_cover.error.not_enough_args", 1, 0));
            String arg1 = GTStringUtils.componentsToString(args.get(0));
            int cnt = -1;
            for (List<MutableComponent> arg : args) {
                if (GTStringUtils.equals(arg, arg1)) cnt++;
            }
            return GTStringUtils.literalLine(cnt);
        });
        addPlaceholder("data", (cover, args) -> {
            if (args.size() < 2) return GTUtil
                    .list(Component.translatable("gtceu.computer_monitor_cover.error.not_enough_args", 2, args.size()));
            try {
                int slot = GTStringUtils.toInt(args.get(1));
                if (slot < 1 || slot > 8) return GTUtil.list(Component
                        .translatable("gtceu.computer_monitor_cover.error.not_in_range", "slot index", 1, 8, slot));
                ItemStack stack = cover.itemStackHandler.getStackInSlot(slot - 1);
                int capacity = -1;
                if (stack.getItem() instanceof ComponentItem componentItem) {
                    for (IItemComponent component : componentItem.getComponents()) {
                        if (component instanceof IDataItem dataComponent) {
                            capacity = dataComponent.getCapacity();
                            break;
                        }
                    }
                }
                if (capacity == -1) return GTUtil.list(Component
                        .translatable("gtceu.computer_monitor_cover.error.missing_item", "any data item", slot));
                ListTag data = stack.getOrCreateTag().getList("computer_monitor_cover_data", Tag.TAG_STRING);
                int p = stack.getOrCreateTag().getInt("computer_monitor_cover_p");
                if (GTStringUtils.equals(args.get(2), "")) args.set(2, GTStringUtils.literalLine(p));
                if (GTStringUtils.equals(args.get(0), "get"))
                    return GTStringUtils.literalLine(data.getString(GTStringUtils.toInt(args.get(2)) % capacity));
                else if (GTStringUtils.equals(args.get(0), "set")) {
                    data.set(GTStringUtils.toInt(args.get(2)) % capacity,
                            StringTag.valueOf(GTStringUtils.componentsToString(args.get(3))));
                    stack.getOrCreateTag().put("computer_monitor_cover_data", data);
                    return GTStringUtils.literalLine("");
                } else if (GTStringUtils.equals(args.get(0), "setp")) {
                    stack.getOrCreateTag().putInt("computer_monitor_cover_p",
                            GTStringUtils.toInt(args.get(3)) % capacity);
                    return GTStringUtils.literalLine("");
                } else if (GTStringUtils.equals(args.get(0), "inc")) {
                    stack.getOrCreateTag().putInt("computer_monitor_cover_p", (p + 1) % capacity);
                    return GTStringUtils.literalLine("");
                } else if (GTStringUtils.equals(args.get(0), "dec")) {
                    stack.getOrCreateTag().putInt("computer_monitor_cover_p", p == 0 ? capacity - 1 : p - 1);
                    return GTStringUtils.literalLine("");
                } else return GTUtil.list(Component.translatable("gtceu.computer_monitor_cover.error.invalid_args"));
            } catch (NumberFormatException e) {
                return GTUtil.list(
                        Component.translatable("gtceu.computer_monitor_cover.error.invalid_number", e.getMessage()));
            } catch (IndexOutOfBoundsException e) {
                return GTUtil.list(Component.translatable("gtceu.computer_monitor_cover.error.invalid_args"));
            }
        });
        addPlaceholder("combine", (cover, args) -> {
            List<MutableComponent> out = GTStringUtils.literalLine("");
            for (int i = 0; i < args.size(); i++) {
                GTStringUtils.append(out, args.get(i));
                if (i != args.size() - 1) GTStringUtils.append(out, " ");
            }
            return out;
        });
        addPlaceholder("nbt", (cover, args) -> {
            if (args.size() != 1) return GTUtil.list(
                    Component.translatable("gtceu.computer_monitor_cover.error.wrong_number_of_args", 1, args.size()));
            try {
                int slot = GTStringUtils.toInt(args.get(0));
                if (slot < 1 || slot > 8) GTUtil.list(Component
                        .translatable("gtceu.computer_monitor_cover.error.not_in_range", "slot index", 1, 8, slot));
                return GTStringUtils
                        .literalLine(cover.itemStackHandler.getStackInSlot(slot - 1).getOrCreateTag().toString());
            } catch (NumberFormatException e) {
                return GTUtil.list(
                        Component.translatable("gtceu.computer_monitor_cover.error.invalid_number", e.getMessage()));
            }
        });
        addPlaceholder("toChars", (cover, args) -> {
            if (args.size() != 1) return GTUtil.list(
                    Component.translatable("gtceu.computer_monitor_cover.error.wrong_number_of_args", 1, args.size()));
            if (args.get(0).isEmpty()) return GTStringUtils.literalLine("");
            StringBuilder out = new StringBuilder();
            for (char c : GTStringUtils.componentsToString(args.get(0)).toCharArray()) out.append(c).append(' ');
            return GTStringUtils.literalLine(out.substring(0, out.length() - 2));
        });
        addPlaceholder("toAscii", (cover, args) -> {
            if (args.size() != 1) return GTUtil.list(
                    Component.translatable("gtceu.computer_monitor_cover.error.wrong_number_of_args", 1, args.size()));
            String arg = GTStringUtils.componentsToString(args.get(0));
            if (arg.length() != 1)
                return GTUtil.list(Component.translatable("gtceu.computer_monitor_cover.error.invalid_args"));
            return GTStringUtils.literalLine((int) arg.toCharArray()[0]);
        });
        addPlaceholder("fromAscii", (cover, args) -> {
            if (args.size() != 1) return GTUtil.list(
                    Component.translatable("gtceu.computer_monitor_cover.error.wrong_number_of_args", 1, args.size()));
            try {
                return GTStringUtils.literalLine((char) GTStringUtils.toInt(args.get(0)));
            } catch (NumberFormatException e) {
                return GTUtil.list(
                        Component.translatable("gtceu.computer_monitor_cover.error.invalid_number", e.getMessage()));
            }
        });
    }
}

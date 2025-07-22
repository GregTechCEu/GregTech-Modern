package com.gregtechceu.gtceu.common.cover;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;

import com.gregtechceu.gtceu.api.cover.IUICover;
import com.gregtechceu.gtceu.api.cover.filter.ItemFilter;
import com.gregtechceu.gtceu.api.gui.widget.IntInputWidget;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.client.renderer.cover.CoverTextRenderer;
import com.gregtechceu.gtceu.client.renderer.cover.IDynamicCoverRenderer;
import com.gregtechceu.gtceu.utils.GTStringUtils;
import com.lowdragmc.lowdraglib.gui.texture.ResourceBorderTexture;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.*;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import com.simibubi.create.AllItems;
import com.simibubi.create.Create;
import com.simibubi.create.content.redstone.link.IRedstoneLinkable;
import com.simibubi.create.content.redstone.link.RedstoneLinkNetworkHandler;
import com.simibubi.create.content.redstone.link.controller.LinkedControllerItem;
import lombok.Getter;
import lombok.Setter;
import net.createmod.catnip.data.Couple;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import static java.util.Map.entry;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ComputerMonitorCover extends CoverBehavior implements IUICover, Container {
    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(ComputerMonitorCover.class, CoverBehavior.MANAGED_FIELD_HOLDER);

    private static final Map<String, BiFunction<ComputerMonitorCover, List<List<MutableComponent>>, List<MutableComponent>>> PLACEHOLDERS = Map.ofEntries(
            entry("energy", (cover, args) -> {
                IEnergyContainer energy = cover.getEnergyContainer();
                return GTStringUtils.literal(energy != null ? energy.getEnergyStored() : 0);
            }),
            entry("energyCapacity", (cover, args) -> {
                IEnergyContainer energy = cover.getEnergyContainer();
                return GTStringUtils.literal(energy != null ? energy.getEnergyCapacity() : 0);
            }),
            entry("calc", (cover, args) -> {
                List<String> stringArgs = new ArrayList<>();
                args.forEach((components) -> stringArgs.add(GTStringUtils.componentsToString(components)));
                return GTStringUtils.literal(GTStringUtils.calc(stringArgs));
            }),
            entry("itemCount", (cover, args) -> {
                IItemHandler itemHandler = cover.coverHolder.getItemHandlerCap(cover.attachedSide, false);
                if (args.isEmpty()) return GTStringUtils.literal(countItems((ItemFilter) null, itemHandler));
                if (args.size() == 1) return GTStringUtils.literal(countItems(GTStringUtils.componentsToString(args.get(0)), itemHandler));
                if (GTStringUtils.equals(args.get(0), "filter")) {
                    try {
                        int slot = GTStringUtils.toInt(args.get(1));
                        if (slot > 8 || slot < 1) return GTStringUtils.literal("Expected slot index between 1 and 8");
                        return GTStringUtils.literal(countItems(ItemFilter.loadFilter(cover.slots.get(slot - 1)), itemHandler));
                    } catch (NumberFormatException e) {
                        return GTStringUtils.literal("Invalid slot '%s'!".formatted(e));
                    } catch (NullPointerException e) {
                        return GTStringUtils.literal("Invalid filter!");
                    }
                }
                return GTStringUtils.literal("Invalid args!");
            }),
            entry("fluidCount", (cover, args) -> {
                IFluidHandler fluidHandler = cover.coverHolder.getFluidHandlerCap(cover.attachedSide, false);
                if (args.isEmpty()) return GTStringUtils.literal(countFluids(null, fluidHandler));
                if (args.size() == 1) return GTStringUtils.literal(countFluids(GTStringUtils.componentsToString(args.get(0)), fluidHandler));
                return GTStringUtils.literal("Expected not more than 1 argument!");
            }),
            entry("if", (cover, args) -> {
                if (args.size() < 2) return GTStringUtils.literal("Expected at least 2 args!");
                try {
                    if (GTStringUtils.toDouble(args.get(0)) != 0) {
                        return args.get(1);
                    } else if (args.size() > 2) return args.get(2);
                    else return GTStringUtils.literal("");
                } catch (NumberFormatException e) {
                    return args.get(1);
                }
            }),
            entry("color", (cover, args) -> {
                if (args.size() != 2) return GTStringUtils.literal("Expected 2 args!");
                ChatFormatting color = ChatFormatting.getByName(GTStringUtils.componentsToString(args.get(0)));
                if (color == null)
                    return GTStringUtils.literal("Unknown color '%s'".formatted(GTStringUtils.componentsToString(args.get(0))));
                return args.get(1).stream().map(c -> c.withStyle(color)).toList();
            }),
            entry("underline", (cover, args) -> {
                if (args.size() != 1) return GTStringUtils.literal("Expected 1 argument!");
                return args.get(0).stream().map(c -> c.withStyle(ChatFormatting.UNDERLINE)).toList();
            }),
            entry("strike", (cover, args) -> {
                if (args.size() != 1) return GTStringUtils.literal("Expected 1 argument!");
                return args.get(0).stream().map(c -> c.withStyle(ChatFormatting.STRIKETHROUGH)).toList();
            }),
            entry("obf", (cover, args) -> {
                if (args.size() != 1) return GTStringUtils.literal("Expected 1 argument!");
                return args.get(0).stream().map(c -> c.withStyle(ChatFormatting.OBFUSCATED)).toList();
            }),
            entry("random", (cover, args) -> {
                if (args.size() != 2) return GTStringUtils.literal("Expected 2 arguments!");
                try {
                    return GTStringUtils.literal(GTValues.RNG.nextIntBetweenInclusive(GTStringUtils.toInt(args.get(0)), GTStringUtils.toInt(args.get(1))));
                } catch (NumberFormatException e) {
                    return GTStringUtils.literal("Invalid number '%s'".formatted(e.getMessage()));
                }
            }),
            entry("repeat", (cover, args) -> {
                if (args.size() != 2) return GTStringUtils.literal("Expected 2 arguments!");
                try {
                    int count = GTStringUtils.toInt(args.get(0));
                    List<MutableComponent> out = GTStringUtils.literal("");
                    for (int i = 0; i < count; i++) GTStringUtils.append(out, args.get(1));
                    return out;
                } catch (NumberFormatException e) {
                    return GTStringUtils.literal("Invalid number '%s'".formatted(e.getMessage()));
                }
            }),
            entry("block", (cover, args) -> {
                if (!args.isEmpty()) return GTStringUtils.literal("Expected no arguments!");
                return GTStringUtils.literal("█");
            }),
            entry("tick", (cover, args) -> {
                if (!args.isEmpty()) return GTStringUtils.literal("Expected no arguments!");
                return GTStringUtils.literal(cover.getTicksSincePlaced());
            }),
            entry("select", (cover, args) -> {
                if (args.isEmpty()) return GTStringUtils.literal("Expected at least 1 argument!");
                try {
                    int i = GTStringUtils.toInt(args.get(0));
                    if (args.size() <= i + 1) return GTStringUtils.literal("Expected at least %d arguments, got %d instead".formatted(i + 2, args.size()));
                    return args.get(i + 1);
                } catch (NumberFormatException e) {
                    return GTStringUtils.literal("Invalid number '%s'".formatted(e.getMessage()));
                }
            }),
            entry("redstone", ComputerMonitorCover::processRedstonePlaceholder)
    );

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
    private final List<ItemStack> slots = new ArrayList<>();
    @Setter
    private String placeholderSearch = "";
    @Setter
    @Getter
    @Persisted
    private int updateInterval = 100;
    private int tick = 0;
    @Getter
    @Persisted
    private long ticksSincePlaced = 0;


    public ComputerMonitorCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide) {
        super(definition, coverHolder, attachedSide);
        renderer = new CoverTextRenderer(this::getText);
        for (int i = 0; i < 8; i++) {
            slots.add(ItemStack.EMPTY);
        }
    }

    public boolean placeholderExists(List<MutableComponent> placeholder) {
        return PLACEHOLDERS.containsKey(GTStringUtils.componentsToString(placeholder));
    }

    public @Nullable List<MutableComponent> processPlaceholder(List<List<MutableComponent>> placeholder) {
        if (!placeholderExists(placeholder.get(0))) return null;
        return PLACEHOLDERS.get(GTStringUtils.componentsToString(placeholder.get(0))).apply(this, placeholder.subList(1, placeholder.size()));
    }

    public List<MutableComponent> getRenderedText() {
        List<MutableComponent> out = new ArrayList<>(GTStringUtils.literal(""));
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
            else if (c == '\\') escaped = true;
            else {
                if (left_bracket) tmp.append('{');
                tmp.append(c);
                left_bracket = false;
            }
        }
        if (left_bracket) tmp.append('{');
        escaped = false;
        for (char c : tmp.toString().toCharArray()) {
            if (c == '\\') {
                if (escaped) {
                    if (incompletePlaceholders.isEmpty()) GTStringUtils.append(out, c);
                    else GTStringUtils.append(GTStringUtils.getLast(incompletePlaceholders.peek()), c);
                    escaped = false;
                } else escaped = true;
            } else if (escaped) {
                if (c == 'n') {
                    if (incompletePlaceholders.isEmpty()) out.add(MutableComponent.create(ComponentContents.EMPTY));
                    else GTStringUtils.getLast(incompletePlaceholders.peek()).add(MutableComponent.create(ComponentContents.EMPTY));
                }
                else if (incompletePlaceholders.isEmpty()) GTStringUtils.append(out, c);
                else GTStringUtils.append(GTStringUtils.getLast(incompletePlaceholders.peek()), c);
                escaped = false;
            } else if (c == ' ') {
                if (incompletePlaceholders.isEmpty()) GTStringUtils.append(out, c);
                else incompletePlaceholders.peek().add(GTStringUtils.literal(""));
            } else if (c == '{') incompletePlaceholders.push(new ArrayList<>(List.of(GTStringUtils.literal(""))));
            else if (c == '}') {
                if (incompletePlaceholders.isEmpty()) return GTStringUtils.literal("Unexpected closing bracket!");
                if (incompletePlaceholders.peek().isEmpty()) {
                    incompletePlaceholders.pop();
                } else if (placeholderExists(incompletePlaceholders.peek().get(0))) {
                    List<MutableComponent> placeholderString = processPlaceholder(incompletePlaceholders.pop());
                    if (incompletePlaceholders.isEmpty()) GTStringUtils.append(out, placeholderString);
                    else GTStringUtils.append(GTStringUtils.getLast(incompletePlaceholders.peek()), placeholderString);
                } else {
                    return GTStringUtils.literal("No such placeholder: '%s'".formatted(GTStringUtils.componentsToString(incompletePlaceholders.peek().get(0))));
                }
            } else if (c == '\n') {
                if (incompletePlaceholders.isEmpty()) out.add(MutableComponent.create(ComponentContents.EMPTY));
                else GTStringUtils.getLast(incompletePlaceholders.peek()).add(MutableComponent.create(ComponentContents.EMPTY));
            } else {
                if (incompletePlaceholders.isEmpty()) GTStringUtils.append(out, c);
                else GTStringUtils.append(GTStringUtils.getLast(incompletePlaceholders.peek()), c);
            }
        }
        if (incompletePlaceholders.isEmpty())
            return out;
        return GTStringUtils.literal("Unclosed bracket!");
    }

    @Override
    public boolean canPipePassThrough() {
        return false;
    }

    @Override
    public @Nullable IDynamicCoverRenderer getDynamicRenderer() {
        return renderer;
    }

    @Override
    public @NotNull ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public Widget createUIWidget() {
        int textFieldWidth = 160, horizontalPadding = 10, verticalPadding = 2;
        final WidgetGroup group = new WidgetGroup(0, 0, 2*textFieldWidth + 3*horizontalPadding, 150);
        final WidgetGroup mainPage = new WidgetGroup(0, 0, 2*textFieldWidth + 3*horizontalPadding, 150);
        final WidgetGroup formatStringArgsPage = new WidgetGroup(0, 0, 2*textFieldWidth + 3*horizontalPadding, 150);
        for (int i = 0; i < 8; i++) {
            TextFieldWidget formatStringInput = new TextFieldWidget();
            formatStringInput.setSize(textFieldWidth, 15);
            formatStringInput.setSelfPosition(horizontalPadding + textFieldWidth/2, 10 + verticalPadding + i*(15 + verticalPadding));
            formatStringInput.setHoverTooltips(
                    "Input string to display on line %d here.".formatted(i + 1),
                    "It can have placeholders, for example: 'Energy: {energy}/{energyCapacity} EU'",
                    "Placeholders can also be inside other placeholders."
            );
            int finalI = i;
            if (i >= formatStringLines.size()) formatStringLines.add("");
            formatStringInput.setCurrentString(formatStringLines.get(i));
            formatStringInput.setTextResponder((s) -> formatStringLines.set(finalI, s));
            mainPage.addWidget(formatStringInput);
            SlotWidget slot = new SlotWidget(
                    this,
                    i,
                    horizontalPadding + 50,
                    20*i,
                    true, true
            );
            slot.setItem(slots.get(i));
            slot.setBackgroundTexture(SlotWidget.ITEM_SLOT_TEXTURE);
            slot.setHoverTooltips(
                    "A slot for items that some placeholders can reference",
                    "Slot number: %d".formatted(i + 1)
            );
            mainPage.addWidget(slot);
        }
        for (int i = 0; i < 8; i++) {
            TextFieldWidget formatStringArgsInput = new TextFieldWidget();
            formatStringArgsInput.setSize(textFieldWidth, 15);
            formatStringArgsInput.setSelfPosition(textFieldWidth/2 + horizontalPadding, 10 + verticalPadding + i*(15 + verticalPadding));
            formatStringArgsInput.setHoverTooltips(
                    "Input placeholder to be used in place of %s '{}' here.".formatted(GTStringUtils.getIntOrderingSuffix(i + 1)),
                    "For example, you can have a string 'Energy: {}/{} EU' and 'energy' and 'energyCapacity' in these text boxes."
            );

            int finalI = i;
            if (i >= formatStringArgs.size()) formatStringArgs.add("");
            formatStringArgsInput.setCurrentString(formatStringArgs.get(i));
            formatStringArgsInput.setTextResponder((s) -> formatStringArgs.set(finalI, s));
            formatStringArgsPage.addWidget(formatStringArgsInput);
        }
        ButtonWidget switchToFormatStringArgsPageButton = new ButtonWidget(
                horizontalPadding + 50,
                10*(15 + verticalPadding) + verticalPadding,
                20, 20,
                new ResourceBorderTexture(),
                clickData -> {
                    group.clearAllWidgets();
                    group.addWidget(formatStringArgsPage);
                }
        );
        ButtonWidget switchBack = new ButtonWidget(
                horizontalPadding + 50,
                10*(15 + verticalPadding) + verticalPadding,
                20, 20,
                new ResourceBorderTexture(),
                clickData -> {
                    group.clearAllWidgets();
                    group.addWidget(mainPage);
                }
        );
        WidgetGroup placeholderReference = new WidgetGroup(280, 0, 100, 0);
        Consumer<String> onSearch = (newSearch) -> {
            setPlaceholderSearch(newSearch);
            placeholderReference.clearAllWidgets();
            int y = 15 + verticalPadding;
            ArrayList<String> placeholders = new ArrayList<>(PLACEHOLDERS.keySet().stream().toList());
            placeholders.removeIf(s -> s == null || !s.contains(placeholderSearch));
            placeholders.sort(String::compareTo);
            for (String placeholder : placeholders) {
                TextTextureWidget placeholderName = new TextTextureWidget(0, y, 80, 15, placeholder);
                placeholderName.getTextTexture().type = TextTexture.TextType.LEFT;
                placeholderName.setHoverTooltips(Component.translatableWithFallback("gtceu.placeholder_info." + placeholder, "No info."));
                placeholderReference.addWidget(placeholderName);
                y += 15;
            }
        };
        TextTextureWidget placeholderReferenceLabel = new TextTextureWidget(
                280, 0,
                160, 15,
                Component.translatable("gtceu.gui.computer_monitor_cover.placeholder_reference").getString()
        );
        placeholderReferenceLabel.getTextTexture().type = TextTexture.TextType.LEFT;
        mainPage.addWidget(placeholderReferenceLabel);
        //TextFieldWidget searchBox = new TextFieldWidget(280, 0, 80, 15, null, onSearch);
        //searchBox.setHoverTooltips("Search");
        //mainPage.addWidget(searchBox);
        onSearch.accept("");
        IntInputWidget updateIntervalInput = new IntInputWidget(0, 0, 60, 20, this::getUpdateInterval, this::setUpdateInterval);
        updateIntervalInput.setMin(1);
        updateIntervalInput.setMax(60*20);
        updateIntervalInput.setHoverTooltips(Component.translatable("gtceu.gui.computer_monitor_cover.update_interval"));
        mainPage.addWidget(updateIntervalInput);
        switchToFormatStringArgsPageButton.setHoverTooltips(Component.translatable("gtceu.gui.computer_monitor_cover.edit_blank_placeholders"));
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
        tick++;
        ticksSincePlaced++;
        if (tick >= updateInterval) {
            tick = 0;
            try {
                if (GTCEu.Mods.isCreateLoaded())
                    TemporaryRedstoneLinkTransmitter.destroyAll();
                setRedstoneSignalOutput(0);
                text = getRenderedText();
            } catch (RuntimeException e) {
                text = GTStringUtils.literal("An unexpected error has occurred: " + e);
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
            String itemId = "%s:%s".formatted(itemStack.getItem().getCreatorModId(itemStack), itemStack.getItem().toString());
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

    private static List<MutableComponent> processRedstonePlaceholder(ComputerMonitorCover cover, List<List<MutableComponent>> args) {
        if (args.isEmpty()) return GTStringUtils.literal("Expected at least 1 argument!");
        else if (GTStringUtils.equals(args.get(0), "get")) {
            if (args.size() < 2) return GTStringUtils.literal("Expected an argument after 'get'!");
            if (GTStringUtils.equals(args.get(1), "link")) {
                if (args.size() < 4) return GTStringUtils.literal("Expected slot number and frequency slot number after 'link'!");
                try {
                    int slot = GTStringUtils.toInt(args.get(2));
                    int freq_slot = GTStringUtils.toInt(args.get(3));
                    if (slot < 1 || slot > 8) return GTStringUtils.literal("Expected slot index between 1 and 8");
                    ItemStack item = cover.slots.get(slot - 1);
                    if (!GTCEu.Mods.isCreateLoaded()) return GTStringUtils.literal("Create is not loaded!");
                    if (item.is(AllItems.LINKED_CONTROLLER.get())) {
                        Couple<RedstoneLinkNetworkHandler.Frequency> freq = LinkedControllerItem.toFrequency(item, freq_slot);
                        return GTStringUtils.literal(getRedstoneLinkPower(cover, freq));
                    } else return GTStringUtils.literal("Invalid redstone link controller!");
                } catch (NumberFormatException e) {
                    return GTStringUtils.literal("Invalid slot number '%s'".formatted(e.getMessage()));
                }
            } else {
                Direction direction = Direction.byName(GTStringUtils.componentsToString(args.get(1)));
                if (direction == null) return GTStringUtils.literal("2nd argument must be either 'link' or a valid direction (up,down,north,south,east,west)");
                return GTStringUtils.literal(cover.coverHolder.getLevel().getSignal(cover.coverHolder.getPos().relative(direction), direction));
            }
        } else if (GTStringUtils.equals(args.get(0), "set")) {
            if (args.size() < 2) return GTStringUtils.literal("Expected an argument after 'set'!");
            if (GTStringUtils.equals(args.get(1), "link")) {
                if (args.size() < 5) return GTStringUtils.literal("Expected slot number, frequency slot number and power after 'link'!");
                try {
                    int slot = GTStringUtils.toInt(args.get(2));
                    int freq_slot = GTStringUtils.toInt(args.get(3));
                    int power = GTStringUtils.toInt(args.get(4));
                    if (power < 0 || power > 15) return GTStringUtils.literal("Expected redstone power to be from 0 to 15");
                    if (slot < 1 || slot > 8) return GTStringUtils.literal("Expected slot index between 1 and 8");
                    ItemStack item = cover.slots.get(slot - 1);
                    if (!GTCEu.Mods.isCreateLoaded()) return GTStringUtils.literal("Create is not loaded!");
                    if (item.is(AllItems.LINKED_CONTROLLER.get())) {
                        Couple<RedstoneLinkNetworkHandler.Frequency> freq = LinkedControllerItem.toFrequency(item, freq_slot);
                        setRedstoneLinkPower(cover, freq, power);
                        return GTStringUtils.literal("");
                    } else return GTStringUtils.literal("Invalid redstone link controller!");
                } catch (NumberFormatException e) {
                    return GTStringUtils.literal("Invalid number '%s'".formatted(e.getMessage()));
                }
            } else {
                try {
                    int power = GTStringUtils.toInt(args.get(1));
                    if (power < 0 || power > 15) return GTStringUtils.literal("Expected redstone power to be from 0 to 15");
                    cover.setRedstoneSignalOutput(power);
                    return GTStringUtils.literal("");
                } catch (NumberFormatException e) {
                    return GTStringUtils.literal("Invalid number '%s'".formatted(e.getMessage()));
                }
            }
        } else {
            return GTStringUtils.literal("1st argument must be either 'set' or 'get'");
        }
    }

    private static int getRedstoneLinkPower(ComputerMonitorCover cover, Couple<RedstoneLinkNetworkHandler.Frequency> freq) {
        IRedstoneLinkable linkable = new IRedstoneLinkable() {
            @Override
            public int getTransmittedStrength() {
                return 0;
            }
            @Override
            public void setReceivedStrength(int power) {}
            @Override
            public boolean isListening() {
                return true;
            }
            @Override
            public boolean isAlive() {
                return true;
            }
            @Override
            public Couple<RedstoneLinkNetworkHandler.Frequency> getNetworkKey() {
                return freq;
            }
            @Override
            public BlockPos getLocation() {
                return cover.coverHolder.getPos();
            }
        };
        Set<IRedstoneLinkable> network = Create.REDSTONE_LINK_NETWORK_HANDLER.getNetworkOf(cover.coverHolder.getLevel(), linkable);
        int power = 0;
        for (IRedstoneLinkable i : network) {
            if (!i.isAlive()) continue;
            if (!RedstoneLinkNetworkHandler.withinRange(i, linkable)) continue;
            power = Math.max(power, i.getTransmittedStrength());
        }
        return power;
    }

    private static void setRedstoneLinkPower(ComputerMonitorCover cover, Couple<RedstoneLinkNetworkHandler.Frequency> freq, int power) {
        TemporaryRedstoneLinkTransmitter linkable = new TemporaryRedstoneLinkTransmitter(freq, power, cover.coverHolder.getPos(), cover.coverHolder.getLevel());
        Create.REDSTONE_LINK_NETWORK_HANDLER.addToNetwork(cover.coverHolder.getLevel(), linkable);
    }

    @Override
    public boolean canConnectRedstone() {
        return true;
    }

    // ============= Container stuff ============= //

    @Override
    public int getContainerSize() {
        return 8;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public ItemStack getItem(int i) {
        return slots.get(i);
    }

    @Override
    public ItemStack removeItem(int i, int i1) {
        return slots.set(i, ItemStack.EMPTY);
    }

    @Override
    public ItemStack removeItemNoUpdate(int i) {
        return slots.set(i, ItemStack.EMPTY);
    }

    @Override
    public void setItem(int i, ItemStack itemStack) {
        slots.set(i, itemStack);
    }

    @Override
    public void setChanged() {}

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void clearContent() {
        slots.replaceAll((itemStack -> ItemStack.EMPTY));
    }

    private static class TemporaryRedstoneLinkTransmitter implements IRedstoneLinkable {
        private static final ArrayList<TemporaryRedstoneLinkTransmitter> transmitters = new ArrayList<>();
        private final int power;
        private final Couple<RedstoneLinkNetworkHandler.Frequency> freq;
        private final BlockPos pos;
        private final Level level;
        private boolean alive;

        public TemporaryRedstoneLinkTransmitter(Couple<RedstoneLinkNetworkHandler.Frequency> frequency, int power, BlockPos pos, Level level) {
            this.power = power;
            this.freq = frequency;
            this.alive = true;
            this.pos = pos;
            this.level = level;
            transmitters.add(this);
        }
        @Override
        public int getTransmittedStrength() {
            return power;
        }
        @Override
        public void setReceivedStrength(int power) {}
        @Override
        public boolean isListening() {
            return false;
        }
        @Override
        public boolean isAlive() {
            return alive;
        }
        @Override
        public Couple<RedstoneLinkNetworkHandler.Frequency> getNetworkKey() {
            return freq;
        }
        @Override
        public BlockPos getLocation() {
            return pos;
        }
        public void destroy() {
            this.alive = false;
            Create.REDSTONE_LINK_NETWORK_HANDLER.updateNetworkOf(level, this);
        }
        public static void destroyAll() {
            while (!transmitters.isEmpty()) {
                transmitters.get(transmitters.size() - 1).destroy();
                transmitters.remove(transmitters.size() - 1);
            }
        }
    }
}

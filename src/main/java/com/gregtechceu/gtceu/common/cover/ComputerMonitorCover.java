package com.gregtechceu.gtceu.common.cover;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;

import com.gregtechceu.gtceu.api.cover.IUICover;
import com.gregtechceu.gtceu.api.cover.filter.ItemFilter;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.client.renderer.cover.CoverTextRenderer;
import com.gregtechceu.gtceu.client.renderer.cover.IDynamicCoverRenderer;
import com.gregtechceu.gtceu.utils.GTStringUtils;
import com.lowdragmc.lowdraglib.gui.texture.ResourceBorderTexture;
import com.lowdragmc.lowdraglib.gui.widget.*;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import lombok.Getter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.function.BiFunction;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ComputerMonitorCover extends CoverBehavior implements IUICover, Container {
    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(ComputerMonitorCover.class, CoverBehavior.MANAGED_FIELD_HOLDER);

    private static final Map<String, BiFunction<ComputerMonitorCover, List<String>, String>> PLACEHOLDERS = Map.of(
            "energy", (cover, args) -> {
                IEnergyContainer energy = cover.getEnergyContainer();
                return energy != null ? String.valueOf(energy.getEnergyStored()) : "0";
            },
            "energyCapacity", (cover, args) -> {
                IEnergyContainer energy = cover.getEnergyContainer();
                return energy != null ? String.valueOf(energy.getEnergyCapacity()) : "0";
            },
            "calc", (cover, args) -> GTStringUtils.calc(args),
            "itemCount", (cover, args) -> {
                IItemHandler itemHandler = cover.coverHolder.getItemHandlerCap(cover.attachedSide, false);
                if (args.isEmpty()) return String.valueOf(countItems((ItemFilter) null, itemHandler));
                if (args.size() == 1) return String.valueOf(countItems(args.get(0), itemHandler));
                if (Objects.equals(args.get(0), "filter")) {
                    try {
                        int slot = Integer.parseInt(args.get(1));
                        if (slot > 8 || slot < 1) return "Expected slot index between 1 and 8";
                        return String.valueOf(countItems(ItemFilter.loadFilter(cover.slots.get(slot - 1)), itemHandler));
                    } catch (NumberFormatException e) {
                        return "Invalid slot!";
                    } catch (NullPointerException e) {
                        return "Invalid filter!";
                    }
                }
                return "Invalid args!";
            }
    );

    private TickableSubscription subscription;
    private final CoverTextRenderer renderer;
    @Persisted
    @DescSynced
    private final List<String> formatStringArgs = new ArrayList<>(8);
    @Persisted
    @DescSynced
    private final List<String> formatStringLines = new ArrayList<>(8);
    @Persisted
    @DescSynced
    @Getter
    private String text = "";
    @Persisted
    private final List<ItemStack> slots = new ArrayList<>();

    public ComputerMonitorCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide) {
        super(definition, coverHolder, attachedSide);
        renderer = new CoverTextRenderer(this::getText);
        for (int i = 0; i < 8; i++) {
            slots.add(ItemStack.EMPTY);
        }
    }

    public boolean placeholderExists(String placeholder) {
        String[] tmp = placeholder.split("\\s+");
        if (tmp.length < 1) return false;
        return PLACEHOLDERS.containsKey(tmp[0]);
    }

    public @Nullable String processPlaceholder(String placeholder) {
        String[] tmp = placeholder.split("\\s+");
        if (tmp.length < 1 || !placeholderExists(placeholder)) return null;
        return PLACEHOLDERS.get(tmp[0]).apply(this, Arrays.asList(tmp).subList(1, tmp.length));
    }

    public String getRenderedText() {
        StringBuilder out = new StringBuilder();
        Stack<StringBuilder> incompletePlaceholders = new Stack<>();
        int formatStringArgsIndex = 0;
        StringBuilder formatString = new StringBuilder();
        formatStringLines.forEach((line) -> formatString.append(line).append("\n"));
        boolean escaped = false;
        for (char c : formatString.toString().toCharArray()) {
            if (c == '\\') {
                if (escaped) {
                    if (incompletePlaceholders.isEmpty()) out.append(c);
                    else incompletePlaceholders.peek().append(c);
                    escaped = false;
                } else escaped = true;
            } else if (escaped) {
                if (c == 'n') c = '\n';
                if (incompletePlaceholders.isEmpty()) out.append(c);
                else incompletePlaceholders.peek().append(c);
                escaped = false;
            } else if (c == '{') incompletePlaceholders.push(new StringBuilder());
            else if (c == '}') {
                if (incompletePlaceholders.isEmpty()) return "Unexpected closing bracket!";
                if (incompletePlaceholders.peek().isEmpty()) {
                    if (formatStringArgsIndex < formatStringArgs.size()) {
                        if (placeholderExists(formatStringArgs.get(formatStringArgsIndex))) {
                            incompletePlaceholders.pop();
                            String placeholderString = processPlaceholder(formatStringArgs.get(formatStringArgsIndex));
                            formatStringArgsIndex++;
                            if (incompletePlaceholders.isEmpty()) out.append(placeholderString);
                            else incompletePlaceholders.peek().append(placeholderString);
                        } else return "No such placeholder: '%s'".formatted(formatStringArgs.get(formatStringArgsIndex));
                    } else {
                        return "There's more empty placeholders than arguments!";
                    }
                } else if (placeholderExists(incompletePlaceholders.peek().toString())) {
                    String placeholderString = processPlaceholder(incompletePlaceholders.pop().toString());
                    if (incompletePlaceholders.isEmpty()) out.append(placeholderString);
                    else incompletePlaceholders.peek().append(placeholderString);
                } else {
                    return "No such placeholder: '%s'".formatted(incompletePlaceholders.peek());
                }
            } else {
                if (incompletePlaceholders.isEmpty()) out.append(c);
                else incompletePlaceholders.peek().append(c);
            }
        }
        if (incompletePlaceholders.isEmpty())
            return out.toString();
        return "Unclosed bracket!";
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
        int textFieldWidth = 120, horizontalPadding = 10, verticalPadding = 2;
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
                    horizontalPadding + 10,
                    20*i,
                    true, true
            );
            slot.setItem(slots.get(i));
            slot.setBackgroundTexture(SlotWidget.ITEM_SLOT_TEXTURE);
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
                horizontalPadding + 10,
                10*(15 + verticalPadding) + verticalPadding,
                20, 20,
                new ResourceBorderTexture(),
                clickData -> {
                    group.clearAllWidgets();
                    group.addWidget(formatStringArgsPage);
                }
        );
        ButtonWidget switchBack = new ButtonWidget(
                horizontalPadding + 10,
                10*(15 + verticalPadding) + verticalPadding,
                20, 20,
                new ResourceBorderTexture(),
                clickData -> {
                    group.clearAllWidgets();
                    group.addWidget(mainPage);
                }
        );
        switchToFormatStringArgsPageButton.setHoverTooltips("Edit blank placeholders");
        switchBack.setHoverTooltips("Edit displayed text");
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
        text = getRenderedText();
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
}

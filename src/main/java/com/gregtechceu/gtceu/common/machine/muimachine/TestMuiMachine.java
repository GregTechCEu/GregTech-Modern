package com.gregtechceu.gtceu.common.machine.muimachine;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.IMuiMachine;
import com.gregtechceu.gtceu.api.mui.base.IPanelHandler;
import com.gregtechceu.gtceu.api.mui.base.drawable.IKey;
import com.gregtechceu.gtceu.api.mui.base.widget.IWidget;
import com.gregtechceu.gtceu.api.mui.drawable.*;
import com.gregtechceu.gtceu.api.mui.drawable.text.AnimatedText;
import com.gregtechceu.gtceu.api.mui.factory.GuiData;
import com.gregtechceu.gtceu.api.mui.factory.PosGuiData;
import com.gregtechceu.gtceu.api.mui.theme.WidgetTheme;
import com.gregtechceu.gtceu.api.mui.utils.Alignment;
import com.gregtechceu.gtceu.api.mui.utils.Color;
import com.gregtechceu.gtceu.api.mui.utils.Interpolation;
import com.gregtechceu.gtceu.api.mui.value.BoolValue;
import com.gregtechceu.gtceu.api.mui.value.IntValue;
import com.gregtechceu.gtceu.api.mui.value.StringValue;
import com.gregtechceu.gtceu.api.mui.value.sync.DynamicSyncHandler;
import com.gregtechceu.gtceu.api.mui.value.sync.GenericSyncValue;
import com.gregtechceu.gtceu.api.mui.value.sync.IntSyncValue;
import com.gregtechceu.gtceu.api.mui.value.sync.ItemSlotSH;
import com.gregtechceu.gtceu.api.mui.value.sync.PanelSyncManager;
import com.gregtechceu.gtceu.api.mui.value.sync.SyncHandlers;
import com.gregtechceu.gtceu.api.mui.widget.EmptyWidget;
import com.gregtechceu.gtceu.api.mui.widget.ParentWidget;
import com.gregtechceu.gtceu.api.mui.widgets.*;
import com.gregtechceu.gtceu.api.mui.widgets.layout.Column;
import com.gregtechceu.gtceu.api.mui.widgets.layout.Flow;
import com.gregtechceu.gtceu.api.mui.widgets.layout.Row;
import com.gregtechceu.gtceu.api.mui.widgets.slot.*;
import com.gregtechceu.gtceu.api.mui.widgets.textfield.TextFieldWidget;
import com.gregtechceu.gtceu.client.mui.screen.ModularPanel;
import com.gregtechceu.gtceu.client.mui.screen.RichTooltip;
import com.gregtechceu.gtceu.client.mui.screen.UISettings;
import com.gregtechceu.gtceu.client.mui.screen.viewport.ModularGuiContext;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.registries.ForgeRegistries;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.DoubleUnaryOperator;

public class TestMuiMachine extends MetaMachine implements IMuiMachine {

    private static final Object2IntMap<Item> handlerSizeMap = new Object2IntOpenHashMap<>() {

        {
            put(Items.DIAMOND, 9);
            put(Items.EMERALD, 9);
            put(Items.GOLD_INGOT, 7);
            put(Items.IRON_INGOT, 6);
            put(Items.CLAY_BALL, 2);
            defaultReturnValue(3);
        }
    };

    private final FluidTank fluidTank = new FluidTank(10000);
    private final FluidTank fluidTankPhantom = new FluidTank(Integer.MAX_VALUE);
    private long time = 0;
    private int val, val2 = 0;
    private String value = "";
    private double doubleValue = 1;
    private final int duration = 80;
    private int progress = 0;
    private int cycleState = 0;
    private ItemStack displayItem = new ItemStack(Items.DIAMOND);
    private final IItemHandlerModifiable inventory = new ItemStackHandler(2) {

        @Override
        public int getSlotLimit(int slot) {
            return slot == 0 ? Integer.MAX_VALUE : 64;
        }
    };
    private final ItemStackHandler bigInventory = new ItemStackHandler(9);

    private final ItemStackHandler mixerItems = new ItemStackHandler(4);
    private final ItemStackHandler smallInv = new ItemStackHandler(4);
    private final FluidTank mixerFluids1 = new FluidTank(16000);
    private final FluidTank mixerFluids2 = new FluidTank(16000);
    private final ItemStackHandler craftingInventory = new ItemStackHandler(10);
    private final ItemStackHandler storageInventory0 = new ItemStackHandler(1);
    private final Map<Item, ItemStackHandler> stackHandlerMap = new Object2ObjectOpenHashMap<>();

    private int num = 2;

    private TickableSubscription sub;

    public TestMuiMachine(IMachineBlockEntity holder) {
        super(holder);
        sub = subscribeServerTick(this::tick);
    }

    @Override
    public ModularPanel buildUI(PosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        // settings.customContainer(() -> new CraftingModularContainer(3, 3, this.craftingInventory));

        syncManager.registerSlotGroup("item_inv", 3);
        syncManager.registerSlotGroup("mixer_items", 2);

        syncManager.syncValue("mixer_fluids", 0, SyncHandlers.fluidSlot(this.mixerFluids1));
        syncManager.syncValue("mixer_fluids", 1, SyncHandlers.fluidSlot(this.mixerFluids2));
        IntSyncValue cycleStateValue = new IntSyncValue(() -> this.cycleState, val -> this.cycleState = val);
        syncManager.syncValue("cycle_state", cycleStateValue);
        syncManager.syncValue("display_item", GenericSyncValue.forItem(() -> this.displayItem, null));
        syncManager.bindPlayerInventory(data.getPlayer());

        DynamicSyncHandler dynamicSyncHandler = new DynamicSyncHandler()
                .widgetProvider((syncManager1, packet) -> {
                    ItemStack itemStack = packet.readItem();
                    if (itemStack.isEmpty()) return new EmptyWidget();
                    Item item = itemStack.getItem();
                    ItemStackHandler handler = stackHandlerMap.computeIfAbsent(item,
                            k -> new ItemStackHandler(handlerSizeMap.getInt(k)));
                    String name = item.getName(itemStack).toString();
                    Flow flow = Flow.row();
                    for (int i = 0; i < handler.getSlots(); i++) {
                        int finalI = i;
                        flow.child(new ItemSlot()
                                .syncHandler(syncManager1.getOrCreateSyncHandler(name, i, ItemSlotSH.class,
                                        () -> new ItemSlotSH(new ModularSlot(handler, finalI)))));
                    }
                    return flow;
                });

        Rectangle colorPickerBackground = new Rectangle().setColor(Color.RED.main);
        ModularPanel panel = new ModularPanel("test_tile");
        IPanelHandler panelSyncHandler = syncManager.panel("other_panel", this::openSecondWindow, true);
        IPanelHandler colorPicker = IPanelHandler.simple(panel,
                (mainPanel,
                 player) -> new ColorPickerDialog(colorPickerBackground::setColor, colorPickerBackground.getColor(),
                         true)
                         .setDraggable(true)
                         .relative(panel)
                         .top(0)
                         .rightRel(1f),
                true);
        PagedWidget.Controller tabController = new PagedWidget.Controller();
        panel.flex()                        // returns object which is responsible for sizing
                .size(176, 220)       // set a static size for the main panel
                .align(Alignment.Center);    // center the panel in the screen
        panel
                .child(new Row()
                        .debugName("Tab row")
                        .coverChildren()
                        .topRel(0f, 4, 1f)
                        .child(new PageButton(0, tabController)
                                .tab(GTGuiTextures.TAB_TOP, -1)
                                .overlay(new EntityDrawable(data.getPlayer())))
                        .child(new PageButton(1, tabController)
                                .tab(GTGuiTextures.TAB_TOP, 0)
                                .overlay(new ItemDrawable(Items.OAK_SAPLING).asIcon()))
                        .child(new PageButton(2, tabController)
                                .tab(GTGuiTextures.TAB_TOP, 0)
                                .overlay(new ItemDrawable(Items.COMPASS).asIcon()))
                        .child(new PageButton(3, tabController)
                                .tab(GTGuiTextures.TAB_TOP, 0)
                                .overlay(new ItemDrawable(Blocks.CHEST).asIcon()))
                        .child(new PageButton(4, tabController)
                                .tab(GTGuiTextures.TAB_TOP, 0)
                                .overlay(new ItemDrawable(Items.ENDER_EYE).asIcon())))

                .child(new Expandable()
                        .debugName("expandable")
                        .top(0)
                        .leftRelOffset(1f, 1)
                        .background(GTGuiTextures.BACKGROUND)
                        .excludeAreaInXei()
                        .stencilTransform((r, expanded) -> {
                            r.width = Math.max(20, r.width - 5);
                            r.height = Math.max(20, r.height - 5);
                        })
                        .animationDuration(500)
                        .interpolation(Interpolation.BOUNCE_OUT)
                        .normalView(new ItemDrawable(Blocks.CRAFTING_TABLE).asIcon().asWidget().size(20).pos(0, 0))
                        .expandedView(new ParentWidget<>()
                                .debugName("crafting tab")
                                .coverChildren()
                                .child(new ItemDrawable(Blocks.CRAFTING_TABLE).asIcon().asWidget().size(20).pos(0, 0))
                                .child(SlotGroupWidget.builder()
                                        .row("III  D")
                                        .row("III  O")
                                        .row("III   ")
                                        .key('I', i -> new ItemSlot().slot(new ModularSlot(this.craftingInventory, i)))
                                        .key('O',
                                                new ItemSlot().slot(new ModularCraftingSlot(this.craftingInventory, 9)))
                                        .key('D',
                                                new ItemDisplayWidget().syncHandler("display_item").displayAmount(true))
                                        .build()
                                        .margin(5, 5, 20, 5).debugName("crafting"))))

                .child(Flow.column()
                        .sizeRel(1f)
                        .paddingBottom(7)
                        .child(new ParentWidget<>()
                                .expanded()
                                .widthRel(1f)
                                .child(new PagedWidget<>()
                                        .debugName("root parent")
                                        .sizeRel(1f)
                                        .controller(tabController)
                                        .addPage(new ParentWidget<>()
                                                .debugName("page 1 parent")
                                                .sizeRel(1f, 1f)
                                                .padding(7, 0)
                                                .child(new Row()
                                                        .debugName("buttons, slots and more tests")
                                                        .height(137)
                                                        .coverChildrenWidth()
                                                        .alignY(Alignment.Center)
                                                        // .padding(7)
                                                        .child(new Column()
                                                                .debugName("buttons and slots test")
                                                                .coverChildren()
                                                                .marginRight(8)
                                                                // .flex(flex -> flex.height(0.5f))
                                                                // .widthRel(0.5f)
                                                                .crossAxisAlignment(Alignment.CrossAxis.CENTER)
                                                                .child(new ButtonWidget<>()
                                                                        .size(60, 18)
                                                                        .overlay(IKey.dynamic(() -> Component
                                                                                .literal("Button " + this.val))))
                                                                .child(new FluidSlot()
                                                                        .margin(2)
                                                                        .syncHandler(
                                                                                SyncHandlers.fluidSlot(this.fluidTank)))
                                                                .child(new ButtonWidget<>()
                                                                        .size(60, 18)
                                                                        .tooltip(tooltip -> {
                                                                            tooltip.showUpTimer(10);
                                                                            tooltip.addLine(IKey.str("Test Line g"));
                                                                            tooltip.addLine(IKey.str(
                                                                                    "An image inside of a tooltip:"));
                                                                            tooltip.addLine(GTGuiTextures.MUI_LOGO
                                                                                    .asIcon().size(50)
                                                                                    .alignment(Alignment.TopCenter));
                                                                            tooltip.addLine(
                                                                                    IKey.str("And here a circle:"));
                                                                            tooltip.addLine(new Circle()
                                                                                    .setColor(Color.RED.darker(2),
                                                                                            Color.RED.brighter(2))
                                                                                    .asIcon()
                                                                                    .size(20))
                                                                                    .addLine(new ItemDrawable(
                                                                                            new ItemStack(
                                                                                                    Items.DIAMOND))
                                                                                            .asIcon())
                                                                                    .pos(RichTooltip.Pos.LEFT);
                                                                        })
                                                                        .onMousePressed(
                                                                                (mouseX, mouseY, mouseButton) -> {
                                                                                    // panel.getScreen().close(true);
                                                                                    // panel.getScreen().openDialog("dialog",
                                                                                    // this::buildDialog,
                                                                                    // ModularUI.LOGGER::info);
                                                                                    // openSecondWindow(context).openIn(panel.getScreen());
                                                                                    panelSyncHandler.openPanel();
                                                                                    return true;
                                                                                })
                                                                        // .flex(flex -> flex.left(3)) // ?
                                                                        .overlay(IKey.str("Button 2")))
                                                                .child(new TextFieldWidget()
                                                                        .size(60, 18)
                                                                        .setTextAlignment(Alignment.Center)
                                                                        .value(SyncHandlers.string(() -> this.value,
                                                                                val -> this.value = val))
                                                                        .margin(0, 2)
                                                                        .hintText(Component.literal("hint")))
                                                                .child(new TextFieldWidget()
                                                                        .size(60, 18)
                                                                        .paddingTop(1)
                                                                        .value(SyncHandlers.doubleNumber(
                                                                                () -> this.doubleValue,
                                                                                val -> this.doubleValue = val))
                                                                        .setNumbersDouble(
                                                                                DoubleUnaryOperator.identity())
                                                                        .hintText(Component.literal("number")))
                                                                // .child(IKey.str("Test
                                                                // string").asWidget().padding(2).debugName("test
                                                                // string"))
                                                                .child(new ScrollingTextWidget(
                                                                        IKey.str("Very very long test string"))
                                                                        .widthRel(1f).height(16))
                                                        // .child(IKey.EMPTY.asWidget().debugName("Empty IKey"))
                                                        )
                                                        .child(new Column()
                                                                .debugName("button and slots test 2")
                                                                .coverChildren()
                                                                // .widthRel(0.5f)
                                                                .crossAxisAlignment(Alignment.CrossAxis.CENTER)
                                                                .child(new ProgressWidget()
                                                                        .progress(() -> this.progress /
                                                                                (double) this.duration)
                                                                        .texture(GTGuiTextures.PROGRESS_BAR_ARROW, 20))
                                                                .child(new ProgressWidget()
                                                                        .progress(() -> this.progress /
                                                                                (double) this.duration)
                                                                        .texture(GTGuiTextures.PROGRESS_BAR_MIXER, 20)
                                                                        .direction(
                                                                                ProgressWidget.Direction.CIRCULAR_CW))
                                                                .child(new Row().coverChildrenWidth().height(22)
                                                                        .child(new ToggleButton()
                                                                                .value(new BoolValue.Dynamic(
                                                                                        () -> cycleStateValue
                                                                                                .getIntValue() == 0,
                                                                                        val -> cycleStateValue
                                                                                                .setIntValue(0)))
                                                                                .overlay(GTGuiTextures.CYCLE_BUTTON
                                                                                        .getSubArea(0, 0, 1, 1 / 3f)))
                                                                        .child(new ToggleButton()
                                                                                .value(new BoolValue.Dynamic(
                                                                                        () -> cycleStateValue
                                                                                                .getIntValue() == 1,
                                                                                        val -> cycleStateValue
                                                                                                .setIntValue(1)))
                                                                                .overlay(GTGuiTextures.CYCLE_BUTTON
                                                                                        .getSubArea(0, 1 / 3f, 1,
                                                                                                2 / 3f)))
                                                                        .child(new ToggleButton()
                                                                                .value(new BoolValue.Dynamic(
                                                                                        () -> cycleStateValue
                                                                                                .getIntValue() == 2,
                                                                                        val -> cycleStateValue
                                                                                                .setIntValue(2)))
                                                                                .overlay(GTGuiTextures.CYCLE_BUTTON
                                                                                        .getSubArea(0, 2 / 3f, 1, 1))))
                                                                /*
                                                                 * .child(new CycleButtonWidget()
                                                                 * .length(3)
                                                                 * .texture(GTGuiTextures.CYCLE_BUTTON)
                                                                 * .addTooltip(0, "State 1")
                                                                 * .addTooltip(1, "State 2")
                                                                 * .addTooltip(2, "State 3")
                                                                 * .background(GTGuiTextures.BUTTON)
                                                                 * .value(SyncHandlers.intNumber(() -> this.cycleState,
                                                                 * val -> this.cycleState = val)))
                                                                 */
                                                                .child(new ItemSlot()
                                                                        .slot(SyncHandlers.itemSlot(this.inventory, 0)
                                                                                .ignoreMaxStackSize(true)
                                                                                .singletonSlotGroup()))
                                                                .child(new FluidSlot()
                                                                        .margin(2)
                                                                        .width(30)
                                                                        .syncHandler(SyncHandlers
                                                                                .fluidSlot(this.fluidTankPhantom)
                                                                                .phantom(true))))))
                                        .addPage(new Column()
                                                .debugName("Slots test page")
                                                .coverChildren()
                                                // .height(120)
                                                .padding(7)
                                                .alignX(0.5f)
                                                .mainAxisAlignment(Alignment.MainAxis.START)
                                                .childPadding(2)
                                                // .child(SlotGroupWidget.playerInventory().left(0))
                                                .child(SlotGroupWidget.builder()
                                                        .matrix("III", "III", "III")
                                                        .key('I', index -> {
                                                            // 4 is the middle slot with a negative priority -> shift
                                                            // click prioritises middle slot
                                                            if (index == 4) {
                                                                return new ItemSlot().slot(
                                                                        SyncHandlers.itemSlot(this.bigInventory, index)
                                                                                .singletonSlotGroup(-100));
                                                            }
                                                            return new ItemSlot().slot(
                                                                    SyncHandlers.itemSlot(this.bigInventory, index)
                                                                            .slotGroup("item_inv"));
                                                        })
                                                        .build().debugName("9 slot inv")
                                                        // .marginBottom(2)
                                                        .child(new SortButtons()
                                                                .slotGroup("item_inv")
                                                                .right(0).top(-11)))
                                                .child(SlotGroupWidget.builder()
                                                        .row("FII")
                                                        .row("FII")
                                                        .key('F',
                                                                index -> new FluidSlot().syncHandler("mixer_fluids",
                                                                        index))
                                                        .key('I',
                                                                index -> ItemSlot.create(index >= 2)
                                                                        .slot(new ModularSlot(this.mixerItems, index)
                                                                                .slotGroup("mixer_items")))
                                                        .build().debugName("mixer inv"))
                                                .child(new Row()
                                                        .coverChildrenHeight()
                                                        .child(new CycleButtonWidget()
                                                                .size(20, 20)
                                                                .stateCount(3)
                                                                .stateOverlay(GTGuiTextures.CYCLE_BUTTON)
                                                                .value(new IntSyncValue(() -> this.val2,
                                                                        val -> this.val2 = val))
                                                                .margin(8, 0))
                                                        .child(IKey.str("Hello World").asWidget().height(18)))
                                                .child(new SpecialButton(
                                                        IKey.str("A very long string that looks cool when animated")
                                                                .withAnimation())
                                                        .height(14)
                                                        .widthRel(1f))
                                        /*
                                         * GuiTextures.LOGO.asIcon()
                                         * .size(80, 80)
                                         * .asWidget()
                                         * .flex(flex -> flex.width(1f).height(1f))
                                         */)
                                        .addPage(new ParentWidget<>()
                                                .debugName("page 3 parent")
                                                .sizeRel(1f, 1f)
                                                .padding(7)
                                                // .child(SlotGroupWidget.playerInventory())
                                                .child(new SliderWidget()
                                                        .widthRel(1f).bottom(50).height(16) // test overwriting of units
                                                        .top(7)
                                                        .stopper(0, 10, 20, 30, 40, 50, 60, 70, 80, 90, 100)
                                                        .background(GTGuiTextures.FLUID_SLOT))
                                                .child(new ButtonWidget<>()
                                                        .debugName("color picker button")
                                                        .top(25)
                                                        .background(colorPickerBackground)
                                                        .disableHoverBackground()
                                                        .onMousePressed((mouseX, mouseY, mouseButton) -> {
                                                            colorPicker.openPanel();
                                                            return true;
                                                        }))
                                                .child(new ListWidget<>()
                                                        .debugName("test config list")
                                                        .widthRel(1f).top(50).bottom(2)
                                                        /*
                                                         * .child(new Rectangle().setColor(0xFF606060).asWidget()
                                                         * .top(1)
                                                         * .left(32)
                                                         * .size(1, 40))
                                                         */
                                                        .child(new Row()
                                                                .debugName("test config 1")
                                                                .widthRel(1f).coverChildrenHeight()
                                                                .crossAxisAlignment(Alignment.CrossAxis.CENTER)
                                                                .childPadding(2)
                                                                .child(new CycleButtonWidget()
                                                                        .value(new BoolValue(false))
                                                                        .stateOverlay(GTGuiTextures.CHECK_BOX)
                                                                        .size(14, 14)
                                                                        .margin(8, 4))
                                                                .child(IKey.str("Boolean config").asWidget()
                                                                        .height(14)))
                                                        .child(new Row()
                                                                .debugName("test config 2")
                                                                .widthRel(1f).height(14)
                                                                .childPadding(2)
                                                                .child(new TextFieldWidget()
                                                                        .value(new IntValue.Dynamic(() -> this.num,
                                                                                val -> this.num = val))
                                                                        .setNumbers(1, Short.MAX_VALUE)
                                                                        .setTextAlignment(Alignment.Center)
                                                                        .background(
                                                                                new Rectangle().setColor(0xFFb1b1b1))
                                                                        .setTextColor(IKey.TEXT_COLOR)
                                                                        .size(20, 14))
                                                                .child(IKey.str("Number config").asWidget()
                                                                        .height(14)))
                                                        .child(IKey.str("Config title").asWidget()
                                                                .color(0xFF404040)
                                                                .alignment(Alignment.CenterLeft)
                                                                .left(5).height(14)
                                                                .tooltip(tooltip -> tooltip.showUpTimer(10)
                                                                        .addLine(IKey.str("Config title tooltip"))))
                                                        .child(new Row()
                                                                .debugName("test config 3")
                                                                .widthRel(1f).height(14)
                                                                .childPadding(2)
                                                                .child(new CycleButtonWidget()
                                                                        .value(new BoolValue(false))
                                                                        .stateOverlay(GTGuiTextures.CHECK_BOX)
                                                                        .size(14, 14))
                                                                .child(IKey.str("Boolean config 3").asWidget()
                                                                        .height(14)))))
                                        .addPage(new ParentWidget<>()
                                                .debugName("page 4 storage")
                                                .sizeRel(1f)
                                                .child(new Column()
                                                        .padding(7)
                                                        .child(new ItemSlot()
                                                                .slot(new ModularSlot(this.storageInventory0, 0)
                                                                        .changeListener(((newItem, onlyAmountChanged,
                                                                                          client, init) -> {
                                                                            if (client && !onlyAmountChanged) {
                                                                                dynamicSyncHandler.notifyUpdate(
                                                                                        packet -> packet
                                                                                                .writeItem(newItem));
                                                                            }
                                                                        }))))
                                                        .child(new DynamicSyncedWidget<>()
                                                                .widthRel(1f)
                                                                .syncHandler(dynamicSyncHandler))))
                                        .addPage(createSchemaPage(data))))
                        .child(SlotGroupWidget.playerInventory(false)));
        /*
         * panel.child(new ButtonWidget<>()
         * .flex(flex -> flex.size(60, 20)
         * .top(7)
         * .left(0.5f))
         * .background(GuiTextures.BUTTON, IKey.dynamic(() -> "Button " + this.val)))
         * .child(SlotGroup.playerInventory())
         * .child(new FluidSlot().flex(flex -> flex
         * .top(30)
         * .left(0.5f))
         * .setSynced("fluid_slot"));
         */
        return panel;
    }

    private IWidget createSchemaPage(GuiData data) {
        ParentWidget<?> page = new ParentWidget<>();
        page.debugName("page 5 schema");
        page.sizeRel(1f);
        page.child(IKey.str("schema").asWidget());
        /*
         * if (getLevel().isClientSide()) {}
         * page.child(new SchemaWidget(new SchemaRenderer(ArraySchema.of(data.getPlayer(), 5))
         * .highlightRenderer(new BlockHighlight(Color.withAlpha(Color.GREEN.brighter(1), 0.9f), 1 / 32f)))
         * .pos(20, 20)
         * .size(100, 100));
         */
        return page;
    }

    public ModularPanel openSecondWindow(PanelSyncManager syncManager, IPanelHandler syncHandler) {
        ModularPanel panel = new Dialog<>("second_window", null)
                .setDisablePanelsBelow(false)
                .setCloseOnOutOfBoundsClick(false)
                .setDraggable(true)
                .size(100, 100);
        SlotGroup slotGroup = new SlotGroup("small_inv", 2);
        IntSyncValue timeSync = new IntSyncValue(() -> (int) java.lang.System.currentTimeMillis());
        syncManager.syncValue(123456, timeSync);
        syncManager.registerSlotGroup(slotGroup);
        AtomicInteger number = new AtomicInteger(0);
        syncManager.syncValue("int_value", new IntSyncValue(number::get, number::set));
        IPanelHandler panelSyncHandler = syncManager.panel("other_panel_2",
                (syncManager1, syncHandler1) -> openThirdWindow(syncManager1, syncHandler1, number), true);
        panel.child(ButtonWidget.panelCloseButton())
                .child(new ButtonWidget<>()
                        .size(10).top(14).right(4)
                        .overlay(IKey.str("3"))
                        .onMousePressed((mouseX, mouseY, mouseButton) -> {
                            panelSyncHandler.openPanel();
                            return true;
                        }))
                .child(IKey.str("2nd Panel")
                        .asWidget()
                        .pos(5, 5))
                .child(SlotGroupWidget.builder()
                        .row("II")
                        .row("II")
                        .key('I', i -> new ItemSlot().slot(new ModularSlot(smallInv, i).slotGroup(slotGroup)))
                        .build()
                        .center())
                .child(new ButtonWidget<>()
                        .bottom(5)
                        .right(5)
                        .tooltip(richTooltip -> richTooltip.textColor(Color.RED.main).add("WARNING! Very Dangerous"))
                        .onMousePressed((mouseX, mouseY, mouseButton) -> {
                            if (!panelSyncHandler.isPanelOpen()) {
                                panelSyncHandler.deleteCachedPanel();
                                number.incrementAndGet();
                            }
                            return true;
                        }));
        return panel;
    }

    public ModularPanel openThirdWindow(PanelSyncManager syncManager, IPanelHandler syncHandler,
                                        AtomicInteger integer) {
        ModularPanel panel = new Dialog<>("third_window", null)
                .setDisablePanelsBelow(false)
                .setCloseOnOutOfBoundsClick(false)
                .setDraggable(true)
                .size(50, 50);
        panel.child(ButtonWidget.panelCloseButton())
                .child(IKey.str("3rd Panel: " + integer.get())
                        .asWidget()
                        .pos(5, 17));
        return panel;
    }

    public @NotNull ModularPanel buildSearchTest(ModularGuiContext context) {
        List<String> items = Arrays.asList("Chicken", "Jockey", "Flint", "Steel", "Steve", "Diamond", "Ingot", "Iron",
                "Armor", "Greg");
        StringValue searchValue = new StringValue("");
        return ModularPanel.defaultPanel("search", 100, 150)
                .child(Flow.column()
                        .padding(5)
                        .child(new TextFieldWidget()
                                .value(searchValue)
                                .height(16)
                                .widthRel(1f))
                        .child(new ListWidget<>()
                                .collapseDisabledChild()
                                .expanded()
                                .widthRel(1f)
                                .children(items.size(), i -> new TextWidget(IKey.str(items.get(i)))
                                        .alignment(Alignment.Center)
                                        .color(Color.WHITE.main)
                                        .widthRel(1f)
                                        .height(16)
                                        .background(GTGuiTextures.MC_BUTTON)
                                        .setEnabledIf(w -> items.get(i).toLowerCase()
                                                .contains(searchValue.getStringValue())))));
    }

    public void tick() {
        if (getLevel().isClientSide) {
            if (this.time++ % 20 == 0) {
                this.val++;
            }
        } else {
            if (this.time++ % 20 == 0) {
                if (++this.val2 == 3) this.val2 = 0;
                Collection<Item> vals = ForgeRegistries.ITEMS.getValues();
                Item item = vals.stream().skip(new Random().nextInt(vals.size())).findFirst().orElse(Items.DIAMOND);
                this.displayItem = new ItemStack(item, 26735987);
            }
        }
        if (++this.progress == this.duration) {
            this.progress = 0;
        }
    }

    private static class SpecialButton extends ButtonWidget<SpecialButton> {

        private final AnimatedText animatedKey;

        private SpecialButton(AnimatedText animatedKey) {
            this.animatedKey = animatedKey.stopAnimation().forward(true);
            this.animatedKey.reset();
        }

        @Override
        public void draw(ModularGuiContext context) {
            this.animatedKey.draw(context, 0, 0, getArea().w(), getArea().h(), WidgetTheme.getDefault()/*
                                                                                                        * getActiveWidgetTheme
                                                                                                        * (widgetTheme,
                                                                                                        * isHovering())
                                                                                                        */);
        }

        @Override
        public void onMouseStartHover() {
            super.onMouseStartHover();
            this.animatedKey.startAnimation().forward(true);
        }

        @Override
        public void onMouseEndHover() {
            super.onMouseEndHover();
            this.animatedKey.forward(false);
        }
    }
}

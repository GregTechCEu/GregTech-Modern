package brachy.modularui.test;

import brachy.modularui.ModularUI;
import brachy.modularui.api.IPanelHandler;
import brachy.modularui.api.IUIHolder;
import brachy.modularui.api.drawable.Text;
import brachy.modularui.drawable.Circle;
import brachy.modularui.drawable.GuiTextures;
import brachy.modularui.drawable.ItemDrawable;
import brachy.modularui.drawable.progress.CircularProgressDrawable;
import brachy.modularui.drawable.progress.CompositeProgress;
import brachy.modularui.drawable.progress.ProgressDrawable;
import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.ModularScreen;
import brachy.modularui.screen.RichTooltip;
import brachy.modularui.screen.UISettings;
import brachy.modularui.utils.Alignment;
import brachy.modularui.utils.Color;
import brachy.modularui.utils.Interpolation;
import brachy.modularui.utils.MultiFluidTankHandler;
import brachy.modularui.value.sync.DoubleSyncValue;
import brachy.modularui.value.sync.DynamicLinkedSyncHandler;
import brachy.modularui.value.sync.DynamicSyncHandler;
import brachy.modularui.value.sync.FluidSlotSyncHandler;
import brachy.modularui.value.sync.GenericListSyncHandler;
import brachy.modularui.value.sync.GenericSyncValue;
import brachy.modularui.value.sync.IntSyncValue;
import brachy.modularui.value.sync.ItemSlotSyncHandler;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widget.EmptyWidget;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widgets.ButtonWidget;
import brachy.modularui.widgets.CycleButtonWidget;
import brachy.modularui.widgets.Expandable;
import brachy.modularui.widgets.ItemDisplayWidget;
import brachy.modularui.widgets.PageButton;
import brachy.modularui.widgets.PagedWidget;
import brachy.modularui.widgets.ProgressWidget;
import brachy.modularui.widgets.SlotGroupWidget;
import brachy.modularui.widgets.ToggleButton;
import brachy.modularui.widgets.dynamic.DynamicWidget;
import brachy.modularui.widgets.layout.Flow;
import brachy.modularui.widgets.slot.FluidSlot;
import brachy.modularui.widgets.slot.ItemSlot;
import brachy.modularui.widgets.slot.ModularCraftingSlot;
import brachy.modularui.widgets.slot.ModularSlot;
import brachy.modularui.widgets.slot.PhantomItemSlot;
import brachy.modularui.widgets.slot.SlotGroup;

import io.netty.buffer.ByteBuf;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.VarInt;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Various test and demos for synced widgets, slots and JEI interactions. Anything that doesn't fall into any of those categories goes into
 * {@link TestGuis}.
 */
public class TestBlockEntity extends AbstractBlockEntity implements IUIHolder<PosGuiData> {

    private static final Object2IntMap<Item> handlerSizeMap = new Object2IntOpenHashMap<>() {{
        put(Items.DIAMOND, 9);
        put(Items.EMERALD, 9);
        put(Items.GOLD_INGOT, 7);
        put(Items.IRON_INGOT, 6);
        put(Items.CLAY_BALL, 2);
        defaultReturnValue(3);
    }};

    private long time = 0;
    private final int duration = 80;
    private int progress = 0;
    private int cycleState = 0;
    private List<Integer> serverInts = new ArrayList<>();
    private ItemStack displayItem = new ItemStack(Items.DIAMOND);
    private final ItemStackHandler storage = new ItemStackHandler(9);
    private final ItemStackHandler smallStorage = new ItemStackHandler(9);
    private final ItemStackHandler oversizedStorage = new ItemStackHandler(3) {
        @Override
        public int getSlotLimit(int slot) {
            return 10000;
        }
    };
    private final ItemStackHandler phantomStorage = new ItemStackHandler(3);
    private final MultiFluidTankHandler fluidStorage = new MultiFluidTankHandler(3, 10000);
    private final MultiFluidTankHandler phantomFluidStorage = new MultiFluidTankHandler(3, 500000);
    private final ItemStackHandler craftingInventory = new ItemStackHandler(10);
    private final ItemStackHandler storageInventory0 = new ItemStackHandler(1);
    private final Map<Item, ItemStackHandler> stackHandlerMap = new Object2ObjectOpenHashMap<>();

    public TestBlockEntity(BlockPos pos, BlockState blockState) {
        super(TestRegistration.TEST_BLOCK_ENTITY.get(), pos, blockState);

    }

    @Override
    public ModularScreen createScreen(PosGuiData data, ModularPanel<?> mainPanel) {
        return new ModularScreen(ModularUI.MOD_ID, mainPanel);
    }

    @Override
    public ModularPanel<?> buildUI(PosGuiData guiData, PanelSyncManager syncManager, UISettings settings) {
        //settings.customContainer(() -> new CraftingModularContainer(3, 3, this.craftingInventory));
        //settings.customGui(() -> TestGuiContainer::new);

        syncManager.registerSlotGroup("item_inv", 3);
        syncManager.registerSlotGroup(new SlotGroup("crafting", 3, SlotGroup.STORAGE_SLOT_PRIO * 2).setAllowSorting(false));
        IntSyncValue cycleStateValue = new IntSyncValue(() -> this.cycleState, val -> this.cycleState = val).allowC2S();
        syncManager.getHyperVisor().syncValue("cycle_state", cycleStateValue);
        syncManager.syncValue("progress", new DoubleSyncValue(() -> (double) this.progress / this.duration));
        syncManager.syncValue("display_item", GenericSyncValue.forItem(() -> this.displayItem, null));
        GenericListSyncHandler<ByteBuf, Integer> numberListSyncHandler = GenericListSyncHandler.<ByteBuf, Integer>builder()
                .getter(() -> this.serverInts)
                .setter(v -> this.serverInts = v)
                .serializer(VarInt::write)
                .deserializer(VarInt::read)
                .immutableCopy()
                .build();
        syncManager.syncValue("number_list", numberListSyncHandler);
        syncManager.bindPlayerInventory(guiData.getPlayer());

        DynamicSyncHandler dynamicSyncHandler = new DynamicSyncHandler()
                .widgetProvider((syncManager1, packet) -> {
                    ItemStack itemStack = ItemStack.OPTIONAL_STREAM_CODEC.decode(packet);
                    if (itemStack.isEmpty()) return new EmptyWidget();
                    Item item = itemStack.getItem();
                    ItemStackHandler handler = stackHandlerMap.computeIfAbsent(item, k -> new ItemStackHandler(handlerSizeMap.getInt(k)));

                    String name = BuiltInRegistries.ITEM.getKey(item).toString();
                    Flow flow = Flow.row();
                    for (int i = 0; i < handler.getSlots(); i++) {
                        int finalI = i;
                        flow.child(new ItemSlot()
                                .syncHandler(syncManager1.getOrCreateSyncHandler(name, i, ItemSlotSyncHandler.class, () -> new ItemSlotSyncHandler(new ModularSlot(handler, finalI)))));
                    }
                    return flow;
                });

        DynamicLinkedSyncHandler<ByteBuf, GenericListSyncHandler<ByteBuf, Integer>> dynamicLinkedSyncHandler = new DynamicLinkedSyncHandler<>(numberListSyncHandler)
                .widgetProvider((syncManager1, value1) -> {
                    List<Integer> vals = value1.getValue();
                    return Flow.row()
                            .widthRel(1f)
                            .coverChildrenHeight()
                            .mainAxisAlignment(Alignment.MainAxis.SPACE_AROUND)
                            .children(vals.size(), i -> Text.str(String.valueOf(vals.get(i))).asWidget().padding(2))
                            .name("synced number col");
                });

        ModularPanel<?> panel = new ModularPanel<>("test_tile");
        IPanelHandler panelSyncHandler = syncManager.syncedPanel("other_panel", true, this::openSecondWindow);

        PagedWidget.Controller tabController = new PagedWidget.Controller();
        panel.size(176, 210)
                .child(Flow.row()
                        .name("Tab row")
                        .coverChildren()
                        .topRel(0f, 4, 1f)
                        .child(new PageButton(0, tabController)
                                .tab(GuiTextures.TAB_TOP, -1)
                                .overlay(new ItemDrawable(Blocks.CHEST).asIcon()))
                        .child(new PageButton(1, tabController)
                                .tab(GuiTextures.TAB_TOP, 0)
                                .overlay(new ItemDrawable(Blocks.ENDER_CHEST).asIcon())))
                .child(new Expandable()
                        .name("expandable")
                        .top(0)
                        .leftRelOffset(1f, 1)
                        .background(GuiTextures.MC_BACKGROUND)
                        .excludeAreaInRecipeViewer()
                        .stencilTransform((r, expanded) -> {
                            r.width = Math.max(20, r.width - 5);
                            r.height = Math.max(20, r.height - 5);
                        })
                        .animationDuration(500)
                        .interpolation(Interpolation.BOUNCE_OUT)
                        .collapsedView(new ItemDrawable(Blocks.CRAFTING_TABLE).asIcon().asWidget().size(20).pos(0, 0))
                        .expandedView(new ParentWidget<>()
                                .name("crafting tab")
                                .coverChildren()
                                .child(new ItemDrawable(Blocks.CRAFTING_TABLE).asIcon().asWidget().size(20).pos(0, 0))
                                .child(Text.str("Expandable & Crafting Demo").asWidget().scale(0.7f).pos(20, 7))
                                .child(SlotGroupWidget.builder()
                                        .row("III  D")
                                        .row("III  O")
                                        .row("III   ")
                                        .key('I', i -> new ItemSlot().slot(new ModularSlot(this.craftingInventory, i))
                                                .addTooltipLine("This slot is empty"))
                                        .key('O', new ItemSlot().slot(new ModularCraftingSlot(this.craftingInventory, 9)
                                                .inputInventory(this.craftingInventory)
                                                .gridSize(3, 3)))
                                        .key('D', new ItemDisplayWidget().syncHandler("display_item").displayAmount(true))
                                        .slotGroup("crafting")
                                        .build()
                                        .margin(5, 5, 20, 5)
                                        .name("crafting"))))
                .child(Flow.col()
                        .name("main_col")
                        .sizeRel(1f)
                        .padding(7)
                        .childPadding(5)
                        .child(new PagedWidget<>()
                                .name("paged")
                                .widthRel(1f)
                                .expanded()
                                .controller(tabController)
                                .addPage(new ParentWidget<>()
                                        .name("page 1")
                                        .sizeRel(1f)
                                        .child(Flow.row()
                                                .crossAxisAlignment(Alignment.CrossAxis.START)
                                                .child(Flow.col()
                                                        .name("buttons_and_values_col")
                                                        .widthRel(0.5f)
                                                        .mainAxisAlignment(Alignment.MainAxis.SPACE_BETWEEN)
                                                        .child(Text.str("Storage slots").asWidget().scale(0.7f).padding(1))
                                                        .child(SlotGroupWidget.builder()
                                                                .matrix("III", "III")
                                                                .key('I', i -> new ItemSlot().slot(new ModularSlot(this.storage, i)))
                                                                .slotGroup("item_inv")
                                                                .build()
                                                                .placeSortButtonsTopRightVertical())
                                                        .child(new ButtonWidget<>()
                                                                .height(16)
                                                                .width(3 * 18)
                                                                .tooltip(tooltip -> {
                                                                    tooltip.showUpTimer(10);
                                                                    tooltip.addLine(Text.str("Test Line g"));
                                                                    tooltip.addLine(Text.str("An image inside of a tooltip:"));
                                                                    tooltip.addDrawableLine(GuiTextures.MUI_LOGO.asIcon().size(50).alignment(Alignment.TopCenter));
                                                                    tooltip.addLine(Text.str("And here a circle:"));
                                                                    tooltip.addDrawableLine(new Circle()
                                                                                    .color(Color.RED.darker(2), Color.RED.brighter(2))
                                                                                    .asIcon()
                                                                                    .size(20))
                                                                            .addDrawableLine(new ItemDrawable(new ItemStack(Items.DIAMOND)).asIcon())
                                                                            .pos(RichTooltip.Pos.LEFT);
                                                                })
                                                                .onMousePressed((context, mouseButton) -> {
                                                                    panelSyncHandler.openPanel();
                                                                    return true;
                                                                })
                                                                .overlay(Text.str("Open Sub Panel").scale(0.75f)))
                                                        .child(Flow.row()
                                                                .name("cycle_button_row")
                                                                .coverChildrenWidth().height(18)
                                                                .reverseLayout(false)
                                                                .child(new ToggleButton()
                                                                        .valueWrapped(cycleStateValue, 0)
                                                                        .overlay(GuiTextures.CYCLE_BUTTON_DEMO.getSubArea(0, 0, 1, 1 / 3f)))
                                                                .child(new ToggleButton()
                                                                        .valueWrapped(cycleStateValue, 1)
                                                                        .overlay(GuiTextures.CYCLE_BUTTON_DEMO.getSubArea(0, 1 / 3f, 1, 2 / 3f)))
                                                                .child(new ToggleButton()
                                                                        .valueWrapped(cycleStateValue, 2)
                                                                        .overlay(GuiTextures.CYCLE_BUTTON_DEMO.getSubArea(0, 2 / 3f, 1, 1))))
                                                        .child(Flow.row()
                                                                .name("progress_row")
                                                                .coverChildrenHeight()
                                                                .mainAxisAlignment(Alignment.MainAxis.SPACE_AROUND)
                                                                .child(new ProgressWidget()
                                                                        .syncHandler("progress")
                                                                        .texture(GuiTextures.PROGRESS_ARROW, ProgressDrawable.Direction.RIGHT)
                                                                        .size(20))
                                                                .child(CompositeProgress.circularLike4Slice(
                                                                                GuiTextures.PROGRESS_CYCLE.getSubArea(0, 0f, 1, 0.5f),
                                                                                GuiTextures.PROGRESS_CYCLE.getSubArea(0, 0.5f, 1, 1f),
                                                                                CircularProgressDrawable.Direction.CW)
                                                                        .asWidget()
                                                                        .syncHandler("progress")
                                                                        .size(20)))
                                                )
                                                .child(Flow.col()
                                                        .name("slots_col")
                                                        .widthRel(0.5f)
                                                        .heightRelOffset(1f, -6) // space for player sort buttons
                                                        .mainAxisAlignment(Alignment.MainAxis.SPACE_BETWEEN)
                                                        .child(Text.str("Oversized slots").asWidget().scale(0.7f).padding(1))
                                                        .child(SlotGroupWidget.builder()
                                                                .matrix("III")
                                                                .key('I', i -> new ItemSlot().slot(new ModularSlot(this.oversizedStorage, i).ignoreMaxStackSize(true)))
                                                                .build())
                                                        .child(Text.str("Phantom slots").asWidget().scale(0.7f).padding(1))
                                                        .child(SlotGroupWidget.builder()
                                                                .matrix("III")
                                                                .key('I', i -> new PhantomItemSlot().slot(new ModularSlot(this.phantomStorage, i).ignoreMaxStackSize(true)))
                                                                .build())
                                                        .child(Text.str("Fluid slots").asWidget().scale(0.7f).padding(1))
                                                        .child(SlotGroupWidget.builder()
                                                                .matrix("FFF")
                                                                .key('F', i -> new FluidSlot().syncHandler(new FluidSlotSyncHandler(this.fluidStorage, i)))
                                                                .build())
                                                        .child(Text.str("Phantom Fluid slots").asWidget().scale(0.7f).padding(1))
                                                        .child(SlotGroupWidget.builder()
                                                                .matrix("FFF")
                                                                .key('F', i -> new FluidSlot().syncHandler(new FluidSlotSyncHandler(this.phantomFluidStorage, i).phantom(true)))
                                                                .build())
                                                )))
                                .addPage(new ParentWidget<>()
                                        .name("dynamic_sync_page")
                                        .sizeRel(1f)
                                        .child(Flow.col()
                                                .name("dynamic widgets")
                                                .child(Text.str("Dynamic synced widget demo. Items act as keys to a unique storage with different amount of slots.").asWidget().scale(0.7f))
                                                .child(new ItemSlot()
                                                        .slot(new ModularSlot(this.storageInventory0, 0)
                                                                .changeListener(((oldItem, newItem, client, init) -> {
                                                                    if (client && !ItemStack.isSameItemSameComponents(oldItem, newItem)) {
                                                                        dynamicSyncHandler.notifyUpdate(packet -> ItemStack.OPTIONAL_STREAM_CODEC.encode(packet, newItem));
                                                                    }
                                                                }))))
                                                .child(new DynamicWidget<>()
                                                        .widthRel(1f)
                                                        .syncHandler(dynamicSyncHandler))
                                                .child(Text.str("Dynamic linked sync handler demo.").asWidget().scale(0.7f).marginTop(6))
                                                .child(new DynamicWidget<>()
                                                        .widthRel(1f)
                                                        .coverChildrenHeight()
                                                        .syncHandler(dynamicLinkedSyncHandler))
                                        )))
                        .child(SlotGroupWidget.playerInventory(false)));
        return panel;
    }

    public ModularPanel<?> openSecondWindow(PanelSyncManager syncManager, IPanelHandler syncHandler) {
        ModularPanel<?> panel = new ModularPanel<>("second_window")
                .disablePanelsBelow(false)
                .closeOnOutOfBoundsClick(false)
                .draggable(true)
                .size(100, 100);
        SlotGroup slotGroup = new SlotGroup("small_inv", 2);
        IntSyncValue timeSync = new IntSyncValue(() -> (int) java.lang.System.currentTimeMillis());
        syncManager.syncValue(123456, timeSync);
        syncManager.registerSlotGroup(slotGroup);
        AtomicInteger number = new AtomicInteger(0);
        syncManager.syncValue("int_value", new IntSyncValue(number::get, number::set));
        IPanelHandler panelSyncHandler = syncManager.syncedPanel("other_panel_2", true, (syncManager1, syncHandler1) ->
                openThirdWindow(syncManager1, syncHandler1, number));
        IntSyncValue num = syncManager.getHyperVisor().findSyncHandler("cycle_state", IntSyncValue.class);
        panel.child(ButtonWidget.panelCloseButton())
                .child(new ButtonWidget<>()
                        .size(10).top(14).right(4)
                        .overlay(Text.str("O"))
                        .addTooltipLine("Opens another sub panel")
                        .onMousePressed((context, mouseButton) -> {
                            panelSyncHandler.openPanel();
                            return true;
                        }))
                .child(Text.str("2nd Panel")
                        .asWidget()
                        .pos(5, 5))
                .child(SlotGroupWidget.builder()
                        .row("II")
                        .row("II")
                        .key('I', i -> new ItemSlot().slot(new ModularSlot(smallStorage, i).slotGroup(slotGroup)))
                        .build()
                        .center())
                .child(new CycleButtonWidget()
                        .size(16).pos(5, 5 + 11)
                        .value(num)
                        .stateOverlay(0, Text.str("1"))
                        .stateOverlay(1, Text.str("2"))
                        .stateOverlay(2, Text.str("3"))
                        .addTooltipLine(Text.str("Hyper Visor test")));
        return panel;
    }

    public ModularPanel<?> openThirdWindow(PanelSyncManager syncManager, IPanelHandler syncHandler, AtomicInteger integer) {
        ModularPanel<?> panel = new ModularPanel<>("third_window")
                .disablePanelsBelow(false)
                .closeOnOutOfBoundsClick(false)
                .draggable(true)
                .size(50, 50);
        panel.child(ButtonWidget.panelCloseButton())
                .child(Text.str("3rd Panel: " + integer.get())
                        .asWidget()
                        .pos(5, 17));
        return panel;
    }

    @Override
    public void update() {
        if (!getLevel().isClientSide) {
            if (this.time++ % 20 == 0) {
                this.displayItem = TestHandler.getRandomItem();
                this.displayItem.setCount(26735987);
            }
            if (++this.time % 60 == 0) {
                Random rnd = new Random();
                this.serverInts.clear();
                for (int i = 0; i < 5; i++) {
                    this.serverInts.add(rnd.nextInt(100));
                }
            }
            if (++this.progress == this.duration) {
                this.progress = 0;
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("item_inv", this.storage.serializeNBT(registries));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.storage.deserializeNBT(registries, tag.getCompound("item_inv"));
    }
}

package com.gregtechceu.gtceu.common.machine.multiblock.electric;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.IMonitorComponent;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.IntInputWidget;
import com.gregtechceu.gtceu.api.gui.widget.SlotWidget;
import com.gregtechceu.gtceu.api.item.IComponentItem;
import com.gregtechceu.gtceu.api.item.component.IItemComponent;
import com.gregtechceu.gtceu.api.item.component.IMonitorModuleItem;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.IDataInfoProvider;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.pattern.*;
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.item.PortableScannerBehavior;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.monitor.MonitorGroup;
import com.gregtechceu.gtceu.data.lang.LangHandler;
import com.gregtechceu.gtceu.utils.GTMath;
import com.gregtechceu.gtceu.utils.GTStringUtils;
import com.gregtechceu.gtceu.utils.GTUtil;

import com.lowdragmc.lowdraglib.gui.texture.*;
import com.lowdragmc.lowdraglib.gui.widget.*;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.items.IItemHandler;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CentralMonitorMachine extends WorkableElectricMultiblockMachine
                                   implements IMonitorComponent, IDataInfoProvider {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(CentralMonitorMachine.class,
            WorkableMultiblockMachine.MANAGED_FIELD_HOLDER);

    private static final Block[] VALID_BLOCKS = new Block[] {
            GTBlocks.CASING_ALUMINIUM_FROSTPROOF.get(),
    };

    public static final TraceabilityPredicate BLOCK_PREDICATE = Predicates.abilities(PartAbility.INPUT_ENERGY)
            .setExactLimit(1)
            .or(Predicates.abilities(PartAbility.DATA_ACCESS).setMaxGlobalLimited(1))
            .or(Predicates.machines(GTMachines.HULL))
            .or(Predicates.machines(GTMachines.BATTERY_BUFFER_4))
            .or(Predicates.machines(GTMachines.BATTERY_BUFFER_8))
            .or(Predicates.machines(GTMachines.BATTERY_BUFFER_16))
            .or(Predicates.machines(GTMachines.MONITOR))
            .or(Predicates.blocks(VALID_BLOCKS));

    @Persisted
    @DescSynced
    @Getter
    private int leftDist = 0, rightDist = 0, upDist = 0, downDist = 0;
    @Persisted
    @DescSynced
    @Getter
    private final List<MonitorGroup> monitorGroups = new ArrayList<>();
    private final Set<IMonitorComponent> selectedComponents = new HashSet<>();
    private final List<IMonitorComponent> selectedTarget = new ArrayList<>();

    private MultiblockState patternFindingState;
    private TickableSubscription subscription;

    public CentralMonitorMachine(IMachineBlockEntity holder) {
        super(holder);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        this.clearPatternFindingState();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        subscription = this.subscribeServerTick(subscription, this::tick);
    }

    private void tick() {
        for (MonitorGroup group : monitorGroups) {
            ItemStack stack = group.getItemStackHandler().getStackInSlot(0);
            if (!stack.isEmpty() && stack.getItem() instanceof IComponentItem componentItem) {
                for (IItemComponent component : componentItem.getComponents()) {
                    if (component instanceof IMonitorModuleItem module) {
                        module.tick(stack, this, group);
                    }
                }
            }
        }
    }

    @Override
    public void onUnload() {
        super.onUnload();
        this.clearPatternFindingState();
        if (subscription != null) subscription.unsubscribe();
    }

    protected void clearPatternFindingState() {
        if (this.patternFindingState != null)
            this.patternFindingState.clean();
        this.patternFindingState = null;
    }

    protected MultiblockState getPatternFindingState() {
        if (this.patternFindingState == null) {
            this.patternFindingState = new MultiblockState(getLevel(), getPos());
            this.patternFindingState.clean();
        }
        return this.patternFindingState;
    }

    public boolean isValidMonitorBlock(Level level, BlockPos pos) {
        if (level.isOutsideBuildHeight(pos)) return false;
        if (!getPatternFindingState().update(pos, BLOCK_PREDICATE)) {
            return false;
        }
        if (Predicates.abilities(PartAbility.INPUT_ENERGY, PartAbility.DATA_ACCESS).test(getPatternFindingState()))
            return true; // workaround because it doesn't work for blocks that have amount limits for some reason
        return BLOCK_PREDICATE.test(getPatternFindingState());
    }

    public void updateStructureDimensions() {
        Level level = getLevel();
        if (level == null) return;

        Direction front = getFrontFacing();
        Direction spin = getUpwardsFacing();

        Direction left = RelativeDirection.LEFT.getRelative(front, spin, false);
        Direction right = RelativeDirection.RIGHT.getRelative(front, spin, false);
        Direction up = RelativeDirection.UP.getRelative(front, spin, false);
        Direction down = RelativeDirection.DOWN.getRelative(front, spin, false);
        BlockPos.MutableBlockPos posLeft = getPos().mutable().move(left);
        BlockPos.MutableBlockPos posRight = getPos().mutable().move(right);
        BlockPos.MutableBlockPos posUp = getPos().mutable().move(up);
        BlockPos.MutableBlockPos posDown = getPos().mutable().move(down);
        this.leftDist = 0;
        this.rightDist = 0;
        this.upDist = 0;
        this.downDist = 0;

        while (isValidMonitorBlock(level, posLeft)) {
            posLeft.move(left);
            leftDist++;
        }
        while (isValidMonitorBlock(level, posRight)) {
            posRight.move(right);
            rightDist++;
        }
        while (isValidMonitorBlockRow(level, posUp, leftDist, rightDist, left, right)) {
            posUp.move(up);
            upDist++;
        }
        while (isValidMonitorBlockRow(level, posDown, leftDist, rightDist, left, right)) {
            posDown.move(down);
            downDist++;
        }
    }

    private boolean isValidMonitorBlockRow(Level level, BlockPos pos, int leftDist, int rightDist, Direction left,
                                           Direction right) {
        BlockPos.MutableBlockPos tmp = pos.mutable();
        tmp.move(left, leftDist);
        for (int i = 0; i < leftDist + rightDist; i++) {
            if (!isValidMonitorBlock(level, tmp)) return false;
            tmp.move(right);
        }
        return isValidMonitorBlock(level, tmp);
    }

    @Override
    public BlockPattern getPattern() {
        updateStructureDimensions();
        if (leftDist + rightDist + upDist + downDist == 0) {
            leftDist = 3;
            rightDist = 0;
            upDist = 1;
            downDist = 1;
        }
        StringBuilder[] pattern = new StringBuilder[upDist + downDist + 1];
        for (int i = 0; i < upDist + downDist + 1; i++) {
            pattern[i] = new StringBuilder(leftDist + rightDist + 1);
            for (int j = 0; j < leftDist + rightDist + 1; j++) {
                if (i == upDist && j == leftDist)
                    pattern[i].append('C'); // controller
                else
                    pattern[i].append('B'); // any valid block
            }
        }
        String[] tmp = new String[upDist + downDist + 1];
        for (int i = 0; i < upDist + downDist + 1; i++) tmp[i] = pattern[i].toString();
        return FactoryBlockPattern.start(RelativeDirection.LEFT, RelativeDirection.UP, RelativeDirection.FRONT)
                .aisle(tmp)
                .where('B', BLOCK_PREDICATE)
                .where('C', Predicates.controller(Predicates.blocks(this.getDefinition().get())))
                .build();
    }

    @Override
    public int getTier() {
        return GTValues.MV;
    }

    public BlockPos toRelative(BlockPos pos) {
        BlockPos tmp = getPos()
                .relative(RelativeDirection.RIGHT.getActualDirection(getFrontFacing()), rightDist)
                .relative(RelativeDirection.UP.getActualDirection(getFrontFacing()), upDist);
        Direction.Axis x = RelativeDirection.LEFT.getActualDirection(getFrontFacing()).getAxis();
        Direction.Axis y = RelativeDirection.UP.getActualDirection(getFrontFacing()).getAxis();
        return new BlockPos(Math.abs(tmp.get(x) - pos.get(x)), Math.abs(tmp.get(y) - pos.get(y)), 0);
    }

    private @Nullable IMonitorComponent getComponent(int row, int col) {
        col = leftDist + rightDist - col;
        BlockPos pos = getPos()
                .relative(RelativeDirection.LEFT.getActualDirection(getFrontFacing()), leftDist - col)
                .relative(RelativeDirection.UP.getActualDirection(getFrontFacing()), upDist - row);
        Level level = getLevel();
        if (level == null) return null;
        if (level.getBlockEntity(pos) instanceof IMonitorComponent component) {
            return component;
        }
        if (level.getBlockEntity(pos) instanceof IMachineBlockEntity machine) {
            if (machine.getMetaMachine() instanceof IMonitorComponent component)
                return component;
        }
        return null;
    }

    private IGuiTexture getComponentTexture(int row, int col) {
        if (row < 0 || col < 0 || row > downDist + upDist + 1 || col > leftDist + rightDist + 1)
            return GuiTextures.BLANK_TRANSPARENT;
        IMonitorComponent component = getComponent(row, col);
        if (component == null) return GuiTextures.BLANK_TRANSPARENT;
        return component.getComponentIcon();
    }

    private boolean isInAnyGroup(IMonitorComponent component) {
        return monitorGroups.stream().anyMatch(group -> group.contains(component.getPos()));
    }

    @Override
    public Widget createUIWidget() {
        selectedComponents.clear();
        WidgetGroup builder = (WidgetGroup) super.createUIWidget();
        WidgetGroup main = new WidgetGroup();
        WidgetGroup options = new WidgetGroup(-100, 20, 60, 20);
        WidgetGroup groupConfig = new WidgetGroup(10, 60, 100, 100);
        groupConfig.setVisible(false);
        ButtonWidget infoWidget = new ButtonWidget(160, 10, 20, 20, null);
        infoWidget.setButtonTexture(GuiTextures.INFO_ICON);
        infoWidget.setHoverTooltips(
                GTStringUtils.toImmutable(LangHandler.getSingleOrMultiLang("gtceu.central_monitor.info_tooltip")));
        builder.addWidget(infoWidget);
        @Nullable
        List<MonitorGroup> configGroup = new ArrayList<>();
        configGroup.add(null);
        Consumer<MonitorGroup> openGroupConfig = (group) -> {
            configGroup.set(0, group);
            if (group == null) {
                main.setVisible(true);
                groupConfig.setVisible(false);
                return;
            }
            groupConfig.clearAllWidgets();
            groupConfig.addWidget(new LabelWidget(0, 0, () -> {
                if (configGroup.get(0) == null) return "Currently editing:";
                return "Currently editing: %s".formatted(configGroup.get(0).getName());
            }));
            SlotWidget slot = new SlotWidget(
                    group.getItemStackHandler(), 0,
                    0, 20);
            WidgetGroup itemUI = new WidgetGroup(40, 20, 100, 100);
            Runnable changeListener = () -> {
                itemUI.clearAllWidgets();
                if (slot.getItem().getItem() instanceof IComponentItem item) {
                    for (IItemComponent component : item.getComponents()) {
                        if (component instanceof IMonitorModuleItem module) {
                            itemUI.addWidget(module.createUIWidget(slot.getItem(), this, group));
                        }
                    }
                }
            };
            slot.setChangeListener(changeListener);
            changeListener.run();
            groupConfig.addWidget(itemUI);
            groupConfig.addWidget(slot);
            main.setVisible(false);
            groupConfig.setVisible(true);
        };
        builder.addWidget(groupConfig);
        DraggableScrollableWidgetGroup groupList = new DraggableScrollableWidgetGroup(-100, 50, 80, 80);
        ArrayList<ArrayList<Consumer<Iterator<IMonitorComponent>>>> imageButtons = new ArrayList<>();
        Map<BlockPos, Runnable> rightClickCallbacks = new HashMap<>();
        List<Integer> dataSlot = new ArrayList<>(); // list to be able to modify it in lambdas
        dataSlot.add(1); // the slot (index starts from 1)
        dataSlot.add(9); // amount of slots
        IntInputWidget dataSlotInput = new IntInputWidget(120, 20, 60, 20, () -> dataSlot.get(0),
                n -> dataSlot.set(0, (int) GTMath.clamp(n, 1, dataSlot.get(1))));
        dataSlotInput.setVisible(false);
        builder.addWidget(dataSlotInput);
        Consumer<MonitorGroup> addGroupToList = group -> {
            ButtonWidget label = new ButtonWidget(20, groupList.widgets.size() * 15 + 5, 80, 10, null);
            TextTexture text = new TextTexture(group.getName());
            text.setType(TextTexture.TextType.LEFT);
            label.setButtonTexture(text);
            label.setOnPressCallback(click -> {
                group.getRelativePositions().forEach(pos -> {
                    BlockPos rel = toRelative(pos);
                    imageButtons.get(rel.getY()).get(rel.getX()).accept(null);
                });
                if (group.getTargetRaw() != null) {
                    rightClickCallbacks.getOrDefault(group.getTargetRaw(), () -> {}).run();
                }
            });
            groupList.addWidget(label);
            ButtonWidget configButton = new ButtonWidget(
                    0, label.getSelfPositionY() - 3,
                    16, 16,
                    GuiTextures.IO_CONFIG_COVER_SETTINGS,
                    click -> {
                        if (configGroup.get(0) == null) {
                            openGroupConfig.accept(group);
                        } else {
                            openGroupConfig.accept(null);
                        }
                    });
            groupList.addWidget(configButton);
        };
        monitorGroups.forEach(addGroupToList);
        builder.addWidget(groupList);
        main.addWidget(options);
        ButtonWidget removeFromGroupButton = new ButtonWidget(0, 0, 60, 20, null);
        removeFromGroupButton.setButtonTexture(new TextTexture("Remove from group"));
        removeFromGroupButton.setVisible(false);
        ButtonWidget setTargetButton = new ButtonWidget(0, 15, 60, 20, null);
        setTargetButton.setButtonTexture(new TextTexture("Set target"));
        setTargetButton.setVisible(false);
        ButtonWidget createGroupButton = new ButtonWidget(0, 0, 60, 20, null);
        createGroupButton.setOnPressCallback(click -> {
            MonitorGroup group = new MonitorGroup("Group #" + (monitorGroups.size() + 1));
            for (IMonitorComponent component : selectedComponents) {
                if (isInAnyGroup(component)) return;
                group.add(component.getPos());
            }
            monitorGroups.add(group);
            addGroupToList.accept(group);
            createGroupButton.setVisible(false);
            removeFromGroupButton.setVisible(true);
            Iterator<IMonitorComponent> it = selectedComponents.iterator();
            while (it.hasNext()) {
                IMonitorComponent c = it.next();
                BlockPos rel = toRelative(c.getPos());
                imageButtons.get(rel.getY()).get(rel.getX()).accept(it);
            }
            if (!selectedTarget.isEmpty()) {
                rightClickCallbacks.getOrDefault(selectedTarget.get(0).getPos(), () -> {}).run();
            }
        });
        setTargetButton.setOnPressCallback(click -> {
            MonitorGroup group = null;
            for (MonitorGroup group2 : monitorGroups) {
                for (IMonitorComponent component : selectedComponents) {
                    if (group2.contains(component.getPos())) {
                        group = group2;
                        break;
                    }
                }
                if (group != null) break;
            }
            if (group == null) return;
            if (selectedTarget.isEmpty()) group.setTarget(null);
            else {
                group.setTarget(selectedTarget.get(0).getPos());
                group.setDataSlot(dataSlot.get(0) - 1);
            }
        });
        removeFromGroupButton.setOnPressCallback(click -> {
            for (MonitorGroup group : monitorGroups) {
                for (IMonitorComponent component : selectedComponents) group.remove(component.getPos());
            }
            monitorGroups.removeIf(MonitorGroup::isEmpty);
            groupList.clearAllWidgets();
            monitorGroups.forEach(addGroupToList);
            removeFromGroupButton.setVisible(false);
            createGroupButton.setVisible(true);
            Iterator<IMonitorComponent> it = selectedComponents.iterator();
            while (it.hasNext()) {
                IMonitorComponent c = it.next();
                BlockPos rel = toRelative(c.getPos());
                imageButtons.get(rel.getY()).get(rel.getX()).accept(it);
            }
            if (!selectedTarget.isEmpty()) {
                rightClickCallbacks.getOrDefault(selectedTarget.get(0).getPos(), () -> {}).run();
            }
        });
        createGroupButton.setButtonTexture(new TextTexture("Create group"));
        createGroupButton.setVisible(false);
        options.addWidget(removeFromGroupButton);
        options.addWidget(createGroupButton);
        options.addWidget(setTargetButton);
        int startX = 20;
        int startY = 59;
        for (int row = 0; row <= downDist + upDist; row++) {
            imageButtons.add(new ArrayList<>());
            for (int col = 0; col <= leftDist + rightDist; col++) {
                IGuiTexture texture = getComponentTexture(row, col);
                GuiTextureGroup textures = new GuiTextureGroup(texture, new ColorBorderTexture(2, 0xFFFFFF));
                IMonitorComponent component = getComponent(row, col);
                if (component == null) continue;
                ButtonWidget img = new ButtonWidget(startX + (16 * col), startY + (16 * row), 16, 16, textures, null);
                Consumer<Iterator<IMonitorComponent>> callback = (it) -> {
                    if (selectedComponents.contains(component)) {
                        if (it == null)
                            selectedComponents.remove(component);
                        else
                            it.remove();
                        if (!selectedTarget.isEmpty() && selectedTarget.get(0) == component) {
                            ColorRectTexture rect = new ColorRectTexture(Color.BLUE);
                            textures.setTextures(rect, texture);
                        } else textures.setTextures(texture);
                        createGroupButton.setVisible(selectedComponents.stream().noneMatch(this::isInAnyGroup));
                        removeFromGroupButton
                                .setVisible(selectedComponents.stream().allMatch(this::isInAnyGroup));
                        setTargetButton.setVisible(removeFromGroupButton.isVisible());
                        if (selectedComponents.isEmpty()) {
                            createGroupButton.setVisible(false);
                            removeFromGroupButton.setVisible(false);
                            setTargetButton.setVisible(false);
                        }
                    } else {
                        boolean inAnyGroup = isInAnyGroup(component);
                        if (selectedComponents.isEmpty() && !inAnyGroup) createGroupButton.setVisible(true);
                        if (inAnyGroup) createGroupButton.setVisible(false);
                        if (selectedComponents.isEmpty() && inAnyGroup) {
                            removeFromGroupButton.setVisible(true);
                            setTargetButton.setVisible(true);
                        }
                        if (!inAnyGroup) {
                            removeFromGroupButton.setVisible(false);
                            setTargetButton.setVisible(false);
                        }
                        selectedComponents.add(component);
                        ColorRectTexture rect = new ColorRectTexture(
                                (selectedTarget.isEmpty() || selectedTarget.get(0) != component) ? Color.RED :
                                        Color.PINK);
                        textures.setTextures(rect, texture);
                    }
                    if (isInAnyGroup(component)) {
                        monitorGroups.forEach(group -> {
                            if (group.contains(component.getPos())) img.setHoverTooltips("Group: " + group.getName());
                        });
                    } else img.setHoverTooltips("Group: none");
                };
                Runnable rightClickCallback = () -> {
                    if (!selectedTarget.isEmpty()) {
                        if (selectedTarget.get(0) == component) {
                            selectedTarget.clear();
                            if (selectedComponents.contains(component)) {
                                ColorRectTexture rect = new ColorRectTexture(Color.RED);
                                textures.setTextures(rect, texture);
                            } else textures.setTextures(texture);
                            dataSlotInput.setVisible(false);
                            return;
                        } else rightClickCallbacks.get(selectedTarget.get(0).getPos()).run();
                    }
                    selectedTarget.add(component);
                    ColorRectTexture rect;
                    if (selectedComponents.contains(component)) {
                        rect = new ColorRectTexture(Color.PINK);
                    } else {
                        rect = new ColorRectTexture(Color.BLUE);
                    }
                    textures.setTextures(rect, texture);
                    if (component.getDataItems() != null) {
                        IItemHandler dataItems = component.getDataItems();
                        MonitorGroup selectedGroup = null;
                        for (MonitorGroup group : monitorGroups) {
                            for (IMonitorComponent c : selectedComponents) {
                                if (group.contains(c.getPos())) {
                                    if (selectedGroup == null || selectedGroup == group) selectedGroup = group;
                                    else {
                                        selectedGroup = null;
                                        break;
                                    }
                                }
                            }
                        }
                        if (selectedGroup != null) dataSlot.set(0, selectedGroup.getDataSlot() + 1);
                        dataSlot.set(1, dataItems.getSlots());
                        dataSlotInput.setVisible(true);
                    }
                };
                if (isInAnyGroup(component)) {
                    monitorGroups.forEach(group -> {
                        if (group.contains(component.getPos())) img.setHoverTooltips("Group: " + group.getName());
                    });
                } else img.setHoverTooltips("Group: none");
                img.setOnPressCallback(click -> {
                    if (click.button == 0) callback.accept(null);
                    else if (click.button == 1) rightClickCallback.run();
                });
                main.addWidget(img);
                GTUtil.getLast(imageButtons).add(callback);
                rightClickCallbacks.put(component.getPos(), rightClickCallback);
            }
        }
        builder.addWidget(main);
        return builder;
    }

    @Override
    public boolean isMonitor() {
        return false;
    }

    @Override
    public IGuiTexture getComponentIcon() {
        return ResourceTexture.fromSpirit(GTCEu.id("block/multiblock/network_switch/overlay_front_active"));
    }

    @Override
    public @NotNull List<Component> getDebugInfo(Player player, int logLevel,
                                                 PortableScannerBehavior.DisplayMode mode) {
        return List.of(Component.literal("Size: (%d+1+%d)x(%d+1+%d)".formatted(leftDist, rightDist, upDist, downDist)));
    }

    @Override
    public @NotNull List<Component> getDataInfo(PortableScannerBehavior.DisplayMode mode) {
        return List.of(Component.literal("Size: %dx%d".formatted(leftDist + rightDist + 1, upDist + downDist + 1)));
    }
}

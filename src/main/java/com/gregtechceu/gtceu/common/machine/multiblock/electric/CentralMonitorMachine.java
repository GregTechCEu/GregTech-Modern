package com.gregtechceu.gtceu.common.machine.multiblock.electric;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.IMonitorComponent;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

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
            .or(Predicates.machines(GTMachines.MONITOR))
            .or(Predicates.blocks(VALID_BLOCKS));

    @Persisted
    @DescSynced
    private int leftDist = 0, rightDist = 0, upDist = 0, downDist = 0;
    @Persisted
    @DescSynced
    private final List<MonitorGroup> monitorGroups = new ArrayList<>();
    private final Set<IMonitorComponent> selectedComponents = new HashSet<>();

    private MultiblockState patternFindingState;

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
    public void onUnload() {
        super.onUnload();
        this.clearPatternFindingState();
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

    private ResourceTexture getComponentTexture(int row, int col) {
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
        updateStructureDimensions();
        WidgetGroup builder = (WidgetGroup) super.createUIWidget();
        WidgetGroup options = new WidgetGroup(-120, 20, 80, 20);
        DraggableScrollableWidgetGroup groupList = new DraggableScrollableWidgetGroup(-120, 50, 80, 80);
        ArrayList<ArrayList<Runnable>> imageButtons = new ArrayList<>();
        Consumer<MonitorGroup> addGroupToList = group -> {
            ButtonWidget label = new ButtonWidget(0, groupList.widgets.size() * 15 + 5, 80, 10, null);
            TextTexture text = new TextTexture(group.getName());
            text.setType(TextTexture.TextType.LEFT);
            label.setButtonTexture(text);
            label.setOnPressCallback(click -> group.getRelativePositions().forEach(pos -> {
                BlockPos rel = toRelative(pos);
                // GTCEu.LOGGER.info("pos = {}, rel = {}, leftDist = {}, upDist = {}", pos, rel, leftDist, upDist);
                imageButtons.get(rel.getY()).get(rel.getX()).run();
            }));
            groupList.addWidget(label);
        };
        monitorGroups.forEach(addGroupToList);
        builder.addWidget(groupList);
        builder.addWidget(options);
        ButtonWidget removeFromGroupButton = new ButtonWidget(0, 0, 80, 20, null);
        removeFromGroupButton.setButtonTexture(new TextTexture("Remove from group"));
        removeFromGroupButton.setVisible(false);
        ButtonWidget createGroupButton = new ButtonWidget(0, 0, 80, 20, null);
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
        });
        createGroupButton.setButtonTexture(new TextTexture("Create group"));
        createGroupButton.setVisible(false);
        options.addWidget(removeFromGroupButton);
        options.addWidget(createGroupButton);
        int startX = 20;
        int startY = 59;
        for (int row = 0; row <= downDist + upDist; row++) {
            imageButtons.add(new ArrayList<>());
            for (int col = 0; col <= leftDist + rightDist; col++) {
                ResourceTexture texture = getComponentTexture(row, col);
                GuiTextureGroup textures = new GuiTextureGroup(texture, new ColorBorderTexture(2, 0xFFFFFF));
                IMonitorComponent component = getComponent(row, col);
                if (component == null) continue;
                ButtonWidget img = new ButtonWidget(startX + (16 * col), startY + (16 * row), 16, 16, textures, null);
                Runnable callback = () -> {
                    if (selectedComponents.contains(component)) {
                        selectedComponents.remove(component);
                        textures.setTextures(texture);
                        createGroupButton.setVisible(selectedComponents.stream().noneMatch(this::isInAnyGroup));
                        removeFromGroupButton
                                .setVisible(selectedComponents.stream().allMatch(this::isInAnyGroup));
                        if (selectedComponents.isEmpty()) {
                            createGroupButton.setVisible(false);
                            removeFromGroupButton.setVisible(false);
                        }
                    } else {
                        boolean inAnyGroup = isInAnyGroup(component);
                        if (selectedComponents.isEmpty() && !inAnyGroup) createGroupButton.setVisible(true);
                        if (inAnyGroup) createGroupButton.setVisible(false);
                        if (selectedComponents.isEmpty() && inAnyGroup) removeFromGroupButton.setVisible(true);
                        if (!inAnyGroup) removeFromGroupButton.setVisible(false);
                        selectedComponents.add(component);
                        ColorRectTexture rect = new ColorRectTexture(Color.RED);
                        textures.setTextures(rect, texture);
                    }
                    if (isInAnyGroup(component)) {
                        monitorGroups.forEach(group -> {
                            if (group.contains(component.getPos())) img.setHoverTooltips("Group: " + group.getName());
                        });
                    } else img.setHoverTooltips("Group: none");
                };
                if (isInAnyGroup(component)) {
                    monitorGroups.forEach(group -> {
                        if (group.contains(component.getPos())) img.setHoverTooltips("Group: " + group.getName());
                    });
                } else img.setHoverTooltips("Group: none");
                img.setOnPressCallback(click -> callback.run());
                builder.addWidget(img);
                GTUtil.getLast(imageButtons).add(callback);
            }
        }
        return builder;
    }

    @Override
    public boolean isMonitor() {
        return false;
    }

    @Override
    public ResourceTexture getComponentIcon() {
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

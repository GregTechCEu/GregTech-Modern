package com.gregtechceu.gtceu.common.item.behavior;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.api.cover.filter.ItemFilter;
import com.gregtechceu.gtceu.api.cover.filter.SimpleItemFilter;
import com.gregtechceu.gtceu.api.cover.filter.TagItemFilter;
import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.gregtechceu.gtceu.api.item.IComponentItem;
import com.gregtechceu.gtceu.api.item.component.IAddInformation;
import com.gregtechceu.gtceu.api.item.component.IInteractionItem;
import com.gregtechceu.gtceu.api.item.component.IItemLifeCycle;
import com.gregtechceu.gtceu.api.mui.IItemUIHolder;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.item.GTDataComponents;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.entity.player.PlayerXpEvent;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.drawable.ItemDrawable;
import brachy.modularui.factory.PlayerInventoryGuiData;
import brachy.modularui.factory.UIFactories;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.RichTooltip;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.*;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widgets.CycleButtonWidget;
import brachy.modularui.widgets.PagedWidget;
import brachy.modularui.widgets.SlotGroupWidget;
import brachy.modularui.widgets.ToggleButton;
import brachy.modularui.widgets.layout.Flow;
import brachy.modularui.widgets.layout.Grid;
import brachy.modularui.widgets.slot.ModularSlot;
import brachy.modularui.widgets.slot.PhantomItemSlot;
import brachy.modularui.widgets.textfield.TextFieldWidget;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.tterrag.registrate.util.entry.ItemEntry;
import io.netty.buffer.ByteBuf;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class ItemMagnetBehavior implements IInteractionItem, IItemLifeCycle, IAddInformation, IItemUIHolder {

    private final int range;
    private final long energyDraw;

    public ItemMagnetBehavior(int range) {
        this.range = range;
        this.energyDraw = GTValues.V[range > 8 ? GTValues.HV : GTValues.LV];
    }

    static {
        NeoForge.EVENT_BUS.register(ItemMagnetBehavior.class);
    }

    @Override
    public ModularPanel<?> buildUI(PlayerInventoryGuiData<?> data, PanelSyncManager syncManager, UISettings settings) {
        ItemStack held = data.getUsedItemStack();
        MagnetComponent magnetData = held.getOrDefault(GTDataComponents.MAGNET, MagnetComponent.EMPTY);
        Filter selectedFilter = magnetData.filterType();

        Map<Filter, ItemStack> stacks = new EnumMap<>(Filter.class);
        for (Filter filter : Filter.values()) {
            ItemStack stack = filter.getFilter(held);
            stacks.put(filter, stack);
        }

        EnumSyncValue<Filter> filterSync = new EnumSyncValue<>(Filter.class,
                magnetData::filterType,
                filter -> held.set(GTDataComponents.MAGNET, new MagnetComponent(magnetData.active, filter)))
                .allowC2S();

        PagedWidget<?> pages = new PagedWidget<>()
                .left((176 - 80) / 2)
                .top((60 - 55) / 2 + 15)
                .size(80, 55)
                .initialPage(selectedFilter.ordinal())
                .addPage(createSimpleFilterPage((SimpleItemFilter) ItemFilter.loadFilter(stacks.get(Filter.SIMPLE))))
                .addPage(createTagFilterPage((TagItemFilter) ItemFilter.loadFilter(stacks.get(Filter.TAG))));
        pages.onUpdateListener(widget -> {
            int selected = filterSync.getIntValue();
            if (selected != widget.getCurrentPageIndex()) {
                widget.setPage(selected);
            }
        });

        syncManager.addCloseListener(player -> {
            held.update(GTDataComponents.MAGNET, MagnetComponent.EMPTY,
                    c -> new MagnetComponent(c.active(), selectedFilter));
        });

        return new ModularPanel<>("item_magnet")
                .size(176, 157)
                .background(GTGuiTextures.BACKGROUND)
                .child(Text.dynamic(() -> Component.translatable(filterSync.getValue().getTooltip()))
                        .asWidget()
                        .left(5)
                        .top(5))
                .child(new CycleButtonWidget()
                        .left(146)
                        .top(5)
                        .size(20)
                        .value(filterSync)
                        .stateCount(Filter.values().length)
                        .stateOverlay(Filter.SIMPLE, new ItemDrawable(GTItems.ITEM_FILTER.asItem()).asIcon().size(16))
                        .stateOverlay(Filter.TAG, new ItemDrawable(GTItems.TAG_FILTER.asItem()).asIcon().size(16))
                        .tooltipBuilder(r -> r.addLine(Text.dynamic(
                                () -> Component.translatable(filterSync.getValue().getTooltip())))))
                .child(pages)
                .child(SlotGroupWidget.playerInventory(false).left(7).top(75).disableSortButtons());
    }

    private ParentWidget<?> createSimpleFilterPage(SimpleItemFilter filter) {
        SimpleItemFilter.FilterItemStackHandler handler = new SimpleItemFilter.FilterItemStackHandler(filter);

        Grid filterGrid = new Grid()
                .coverChildren()
                .gridOfSizeWidth(9, 3, (x, y, i) -> new PhantomItemSlot()
                        .size(16)
                        .syncHandler(new PhantomItemSlotSyncHandler(new ModularSlot(handler, i)
                                .changeListener((stack, amount, client, init) -> handler.setStackInSlot(i, stack))
                                .ignoreMaxStackSize(true).accessibility(true, false))));

        BooleanSyncValue blacklist = new BooleanSyncValue(filter::isBlackList, filter::setBlackList).allowC2S();

        BooleanSyncValue ignoreNBT = new BooleanSyncValue(filter::isIgnoreNbt, filter::setIgnoreNbt).allowC2S();

        Flow filterButtons = Flow.col()
                .coverChildren()
                .child(new ToggleButton().stateBackground(GTGuiTextures.BUTTON_BLACKLIST).value(blacklist))
                .child(new ToggleButton().stateBackground(GTGuiTextures.BUTTON_IGNORE_NBT).value(ignoreNBT));

        return new ParentWidget<>()
                .size(80, 55)
                .child(filterGrid.left(0).top(0))
                .child(filterButtons.left(58).top(0));
    }

    private ParentWidget<?> createTagFilterPage(TagItemFilter filter) {
        StringSyncValue filterString = new StringSyncValue(filter::getFilterString, filter::setFilterString).allowC2S();
        RichTooltip infoTooltip = new RichTooltip().add("cover.tag_filter.info");

        return new ParentWidget<>()
                .size(80, 55)
                .child(Flow.row()
                        .coverChildren()
                        .left(0)
                        .top(18)
                        .child(new TextFieldWidget()
                                .width(62)
                                .value(filterString))
                        .child(GTGuiTextures.INFO.asWidget()
                                .size(16)
                                .tooltip(infoTooltip)));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(ItemStack item, Level world, @NotNull Player player,
                                                  InteractionHand hand) {
        if (!player.level().isClientSide) {
            if (player.isShiftKeyDown()) {
                player.displayClientMessage(Component.translatable(toggleActive(player.getItemInHand(hand)) ?
                        "behavior.item_magnet.enabled" : "behavior.item_magnet.disabled"), true);
            } else {
                UIFactories.playerInventory().openFromHand(player, hand);
            }
        }
        return InteractionResultHolder.pass(player.getItemInHand(hand));
    }

    private static boolean isActive(ItemStack stack) {
        if (stack == ItemStack.EMPTY) {
            return false;
        }
        return stack.getOrDefault(GTDataComponents.MAGNET, MagnetComponent.EMPTY).active();
    }

    private static boolean toggleActive(ItemStack stack) {
        MutableBoolean active = new MutableBoolean();
        stack.update(GTDataComponents.MAGNET, MagnetComponent.EMPTY,
                c -> {
                    active.setValue(!c.active);
                    return new MagnetComponent(!c.active, c.filterType);
                });
        return active.booleanValue();
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        // Adapted logic from Draconic Evolution
        // https://github.com/Draconic-Inc/Draconic-Evolution/blob/1.12.2/src/main/java/com/brandon3055/draconicevolution/items/tools/Magnet.java
        if (!entity.isShiftKeyDown() && entity.tickCount % 10 == 0 && isActive(stack) &&
                entity instanceof Player player) {
            Level world = entity.level();
            if (!drainEnergy(true, stack, energyDraw)) {
                return;
            }

            List<ItemEntity> items = world.getEntitiesOfClass(ItemEntity.class,
                    new AABB(entity.getX(), entity.getY(), entity.getZ(), entity.getX(), entity.getY(), entity.getZ())
                            .inflate(range, range, range));

            ItemFilter filter = null;
            boolean didMoveEntity = false;
            for (ItemEntity itemEntity : items) {
                if (itemEntity.isRemoved()) {
                    continue;
                }

                CompoundTag itemTag = itemEntity.getPersistentData();
                if (itemTag.contains("PreventRemoteMovement")) {
                    continue;
                }

                if (itemEntity.getOwner() != null && itemEntity.getOwner().equals(entity) &&
                        itemEntity.hasPickUpDelay()) {
                    continue;
                }

                Player closest = world.getNearestPlayer(itemEntity, 4);
                if (closest != null && closest != entity) {
                    continue;
                }

                if (!world.isClientSide) {
                    if (filter == null) {
                        filter = stack.getOrDefault(GTDataComponents.MAGNET, MagnetComponent.EMPTY).filterType()
                                .loadFilter(stack);
                    }

                    if (!filter.test(itemEntity.getItem())) {
                        continue;
                    }

                    if (itemEntity.hasPickUpDelay()) {
                        itemEntity.setNoPickUpDelay();
                    }
                    itemEntity.setDeltaMovement(0, 0, 0);
                    itemEntity.setPos(entity.getX() - 0.2 + (world.random.nextDouble() * 0.4), entity.getY() - 0.6,
                            entity.getZ() - 0.2 + (world.random.nextDouble() * 0.4));
                    didMoveEntity = true;
                }
            }

            if (didMoveEntity) {
                world.playSound(null, entity, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS,
                        0.1F, 0.5F * ((world.random.nextFloat() - world.random.nextFloat()) * 0.7F + 2F));
            }

            List<ExperienceOrb> xp = world.getEntitiesOfClass(ExperienceOrb.class,
                    new AABB(entity.getX(), entity.getY(), entity.getZ(), entity.getX(), entity.getY(), entity.getZ())
                            .inflate(4, 4, 4));

            for (ExperienceOrb orb : xp) {
                if (!world.isClientSide && !orb.isRemoved()) {
                    if (player.takeXpDelay == 0) {
                        if (NeoForge.EVENT_BUS.post(new PlayerXpEvent.PickupXp(player, orb)).isCanceled()) {
                            continue;
                        }
                        world.playSound(null, entity, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS,
                                0.1F, 0.5F * ((world.random.nextFloat() - world.random.nextFloat()) * 0.7F + 1.8F));
                        player.take(orb, 1);
                        player.giveExperiencePoints(orb.value);
                        orb.discard();
                        didMoveEntity = true;
                    }
                }
            }

            if (didMoveEntity) {
                drainEnergy(false, stack, energyDraw);
            }
        }
    }

    @SubscribeEvent
    public static void onItemToss(@NotNull ItemTossEvent event) {
        if (hasMagnet(event.getPlayer())) {
            event.getEntity().setPickUpDelay(60);
        }
    }

    private static boolean hasMagnet(@NotNull Player player) {
        Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stackInSlot = inventory.getItem(i);
            if (isMagnet(stackInSlot) && isActive(stackInSlot)) {
                return true;
            }
        }

        if (!GTCEu.Mods.isCuriosLoaded()) {
            return false;
        }
        return CuriosUtils.hasMagnetCurios(player);
    }

    private static boolean isMagnet(@NotNull ItemStack stack) {
        if (stack.getItem() instanceof IComponentItem metaItem) {
            for (var behavior : metaItem.getComponents()) {
                if (behavior instanceof ItemMagnetBehavior) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean drainEnergy(boolean simulate, @NotNull ItemStack stack, long amount) {
        IElectricItem electricItem = GTCapabilityHelper.getElectricItem(stack);
        if (electricItem == null)
            return false;

        return electricItem.discharge(amount, Integer.MAX_VALUE, true, false, simulate) >= amount;
    }

    @Override
    public void appendHoverText(ItemStack itemStack, Item.TooltipContext context, List<Component> lines,
                                TooltipFlag isAdvanced) {
        lines.add(Component
                .translatable(isActive(itemStack) ? "behavior.item_magnet.enabled" : "behavior.item_magnet.disabled"));
    }

    private static class CuriosUtils {

        public static boolean hasMagnetCurios(Player player) {
            return CuriosApi.getCuriosInventory(player)
                    .map(curios -> curios.findFirstCurio(i -> isMagnet(i) && isActive(i)).isPresent())
                    .orElse(false);
        }
    }

    public enum Filter implements StringRepresentable {

        SIMPLE(GTItems.ITEM_FILTER, "item_filter"),
        TAG(GTItems.TAG_FILTER, "item_tag_filter");

        public static final Codec<Filter> CODEC = StringRepresentable.fromEnum(Filter::values);

        public final ItemEntry<ComponentItem> item;
        public final String texture;

        Filter(ItemEntry<ComponentItem> item, String texture) {
            this.item = item;
            this.texture = texture;
        }

        public ItemStack getFilter(ItemStack magnet) {
            var mockStack = new ItemStack(item.asItem());
            switch (this) {
                case SIMPLE -> mockStack.set(GTDataComponents.SIMPLE_ITEM_FILTER,
                        magnet.get(GTDataComponents.SIMPLE_ITEM_FILTER));
                case TAG -> mockStack.set(GTDataComponents.TAG_FILTER_EXPRESSION,
                        magnet.get(GTDataComponents.TAG_FILTER_EXPRESSION));
            }
            return mockStack;
        }

        @Override
        public @NonNull String getSerializedName() {
            return texture;
        }

        public ItemFilter loadFilter(ItemStack magnet) {
            var stack = getFilter(magnet);
            return ItemFilter.loadFilter(stack);
        }

        public void saveFilter(ItemStack stack, ItemFilter filter) {
            switch (this) {
                case SIMPLE -> {
                    if (filter instanceof SimpleItemFilter simple) {
                        stack.set(GTDataComponents.SIMPLE_ITEM_FILTER, simple);
                    }
                }
                case TAG -> {
                    if (filter instanceof TagItemFilter tag) {
                        stack.set(GTDataComponents.TAG_FILTER_EXPRESSION, tag.getFilterString());
                    }
                }
            }
        }

        public static Filter get(int ordinal) {
            return Filter.values()[ordinal];
        }

        public @NotNull String getTooltip() {
            return item.asItem().getDescriptionId();
        }
    }

    public record MagnetComponent(boolean active, Filter filterType) {

        public static final Codec<MagnetComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.BOOL.orElse(false).fieldOf("active").forGetter(MagnetComponent::active),
                Filter.CODEC.fieldOf("filter_type").forGetter(MagnetComponent::filterType))
                .apply(instance, MagnetComponent::new));
        public static final StreamCodec<ByteBuf, MagnetComponent> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.BOOL, MagnetComponent::active,
                ByteBufCodecs.VAR_INT, c -> c.filterType().ordinal(),
                (active, ordinal) -> new MagnetComponent(active, Filter.get(ordinal)));

        public static final MagnetComponent EMPTY = new MagnetComponent(false, Filter.SIMPLE);
    }
}

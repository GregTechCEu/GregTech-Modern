package com.gregtechceu.gtceu.common.item;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.api.cover.filter.ItemFilter;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.UITemplate;
import com.gregtechceu.gtceu.api.gui.widget.EnumSelectorWidget;
import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.gregtechceu.gtceu.api.item.IComponentItem;
import com.gregtechceu.gtceu.api.item.component.IAddInformation;
import com.gregtechceu.gtceu.api.item.component.IInteractionItem;
import com.gregtechceu.gtceu.api.item.component.IItemLifeCycle;
import com.gregtechceu.gtceu.api.item.component.IItemUIFactory;
import com.gregtechceu.gtceu.common.data.GTItems;

import com.lowdragmc.lowdraglib.gui.factory.HeldItemUIFactory;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.player.PlayerXpEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import com.tterrag.registrate.util.entry.ItemEntry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import oshi.util.tuples.Triplet;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.*;

public class ItemMagnetBehavior implements IInteractionItem, IItemLifeCycle, IAddInformation, IItemUIFactory {

    public static final String FILTER_TAG = "MagnetFilter";
    public static final String FILTER_ORDINAL_TAG = "FilterOrdinal";

    private final int range;
    private final long energyDraw;

    public ItemMagnetBehavior(int range) {
        this.range = range;
        this.energyDraw = GTValues.V[range > 8 ? GTValues.HV : GTValues.LV];
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public ModularUI createUI(HeldItemUIFactory.HeldItemHolder holder, Player entityPlayer) {
        var held = holder.getHeld();
        var tag = held.getOrCreateTag();
        var selected = Filter.get(tag.getInt(FILTER_ORDINAL_TAG));
        var widgets = new HashSet<Triplet<Filter, Widget, Widget>>();
        var stacks = new HashMap<Filter, ItemStack>();
        var ui = new ModularUI(176, 157, holder, entityPlayer)
                .background(GuiTextures.BACKGROUND)
                .widget(new EnumSelectorWidget<>(146, 5, 20, 20,
                        Filter.values(), selected, (val) -> updateSelection(tag, val, widgets)))
                .widget(UITemplate.bindPlayerInventory(entityPlayer.getInventory(), GuiTextures.SLOT, 7, 75, true));
        for (var f : Filter.values()) {
            var stack = f.getFilter(held);
            stack.setTag(tag.getCompound(FILTER_TAG).copy());
            stacks.put(f, stack);
            var description = new LabelWidget(5, 5, stack.getDescriptionId());
            var config = ItemFilter
                    .loadFilter(stack)
                    .openConfigurator((176 - 80) / 2, (60 - 55) / 2 + 15);
            var visible = f == selected;
            description.setVisible(visible);
            config.setVisible(visible);
            widgets.add(new Triplet<>(f, description, config));
            ui.widget(description);
            ui.widget(config);
        }
        ui.registerCloseListener(() -> {
            var selection = Filter.get(tag.getInt(FILTER_ORDINAL_TAG));
            tag.put(FILTER_TAG, stacks.get(selection).getOrCreateTag());
        });
        return ui;
    }

    private void updateSelection(CompoundTag tag, Filter filter, Collection<Triplet<Filter, Widget, Widget>> widgets) {
        tag.putInt(FILTER_ORDINAL_TAG, filter.ordinal());
        widgets.forEach(tri -> {
            var visible = tri.getA() == filter;
            tri.getB().setVisible(visible);
            tri.getC().setVisible(visible);
        });
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Item item, Level world, @NotNull Player player,
                                                  InteractionHand hand) {
        if (!player.level().isClientSide && player.isShiftKeyDown()) {
            player.displayClientMessage(Component.translatable(toggleActive(player.getItemInHand(hand)) ?
                    "behavior.item_magnet.enabled" : "behavior.item_magnet.disabled"), true);
        } else {
            IItemUIFactory.super.use(item, world, player, hand);
        }
        return InteractionResultHolder.pass(player.getItemInHand(hand));
    }

    private static boolean isActive(ItemStack stack) {
        if (stack == ItemStack.EMPTY) {
            return false;
        }
        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return false;
        }
        if (tag.contains("IsActive")) {
            return tag.getBoolean("IsActive");
        }
        return false;
    }

    private static boolean toggleActive(ItemStack stack) {
        boolean isActive = isActive(stack);
        // noinspection ConstantConditions
        stack.getOrCreateTag().putBoolean("IsActive", !isActive);
        return !isActive;
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
                        filter = Filter.get(stack.getOrCreateTag().getInt(FILTER_ORDINAL_TAG)).loadFilter(stack);
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
                        if (MinecraftForge.EVENT_BUS.post(new PlayerXpEvent.PickupXp(player, orb))) {
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
    public void onItemToss(@NotNull ItemTossEvent event) {
        if (event.getPlayer() == null) return;
        if (hasMagnet(event.getPlayer())) {
            event.getEntity().setPickUpDelay(60);
        }
    }

    private boolean hasMagnet(@NotNull Player player) {
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
    public void appendHoverText(ItemStack itemStack, @Nullable Level level, List<Component> lines,
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

    public enum Filter implements EnumSelectorWidget.SelectableEnum {

        SIMPLE(GTItems.ITEM_FILTER, "item_filter"),
        TAG(GTItems.TAG_FILTER, "item_tag_filter");

        public final ItemEntry<ComponentItem> item;
        public final String texture;

        Filter(ItemEntry<ComponentItem> item, String texture) {
            this.item = item;
            this.texture = texture;
        }

        public ItemStack getFilter(ItemStack magnet) {
            var tag = magnet.getOrCreateTag();
            var mockStack = new ItemStack(item);
            mockStack.setTag(tag.getCompound(FILTER_TAG));
            return mockStack;
        }

        public ItemFilter loadFilter(ItemStack magnet) {
            var stack = getFilter(magnet);
            return ItemFilter.loadFilter(stack);
        }

        public static Filter get(int ordinal) {
            return Filter.values()[ordinal];
        }

        @Override
        public @NotNull String getTooltip() {
            return item.asItem().getDescriptionId();
        }

        @Override
        public @NotNull IGuiTexture getIcon() {
            return new ResourceTexture("gtceu:textures/item/" + texture + ".png");
        }
    }
}

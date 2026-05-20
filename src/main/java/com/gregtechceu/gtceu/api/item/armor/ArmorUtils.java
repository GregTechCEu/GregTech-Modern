package com.gregtechceu.gtceu.api.item.armor;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.common.data.GTSoundEntries;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.core.mixins.ServerGamePacketListenerImplAccessor;
import com.gregtechceu.gtceu.utils.ItemStackHashStrategy;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.ForgeEventFactory;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

public class ArmorUtils {

    public static final int MIN_NIGHTVISION_CHARGE = 4;
    public static final int NIGHTVISION_DURATION = 20 * 20; // 20 seconds
    // Flashing starts at 10 seconds + two second buffer to prevent flicker
    public static final int NIGHT_VISION_RESET = 12 * 20;

    /**
     * Check is possible to charge item
     */
    public static boolean isPossibleToCharge(ItemStack chargeable) {
        IElectricItem container = GTCapabilityHelper.getElectricItem(chargeable);
        if (container != null) {
            return container.getCharge() < container.getMaxCharge() &&
                    (container.getCharge() + container.getTransferLimit()) <= container.getMaxCharge();
        }
        return false;
    }

    /**
     * Searches all three player inventories for items that can be charged
     *
     * @param tier of charger
     * @return Map of the inventory and a list of the index of a chargable item
     */
    public static List<Pair<NonNullList<ItemStack>, IntList>> getChargeableItem(Player player, int tier) {
        List<Pair<NonNullList<ItemStack>, IntList>> inventorySlotMap = new ArrayList<>();

        IntList openMainSlots = new IntArrayList();
        for (int i = 0; i < player.getInventory().items.size(); i++) {
            ItemStack current = player.getInventory().items.get(i);
            IElectricItem item = GTCapabilityHelper.getElectricItem(current);
            if (item == null) continue;

            if (isPossibleToCharge(current) && item.getTier() <= tier) {
                openMainSlots.add(i);
            }
        }

        if (!openMainSlots.isEmpty()) {
            inventorySlotMap.add(Pair.of(player.getInventory().items, openMainSlots));
        }

        IntList openArmorSlots = new IntArrayList();
        for (int i = 0; i < player.getInventory().armor.size(); i++) {
            ItemStack current = player.getInventory().armor.get(i);
            IElectricItem item = GTCapabilityHelper.getElectricItem(current);
            if (item == null) {
                continue;
            }

            if (isPossibleToCharge(current) && item.getTier() <= tier) {
                openArmorSlots.add(i);
            }
        }

        if (!openArmorSlots.isEmpty()) {
            inventorySlotMap.add(Pair.of(player.getInventory().armor, openArmorSlots));
        }

        ItemStack offHand = player.getInventory().offhand.get(0);
        IElectricItem offHandItem = GTCapabilityHelper.getElectricItem(offHand);
        if (offHandItem == null) {
            return inventorySlotMap;
        }

        if (isPossibleToCharge(offHand) && offHandItem.getTier() <= tier) {
            inventorySlotMap.add(Pair.of(player.getInventory().offhand, new IntArrayList(new int[] { 0 })));
        }

        return inventorySlotMap;
    }

    /**
     * Spawn particle behind player with speedY speed
     */
    public static void spawnParticle(Level world, Player player, ParticleOptions type, double speedY) {
        if (type != null) {
            Vec3 forward = player.getForward();
            world.addParticle(type, player.getX() - forward.x, player.getY() + 0.5D, player.getZ() - forward.z, 0.0D,
                    speedY, 0.0D);
        }
    }

    public static void playJetpackSound(@Nonnull Player player) {
        if (player.level().isClientSide()) {
            float cons = (float) player.getDeltaMovement().y + player.moveDist;
            cons = Mth.clamp(cons, 0.6F, 1.0F);

            if (player.getDeltaMovement().y > 0.05F) {
                cons += 0.1F;
            }

            if (player.getDeltaMovement().y < -0.05F) {
                cons -= 0.4F;
            }

            player.playSound(GTSoundEntries.JET_ENGINE.getMainEvent(), 0.3F, cons);
        }
    }

    /**
     * Resets private field, amount of ticks player in the sky
     */
    public static void resetPlayerFloatingTime(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            ((ServerGamePacketListenerImplAccessor) serverPlayer.connection).setAboveGroundTickCount(0);
        }
    }

    /**
     * This method feeds player with food, if food heal amount more than
     * empty food gaps, then reminder adds to saturation
     *
     * @return result of eating food
     */
    public static InteractionResultHolder<ItemStack> eat(Player player, ItemStack food) {
        if (!food.isEdible()) {
            return InteractionResultHolder.fail(food);
        }

        FoodProperties foodItem = food.getFoodProperties(player);
        if (foodItem != null && player.getFoodData().needsFood()) {
            ItemStack result = ForgeEventFactory.onItemUseFinish(player, food.copy(), player.getUseItemRemainingTicks(),
                    food.finishUsingItem(player.level(), player));
            return InteractionResultHolder.success(result);
        } else {
            return InteractionResultHolder.fail(food);
        }
    }

    /**
     * Format itemstacks list from [1xitem@1, 1xitem@1, 1xitem@2] to
     * [2xitem@1, 1xitem@2]
     *
     * @return Formated list
     */
    public static List<ItemStack> format(List<ItemStack> input) {
        Object2IntMap<ItemStack> items = new Object2IntOpenCustomHashMap<>(
                ItemStackHashStrategy.comparingAllButCount());
        List<ItemStack> output = new ArrayList<>();
        for (ItemStack itemStack : input) {
            if (items.containsKey(itemStack)) {
                int amount = items.getInt(itemStack);
                items.replace(itemStack, ++amount);
            } else {
                items.put(itemStack, 1);
            }
        }
        for (Object2IntMap.Entry<ItemStack> entry : items.object2IntEntrySet()) {
            ItemStack stack = entry.getKey().copy();
            stack.setCount(entry.getIntValue());
            output.add(stack);
        }
        return output;
    }

    @Nonnull
    public static String format(long value) {
        return new DecimalFormat("###,###.##").format(value);
    }

    public static String format(double value) {
        return new DecimalFormat("###,###.##").format(value);
    }

    /**
     * Modular HUD class for armor
     * now available only string rendering, if will be needed,
     * may be will add some additional functions
     */
    @OnlyIn(Dist.CLIENT)
    public static class ModularHUD {

        private byte stringAmount = 0;
        private final List<Component> stringList;
        private static final Minecraft mc = Minecraft.getInstance();

        public ModularHUD() {
            this.stringList = new ArrayList<>();
        }

        public void newString(Component string) {
            this.stringAmount++;
            this.stringList.add(string);
        }

        public void draw(GuiGraphics poseStack) {
            for (int i = 0; i < stringAmount; i++) {
                IntIntPair coords = this.getStringCoord(i);
                poseStack.drawString(mc.font, stringList.get(i), coords.firstInt(), coords.secondInt(), 0xFFFFFF,
                        false);
            }
        }

        @Nonnull
        private IntIntPair getStringCoord(int index) {
            int posX;
            int posY;
            int fontHeight = mc.font.lineHeight;
            int windowHeight = mc.getWindow().getGuiScaledHeight();
            int windowWidth = mc.getWindow().getGuiScaledWidth();
            int stringWidth = mc.font.width(stringList.get(index));
            switch (ConfigHolder.INSTANCE.client.armorHud.hudLocation) {
                case 1 -> {
                    posX = 1 + ConfigHolder.INSTANCE.client.armorHud.hudOffsetX;
                    posY = 1 + ConfigHolder.INSTANCE.client.armorHud.hudOffsetY + (fontHeight * index);
                }
                case 2 -> {
                    posX = windowWidth - (1 + ConfigHolder.INSTANCE.client.armorHud.hudOffsetX) - stringWidth;
                    posY = 1 + ConfigHolder.INSTANCE.client.armorHud.hudOffsetY + (fontHeight * index);
                }
                case 3 -> {
                    posX = 1 + ConfigHolder.INSTANCE.client.armorHud.hudOffsetX;
                    posY = windowHeight - fontHeight * (stringAmount - index) - 1 -
                            ConfigHolder.INSTANCE.client.armorHud.hudOffsetY;
                }
                case 4 -> {
                    posX = windowWidth - (1 + ConfigHolder.INSTANCE.client.armorHud.hudOffsetX) - stringWidth;
                    posY = windowHeight - fontHeight * (stringAmount - index) - 1 -
                            ConfigHolder.INSTANCE.client.armorHud.hudOffsetY;
                }
                default -> throw new IllegalArgumentException(
                        "Armor Hud config hudLocation is improperly configured. Allowed values: [1,2,3,4]");
            }
            return IntIntPair.of(posX, posY);
        }

        public void reset() {
            this.stringAmount = 0;
            this.stringList.clear();
        }
    }
}

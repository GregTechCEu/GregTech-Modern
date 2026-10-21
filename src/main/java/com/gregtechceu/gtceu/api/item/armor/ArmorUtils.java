package com.gregtechceu.gtceu.api.item.armor;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.api.capability.compat.FeCompat;
import com.gregtechceu.gtceu.common.data.GTSoundEntries;
import com.gregtechceu.gtceu.common.data.item.GTDataComponents;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.core.mixins.ServerGamePacketListenerImplAccessor;
import com.gregtechceu.gtceu.utils.input.SyncedKeyMappings;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.EmptyItemHandler;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import it.unimi.dsi.fastutil.ints.IntList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ArmorUtils {

    public static final int MIN_NIGHTVISION_CHARGE = 4;
    public static final int NIGHTVISION_DURATION = 20 * 20; // 20 seconds
    // Flashing starts at 10 seconds + two second buffer to prevent flicker
    public static final int NIGHT_VISION_RESET = 12 * 20;
    public static final long CREDITS_PER_EU = GTValues.M;
    // 2^63 / the first double above the long range
    private static final double LONG_LIMIT = 0x1.0p63;

    // Full strength running cost in EU/t
    public static double getModuleEnergyCost(int tier) {
        return Math.scalb(0.25, 2 * (tier - GTValues.LV) + (tier >= GTValues.EV ? 1 : 0));
    }

    /**
     * Converts an EU cost to credits, rounding up
     *
     * @param amount of EU to convert into credits
     * @return credits or -1 for invalid or overflowing costs
     */
    public static long toModuleEnergyCredits(double amount) {
        if (!Double.isFinite(amount) || amount < 0) return -1;
        double credits = Math.ceil(amount * CREDITS_PER_EU);
        return credits >= LONG_LIMIT ? -1 : (long) credits;
    }

    // Consume whole EU and retain the unused credits for later ticks or actions.
    public static boolean useModuleEnergy(ItemStack stack, long costCredits, boolean simulate) {
        if (costCredits < 0) return false;
        if (costCredits == 0) return true;
        IElectricItem battery = GTCapabilityHelper.getElectricItem(stack);
        if (battery == null) return false;
        long credit = Math.clamp((long) stack.getOrDefault(GTDataComponents.MODULE_ENERGY_CREDIT, 0),
                0L, CREDITS_PER_EU - 1);
        long requiredEU = costCredits / CREDITS_PER_EU;
        long remainingCredit = credit - costCredits % CREDITS_PER_EU;
        if (remainingCredit < 0) {
            requiredEU++;
            remainingCredit += CREDITS_PER_EU;
        }
        if (!battery.canUse(requiredEU)) return false;
        if (simulate) return true;
        if (requiredEU > 0) {
            long extracted = battery.discharge(requiredEU, battery.getTier(), true, false, false);
            if (extracted != requiredEU) {
                battery.charge(extracted, battery.getTier(), true, false);
                return false;
            }
        }
        stack.set(GTDataComponents.MODULE_ENERGY_CREDIT, (int) remainingCredit);
        return true;
    }

    public static boolean isMoving(LivingEntity entity) {
        if (entity instanceof Player player) {
            return SyncedKeyMappings.VANILLA_FORWARD.isKeyDown(player) ||
                    SyncedKeyMappings.VANILLA_BACKWARD.isKeyDown(player) ||
                    SyncedKeyMappings.VANILLA_LEFT.isKeyDown(player) ||
                    SyncedKeyMappings.VANILLA_RIGHT.isKeyDown(player);
        }
        return entity.getDeltaMovement().horizontalDistanceSqr() > 1.0E-6;
    }

    public static boolean isMovingOnGround(LivingEntity entity) {
        return entity.onGround() && !entity.isInWater() && !entity.isPassenger() && !entity.isFallFlying() &&
                !(entity instanceof Player player && player.getAbilities().flying) && isMoving(entity);
    }

    /**
     * Shares the energy evenly across all valid items, respecting max I/O rates
     */
    public static void chargeCarriedItems(Player player, ItemStack sourceStack, IElectricItem source, long budget) {
        long remaining = Math.min(budget, source.getCharge());
        if (remaining <= 0) return;
        record Candidate(ItemStack stack, long demand) {}
        var stacks = new ArrayList<ItemStack>();
        if (GTCEu.Mods.isCuriosLoaded()) {
            IItemHandler curios = CuriosApi.getCuriosInventory(player)
                    .<IItemHandler>map(ICuriosItemHandler::getEquippedCurios)
                    .orElse(EmptyItemHandler.INSTANCE);
            for (int i = 0; i < curios.getSlots(); i++) stacks.add(curios.getStackInSlot(i));
        }
        var inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) stacks.add(inventory.getItem(i));
        var candidates = new ArrayList<Candidate>();
        for (ItemStack stack : stacks) {
            long demand = chargeCarriedItem(remaining, sourceStack, source, stack, true);
            if (demand > 0) candidates.add(new Candidate(stack, demand));
        }
        if (candidates.isEmpty()) return;
        Collections.rotate(candidates, Math.floorMod(player.tickCount, candidates.size()));
        candidates.sort(Comparator.comparingLong(Candidate::demand));
        for (int i = 0; i < candidates.size() && remaining > 0; i++) {
            Candidate candidate = candidates.get(i);
            long share = Math.min(candidate.demand(), remaining / (candidates.size() - i));
            if (share > 0) {
                remaining -= chargeCarriedItem(share, sourceStack, source, candidate.stack(), false);
            }
        }
    }

    private static long chargeCarriedItem(long remaining, ItemStack sourceStack, IElectricItem source,
                                          ItemStack target, boolean simulate) {
        if (target == sourceStack) return 0;
        IElectricItem targetEnergy = GTCapabilityHelper.getElectricItem(target);
        if (targetEnergy != null) {
            if (targetEnergy.canProvideChargeExternally()) return 0;
            long available = source.discharge(remaining, source.getTier(), true, false, true);
            long accepted = targetEnergy.charge(available, source.getTier(), false, true);
            if (simulate) return accepted;
            long extracted = source.discharge(accepted, source.getTier(), true, false, false);
            long received = targetEnergy.charge(extracted, source.getTier(), false, false);
            if (received < extracted) source.charge(extracted - received, source.getTier(), true, false);
            return received;
        }
        if (ConfigHolder.INSTANCE.compat.energy.nativeEUToFE) {
            var targetFE = GTCapabilityHelper.getForgeEnergyItem(target);
            if (targetFE != null && targetFE.canReceive()) {
                long available = source.discharge(remaining, source.getTier(), true, false, true);
                long accepted = FeCompat.insertEu(targetFE, available, true);
                if (simulate) return accepted;
                long extracted = source.discharge(accepted, source.getTier(), true, false, false);
                long received = FeCompat.insertEu(targetFE, extracted, false);
                if (received < extracted) source.charge(extracted - received, source.getTier(), true, false);
                return received;
            }
        }
        return 0;
    }

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

        ItemStack offHand = player.getInventory().offhand.getFirst();
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
    public static void spawnParticle(Level world, Player player, @Nullable ParticleOptions type, double speedY) {
        if (type != null) {
            Vec3 forward = player.getForward();
            world.addParticle(type, player.getX() - forward.x, player.getY() + 0.5D, player.getZ() - forward.z, 0.0D,
                    speedY, 0.0D);
        }
    }

    public static void playJetpackSound(@NotNull Player player) {
        if (player.level().isClientSide()) {
            float cons = (float) player.getDeltaMovement().y + player.moveDist;
            cons = Mth.clamp(cons, 0.6F, 1.0F);

            if (player.getDeltaMovement().y > 0.05F) {
                cons += 0.1F;
            }

            if (player.getDeltaMovement().y < -0.05F) {
                cons -= 0.4F;
            }

            player.playSound(GTSoundEntries.JET_ENGINE.value().getMainEvent(), 0.3F, cons);
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
        if (food.getFoodProperties(player) == null) {
            return InteractionResultHolder.fail(food);
        }

        FoodProperties foodItem = food.getFoodProperties(player);
        if (foodItem != null && player.getFoodData().needsFood()) {
            ItemStack result = EventHooks.onItemUseFinish(player, food.copy(), player.getUseItemRemainingTicks(),
                    food.finishUsingItem(player.level(), player));
            return InteractionResultHolder.success(result);
        } else {
            return InteractionResultHolder.fail(food);
        }
    }

    /**
     * Modular HUD class for armor
     * now available only string rendering, if will be needed,
     * may be will add some additional functions
     */
    @OnlyIn(Dist.CLIENT)
    public static class ModularHUD {

        private byte stringAmount = 0;
        private final List<HudLine> stringList;
        private static final Minecraft mc = Minecraft.getInstance();

        private record HudLine(int columnWidth, Component separator, Component[] columns) {}

        public ModularHUD() {
            this.stringList = new ArrayList<>();
        }

        public void newLine(Component string) {
            newLine(mc.font.width(string), Component.empty(), string);
        }

        public void newLine(int columnWidth, Component separator, Component... columns) {
            this.stringAmount++;
            this.stringList.add(new HudLine(columnWidth, separator, columns));
        }

        public void draw(GuiGraphics guiGraphics) {
            for (int i = 0; i < stringAmount; i++) {
                HudLine line = stringList.get(i);
                int separatorWidth = mc.font.width(line.separator());
                int width = line.columnWidth() * line.columns().length +
                        separatorWidth * (line.columns().length - 1);
                IntIntPair coords = this.getStringCoord(i, width);
                int x = coords.firstInt();
                for (int column = 0; column < line.columns().length; column++) {
                    Component text = line.columns()[column];
                    guiGraphics.drawString(mc.font, text, x + line.columnWidth() - mc.font.width(text),
                            coords.secondInt(), 0xFFFFFF, false);
                    x += line.columnWidth();
                    if (column < line.columns().length - 1) {
                        guiGraphics.drawString(mc.font, line.separator(), x, coords.secondInt(), 0xFFFFFF, false);
                        x += separatorWidth;
                    }
                }
            }
        }

        private IntIntPair getStringCoord(int index, int stringWidth) {
            int posX;
            int posY;
            int fontHeight = mc.font.lineHeight;
            int windowHeight = mc.getWindow().getGuiScaledHeight();
            int windowWidth = mc.getWindow().getGuiScaledWidth();
            switch (ConfigHolder.INSTANCE.client.armorHud.hudLocation) {
                case 1 -> {
                    posX = 10 + ConfigHolder.INSTANCE.client.armorHud.hudOffsetX;
                    posY = 10 + ConfigHolder.INSTANCE.client.armorHud.hudOffsetY + (fontHeight * index);
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

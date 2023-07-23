package com.gregtechceu.gtceu.common.item;

import gregtech.api.GTValues;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IElectricItem;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.integration.baubles.BaublesModule;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.player.PlayerPickupXpEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nonnull;
import java.util.List;

public class ItemMagnetBehavior implements IItemBehaviour {

    private final int range;
    private final long energyDraw;

    public ItemMagnetBehavior(int range) {
        this.range = range;
        this.energyDraw = GTValues.V[range > 8 ? GTValues.HV : GTValues.LV];
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, @Nonnull EntityPlayer player, EnumHand hand) {
        if (!player.world.isRemote && player.isSneaking()) {
            player.sendStatusMessage(new TextComponentTranslation(toggleActive(player.getHeldItem(hand)) ? "behavior.item_magnet.enabled" : "behavior.item_magnet.disabled"), true);
        }
        return ActionResult.newResult(EnumActionResult.PASS, player.getHeldItem(hand));
    }

    private static boolean isActive(ItemStack stack) {
        if (stack == ItemStack.EMPTY) {
            return false;
        }
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) {
            return false;
        }
        if (tag.hasKey("IsActive")) {
            return tag.getBoolean("IsActive");
        }
        return false;
    }

    private static boolean toggleActive(ItemStack stack) {
        boolean isActive = isActive(stack);
        if (!stack.hasTagCompound()) {
            stack.setTagCompound(new NBTTagCompound());
        }
        //noinspection ConstantConditions
        stack.getTagCompound().setBoolean("IsActive", !isActive);
        return !isActive;
    }

    @Override
    public void onUpdate(ItemStack stack, Entity entity) {
        // Adapted logic from Draconic Evolution
        // https://github.com/Draconic-Inc/Draconic-Evolution/blob/1.12.2/src/main/java/com/brandon3055/draconicevolution/items/tools/Magnet.java
        if (!entity.isSneaking() && entity.ticksExisted % 10 == 0 && isActive(stack) && entity instanceof EntityPlayer player) {
            World world = entity.getEntityWorld();
            if (!drainEnergy(true, stack, energyDraw)) {
                return;
            }

            List<EntityItem> items = world.getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(entity.posX, entity.posY, entity.posZ, entity.posX, entity.posY, entity.posZ).grow(range, range, range));

            boolean didMoveEntity = false;
            for (EntityItem itemEntity : items) {
                if (itemEntity.isDead) {
                    continue;
                }

                NBTTagCompound itemTag = itemEntity.getEntityData();
                if (itemTag != null && itemTag.hasKey("PreventRemoteMovement")) {
                    continue;
                }

                if (itemEntity.getThrower() != null && itemEntity.getThrower().equals(entity.getName()) && itemEntity.pickupDelay > 0) {
                    continue;
                }

                EntityPlayer closest = world.getClosestPlayerToEntity(itemEntity, 4);
                if (closest != null && closest != entity) {
                    continue;
                }

                if (!world.isRemote) {
                    if (itemEntity.pickupDelay > 0) {
                        itemEntity.pickupDelay = 0;
                    }
                    itemEntity.motionX = itemEntity.motionY = itemEntity.motionZ = 0;
                    itemEntity.setPosition(entity.posX - 0.2 + (world.rand.nextDouble() * 0.4), entity.posY - 0.6, entity.posZ - 0.2 + (world.rand.nextDouble() * 0.4));
                    didMoveEntity = true;
                }
            }

            if (didMoveEntity) {
                world.playSound(null, entity.posX, entity.posY, entity.posZ, SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 0.1F, 0.5F * ((world.rand.nextFloat() - world.rand.nextFloat()) * 0.7F + 2F));
            }

            List<EntityXPOrb> xp = world.getEntitiesWithinAABB(EntityXPOrb.class, new AxisAlignedBB(entity.posX, entity.posY, entity.posZ, entity.posX, entity.posY, entity.posZ).grow(4, 4, 4));

            for (EntityXPOrb orb : xp) {
                if (!world.isRemote && !orb.isDead) {
                    if (orb.delayBeforeCanPickup == 0) {
                        if (MinecraftForge.EVENT_BUS.post(new PlayerPickupXpEvent(player, orb))) {
                            continue;
                        }
                        world.playSound(null, entity.posX, entity.posY, entity.posZ, SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 0.1F, 0.5F * ((world.rand.nextFloat() - world.rand.nextFloat()) * 0.7F + 1.8F));
                        player.onItemPickup(orb, 1);
                        player.addExperience(orb.xpValue);
                        orb.setDead();
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
    public void onItemToss(@Nonnull ItemTossEvent event) {
        if (event.getPlayer() == null) return;

        IInventory inventory = event.getPlayer().inventory;
        if (Loader.isModLoaded(GTValues.MODID_BAUBLES)) {
            inventory = BaublesModule.getBaublesWrappedInventory(event.getPlayer());
        }

        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            ItemStack stackInSlot = inventory.getStackInSlot(i);
            if (isMagnet(stackInSlot) && isActive(stackInSlot)) {
                event.getEntityItem().setPickupDelay(60);
                return;
            }
        }
    }

    private boolean isMagnet(@Nonnull ItemStack stack) {
        if (stack.getItem() instanceof MetaItem<?> metaItem) {
            for (var behavior : metaItem.getBehaviours(stack)) {
                if (behavior instanceof ItemMagnetBehavior) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean drainEnergy(boolean simulate, @Nonnull ItemStack stack, long amount) {
        IElectricItem electricItem = stack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
        if (electricItem == null)
            return false;

        return electricItem.discharge(amount, Integer.MAX_VALUE, true, false, simulate) >= amount;
    }

    @Override
    public void addInformation(ItemStack itemStack, List<String> lines) {
        IItemBehaviour.super.addInformation(itemStack, lines);
        lines.add(I18n.format(isActive(itemStack) ? "behavior.item_magnet.enabled" : "behavior.item_magnet.disabled"));
    }
}

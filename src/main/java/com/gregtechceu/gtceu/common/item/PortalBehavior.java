package com.gregtechceu.gtceu.common.item;

import com.gregtechceu.gtceu.api.item.component.IAddInformation;
import com.gregtechceu.gtceu.api.item.component.IInteractionItem;
import com.gregtechceu.gtceu.common.entity.PortalEntity;
import com.gregtechceu.gtceu.utils.TeleportHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PortalBehavior implements IInteractionItem, IAddInformation {

    private int targetX, targetY, targetZ;
    private ResourceLocation dimName;

    public void spawnPortals(Player player) {
        Vec3 pos = new Vec3(player.getX() + player.getLookAngle().x * 5,
                player.getY(), player.getZ() + player.getLookAngle().z * 5);

        PortalEntity portalA = new PortalEntity(player.level(), pos.x, pos.y, pos.z);
        portalA.setRot(player.getYRot(), 0.0f);

        PortalEntity portalB = new PortalEntity(player.level(), pos.x, pos.y + 7, pos.z);
        portalA.setRot(player.getYRot(), 0.0f);

        ResourceLocation dimB = player.level().dimension().location();

        portalA.setTargetCoordinates(dimName, pos.x, pos.y + 7, pos.z);
        portalB.setTargetCoordinates(dimB, pos.x, pos.y, pos.z);

        player.level().addFreshEntity(portalA);
        TeleportHandler.getWorldByDimension(dimName).setChunkForced((int)pos.x>>4, (int)pos.z>>4, true);
        TeleportHandler.getWorldByDimension(dimName).addFreshEntity(portalB);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {

    }

    @Override
    public InteractionResultHolder<ItemStack> use(Item item, Level level, Player player, InteractionHand usedHand) {
        if(player.isCrouching()) {
            // todo gui shit
        }
        else {
            this.dimName = player.level().dimension().location();
            spawnPortals(player);
        }

        return IInteractionItem.super.use(item, level, player, usedHand);
    }
}

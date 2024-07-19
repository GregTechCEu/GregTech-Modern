package com.gregtechceu.gtceu.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.ITeleporter;

import java.util.function.Function;

public class TeleportHandler {

    public static ServerLevel getWorldByDimensionID(int id) {
        ServerLevel level = Dimension
    }

    public static void teleport(Entity teleporter, Entity teleportTo) {
        teleport(teleporter, (ServerLevel) teleportTo.level(), teleportTo.position().x, teleportTo.position().y, teleportTo.position().z);
    }

    public static void teleport(Entity teleporter, ServerLevel dimension, BlockPos teleportTo) {
        teleport(teleporter, dimension, teleportTo.getX(), teleportTo.getY(), teleporter.getZ());
    }

    public static void teleport(Entity teleporter, ServerLevel dimension, double teleportToX, double teleportToY, double teleportToZ) {
        if(teleporter.level().isClientSide || !teleporter.isAlive()) {
            return;
        }

        if(!teleporter.getPassengers().isEmpty()) {
            teleporter.ejectPassengers();
        }
        if(teleporter.getVehicle() != null) {
            teleporter.removeVehicle();
        }

        if(teleporter.level() != dimension) {
            PortalInfo portalInfo = new PortalInfo(new Vec3(teleportToX, teleportToY, teleportToZ), Vec3.ZERO, teleporter.getYRot(),
                    teleporter.getXRot());
            teleporter.changeDimension(dimension, new ITeleporter() {
                @Override
                public Entity placeEntity(Entity entity, ServerLevel currentLevel, ServerLevel destLevel, float yaw,
                                          Function<Boolean, Entity> repositionEntity) {
                    return repositionEntity.apply(false);
                }

                @Override
                public PortalInfo getPortalInfo(Entity entity, ServerLevel destLevel,
                                                Function<ServerLevel, PortalInfo> defaultPortalInfo) {
                    return portalInfo;
                }
            });
        }

        teleporter.setPos(teleportToX, teleportToY, teleportToZ);
    }

    public static void teleport(Entity teleporter, ServerLevel dimension, ITeleporter customTeleporter, double teleportToX, double teleportToY, double teleportToZ) {
        if(teleporter.level().isClientSide || !teleporter.isAlive()) {
            return;
        }

        if(!teleporter.getPassengers().isEmpty()) {
            teleporter.ejectPassengers();
        }
        if(teleporter.getVehicle() != null) {
            teleporter.removeVehicle();
        }

        if(teleporter.level() != dimension) {
            PortalInfo portalInfo = new PortalInfo(new Vec3(teleportToX, teleportToY, teleportToZ), Vec3.ZERO, teleporter.getYRot(),
                    teleporter.getXRot());
            teleporter.changeDimension(dimension, customTeleporter);
        }

        teleporter.setPos(teleportToX, teleportToY, teleportToZ);
    }
}

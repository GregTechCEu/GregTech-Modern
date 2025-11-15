package com.gregtechceu.gtceu.client.mui.schemarenderer;

import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;

import lombok.Getter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import static net.minecraft.util.Mth.HALF_PI;

@Accessors(fluent = true)
public class Camera {

    @Getter
    private final Vector3f pos = new Vector3f();
    @Getter
    private final Vector3f lookAt = new Vector3f();
    private final Vector3f temp = new Vector3f();
    @Getter
    private float yaw;
    @Getter
    private float pitch;
    @Getter
    private float dist;

    public Camera() {}

    public Camera setPosAndLookAt(Vector3f pos, Vector3f lookAt) {
        return setPosAndLookAt(pos.x, pos.y, pos.z, lookAt.x, lookAt.y, lookAt.z);
    }

    public Camera setPosAndLookAt(float xPos, float yPos, float zPos, Vector3f lookAt) {
        return setPosAndLookAt(xPos, yPos, zPos, lookAt.x, lookAt.y, lookAt.z);
    }

    public Camera setPosAndLookAt(Vector3f pos, float xLook, float yLook, float zLook) {
        return setPosAndLookAt(pos.x, pos.y, pos.z, xLook, yLook, zLook);
    }

    public Camera setPosAndLookAt(float xPos, float yPos, float zPos, float xLook, float yLook, float zLook) {
        this.pos.set(xPos, yPos, zPos);
        this.lookAt.set(xLook, yLook, zLook);
        Vector3f v = this.temp.set(lookAt).sub(pos).normalize();
        this.yaw = (float) Math.atan2(-v.x, v.z);
        this.pitch = (float) Math.asin(-v.y);
        this.dist = pos.distance(lookAt);
        return this;
    }

    public Camera setLookAtKeepPos(float x, float y, float z) {
        setPosAndLookAt(this.pos, x, y, z);
        return this;
    }

    public Camera setLookAtKeepAngle(float x, float y, float z) {
        // just calculate the offset
        this.temp.set(x, y, z);
        this.temp.sub(this.lookAt, this.temp);
        this.pos.add(this.temp);
        this.lookAt.set(this.temp);
        return this;
    }

    public Camera setPosKeepLookAt(float x, float y, float z) {
        return setPosAndLookAt(x, y, z, this.lookAt);
    }

    public Camera setPosKeepAngle(float x, float y, float z) {
        // just calculate the offset
        this.temp.set(x, y, z);
        this.temp.sub(this.pos, this.temp);
        this.lookAt.add(this.temp);
        this.pos.set(this.temp);
        return this;
    }

    public Camera setAngleKeepLookAt(float radius, float yaw, float pitch) {
        return setLookAtAndAngle(this.lookAt, radius, yaw, pitch);
    }

    public Camera setLookAtAndAngle(Vector3f lookAt, float radius, float yaw, float pitch) {
        return setLookAtAndAngle(lookAt.x, lookAt.y, lookAt.z, radius, yaw, pitch);
    }

    public Camera setLookAtAndAngle(Vec3i lookAt, float radius, float yaw, float pitch) {
        return setLookAtAndAngle(lookAt.getX(), lookAt.getY(), lookAt.getZ(), radius, yaw, pitch);
    }

    public Camera setLookAtAndAngle(float lookAtX, float lookAtY, float lookAtZ, float dist, float yaw, float pitch) {
        this.lookAt.set(lookAtX, lookAtY, lookAtZ);
        this.yaw = yaw;
        this.pitch = pitch;
        this.dist = dist;
        Vector3f v = this.temp;
        v.set(Mth.cos(yaw), 0, Mth.sin(yaw));
        v.y = ((float) Math.tan(pitch)) * v.length();
        v.normalize().mul(dist);
        this.pos.set(v.add(lookAtX, lookAtY, lookAtZ));
        return this;
    }

    public Camera setPosAndAngle(float posX, float posY, float posZ, float dist, float yaw, float pitch) {
        this.pos.set(posX, posY, posZ);
        this.yaw = yaw;
        this.pitch = pitch;
        this.dist = dist;
        Vector3f v = this.temp;
        v.set(Mth.cos(HALF_PI - yaw), 0, Mth.sin(HALF_PI - yaw));
        v.y = ((float) Math.tan(HALF_PI - pitch)) * v.length();
        v.normalize().mul(dist);
        this.lookAt.set(v).add(this.pos);
        return this;
    }

    public void setDistanceKeepLookAt(float dist) {
        if (dist == this.dist) return;
        this.dist = dist;
        this.pos.sub(this.lookAt, this.temp);
        this.temp.normalize().mul(dist);
        this.lookAt.sub(this.temp, this.pos);
    }

    public void scaleDistanceKeepLookAt(float dist) {
        if (dist == 1) return;
        this.dist *= dist;
        this.pos.sub(this.lookAt, this.temp);
        this.temp.mul(dist);
        this.lookAt.add(this.temp, this.pos);
    }

    public Vector3f getLookVec() {
        return getLookVec(null);
    }

    public Vector3f getLookVec(@Nullable Vector3f dest) {
        if (dest == null) dest = new Vector3f();
        return lookAt.sub(pos, dest);
    }
}

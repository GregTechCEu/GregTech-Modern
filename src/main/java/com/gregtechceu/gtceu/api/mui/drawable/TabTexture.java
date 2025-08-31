package com.gregtechceu.gtceu.api.mui.drawable;

import com.gregtechceu.gtceu.api.mui.base.GuiAxis;

import lombok.Getter;

import java.util.Objects;

public class TabTexture {

    public static TabTexture of(UITexture texture, GuiAxis axis, boolean positive, int width, int height,
                                int textureInset) {
        Objects.requireNonNull(texture);
        UITexture sa, ma, ea, si, mi, ei;
        if (axis.isVertical() && !positive) {
            si = texture.getSubArea(0, 0, 1 / 3f, 0.5f);
            mi = texture.getSubArea(1 / 3f, 0, 2 / 3f, 0.5f);
            ei = texture.getSubArea(2 / 3f, 0, 1f, 0.5f);
            sa = texture.getSubArea(0, 0.5f, 1 / 3f, 1);
            ma = texture.getSubArea(1 / 3f, 0.5f, 2 / 3f, 1);
            ea = texture.getSubArea(2 / 3f, 0.5f, 1f, 1);
        } else if (axis.isVertical()) {
            sa = texture.getSubArea(0, 0, 1 / 3f, 0.5f);
            ma = texture.getSubArea(1 / 3f, 0, 2 / 3f, 0.5f);
            ea = texture.getSubArea(2 / 3f, 0, 1f, 0.5f);
            si = texture.getSubArea(0, 0.5f, 1 / 3f, 1);
            mi = texture.getSubArea(1 / 3f, 0.5f, 2 / 3f, 1);
            ei = texture.getSubArea(2 / 3f, 0.5f, 1f, 1);
        } else if (axis.isHorizontal() && !positive) {
            si = texture.getSubArea(0, 0, 0.5f, 1 / 3f);
            mi = texture.getSubArea(0, 1 / 3f, 0.5f, 2 / 3f);
            ei = texture.getSubArea(0, 2 / 3f, 0.5f, 1f);
            sa = texture.getSubArea(0.5f, 0, 1, 1 / 3f);
            ma = texture.getSubArea(0.5f, 1 / 3f, 1, 2 / 3f);
            ea = texture.getSubArea(0.5f, 2 / 3f, 1, 1f);
        } else if (axis.isHorizontal()) {
            sa = texture.getSubArea(0, 0, 0.5f, 1 / 3f);
            ma = texture.getSubArea(0, 1 / 3f, 0.5f, 2 / 3f);
            ea = texture.getSubArea(0, 2 / 3f, 0.5f, 1f);
            si = texture.getSubArea(0.5f, 0, 1, 1 / 3f);
            mi = texture.getSubArea(0.5f, 1 / 3f, 1, 2 / 3f);
            ei = texture.getSubArea(0.5f, 2 / 3f, 1, 1f);
        } else {
            throw new IllegalArgumentException();
        }
        return new TabTexture(sa, ma, ea, si, mi, ei, width, height, textureInset, axis, positive);
    }

    private final UITexture startActive;
    private final UITexture active;
    private final UITexture endActive;

    private final UITexture startInactive;
    private final UITexture inactive;
    private final UITexture endInactive;
    @Getter
    private final int width, height;
    @Getter
    private final int textureInset;
    @Getter
    private final GuiAxis axis;
    @Getter
    private final boolean positive;

    public TabTexture(UITexture startActive, UITexture active, UITexture endActive, UITexture startInactive,
                      UITexture inactive, UITexture endInactive, int width, int height, int textureInset, GuiAxis axis,
                      boolean positive) {
        this.startActive = startActive;
        this.active = active;
        this.endActive = endActive;
        this.startInactive = startInactive;
        this.inactive = inactive;
        this.endInactive = endInactive;
        this.width = width;
        this.height = height;
        this.textureInset = textureInset;
        this.axis = axis;
        this.positive = positive;
    }

    public UITexture getStart(boolean active) {
        return active ? this.startActive : this.startInactive;
    }

    public UITexture getMiddle(boolean active) {
        return active ? this.active : this.inactive;
    }

    public UITexture getEnd(boolean active) {
        return active ? this.endActive : this.endInactive;
    }

    public UITexture get(int location, boolean active) {
        if (location == 0) {
            return getMiddle(active);
        }
        if (location < 0) {
            return getStart(active);
        }
        return getEnd(active);
    }
}

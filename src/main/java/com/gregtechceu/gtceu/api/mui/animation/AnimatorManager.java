package com.gregtechceu.gtceu.api.mui.animation;

import net.minecraft.Util;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

public class AnimatorManager {

    private static final List<IAnimator> animators = new ArrayList<>(16);
    private static final List<IAnimator> queuedAnimators = new ArrayList<>(8);
    private static long lastTime = 0;

    static void startAnimation(IAnimator animator) {
        if (!animators.contains(animator) && !queuedAnimators.contains(animator)) {
            queuedAnimators.add(animator);
        }
    }

    private AnimatorManager() {}

    public static void init() {
        MinecraftForge.EVENT_BUS.register(new AnimatorManager());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onDraw(ScreenEvent.Render.Pre event) {
        long time = Util.getMillis();
        int elapsedTime = IAnimator.getTimeDiff(lastTime, time);
        if (lastTime > 0 && !animators.isEmpty()) {
            animators.removeIf(animator -> {
                if (animator == null) return true;
                if (animator.isPaused()) return false;
                animator.advance(elapsedTime);
                return !animator.isAnimating();
            });
        }
        lastTime = time;
        animators.addAll(queuedAnimators);
        queuedAnimators.clear();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onDraw(ScreenEvent.Opening event) {
        if (event.getNewScreen() == null) {
            // stop and yeet all animators on gui close
            animators.forEach(iAnimator -> iAnimator.stop(false));
            animators.clear();
        }
    }
}

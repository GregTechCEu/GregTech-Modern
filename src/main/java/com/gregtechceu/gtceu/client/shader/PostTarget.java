package com.gregtechceu.gtceu.client.shader;

import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.client.Minecraft;

public class PostTarget extends RenderTarget {

   public PostTarget(int width, int height, boolean useDepth) {
      super(useDepth);
      this.resize(width, height, Minecraft.ON_OSX);
   }
}

/*
 * Copyright (c) 2016, 2017, 2018, 2019 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.gregtechceu.gtceu.core.mixins.client;

import com.gregtechceu.gtceu.client.model.quad.SpriteFinder;

import net.minecraft.client.renderer.texture.SpriteLoader;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(TextureAtlas.class)
public class TextureAtlasMixin implements SpriteFinder.SpriteFinderAccess {

    @Shadow
    private Map<ResourceLocation, TextureAtlasSprite> texturesByName;

    @Unique
    private SpriteFinder gtceu$spriteFinder = null;

    @Inject(at = @At("TAIL"), method = "upload")
    private void uploadHook(SpriteLoader.Preparations preparations, CallbackInfo info) {
        gtceu$spriteFinder = null;
    }

    @Override
    public @NotNull SpriteFinder gtceu$spriteFinder() {
        if (this.gtceu$spriteFinder == null) {
            this.gtceu$spriteFinder = new SpriteFinder(texturesByName, (TextureAtlas) (Object) this);
        }
        return this.gtceu$spriteFinder;
    }
}

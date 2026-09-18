package brachy.modularui.drawable;

import brachy.modularui.ModularUI;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.TextureAtlasHolder;
import net.minecraft.resources.Identifier;

import org.jetbrains.annotations.NotNull;

/**
 * 1.21 has this class in vanilla, it can be used via {@code Minecraft.getGuiSprites()}.<br>
 * Here in 1.20 land, though, we have to implement it ourselves.
 * <p>
 * Note that the atlas JSON <i>should</i> be kept, as MC 1.21 only adds textures in gui/sprites to the atlas. We want all of them.
 */
public class GuiSpriteManager extends TextureAtlasHolder {

    public static final Identifier LOCATION_GUI = ModularUI.id("textures/atlas/gui.png");

    private static final Identifier atlasInfoLocation = ModularUI.id("gui");
    private static GuiSpriteManager instance = null;

    public GuiSpriteManager(TextureManager textureManager) {
        super(textureManager, LOCATION_GUI, atlasInfoLocation);

        if (instance != null) {
            throw new IllegalStateException("Cannot create more than one GuiTextureAtlas instance!");
        }
        instance = this;
    }

    public static GuiSpriteManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Cannot get GuiTextureAtlas instance before it's initialized!");
        }
        return instance;
    }

    /**
     * Gets a sprite associated with the passed resource location.
     */
    @Override
    public @NotNull TextureAtlasSprite getSprite(@NotNull Identifier location) {
        return super.getSprite(location);
    }

}

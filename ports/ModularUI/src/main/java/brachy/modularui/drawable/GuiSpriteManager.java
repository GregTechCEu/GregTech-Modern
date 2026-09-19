package brachy.modularui.drawable;

import brachy.modularui.ModularUI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

/** Access to the GUI atlas owned, reloaded, animated and disposed by Minecraft's AtlasManager. */
public final class GuiSpriteManager {
    public static final Identifier LOCATION_GUI = ModularUI.id("textures/atlas/gui.png");
    public static final Identifier ATLAS_ID = ModularUI.id("gui");
    private static final GuiSpriteManager INSTANCE = new GuiSpriteManager();

    private GuiSpriteManager() {}

    public static GuiSpriteManager getInstance() {
        return INSTANCE;
    }

    public @NotNull TextureAtlasSprite getSprite(@NotNull Identifier location) {
        return Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(ATLAS_ID).getSprite(location);
    }
}

package brachy.modularui.drawable;

import brachy.modularui.ModularUI;

import net.minecraft.client.renderer.texture.SpriteLoader;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.metadata.gui.GuiMetadataSection;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.PreparableReloadListener;

import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Loads ModularUI's GUI atlas, including textures outside vanilla's gui/sprites directory.
 * The texture manager owns the atlas and handles its animation ticks and disposal.
 */
public class GuiSpriteManager implements PreparableReloadListener {

    public static final Identifier LOCATION_GUI = ModularUI.id("textures/atlas/gui.png");

    private static final Identifier atlasInfoLocation = ModularUI.id("gui");
    private static GuiSpriteManager instance = null;
    private final TextureAtlas atlas;

    public GuiSpriteManager(TextureManager textureManager) {
        if (instance != null) {
            throw new IllegalStateException("Cannot create more than one GuiTextureAtlas instance!");
        }
        this.atlas = new TextureAtlas(LOCATION_GUI);
        textureManager.register(LOCATION_GUI, this.atlas);
        instance = this;
    }

    @Override
    public CompletableFuture<Void> reload(SharedState currentReload, Executor taskExecutor,
                                          PreparationBarrier preparationBarrier, Executor reloadExecutor) {
        return SpriteLoader.create(this.atlas)
                .loadAndStitch(currentReload.resourceManager(), atlasInfoLocation, 0, taskExecutor,
                        Set.of(GuiMetadataSection.TYPE))
                .thenCompose(preparations -> preparations.readyForUpload().thenApply(unused -> preparations))
                .thenCompose(preparationBarrier::wait)
                .thenAcceptAsync(this.atlas::upload, reloadExecutor);
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
    public @NotNull TextureAtlasSprite getSprite(@NotNull Identifier location) {
        return this.atlas.getSprite(location);
    }

}

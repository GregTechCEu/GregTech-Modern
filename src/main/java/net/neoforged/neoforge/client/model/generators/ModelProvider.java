package net.neoforged.neoforge.client.model.generators;

import com.gregtechceu.gtceu.utils.data.ExistingFileHelper;
import com.gregtechceu.gtceu.utils.data.ExistingFileHelper.ResourceType;

import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Function;

public abstract class ModelProvider<T extends ModelBuilder<T>> implements DataProvider {

    public static final String BLOCK_FOLDER = "block";
    public static final String ITEM_FOLDER = "item";

    public static final ResourceType TEXTURE = new ResourceType(PackType.CLIENT_RESOURCES, ".png", "textures");
    protected static final ResourceType MODEL = new ResourceType(PackType.CLIENT_RESOURCES, ".json", "models");
    protected static final ResourceType MODEL_WITH_EXTENSION = new ResourceType(PackType.CLIENT_RESOURCES, "",
            "models");

    protected final PackOutput output;
    protected final String modid;
    protected final String folder;
    protected final Function<Identifier, T> factory;

    @VisibleForTesting
    public final Map<Identifier, T> generatedModels = new HashMap<>();
    @VisibleForTesting
    public final ExistingFileHelper existingFileHelper;

    protected ModelProvider(PackOutput output, String modid, String folder, Function<Identifier, T> factory,
                            ExistingFileHelper existingFileHelper) {
        this.output = Preconditions.checkNotNull(output);
        this.modid = Preconditions.checkNotNull(modid);
        this.folder = Preconditions.checkNotNull(folder);
        this.factory = Preconditions.checkNotNull(factory);
        this.existingFileHelper = Preconditions.checkNotNull(existingFileHelper);
    }

    protected ModelProvider(PackOutput output, String modid, String folder,
                            BiFunction<Identifier, ExistingFileHelper, T> factory,
                            ExistingFileHelper existingFileHelper) {
        this(output, modid, folder, id -> factory.apply(id, existingFileHelper), existingFileHelper);
    }

    protected abstract void registerModels();

    public T getBuilder(String path) {
        Preconditions.checkNotNull(path, "Path must not be null");
        Identifier outputLoc = extendWithFolder(path.contains(":") ? Identifier.parse(path) :
                Identifier.fromNamespaceAndPath(modid, path));
        existingFileHelper.trackGenerated(outputLoc, MODEL);
        return generatedModels.computeIfAbsent(outputLoc, factory);
    }

    private Identifier extendWithFolder(Identifier id) {
        if (id.getPath().contains("/")) return id;
        return id.withPath(path -> folder + "/" + path);
    }

    public Identifier modLoc(String name) {
        return Identifier.fromNamespaceAndPath(modid, name);
    }

    public Identifier mcLoc(String name) {
        return name.contains(":") ? Identifier.parse(name) : Identifier.withDefaultNamespace(name);
    }

    public T withExistingParent(String name, String parent) {
        return withExistingParent(name, mcLoc(parent));
    }

    public T withExistingParent(String name, Identifier parent) {
        return getBuilder(name).parent(getExistingFile(parent));
    }

    public T cube(String name, Identifier down, Identifier up, Identifier north, Identifier south, Identifier east,
                  Identifier west) {
        return withExistingParent(name, "block/cube")
                .texture("down", down)
                .texture("up", up)
                .texture("north", north)
                .texture("south", south)
                .texture("east", east)
                .texture("west", west);
    }

    public T cube(String name, Material down, Material up, Material north, Material south, Material east,
                  Material west) {
        return cube(name, down.sprite(), up.sprite(), north.sprite(), south.sprite(), east.sprite(), west.sprite());
    }

    public T singleTexture(String name, Identifier parent, Identifier texture) {
        return singleTexture(name, parent, "texture", texture);
    }

    public T singleTexture(String name, Identifier parent, Material texture) {
        return singleTexture(name, parent, texture.sprite());
    }

    public T singleTexture(String name, Identifier parent, String textureKey, Identifier texture) {
        return withExistingParent(name, parent).texture(textureKey, texture);
    }

    private T singleTexture(String name, String parent, String textureKey, Identifier texture) {
        return singleTexture(name, mcLoc(parent), textureKey, texture);
    }

    public T cubeAll(String name, Identifier texture) {
        return singleTexture(name, "block/cube_all", "all", texture);
    }

    public T cubeAll(String name, Material texture) {
        return cubeAll(name, texture.sprite());
    }

    public T cubeTop(String name, Identifier side, Identifier top) {
        return withExistingParent(name, "block/cube_top").texture("side", side).texture("top", top);
    }

    public T cubeTop(String name, Material side, Material top) {
        return cubeTop(name, side.sprite(), top.sprite());
    }

    private T sideBottomTop(String name, String parent, Identifier side, Identifier bottom, Identifier top) {
        return withExistingParent(name, parent).texture("side", side).texture("bottom", bottom).texture("top", top);
    }

    public T cubeBottomTop(String name, Identifier side, Identifier bottom, Identifier top) {
        return sideBottomTop(name, "block/cube_bottom_top", side, bottom, top);
    }

    public T cubeBottomTop(String name, Material side, Material bottom, Material top) {
        return cubeBottomTop(name, side.sprite(), bottom.sprite(), top.sprite());
    }

    public T cubeColumn(String name, Identifier side, Identifier end) {
        return withExistingParent(name, "block/cube_column").texture("side", side).texture("end", end);
    }

    public T cubeColumn(String name, Material side, Material end) {
        return cubeColumn(name, side.sprite(), end.sprite());
    }

    public T cubeColumnHorizontal(String name, Identifier side, Identifier end) {
        return withExistingParent(name, "block/cube_column_horizontal").texture("side", side).texture("end", end);
    }

    public T cubeColumnHorizontal(String name, Material side, Material end) {
        return cubeColumnHorizontal(name, side.sprite(), end.sprite());
    }

    public T orientable(String name, Identifier side, Identifier front, Identifier top) {
        return withExistingParent(name, "block/orientable")
                .texture("side", side)
                .texture("front", front)
                .texture("top", top);
    }

    public T orientable(String name, Material side, Material front, Material top) {
        return orientable(name, side.sprite(), front.sprite(), top.sprite());
    }

    public T stairs(String name, Identifier side, Identifier bottom, Identifier top) {
        return sideBottomTop(name, "block/stairs", side, bottom, top);
    }

    public T stairs(String name, Material side, Material bottom, Material top) {
        return stairs(name, side.sprite(), bottom.sprite(), top.sprite());
    }

    public T stairsOuter(String name, Identifier side, Identifier bottom, Identifier top) {
        return sideBottomTop(name, "block/outer_stairs", side, bottom, top);
    }

    public T stairsOuter(String name, Material side, Material bottom, Material top) {
        return stairsOuter(name, side.sprite(), bottom.sprite(), top.sprite());
    }

    public T stairsInner(String name, Identifier side, Identifier bottom, Identifier top) {
        return sideBottomTop(name, "block/inner_stairs", side, bottom, top);
    }

    public T stairsInner(String name, Material side, Material bottom, Material top) {
        return stairsInner(name, side.sprite(), bottom.sprite(), top.sprite());
    }

    public T slab(String name, Identifier side, Identifier bottom, Identifier top) {
        return sideBottomTop(name, "block/slab", side, bottom, top);
    }

    public T slab(String name, Material side, Material bottom, Material top) {
        return slab(name, side.sprite(), bottom.sprite(), top.sprite());
    }

    public T slabTop(String name, Identifier side, Identifier bottom, Identifier top) {
        return sideBottomTop(name, "block/slab_top", side, bottom, top);
    }

    public T slabTop(String name, Material side, Material bottom, Material top) {
        return slabTop(name, side.sprite(), bottom.sprite(), top.sprite());
    }

    public T button(String name, Identifier texture) {
        return singleTexture(name, "block/button", "texture", texture);
    }

    public T button(String name, Material texture) {
        return button(name, texture.sprite());
    }

    public T buttonPressed(String name, Identifier texture) {
        return singleTexture(name, "block/button_pressed", "texture", texture);
    }

    public T buttonPressed(String name, Material texture) {
        return buttonPressed(name, texture.sprite());
    }

    public T buttonInventory(String name, Identifier texture) {
        return singleTexture(name, "block/button_inventory", "texture", texture);
    }

    public T buttonInventory(String name, Material texture) {
        return buttonInventory(name, texture.sprite());
    }

    public T pressurePlate(String name, Identifier texture) {
        return singleTexture(name, "block/pressure_plate_up", "texture", texture);
    }

    public T pressurePlate(String name, Material texture) {
        return pressurePlate(name, texture.sprite());
    }

    public T pressurePlateDown(String name, Identifier texture) {
        return singleTexture(name, "block/pressure_plate_down", "texture", texture);
    }

    public T pressurePlateDown(String name, Material texture) {
        return pressurePlateDown(name, texture.sprite());
    }

    public T sign(String name, Identifier texture) {
        return getBuilder(name).texture("particle", texture);
    }

    public T sign(String name, Material texture) {
        return sign(name, texture.sprite());
    }

    public T fencePost(String name, Identifier texture) {
        return singleTexture(name, "block/fence_post", "texture", texture);
    }

    public T fenceSide(String name, Identifier texture) {
        return singleTexture(name, "block/fence_side", "texture", texture);
    }

    public T fenceInventory(String name, Identifier texture) {
        return singleTexture(name, "block/fence_inventory", "texture", texture);
    }

    public T fenceGate(String name, Identifier texture) {
        return singleTexture(name, "block/template_fence_gate", "texture", texture);
    }

    public T fenceGateOpen(String name, Identifier texture) {
        return singleTexture(name, "block/template_fence_gate_open", "texture", texture);
    }

    public T fenceGateWall(String name, Identifier texture) {
        return singleTexture(name, "block/template_fence_gate_wall", "texture", texture);
    }

    public T fenceGateWallOpen(String name, Identifier texture) {
        return singleTexture(name, "block/template_fence_gate_wall_open", "texture", texture);
    }

    public T doorBottomLeft(String name, Identifier bottom, Identifier top) {
        return door(name, "block/door_bottom_left", bottom, top);
    }

    public T doorBottomLeftOpen(String name, Identifier bottom, Identifier top) {
        return door(name, "block/door_bottom_left_open", bottom, top);
    }

    public T doorBottomRight(String name, Identifier bottom, Identifier top) {
        return door(name, "block/door_bottom_right", bottom, top);
    }

    public T doorBottomRightOpen(String name, Identifier bottom, Identifier top) {
        return door(name, "block/door_bottom_right_open", bottom, top);
    }

    public T doorTopLeft(String name, Identifier bottom, Identifier top) {
        return door(name, "block/door_top_left", bottom, top);
    }

    public T doorTopLeftOpen(String name, Identifier bottom, Identifier top) {
        return door(name, "block/door_top_left_open", bottom, top);
    }

    public T doorTopRight(String name, Identifier bottom, Identifier top) {
        return door(name, "block/door_top_right", bottom, top);
    }

    public T doorTopRightOpen(String name, Identifier bottom, Identifier top) {
        return door(name, "block/door_top_right_open", bottom, top);
    }

    private T door(String name, String parent, Identifier bottom, Identifier top) {
        return withExistingParent(name, parent).texture("bottom", bottom).texture("top", top);
    }

    public T trapdoorBottom(String name, Identifier texture) {
        return singleTexture(name, "block/template_trapdoor_bottom", "texture", texture);
    }

    public T trapdoorTop(String name, Identifier texture) {
        return singleTexture(name, "block/template_trapdoor_top", "texture", texture);
    }

    public T trapdoorOpen(String name, Identifier texture) {
        return singleTexture(name, "block/template_trapdoor_open", "texture", texture);
    }

    public T trapdoorOrientableBottom(String name, Identifier texture) {
        return singleTexture(name, "block/template_orientable_trapdoor_bottom", "texture", texture);
    }

    public T trapdoorOrientableTop(String name, Identifier texture) {
        return singleTexture(name, "block/template_orientable_trapdoor_top", "texture", texture);
    }

    public T trapdoorOrientableOpen(String name, Identifier texture) {
        return singleTexture(name, "block/template_orientable_trapdoor_open", "texture", texture);
    }

    public T leaves(String name, Identifier texture) {
        return singleTexture(name, "block/leaves", "all", texture);
    }

    public T leaves(String name, Material texture) {
        return leaves(name, texture.sprite());
    }

    public T nested() {
        return factory.apply(Identifier.fromNamespaceAndPath("dummy", "dummy"));
    }

    public ModelFile.ExistingModelFile getExistingFile(Identifier path) {
        ModelFile.ExistingModelFile file = new ModelFile.ExistingModelFile(extendWithFolder(path), existingFileHelper);
        file.assertExistence();
        return file;
    }

    public void clear() {
        generatedModels.clear();
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        clear();
        registerModels();
        return generateAll(cache);
    }

    protected CompletableFuture<?> generateAll(CachedOutput cache) {
        var pathProvider = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "models");
        return DataProvider.saveAll(cache, ModelBuilder::toJson, pathProvider::json, generatedModels);
    }
}

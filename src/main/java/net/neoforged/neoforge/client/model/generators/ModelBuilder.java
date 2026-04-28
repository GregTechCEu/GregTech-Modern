package net.neoforged.neoforge.client.model.generators;

import com.gregtechceu.gtceu.utils.data.ExistingFileHelper;

import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.neoforged.neoforge.common.util.TransformationHelper;

import com.google.common.base.Preconditions;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.math.Transformation;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public class ModelBuilder<T extends ModelBuilder<T>> extends ModelFile {

    @Nullable
    protected ModelFile parent;
    protected final Map<String, String> textures = new LinkedHashMap<>();
    protected final TransformsBuilder transforms = new TransformsBuilder();
    protected final ExistingFileHelper existingFileHelper;
    protected String renderType = null;
    protected boolean ambientOcclusion = true;
    protected UnbakedModel.GuiLight guiLight = null;
    protected final java.util.List<ElementBuilder> elements = new java.util.ArrayList<>();
    protected CustomLoaderBuilder<T> customLoader = null;
    private final RootTransformsBuilder rootTransforms = new RootTransformsBuilder();

    protected ModelBuilder(Identifier outputLocation, ExistingFileHelper existingFileHelper) {
        super(outputLocation);
        this.existingFileHelper = existingFileHelper;
    }

    @SuppressWarnings("unchecked")
    private T self() {
        return (T) this;
    }

    @Override
    protected boolean exists() {
        return true;
    }

    public T parent(ModelFile parent) {
        this.parent = Preconditions.checkNotNull(parent, "Parent must not be null");
        parent.assertExistence();
        return self();
    }

    public T texture(String key, String texture) {
        Preconditions.checkNotNull(key, "Key must not be null");
        Preconditions.checkNotNull(texture, "Texture must not be null");
        if (texture.charAt(0) == '#') {
            textures.put(key, texture);
        } else {
            texture(key, ModelFile.parse(texture.contains(":") ? texture : location.getNamespace() + ":" + texture));
        }
        return self();
    }

    public T texture(String key, Identifier texture) {
        Preconditions.checkNotNull(key, "Key must not be null");
        Preconditions.checkNotNull(texture, "Texture must not be null");
        Preconditions.checkArgument(existingFileHelper.exists(texture, ModelProvider.TEXTURE),
                "Texture %s does not exist in any known resource pack", texture);
        textures.put(key, texture.toString());
        return self();
    }

    public T texture(String key, Material texture) {
        return texture(key, texture.sprite());
    }

    public T renderType(String renderType) {
        Preconditions.checkNotNull(renderType, "Render type must not be null");
        this.renderType = ModelFile.parse(renderType).toString();
        return self();
    }

    public T renderType(Identifier renderType) {
        this.renderType = Preconditions.checkNotNull(renderType, "Render type must not be null").toString();
        return self();
    }

    public T ao(boolean ao) {
        this.ambientOcclusion = ao;
        return self();
    }

    public T guiLight(UnbakedModel.GuiLight light) {
        this.guiLight = light;
        return self();
    }

    public TransformsBuilder transforms() {
        return transforms;
    }

    public ElementBuilder element() {
        Preconditions.checkState(customLoader == null || customLoader.allowInlineElements,
                "Custom model loader %s does not support inline elements",
                customLoader != null ? customLoader.loaderId : null);
        ElementBuilder element = new ElementBuilder();
        elements.add(element);
        return element;
    }

    public ElementBuilder element(int index) {
        return elements.get(index);
    }

    public int getElementCount() {
        return elements.size();
    }

    public <L extends CustomLoaderBuilder<T>> L customLoader(BiFunction<T, ExistingFileHelper, L> customLoaderFactory) {
        L loader = customLoaderFactory.apply(self(), existingFileHelper);
        Preconditions.checkState(loader.allowInlineElements || elements.isEmpty(),
                "Custom model loader %s does not support inline elements", loader.loaderId);
        this.customLoader = loader;
        return loader;
    }

    public RootTransformsBuilder rootTransforms() {
        return rootTransforms;
    }

    @VisibleForTesting
    public JsonObject toJson() {
        JsonObject root = new JsonObject();
        if (parent != null) root.addProperty("parent", parent.getLocation().toString());
        if (!ambientOcclusion) root.addProperty("ambientocclusion", false);
        if (guiLight != null) root.addProperty("gui_light", guiLight.getSerializedName());
        if (renderType != null) root.addProperty("render_type", renderType);

        if (!transforms.transforms.isEmpty()) {
            JsonObject display = new JsonObject();
            transforms.transforms.forEach((context, builder) -> {
                JsonObject transform = builder.toJson();
                if (transform.size() > 0) display.add(context.getSerializedName(), transform);
            });
            if (display.size() > 0) root.add("display", display);
        }

        if (!textures.isEmpty()) {
            JsonObject json = new JsonObject();
            textures.forEach((key, texture) -> json.addProperty(key, serializeLocOrKey(texture)));
            root.add("textures", json);
        }

        if (!elements.isEmpty()) {
            JsonArray json = new JsonArray();
            elements.stream().map(ElementBuilder::toJson).forEach(json::add);
            root.add("elements", json);
        }

        JsonObject transform = rootTransforms.toJson();
        if (transform.size() > 0) root.add("transform", transform);

        return customLoader == null ? root : customLoader.toJson(root);
    }

    private String serializeLocOrKey(String texture) {
        return texture.charAt(0) == '#' ? texture : ModelFile.parse(texture).toString();
    }

    private static JsonArray serializeVector3f(Vector3f vec) {
        JsonArray array = new JsonArray();
        array.add(serializeFloat(vec.x()));
        array.add(serializeFloat(vec.y()));
        array.add(serializeFloat(vec.z()));
        return array;
    }

    private static Number serializeFloat(float value) {
        return (int) value == value ? (int) value : value;
    }

    public class ElementBuilder {

        private Vector3f from = new Vector3f();
        private Vector3f to = new Vector3f(16, 16, 16);
        private final Map<Direction, FaceBuilder> faces = new LinkedHashMap<>();
        @Nullable
        private RotationBuilder rotation;
        private boolean shade = true;

        public ElementBuilder from(float x, float y, float z) {
            this.from = validate(new Vector3f(x, y, z));
            return this;
        }

        public ElementBuilder to(float x, float y, float z) {
            this.to = validate(new Vector3f(x, y, z));
            return this;
        }

        private Vector3f validate(Vector3f value) {
            validateCoordinate(value.x(), 'x');
            validateCoordinate(value.y(), 'y');
            validateCoordinate(value.z(), 'z');
            return value;
        }

        private void validateCoordinate(float coord, char name) {
            Preconditions.checkArgument(coord >= -16.0F && coord <= 32.0F,
                    String.format("Position %s out of range, must be within [-16, 32]. Found: %s", name, coord));
        }

        public FaceBuilder face(Direction dir) {
            return faces.computeIfAbsent(Preconditions.checkNotNull(dir, "Direction must not be null"),
                    FaceBuilder::new);
        }

        public RotationBuilder rotation() {
            if (rotation == null) rotation = new RotationBuilder();
            return rotation;
        }

        public ElementBuilder shade(boolean shade) {
            this.shade = shade;
            return this;
        }

        public ElementBuilder allFaces(BiConsumer<Direction, FaceBuilder> action) {
            Arrays.stream(Direction.values()).forEach(direction -> action.accept(direction, face(direction)));
            return this;
        }

        public ElementBuilder allFacesExcept(BiConsumer<Direction, FaceBuilder> action, Set<Direction> excluded) {
            Arrays.stream(Direction.values())
                    .filter(direction -> !excluded.contains(direction))
                    .forEach(direction -> action.accept(direction, face(direction)));
            return this;
        }

        public ElementBuilder faces(BiConsumer<Direction, FaceBuilder> action) {
            faces.forEach(action);
            return this;
        }

        public ElementBuilder textureAll(String texture) {
            return allFaces((direction, face) -> face.texture(texture));
        }

        public ElementBuilder texture(String texture) {
            return faces((direction, face) -> face.texture(texture));
        }

        public ElementBuilder cube(String texture) {
            return allFaces((direction, face) -> face.texture(texture).cullface(direction));
        }

        public ElementBuilder emissivity(int blockLight, int skyLight) {
            return this;
        }

        public ElementBuilder color(int color) {
            return this;
        }

        public ElementBuilder ao(boolean ao) {
            return this;
        }

        public T end() {
            return self();
        }

        private JsonObject toJson() {
            JsonObject json = new JsonObject();
            json.add("from", serializeVector3f(from));
            json.add("to", serializeVector3f(to));
            if (rotation != null) json.add("rotation", rotation.toJson());
            if (!shade) json.addProperty("shade", false);
            if (!faces.isEmpty()) {
                JsonObject facesJson = new JsonObject();
                faces.forEach(
                        (direction, face) -> facesJson.add(direction.getSerializedName(), face.toJson(direction)));
                json.add("faces", facesJson);
            }
            return json;
        }

        public class FaceBuilder {

            @Nullable
            private Direction cullface;
            private int tintindex = -1;
            private String texture = "minecraft:missingno";
            @Nullable
            private float[] uvs;
            private FaceRotation rotation = FaceRotation.ZERO;

            FaceBuilder(Direction dir) {}

            public FaceBuilder cullface(@Nullable Direction dir) {
                this.cullface = dir;
                return this;
            }

            public FaceBuilder tintindex(int index) {
                this.tintindex = index;
                return this;
            }

            public FaceBuilder texture(String texture) {
                this.texture = Preconditions.checkNotNull(texture, "Texture must not be null");
                return this;
            }

            public FaceBuilder uvs(float u1, float v1, float u2, float v2) {
                this.uvs = new float[] { u1, v1, u2, v2 };
                return this;
            }

            public FaceBuilder rotation(FaceRotation rotation) {
                this.rotation = Preconditions.checkNotNull(rotation, "Rotation must not be null");
                return this;
            }

            public FaceBuilder emissivity(int blockLight, int skyLight) {
                return this;
            }

            public FaceBuilder color(int color) {
                return this;
            }

            public FaceBuilder ao(boolean ao) {
                return this;
            }

            public ElementBuilder end() {
                return ElementBuilder.this;
            }

            private JsonObject toJson(Direction direction) {
                JsonObject json = new JsonObject();
                json.addProperty("texture", serializeLocOrKey(texture));
                if (uvs != null) {
                    JsonArray uv = new JsonArray();
                    for (float value : uvs) uv.add(serializeFloat(value));
                    json.add("uv", uv);
                }
                if (cullface != null) json.addProperty("cullface", cullface.getSerializedName());
                if (rotation.rotation != 0) json.addProperty("rotation", rotation.rotation);
                if (tintindex != -1) json.addProperty("tintindex", tintindex);
                return json;
            }
        }

        public class RotationBuilder {

            private Vector3f origin = new Vector3f();
            private Direction.Axis axis = Direction.Axis.Y;
            private float angle;
            private boolean rescale;

            public RotationBuilder origin(float x, float y, float z) {
                this.origin = new Vector3f(x, y, z);
                return this;
            }

            public RotationBuilder axis(Direction.Axis axis) {
                this.axis = Preconditions.checkNotNull(axis, "Axis must not be null");
                return this;
            }

            public RotationBuilder angle(float angle) {
                Preconditions.checkArgument(angle == 0.0F || Mth.abs(angle) == 22.5F || Mth.abs(angle) == 45.0F,
                        "Invalid rotation %s found, only -45/-22.5/0/22.5/45 allowed", angle);
                this.angle = angle;
                return this;
            }

            public RotationBuilder rescale(boolean rescale) {
                this.rescale = rescale;
                return this;
            }

            public ElementBuilder end() {
                return ElementBuilder.this;
            }

            private JsonObject toJson() {
                JsonObject json = new JsonObject();
                json.add("origin", serializeVector3f(origin));
                json.addProperty("axis", axis.getSerializedName());
                json.addProperty("angle", angle);
                if (rescale) json.addProperty("rescale", true);
                return json;
            }
        }
    }

    public enum FaceRotation {

        ZERO(0),
        CLOCKWISE_90(90),
        UPSIDE_DOWN(180),
        COUNTERCLOCKWISE_90(270);

        final int rotation;

        FaceRotation(int rotation) {
            this.rotation = rotation;
        }
    }

    public class TransformsBuilder {

        private final Map<ItemDisplayContext, TransformVecBuilder> transforms = new LinkedHashMap<>();

        public TransformVecBuilder transform(ItemDisplayContext type) {
            return transforms.computeIfAbsent(Preconditions.checkNotNull(type, "Perspective cannot be null"),
                    TransformVecBuilder::new);
        }

        public T end() {
            return self();
        }

        public class TransformVecBuilder {

            private static final Vector3f DEFAULT_ROTATION = new Vector3f();
            private static final Vector3f DEFAULT_TRANSLATION = new Vector3f();
            private static final Vector3f DEFAULT_SCALE = new Vector3f(1, 1, 1);

            private Vector3f rotation = new Vector3f(DEFAULT_ROTATION);
            private Vector3f translation = new Vector3f(DEFAULT_TRANSLATION);
            private Vector3f scale = new Vector3f(DEFAULT_SCALE);
            private Vector3f rightRotation = new Vector3f(DEFAULT_ROTATION);

            TransformVecBuilder(ItemDisplayContext type) {}

            public TransformVecBuilder rotation(float x, float y, float z) {
                this.rotation = new Vector3f(x, y, z);
                return this;
            }

            public TransformVecBuilder leftRotation(float x, float y, float z) {
                return rotation(x, y, z);
            }

            public TransformVecBuilder translation(float x, float y, float z) {
                this.translation = new Vector3f(x, y, z);
                return this;
            }

            public TransformVecBuilder scale(float scale) {
                return scale(scale, scale, scale);
            }

            public TransformVecBuilder scale(float x, float y, float z) {
                this.scale = new Vector3f(x, y, z);
                return this;
            }

            public TransformVecBuilder rightRotation(float x, float y, float z) {
                this.rightRotation = new Vector3f(x, y, z);
                return this;
            }

            public TransformsBuilder end() {
                return TransformsBuilder.this;
            }

            private JsonObject toJson() {
                JsonObject json = new JsonObject();
                if (!translation.equals(DEFAULT_TRANSLATION)) json.add("translation", serializeVector3f(translation));
                if (!rotation.equals(DEFAULT_ROTATION)) {
                    json.add(rightRotation.equals(DEFAULT_ROTATION) ? "rotation" : "left_rotation",
                            serializeVector3f(rotation));
                }
                if (!scale.equals(DEFAULT_SCALE)) json.add("scale", serializeVector3f(scale));
                if (!rightRotation.equals(DEFAULT_ROTATION))
                    json.add("right_rotation", serializeVector3f(rightRotation));
                return json;
            }
        }
    }

    public class RootTransformsBuilder {

        private static final Vector3f ONE = new Vector3f(1, 1, 1);
        private Vector3f translation = new Vector3f();
        private Quaternionf leftRotation = new Quaternionf();
        private Quaternionf rightRotation = new Quaternionf();
        private Vector3f scale = new Vector3f(ONE);
        @Nullable
        private TransformationHelper.TransformOrigin origin;
        @Nullable
        private Vector3f originVec;

        public RootTransformsBuilder translation(Vector3f translation) {
            this.translation = Preconditions.checkNotNull(translation, "Translation must not be null");
            return this;
        }

        public RootTransformsBuilder translation(float x, float y, float z) {
            return translation(new Vector3f(x, y, z));
        }

        public RootTransformsBuilder rotation(Quaternionf rotation) {
            this.leftRotation = Preconditions.checkNotNull(rotation, "Rotation must not be null");
            return this;
        }

        public RootTransformsBuilder rotation(float x, float y, float z, boolean isDegrees) {
            return rotation(TransformationHelper.quatFromXYZ(x, y, z, isDegrees));
        }

        public RootTransformsBuilder leftRotation(Quaternionf leftRotation) {
            return rotation(leftRotation);
        }

        public RootTransformsBuilder leftRotation(float x, float y, float z, boolean isDegrees) {
            return leftRotation(TransformationHelper.quatFromXYZ(x, y, z, isDegrees));
        }

        public RootTransformsBuilder rightRotation(Quaternionf rightRotation) {
            this.rightRotation = Preconditions.checkNotNull(rightRotation, "Rotation must not be null");
            return this;
        }

        public RootTransformsBuilder rightRotation(float x, float y, float z, boolean isDegrees) {
            return rightRotation(TransformationHelper.quatFromXYZ(x, y, z, isDegrees));
        }

        public RootTransformsBuilder postRotation(Quaternionf postRotation) {
            return rightRotation(postRotation);
        }

        public RootTransformsBuilder postRotation(float x, float y, float z, boolean isDegrees) {
            return postRotation(TransformationHelper.quatFromXYZ(x, y, z, isDegrees));
        }

        public RootTransformsBuilder scale(float scale) {
            return scale(new Vector3f(scale, scale, scale));
        }

        public RootTransformsBuilder scale(float x, float y, float z) {
            return scale(new Vector3f(x, y, z));
        }

        public RootTransformsBuilder scale(Vector3f scale) {
            this.scale = Preconditions.checkNotNull(scale, "Scale must not be null");
            return this;
        }

        public RootTransformsBuilder transform(Transformation transformation) {
            Preconditions.checkNotNull(transformation, "Transformation must not be null");
            this.translation = new Vector3f(transformation.translation());
            this.leftRotation = new Quaternionf(transformation.leftRotation());
            this.rightRotation = new Quaternionf(transformation.rightRotation());
            this.scale = new Vector3f(transformation.scale());
            return this;
        }

        public RootTransformsBuilder origin(Vector3f origin) {
            this.originVec = Preconditions.checkNotNull(origin, "Origin must not be null");
            this.origin = null;
            return this;
        }

        public RootTransformsBuilder origin(TransformationHelper.TransformOrigin origin) {
            this.origin = Preconditions.checkNotNull(origin, "Origin must not be null");
            this.originVec = null;
            return this;
        }

        public ModelBuilder<T> end() {
            return ModelBuilder.this;
        }

        public JsonObject toJson() {
            JsonObject json = new JsonObject();
            if (!translation.equals(0, 0, 0)) json.add("translation", writeVec3(translation));
            if (!scale.equals(ONE)) json.add("scale", writeVec3(scale));
            if (!leftRotation.equals(0, 0, 0, 1)) json.add("rotation", writeQuaternion(leftRotation));
            if (!rightRotation.equals(0, 0, 0, 1)) json.add("post_rotation", writeQuaternion(rightRotation));
            if (origin != null) {
                json.addProperty("origin", origin.getSerializedName());
            } else if (originVec != null && !originVec.equals(0, 0, 0)) {
                json.add("origin", writeVec3(originVec));
            }
            return json;
        }

        private static JsonArray writeVec3(Vector3f vector) {
            JsonArray array = new JsonArray();
            array.add(vector.x());
            array.add(vector.y());
            array.add(vector.z());
            return array;
        }

        private static JsonArray writeQuaternion(Quaternionf quaternion) {
            JsonArray array = new JsonArray();
            array.add(quaternion.x());
            array.add(quaternion.y());
            array.add(quaternion.z());
            array.add(quaternion.w());
            return array;
        }
    }
}

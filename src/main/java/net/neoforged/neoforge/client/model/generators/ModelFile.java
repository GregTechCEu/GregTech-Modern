package net.neoforged.neoforge.client.model.generators;

import com.gregtechceu.gtceu.utils.data.ExistingFileHelper;

import net.minecraft.resources.Identifier;

import com.google.common.base.Preconditions;

public abstract class ModelFile {

    protected final Identifier location;

    protected ModelFile(Identifier location) {
        this.location = location;
    }

    protected abstract boolean exists();

    public Identifier getLocation() {
        assertExistence();
        return location;
    }

    public Identifier getUncheckedLocation() {
        return location;
    }

    public void assertExistence() {
        Preconditions.checkState(exists(), "Model at %s does not exist", location);
    }

    public static class UncheckedModelFile extends ModelFile {

        public UncheckedModelFile(String location) {
            this(parse(location));
        }

        public UncheckedModelFile(Identifier location) {
            super(location);
        }

        @Override
        protected boolean exists() {
            return true;
        }
    }

    public static class ExistingModelFile extends ModelFile {

        private final ExistingFileHelper existingHelper;

        public ExistingModelFile(Identifier location, ExistingFileHelper existingHelper) {
            super(location);
            this.existingHelper = existingHelper;
        }

        @Override
        protected boolean exists() {
            if (location.getPath().contains(".")) {
                return existingHelper.exists(location, ModelProvider.MODEL_WITH_EXTENSION);
            }
            return existingHelper.exists(location, ModelProvider.MODEL);
        }
    }

    static Identifier parse(String location) {
        return location.contains(":") ? Identifier.parse(location) : Identifier.withDefaultNamespace(location);
    }
}

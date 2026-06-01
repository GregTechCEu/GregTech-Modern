package com.gregtechceu.gtceu.core.config;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class Option {

    @Getter
    private final String name;
    @Getter
    private final boolean defaultEnabled;
    @Getter
    private @Nullable List<String> comment = null;

    private @Nullable Set<String> modDefined = null;
    private boolean enabled;
    @Getter
    private boolean userDefined;
    @Getter
    @Setter
    private @Nullable Option parent = null;
    @Getter
    @Setter
    private boolean hidden;
    @Setter
    private boolean parentValueInverted;

    public Option(String name, boolean enabled, boolean userDefined) {
        this.name = name;
        this.defaultEnabled = enabled;

        this.enabled = enabled;
        this.userDefined = userDefined;
    }

    public void setEnabled(boolean enabled, boolean userDefined) {
        if (this.enabled == enabled) return;

        this.enabled = enabled;
        this.userDefined = userDefined;
    }

    public void addModOverride(boolean enabled, String modId) {
        if (this.enabled == enabled) return;

        this.enabled = enabled;

        if (this.modDefined == null) {
            this.modDefined = new LinkedHashSet<>();
        }

        this.modDefined.add(modId);
    }

    public void addComment(String... comment) {
        if (this.comment == null) {
            this.comment = new ArrayList<>();
        }
        Collections.addAll(this.comment, comment);
    }

    public int getDepth() {
        if (this.parent == null) return 0;
        else return this.parent.getDepth() + 1;
    }

    public boolean isEnabled() {
        if (this.hidden && this.parent != null) {
            return this.parent.isEnabled() != parentValueInverted;
        }
        return this.enabled;
    }

    /**
     * Checks if this option will effectively be disabled (regardless of its own status)
     * by the parent rule being disabled.
     */
    public boolean isEffectivelyDisabledByParent() {
        return this.parent != null &&
                (!this.parent.enabled || this.parent.isEffectivelyDisabledByParent()) != parentValueInverted;
    }

    public boolean isOverridden() {
        return this.isUserDefined() || this.isModDefined() || (this.hidden && this.parent != null);
    }

    public boolean isModDefined() {
        return this.modDefined != null;
    }

    public String getSelfName() {
        if (this.parent == null) return this.name;
        else return this.name.substring(this.parent.getName().length() + 1);
    }

    public void clearModsDefiningValue() {
        this.modDefined = null;
    }

    public void clearUserDefined() {
        this.userDefined = false;
    }

    public Collection<String> getDefiningMods() {
        return this.modDefined != null ? Collections.unmodifiableCollection(this.modDefined) : Collections.emptyList();
    }
}

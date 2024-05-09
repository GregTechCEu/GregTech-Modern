package com.gregtechceu.gtceu.common.blockentity;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.*;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.UUID;
import java.util.function.Function;

public class GTSignBlockEntity extends BlockEntity {
    public static final int LINES = 4;
    private static final String[] RAW_TEXT_FIELD_NAMES = new String[]{"Text1", "Text2", "Text3", "Text4"};
    private static final String[] FILTERED_TEXT_FIELD_NAMES = new String[]{"FilteredText1", "FilteredText2", "FilteredText3", "FilteredText4"};
    private final Component[] messages;
    private final Component[] filteredMessages;
    private boolean isEditable;
    @Nullable
    private UUID playerWhoMayEdit;
    @Nullable
    private FormattedCharSequence[] renderMessages;
    private boolean renderMessagedFiltered;
    private DyeColor color;
    private boolean hasGlowingText;

    public GTSignBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
        this.messages = new Component[]{CommonComponents.EMPTY, CommonComponents.EMPTY, CommonComponents.EMPTY, CommonComponents.EMPTY};
        this.filteredMessages = new Component[]{CommonComponents.EMPTY, CommonComponents.EMPTY, CommonComponents.EMPTY, CommonComponents.EMPTY};
        this.isEditable = true;
        this.color = DyeColor.BLACK;
    }

    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);

        for(int i = 0; i < 4; ++i) {
            Component component = this.messages[i];
            String string = Component.Serializer.toJson(component);
            tag.putString(RAW_TEXT_FIELD_NAMES[i], string);
            Component component2 = this.filteredMessages[i];
            if (!component2.equals(component)) {
                tag.putString(FILTERED_TEXT_FIELD_NAMES[i], Component.Serializer.toJson(component2));
            }
        }

        tag.putString("Color", this.color.getName());
        tag.putBoolean("GlowingText", this.hasGlowingText);
    }

    public void load(CompoundTag tag) {
        this.isEditable = false;
        super.load(tag);
        this.color = DyeColor.byName(tag.getString("Color"), DyeColor.BLACK);

        for(int i = 0; i < 4; ++i) {
            String string = tag.getString(RAW_TEXT_FIELD_NAMES[i]);
            Component component = this.loadLine(string);
            this.messages[i] = component;
            String string2 = FILTERED_TEXT_FIELD_NAMES[i];
            if (tag.contains(string2, 8)) {
                this.filteredMessages[i] = this.loadLine(tag.getString(string2));
            } else {
                this.filteredMessages[i] = component;
            }
        }

        this.renderMessages = null;
        this.hasGlowingText = tag.getBoolean("GlowingText");
    }

    private Component loadLine(String line) {
        Component component = this.deserializeTextSafe(line);
        if (this.level instanceof ServerLevel) {
            try {
                return ComponentUtils.updateForEntity(this.createCommandSourceStack((ServerPlayer)null), component, (Entity)null, 0);
            } catch (CommandSyntaxException var4) {
            }
        }

        return component;
    }

    private Component deserializeTextSafe(String text) {
        try {
            Component component = Component.Serializer.fromJson(text);
            if (component != null) {
                return component;
            }
        } catch (Exception var3) {
        }

        return CommonComponents.EMPTY;
    }

    public Component getMessage(int line, boolean filtered) {
        return this.getMessages(filtered)[line];
    }

    public void setMessage(int line, Component message) {
        this.setMessage(line, message, message);
    }

    public void setMessage(int line, Component message, Component filteredMessage) {
        this.messages[line] = message;
        this.filteredMessages[line] = filteredMessage;
        this.renderMessages = null;
    }

    public FormattedCharSequence[] getRenderMessages(boolean filtered, Function<Component, FormattedCharSequence> messageTransformer) {
        if (this.renderMessages == null || this.renderMessagedFiltered != filtered) {
            this.renderMessagedFiltered = filtered;
            this.renderMessages = new FormattedCharSequence[4];

            for(int i = 0; i < 4; ++i) {
                this.renderMessages[i] = (FormattedCharSequence)messageTransformer.apply(this.getMessage(i, filtered));
            }
        }

        return this.renderMessages;
    }

    private Component[] getMessages(boolean filtered) {
        return filtered ? this.filteredMessages : this.messages;
    }

    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }

    public boolean onlyOpCanSetNbt() {
        return true;
    }

    public boolean isEditable() {
        return this.isEditable;
    }

    public void setEditable(boolean isEditable) {
        this.isEditable = isEditable;
        if (!isEditable) {
            this.playerWhoMayEdit = null;
        }

    }

    public void setAllowedPlayerEditor(UUID playWhoMayEdit) {
        this.playerWhoMayEdit = playWhoMayEdit;
    }

    @Nullable
    public UUID getPlayerWhoMayEdit() {
        return this.playerWhoMayEdit;
    }

    public boolean executeClickCommands(ServerPlayer level) {
        Component[] var2 = this.getMessages(level.isTextFilteringEnabled());
        int var3 = var2.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            Component component = var2[var4];
            Style style = component.getStyle();
            ClickEvent clickEvent = style.getClickEvent();
            if (clickEvent != null && clickEvent.getAction() == ClickEvent.Action.RUN_COMMAND) {
                level.getServer().getCommands().performPrefixedCommand(this.createCommandSourceStack(level), clickEvent.getValue());
            }
        }

        return true;
    }

    public CommandSourceStack createCommandSourceStack(@Nullable ServerPlayer player) {
        String string = player == null ? "Sign" : player.getName().getString();
        Component component = player == null ? Component.literal("Sign") : player.getDisplayName();
        return new CommandSourceStack(CommandSource.NULL, Vec3.atCenterOf(this.worldPosition), Vec2.ZERO, (ServerLevel)this.level, 2, string, (Component)component, this.level.getServer(), player);
    }

    public DyeColor getColor() {
        return this.color;
    }

    public boolean setColor(DyeColor color) {
        if (color != this.getColor()) {
            this.color = color;
            this.markUpdated();
            return true;
        } else {
            return false;
        }
    }

    public boolean hasGlowingText() {
        return this.hasGlowingText;
    }

    public boolean setHasGlowingText(boolean hasGlowingText) {
        if (this.hasGlowingText != hasGlowingText) {
            this.hasGlowingText = hasGlowingText;
            this.markUpdated();
            return true;
        } else {
            return false;
        }
    }

    private void markUpdated() {
        this.setChanged();
        this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
    }
}

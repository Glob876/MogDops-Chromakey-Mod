package me.mogdop;

import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachmentBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;

public class ChromakeyBlockEntity extends BlockEntity implements RenderAttachmentBlockEntity, net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable {
    private int customColor = -1;

    public ChromakeyBlockEntity() {
        super(ChromakeyMod.CHROMAKEY_BLOCK_ENTITY);
    }

    public int getCustomColor() { return customColor; }

    @Override
    public Object getRenderAttachmentData() { return customColor; }

    public void setCustomColor(int color) {
        customColor = color & 0xFFFFFF;
        markDirty();
        if (world != null) {
            world.updateListeners(pos, getCachedState(), getCachedState(), 3);
            if (!world.isClient) sync();
        }
    }

    @Override
    public NbtCompound toClientTag(NbtCompound tag) { return writeNbt(tag); }

    @Override
    public void fromClientTag(NbtCompound tag) { fromTag(getCachedState(), tag); }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.putInt("custom_color", customColor);
        return nbt;
    }

    @Override
    public void fromTag(BlockState state, NbtCompound nbt) {
        int oldColor = customColor;
        super.fromTag(state, nbt);
        customColor = nbt.contains("custom_color") ? nbt.getInt("custom_color") : -1;
        if (world != null && world.isClient && oldColor != customColor)
            world.updateListeners(pos, getCachedState(), getCachedState(), 3);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() { return writeNbt(new NbtCompound()); }
}

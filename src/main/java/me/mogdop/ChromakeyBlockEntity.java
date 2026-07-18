package me.mogdop;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;

public class ChromakeyBlockEntity extends BlockEntity {
    private int customColor = -1;

    public ChromakeyBlockEntity(BlockPos pos, BlockState state) {
        super(ChromakeyMod.CHROMAKEY_BLOCK_ENTITY, pos, state);
    }

    public int getCustomColor() {
        return this.customColor;
    }

    public void setCustomColor(int color) {
        this.customColor = color;
        this.markDirty();
        if (world != null) {
            world.updateListeners(pos, getCachedState(), getCachedState(), 3);
        }
    }

    @Override
    public void writeNbt(NbtCompound nbt) { // Откат: убран WrapperLookup в 1.20.1
        super.writeNbt(nbt);
        nbt.putInt("custom_color", this.customColor);
    }

    @Override
    public void readNbt(NbtCompound nbt) { // Откат: убран WrapperLookup в 1.20.1
        int oldColor = this.customColor;
        super.readNbt(nbt);
        this.customColor = nbt.contains("custom_color") ? nbt.getInt("custom_color") : -1;
        
        // Исправление мгновенного применения
        if (this.world != null && this.world.isClient && this.customColor != oldColor) {
            this.world.updateListeners(this.pos, this.getCachedState(), this.getCachedState(), 3);
        }
    }

    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() { // Откат: убран WrapperLookup в 1.20.1
        NbtCompound nbt = new NbtCompound();
        writeNbt(nbt);
        return nbt;
    }
}
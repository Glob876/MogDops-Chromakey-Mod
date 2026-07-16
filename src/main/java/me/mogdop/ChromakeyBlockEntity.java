package me.mogdop;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;

public class ChromakeyBlockEntity extends BlockEntity {
    private int customColor = -1; // -1 означает использование стандартного цвета из Enum

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
    protected void writeData(WriteView view) {
        super.writeData(view);
        view.putInt("custom_color", this.customColor);
    }

    @Override
    protected void readData(ReadView view) {
        super.readData(view);
        // Передаем дефолтное значение (-1) вторым аргументом
        this.customColor = view.getInt("custom_color", -1);
    }

    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
        return createNbt(registryLookup);
    }
}
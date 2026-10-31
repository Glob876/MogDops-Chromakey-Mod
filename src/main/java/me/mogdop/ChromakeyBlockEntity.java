package me.mogdop;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;

public class ChromakeyBlockEntity extends BlockEntity {
    @Override
    public Object getRenderData() { return customColor; }
    private int customColor = -1;

    public ChromakeyBlockEntity(BlockPos pos, BlockState state) {
        super(ChromakeyMod.CHROMAKEY_BLOCK_ENTITY, pos, state);
    }

    public int getCustomColor() {
        return this.customColor;
    }

    public void setCustomColor(int color) {
        this.customColor = color & 0xFFFFFF;
        this.markDirty();
        if (world != null) {
            world.updateListeners(pos, getCachedState(), getCachedState(), 3);
        }
    }

    @Override
    protected void writeNbt(NbtCompound view, RegistryWrapper.WrapperLookup registries) {
        super.writeNbt(view, registries);
        view.putInt("custom_color", this.customColor);
    }

    @Override
    protected void readNbt(NbtCompound view, RegistryWrapper.WrapperLookup registries) {
        int oldColor = this.customColor;
        super.readNbt(view, registries);
        this.customColor = view.contains("custom_color") ? view.getInt("custom_color") : -1;
        
        // Исправление мгновенного применения: Форсируем перерисовку чанка при изменении цвета
        if (this.world != null && this.world.isClient() && this.customColor != oldColor) {
            this.world.updateListeners(this.pos, this.getCachedState(), this.getCachedState(), 3);
        }
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
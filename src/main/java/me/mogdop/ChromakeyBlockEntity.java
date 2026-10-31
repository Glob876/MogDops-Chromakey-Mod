package me.mogdop;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.BlockPos;

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
        this.setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(net.minecraft.world.level.storage.ValueOutput view) {
        super.saveAdditional(view);
        view.putInt("custom_color", this.customColor);
    }

    @Override
    protected void loadAdditional(net.minecraft.world.level.storage.ValueInput view) {
        int oldColor = this.customColor;
        super.loadAdditional(view);
        this.customColor = view.getIntOr("custom_color", -1);
        
        // Исправление мгновенного применения: Форсируем перерисовку чанка при изменении цвета
        if (this.level != null && this.level.isClientSide() && this.customColor != oldColor) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registryLookup) {
        return saveWithoutMetadata(registryLookup);
    }
}
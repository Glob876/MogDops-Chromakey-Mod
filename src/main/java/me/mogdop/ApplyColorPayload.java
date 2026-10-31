package me.mogdop;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.core.BlockPos;

public record ApplyColorPayload(BlockPos pos, int rgb) implements CustomPacketPayload {
    public static final Type<ApplyColorPayload> ID = new Type<>(Identifier.fromNamespaceAndPath(ChromakeyMod.MOD_ID, "apply_color"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ApplyColorPayload> CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC, ApplyColorPayload::pos,
        ByteBufCodecs.INT, ApplyColorPayload::rgb,
        ApplyColorPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
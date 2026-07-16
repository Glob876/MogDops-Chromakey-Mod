package me.mogdop;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public record ApplyColorPayload(BlockPos pos, int rgb) implements CustomPayload {
    public static final Id<ApplyColorPayload> ID = new Id<>(Identifier.of(ChromakeyMod.MOD_ID, "apply_color"));
    public static final PacketCodec<RegistryByteBuf, ApplyColorPayload> CODEC = PacketCodec.tuple(
        BlockPos.PACKET_CODEC, ApplyColorPayload::pos,
        PacketCodecs.INTEGER, ApplyColorPayload::rgb,
        ApplyColorPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
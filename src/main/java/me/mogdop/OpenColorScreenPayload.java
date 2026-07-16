package me.mogdop;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public record OpenColorScreenPayload(BlockPos pos) implements CustomPayload {
    public static final Id<OpenColorScreenPayload> ID = new Id<>(Identifier.of(ChromakeyMod.MOD_ID, "open_color_screen"));
    public static final PacketCodec<RegistryByteBuf, OpenColorScreenPayload> CODEC = PacketCodec.tuple(
        BlockPos.PACKET_CODEC, OpenColorScreenPayload::pos,
        OpenColorScreenPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
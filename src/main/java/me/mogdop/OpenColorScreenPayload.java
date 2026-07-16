package me.mogdop;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record OpenColorScreenPayload() implements CustomPayload {
    public static final Id<OpenColorScreenPayload> ID = new Id<>(Identifier.of(ChromakeyMod.MOD_ID, "open_color_screen"));
    public static final PacketCodec<RegistryByteBuf, OpenColorScreenPayload> CODEC = PacketCodec.unit(new OpenColorScreenPayload());

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
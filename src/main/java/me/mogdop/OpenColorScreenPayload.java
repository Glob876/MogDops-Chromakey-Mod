package me.mogdop;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.core.BlockPos;

public record OpenColorScreenPayload(BlockPos pos) implements CustomPacketPayload {
    public static final Type<OpenColorScreenPayload> ID = new Type<>(Identifier.fromNamespaceAndPath(ChromakeyMod.MOD_ID, "open_color_screen"));
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenColorScreenPayload> CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC, OpenColorScreenPayload::pos,
        OpenColorScreenPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
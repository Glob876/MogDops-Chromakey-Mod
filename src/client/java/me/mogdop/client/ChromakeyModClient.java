package me.mogdop.client;

import me.mogdop.ChromakeyMod;
import me.mogdop.ChromakeyBlock;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachedBlockView;
import net.minecraft.util.math.BlockPos;

public class ChromakeyModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(ChromakeyMod.OPEN_SCREEN, (client, handler, buf, sender) -> {
            BlockPos pos = buf.readBlockPos();
            client.execute(() -> client.openScreen(new ChromakeyColorScreen(pos)));
        });
        ColorProviderRegistry.BLOCK.register((state, world, pos, tint) -> {
            if (tint != 0 || !state.get(ChromakeyBlock.LIT)) return -1;
            if (world instanceof RenderAttachedBlockView && pos != null) {
                Object color = ((RenderAttachedBlockView) world).getBlockEntityRenderAttachment(pos);
                if (color instanceof Integer && (Integer) color != -1) return (Integer) color;
            }
            return state.get(ChromakeyBlock.COLOR).getColorHex();
        }, ChromakeyMod.GREEN_CHROMAKEY_BLOCK);
    }
}

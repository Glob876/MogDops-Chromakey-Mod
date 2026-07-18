package me.mogdop.client;

import me.mogdop.ChromakeyBlock;
import me.mogdop.ChromakeyMod;
import me.mogdop.ChromakeyBlockEntity;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;

public class ChromakeyModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Классический приемник пакета в 1.20.1
        ClientPlayNetworking.registerGlobalReceiver(ChromakeyMod.OPEN_COLOR_SCREEN_ID, (client, handler, buf, responseSender) -> {
            BlockPos pos = buf.readBlockPos();
            client.execute(() -> {
                client.setScreen(new ChromakeyColorScreen(pos));
            });
        });

        ColorProviderRegistry.BLOCK.register((state, world, pos, tintIndex) -> {
            if (tintIndex == 0) {
                if (state != null && !state.get(ChromakeyBlock.LIT)) {
                    return -1;
                }
                if (world != null && pos != null) {
                    BlockEntity be = world.getBlockEntity(pos);
                    if (be instanceof ChromakeyBlockEntity chromakeyBe) {
                        int customColor = chromakeyBe.getCustomColor();
                        if (customColor != -1) {
                            return customColor;
                        }
                    }
                }
                return state.get(ChromakeyBlock.COLOR).getColorHex();
            }
            return -1;
        }, ChromakeyMod.GREEN_CHROMAKEY_BLOCK);
    }
}
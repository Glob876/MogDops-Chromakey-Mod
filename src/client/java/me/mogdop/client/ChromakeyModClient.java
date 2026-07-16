package me.mogdop.client;

import me.mogdop.ChromakeyBlock;
import me.mogdop.ChromakeyColor;
import me.mogdop.ChromakeyMod;
import me.mogdop.ChromakeyBlockEntity;
import me.mogdop.OpenColorScreenPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.block.entity.BlockEntity;

public class ChromakeyModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Регистрация приемника пакета открытия экрана
        ClientPlayNetworking.registerGlobalReceiver(OpenColorScreenPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                context.client().setScreen(new ChromakeyColorScreen());
            });
        });

        // Регистрируем окрашивание блока
        ColorProviderRegistry.BLOCK.register((state, world, pos, tintIndex) -> {
            if (tintIndex == 0) {
                if (state != null && !state.get(ChromakeyBlock.LIT)) {
                    return -1;
                }
                
                // Если у блока есть кастомный цвет в BlockEntity, используем его!
                if (world != null && pos != null) {
                    BlockEntity be = world.getBlockEntity(pos);
                    if (be instanceof ChromakeyBlockEntity chromakeyBe) {
                        int customColor = chromakeyBe.getCustomColor();
                        if (customColor != -1) {
                            return customColor;
                        }
                    }
                }
                
                ChromakeyColor color = state.get(ChromakeyBlock.COLOR);
                return color.getColorHex();
            }
            return -1;
        }, ChromakeyMod.GREEN_CHROMAKEY_BLOCK);
    }
}
package me.mogdop;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ChromakeyControllerItem extends Item {
    public ChromakeyControllerItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        BlockState state = world.getBlockState(pos);
        PlayerEntity player = context.getPlayer();

        if (state.getBlock() instanceof ChromakeyBlock chromakeyBlock) {
            if (!world.isClient()) {
                ChromakeyColor currentColor = state.get(ChromakeyBlock.COLOR);
                ChromakeyColor nextColor;

                // Проверяем, зажат ли у игрока SHIFT (Sneaking)
                if (player != null && player.isSneaking()) {
                    // Переключаем только между Red, Green, White
                    nextColor = switch (currentColor) {
                        case RED -> ChromakeyColor.GREEN;
                        case GREEN -> ChromakeyColor.WHITE;
                        default -> ChromakeyColor.RED; // Сбрасывает любые другие цвета на RED
                    };
                } else {
                    // Обычное нажатие ПКМ: перебираем все доступные цвета по полному кругу
                    ChromakeyColor[] colors = ChromakeyColor.values();
                    int nextIndex = (currentColor.ordinal() + 1) % colors.length;
                    ChromakeyColor nextColorResult = colors[nextIndex];
                    nextColor = nextColorResult;
                }

                BlockState newState = state.with(ChromakeyBlock.COLOR, nextColor);
                world.setBlockState(pos, newState);
                chromakeyBlock.propagateState(world, pos, newState); // Перекрашиваем всю стену!

                // Отправляем сообщение только если в конфиге включен параметр showColorChangeMessage
                if (player != null && ChromakeyConfig.showColorChangeMessage) {
                    player.sendMessage(Text.translatable("message.mogdops-chromakey-mod.color_changed", 
                        Text.translatable("color.mogdops-chromakey-mod." + nextColor.asString())
                    ).formatted(Formatting.GREEN), true);
                }
            }
            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS;
    }
}
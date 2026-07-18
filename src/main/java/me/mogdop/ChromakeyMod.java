package me.mogdop;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChromakeyMod implements ModInitializer {
    public static final String MOD_ID = "mogdops-chromakey-mod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    // Классические идентификаторы пакетов (new Identifier вместо Identifier.of в 1.20.1)
    public static final Identifier OPEN_COLOR_SCREEN_ID = new Identifier(MOD_ID, "open_color_screen");
    public static final Identifier APPLY_COLOR_ID = new Identifier(MOD_ID, "apply_color");

    public static final RegistryKey<Block> GREEN_CHROMAKEY_BLOCK_KEY = RegistryKey.of(
        RegistryKeys.BLOCK,
        new Identifier(MOD_ID, "green_chromakey_block")
    );

    public static final Block GREEN_CHROMAKEY_BLOCK = Registry.register(
        Registries.BLOCK,
        GREEN_CHROMAKEY_BLOCK_KEY.getValue(),
        new ChromakeyBlock(AbstractBlock.Settings.create()
            .strength(1.5f)
            .sounds(BlockSoundGroup.STONE)
            .luminance(state -> state.get(ChromakeyBlock.LIT) ? 15 : 0)
        )
    );

    public static final BlockEntityType<ChromakeyBlockEntity> CHROMAKEY_BLOCK_ENTITY = Registry.register(
        Registries.BLOCK_ENTITY_TYPE,
        new Identifier(MOD_ID, "chromakey_block_entity"),
        FabricBlockEntityTypeBuilder.create(ChromakeyBlockEntity::new, GREEN_CHROMAKEY_BLOCK).build()
    );

    public static final RegistryKey<Item> GREEN_CHROMAKEY_BLOCK_ITEM_KEY = RegistryKey.of(
        RegistryKeys.ITEM,
        new Identifier(MOD_ID, "green_chromakey_block")
    );

    public static final Item GREEN_CHROMAKEY_BLOCK_ITEM = Registry.register(
        Registries.ITEM,
        GREEN_CHROMAKEY_BLOCK_ITEM_KEY.getValue(),
        new BlockItem(
            GREEN_CHROMAKEY_BLOCK,
            new Item.Settings()
        )
    );

    public static final RegistryKey<Item> CHROMAKEY_CONTROLLER_KEY = RegistryKey.of(
        RegistryKeys.ITEM,
        new Identifier(MOD_ID, "chromakey_controller")
    );

    public static final Item CHROMAKEY_CONTROLLER = Registry.register(
        Registries.ITEM,
        CHROMAKEY_CONTROLLER_KEY.getValue(),
        new ChromakeyControllerItem(
            new Item.Settings().maxCount(1)
        )
    );

    public static final RegistryKey<Item> CHROMAKEY_PROCESSOR_KEY = RegistryKey.of(
        RegistryKeys.ITEM,
        new Identifier(MOD_ID, "chromakey_processor")
    );

    public static final Item CHROMAKEY_PROCESSOR = Registry.register(
        Registries.ITEM,
        CHROMAKEY_PROCESSOR_KEY.getValue(),
        new Item(new Item.Settings())
    );

    public static final RegistryKey<Item> CHROMAKEY_BLANK_KEY = RegistryKey.of(
        RegistryKeys.ITEM,
        new Identifier(MOD_ID, "chromakey_blank")
    );

    public static final Item CHROMAKEY_BLANK = Registry.register(
        Registries.ITEM,
        CHROMAKEY_BLANK_KEY.getValue(),
        new Item(new Item.Settings())
    );

    public static final RegistryKey<ItemGroup> CHROMAKEY_ITEM_GROUP_KEY = RegistryKey.of(
        RegistryKeys.ITEM_GROUP,
        new Identifier(MOD_ID, "chromakey_item_group")
    );

    public static final ItemGroup CHROMAKEY_ITEM_GROUP = Registry.register(
        Registries.ITEM_GROUP,
        CHROMAKEY_ITEM_GROUP_KEY.getValue(),
        FabricItemGroup.builder()
            .icon(() -> new ItemStack(GREEN_CHROMAKEY_BLOCK_ITEM))
            .displayName(Text.translatable("itemGroup.mogdops-chromakey-mod.chromakey_item_group"))
            .entries((context, entries) -> {
                entries.add(GREEN_CHROMAKEY_BLOCK_ITEM);
                entries.add(CHROMAKEY_CONTROLLER);
                entries.add(CHROMAKEY_PROCESSOR);
                entries.add(CHROMAKEY_BLANK);
            })
            .build()
    );

    @Override
    public void onInitialize() {
        LOGGER.info("Инициализация MogDops Chromakey Mod...");
        ChromakeyConfig.load();

        // Классический приемник и обработчик пакета применения цвета на сервере в 1.20.1
        ServerPlayNetworking.registerGlobalReceiver(APPLY_COLOR_ID, (server, player, handler, buf, responseSender) -> {
            BlockPos pos = buf.readBlockPos();
            int rgb = buf.readInt();
            server.execute(() -> {
                World world = player.getWorld();
                BlockState state = world.getBlockState(pos);
                if (state.getBlock() instanceof ChromakeyBlock chromakeyBlock) {
                    chromakeyBlock.propagateState(world, pos, null, rgb);
                    player.sendMessage(Text.literal("Applied color: " + String.format("#%06X", rgb)).formatted(Formatting.GREEN), true);
                }
            });
        });
    }
}
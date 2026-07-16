package me.mogdop;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChromakeyMod implements ModInitializer {
    public static final String MOD_ID = "mogdops-chromakey-mod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    // Ключ и регистрация Блока Хромакея
    public static final RegistryKey<Block> GREEN_CHROMAKEY_BLOCK_KEY = RegistryKey.of(
        RegistryKeys.BLOCK,
        Identifier.of(MOD_ID, "green_chromakey_block")
    );

    public static final Block GREEN_CHROMAKEY_BLOCK = Registry.register(
        Registries.BLOCK,
        GREEN_CHROMAKEY_BLOCK_KEY,
        new ChromakeyBlock(AbstractBlock.Settings.create()
            .registryKey(GREEN_CHROMAKEY_BLOCK_KEY)
            .strength(1.5f)
            .sounds(BlockSoundGroup.STONE)
            .luminance(state -> state.get(ChromakeyBlock.LIT) ? 15 : 0)
        )
    );

    // Регистрация Block Entity
    public static final BlockEntityType<ChromakeyBlockEntity> CHROMAKEY_BLOCK_ENTITY = Registry.register(
        Registries.BLOCK_ENTITY_TYPE,
        Identifier.of(MOD_ID, "chromakey_block_entity"),
        BlockEntityType.Builder.create(ChromakeyBlockEntity::new, GREEN_CHROMAKEY_BLOCK).build(null)
    );

    // Ключ и регистрация Предмета блока
    public static final RegistryKey<Item> GREEN_CHROMAKEY_BLOCK_ITEM_KEY = RegistryKey.of(
        RegistryKeys.ITEM,
        Identifier.of(MOD_ID, "green_chromakey_block")
    );

    public static final Item GREEN_CHROMAKEY_BLOCK_ITEM = Registry.register(
        Registries.ITEM,
        GREEN_CHROMAKEY_BLOCK_ITEM_KEY,
        new BlockItem(
            GREEN_CHROMAKEY_BLOCK,
            new Item.Settings()
                .registryKey(GREEN_CHROMAKEY_BLOCK_ITEM_KEY)
                .useBlockPrefixedTranslationKey()
        )
    );

    // Ключ и регистрация Предмета Контроллера
    public static final RegistryKey<Item> CHROMAKEY_CONTROLLER_KEY = RegistryKey.of(
        RegistryKeys.ITEM,
        Identifier.of(MOD_ID, "chromakey_controller")
    );

    public static final Item CHROMAKEY_CONTROLLER = Registry.register(
        Registries.ITEM,
        CHROMAKEY_CONTROLLER_KEY,
        new ChromakeyControllerItem(
            new Item.Settings()
                .registryKey(CHROMAKEY_CONTROLLER_KEY)
                .maxCount(1)
        )
    );

    // Процессор и заготовка
    public static final RegistryKey<Item> CHROMAKEY_PROCESSOR_KEY = RegistryKey.of(
        RegistryKeys.ITEM,
        Identifier.of(MOD_ID, "chromakey_processor")
    );

    public static final Item CHROMAKEY_PROCESSOR = Registry.register(
        Registries.ITEM,
        CHROMAKEY_PROCESSOR_KEY,
        new Item(new Item.Settings().registryKey(CHROMAKEY_PROCESSOR_KEY))
    );

    public static final RegistryKey<Item> CHROMAKEY_BLANK_KEY = RegistryKey.of(
        RegistryKeys.ITEM,
        Identifier.of(MOD_ID, "chromakey_blank")
    );

    public static final Item CHROMAKEY_BLANK = Registry.register(
        Registries.ITEM,
        CHROMAKEY_BLANK_KEY,
        new Item(new Item.Settings().registryKey(CHROMAKEY_BLANK_KEY))
    );

    // Вкладка творческого режима
    public static final RegistryKey<ItemGroup> CHROMAKEY_ITEM_GROUP_KEY = RegistryKey.of(
        RegistryKeys.ITEM_GROUP,
        Identifier.of(MOD_ID, "chromakey_item_group")
    );

    public static final ItemGroup CHROMAKEY_ITEM_GROUP = Registry.register(
        Registries.ITEM_GROUP,
        CHROMAKEY_ITEM_GROUP_KEY,
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

        // Регистрация сетевых кодеков
        PayloadTypeRegistry.playS2C().register(OpenColorScreenPayload.ID, OpenColorScreenPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ApplyColorPayload.ID, ApplyColorPayload.CODEC);

        // Обработка пакета применения цвета на сервере (запись в NBT предмета через NbtComponent)
        ServerPlayNetworking.registerGlobalReceiver(ApplyColorPayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                ItemStack stack = context.player().getMainHandStack();
                if (stack.getItem() instanceof ChromakeyControllerItem) {
                    NbtComponent customData = stack.get(DataComponentTypes.CUSTOM_DATA);
                    NbtCompound nbt = customData != null ? customData.copyNbt() : new NbtCompound();
                    nbt.putInt("custom_color", payload.rgb());
                    stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
                    context.player().sendMessage(Text.literal("Saved color: " + String.format("#%06X", payload.rgb())).formatted(Formatting.GREEN), true);
                }
            });
        });
    }
}
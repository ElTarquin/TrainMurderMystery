package dev.doctor4t.trainmurdermystery.index;

import dev.doctor4t.ratatouille.util.registrar.BlockEntityTypeRegistrar;
import dev.doctor4t.trainmurdermystery.TMM;
import dev.doctor4t.trainmurdermystery.block_entity.*;
import net.minecraft.block.entity.BlockEntityType;

public interface TMMBlockEntities {
    BlockEntityTypeRegistrar registrar = new BlockEntityTypeRegistrar(TMM.MOD_ID);

    BlockEntityType<SprinklerBlockEntity> SPRINKLER = registrar.create("sprinkler", BlockEntityType.Builder.create(SprinklerBlockEntity::new, TMMBlocks.GOLD_SPRINKLER, TMMBlocks.STAINLESS_STEEL_SPRINKLER));
    BlockEntityType<SmallDoorBlockEntity> SMALL_GLASS_DOOR = registrar.create("small_glass_door", BlockEntityType.Builder.create(SmallDoorBlockEntity::createGlass, TMMBlocks.SMALL_GLASS_DOOR));
    BlockEntityType<SmallDoorBlockEntity> SMALL_WOOD_DOOR = registrar.create("small_wood_door", BlockEntityType.Builder.create(SmallDoorBlockEntity::createWood, TMMBlocks.SMALL_WOOD_DOOR));
    BlockEntityType<SmallDoorBlockEntity> SMALL_TRAIN_DOOR = registrar.create("small_train_door", BlockEntityType.Builder.create(SmallDoorBlockEntity::createTrain, TMMBlocks.SMALL_TRAIN_DOOR));
    BlockEntityType<WheelBlockEntity> WHEEL = registrar.create("wheel", BlockEntityType.Builder.create(WheelBlockEntity::create, TMMBlocks.WHEEL));
    BlockEntityType<PlateBlockEntity> PLATE = registrar.create("plate", BlockEntityType.Builder.create(PlateBlockEntity::create, TMMBlocks.FOOD_PLATTER));
    BlockEntityType<DrinkPlateBlockEntity> DRINK_PLATE = registrar.create("drink_plate", BlockEntityType.Builder.create(DrinkPlateBlockEntity::create, TMMBlocks.DRINK_TRAY));
    BlockEntityType<TrimmedBedBlockEntity> TRIMMED_BED = registrar.create("trimmed_bed", BlockEntityType.Builder.create(TrimmedBedBlockEntity::create, TMMBlocks.RED_TRIMMED_BED, TMMBlocks.WHITE_TRIMMED_BED));

    static void initialize() {
        registrar.registerEntries();
    }
}

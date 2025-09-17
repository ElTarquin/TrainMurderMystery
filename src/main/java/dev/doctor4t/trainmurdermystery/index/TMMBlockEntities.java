package dev.doctor4t.trainmurdermystery.index;

import dev.doctor4t.ratatouille.util.registrar.BlockEntityTypeRegistrar;
import dev.doctor4t.trainmurdermystery.TMM;
import dev.doctor4t.trainmurdermystery.block_entity.SmallDoorBlockEntity;
import dev.doctor4t.trainmurdermystery.block_entity.SprinklerBlockEntity;
import net.minecraft.block.entity.BlockEntityType;

public interface TMMBlockEntities {
    BlockEntityTypeRegistrar registrar = new BlockEntityTypeRegistrar(TMM.MOD_ID);

    BlockEntityType<SprinklerBlockEntity> SPRINKLER = registrar.create("sprinkler", BlockEntityType.Builder.create(SprinklerBlockEntity::new, TMMBlocks.GOLD_SPRINKLER, TMMBlocks.STAINLESS_STEEL_SPRINKLER));
    BlockEntityType<SmallDoorBlockEntity> SMALL_GLASS_DOOR = registrar.create("small_glass_door", BlockEntityType.Builder.create(SmallDoorBlockEntity::createGlass, TMMBlocks.SMALL_GLASS_DOOR));
    BlockEntityType<SmallDoorBlockEntity> SMALL_WOOD_DOOR = registrar.create("small_wood_door", BlockEntityType.Builder.create(SmallDoorBlockEntity::createWood, TMMBlocks.SMALL_WOOD_DOOR));
    BlockEntityType<SmallDoorBlockEntity> SMALL_TRAIN_DOOR = registrar.create("small_train_door", BlockEntityType.Builder.create(SmallDoorBlockEntity::createTrain, TMMBlocks.SMALL_TRAIN_DOOR));

    static void initialize() {
        registrar.registerEntries();
    }
}

package dev.doctor4t.trainmurdermystery.cca;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.World;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;

public class WorldTrainComponent implements AutoSyncedComponent {
    private final World world;
    private float trainSpeed = 130; // im km/h

    public WorldTrainComponent(World world) {
        this.world = world;
    }

    private void sync() {
        TMMComponents.TRAIN.sync(this.world);
    }

    public void setTrainSpeed(float trainSpeed) {
        this.trainSpeed = trainSpeed;
        this.sync();
    }

    public float getTrainSpeed() {
        return trainSpeed;
    }

    @Override
    public void readFromNbt(NbtCompound nbtCompound, RegistryWrapper.WrapperLookup wrapperLookup) {
        this.trainSpeed = nbtCompound.getFloat("Speed");

    }

    @Override
    public void writeToNbt(NbtCompound nbtCompound, RegistryWrapper.WrapperLookup wrapperLookup) {
        nbtCompound.putFloat("Speed", trainSpeed);

    }
}

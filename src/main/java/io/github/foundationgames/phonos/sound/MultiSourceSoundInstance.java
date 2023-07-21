package io.github.foundationgames.phonos.sound;

import io.github.foundationgames.phonos.sound.emitter.SoundEmitterTree;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.AbstractSoundInstance;
import net.minecraft.client.sound.TickableSoundInstance;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.random.Random;
import org.joml.Vector3d;

import java.util.concurrent.atomic.AtomicReference;

public class MultiSourceSoundInstance extends AbstractSoundInstance implements TickableSoundInstance {
    public final AtomicReference<SoundEmitterTree> emitters;
    private double x;
    private double y;
    private double z;
    
    private boolean done;

    public MultiSourceSoundInstance(SoundEmitterTree tree, SoundEvent sound, Random random, float volume, float pitch) {
        super(sound.getId(), SoundCategory.MASTER, random);
        this.emitters = new AtomicReference<>(tree);
        this.volume = volume;
        this.pitch = pitch;

        this.updatePosition();
    }

    @Override
    public double getX() {
        return x;
    }

    @Override
    public double getY() {
        return y;
    }

    @Override
    public double getZ() {
        return z;
    }

    private void updatePosition() {
    	var mc = MinecraftClient.getInstance();
        var camPos = mc.gameRenderer.getCamera().getPos();

        var pos = new Vector3d();

        double[] emWeights = {0};
        boolean[] foundSources = {false};

        var emPos = new Vector3d();

        this.x = 0;
        this.y = 999;
        this.z = 0;

        this.emitters.get().forEachSource(mc.world, em -> {
            double weight;

            foundSources[0] = true;
            emPos.set(em.x(), em.y(), em.z());

            double dist = emPos.distance(camPos.x, camPos.y, camPos.z);

            // This won't make sense unless you graph it
            if (dist <= 1.888) {
                weight = -0.05 * Math.pow(dist, 4) + 1;
            } else {
                weight = 0.1 / (dist - 1.6138);
            }

            pos.add(emPos.mul(weight));
            emWeights[0] += weight;
        });

        if (foundSources[0]) {
            pos.div(emWeights[0]);

            this.x = pos.x();
            this.y = pos.y();
            this.z = pos.z();
        }
    }

    @Override
    public boolean isDone() {
        return done;
    }

    protected final void setDone() {
        this.done = true;
        this.repeat = false;
    }

    @Override
    public void tick() {
        updatePosition();
    }
}
package com.crashinvaders.common.sod;

import com.badlogic.gdx.math.MathUtils;

// A pure gold inspired by https://youtu.be/KPoeNZZ6H4s
public class SecondOrderDynamicsArray {

    private final int size;

    private float f, z, r;
    // Dynamic constants.
    private float k1, k2, k3;
    // Previous dst.
    private final float[] pDst;
    // State variables.
    private final float[] pos;
    private final float[] acc;
    private final float[] vel;

    public SecondOrderDynamicsArray(int size) {
        this.size = size;

        pDst = new float[size];
        pos = new float[size];
        acc = new float[size];
        vel = new float[size];
    }

    public void reset(float f, float z, float r, float[] dst) {
        configure(f, z, r);
        moveInstant(dst); // Reset state.
    }

    public void configure(float f, float z, float r) {
        this.f = f;
        this.z = z;
        this.r = r;

        // Compute constants.
        float pi = MathUtils.PI;
        k1 = z / (pi * f);
        k2 = 1 / ((2f * pi * f) * (2f * pi * f));
        k3 = r * z / (2f * pi * f);
    }

    public void moveInstant(float[] dst) {
        for (int i = 0; i < size; i++) {
            pDst[i] = pos[i] = dst[i];
            acc[i] = 0f;
        }
    }

    public void update(float deltaTime, float[] dst) {
        if (deltaTime == 0f)
            return;

        // Clamp k2 to guarantee stability without jitter.
        float k2Stable = Math.max(k2, Math.max(deltaTime * deltaTime / 2f + deltaTime * k1 / 2f, deltaTime * k1));

        for (int i = 0; i < size; i++) {
            // Estimate velocity.
            float xd = (dst[i] - pDst[i]) / deltaTime;
            pDst[i] = dst[i];

            // Integrate position by velocity.
            float vel = deltaTime * acc[i];
            pos[i] = pos[i] + vel;

            // Integrate velocity by acceleration.
            acc[i] = acc[i] + deltaTime * (dst[i] + k3 * xd - pos[i] - k1 * acc[i]) / k2Stable;
        }
    }

    public int getSize() {
        return size;
    }

    public float getF() {
        return f;
    }

    public float getZ() {
        return z;
    }

    public float getR() {
        return r;
    }

    public float getPos(int index) {
        return pos[index];
    }

    public void setPos(int index, float value) {
        pos[index] = value;
    }

    public float getVel(int index) {
        return vel[index];
    }

    public float getAcc(int index) {
        return acc[index];
    }

    public void setAcc(int index, float value) {
        acc[index] = value;
    }
}

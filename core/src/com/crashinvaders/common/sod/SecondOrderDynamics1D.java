package com.crashinvaders.common.sod;

import com.badlogic.gdx.math.MathUtils;

// A pure gold inspired by https://youtu.be/KPoeNZZ6H4s
public class SecondOrderDynamics1D
{
    private float f, z, r;
    // Dynamic constants.
    private float k1, k2, k3;
    // Previous dst.
    private float pDst;
    // State variables.
    private float pos;
    private float acc;
    private float vel;

    public void reset(float f, float z, float r, float dst)
    {
        configure(f, z, r);
        moveInstant(dst); // Reset state.
    }

    public void configure(float f, float z, float r)
    {
        this.f = f;
        this.z = z;
        this.r = r;

        // Compute constants.
        float pi = MathUtils.PI;
        k1 = z / (pi * f);
        k2 = 1 / ((2f * pi * f) * (2f * pi * f));
        k3 = r * z / (2f * pi * f);
    }

    public void moveInstant(float dst)
    {
        pDst = pos = dst;
        acc = 0f;
    }

    public void update(float deltaTime, float dst) {
        if (deltaTime == 0f)
            return;

        // Estimate velocity.
        float xd_x = (dst - pDst) / deltaTime;
        pDst = dst;

        // Clamp k2 to guarantee stability without jitter.
        float k2Stable = Math.max(k2, Math.max(deltaTime * deltaTime / 2f + deltaTime * k1 / 2f, deltaTime * k1));

        // Integrate position by velocity.
        vel = deltaTime * acc;
        pos = pos + vel;

        // Integrate velocity by acceleration.
        acc = acc + deltaTime * (dst + k3 * xd_x - pos - k1 * acc) / k2Stable;
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

    public float getPos() { return pos; }

    public float getVel() { return vel; }

    public float getAcc() {return acc;}
    public void setAcc(float acc) {this.acc = acc;}
}

package com.crashinvaders.common.sod;

import com.badlogic.gdx.math.MathUtils;

// A pure gold inspired by https://youtu.be/KPoeNZZ6H4s
public class SecondOrderDynamics2D
{
    private float f, z, r;
    // Dynamic constants.
    private float k1, k2, k3;
    // Previous dst.
    private float pDstX, pDstY;
    // State variables.
    private float posX, posY;
    private float accX, accY;
    private float velX, velY;

    public void reset(float f, float z, float r, float dstX, float dstY)
    {
        configure(f, z, r);
        moveInstant(dstX, dstY); // Reset state.
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

    public void moveInstant(float dstX, float dstY)
    {
        pDstX = posX = dstX;
        pDstY = posY = dstY;
        accX = accY = 0f;
    }

    public void update(float deltaTime, float dstX, float dstY) {
        if (deltaTime == 0f)
            return;

        // Estimate velocity.
        float xd_x = (dstX - pDstX) / deltaTime;
        float xd_y = (dstY - pDstY) / deltaTime;
        pDstX = dstX;
        pDstY = dstY;

        // Clamp k2 to guarantee stability without jitter.
        float k2Stable = Math.max(k2, Math.max(deltaTime * deltaTime / 2f + deltaTime * k1 / 2f, deltaTime * k1));

        // Integrate position by velocity.
        velX = deltaTime * accX;
        velY = deltaTime * accY;
        posX = posX + velX;
        posY = posY + velY;

        // Integrate velocity by acceleration.
        accX = accX + deltaTime * (dstX + k3 * xd_x - posX - k1 * accX) / k2Stable;
        accY = accY + deltaTime * (dstY + k3 * xd_y - posY - k1 * accY) / k2Stable;
    }

    public float getF() {return f;}
    public float getZ() {return z;}
    public float getR() {return r;}

    public float getPosX() { return posX; }
    public void setPosX(float posX) { this.posX = posX; }
    public float getPosY() { return posY; }
    public void setPosY(float posY) { this.posY = posY; }

    public float getVelX() { return velX; }
    public float getVelY() { return velY; }

    public float getAccX() {return accX;}
    public void setAccX(float accX) { this.accX = accX; }
    public float getAccY() {return accY;}
    public void setAccY(float accY) { this.accY = accY; }
}

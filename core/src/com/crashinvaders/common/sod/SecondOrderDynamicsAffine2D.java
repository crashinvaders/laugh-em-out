package com.crashinvaders.common.sod;

import com.badlogic.gdx.math.Affine2;
import com.badlogic.gdx.math.MathUtils;

// A pure gold inspired by https://youtu.be/KPoeNZZ6H4s
public class SecondOrderDynamicsAffine2D
{
    private static final int COMP_COUNT = 6;

    private static final float[] tmpArray = new float[COMP_COUNT];
    private static final Affine2 tmpMatrix = new Affine2();

    private float f, z, r;
    // Dynamic constants.
    private float k1, k2, k3;
    // Previous dst.
    private final float[] pDst = new float[]{1, 0, 0, 0, 1, 0}; // Identity matrix.
    // State variables.
    private final float[] pos = new float[]{1, 0, 0, 0, 1, 0}; // Identity matrix.
    private final float[] acc = new float[COMP_COUNT];

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

    public void resetState(Affine2 matrix)
    {
        readFromMatrix(tmpArray, matrix);
        for (int i = 0; i < COMP_COUNT; i++) {

            pDst[i] = pos[i] = tmpArray[i];
            acc[i] = 0f;
        }
    }

    public void update(float deltaTime, Affine2 matrix) {
        if (deltaTime == 0f)
            return;

        // Clamp k2 to guarantee stability without jitter.
        float k2Stable = Math.max(k2, Math.max(deltaTime * deltaTime / 2f + deltaTime * k1 / 2f, deltaTime * k1));

        float[] dst = tmpArray;
        readFromMatrix(dst, matrix);
        for (int i = 0; i < COMP_COUNT; i++) {
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

    public float getF() {return f;}
    public float getZ() {return z;}
    public float getR() {return r;}

    public Affine2 getPosMatrix() {
        writeToMatrix(pos, tmpMatrix);
        return tmpMatrix;
    }

    public void setPosMatrix(Affine2 matrix) {
        readFromMatrix(pos, matrix);
        readFromMatrix(pDst, matrix);
    }

    public Affine2 getAccMatrix() {
        writeToMatrix(acc, tmpMatrix);
        return tmpMatrix;
    }

    public void setAccMatrix(Affine2 matrix) {
        readFromMatrix(acc, matrix);
    }

    public void addAccMatrix(Affine2 matrix) {
        readFromMatrix(tmpArray, matrix);
        for (int i = 0; i < COMP_COUNT; i++) {
            acc[i] += tmpArray[i];
        }
    }

    private static void readFromMatrix(float[] array, Affine2 matrix) {
        array[0] = matrix.m00;
        array[1] = matrix.m01;
        array[2] = matrix.m02;
        array[3] = matrix.m10;
        array[4] = matrix.m11;
        array[5] = matrix.m12;
    }

    private static void writeToMatrix(float[] array, Affine2 matrix) {
        matrix.m00 = array[0];
        matrix.m01 = array[1];
        matrix.m02 = array[2];
        matrix.m10 = array[3];
        matrix.m11 = array[4];
        matrix.m12 = array[5];
    }
}

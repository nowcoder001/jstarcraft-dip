package com.jstarcraft.dip.lsh;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import com.github.kilianB.ArrayUtil;
import com.github.kilianB.Require;
import com.jstarcraft.dip.color.ColorPixel;
import com.jstarcraft.dip.lsh.kernel.Kernel;

/**
 * Calculate a hash value based on the average luminosity in an image. The
 * kernel hash variant applies additional kernels to the already rescaled
 * version of the image.
 * 
 * <p>
 * Be aware that to guarantee hash consistency the supplied kernels supplied are
 * implicitly assumed to be immutable. Do NOT change the settings of kernels
 * after adding them to the hashing algorithm or else hash collections as well
 * as trouble with the database image matchers may arise.
 * 
 * @author Kilian
 *
 */
// 参考Kilian的AverageKernelHash
public class KernelHash extends AverageHash {

    /**
     * The kernel filtering the luminosity of the image
     */
    private final List<Kernel> kernels;

    /**
     * @param bitResolution The bit resolution specifies the final length of the
     *                      generated hash. A higher resolution will increase
     *                      computation time and space requirement while being able
     *                      to track finer detail in the image. Be aware that a high
     *                      key is not always desired.
     *                      <p>
     * 
     *                      The average kernel hash will produce a hash with at
     *                      least the number of bits defined by this argument. In
     *                      turn this also means that different bit resolutions may
     *                      be mapped to the same final key length.
     * 
     *                      <pre>
     *  64 = 8x8 = 65 bit key
     *  128 = 11.3 -&gt; 12 -&gt; 144 bit key
     *  256 = 16 x 16 = 256 bit key
     *                      </pre>
     */
    public KernelHash(int bitResolution) {
        this(bitResolution, Kernel.boxFilterNormalized(3, 3));
    }

    /**
     * @param bitResolution The bit resolution specifies the final length of the
     *                      generated hash. A higher resolution will increase
     *                      computation time and space requirement while being able
     *                      to track finer detail in the image. Be aware that a high
     *                      key is not always desired.
     *                      <p>
     * 
     *                      The average hash requires to re scale the base image
     *                      according to the required bit resolution. If the square
     *                      root of the bit resolution is not a natural number the
     *                      resolution will be rounded to the next whole number.
     *                      </p>
     * 
     *                      The average hash will produce a hash with at least the
     *                      number of bits defined by this argument. In turn this
     *                      also means that different bit resolutions may be mapped
     *                      to the same final key length.
     * 
     *                      <pre>
     *  64 = 8x8 = 65 bit key
     *  128 = 11.3 -&gt; 12 -&gt; 144 bit key
     *  256 = 16 x 16 = 256 bit key
     *                      </pre>
     * 
     * @param kernels       applied before the rescaled image is compared to the
     *                      filter Since raw luminosity values are compared to the
     *                      computed kernel value the kernel should be in the same
     *                      range
     */
    public KernelHash(int bitResolution, Kernel... kernels) {
        super(bitResolution);
        this.kernels = new ArrayList<Kernel>(Arrays.asList(Require.deepNonNull(kernels, "The kernel may not be null")));
    }

    @Override
    protected BigInteger hash(ColorPixel pixel, HashBuilder hash) {
        int[][] luminance = pixel.getLuminanceMatrix();

        // Calculate the average color of the entire image

        for (Kernel kernel : kernels) {
            luminance = kernel.applyInt(luminance);
        }

        double average = ArrayUtil.average(luminance);

        return computeHash(hash, luminance, average);
    }

    @Override
    protected int precomputeAlgoId() {
        // *31 to create a distinct id compare to v 2.0.0 bugfix
        return Objects.hash(getClass().getName(), height, width, kernels) * 31;
    }

    @Override
    public String toString() {
        return "AverageKernelHash [height=" + height + ", width=" + width + ", filters=" + kernels + "]";
    }

}

package name.lapidary.window;

import java.util.Arrays;

public record WindowDesign(
        int blockWidth,
        int blockHeight,
        String backgroundId,
        byte[] pixels
) {

    public static final int PIXELS_PER_BLOCK =
            16;

    public static final int MIN_BLOCK_SIZE =
            1;

    public static final int MAX_BLOCK_SIZE =
            5;

    public static final byte BACKGROUND_PIXEL =
            16;

    public static final int COLOR_COUNT =
            16;

    public static final int MAX_PIXEL_COUNT =
            MAX_BLOCK_SIZE
                    * PIXELS_PER_BLOCK
                    * MAX_BLOCK_SIZE
                    * PIXELS_PER_BLOCK;

    public WindowDesign {
        if (blockWidth < MIN_BLOCK_SIZE
                || blockWidth > MAX_BLOCK_SIZE) {

            throw new IllegalArgumentException(
                    "Window width must be from 1 to 5 blocks."
            );
        }

        if (blockHeight < MIN_BLOCK_SIZE
                || blockHeight > MAX_BLOCK_SIZE) {

            throw new IllegalArgumentException(
                    "Window height must be from 1 to 5 blocks."
            );
        }

        if (backgroundId == null) {
            throw new IllegalArgumentException(
                    "Window background cannot be null."
            );
        }

        int expectedLength =
                blockWidth
                        * PIXELS_PER_BLOCK
                        * blockHeight
                        * PIXELS_PER_BLOCK;

        if (pixels == null
                || pixels.length != expectedLength) {

            throw new IllegalArgumentException(
                    "Window pixel array has the wrong length."
            );
        }

        for (byte pixel : pixels) {
            int value =
                    Byte.toUnsignedInt(pixel);

            if (value < 0
                    || value > BACKGROUND_PIXEL) {

                throw new IllegalArgumentException(
                        "Invalid stained-glass pixel value: "
                                + value
                );
            }
        }

        /*
         * Normalize unknown IDs to a known background and prevent callers
         * from retaining a mutable reference to the design's pixel array.
         */
        backgroundId =
                WindowBackground.byId(
                        backgroundId
                ).id();

        pixels =
                pixels.clone();
    }

    @Override
    public byte[] pixels() {
        return pixels.clone();
    }

    public int pixelWidth() {
        return blockWidth
                * PIXELS_PER_BLOCK;
    }

    public int pixelHeight() {
        return blockHeight
                * PIXELS_PER_BLOCK;
    }

    public int pixelCount() {
        return pixels.length;
    }

    public WindowBackground background() {
        return WindowBackground.byId(
                backgroundId
        );
    }

    public byte pixelAt(
            int x,
            int y
    ) {
        if (x < 0
                || x >= pixelWidth()
                || y < 0
                || y >= pixelHeight()) {

            throw new IndexOutOfBoundsException(
                    "Pixel coordinate outside window design."
            );
        }

        return pixels[
                y * pixelWidth() + x
        ];
    }

    public static WindowDesign blank(
            int blockWidth,
            int blockHeight,
            WindowBackground background
    ) {
        byte[] pixels =
                new byte[
                        blockWidth
                                * PIXELS_PER_BLOCK
                                * blockHeight
                                * PIXELS_PER_BLOCK
                        ];

        Arrays.fill(
                pixels,
                BACKGROUND_PIXEL
        );

        return new WindowDesign(
                blockWidth,
                blockHeight,
                background.id(),
                pixels
        );
    }

    public WindowDesign resized(
            int newBlockWidth,
            int newBlockHeight
    ) {
        WindowDesign blank =
                blank(
                        newBlockWidth,
                        newBlockHeight,
                        background()
                );

        byte[] resizedPixels =
                blank.pixels();

        int copiedWidth =
                Math.min(
                        pixelWidth(),
                        blank.pixelWidth()
                );

        int copiedHeight =
                Math.min(
                        pixelHeight(),
                        blank.pixelHeight()
                );

        for (int y = 0;
             y < copiedHeight;
             y++) {

            System.arraycopy(
                    pixels,
                    y * pixelWidth(),
                    resizedPixels,
                    y * blank.pixelWidth(),
                    copiedWidth
            );
        }

        return new WindowDesign(
                newBlockWidth,
                newBlockHeight,
                backgroundId,
                resizedPixels
        );
    }
}

package name.lapidary.window;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

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

            if (value > BACKGROUND_PIXEL) {
                throw new IllegalArgumentException(
                        "Invalid stained-glass pixel value: "
                                + value
                );
            }
        }

        /*
         * Older Phase 1 designs stored values such as "stone_bricks".
         * Treat an ID without a namespace as a vanilla Minecraft block.
         */
        backgroundId =
                normalizeBackgroundId(
                        backgroundId
                );

        /*
         * Prevent callers from retaining a mutable reference to the
         * design's saved pixels.
         */
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

    public Block backgroundBlock() {
        ResourceLocation id =
                ResourceLocation.tryParse(
                        backgroundId
                );

        if (id == null) {
            return Blocks.STONE_BRICKS;
        }

        return BuiltInRegistries.BLOCK.get(
                id
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
            Block backgroundBlock
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
                BuiltInRegistries.BLOCK
                        .getKey(backgroundBlock)
                        .toString(),
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
                        backgroundBlock()
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

    private static String normalizeBackgroundId(
            String rawId
    ) {
        if (rawId == null
                || rawId.isBlank()) {

            throw new IllegalArgumentException(
                    "Window background cannot be empty."
            );
        }

        String expandedId =
                rawId.contains(":")
                        ? rawId
                        : "minecraft:" + rawId;

        ResourceLocation id =
                ResourceLocation.tryParse(
                        expandedId
                );

        if (id == null) {
            throw new IllegalArgumentException(
                    "Invalid background block ID: "
                            + rawId
            );
        }

        Block block =
                BuiltInRegistries.BLOCK.get(
                        id
                );

        /*
         * AIR is also the registry's fallback for an unknown ID. It has
         * no usable item form, so neither AIR nor missing blocks may be
         * used as a window background.
         */
        if (block == Blocks.AIR
                || block.asItem() == Items.AIR) {

            throw new IllegalArgumentException(
                    "Background must be a registered block item: "
                            + rawId
            );
        }

        return BuiltInRegistries.BLOCK
                .getKey(block)
                .toString();
    }
}

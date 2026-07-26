package name.lapidary.window;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.Optional;

public final class WindowDesignData {

    private static final String ROOT_TAG =
            "LapidaryWindowDesign";

    private static final String WIDTH_TAG =
            "BlockWidth";

    private static final String HEIGHT_TAG =
            "BlockHeight";

    private static final String BACKGROUND_TAG =
            "Background";

    private static final String PIXELS_TAG =
            "Pixels";

    private WindowDesignData() {
    }

    public static void write(
            ItemStack stack,
            WindowDesign design
    ) {
        CompoundTag root =
                new CompoundTag();

        writeToTag(
                root,
                ROOT_TAG,
                design
        );

        stack.set(
                DataComponents.CUSTOM_DATA,
                CustomData.of(root)
        );
    }

    public static Optional<WindowDesign> read(
            ItemStack stack
    ) {
        CustomData customData =
                stack.get(
                        DataComponents.CUSTOM_DATA
                );

        if (customData == null) {
            return Optional.empty();
        }

        return readFromTag(
                customData.copyTag(),
                ROOT_TAG
        );
    }

    /**
     * Writes a design beneath a named compound. This is shared by the
     * custom item and the placed controller block entity.
     */
    public static void writeToTag(
            CompoundTag parent,
            String key,
            WindowDesign design
    ) {
        CompoundTag designTag =
                new CompoundTag();

        designTag.putInt(
                WIDTH_TAG,
                design.blockWidth()
        );

        designTag.putInt(
                HEIGHT_TAG,
                design.blockHeight()
        );

        designTag.putString(
                BACKGROUND_TAG,
                design.backgroundId()
        );

        designTag.putByteArray(
                PIXELS_TAG,
                design.pixels()
        );

        parent.put(
                key,
                designTag
        );
    }

    /**
     * Reads and validates a design from a named compound.
     */
    public static Optional<WindowDesign> readFromTag(
            CompoundTag parent,
            String key
    ) {
        if (!parent.contains(
                key,
                Tag.TAG_COMPOUND
        )) {
            return Optional.empty();
        }

        CompoundTag designTag =
                parent.getCompound(
                        key
                );

        if (!designTag.contains(
                WIDTH_TAG,
                Tag.TAG_INT
        )
                || !designTag.contains(
                HEIGHT_TAG,
                Tag.TAG_INT
        )
                || !designTag.contains(
                BACKGROUND_TAG,
                Tag.TAG_STRING
        )
                || !designTag.contains(
                PIXELS_TAG,
                Tag.TAG_BYTE_ARRAY
        )) {

            return Optional.empty();
        }

        try {
            return Optional.of(
                    new WindowDesign(
                            designTag.getInt(
                                    WIDTH_TAG
                            ),
                            designTag.getInt(
                                    HEIGHT_TAG
                            ),
                            designTag.getString(
                                    BACKGROUND_TAG
                            ),
                            designTag.getByteArray(
                                    PIXELS_TAG
                            )
                    )
            );
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }
    }
}

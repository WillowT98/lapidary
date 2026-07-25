package name.lapidary.magic;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public record PlayerMagicData(
        List<String> knownSpells,
        List<String> knownRituals,
        List<String> preparedSpells,
        int selectedSlot
) {

    public static final int PREPARED_SLOT_COUNT = 8;
    public static final String EMPTY_SLOT = "";

    public static final Codec<PlayerMagicData> CODEC =
            RecordCodecBuilder.create(instance ->
                    instance.group(
                            Codec.STRING
                                    .listOf()
                                    .optionalFieldOf(
                                            "known_spells",
                                            List.<String>of()
                                    )
                                    .forGetter(
                                            PlayerMagicData::knownSpells
                                    ),

                            Codec.STRING
                                    .listOf()
                                    .optionalFieldOf(
                                            "known_rituals",
                                            List.<String>of()
                                    )
                                    .forGetter(
                                            PlayerMagicData::knownRituals
                                    ),

                            Codec.STRING
                                    .listOf()
                                    .optionalFieldOf(
                                            "prepared_spells",
                                            List.<String>of()
                                    )
                                    .forGetter(
                                            PlayerMagicData::preparedSpells
                                    ),

                            Codec.INT
                                    .optionalFieldOf(
                                            "selected_slot",
                                            0
                                    )
                                    .forGetter(
                                            PlayerMagicData::selectedSlot
                                    )
                    ).apply(
                            instance,
                            PlayerMagicData::new
                    )
            );

    /*
     * Normalize all data whenever a new immutable state is created.
     *
     * This means malformed save data cannot produce more than eight
     * slots or an invalid selected-slot index.
     */
    public PlayerMagicData {
        knownSpells = normalizeIdentifierList(knownSpells);
        knownRituals = normalizeIdentifierList(knownRituals);
        preparedSpells = normalizePreparedSlots(preparedSpells);
        selectedSlot = Math.clamp(
                selectedSlot,
                0,
                PREPARED_SLOT_COUNT - 1
        );
    }

    public static PlayerMagicData empty() {
        return new PlayerMagicData(
                List.of(),
                List.of(),
                Collections.nCopies(
                        PREPARED_SLOT_COUNT,
                        EMPTY_SLOT
                ),
                0
        );
    }

    public boolean knowsSpell(ResourceLocation spellId) {
        return knownSpells.contains(spellId.toString());
    }

    public boolean knowsRitual(ResourceLocation ritualId) {
        return knownRituals.contains(ritualId.toString());
    }

    public Optional<ResourceLocation> preparedSpell(int slot) {
        if (!isValidSlot(slot)) {
            return Optional.empty();
        }

        String value = preparedSpells.get(slot);

        if (value.isBlank()) {
            return Optional.empty();
        }

        return Optional.ofNullable(
                ResourceLocation.tryParse(value)
        );
    }

    public Optional<ResourceLocation> selectedSpell() {
        return preparedSpell(selectedSlot);
    }

    public PlayerMagicData withKnownSpell(
            ResourceLocation spellId
    ) {
        Set<String> updated =
                new LinkedHashSet<>(knownSpells);

        updated.add(spellId.toString());

        return new PlayerMagicData(
                List.copyOf(updated),
                knownRituals,
                preparedSpells,
                selectedSlot
        );
    }

    public PlayerMagicData withKnownRitual(
            ResourceLocation ritualId
    ) {
        Set<String> updated =
                new LinkedHashSet<>(knownRituals);

        updated.add(ritualId.toString());

        return new PlayerMagicData(
                knownSpells,
                List.copyOf(updated),
                preparedSpells,
                selectedSlot
        );
    }

    public PlayerMagicData withPreparedSpell(
            int slot,
            ResourceLocation spellId
    ) {
        if (!isValidSlot(slot)) {
            return this;
        }

        List<String> updated =
                new ArrayList<>(preparedSpells);

        /*
         * Prevent the same spell from occupying multiple wedges.
         */
        updated.replaceAll(existing ->
                existing.equals(spellId.toString())
                        ? EMPTY_SLOT
                        : existing
        );

        updated.set(
                slot,
                spellId.toString()
        );

        return new PlayerMagicData(
                knownSpells,
                knownRituals,
                updated,
                selectedSlot
        );
    }

    public PlayerMagicData withoutPreparedSpell(int slot) {
        if (!isValidSlot(slot)) {
            return this;
        }

        List<String> updated =
                new ArrayList<>(preparedSpells);

        updated.set(
                slot,
                EMPTY_SLOT
        );

        return new PlayerMagicData(
                knownSpells,
                knownRituals,
                updated,
                selectedSlot
        );
    }

    public PlayerMagicData withSwappedSlots(
            int firstSlot,
            int secondSlot
    ) {
        if (!isValidSlot(firstSlot)
                || !isValidSlot(secondSlot)
                || firstSlot == secondSlot) {
            return this;
        }

        List<String> updated =
                new ArrayList<>(preparedSpells);

        Collections.swap(
                updated,
                firstSlot,
                secondSlot
        );

        return new PlayerMagicData(
                knownSpells,
                knownRituals,
                updated,
                selectedSlot
        );
    }

    public PlayerMagicData withSelectedSlot(int slot) {
        if (!isValidSlot(slot)) {
            return this;
        }

        return new PlayerMagicData(
                knownSpells,
                knownRituals,
                preparedSpells,
                slot
        );
    }

    public static boolean isValidSlot(int slot) {
        return slot >= 0
                && slot < PREPARED_SLOT_COUNT;
    }

    private static List<String> normalizeIdentifierList(
            List<String> values
    ) {
        if (values == null || values.isEmpty()) {
            return List.of();
        }

        Set<String> normalized =
                new LinkedHashSet<>();

        for (String value : values) {
            String validId = normalizeIdentifier(value);

            if (!validId.isEmpty()) {
                normalized.add(validId);
            }
        }

        return List.copyOf(normalized);
    }

    private static List<String> normalizePreparedSlots(
            List<String> values
    ) {
        List<String> normalized =
                new ArrayList<>(
                        Collections.nCopies(
                                PREPARED_SLOT_COUNT,
                                EMPTY_SLOT
                        )
                );

        if (values == null) {
            return List.copyOf(normalized);
        }

        int count = Math.min(
                values.size(),
                PREPARED_SLOT_COUNT
        );

        for (int index = 0; index < count; index++) {
            normalized.set(
                    index,
                    normalizeIdentifier(values.get(index))
            );
        }

        return List.copyOf(normalized);
    }

    private static String normalizeIdentifier(String value) {
        if (value == null || value.isBlank()) {
            return EMPTY_SLOT;
        }

        ResourceLocation identifier =
                ResourceLocation.tryParse(value);

        return identifier == null
                ? EMPTY_SLOT
                : identifier.toString();
    }


}
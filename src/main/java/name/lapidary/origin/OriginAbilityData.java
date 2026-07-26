package name.lapidary.origin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record OriginAbilityData(
        int mothFlaps,
        int fairyFlightCharge,
        long sharpenedUntil,
        long transformedUntil,
        boolean mothDiving,
        String fairyHostUuid,
        long activeCooldownUntil,
        long magicCooldownUntil,
        long voiceCooldownUntil
) {

    public static final Codec<OriginAbilityData> CODEC =
            RecordCodecBuilder.create(
                    instance ->
                            instance.group(
                                    Codec.INT.optionalFieldOf(
                                            "moth_flaps",
                                            OriginManager.MOTH_MAX_FLAPS
                                    ).forGetter(
                                            OriginAbilityData::mothFlaps
                                    ),
                                    Codec.INT.optionalFieldOf(
                                            "fairy_flight_charge",
                                            OriginManager.FAIRY_MAX_FLIGHT_CHARGE
                                    ).forGetter(
                                            OriginAbilityData::fairyFlightCharge
                                    ),
                                    Codec.LONG.optionalFieldOf(
                                            "sharpened_until",
                                            0L
                                    ).forGetter(
                                            OriginAbilityData::sharpenedUntil
                                    ),
                                    Codec.LONG.optionalFieldOf(
                                            "transformed_until",
                                            0L
                                    ).forGetter(
                                            OriginAbilityData::transformedUntil
                                    ),
                                    Codec.BOOL.optionalFieldOf(
                                            "moth_diving",
                                            false
                                    ).forGetter(
                                            OriginAbilityData::mothDiving
                                    ),
                                    Codec.STRING.optionalFieldOf(
                                            "fairy_host_uuid",
                                            ""
                                    ).forGetter(
                                            OriginAbilityData::fairyHostUuid
                                    ),
                                    Codec.LONG.optionalFieldOf(
                                            "active_cooldown_until",
                                            0L
                                    ).forGetter(
                                            OriginAbilityData::activeCooldownUntil
                                    ),
                                    Codec.LONG.optionalFieldOf(
                                            "magic_cooldown_until",
                                            0L
                                    ).forGetter(
                                            OriginAbilityData::magicCooldownUntil
                                    ),
                                    Codec.LONG.optionalFieldOf(
                                            "voice_cooldown_until",
                                            0L
                                    ).forGetter(
                                            OriginAbilityData::voiceCooldownUntil
                                    )
                            ).apply(
                                    instance,
                                    OriginAbilityData::new
                            )
            );

    public static OriginAbilityData empty() {
        return new OriginAbilityData(
                OriginManager.MOTH_MAX_FLAPS,
                OriginManager.FAIRY_MAX_FLIGHT_CHARGE,
                0L,
                0L,
                false,
                "",
                0L,
                0L,
                0L
        );
    }

    public OriginAbilityData withMothFlaps(
            int value
    ) {
        return new OriginAbilityData(
                value,
                fairyFlightCharge,
                sharpenedUntil,
                transformedUntil,
                mothDiving,
                fairyHostUuid,
                activeCooldownUntil,
                magicCooldownUntil,
                voiceCooldownUntil
        );
    }

    public OriginAbilityData withFairyFlightCharge(
            int value
    ) {
        return new OriginAbilityData(
                mothFlaps,
                value,
                sharpenedUntil,
                transformedUntil,
                mothDiving,
                fairyHostUuid,
                activeCooldownUntil,
                magicCooldownUntil,
                voiceCooldownUntil
        );
    }

    public OriginAbilityData withSharpenedUntil(
            long value
    ) {
        return new OriginAbilityData(
                mothFlaps,
                fairyFlightCharge,
                value,
                transformedUntil,
                mothDiving,
                fairyHostUuid,
                activeCooldownUntil,
                magicCooldownUntil,
                voiceCooldownUntil
        );
    }

    public OriginAbilityData withTransformedUntil(
            long value
    ) {
        return new OriginAbilityData(
                mothFlaps,
                fairyFlightCharge,
                sharpenedUntil,
                value,
                mothDiving,
                fairyHostUuid,
                activeCooldownUntil,
                magicCooldownUntil,
                voiceCooldownUntil
        );
    }

    public OriginAbilityData withMothDiving(
            boolean value
    ) {
        return new OriginAbilityData(
                mothFlaps,
                fairyFlightCharge,
                sharpenedUntil,
                transformedUntil,
                value,
                fairyHostUuid,
                activeCooldownUntil,
                magicCooldownUntil,
                voiceCooldownUntil
        );
    }

    public OriginAbilityData withFairyHostUuid(
            String value
    ) {
        return new OriginAbilityData(
                mothFlaps,
                fairyFlightCharge,
                sharpenedUntil,
                transformedUntil,
                mothDiving,
                value,
                activeCooldownUntil,
                magicCooldownUntil,
                voiceCooldownUntil
        );
    }

    public OriginAbilityData withActiveCooldownUntil(
            long value
    ) {
        return new OriginAbilityData(
                mothFlaps,
                fairyFlightCharge,
                sharpenedUntil,
                transformedUntil,
                mothDiving,
                fairyHostUuid,
                value,
                magicCooldownUntil,
                voiceCooldownUntil
        );
    }

    public OriginAbilityData withMagicCooldownUntil(
            long value
    ) {
        return new OriginAbilityData(
                mothFlaps,
                fairyFlightCharge,
                sharpenedUntil,
                transformedUntil,
                mothDiving,
                fairyHostUuid,
                activeCooldownUntil,
                value,
                voiceCooldownUntil
        );
    }

    public OriginAbilityData withVoiceCooldownUntil(
            long value
    ) {
        return new OriginAbilityData(
                mothFlaps,
                fairyFlightCharge,
                sharpenedUntil,
                transformedUntil,
                mothDiving,
                fairyHostUuid,
                activeCooldownUntil,
                magicCooldownUntil,
                value
        );
    }
}

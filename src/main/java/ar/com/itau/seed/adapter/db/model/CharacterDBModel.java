package ar.com.itau.seed.adapter.db.model;

import ar.com.itau.seed.domain.SWCharacter;
import lombok.Data;
import lombok.NonNull;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

@Entity(name = "CHARACTER")
@Data
public class CharacterDBModel {

    private static LocalDateTime now() {
        return ZonedDateTime.now().withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
    }

    public static CharacterDBModel from(final SWCharacter domain){
        final CharacterDBModel model = new CharacterDBModel();
        final LocalDateTime currentTimestamp = now();
        model.setBirthYear(domain.getBirthYear());
        model.setHeight(domain.getHeight());
        model.setEyeColor(domain.getEyeColor());
        model.setGender(domain.getGender());
        model.setName(domain.getName());
        model.setBirthYear(domain.getBirthYear());
        model.setMass(domain.getMass());
        model.setCreatedAt(currentTimestamp);
        model.setUpdatedAt(currentTimestamp);
        return model;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", updatable = false)
    private Long id;

    @Column(name = "NAME", nullable = false, updatable = false)
    String name;

    @Column(name = "HEIGHT", nullable = false, updatable = false)
    Integer height;

    @Column(name = "MASS", nullable = false, updatable = false)
    Integer mass;

    @Column(name = "HAIR_COLOR", nullable = false, updatable = false)
    String hairColor;

    @Column(name = "EYE_COLOR", nullable = false, updatable = false)
    String eyeColor;

    @Column(name = "BIRTH_YEAR", nullable = false, updatable = false)
    String birthYear;

    @Column(name = "GENDER", nullable = false, updatable = false)
    String gender;

    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    LocalDateTime createdAt;

    @Column(name = "UPDATED_AT", nullable = false, updatable = false)
    LocalDateTime updatedAt;

    public SWCharacter toDomain(){
        return SWCharacter.builder()
                .birthYear(birthYear)
                .gender(gender)
                .height(height)
                .mass(mass)
                .eyeColor(eyeColor)
                .hairColor(hairColor)
                .name(name)
                .updatedAt(updatedAt)
                .createdAt(createdAt)
                .build();
    }

}

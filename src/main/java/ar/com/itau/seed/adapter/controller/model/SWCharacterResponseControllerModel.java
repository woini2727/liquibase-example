package ar.com.itau.seed.adapter.controller.model;

import ar.com.itau.seed.domain.SWCharacter;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class SWCharacterResponseControllerModel {
    String name;
    Integer height;
    Integer mass;
    String hairColor;
    String eyeColor;
    String birthYear;
    String gender;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;

    public static SWCharacterResponseControllerModel from(SWCharacter domain){
        return SWCharacterResponseControllerModel.builder()
                .name(domain.getName())
                .height(domain.getHeight())
                .mass(domain.getMass())
                .hairColor(domain.getHairColor())
                .eyeColor(domain.getEyeColor())
                .birthYear(domain.getBirthYear())
                .gender(domain.getGender())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
}

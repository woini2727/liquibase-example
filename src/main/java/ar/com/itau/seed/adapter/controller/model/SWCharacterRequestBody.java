package ar.com.itau.seed.adapter.controller.model;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class SWCharacterRequestBody {

    @NonNull String name;
    @NonNull Integer height;
    @NonNull Integer mass;
    @NonNull String hairColor;
    @NonNull String eyeColor;
    @NonNull String birthYear;
    @NonNull String gender;
    @NonNull LocalDateTime createdAt;
    @NonNull LocalDateTime updatedAt;
}

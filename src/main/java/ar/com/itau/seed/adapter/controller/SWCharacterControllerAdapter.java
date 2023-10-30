package ar.com.itau.seed.adapter.controller;

import ar.com.itau.seed.adapter.controller.model.SWCharacterControllerModel;
import ar.com.itau.seed.adapter.controller.model.SWCharacterRequestBody;
import ar.com.itau.seed.adapter.controller.model.SWCharacterResponseControllerModel;
import ar.com.itau.seed.application.port.in.GetSWCharacterByIdQuery;
import ar.com.itau.seed.application.port.in.PostSWCharacter;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.util.concurrent.CompletionStage;

@RestController
@RequestMapping("/api/v1/characters")
@Slf4j
@Validated
public class SWCharacterControllerAdapter {

    private final GetSWCharacterByIdQuery getSWCharacterByIdQuery;
    private final PostSWCharacter postSWCharacter;

    public SWCharacterControllerAdapter(
            final GetSWCharacterByIdQuery getSWCharacterByIdQuery,
            final PostSWCharacter postSWCharacter) {
        this.getSWCharacterByIdQuery = getSWCharacterByIdQuery;
        this.postSWCharacter = postSWCharacter;
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            tags = {"Characters"},
            summary = "Get character",
            description = "Get a character by its unique ID"
    )
    public CompletionStage<SWCharacterControllerModel> get(
            @NotNull @Positive @PathVariable("id") final Integer id
    ) {
        log.info("Call to get character by ID {}", id);
        return getSWCharacterByIdQuery.get(id)
                .thenApply(domain -> {
                    final SWCharacterControllerModel response = SWCharacterControllerModel.from(domain);
                    log.info("Replying to get character by ID request with {}", response);
                    return response;
                });
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            tags = {"Characters"},
            summary = "Post character",
            description = "Post a character by its unique ID"
    )
    public CompletionStage<SWCharacterResponseControllerModel> get(
            @Validated @RequestBody final SWCharacterRequestBody request
    ){
        log.info(request.toString());
        return postSWCharacter.execute(request.toDomain()).thenApply(
                character -> {
                    final SWCharacterResponseControllerModel response = SWCharacterResponseControllerModel.from(character);
                    log.info("Response to POST /characters {}", response);
                    return response;
                }
        );
    }
}

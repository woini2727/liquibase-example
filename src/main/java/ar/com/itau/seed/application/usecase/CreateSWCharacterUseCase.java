package ar.com.itau.seed.application.usecase;

import ar.com.itau.seed.application.port.in.PostSWCharacter;
import ar.com.itau.seed.application.port.out.SWCharacterDBRepository;
import ar.com.itau.seed.domain.SWCharacter;
import lombok.Builder;
import lombok.Value;
import lombok.With;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;

@Component
@Slf4j
public class CreateSWCharacterUseCase implements PostSWCharacter {
    private final Executor executor;
    private final SWCharacterDBRepository characterDBRepository;

    public CreateSWCharacterUseCase(
            final @Qualifier("asyncExecutor") Executor executor,
            final SWCharacterDBRepository characterDBRepository) {
        this.executor = executor;
        this.characterDBRepository = characterDBRepository;
    }

    @Override
    public CompletionStage<SWCharacter> execute(SWCharacter request) {
        return CompletableFuture.supplyAsync(()-> create(request), executor)
                .thenApply(this::search)
                .thenApply( result -> {
                    log.info("xd");
                    return result.getCharacter();
                });
    }

    private Request create(SWCharacter request){
       Long id = characterDBRepository.create(request);
       return Request.builder()
               .id(id)
               .build();
    }

    private Request search(Request request){
        return request.withCharacter(characterDBRepository.getById(request.getId()));
    }

    @Value
    @Builder
    @With
    private static class Request{
        SWCharacter character;
        Long id;
    }
}

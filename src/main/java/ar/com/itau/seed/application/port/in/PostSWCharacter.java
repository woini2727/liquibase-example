package ar.com.itau.seed.application.port.in;

import ar.com.itau.seed.domain.SWCharacter;

import java.util.concurrent.CompletionStage;

public interface PostSWCharacter {
    CompletionStage<SWCharacter> execute(SWCharacter request);
}

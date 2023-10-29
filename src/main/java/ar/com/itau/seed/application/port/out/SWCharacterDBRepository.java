package ar.com.itau.seed.application.port.out;

import ar.com.itau.seed.domain.SWCharacter;

public interface SWCharacterDBRepository {

    Long create(SWCharacter request);

    SWCharacter getById(Long id);
}

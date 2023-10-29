package ar.com.itau.seed.adapter.db;

import ar.com.itau.seed.adapter.db.model.CharacterDBModel;
import ar.com.itau.seed.application.port.out.SWCharacterDBRepository;
import ar.com.itau.seed.config.ErrorCode;
import ar.com.itau.seed.config.exception.NotFoundException;
import ar.com.itau.seed.domain.SWCharacter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Repository
@Slf4j
@RequiredArgsConstructor
public class CharacterDbAdapter implements SWCharacterDBRepository {

    private final CharacterPersistenceRepository repository;

    @Override
    public Long create(SWCharacter request) {
        final CharacterDBModel createModel = CharacterDBModel.from(request);
        final CharacterDBModel model = repository.save(createModel);
        return model.getId();
    }

    @Override
    public SWCharacter getById(Long id) {
       return repository.findById(id).map( model ->
       {
           log.info("Got payment {} with id {}", model, id);
           return model.toDomain();
       }).orElseThrow(
               () -> new NotFoundException(ErrorCode.CHARACTER_NOT_FOUND));
    }
}

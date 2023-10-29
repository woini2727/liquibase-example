package ar.com.itau.seed.adapter.db;

import ar.com.itau.seed.adapter.db.model.CharacterDBModel;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface CharacterPersistenceRepository extends PagingAndSortingRepository<CharacterDBModel,Long> {
}

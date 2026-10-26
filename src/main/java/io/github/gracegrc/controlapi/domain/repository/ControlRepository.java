package io.github.gracegrc.controlapi.domain.repository;

import io.github.gracegrc.controlapi.domain.model.entity.ControlEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.repository.ListCrudRepository;

public interface ControlRepository extends ListCrudRepository<ControlEntity, UUID> {

	List<ControlEntity> findByGroupId(UUID groupId);

	List<ControlEntity> findByParentControlId(UUID parentControlId);

	List<ControlEntity> findByGroupIdAndParentControlId(UUID groupId, UUID parentControlId);

}

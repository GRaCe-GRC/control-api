package io.github.gracegrc.controlapi.domain.repository;

import io.github.gracegrc.controlapi.domain.model.entity.ControlGroupEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.repository.ListCrudRepository;

public interface ControlGroupRepository extends ListCrudRepository<ControlGroupEntity, UUID> {

	List<ControlGroupEntity> findByParentGroupId(UUID parentGroupId);

}

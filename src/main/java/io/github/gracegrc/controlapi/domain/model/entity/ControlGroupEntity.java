package io.github.gracegrc.controlapi.domain.model.entity;

import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.InsertOnlyProperty;
import org.springframework.data.relational.core.mapping.Table;

@Table("control_groups")
public record ControlGroupEntity(
		@Id UUID id,
		UUID parentGroupId,
		String groupRef,
		String title,
		@InsertOnlyProperty Instant createdAt,
		@InsertOnlyProperty String createdBy) {
}

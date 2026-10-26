package io.github.gracegrc.controlapi.domain.model.entity;

import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.InsertOnlyProperty;
import org.springframework.data.relational.core.mapping.Table;

@Table("controls")
public record ControlEntity(
		@Id UUID id,
		UUID groupId,
		UUID parentControlId,
		String controlRef,
		String title,
		String statement,
		@InsertOnlyProperty Instant createdAt,
		@InsertOnlyProperty String createdBy) {
}

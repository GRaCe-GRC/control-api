package io.github.gracegrc.controlapi.domain.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;

import io.github.gracegrc.controlapi.domain.model.dto.ControlGroupRequestDto;
import io.github.gracegrc.controlapi.domain.model.dto.ControlGroupResponseDto;
import io.github.gracegrc.controlapi.domain.model.dto.ControlGroupUpdateRequestDto;
import io.github.gracegrc.controlapi.domain.model.exception.ControlGroupAlreadyExistsException;
import io.github.gracegrc.controlapi.domain.model.exception.ControlGroupInUseException;
import io.github.gracegrc.controlapi.domain.model.exception.ControlGroupNotFoundException;
import io.github.gracegrc.controlapi.domain.service.ControlGroupService;
import io.github.gracegrc.controlapi.domain.service.ControlService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

@WebMvcTest(ControlApiController.class)
class ControlApiControllerTest {

	private static final UUID ID = UUID.fromString("5b1e8c2a-3d4f-4a6b-8c9d-0e1f2a3b4c5d");

	private static final UUID PARENT_ID = UUID.fromString("9a8b7c6d-5e4f-4a3b-9c2d-1e0f9a8b7c6d");

	private static final ControlGroupResponseDto GROUP = new ControlGroupResponseDto(ID, null, "AC", "Access Control",
			Instant.parse("2026-10-01T10:15:30Z"), "admin-console");

	@Autowired
	private MockMvcTester mvc;

	@MockitoBean
	private ControlGroupService controlGroupService;

	@MockitoBean
	private ControlService controlService;

	@Test
	void postGroupCreatesTheGroupAndReturnsAnEmptyBody() {
		given(controlGroupService.create(new ControlGroupRequestDto(PARENT_ID, "AC", "Access Control", "admin-console")))
			.willReturn(ID);

		assertThat(mvc.post()
			.uri("/api/v1/control-groups")
			.contentType(MediaType.APPLICATION_JSON)
			.content("{\"parentGroupId\":\"" + PARENT_ID
					+ "\",\"groupRef\":\"AC\",\"title\":\"Access Control\",\"createdBy\":\"admin-console\"}"))
			.hasStatus(HttpStatus.CREATED)
			.hasHeader("Location", "http://localhost/api/v1/control-groups/" + ID)
			.body()
			.isEmpty();
	}

	@Test
	void postGroupWithInvalidFieldsReturnsBadRequestWithTheErrors() {
		assertThat(mvc.post()
			.uri("/api/v1/control-groups")
			.contentType(MediaType.APPLICATION_JSON)
			.content("{\"groupRef\":\"\",\"title\":\"Access Control\"}"))
			.hasStatus(HttpStatus.BAD_REQUEST)
			.bodyJson()
			.isLenientlyEqualTo("{\"status\":400,\"errors\":{\"groupRef\":\"must not be blank\","
					+ "\"createdBy\":\"must not be blank\"}}");
		then(controlGroupService).shouldHaveNoInteractions();
	}

	@Test
	void postGroupWithADuplicateRefReturnsConflict() {
		given(controlGroupService.create(any())).willThrow(new ControlGroupAlreadyExistsException("AC"));

		assertThat(mvc.post()
			.uri("/api/v1/control-groups")
			.contentType(MediaType.APPLICATION_JSON)
			.content("{\"groupRef\":\"AC\",\"title\":\"Access Control\",\"createdBy\":\"admin-console\"}"))
			.hasStatus(HttpStatus.CONFLICT)
			.bodyJson()
			.isLenientlyEqualTo("{\"status\":409,\"detail\":\"A control group with ref 'AC' already exists\"}");
	}

	@Test
	void postGroupWithAnUnknownParentReturnsNotFound() {
		given(controlGroupService.create(any())).willThrow(new ControlGroupNotFoundException(PARENT_ID));

		assertThat(mvc.post()
			.uri("/api/v1/control-groups")
			.contentType(MediaType.APPLICATION_JSON)
			.content("{\"parentGroupId\":\"" + PARENT_ID
					+ "\",\"groupRef\":\"AC\",\"title\":\"Access Control\",\"createdBy\":\"admin-console\"}"))
			.hasStatus(HttpStatus.NOT_FOUND)
			.bodyJson()
			.isLenientlyEqualTo("{\"status\":404,\"detail\":\"Control group " + PARENT_ID + " not found\"}");
	}

	@Test
	void getGroupsReturnsTheList() {
		given(controlGroupService.findAll(null)).willReturn(List.of(GROUP));

		assertThat(mvc.get().uri("/api/v1/control-groups")).hasStatusOk()
			.bodyJson()
			.isLenientlyEqualTo("[{\"id\":\"" + ID + "\",\"groupRef\":\"AC\"}]");
	}

	@Test
	void getGroupsWithParentIdPassesItToTheService() {
		given(controlGroupService.findAll(PARENT_ID)).willReturn(List.of());

		assertThat(mvc.get().uri("/api/v1/control-groups?parentId={id}", PARENT_ID)).hasStatusOk()
			.bodyJson()
			.isLenientlyEqualTo("[]");
	}

	@Test
	void getGroupsWithAMalformedParentIdReturnsBadRequest() {
		assertThat(mvc.get().uri("/api/v1/control-groups?parentId=not-a-uuid")).hasStatus(HttpStatus.BAD_REQUEST);
		then(controlGroupService).shouldHaveNoInteractions();
	}

	@Test
	void getGroupByIdReturnsTheGroup() {
		given(controlGroupService.findById(ID)).willReturn(GROUP);

		assertThat(mvc.get().uri("/api/v1/control-groups/{id}", ID)).hasStatusOk()
			.bodyJson()
			.isLenientlyEqualTo("{\"id\":\"" + ID + "\",\"parentGroupId\":null,\"groupRef\":\"AC\"}");
	}

	@Test
	void getGroupByIdWithAnUnknownIdReturnsNotFound() {
		given(controlGroupService.findById(ID)).willThrow(new ControlGroupNotFoundException(ID));

		assertThat(mvc.get().uri("/api/v1/control-groups/{id}", ID)).hasStatus(HttpStatus.NOT_FOUND)
			.bodyJson()
			.isLenientlyEqualTo("{\"status\":404,\"detail\":\"Control group " + ID + " not found\"}");
	}

	@Test
	void getGroupByIdWithAMalformedIdReturnsBadRequest() {
		assertThat(mvc.get().uri("/api/v1/control-groups/not-a-uuid")).hasStatus(HttpStatus.BAD_REQUEST);
		then(controlGroupService).shouldHaveNoInteractions();
	}

	@Test
	void putGroupUpdatesTheGroup() {
		ControlGroupResponseDto updated = new ControlGroupResponseDto(ID, PARENT_ID, "AC", "Access",
				GROUP.createdAt(), "admin-console");
		given(controlGroupService.update(ID, new ControlGroupUpdateRequestDto(PARENT_ID, "AC", "Access")))
			.willReturn(updated);

		assertThat(mvc.put()
			.uri("/api/v1/control-groups/{id}", ID)
			.contentType(MediaType.APPLICATION_JSON)
			.content("{\"parentGroupId\":\"" + PARENT_ID + "\",\"groupRef\":\"AC\",\"title\":\"Access\"}"))
			.hasStatusOk()
			.bodyJson()
			.isLenientlyEqualTo("{\"id\":\"" + ID + "\",\"parentGroupId\":\"" + PARENT_ID + "\",\"title\":\"Access\"}");
	}

	@Test
	void putGroupWithoutTitleReturnsBadRequest() {
		assertThat(mvc.put()
			.uri("/api/v1/control-groups/{id}", ID)
			.contentType(MediaType.APPLICATION_JSON)
			.content("{\"groupRef\":\"AC\"}"))
			.hasStatus(HttpStatus.BAD_REQUEST)
			.bodyJson()
			.isLenientlyEqualTo("{\"errors\":{\"title\":\"must not be blank\"}}");
		then(controlGroupService).shouldHaveNoInteractions();
	}

	@Test
	void putGroupWithAnUnknownIdReturnsNotFound() {
		given(controlGroupService.update(eq(ID), any())).willThrow(new ControlGroupNotFoundException(ID));

		assertThat(mvc.put()
			.uri("/api/v1/control-groups/{id}", ID)
			.contentType(MediaType.APPLICATION_JSON)
			.content("{\"groupRef\":\"AC\",\"title\":\"Access Control\"}"))
			.hasStatus(HttpStatus.NOT_FOUND);
	}

	@Test
	void deleteGroupReturnsNoContent() {
		assertThat(mvc.delete().uri("/api/v1/control-groups/{id}", ID)).hasStatus(HttpStatus.NO_CONTENT);
		then(controlGroupService).should().delete(ID);
	}

	@Test
	void deleteGroupWithAnUnknownIdReturnsNotFound() {
		willThrow(new ControlGroupNotFoundException(ID)).given(controlGroupService).delete(ID);

		assertThat(mvc.delete().uri("/api/v1/control-groups/{id}", ID)).hasStatus(HttpStatus.NOT_FOUND);
	}

	@Test
	void deleteGroupWithChildrenReturnsConflict() {
		willThrow(new ControlGroupInUseException(ID)).given(controlGroupService).delete(ID);

		assertThat(mvc.delete().uri("/api/v1/control-groups/{id}", ID)).hasStatus(HttpStatus.CONFLICT)
			.bodyJson()
			.isLenientlyEqualTo(
					"{\"status\":409,\"detail\":\"Control group " + ID + " still has child groups or controls\"}");
	}

}

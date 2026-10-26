package io.github.gracegrc.controlapi.domain.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;

import io.github.gracegrc.controlapi.domain.model.dto.ControlRequestDto;
import io.github.gracegrc.controlapi.domain.model.dto.ControlResponseDto;
import io.github.gracegrc.controlapi.domain.model.dto.ControlUpdateRequestDto;
import io.github.gracegrc.controlapi.domain.model.exception.ControlAlreadyExistsException;
import io.github.gracegrc.controlapi.domain.model.exception.ControlGroupInUseException;
import io.github.gracegrc.controlapi.domain.model.exception.ControlGroupNotFoundException;
import io.github.gracegrc.controlapi.domain.model.exception.ControlInUseException;
import io.github.gracegrc.controlapi.domain.model.exception.ControlNotFoundException;
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
class ControlApiControllerControlsTest {

	private static final UUID ID = UUID.fromString("7c3d2e1f-0a9b-4c8d-8e7f-6a5b4c3d2e1f");

	private static final UUID GROUP_ID = UUID.fromString("5b1e8c2a-3d4f-4a6b-8c9d-0e1f2a3b4c5d");

	private static final UUID PARENT_ID = UUID.fromString("2d4f6a8c-1b3d-4e5f-9a7b-8c9d0e1f2a3b");

	private static final ControlResponseDto CONTROL = new ControlResponseDto(ID, GROUP_ID, null, "AC-2",
			"Account Management", "Manage accounts.", Instant.parse("2026-10-01T10:15:30Z"), "admin-console");

	@Autowired
	private MockMvcTester mvc;

	@MockitoBean
	private ControlGroupService controlGroupService;

	@MockitoBean
	private ControlService controlService;

	@Test
	void postControlCreatesTheControlAndReturnsAnEmptyBody() {
		given(controlService.create(new ControlRequestDto(GROUP_ID, null, "AC-2", "Account Management",
				"Manage accounts.", "admin-console")))
			.willReturn(ID);

		assertThat(mvc.post()
			.uri("/api/v1/controls")
			.contentType(MediaType.APPLICATION_JSON)
			.content("{\"groupId\":\"" + GROUP_ID + "\",\"controlRef\":\"AC-2\",\"title\":\"Account Management\","
					+ "\"statement\":\"Manage accounts.\",\"createdBy\":\"admin-console\"}"))
			.hasStatus(HttpStatus.CREATED)
			.hasHeader("Location", "http://localhost/api/v1/controls/" + ID)
			.body()
			.isEmpty();
	}

	@Test
	void postControlWithInvalidFieldsReturnsBadRequestWithTheErrors() {
		assertThat(mvc.post()
			.uri("/api/v1/controls")
			.contentType(MediaType.APPLICATION_JSON)
			.content("{\"controlRef\":\"" + "A".repeat(256) + "\",\"title\":\"\",\"createdBy\":\"admin-console\"}"))
			.hasStatus(HttpStatus.BAD_REQUEST)
			.bodyJson()
			.isLenientlyEqualTo("{\"status\":400,\"errors\":{\"controlRef\":\"size must be between 0 and 255\","
					+ "\"title\":\"must not be blank\"}}");
		then(controlService).shouldHaveNoInteractions();
	}

	@Test
	void postControlWithADuplicateRefReturnsConflict() {
		given(controlService.create(any())).willThrow(new ControlAlreadyExistsException("AC-2"));

		assertThat(mvc.post()
			.uri("/api/v1/controls")
			.contentType(MediaType.APPLICATION_JSON)
			.content("{\"controlRef\":\"AC-2\",\"title\":\"Account Management\",\"createdBy\":\"admin-console\"}"))
			.hasStatus(HttpStatus.CONFLICT)
			.bodyJson()
			.isLenientlyEqualTo("{\"status\":409,\"detail\":\"A control with ref 'AC-2' already exists\"}");
	}

	@Test
	void postControlWithAnUnknownGroupReturnsNotFound() {
		given(controlService.create(any())).willThrow(new ControlGroupNotFoundException(GROUP_ID));

		assertThat(mvc.post()
			.uri("/api/v1/controls")
			.contentType(MediaType.APPLICATION_JSON)
			.content("{\"groupId\":\"" + GROUP_ID
					+ "\",\"controlRef\":\"AC-2\",\"title\":\"Account Management\",\"createdBy\":\"admin-console\"}"))
			.hasStatus(HttpStatus.NOT_FOUND)
			.bodyJson()
			.isLenientlyEqualTo("{\"status\":404,\"detail\":\"Control group " + GROUP_ID + " not found\"}");
	}

	@Test
	void getControlsReturnsTheList() {
		given(controlService.findAll(null, null)).willReturn(List.of(CONTROL));

		assertThat(mvc.get().uri("/api/v1/controls")).hasStatusOk()
			.bodyJson()
			.isLenientlyEqualTo("[{\"id\":\"" + ID + "\",\"controlRef\":\"AC-2\"}]");
	}

	@Test
	void getControlsWithGroupIdAndParentIdPassesBothToTheService() {
		given(controlService.findAll(GROUP_ID, PARENT_ID)).willReturn(List.of());

		assertThat(mvc.get().uri("/api/v1/controls?groupId={g}&parentId={p}", GROUP_ID, PARENT_ID)).hasStatusOk()
			.bodyJson()
			.isLenientlyEqualTo("[]");
	}

	@Test
	void getControlsWithAMalformedGroupIdReturnsBadRequest() {
		assertThat(mvc.get().uri("/api/v1/controls?groupId=nope")).hasStatus(HttpStatus.BAD_REQUEST);
		then(controlService).shouldHaveNoInteractions();
	}

	@Test
	void getControlByIdReturnsTheControl() {
		given(controlService.findById(ID)).willReturn(CONTROL);

		assertThat(mvc.get().uri("/api/v1/controls/{id}", ID)).hasStatusOk()
			.bodyJson()
			.isLenientlyEqualTo("{\"id\":\"" + ID + "\",\"groupId\":\"" + GROUP_ID
					+ "\",\"parentControlId\":null,\"statement\":\"Manage accounts.\"}");
	}

	@Test
	void getControlByIdWithAnUnknownIdReturnsNotFound() {
		given(controlService.findById(ID)).willThrow(new ControlNotFoundException(ID));

		assertThat(mvc.get().uri("/api/v1/controls/{id}", ID)).hasStatus(HttpStatus.NOT_FOUND)
			.bodyJson()
			.isLenientlyEqualTo("{\"status\":404,\"detail\":\"Control " + ID + " not found\"}");
	}

	@Test
	void putControlUpdatesTheControl() {
		ControlResponseDto updated = new ControlResponseDto(ID, GROUP_ID, PARENT_ID, "AC-2(1)", "Automated", null,
				CONTROL.createdAt(), "admin-console");
		given(controlService.update(ID, new ControlUpdateRequestDto(GROUP_ID, PARENT_ID, "AC-2(1)", "Automated", null)))
			.willReturn(updated);

		assertThat(mvc.put()
			.uri("/api/v1/controls/{id}", ID)
			.contentType(MediaType.APPLICATION_JSON)
			.content("{\"groupId\":\"" + GROUP_ID + "\",\"parentControlId\":\"" + PARENT_ID
					+ "\",\"controlRef\":\"AC-2(1)\",\"title\":\"Automated\"}"))
			.hasStatusOk()
			.bodyJson()
			.isLenientlyEqualTo("{\"id\":\"" + ID + "\",\"parentControlId\":\"" + PARENT_ID + "\"}");
	}

	@Test
	void putControlWithoutControlRefReturnsBadRequest() {
		assertThat(mvc.put()
			.uri("/api/v1/controls/{id}", ID)
			.contentType(MediaType.APPLICATION_JSON)
			.content("{\"title\":\"Account Management\"}"))
			.hasStatus(HttpStatus.BAD_REQUEST)
			.bodyJson()
			.isLenientlyEqualTo("{\"errors\":{\"controlRef\":\"must not be blank\"}}");
		then(controlService).shouldHaveNoInteractions();
	}

	@Test
	void putControlWithAnUnknownIdReturnsNotFound() {
		given(controlService.update(eq(ID), any())).willThrow(new ControlNotFoundException(ID));

		assertThat(mvc.put()
			.uri("/api/v1/controls/{id}", ID)
			.contentType(MediaType.APPLICATION_JSON)
			.content("{\"controlRef\":\"AC-2\",\"title\":\"Account Management\"}"))
			.hasStatus(HttpStatus.NOT_FOUND);
	}

	@Test
	void deleteControlReturnsNoContent() {
		assertThat(mvc.delete().uri("/api/v1/controls/{id}", ID)).hasStatus(HttpStatus.NO_CONTENT);
		then(controlService).should().delete(ID);
	}

	@Test
	void deleteControlWithEnhancementsReturnsConflict() {
		willThrow(new ControlInUseException(ID)).given(controlService).delete(ID);

		assertThat(mvc.delete().uri("/api/v1/controls/{id}", ID)).hasStatus(HttpStatus.CONFLICT)
			.bodyJson()
			.isLenientlyEqualTo("{\"status\":409,\"detail\":\"Control " + ID + " still has enhancements\"}");
	}

	@Test
	void deleteGroupWithControlsReturnsConflict() {
		willThrow(new ControlGroupInUseException(GROUP_ID)).given(controlGroupService).delete(GROUP_ID);

		assertThat(mvc.delete().uri("/api/v1/control-groups/{id}", GROUP_ID)).hasStatus(HttpStatus.CONFLICT)
			.bodyJson()
			.isLenientlyEqualTo("{\"status\":409,\"detail\":\"Control group " + GROUP_ID
					+ " still has child groups or controls\"}");
	}

}

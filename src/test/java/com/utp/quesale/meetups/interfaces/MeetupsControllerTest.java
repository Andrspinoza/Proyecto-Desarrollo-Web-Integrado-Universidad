package com.utp.quesale.meetups.interfaces;

import com.utp.quesale.meetups.application.commands.CreateMeetupCommand;
import com.utp.quesale.meetups.application.dtos.MeetupDto;
import com.utp.quesale.meetups.application.dtos.ParticipationDto;
import com.utp.quesale.meetups.application.handlers.commands.MeetupCommandHandler;
import com.utp.quesale.meetups.application.handlers.queries.GetMeetupQueryHandler;
import com.utp.quesale.meetups.application.handlers.queries.ListMeetupsQueryHandler;
import com.utp.quesale.meetups.application.handlers.queries.ListMyMeetupsQueryHandler;
import com.utp.quesale.meetups.application.handlers.queries.ListParticipationsQueryHandler;
import com.utp.quesale.meetups.application.queries.ListMeetupsQuery;
import com.utp.quesale.meetups.domain.valueobjects.MeetupStatus;
import com.utp.quesale.meetups.domain.valueobjects.MeetupType;
import com.utp.quesale.meetups.domain.valueobjects.MeetupVisibility;
import com.utp.quesale.meetups.domain.valueobjects.ParticipationStatus;
import com.utp.quesale.meetups.interfaces.controllers.MeetupsController;
import com.utp.quesale.shared.interfaces.rest.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MeetupsControllerTest {

    private MeetupCommandHandler commandHandler;
    private GetMeetupQueryHandler getMeetupHandler;
    private ListMeetupsQueryHandler listMeetupsHandler;
    private ListParticipationsQueryHandler listParticipationsHandler;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        commandHandler = mock(MeetupCommandHandler.class);
        getMeetupHandler = mock(GetMeetupQueryHandler.class);
        listMeetupsHandler = mock(ListMeetupsQueryHandler.class);
        ListMyMeetupsQueryHandler listMyMeetupsHandler = mock(ListMyMeetupsQueryHandler.class);
        listParticipationsHandler = mock(ListParticipationsQueryHandler.class);

        MeetupsController controller = new MeetupsController(
                commandHandler,
                getMeetupHandler,
                listMeetupsHandler,
                listMyMeetupsHandler,
                listParticipationsHandler);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void createsMeetupAndReturnsItsLocation() throws Exception {
        when(commandHandler.handle(any(CreateMeetupCommand.class))).thenReturn(meetupDto());

        mockMvc.perform(post("/api/v1/meetups")
                        .header("X-Student-Id", 7)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Repaso de algoritmos",
                                  "description": "Repaso antes del examen",
                                  "type": "ACADEMIC",
                                  "visibility": "PUBLIC",
                                  "scheduledAt": "2030-11-01T10:00:00",
                                  "place": {
                                    "name": "Biblioteca",
                                    "address": "Av. Universitaria 123",
                                    "district": "Los Olivos"
                                  },
                                  "capacity": 10
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/meetups/12"))
                .andExpect(jsonPath("$.id").value(12))
                .andExpect(jsonPath("$.title").value("Repaso de algoritmos"));
    }

    @Test
    void listsPublicMeetupsUsingTheRequestedFilters() throws Exception {
        when(listMeetupsHandler.handle(any(ListMeetupsQuery.class))).thenReturn(List.of(meetupDto()));

        mockMvc.perform(get("/api/v1/meetups")
                        .header("X-Student-Id", 7)
                        .param("type", "ACADEMIC")
                        .param("status", "SCHEDULED")
                        .param("from", "2030-11-01T00:00:00")
                        .param("to", "2030-11-30T23:59:59"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(12));

        ArgumentCaptor<ListMeetupsQuery> queryCaptor = ArgumentCaptor.forClass(ListMeetupsQuery.class);
        verify(listMeetupsHandler).handle(queryCaptor.capture());
        assertEquals(new ListMeetupsQuery(
                        7L,
                        MeetupType.ACADEMIC,
                        MeetupStatus.SCHEDULED,
                        LocalDateTime.parse("2030-11-01T00:00:00"),
                        LocalDateTime.parse("2030-11-30T23:59:59")),
                queryCaptor.getValue());
    }

    @Test
    void getsMeetupById() throws Exception {
        when(getMeetupHandler.handle(any())).thenReturn(meetupDto());

        mockMvc.perform(get("/api/v1/meetups/12").header("X-Student-Id", 7))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(12))
                .andExpect(jsonPath("$.place.name").value("Biblioteca"));
    }

    @Test
    void listsMeetupParticipations() throws Exception {
        when(listParticipationsHandler.handle(any())).thenReturn(List.of(participationDto()));

        mockMvc.perform(get("/api/v1/meetups/12/participations").header("X-Student-Id", 7))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].studentId").value(8))
                .andExpect(jsonPath("$[0].status").value("CONFIRMED"));
    }

    private MeetupDto meetupDto() {
        return new MeetupDto(
                12L,
                7L,
                3L,
                "Repaso de algoritmos",
                "Repaso antes del examen",
                MeetupType.ACADEMIC,
                MeetupVisibility.PUBLIC,
                LocalDateTime.parse("2030-11-01T10:00:00"),
                "Biblioteca",
                "Av. Universitaria 123",
                "Los Olivos",
                10,
                MeetupStatus.SCHEDULED,
                9,
                List.of(participationDto()));
    }

    private ParticipationDto participationDto() {
        return new ParticipationDto(20L, 8L, ParticipationStatus.CONFIRMED, Instant.parse("2030-10-01T12:00:00Z"));
    }
}

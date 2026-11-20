package com.utp.quesale.calls.application;

import com.utp.quesale.calls.application.commands.RelayCallSignalCommand;
import com.utp.quesale.calls.application.handlers.commands.CallSignalCommandHandler;
import com.utp.quesale.calls.domain.exceptions.CallBlockedException;
import com.utp.quesale.calls.domain.exceptions.CallEmailNotVerifiedException;
import com.utp.quesale.calls.domain.exceptions.StudentsNotConnectedForCallException;
import com.utp.quesale.calls.domain.services.CallSignalPublisher;
import com.utp.quesale.calls.domain.valueobjects.CallSignal;
import com.utp.quesale.calls.domain.valueobjects.CallSignalType;
import com.utp.quesale.connections.interfaces.acl.ConnectionsContextFacade;
import com.utp.quesale.iam.interfaces.acl.IamContextFacade;
import com.utp.quesale.moderation.interfaces.acl.ModerationContextFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CallSignalCommandHandlerTest {

    private static final Instant NOW = Instant.parse("2026-11-18T12:00:00Z");

    private ConnectionsContextFacade connections;
    private ModerationContextFacade moderation;
    private IamContextFacade iam;
    private CallSignalPublisher publisher;
    private CallSignalCommandHandler handler;

    @BeforeEach
    void setUp() {
        connections = mock(ConnectionsContextFacade.class);
        moderation = mock(ModerationContextFacade.class);
        iam = mock(IamContextFacade.class);
        publisher = mock(CallSignalPublisher.class);
        handler = new CallSignalCommandHandler(connections, moderation, iam, publisher,
                Clock.fixed(NOW, ZoneOffset.UTC));
        when(connections.areConnected(1L, 2L)).thenReturn(true);
        when(moderation.isBlocked(1L, 2L)).thenReturn(false);
    }

    private RelayCallSignalCommand command(String type) {
        return new RelayCallSignalCommand(1L, 2L, type, "{\"sdp\":\"x\"}");
    }

    @Test
    @DisplayName("Entre estudiantes conectados y sin bloqueo la señal se entrega al destinatario")
    void relaysBetweenConnectedStudents() {
        handler.handle(command("offer"));

        ArgumentCaptor<CallSignal> captor = ArgumentCaptor.forClass(CallSignal.class);
        verify(publisher).publish(captor.capture());
        CallSignal signal = captor.getValue();
        assertEquals(1L, signal.fromStudentId());
        assertEquals(2L, signal.toStudentId());
        assertEquals(CallSignalType.OFFER, signal.type());
        assertEquals(NOW, signal.sentAt());
    }

    @Test
    @DisplayName("Sin una conexión aceptada no se puede llamar")
    void notConnectedIsRejected() {
        when(connections.areConnected(1L, 2L)).thenReturn(false);

        assertThrows(StudentsNotConnectedForCallException.class, () -> handler.handle(command("offer")));
        verify(publisher, never()).publish(any());
    }

    @Test
    @DisplayName("Con un bloqueo no se puede llamar")
    void blockedIsRejected() {
        when(moderation.isBlocked(1L, 2L)).thenReturn(true);

        assertThrows(CallBlockedException.class, () -> handler.handle(command("ice-candidate")));
        verify(publisher, never()).publish(any());
    }

    @Test
    @DisplayName("Colgar siempre se entrega, aunque ya exista un bloqueo")
    void hangupAlwaysGoesThrough() {
        when(connections.areConnected(1L, 2L)).thenReturn(false);
        when(moderation.isBlocked(1L, 2L)).thenReturn(true);

        handler.handle(command("hangup"));
        handler.handle(command("reject"));

        verify(publisher, times(2)).publish(any());
    }

    @Test
    @DisplayName("Si se exige verificar el correo, un estudiante sin verificar no puede llamar")
    void unverifiedCallerIsRejected() {
        when(iam.isEmailVerificationRequired()).thenReturn(true);
        when(iam.isEmailVerified(1L)).thenReturn(false);

        assertThrows(CallEmailNotVerifiedException.class, () -> handler.handle(command("offer")));
        verify(publisher, never()).publish(any());
    }

    @Test
    @DisplayName("Si se exige verificar el correo, un estudiante verificado sí puede llamar")
    void verifiedCallerPasses() {
        when(iam.isEmailVerificationRequired()).thenReturn(true);
        when(iam.isEmailVerified(1L)).thenReturn(true);

        handler.handle(command("offer"));

        verify(publisher).publish(any());
    }

    @Test
    @DisplayName("Un tipo desconocido o llamarse a sí mismo es una petición inválida")
    void invalidSignalsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> handler.handle(command("grito")));
        assertThrows(IllegalArgumentException.class,
                () -> handler.handle(new RelayCallSignalCommand(1L, 1L, "offer", null)));
        verify(publisher, never()).publish(any());
    }
}

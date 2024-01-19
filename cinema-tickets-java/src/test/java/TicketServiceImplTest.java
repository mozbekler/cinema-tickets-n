import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.TicketServiceImpl;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class TicketServiceImplTest {

    @Mock
    private TicketPaymentService ticketPaymentService;

    @Mock
    private SeatReservationService seatReservationService;

    private TicketServiceImpl ticketService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        ticketService = new TicketServiceImpl(ticketPaymentService, seatReservationService);
    }

    @Test
    void purchaseTickets_validPurchase_success() throws InvalidPurchaseException {
        TicketTypeRequest ticketTypeRequest = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 2);
        TicketTypeRequest[] ticketTypeRequests = {ticketTypeRequest};

        ticketService.purchaseTickets(123L, ticketTypeRequests);

        verify(ticketPaymentService, times(1)).makePayment(123L, 40);
        verify(seatReservationService, times(1)).reserveSeat(123L, 2);
    }

    @Test
    void purchaseTickets_validPurchase_success2() throws InvalidPurchaseException {

        TicketTypeRequest ticketTypeRequest = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1);
        TicketTypeRequest ticketTypeRequest2 = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 5);
        TicketTypeRequest ticketTypeRequest3 = new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1);
        TicketTypeRequest[] ticketTypeRequests = {ticketTypeRequest,ticketTypeRequest2,ticketTypeRequest3};

        ticketService.purchaseTickets(123L, ticketTypeRequests);

        verify(ticketPaymentService, times(1)).makePayment(123L, 70);
        verify(seatReservationService, times(1)).reserveSeat(123L, 6);
    }

    @Test
    void purchaseTickets_invalidAccountId_throwsException() {
        TicketTypeRequest ticketTypeRequest = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 2);
        TicketTypeRequest[] ticketTypeRequests = {ticketTypeRequest};

        Assertions.assertThrows(InvalidPurchaseException.class, () -> ticketService.purchaseTickets(null, ticketTypeRequests));

        Mockito.verifyNoInteractions(ticketPaymentService);
        Mockito.verifyNoInteractions(seatReservationService);
    }

    @Test
    void purchaseTickets_noAdultTickets_throwsException() {
        TicketTypeRequest ticketTypeRequest = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 2);
        TicketTypeRequest[] ticketTypeRequests = {ticketTypeRequest};

        Assertions.assertThrows(InvalidPurchaseException.class, () -> ticketService.purchaseTickets(123L, ticketTypeRequests));

        Mockito.verifyNoInteractions(ticketPaymentService);
        Mockito.verifyNoInteractions(seatReservationService);
    }

    @Test
    void purchaseTickets_exceedMaxTickets_throwsException() {
        TicketTypeRequest ticketTypeRequest = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 21);
        TicketTypeRequest[] ticketTypeRequests = {ticketTypeRequest};

        Assertions.assertThrows(InvalidPurchaseException.class, () -> ticketService.purchaseTickets(123L, ticketTypeRequests));

        Mockito.verifyNoInteractions(ticketPaymentService);
        Mockito.verifyNoInteractions(seatReservationService);
    }

    @Test
    void purchaseTickets_nullTicketTypeRequests_throwsException(){
        TicketTypeRequest[] ticketTypeRequests = null;

        Assertions.assertThrows(InvalidPurchaseException.class, () -> ticketService.purchaseTickets(123L, ticketTypeRequests));

        Mockito.verifyNoInteractions(ticketPaymentService);
        Mockito.verifyNoInteractions(seatReservationService);
    }

    @Test
    void purchaseTickets_someTicketTypeRequestsAreNull_throwsException(){
        TicketTypeRequest ticketTypeRequest = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1);
        TicketTypeRequest ticketTypeRequest2 = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 5);
        TicketTypeRequest ticketTypeRequest3 = null;
        TicketTypeRequest[] ticketTypeRequests = {ticketTypeRequest,ticketTypeRequest2,ticketTypeRequest3};

        Assertions.assertThrows(InvalidPurchaseException.class, () -> ticketService.purchaseTickets(123L, ticketTypeRequests));

        Mockito.verifyNoInteractions(ticketPaymentService);
        Mockito.verifyNoInteractions(seatReservationService);
    }

    @Test
    void infantTicketsAreMoreThanAdultTickets_throwsException(){
        TicketTypeRequest ticketTypeRequest = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1);
        TicketTypeRequest ticketTypeRequest2 = new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 5);
        TicketTypeRequest[] ticketTypeRequests = {ticketTypeRequest,ticketTypeRequest2,ticketTypeRequest2};

        Assertions.assertThrows(InvalidPurchaseException.class, () -> ticketService.purchaseTickets(123L, ticketTypeRequests));

        Mockito.verifyNoInteractions(ticketPaymentService);
        Mockito.verifyNoInteractions(seatReservationService);

    }

}

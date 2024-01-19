package uk.gov.dwp.uc.pairtest;

import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketPrice;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import java.util.Arrays;
import java.util.Objects;

public class TicketServiceImpl implements TicketService {
    /**
     * Should only have private methods other than the one below.
     */

    private final TicketPaymentService ticketPaymentService;
    private final SeatReservationService seatReservationService;

    public TicketServiceImpl(TicketPaymentService ticketPaymentService, SeatReservationService seatReservationService) {
        this.ticketPaymentService = ticketPaymentService;
        this.seatReservationService = seatReservationService;
    }

    @Override
    public void purchaseTickets(Long accountId, TicketTypeRequest... ticketTypeRequests) throws InvalidPurchaseException {
        int totalAmountToPay = 0;
        int totalSeatsToAllocate = 0;

        validatePurchase(accountId, ticketTypeRequests);

        for (TicketTypeRequest request : ticketTypeRequests) {
            totalAmountToPay += TicketPrice.getTicketPrice(request.getTicketType()) * request.getNoOfTickets();

            if (!request.getTicketType().equals(TicketTypeRequest.Type.INFANT)) {
                totalSeatsToAllocate += request.getNoOfTickets();
            }
        }

        ticketPaymentService.makePayment(accountId, totalAmountToPay);
        seatReservationService.reserveSeat(accountId, totalSeatsToAllocate);
    }

    private void validatePurchase(Long accountId, TicketTypeRequest[] ticketTypeRequests) {
        if (accountId == null || accountId <= 0) throw new InvalidPurchaseException("Invalid Account Id");

        if (ticketTypeRequests == null) throw new InvalidPurchaseException("Ticket Type Requests can not be null");

        anyNullTickets(ticketTypeRequests);

        anyAdultTickets(ticketTypeRequests);

        totalNoOdTicketsMoreThan20(ticketTypeRequests);

        infantsMoreThanAdults(ticketTypeRequests);


    }

    private static void anyNullTickets(TicketTypeRequest[] ticketTypeRequests) {
        boolean anyNullTickets = Arrays.stream(ticketTypeRequests)
                .anyMatch(Objects::isNull);

        if (anyNullTickets) throw new InvalidPurchaseException("Ticket Type Requests can not be null");
    }

    private static void anyAdultTickets(TicketTypeRequest[] ticketTypeRequests) {
        boolean anyAdultTickets = Arrays.stream(ticketTypeRequests)
                .map(TicketTypeRequest::getTicketType)
                .anyMatch(e -> e.equals(TicketTypeRequest.Type.ADULT));

        if (!anyAdultTickets)
            throw new InvalidPurchaseException("There must be at least one adult to purchase tickets");
    }

    private static void totalNoOdTicketsMoreThan20(TicketTypeRequest[] ticketTypeRequests) {
        int totalNoOfTickets = Arrays.stream(ticketTypeRequests).map(TicketTypeRequest::getNoOfTickets)
                .reduce(0, Integer::sum);

        if (totalNoOfTickets > 20)
            throw new InvalidPurchaseException("There can not be more than 20 tickets in a purchase");
    }

    private static void infantsMoreThanAdults(TicketTypeRequest[] ticketTypeRequests) {
        long countAdultTickets = Arrays.stream(ticketTypeRequests)
                .map(TicketTypeRequest::getTicketType)
                .filter(e -> e.equals(TicketTypeRequest.Type.ADULT))
                .count();
        long countInfantTickets = Arrays.stream(ticketTypeRequests)
                .map(TicketTypeRequest::getTicketType)
                .filter(e -> e.equals(TicketTypeRequest.Type.INFANT))
                .count();

        if (countInfantTickets > countAdultTickets)
            throw new InvalidPurchaseException("There can not be more infants than adults");
    }

}

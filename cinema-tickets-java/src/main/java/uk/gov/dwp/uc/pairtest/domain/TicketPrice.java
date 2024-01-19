package uk.gov.dwp.uc.pairtest.domain;

public class TicketPrice {


    private static final int ADULT_TICKET_PRICE = 20;
    private static final int CHILD_TICKET_PRICE = 10;
    private static final int INFANT_TICKET_PRICE = 0;

    public static int getTicketPrice(TicketTypeRequest.Type type){
        switch (type){
            case ADULT:
                return ADULT_TICKET_PRICE;
            case CHILD:
                return CHILD_TICKET_PRICE;
            case  INFANT:
                return INFANT_TICKET_PRICE;
        }
    return 0;
    }

}

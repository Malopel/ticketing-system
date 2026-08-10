package accordion_symphonic.ticketing.ticketcategory;

import accordion_symphonic.ticketing.availability.TicketAvailabilityService;
import accordion_symphonic.ticketing.concert.Concert;
import accordion_symphonic.ticketing.concert.ConcertNotFoundException;
import accordion_symphonic.ticketing.concert.ConcertRepository;
import accordion_symphonic.ticketing.concert.ConcertStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketCategoryServiceTest {

    @Mock
    private TicketCategoryRepository ticketCategoryRepository;

    @Mock
    private ConcertRepository concertRepository;

    @Mock
    private TicketAvailabilityService ticketAvailabilityService;

    private TicketCategoryService ticketCategoryService;

    @BeforeEach
    void setUp() {
        ticketCategoryService = new TicketCategoryService(
                ticketCategoryRepository,
                concertRepository,
                ticketAvailabilityService
        );
    }

    @Test
    void getCategoriesForPublishedConcertReturnsCategories() {
        Long concertId = 1L;

        Concert concert = new Concert(
                "Accordion Night",
                "Beschreibung",
                LocalDateTime.now().plusDays(30),
                "Heidelberg"
        );

        concert.publish();

        TicketCategory category = new TicketCategory(
                "Normalpreis",
                new BigDecimal("25.00"),
                100,
                concert
        );

        when(concertRepository.findByIdAndStatus(
                concertId,
                ConcertStatus.PUBLISHED
        )).thenReturn(Optional.of(concert));

        when(ticketCategoryRepository.findByConcertId(concertId))
                .thenReturn(List.of(category));

        when(ticketAvailabilityService.getAvailableTickets(
                category.getId(),
                category.getCapacity()
        )).thenReturn(80);

        List<TicketCategoryResponse> result =
                ticketCategoryService.getCategoriesForConcert(concertId);

        assertEquals(1, result.size());

        verify(ticketCategoryRepository)
                .findByConcertId(concertId);
    }

    @Test
    void getCategoriesForDraftConcertIsRejected() {
        Long concertId = 1L;

        when(concertRepository.findByIdAndStatus(
                concertId,
                ConcertStatus.PUBLISHED
        )).thenReturn(Optional.empty());

        assertThrows(
                ConcertNotFoundException.class,
                () -> ticketCategoryService.getCategoriesForConcert(concertId)
        );

        verify(ticketCategoryRepository, never())
                .findByConcertId(anyLong());

        verifyNoInteractions(ticketAvailabilityService);
    }

    @Test
    void getCategoriesForArchivedConcertIsRejected() {
        Long concertId = 1L;

        when(concertRepository.findByIdAndStatus(
                concertId,
                ConcertStatus.PUBLISHED
        )).thenReturn(Optional.empty());

        assertThrows(
                ConcertNotFoundException.class,
                () -> ticketCategoryService.getCategoriesForConcert(concertId)
        );

        verify(ticketCategoryRepository, never())
                .findByConcertId(anyLong());

        verifyNoInteractions(ticketAvailabilityService);
    }
}
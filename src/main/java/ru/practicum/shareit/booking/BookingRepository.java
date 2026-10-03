package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.booking.model.Booking;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByBookerIdOrderByStartDesc(Long userId);

    List<Booking> findByBookerIdAndStatusOrderByStartDesc(Long userId, BookingStatus status);

    @Query("select b from Booking b " +
            "where b.booker.id = :userId and b.start <= :now and b.end >= :now")
    List<Booking> findCurrentByBooker(@Param("userId") Long userId,
                                      @Param("now") LocalDateTime now);

    @Query("select b from Booking b " +
            "where b.booker.id = :userId and b.end < :now")
    List<Booking> findPastByBooker(@Param("userId") Long userId,
                                   @Param("now") LocalDateTime now);

    @Query("select b from Booking b " +
            "where b.booker.id = :userId and b.start > :now")
    List<Booking> findFutureByBooker(@Param("userId") Long userId,
                                     @Param("now") LocalDateTime now);

    @Query("select b from Booking b " +
            "where b.item.owner = :userId " +
            "order by b.start desc")
    List<Booking> findByOwnerId(@Param("userId") Long userId);

    @Query("select b from Booking b " +
            "where b.item.owner = :userId and b.status = :status " +
            "order by b.start desc")
    List<Booking> findByOwnerIdAndStatus(@Param("userId") Long userId,
                                         @Param("status") BookingStatus status);

    @Query("select b from Booking b " +
            "where b.item.owner = :userId and b.start <= :now and b.end >= :now")
    List<Booking> findCurrentByOwner(@Param("userId") Long userId,
                                     @Param("now") LocalDateTime now);

    @Query("select b from Booking b " +
            "where b.item.owner = :userId and b.end < :now")
    List<Booking> findPastByOwner(@Param("userId") Long userId,
                                  @Param("now") LocalDateTime now);

    @Query("select b from Booking b " +
            "where b.item.owner = :userId and b.start > :now")
    List<Booking> findFutureByOwner(@Param("userId") Long userId,
                                    @Param("now") LocalDateTime now);

    @Query("select b from Booking b " +
            "where b.item.id = :itemId and b.status = 'APPROVED' and b.end < :now " +
            "order by b.end desc")
    Optional<Booking> findLastApprovedByItem(@Param("itemId") Long itemId,
                                             @Param("now") LocalDateTime now);

    @Query("select b from Booking b " +
            "where b.item.id = :itemId and b.status = 'APPROVED' and b.start > :now " +
            "order by b.start asc")
    Optional<Booking> findNextApprovedByItem(@Param("itemId") Long itemId,
                                             @Param("now") LocalDateTime now);

    @Query("select count(b) > 0 from Booking b " +
            "where b.booker.id = :userId and b.item.id = :itemId and b.status = 'APPROVED' and b.end < :now")
    boolean existsApprovedBookingByBookerAndItemAndEndBefore(@Param("userId") Long userId,
                                                             @Param("itemId") Long itemId,
                                                             @Param("now") LocalDateTime now);

}

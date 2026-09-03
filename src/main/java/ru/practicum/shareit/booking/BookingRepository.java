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
            "where b.booker.id = ?1 and b.start <= ?2 and b.end >= ?2")
    List<Booking> findCurrentByBooker(Long userId, LocalDateTime now);

    @Query("select b from Booking b " +
            "where b.booker.id = ?1 and b.end < ?2")
    List<Booking> findPastByBooker(Long userId, LocalDateTime now);

    @Query("select b from Booking b " +
            "where b.booker.id = ?1 and b.start > ?2")
    List<Booking> findFutureByBooker(Long userId, LocalDateTime now);

    @Query("select b from Booking b " +
            "where b.item.owner = ?1 " +
            "order by b.start desc")
    List<Booking> findByOwnerId(Long userId);

    @Query("select b from Booking b " +
            "where b.item.owner = ?1 and b.status = ?2 " +
            "order by b.start desc")
    List<Booking> findByOwnerIdAndStatus(Long userId, BookingStatus status);

    @Query("select b from Booking b " +
            "where b.item.owner = ?1 and b.start <= ?2 and b.end >= ?2")
    List<Booking> findCurrentByOwner(Long userId, LocalDateTime now);

    @Query("select b from Booking b " +
            "where b.item.owner = ?1 and b.end < ?2")
    List<Booking> findPastByOwner(Long userId, LocalDateTime now);

    @Query("select b from Booking b " +
            "where b.item.owner = ?1 and b.start > ?2")
    List<Booking> findFutureByOwner(Long userId, LocalDateTime now);

    @Query("select b from Booking b " +
            "where b.item.id = ?1 and b.status = 'APPROVED' and b.end < ?2 " +
            "order by b.end desc")
    Optional<Booking> findLastApprovedByItem(Long itemId, LocalDateTime now);

    @Query("select b from Booking b " +
            "where b.item.id = ?1 and b.status = 'APPROVED' and b.start > ?2 " +
            "order by b.end desc")
    Optional<Booking> findNextApprovedByItem(Long itemId, LocalDateTime now);

    @Query("select count(b) > 0 from Booking b " +
            "where b.booker.id = :userId and b.item.id = :itemId and b.status = 'APPROVED' and b.end < :now")
    boolean existsApprovedBookingByBookerAndItemAndEndBefore(@Param("userId") Long userId,
                                                             @Param("itemId") Long itemId,
                                                             @Param("now") LocalDateTime now);

}

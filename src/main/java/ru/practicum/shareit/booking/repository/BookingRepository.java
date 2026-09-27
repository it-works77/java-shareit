package ru.practicum.shareit.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findAllByBookerIdAndStatusOrderByStartDesc(Long bookerId, BookingStatus status);

    List<Booking> findAllByBookerIdOrderByStartDesc(Long bookerId);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.booker.id = :bookerId AND b.status = :status " +
            "AND :now BETWEEN b.start AND b.end " +
            "ORDER BY b.start DESC")
    List<Booking> findAllCurrentByBookerIdAndStatus(@Param("bookerId") Long bookerId,
                                                    @Param("status") BookingStatus bookingStatus,
                                                    @Param("now") LocalDateTime now);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.booker.id = :bookerId AND b.status = :status " +
            "AND b.end < :now " +
            "ORDER BY b.start DESC")
    List<Booking> findAllPastByBookerIdAndStatus(@Param("bookerId") Long bookerId,
                                                 @Param("status") BookingStatus bookingStatus,
                                                 @Param("now") LocalDateTime now);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.booker.id = :bookerId AND b.status = :status " +
            "AND b.start > :now " +
            "ORDER BY b.start DESC")
    List<Booking> findAllFutureByBookerIdAndStatus(@Param("bookerId") Long bookerId,
                                                   @Param("status") BookingStatus bookingStatus,
                                                   @Param("now") LocalDateTime now);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.item.ownerId = :ownerId " +
            "ORDER BY b.start DESC")
    List<Booking> findAllByOwnerIdOrderByStartDesc(@Param("ownerId") Long ownerId);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.item.ownerId = :ownerId AND b.status = :status " +
            "AND :now BETWEEN b.start AND b.end " +
            "ORDER BY b.start DESC")
    List<Booking> findAllCurrentByOwnerIdAndStatus(@Param("ownerId") Long ownerId,
                                                   @Param("status") BookingStatus bookingStatus,
                                                   @Param("now") LocalDateTime now);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.item.ownerId = :ownerId AND b.status = :status " +
            "AND b.end < :now " +
            "ORDER BY b.start DESC")
    List<Booking> findAllPastByOwnerIdAndStatus(@Param("ownerId") Long ownerId,
                                                @Param("status") BookingStatus bookingStatus,
                                                @Param("now") LocalDateTime now);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.item.ownerId = :ownerId AND b.status = :status " +
            "AND b.start > :now " +
            "ORDER BY b.start DESC")
    List<Booking> findAllFutureByOwnerIdAndStatus(@Param("ownerId") Long ownerId,
                                                  @Param("status") BookingStatus bookingStatus,
                                                  @Param("now") LocalDateTime now);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.item.ownerId = :ownerId AND b.status = :status " +
            "ORDER BY b.start DESC")
    List<Booking> findAllByOwnerIdAndStatusOrderByStartDesc(@Param("ownerId") Long ownerId,
                                                            @Param("status") BookingStatus bookingStatus);


    @Query("select max(b.end) " +
            "from Booking b " +
            "where b.item.id = :itemId " +
            "and b.end < :now")
    LocalDateTime getLastBookingEndDateByItemId(@Param("itemId") Long itemId,
                                                @Param("now") LocalDateTime now);

    @Query("select min(b.start) " +
            "from Booking b " +
            "where b.item.id = :itemId " +
            "and b.start > :now")
    LocalDateTime getNextBookingStartDateByItemId(@Param("itemId") Long itemId,
                                                  @Param("now") LocalDateTime now);

}

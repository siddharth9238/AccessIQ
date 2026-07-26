package com.accessiq.repository;

import com.accessiq.model.Request;
import com.accessiq.model.RequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RequestRepository extends JpaRepository<Request, Long> {

    @Query("SELECT r FROM Request r LEFT JOIN FETCH r.createdBy WHERE r.createdBy.id = :userId")
    List<Request> findByCreatedById(@Param("userId") Long userId);

    @Query("SELECT r FROM Request r LEFT JOIN FETCH r.createdBy WHERE r.createdBy.id = :userId")
    Page<Request> findByCreatedById(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT r FROM Request r LEFT JOIN FETCH r.createdBy WHERE r.status = :status")
    List<Request> findByStatus(@Param("status") RequestStatus status);

    @Query("SELECT r FROM Request r LEFT JOIN FETCH r.createdBy WHERE r.status = :status")
    Page<Request> findByStatus(@Param("status") RequestStatus status, Pageable pageable);

    @Query("SELECT r FROM Request r LEFT JOIN FETCH r.createdBy WHERE r.createdBy.email = :email")
    List<Request> findByCreatorEmail(@Param("email") String email);

    @Query("SELECT r FROM Request r LEFT JOIN FETCH r.createdBy WHERE r.createdBy.email = :email")
    Page<Request> findByCreatorEmail(@Param("email") String email, Pageable pageable);

    @Query("SELECT r FROM Request r LEFT JOIN FETCH r.createdBy WHERE r.status = :status AND r.createdBy.email = :email")
    List<Request> findByStatusAndCreatorEmail(@Param("status") RequestStatus status, @Param("email") String email);

    @Query("SELECT r FROM Request r LEFT JOIN FETCH r.createdBy WHERE r.status = :status AND r.createdBy.email = :email")
    Page<Request> findByStatusAndCreatorEmail(@Param("status") RequestStatus status, @Param("email") String email, Pageable pageable);

    @Query("SELECT r FROM Request r LEFT JOIN FETCH r.createdBy WHERE LOWER(r.title) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Request> searchByKeyword(@Param("keyword") String keyword);

    @Query("SELECT r FROM Request r LEFT JOIN FETCH r.createdBy WHERE LOWER(r.title) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Request> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT r FROM Request r LEFT JOIN FETCH r.createdBy")
    List<Request> findAllWithCreator();
}
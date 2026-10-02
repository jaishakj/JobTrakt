package com.jobtrakt.application;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {
    @Query("select a from JobApplication a where a.user.id=:userId and (:status is null or a.status=:status) " +
           "and (:search is null or lower(a.company) like lower(concat('%',:search,'%')) " +
           "or lower(a.position) like lower(concat('%',:search,'%')) or lower(coalesce(a.location,'')) like lower(concat('%',:search,'%')))" )
    Page<JobApplication> search(@Param("userId") Long userId, @Param("status") ApplicationStatus status,
                                @Param("search") String search, Pageable pageable);
    Optional<JobApplication> findByIdAndUserId(Long id, Long userId);
    long countByUserId(Long userId);
    long countByUserIdAndStatus(Long userId, ApplicationStatus status);
    long countByUserIdAndCreatedAtAfter(Long userId, Instant since);
    long countByUserIdAndInterviewDateBetween(Long userId, Instant from, Instant to);
    long countByUserIdAndAppliedDateBetween(Long userId, LocalDate from, LocalDate to);
    List<JobApplication> findTop6ByUserIdOrderByCreatedAtDesc(Long userId);
    @Query("select a from JobApplication a where a.user.id=:userId and a.status=ApplicationStatus.INTERVIEW and a.interviewDate > :from order by a.interviewDate")
    List<JobApplication> upcomingInterviews(@Param("userId") Long userId, @Param("from") Instant from, Pageable pageable);
    boolean existsByUserIdAndInterviewDateAndIdNot(Long userId, Instant interviewDate, Long id);
}

package com.jobtrakt.job;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface JobRepository extends JpaRepository<BackgroundJob, Long> {
    @Query(value = "select * from jobs where status in ('PENDING','FAILED') and scheduled_at <= :now " +
            "order by case priority when 'HIGH' then 0 when 'NORMAL' then 1 else 2 end, scheduled_at, created_at " +
            "limit 1 for update skip locked", nativeQuery = true)
    List<BackgroundJob> lockNextDue(@Param("now") Instant now);
    Page<BackgroundJob> findByUserId(Long userId, Pageable pageable);
    Page<BackgroundJob> findByUserIdAndStatus(Long userId, JobStatus status, Pageable pageable);
    Optional<BackgroundJob> findByIdAndUserId(Long id, Long userId);
    Optional<BackgroundJob> findByDedupeKey(String dedupeKey);
    List<BackgroundJob> findTop20ByApplicationIdAndUserIdOrderByCreatedAtDesc(Long applicationId, Long userId);
    boolean existsByDedupeKey(String dedupeKey);
    long countByUserIdAndStatus(Long userId, JobStatus status);
    @Query("select j.status as status, count(j) as total from BackgroundJob j where j.user.id=:userId group by j.status")
    List<JobStatusCount> countStatuses(@Param("userId") Long userId);
    boolean existsByApplicationIdAndTypeAndStatusIn(Long applicationId, JobType type, List<JobStatus> statuses);
}

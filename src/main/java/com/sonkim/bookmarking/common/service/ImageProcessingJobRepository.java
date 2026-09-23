package com.sonkim.bookmarking.common.service;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.List;
import java.time.LocalDateTime;

public interface ImageProcessingJobRepository extends JpaRepository<ImageProcessingJob, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select job from ImageProcessingJob job where job.id = :id")
    Optional<ImageProcessingJob> findByIdForUpdate(Long id);

    @Query("""
            select job.id from ImageProcessingJob job
            where (job.status = :pending and job.attemptCount < :maxAttempts)
               or (job.status = :failed and job.attemptCount < :maxAttempts
                   and job.nextAttemptAt <= :now)
               or (job.status = :processing and job.updatedAt <= :staleBefore)
            order by job.createdAt
            """)
    List<Long> findRecoveryCandidateIds(
            @Param("pending") ImageProcessingJob.Status pending,
            @Param("failed") ImageProcessingJob.Status failed,
            @Param("processing") ImageProcessingJob.Status processing,
            @Param("maxAttempts") int maxAttempts,
            @Param("now") LocalDateTime now,
            @Param("staleBefore") LocalDateTime staleBefore,
            Pageable pageable);
}

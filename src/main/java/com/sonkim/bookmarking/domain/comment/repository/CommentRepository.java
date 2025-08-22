package com.sonkim.bookmarking.domain.comment.repository;

import com.sonkim.bookmarking.domain.comment.dto.CommentReplyCountDto;
import com.sonkim.bookmarking.domain.comment.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    Page<Comment> findByBookmark_IdAndParentIsNullOrderByCreatedAtAsc(Long bookmarkId, Pageable pageable);

    @Query(value =
            "WITH RECURSIVE descendants AS (" +
            "    SELECT id, parent_id, parent_id as root_id FROM comment WHERE parent_id IN :parentIds" +
            "    UNION ALL" +
            "    SELECT c.id, c.parent_id, d.root_id FROM comment c INNER JOIN descendants d ON c.parent_id = d.id" +
            ")" +
            "SELECT root_id as commentId, COUNT(*) as replyCount " +
            "FROM descendants GROUP BY root_id",
            nativeQuery = true)
    List<CommentReplyCountDto> countRepliesForParents(@Param("parentIds") List<Long> parentIds);

    @Query(value =
            "WITH RECURSIVE descendants AS (" +
            "    SELECT * FROM comment WHERE parent_id = :parentId" +
            "    UNION ALL" +
            "    SELECT c.* FROM comment c INNER JOIN descendants d ON c.parent_id = d.id" +
            ")" +
            "SELECT * FROM descendants ORDER BY created_at ASC LIMIT :#{#pageable.pageSize} OFFSET :#{#pageable.offset}",
            nativeQuery = true)
    List<Comment> findDescendantsPaged(@Param("parentId") Long parentId, Pageable pageable);

    @Query(value =
            "WITH RECURSIVE descendants AS (" +
            "    SELECT id, parent_id FROM comment WHERE parent_id = :parentId" +
            "    UNION ALL" +
            "    SELECT c.id, c.parent_id FROM comment c INNER JOIN descendants d ON c.parent_id = d.id" +
            ")" +
            "SELECT COUNT(*) FROM descendants",
            nativeQuery = true)
    long countDescendants(@Param("parentId") Long parentId);

    @Query("SELECT c FROM Comment c LEFT JOIN c.children WHERE c.id = :commentId")
    Optional<Comment> findCommentWithChildrenById(@Param("commentId") Long commentId);
}

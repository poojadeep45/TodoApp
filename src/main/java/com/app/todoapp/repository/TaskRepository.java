package com.app.todoapp.repository;

import com.app.todoapp.entities.Priority;
import com.app.todoapp.entities.Task;
import com.app.todoapp.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findAllByOwnerOrderByCompletedAscDueDateAscPriorityDesc(User owner);

    Optional<Task> findByIdAndOwner(Long id, User owner);

    boolean existsByIdAndOwner(Long id, User owner);

    @Query("SELECT t FROM Task t WHERE t.owner = :owner AND " +
            "(:keyword IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "(:priority IS NULL OR t.priority = :priority) AND " +
            "(:completed IS NULL OR t.completed = :completed) AND " +
            "(:category IS NULL OR t.category = :category) " +
            "ORDER BY t.completed ASC, t.dueDate ASC, t.priority DESC")
    List<Task> search(@Param("owner") User owner,
                      @Param("keyword") String keyword,
                      @Param("priority") Priority priority,
                      @Param("completed") Boolean completed,
                      @Param("category") String category);

    @Query("SELECT DISTINCT t.category FROM Task t WHERE t.owner = :owner AND t.category IS NOT NULL ORDER BY t.category")
    List<String> findDistinctCategoriesByOwner(@Param("owner") User owner);

    long countByOwnerAndCompleted(User owner, boolean completed);

    long countByOwner(User owner);

    List<Task> findByOwnerAndCompletedFalseAndDueDateBeforeOrderByDueDateAsc(User owner, java.time.LocalDate date);

    List<Task> findByOwnerAndCompletedFalseAndDueDateOrderByDueDateAsc(User owner, java.time.LocalDate date);

    List<Task> findByOwnerAndCompletedFalseAndDueDateBetweenOrderByDueDateAsc(User owner, java.time.LocalDate start, java.time.LocalDate end);
}
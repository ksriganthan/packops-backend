package ch.packops.packopsbackend.repository;

import ch.packops.packopsbackend.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findById(Long id);

    Optional<Category> findByName(String name);

    Optional<Category> findByNameIgnoreCase(String categoryName);
}

package org.fsm.repository;

import org.fsm.entity.Product;
import org.fsm.entity.User;
import org.fsm.entity.WishList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WishListRepository extends JpaRepository<WishList, Long> {

    List<WishList> findByUserIdOrderByAddedAtDesc(Long userId);

    long countByUser(User user);

    /**
     * ⭐ NEW: Check if item exists with exact same options
     */
    boolean existsByUserAndProductAndSelectedOptionsJson(
            User user,
            Product product,
            String selectedOptionsJson
    );
}
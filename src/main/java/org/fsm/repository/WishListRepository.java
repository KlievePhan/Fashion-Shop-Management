package org.fsm.repository;

import org.fsm.entity.User;
import org.fsm.entity.WishList;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WishListRepository extends JpaRepository<WishList, Long> {
    boolean existsByUserIdAndProductIdAndProductVariantId(
            Long userId, Long productId, Long productVariantId);

    void deleteByUserIdAndProductIdAndProductVariantId(
            Long userId, Long productId, Long productVariantId);

    List<WishList> findByUserIdOrderByAddedAtDesc(Long userId);

    Optional<WishList> findByUserIdAndProductIdAndProductVariantId(
            Long userId, Long productId, Long productVariantId);

    int countByUser(User user);
}

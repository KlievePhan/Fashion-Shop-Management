package org.fsm.repository;

import org.fsm.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByVnpTxnref(String vnpTxnref);
    Optional<Payment> findByOrderId(Long orderId);
}

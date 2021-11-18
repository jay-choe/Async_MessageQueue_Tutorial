package async.example.domain.entity.repository;

import async.example.domain.entity.OrderLog;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderLogRepository extends JpaRepository<OrderLog, Integer> {
    Optional<OrderLog> findByProductId(Integer productId);
}

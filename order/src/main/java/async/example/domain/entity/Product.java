package async.example.domain.entity;

import javax.persistence.Entity;
import javax.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Entity
@Getter
@Slf4j
@NoArgsConstructor
public class Product {
    @Id
    private Integer id;

    private String name;

    private Long price;

    private Integer stock;

    public void updateStock(final int decreaseStock) {
        final int modifiedStock = this.stock - decreaseStock;
        assert(modifiedStock >= 0);
        this.stock = modifiedStock;
    }
}

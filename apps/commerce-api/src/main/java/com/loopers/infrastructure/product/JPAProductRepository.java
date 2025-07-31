package com.loopers.infrastructure.product;



import com.loopers.infrastructure.product.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JPAProductRepository extends JpaRepository<ProductEntity,Long> {

}

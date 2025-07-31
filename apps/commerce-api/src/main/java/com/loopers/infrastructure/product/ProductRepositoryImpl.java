package com.loopers.infrastructure.product;

import com.loopers.application.product.ProductInfo;
import com.loopers.application.product.ProductSearchCommand;
import com.loopers.application.product.QProductInfo;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.product.ProductSortType;
import com.loopers.domain.product.model.Product;
import com.loopers.infrastructure.brand.Entity.QBrandEntity;
import com.loopers.infrastructure.brand.JpaBrandRepository;
import com.loopers.infrastructure.like.entity.QLikeEntity;
import com.loopers.infrastructure.product.entity.ProductEntity;
import com.loopers.infrastructure.product.entity.QProductEntity;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepository {

    private final JPAProductRepository jpaProductRepository;

    private final JPAQueryFactory queryFactory;

    private final QProductEntity product = QProductEntity.productEntity;
    private final QBrandEntity brand = QBrandEntity.brandEntity;
    private final QLikeEntity like = QLikeEntity.likeEntity;

    @Override
    public Page<ProductInfo> searchByCondition(ProductSearchCommand command) {
        BooleanExpression where = product.deletedAt.isNull();

        if (command.brandId() != null) {
            where = where.and(product.brandId.eq(command.brandId()));
        }

        OrderSpecifier<?> orderSpecifier = getOrderSpecifier(command.sortType());

        List<ProductInfo> content = queryFactory
            .select(new QProductInfo(
                product.id,
                product.name,
                brand.name,
                product.price,
                like.count().intValue()
            ))
            .from(product)
            .leftJoin(brand).on(product.brandId.eq(brand.id))
            .leftJoin(like).on(like.productId.eq(product.id))
            .where(where)
            .groupBy(product.id, product.name, brand.name, product.price)
            .orderBy(orderSpecifier)
            .offset(command.pageable().getOffset())
            .limit(command.pageable().getPageSize())
            .fetch();

        Long total = queryFactory
            .select(product.count())
            .from(product)
            .where(where)
            .fetchOne();

        return new PageImpl<>(content, command.pageable(), total == null ? 0 : total);
    }


    @Override
    public Optional<ProductInfo> findProductInfoById(Long id) {
        ProductInfo info = queryFactory
            .select(new QProductInfo(
                product.id,
                product.name,
                brand.name,
                product.price,
                Expressions.constant(0)
            ))
            .from(product)
            .leftJoin(brand).on(product.brandId.eq(brand.id))
            .where(product.id.eq(id))
            .fetchOne();

        if (info == null || info.brandName() == null) {
            return Optional.empty();
        }
        return Optional.of(info);

    }

    @Override
    public void save(Product product) {
        jpaProductRepository.save(ProductEntity.from(product));
    }

    private OrderSpecifier<?> getOrderSpecifier(ProductSortType sortType) {
        return switch (sortType) {
            case LATEST -> product.createdAt.desc();
            case PRICE_DESC -> product.price.desc();
            case LIKES_DESC -> like.count().desc();
        };
    }
}

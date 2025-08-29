package com.loopers.infrastructure.product;

import com.loopers.application.product.ProductInfo;
import com.loopers.application.product.ProductSearchCommand;
import com.loopers.application.product.QProductInfo;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.product.ProductSortType;
import com.loopers.domain.product.model.Product;
import com.loopers.infrastructure.brand.Entity.QBrandEntity;
import com.loopers.infrastructure.product.entity.ProductEntity;
import com.loopers.infrastructure.product.entity.QProductEntity;
import com.loopers.infrastructure.product.like.entity.QProductLikeEntity;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepository {

    private final JPAProductRepository jpaProductRepository;
    private final JPAQueryFactory queryFactory;

    private final QProductEntity product  = QProductEntity.productEntity;
    private final QBrandEntity   brand  = QBrandEntity.brandEntity;
    private final QProductLikeEntity productLike = QProductLikeEntity.productLikeEntity;

    @Override
    public Page<ProductInfo> searchByCondition(ProductSearchCommand c) {
        BooleanExpression where = product.deletedAt.isNull();
        if (c.brandId() != null) {
            where = where.and(product.brandId.eq(c.brandId()));
        }

        ProductSortType sort = (c.sortType() == null) ? ProductSortType.LATEST : c.sortType();
        

        if (sort == ProductSortType.LIKES_DESC) {
            return searchByLikesDesc(c, where);
        }
        

        OrderSpecifier<?> order = switch (sort) {
            case LATEST     -> product.createdAt.desc();
            case PRICE_DESC -> product.price.desc();
            case LIKES_DESC -> productLike.likeCount.desc(); // 이 경우는 위에서 걸러짐
        };

        List<ProductInfo> content = queryFactory
            .select(new QProductInfo(
                product.id,
                product.name,
                brand.name,
                product.price,
                productLike.likeCount.coalesce(0)
            ))
            .from(product)
            .join(brand).on(product.brandId.eq(brand.id))
            .leftJoin(productLike).on(productLike.productId.eq(product.id))
            .where(where)
            .orderBy(order)
            .offset(c.pageable().getOffset())
            .limit(c.pageable().getPageSize())
            .fetch();

        Long total = queryFactory
            .select(product.count())
            .from(product)
            .where(where)
            .fetchOne();

        return new PageImpl<>(content, c.pageable(), total == null ? 0 : total);
    }


    private Page<ProductInfo> searchByLikesDesc(ProductSearchCommand c, BooleanExpression baseWhere) {
        BooleanExpression likesWhere = product.deletedAt.isNull();
        if (c.brandId() != null) {
            likesWhere = likesWhere.and(product.brandId.eq(c.brandId()));
        }

        List<ProductInfo> content = queryFactory
            .select(new QProductInfo(
                product.id,
                product.name,
                brand.name,
                product.price,
                productLike.likeCount.coalesce(0)
            ))
            .from(productLike)
            .join(product).on(product.id.eq(productLike.productId))
            .join(brand).on(product.brandId.eq(brand.id))
            .where(likesWhere)
            .orderBy(productLike.likeCount.desc())
            .offset(c.pageable().getOffset())
            .limit(c.pageable().getPageSize())
            .fetch();

        Long total = queryFactory
            .select(productLike.count())
            .from(productLike)
            .join(product).on(product.id.eq(productLike.productId))
            .where(likesWhere)
            .fetchOne();

        return new PageImpl<>(content, c.pageable(), total == null ? 0 : total);
    }

    @Override
    public Optional<ProductInfo> findProductInfoById(Long id) {
        ProductInfo info = queryFactory
            .select(new QProductInfo(
                product.id,
                product.name,
                brand.name,
                product.price,
                productLike.likeCount.coalesce(0)
            ))
            .from(product)
            .join(brand).on(product.brandId.eq(brand.id))
            .leftJoin(productLike).on(productLike.productId.eq(product.id))
            .where(product.deletedAt.isNull(), product.id.eq(id))
            .fetchOne();

        return Optional.ofNullable(info);
    }

    @Override
    public boolean existsById(Long productId) {
        return jpaProductRepository.existsById(productId);
    }

    @Override
    public void save(Product product) {
        jpaProductRepository.save(ProductEntity.from(product));
    }

    @Override
    public Optional<Product> findById(Long productId) {
        return jpaProductRepository.findById(productId).map(ProductEntity::toModel);
    }

    @Override
    public Optional<Product> findWithPessimisticLockById(Long productId) {
        return jpaProductRepository.findWithPessimisticLockById(productId).map(ProductEntity::toModel);
    }
}

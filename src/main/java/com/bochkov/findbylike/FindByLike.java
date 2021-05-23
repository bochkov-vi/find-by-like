package com.bochkov.findbylike;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Streams;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.*;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * The interface Maskable.
 */
public interface FindByLike {


    /**
     * Mask specification specification.
     *
     * @param <T>               the type parameter
     * @param mask              the mask
     * @param maskedPopertyName the masked poperty name
     * @return the specification
     */

    static BiFunction<Root, String, Path> DEFAULT_PROPERTY_EXTRACTOR = (root, maskedPopertyName) -> {
        Path maskedProperty = fetchNestedPath(root, maskedPopertyName);

        return maskedProperty;
    };


    static Predicate stringMaskExpression(String mask, Expression maskedProperty, CriteriaBuilder cb) {
        mask = Optional.ofNullable(mask).orElse("%");
        return cb.like(cb.lower(maskedProperty.as(String.class)), Optional.of(mask).map(String::trim).map(String::toLowerCase).map(s -> "%" + s + "%").orElse(null));
    }

    static public <T> Specification<T> maskSpecification(final String mask, final String maskedPopertyName, final BiFunction<Root, String, Path> propertyExtractor) {
        return (root, query, cb) -> {
            Predicate result;
            Path maskedProperty = propertyExtractor.apply(root, maskedPopertyName);
            result = stringMaskExpression(mask, maskedProperty, cb);
            if (result != null) {
                List<Order> orders = query.getOrderList();
                if (orders == null) {
                    orders = ImmutableList.of();
                }
                Expression locate = cb.locate(maskedProperty.as(String.class), Optional.ofNullable(mask).orElse(""));
                List<Expression> expressionToOrder = Lists.newArrayList(locate, cb.length(maskedProperty.as(String.class)), maskedProperty);
                orders = Streams.concat(orders.stream(), expressionToOrder.stream().map(ex -> cb.asc(ex))).collect(Collectors.toList());
                query.orderBy(orders);
            }
            return result;
        };
    }

    /**
     * Fetch nested path path.
     *
     * @param <T>       the type parameter
     * @param root      the root
     * @param fieldname the fieldname
     * @return the path
     */
    public static <T> Path<T> fetchNestedPath(Path<T> root, String fieldname) {
        String[] fields = fieldname.split("\\.");
        Path<T> result = null;
        for (String field : fields) {
            if (result == null) {
                result = toPath(root, field);
            } else {
                result = toPath(result, field);
            }
        }
        return result;
    }

    static Path toPath(Path root, String fieldName) {
        Path result = null;
        result = root.get(fieldName);
        try {
            result = ((From) root).join(fieldName, JoinType.LEFT);
        } catch (Exception ignored) {
        }
        return result;

    }

    /**
     * Mask specification specification.
     *
     * @param <T>             the type parameter
     * @param mask            the mask
     * @param maskedPoperties the masked poperties
     * @return the specification
     */
    static <T> Specification<T> maskSpecification(final String mask, final Iterable<String> maskedPoperties) {
        return maskSpecification(mask, maskedPoperties, DEFAULT_PROPERTY_EXTRACTOR);
    }

    static <T> Specification<T> maskSpecification(final String mask, final Iterable<String> maskedPoperties, final BiFunction<Root, String, Path> propertyExtractor) {
        Specification<T> where = null;
        for (String p : maskedPoperties) {
            Specification<T> spec = maskSpecification(mask, p, propertyExtractor);
            if (where == null) {
                where = Specification.where(spec);
            } else {
                where = where.or(spec);
            }
        }
        return where;
    }
}

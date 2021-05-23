package org.springframework.data.jpa.repository.query;

import org.springframework.data.mapping.PropertyPath;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;

import static org.springframework.data.jpa.repository.query.QueryUtils.toExpressionRecursively;

public abstract class FindByLikeQueryUtils {
    public static <T> Expression<T> toExpression(String property, From<?, ?> from, CriteriaBuilder cb) {
        PropertyPath propertyPath = PropertyPath.from(property, from.getJavaType());
        return toExpression(propertyPath, from, cb);
    }

    public static <T> Expression<T>  toExpression(PropertyPath propertyPath, From<?, ?> from, CriteriaBuilder cb) {
        Expression<T> expression = toExpressionRecursively(from, propertyPath);
        return expression;
    }
}

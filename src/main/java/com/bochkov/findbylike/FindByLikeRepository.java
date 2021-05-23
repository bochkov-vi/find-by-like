package com.bochkov.findbylike;

import com.google.common.collect.Lists;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import static com.bochkov.findbylike.FindByLike.maskSpecification;

public interface FindByLikeRepository<T> extends JpaSpecificationExecutor<T> {

    default Page<T> findByLike(String mask, Pageable pageable, String... properties) {
        return findAll(maskSpecification(mask, Lists.newArrayList(properties)), pageable);
    }

    default Page<T> findByLike(String mask, Pageable pageable, Iterable<String> properties) {
        return findAll(maskSpecification(mask, properties), pageable);
    }


}

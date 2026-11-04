package com.mirjdev.examplesspring.entity.repository;

import com.mirjdev.examplesspring.dto.ClientFilter;
import com.mirjdev.examplesspring.entity.Client;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class ClientSpecifications {

    private ClientSpecifications() {
    }

    public static Specification<Client> matching(ClientFilter filter) {
        return (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (filter.id() != null) {
                predicates.add(builder.equal(root.get("id"), filter.id()));
            }
            contains(predicates, root, builder, "email", filter.email());
            contains(predicates, root, builder, "fio", filter.fio());
            contains(predicates, root, builder, "phone", filter.phone());
            if (filter.createdFrom() != null) {
                predicates.add(builder.greaterThanOrEqualTo(root.get("createdAt"), filter.createdFrom()));
            }
            if (filter.createdTo() != null) {
                predicates.add(builder.lessThanOrEqualTo(root.get("createdAt"), filter.createdTo()));
            }
            if (filter.version() != null) {
                predicates.add(builder.equal(root.get("version"), filter.version()));
            }
            return builder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private static void contains(List<Predicate> predicates, Root<Client> root, CriteriaBuilder builder,
                                 String field, String value) {
        if (StringUtils.hasText(value)) {
            String escaped = value.strip().toLowerCase(Locale.ROOT)
                    .replace("\\", "\\\\")
                    .replace("%", "\\%")
                    .replace("_", "\\_");
            predicates.add(builder.like(builder.lower(root.get(field)), "%" + escaped + "%", '\\'));
        }
    }
}

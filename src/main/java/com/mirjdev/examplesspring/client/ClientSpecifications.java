package com.mirjdev.examplesspring.client;

import com.mirjdev.examplesspring.client.dto.ClientFilter;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Динамические фильтры для поиска клиентов.
 */
public final class ClientSpecifications {

    private static final char LIKE_ESCAPE = '\\';

    private ClientSpecifications() {
    }

    public static Specification<Client> matching(ClientFilter filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            addContains(predicates, criteriaBuilder, root, "fio", filter.getFio());
            addContains(predicates, criteriaBuilder, root, "email", filter.getEmail());
            addContains(predicates, criteriaBuilder, root, "phone", filter.getPhone());
            addGlobalQuery(predicates, criteriaBuilder, root, filter.getQuery());

            if (filter.getCreatedFrom() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.<Instant>get("createdAt"), filter.getCreatedFrom()));
            }
            if (filter.getCreatedTo() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.<Instant>get("createdAt"), filter.getCreatedTo()));
            }

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    /**
     * Паттерн для LIKE. Спецсимволы {@code %}, {@code _} и сам escape-символ
     * экранируются, чтобы пользовательский ввод не превращался в wildcard.
     */
    private static String toLikePattern(String value) {
        String escaped = value.trim()
                .toLowerCase(Locale.ROOT)
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
        return "%" + escaped + "%";
    }

    private static void addGlobalQuery(List<Predicate> predicates,
                                       CriteriaBuilder criteriaBuilder,
                                       Root<Client> root,
                                       String query) {
        String value = trimToNull(query);
        if (value == null) {
            return;
        }
        String pattern = toLikePattern(value);
        predicates.add(criteriaBuilder.or(
                contains(criteriaBuilder, root, "fio", pattern),
                contains(criteriaBuilder, root, "email", pattern),
                contains(criteriaBuilder, root, "phone", pattern)
        ));
    }

    private static void addContains(List<Predicate> predicates,
                                    CriteriaBuilder criteriaBuilder,
                                    Root<Client> root,
                                    String field,
                                    String value) {
        String notBlankValue = trimToNull(value);
        if (notBlankValue != null) {
            predicates.add(contains(criteriaBuilder, root, field, toLikePattern(notBlankValue)));
        }
    }

    private static Predicate contains(CriteriaBuilder criteriaBuilder,
                                      Root<Client> root,
                                      String field,
                                      String pattern) {
        return criteriaBuilder.like(criteriaBuilder.lower(root.<String>get(field)), pattern, LIKE_ESCAPE);
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}

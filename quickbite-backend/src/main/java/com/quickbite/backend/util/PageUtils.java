package com.quickbite.backend.util;

import com.quickbite.backend.exception.BadRequestException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Set;

public final class PageUtils {

    private static final int MAX_PAGE_SIZE = 50;

    private PageUtils() {}

    // Whitelists sortable fields (an unknown field would otherwise cause a 500), caps the page size
    // and adds id as a tie-breaker so pagination order stays stable.
    public static Pageable sanitize(Pageable pageable, Set<String> allowedSortFields, Sort defaultSort) {
        for (Sort.Order order : pageable.getSort()) {
            if (!allowedSortFields.contains(order.getProperty())) {
                throw new BadRequestException("Cannot sort by '" + order.getProperty() + "'");
            }
        }
        Sort sort = pageable.getSort().isSorted() ? pageable.getSort() : defaultSort;
        if (sort.getOrderFor("id") == null) {
            sort = sort.and(Sort.by("id"));
        }
        int size = Math.min(Math.max(pageable.getPageSize(), 1), MAX_PAGE_SIZE);
        return PageRequest.of(Math.max(pageable.getPageNumber(), 0), size, sort);
    }
}
package com.rotopay.expensetracker.api.v1.mapper;

import com.rotopay.expensetracker.api.v1.response.CategoryResponseV1;
import com.rotopay.expensetracker.entity.Category;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public CategoryResponseV1 toResponse(Category entity) {
        return CategoryResponseV1.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .icon(entity.getIcon())
                .color(entity.getColor())
                .isSystem(entity.getIsSystem())
                .build();
    }
}

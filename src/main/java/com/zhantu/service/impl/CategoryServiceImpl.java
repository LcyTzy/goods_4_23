package com.zhantu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhantu.entity.Category;
import com.zhantu.mapper.CategoryMapper;
import com.zhantu.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Override
    public boolean save(Category entity) {
        if (entity.getParentId() == null || entity.getParentId() == 0) {
            entity.setParentId(0L);
            entity.setLevel(1);
        } else {
            Category parent = this.getById(entity.getParentId());
            if (parent != null) {
                entity.setLevel(parent.getLevel() + 1);
            } else {
                entity.setLevel(2);
            }
        }
        return super.save(entity);
    }

    @Override
    public boolean updateById(Category entity) {
        Category old = this.getById(entity.getId());
        if (old != null && entity.getParentId() != null && !entity.getParentId().equals(old.getParentId())) {
            if (entity.getParentId() == 0) {
                entity.setLevel(1);
            } else {
                Category parent = this.getById(entity.getParentId());
                if (parent != null) {
                    entity.setLevel(parent.getLevel() + 1);
                }
            }
        }
        return super.updateById(entity);
    }

    @Override
    public List<Category> getCategoryTree() {
        List<Category> allCategories = this.list();
        List<Category> rootCategories = allCategories.stream()
                .filter(c -> c.getParentId() != null && c.getParentId() == 0)
                .collect(Collectors.toList());
        
        for (Category root : rootCategories) {
            List<Category> children = allCategories.stream()
                    .filter(c -> c.getParentId() != null && c.getParentId().equals(root.getId()))
                    .collect(Collectors.toList());
            root.setChildren(children);
        }
        
        return rootCategories;
    }
}

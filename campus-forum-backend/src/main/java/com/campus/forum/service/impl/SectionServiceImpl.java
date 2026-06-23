package com.campus.forum.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.forum.common.exception.BusinessException;
import com.campus.forum.entity.Section;
import com.campus.forum.mapper.SectionMapper;
import com.campus.forum.service.SectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SectionServiceImpl implements SectionService {

    private final SectionMapper sectionMapper;

    @Override
    public List<Section> listAll() {
        return sectionMapper.selectList(
                new LambdaQueryWrapper<Section>()
                        .eq(Section::getStatus, 1)
                        .orderByAsc(Section::getSort)
        );
    }

    @Override
    public Section getById(Long id) {
        Section section = sectionMapper.selectById(id);
        if (section == null) throw new BusinessException("版块不存在");
        return section;
    }

    @Override
    public Section create(String name, String code, String description, String icon) {
        Section section = new Section();
        section.setName(name);
        section.setCode(code);
        section.setDescription(description);
        section.setIcon(icon);
        section.setStatus(1);
        section.setSort(100);
        sectionMapper.insert(section);
        return section;
    }

    @Override
    public Section update(Long id, String name, String description, String icon, Integer sort) {
        Section section = sectionMapper.selectById(id);
        if (section == null) throw new BusinessException("版块不存在");
        if (name != null) section.setName(name);
        if (description != null) section.setDescription(description);
        if (icon != null) section.setIcon(icon);
        if (sort != null) section.setSort(sort);
        sectionMapper.updateById(section);
        return section;
    }

    @Override
    public boolean delete(Long id) {
        Section section = sectionMapper.selectById(id);
        if (section == null) throw new BusinessException("版块不存在");
        section.setStatus(0);
        return sectionMapper.updateById(section) > 0;
    }
}

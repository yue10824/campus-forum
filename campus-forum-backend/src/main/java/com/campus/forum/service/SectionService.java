package com.campus.forum.service;

import com.campus.forum.entity.Section;
import java.util.List;

public interface SectionService {
    List<Section> listAll();
    Section getById(Long id);
    Section create(String name, String code, String description, String icon);
    Section update(Long id, String name, String description, String icon, Integer sort);
    boolean delete(Long id);
}

package com.campus.forum.controller;

import com.campus.forum.common.Result;
import com.campus.forum.entity.Section;
import com.campus.forum.service.SectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "版块模块")
@RestController
@RequestMapping("/api/sections")
@RequiredArgsConstructor
public class SectionController {

    private final SectionService sectionService;

    @Operation(summary = "版块列表")
    @GetMapping
    public Result<List<Section>> list() {
        return Result.success(sectionService.listAll());
    }

    @Operation(summary = "版块详情")
    @GetMapping("/{id}")
    public Result<Section> detail(@PathVariable Long id) {
        return Result.success(sectionService.getById(id));
    }
}

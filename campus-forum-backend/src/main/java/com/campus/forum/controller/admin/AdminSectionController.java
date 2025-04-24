package com.campus.forum.controller.admin;

import com.campus.forum.common.Result;
import com.campus.forum.entity.Section;
import com.campus.forum.service.SectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "管理端-版块管理")
@RestController
@RequestMapping("/api/admin/sections")
@RequiredArgsConstructor
public class AdminSectionController {

    private final SectionService sectionService;

    @Operation(summary = "版块列表")
    @GetMapping
    public Result<List<Section>> list() {
        return Result.success(sectionService.listAll());
    }

    @Operation(summary = "新建版块")
    @PostMapping
    public Result<Section> create(@RequestBody Map<String, String> body) {
        return Result.success(sectionService.create(
                body.get("name"), body.get("code"),
                body.get("description"), body.get("icon")));
    }

    @Operation(summary = "更新版块")
    @PutMapping("/{id}")
    public Result<Section> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Integer sort = body.get("sort") != null ? Integer.valueOf(body.get("sort").toString()) : null;
        return Result.success(sectionService.update(id,
                (String) body.get("name"), (String) body.get("description"),
                (String) body.get("icon"), sort));
    }

    @Operation(summary = "删除版块（软删除）")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        sectionService.delete(id);
        return Result.success();
    }
}

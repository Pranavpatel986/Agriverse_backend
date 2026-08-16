package com.agriverse.api.content.controller;

import com.agriverse.api.common.dto.ContentListResponse;
import com.agriverse.api.content.dto.TagResponse;
import com.agriverse.api.content.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Public tag listing -- feeds the article editor's tag picker. Write access is admin-only, see AdminTagController. */
@RestController
@RequestMapping("/api/v1/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @GetMapping
    public ContentListResponse<TagResponse> list() {
        return tagService.list();
    }
}

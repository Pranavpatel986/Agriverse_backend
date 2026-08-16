package com.agriverse.api.learning.service;

import com.agriverse.api.common.dto.PageResponse;
import com.agriverse.api.common.exception.BadRequestException;
import com.agriverse.api.common.exception.ResourceNotFoundException;
import com.agriverse.api.common.util.SlugUtil;
import com.agriverse.api.content.entity.Article;
import com.agriverse.api.content.entity.Category;
import com.agriverse.api.content.repository.ArticleRepository;
import com.agriverse.api.content.repository.CategoryRepository;
import com.agriverse.api.identity.entity.User;
import com.agriverse.api.identity.repository.UserRepository;
import com.agriverse.api.learning.dto.*;
import com.agriverse.api.learning.entity.Quiz;
import com.agriverse.api.learning.entity.Roadmap;
import com.agriverse.api.learning.entity.RoadmapStep;
import com.agriverse.api.learning.entity.UserRoadmapProgress;
import com.agriverse.api.learning.repository.QuizRepository;
import com.agriverse.api.learning.repository.RoadmapRepository;
import com.agriverse.api.learning.repository.RoadmapStepRepository;
import com.agriverse.api.learning.repository.UserRoadmapProgressRepository;
import com.agriverse.api.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/** Backs the Roadmaps endpoint group (REST API Specification, Section 9). */
@Service
@RequiredArgsConstructor
public class RoadmapService {

    private final RoadmapRepository roadmapRepository;
    private final RoadmapStepRepository roadmapStepRepository;
    private final CategoryRepository categoryRepository;
    private final ArticleRepository articleRepository;
    private final QuizRepository quizRepository;
    private final UserRoadmapProgressRepository progressRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public PageResponse<RoadmapSummaryResponse> list(String categorySlug, Pageable pageable) {
        // Small catalog (Phase 1 learning content) — filter/paginate in memory rather than adding a bespoke query.
        List<Roadmap> all = roadmapRepository.findAll();
        List<Roadmap> filtered = categorySlug == null ? all
                : all.stream().filter(r -> r.getCategory() != null && categorySlug.equals(r.getCategory().getSlug())).toList();
        int start = Math.min(pageable.getPageNumber() * pageable.getPageSize(), filtered.size());
        int end = Math.min(start + pageable.getPageSize(), filtered.size());
        List<Roadmap> pageSlice = filtered.subList(start, end);
        List<RoadmapSummaryResponse> content = pageSlice.stream()
                .map(r -> new RoadmapSummaryResponse(r.getPublicId(), r.getTitle(), r.getSlug(), r.getDescription(),
                        roadmapStepRepository.countByRoadmapId(r.getId())))
                .toList();
        Page<Roadmap> page = new PageImpl<>(pageSlice, pageable, filtered.size());
        return PageResponse.of(page, content);
    }

    @Transactional(readOnly = true)
    public RoadmapDetailResponse getBySlug(String slug) {
        Roadmap roadmap = roadmapRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Roadmap not found: " + slug));
        List<RoadmapStep> steps = roadmapStepRepository.findByRoadmapIdOrderByStepOrderAsc(roadmap.getId());

        List<RoadmapStepResponse> stepResponses = steps.stream().map(step -> {
            UUID quizId = quizRepository.findAll().stream()
                    .filter(q -> q.getRoadmapStep() != null && q.getRoadmapStep().getId().equals(step.getId()))
                    .findFirst().map(Quiz::getPublicId).orElse(null);
            return new RoadmapStepResponse(step.getPublicId(), step.getStepOrder(), step.getTitle(),
                    step.getArticle() != null ? step.getArticle().getSlug() : null, quizId);
        }).toList();

        RoadmapProgressResponse progress = SecurityUtils.currentPrincipal()
                .flatMap(p -> progressRepository.findByUserIdAndRoadmapId(p.getId(), roadmap.getId()))
                .map(p -> new RoadmapProgressResponse(p.getCompletedSteps(), p.isCompleted()))
                .orElse(null);

        return new RoadmapDetailResponse(roadmap.getPublicId(), roadmap.getTitle(), stepResponses, progress);
    }

    @Transactional
    public RoadmapCreatedResponse create(CreateRoadmapRequest request) {
        Category category = request.categoryId() != null
                ? categoryRepository.findByPublicId(request.categoryId())
                    .orElseThrow(() -> ResourceNotFoundException.of("Category", request.categoryId()))
                : null;

        validateStepOrdering(request.steps());

        Roadmap roadmap = new Roadmap();
        roadmap.setTitle(request.title());
        roadmap.setSlug(uniqueSlug(request.title()));
        roadmap.setDescription(request.description());
        roadmap.setCategory(category);
        roadmapRepository.save(roadmap);

        for (CreateRoadmapStepRequest stepReq : request.steps()) {
            RoadmapStep step = new RoadmapStep();
            step.setRoadmap(roadmap);
            step.setStepOrder(stepReq.order());
            step.setTitle(stepReq.title());
            if (stepReq.articleId() != null) {
                Article article = articleRepository.findByPublicIdAndDeletedAtIsNull(stepReq.articleId())
                        .orElseThrow(() -> ResourceNotFoundException.of("Article", stepReq.articleId()));
                step.setArticle(article);
            }
            roadmapStepRepository.save(step);
        }

        return new RoadmapCreatedResponse(roadmap.getPublicId(), roadmap.getSlug());
    }

    @Transactional(readOnly = true)
    public RoadmapDetailedProgressResponse getProgress(UUID roadmapId) {
        Roadmap roadmap = roadmapRepository.findByPublicId(roadmapId)
                .orElseThrow(() -> ResourceNotFoundException.of("Roadmap", roadmapId));
        Long userId = SecurityUtils.currentUserId();
        UserRoadmapProgress progress = progressRepository.findByUserIdAndRoadmapId(userId, roadmap.getId()).orElse(null);
        if (progress == null) {
            return new RoadmapDetailedProgressResponse(0, null, false);
        }
        UUID currentStepId = progress.getCurrentStep() != null ? progress.getCurrentStep().getPublicId() : null;
        return new RoadmapDetailedProgressResponse(progress.getCompletedSteps(), currentStepId, progress.isCompleted());
    }

    @Transactional
    public RoadmapProgressResponse updateProgress(UUID roadmapId, UpdateRoadmapProgressRequest request) {
        Roadmap roadmap = roadmapRepository.findByPublicId(roadmapId)
                .orElseThrow(() -> ResourceNotFoundException.of("Roadmap", roadmapId));
        List<RoadmapStep> steps = roadmapStepRepository.findByRoadmapIdOrderByStepOrderAsc(roadmap.getId());
        RoadmapStep targetStep = steps.stream()
                .filter(s -> s.getPublicId().equals(request.stepId()))
                .findFirst()
                .orElseThrow(() -> new BadRequestException("stepId must belong to the specified roadmap"));

        Long userId = SecurityUtils.currentUserId();
        UserRoadmapProgress progress = progressRepository.findByUserIdAndRoadmapId(userId, roadmap.getId())
                .orElseGet(() -> {
                    UserRoadmapProgress p = new UserRoadmapProgress();
                    User user = userRepository.getReferenceById(userId);
                    p.setUser(user);
                    p.setRoadmap(roadmap);
                    p.setCompletedSteps(0);
                    return p;
                });

        int delta = request.completed() ? 1 : -1;
        progress.setCompletedSteps(Math.max(0, Math.min(steps.size(), progress.getCompletedSteps() + delta)));
        progress.setCompleted(progress.getCompletedSteps() >= steps.size() && !steps.isEmpty());

        RoadmapStep nextIncomplete = steps.stream()
                .sorted(Comparator.comparing(RoadmapStep::getStepOrder))
                .skip(progress.getCompletedSteps())
                .findFirst().orElse(null);
        progress.setCurrentStep(nextIncomplete != null ? nextIncomplete : targetStep);

        progressRepository.save(progress);
        return new RoadmapProgressResponse(progress.getCompletedSteps(), progress.isCompleted());
    }

    private void validateStepOrdering(List<CreateRoadmapStepRequest> steps) {
        List<Integer> orders = steps.stream().map(CreateRoadmapStepRequest::order).sorted().toList();
        for (int i = 0; i < orders.size(); i++) {
            if (orders.get(i) != i + 1) {
                throw new BadRequestException("steps must have unique, sequential order values starting at 1");
            }
        }
    }

    private String uniqueSlug(String title) {
        String base = SlugUtil.slugify(title);
        String slug = base;
        int suffix = 2;
        while (roadmapRepository.existsBySlug(slug)) {
            slug = base + "-" + suffix++;
        }
        return slug;
    }
}

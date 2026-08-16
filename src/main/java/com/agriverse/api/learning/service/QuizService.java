package com.agriverse.api.learning.service;

import com.agriverse.api.common.dto.ContentListResponse;
import com.agriverse.api.common.exception.BadRequestException;
import com.agriverse.api.common.exception.ResourceNotFoundException;
import com.agriverse.api.content.repository.CategoryRepository;
import com.agriverse.api.identity.repository.UserRepository;
import com.agriverse.api.learning.dto.*;
import com.agriverse.api.learning.entity.*;
import com.agriverse.api.learning.repository.*;
import com.agriverse.api.security.SecurityUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Backs the Quizzes endpoint group (REST API Specification, Section 10). */
@Service
@RequiredArgsConstructor
public class QuizService {

    private final QuizRepository quizRepository;
    private final QuizQuestionRepository questionRepository;
    private final QuizOptionRepository optionRepository;
    private final QuizAttemptRepository attemptRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public QuizDetailResponse getById(UUID id) {
        Quiz quiz = quizRepository.findByPublicId(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Quiz", id));
        List<QuizQuestion> questions = questionRepository.findByQuizIdOrderByQuestionOrderAsc(quiz.getId());
        List<QuizQuestionPublicResponse> questionResponses = questions.stream().map(q -> {
            List<QuizOptionPublicResponse> options = optionRepository.findByQuestionId(q.getId()).stream()
                    .map(o -> new QuizOptionPublicResponse(optionPublicId(o), o.getOptionText()))
                    .toList();
            return new QuizQuestionPublicResponse(questionPublicId(q), q.getQuestionText(), options);
        }).toList();
        return new QuizDetailResponse(quiz.getPublicId(), quiz.getTitle(), quiz.getPassingScore(), questionResponses);
    }

    @Transactional
    public QuizCreatedResponse create(CreateQuizRequest request) {
        if (request.passingScore() > request.questions().size()) {
            throw new BadRequestException("passingScore must not exceed the number of questions");
        }
        for (CreateQuizQuestionRequest q : request.questions()) {
            long correctCount = q.options().stream().filter(CreateQuizOptionRequest::isCorrect).count();
            if (q.options().size() < 2 || correctCount != 1) {
                throw new BadRequestException("each question must have at least 2 options and exactly one marked isCorrect");
            }
        }

        Quiz quiz = new Quiz();
        quiz.setTitle(request.title());
        quiz.setDescription(request.description());
        quiz.setPassingScore(request.passingScore());
        quizRepository.save(quiz);

        int order = 1;
        for (CreateQuizQuestionRequest qReq : request.questions()) {
            QuizQuestion question = new QuizQuestion();
            question.setQuiz(quiz);
            question.setQuestionText(qReq.questionText());
            question.setQuestionOrder(order++);
            questionRepository.save(question);

            for (CreateQuizOptionRequest oReq : qReq.options()) {
                QuizOption option = new QuizOption();
                option.setQuestion(question);
                option.setOptionText(oReq.optionText());
                option.setCorrect(oReq.isCorrect());
                optionRepository.save(option);
            }
        }

        return new QuizCreatedResponse(quiz.getPublicId());
    }

    @Transactional
    public QuizAttemptResultResponse submitAttempt(UUID quizId, SubmitQuizAttemptRequest request) {
        Quiz quiz = quizRepository.findByPublicId(quizId)
                .orElseThrow(() -> ResourceNotFoundException.of("Quiz", quizId));
        List<QuizQuestion> questions = questionRepository.findByQuizIdOrderByQuestionOrderAsc(quiz.getId());

        if (request.answers().size() != questions.size()) {
            throw new BadRequestException("answers must cover every question in the quiz exactly once");
        }

        Map<UUID, QuizQuestion> questionByPublicId = new HashMap<>();
        for (QuizQuestion q : questions) {
            questionByPublicId.put(questionPublicId(q), q);
        }

        int score = 0;
        ArrayNode answersSnapshot = objectMapper.createArrayNode();
        for (SubmitAnswerRequest answer : request.answers()) {
            QuizQuestion question = questionByPublicId.get(answer.questionId());
            if (question == null) {
                throw new BadRequestException("questionId does not belong to this quiz: " + answer.questionId());
            }
            List<QuizOption> options = optionRepository.findByQuestionId(question.getId());
            QuizOption selected = options.stream()
                    .filter(o -> optionPublicId(o).equals(answer.selectedOptionId()))
                    .findFirst()
                    .orElseThrow(() -> new BadRequestException("selectedOptionId does not belong to questionId " + answer.questionId()));
            if (selected.isCorrect()) {
                score++;
            }
            ObjectNode entry = objectMapper.createObjectNode();
            entry.put("questionId", answer.questionId().toString());
            entry.put("selectedOptionId", answer.selectedOptionId().toString());
            answersSnapshot.add(entry);
        }

        boolean passed = score >= quiz.getPassingScore();

        QuizAttempt attempt = new QuizAttempt();
        attempt.setQuiz(quiz);
        attempt.setUser(userRepository.getReferenceById(SecurityUtils.currentUserId()));
        attempt.setScore(score);
        attempt.setTotalQuestions(questions.size());
        attempt.setPassed(passed);
        attempt.setAnswers(answersSnapshot);
        attemptRepository.save(attempt);

        return new QuizAttemptResultResponse(score, questions.size(), passed);
    }

    @Transactional(readOnly = true)
    public ContentListResponse<QuizAttemptHistoryItemResponse> myAttempts(UUID quizId, Pageable pageable) {
        Quiz quiz = quizRepository.findByPublicId(quizId)
                .orElseThrow(() -> ResourceNotFoundException.of("Quiz", quizId));
        Page<QuizAttempt> page = attemptRepository.findByQuizIdAndUserIdOrderByAttemptedAtDesc(
                quiz.getId(), SecurityUtils.currentUserId(), pageable);
        return new ContentListResponse<>(page.getContent().stream()
                .map(a -> new QuizAttemptHistoryItemResponse(a.getScore(), a.getTotalQuestions(), a.isPassed(), a.getAttemptedAt()))
                .toList());
    }

    /**
     * Quiz_Questions/Quiz_Options are internal-id-only per the DB spec (no
     * public_id column) since they are never referenced across aggregate
     * boundaries in storage — but the API contract exposes opaque ids for
     * them. We derive a stable, deterministic UUID from the internal id
     * rather than adding more schema columns for what are pure read-model
     * identifiers scoped to a single quiz submission round-trip.
     */
    private UUID questionPublicId(QuizQuestion q) {
        return UUID.nameUUIDFromBytes(("quiz-question-" + q.getId()).getBytes());
    }

    private UUID optionPublicId(QuizOption o) {
        return UUID.nameUUIDFromBytes(("quiz-option-" + o.getId()).getBytes());
    }
}

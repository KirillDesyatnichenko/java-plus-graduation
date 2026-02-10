package ru.yandex.practicum.analyzer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.analyzer.config.RecommendationProperties;
import ru.yandex.practicum.analyzer.model.EventSimilarity;
import ru.yandex.practicum.analyzer.model.UserEventWeight;
import ru.yandex.practicum.analyzer.repository.EventScoreView;
import ru.yandex.practicum.analyzer.repository.EventSimilarityRepository;
import ru.yandex.practicum.analyzer.repository.UserEventWeightRepository;

import java.util.*;

@Service
@RequiredArgsConstructor
public class RecommendationService {
    private final UserEventWeightRepository userEventRepository;
    private final EventSimilarityRepository similarityRepository;
    private final RecommendationProperties recommendationProperties;

    public List<RecommendedEvent> getSimilarEvents(long eventId, long userId, int limit) {
        int targetLimit = resolveLimit(limit);

        // события, с которыми уже взаимодействовал пользователь
        List<UserEventWeight> interactions = userEventRepository.findAllByUserId(userId);
        Set<Long> interactedEvents = new HashSet<>();
        for (UserEventWeight interaction : interactions) {
            interactedEvents.add(interaction.getId().getEventId());
        }

        List<EventSimilarity> similarities = similarityRepository.findAllByEvent(eventId);
        List<RecommendedEvent> result = new ArrayList<>();
        for (EventSimilarity similarity : similarities) {
            long eventA = similarity.getId().getEventA();
            long eventB = similarity.getId().getEventB();
            long other = eventA == eventId ? eventB : eventA;

            // пользователь уже взаимодействовал с этим мероприятием — не рекомендуем его
            if (interactedEvents.contains(other)) {
                continue;
            }

            // если пользователь взаимодействовал с обоими мероприятиями пары,
            // то он не должен вносить вклад в рекомендацию этой пары
            boolean interactedWithA = interactedEvents.contains(eventA);
            boolean interactedWithB = interactedEvents.contains(eventB);
            if (interactedWithA && interactedWithB) {
                continue;
            }

            result.add(new RecommendedEvent(other, similarity.getScore()));
        }
        return result.stream()
                .sorted(comparator())
                .limit(targetLimit)
                .toList();
    }

    public List<RecommendedEvent> getRecommendationsForUser(long userId, int limit) {
        int targetLimit = resolveLimit(limit);
        List<UserEventWeight> interactions = userEventRepository.findAllByUserId(userId);
        if (interactions.isEmpty()) {
            return List.of();
        }

        Map<Long, Double> weights = new HashMap<>();
        Set<Long> interactedEvents = new HashSet<>();
        for (UserEventWeight interaction : interactions) {
            long eventId = interaction.getId().getEventId();
            weights.put(eventId, interaction.getWeight());
            interactedEvents.add(eventId);
        }

        Map<Long, ScoreAccumulator> candidates = new HashMap<>();
        List<EventSimilarity> similarities = similarityRepository.findAllByAnyEvent(interactedEvents);
        for (EventSimilarity similarity : similarities) {
            long eventA = similarity.getId().getEventA();
            long eventB = similarity.getId().getEventB();
            double score = similarity.getScore();

            if (interactedEvents.contains(eventA) && !interactedEvents.contains(eventB)) {
                accumulate(candidates, eventB, score, weights.get(eventA));
            } else if (interactedEvents.contains(eventB) && !interactedEvents.contains(eventA)) {
                accumulate(candidates, eventA, score, weights.get(eventB));
            }
        }

        return candidates.entrySet().stream()
                .map(entry -> new RecommendedEvent(entry.getKey(), entry.getValue().predicted()))
                .sorted(comparator())
                .limit(targetLimit)
                .toList();
    }

    public List<RecommendedEvent> getInteractionsCount(List<Long> eventIds, int limit) {
        int targetLimit = resolveLimit(limit);
        List<EventScoreView> scores = (eventIds == null || eventIds.isEmpty())
                ? userEventRepository.sumWeightsForAll()
                : userEventRepository.sumWeightsByEventIds(eventIds);

        return scores.stream()
                .map(view -> new RecommendedEvent(view.getEventId(), view.getScore() == null ? 0.0 : view.getScore()))
                .sorted(comparator())
                .limit(targetLimit)
                .toList();
    }

    private int resolveLimit(int limit) {
        return limit > 0 ? limit : recommendationProperties.getDefaultLimit();
    }

    private Comparator<RecommendedEvent> comparator() {
        return Comparator.comparingDouble(RecommendedEvent::score).reversed()
                .thenComparingLong(RecommendedEvent::eventId);
    }

    private void accumulate(Map<Long, ScoreAccumulator> candidates, long eventId, double similarity, double weight) {
        if (similarity <= 0.0) {
            return;
        }
        ScoreAccumulator acc = candidates.computeIfAbsent(eventId, id -> new ScoreAccumulator());
        acc.sumSimilarity += similarity;
        acc.sumWeighted += similarity * weight;
    }

    private static final class ScoreAccumulator {
        private double sumSimilarity;
        private double sumWeighted;

        private double predicted() {
            if (sumSimilarity <= 0.0) {
                return 0.0;
            }
            return sumWeighted / sumSimilarity;
        }
    }
}
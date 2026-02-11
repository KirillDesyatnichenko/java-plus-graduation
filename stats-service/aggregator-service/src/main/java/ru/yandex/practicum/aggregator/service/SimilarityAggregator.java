package ru.yandex.practicum.aggregator.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.yandex.practicum.aggregator.config.ActionWeightsProperties;

import java.util.*;

@Service
@RequiredArgsConstructor
public class SimilarityAggregator {
    private final ActionWeightsProperties weights;
    private final SimilarityProducer producer;

    private final Map<Integer, Map<Integer, Double>> userEventWeights = new HashMap<>();
    private final Map<Integer, Double> eventWeightSums = new HashMap<>();
    private final Map<Integer, Map<Integer, Double>> eventMinSums = new HashMap<>();
    private final Map<Integer, Set<Integer>> eventPairIndex = new HashMap<>();
    private final Map<Long, Double> publishedScores = new HashMap<>();

    public synchronized void handle(UserActionAvro action) {
        int userId = action.getUserId();
        int eventId = action.getEventId();
        double newWeight = resolveWeight(action.getActionType());

        Map<Integer, Double> userWeights = userEventWeights.computeIfAbsent(userId, id -> new HashMap<>());
        Double oldWeight = userWeights.get(eventId);
        if (oldWeight != null && oldWeight >= newWeight) {
            return;
        }

        double previousWeight = oldWeight == null ? 0.0 : oldWeight;
        userWeights.put(eventId, newWeight);
        eventWeightSums.put(eventId, eventWeightSums.getOrDefault(eventId, 0.0) + (newWeight - previousWeight));

        for (Map.Entry<Integer, Double> entry : userWeights.entrySet()) {
            int otherEventId = entry.getKey();
            if (otherEventId == eventId) {
                continue;
            }
            double otherWeight = entry.getValue();
            int eventA = Math.min(eventId, otherEventId);
            int eventB = Math.max(eventId, otherEventId);

            double oldMin = Math.min(previousWeight, otherWeight);
            double newMin = Math.min(newWeight, otherWeight);
            if (Double.compare(oldMin, newMin) == 0) {
                continue;
            }

            Map<Integer, Double> minSums = eventMinSums.computeIfAbsent(eventA, id -> new HashMap<>());
            minSums.put(eventB, minSums.getOrDefault(eventB, 0.0) + (newMin - oldMin));
            indexPair(eventA, eventB);
        }

        recomputeSimilarities(eventId);
    }

    private double resolveWeight(ActionTypeAvro actionType) {
        if (actionType == null) {
            return weights.getView();
        }
        return switch (actionType) {
            case VIEW -> weights.getView();
            case REGISTER -> weights.getRegister();
            case LIKE -> weights.getLike();
        };
    }

    private void indexPair(int eventA, int eventB) {
        eventPairIndex.computeIfAbsent(eventA, id -> new HashSet<>()).add(eventB);
        eventPairIndex.computeIfAbsent(eventB, id -> new HashSet<>()).add(eventA);
    }

    private void recomputeSimilarities(int eventId) {
        Set<Integer> related = eventPairIndex.getOrDefault(eventId, Collections.emptySet());
        for (int otherEventId : related) {
            int eventA = Math.min(eventId, otherEventId);
            int eventB = Math.max(eventId, otherEventId);
            long pairKey = composePairKey(eventA, eventB);
            double minSum = eventMinSums.getOrDefault(eventA, Collections.emptyMap())
                    .getOrDefault(eventB, 0.0);
            double sumA = eventWeightSums.getOrDefault(eventA, 0.0);
            double sumB = eventWeightSums.getOrDefault(eventB, 0.0);
            double score = (sumA <= 0.0 || sumB <= 0.0) ? 0.0 : minSum / Math.sqrt(sumA * sumB);
            Double prevScore = publishedScores.get(pairKey);
            if (prevScore == null || Double.compare(prevScore, score) != 0) {
                producer.send(eventA, eventB, score);
                publishedScores.put(pairKey, score);
            }
        }
    }

    private long composePairKey(int eventA, int eventB) {
        return ((long) eventA << 32) | (eventB & 0xffffffffL);
    }
}
package ru.yandex.practicum.analyzer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.yandex.practicum.analyzer.config.ActionWeightsProperties;
import ru.yandex.practicum.analyzer.model.UserEventKey;
import ru.yandex.practicum.analyzer.model.UserEventWeight;
import ru.yandex.practicum.analyzer.repository.UserEventWeightRepository;

@Service
@RequiredArgsConstructor
public class UserActionProcessor {
    private final UserEventWeightRepository repository;
    private final ActionWeightsProperties weights;

    @Transactional
    public void handle(UserActionAvro action) {
        long userId = action.getUserId();
        long eventId = action.getEventId();
        double weight = resolveWeight(action.getActionType());

        UserEventKey key = new UserEventKey(userId, eventId);
        UserEventWeight existing = repository.findById(key).orElse(null);
        if (existing != null && existing.getWeight() >= weight) {
            return;
        }

        if (existing == null) {
            repository.save(new UserEventWeight(key, weight));
        } else {
            existing.setWeight(weight);
            repository.save(existing);
        }
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
}
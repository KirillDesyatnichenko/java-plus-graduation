package ru.yandex.practicum.client.recommendation;

import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.stats.proto.recommendations.InteractionsCountRequestProto;
import ru.yandex.practicum.stats.proto.recommendations.RecommendationsControllerGrpc;
import ru.yandex.practicum.stats.proto.recommendations.RecommendedEventProto;
import ru.yandex.practicum.stats.proto.recommendations.SimilarEventsRequestProto;
import ru.yandex.practicum.stats.proto.recommendations.UserPredictionsRequestProto;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
@Slf4j
public class RecommendationsGrpcClient {

    @GrpcClient("analyzer-service")
    private RecommendationsControllerGrpc.RecommendationsControllerBlockingStub stub;

    public List<RecommendedEvent> getRecommendationsForUser(long userId, int maxResults) {
        UserPredictionsRequestProto request = UserPredictionsRequestProto.newBuilder()
                .setUserId(userId)
                .setMaxResults(maxResults)
                .build();
        return readStream(() -> stub.getRecommendationsForUser(request));
    }

    public List<RecommendedEvent> getSimilarEvents(long eventId, long userId, int maxResults) {
        SimilarEventsRequestProto request = SimilarEventsRequestProto.newBuilder()
                .setEventId(eventId)
                .setUserId(userId)
                .setMaxResults(maxResults)
                .build();
        return readStream(() -> stub.getSimilarEvents(request));
    }

    public List<RecommendedEvent> getInteractionsCount(List<Long> eventIds, int maxResults) {
        InteractionsCountRequestProto request = InteractionsCountRequestProto.newBuilder()
                .addAllEventIds(eventIds == null ? List.of() : eventIds)
                .setMaxResults(maxResults)
                .build();
        return readStream(() -> stub.getInteractionsCount(request));
    }

    private List<RecommendedEvent> readStream(StreamSupplier supplier) {
        try {
            Iterator<RecommendedEventProto> iterator = supplier.get();
            List<RecommendedEvent> result = new ArrayList<>();
            while (iterator.hasNext()) {
                RecommendedEventProto item = iterator.next();
                result.add(new RecommendedEvent(item.getEventId(), item.getScore()));
            }
            return result;
        } catch (Exception ex) {
            log.error("Не удалось получить рекомендации из Analyzer: {}", ex.getMessage());
            return List.of();
        }
    }

    @FunctionalInterface
    private interface StreamSupplier {
        Iterator<RecommendedEventProto> get();
    }
}
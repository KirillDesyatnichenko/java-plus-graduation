package ru.yandex.practicum.analyzer.grpc;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.yandex.practicum.analyzer.service.RecommendationService;
import ru.yandex.practicum.analyzer.service.RecommendedEvent;
import ru.yandex.practicum.stats.proto.recommendations.InteractionsCountRequestProto;
import ru.yandex.practicum.stats.proto.recommendations.RecommendationsControllerGrpc;
import ru.yandex.practicum.stats.proto.recommendations.RecommendedEventProto;
import ru.yandex.practicum.stats.proto.recommendations.SimilarEventsRequestProto;
import ru.yandex.practicum.stats.proto.recommendations.UserPredictionsRequestProto;

import java.util.List;

@GrpcService
@RequiredArgsConstructor
public class RecommendationsGrpcService extends RecommendationsControllerGrpc.RecommendationsControllerImplBase {
    private final RecommendationService recommendationService;

    @Override
    public void getRecommendationsForUser(UserPredictionsRequestProto request,
                                          StreamObserver<RecommendedEventProto> responseObserver) {
        try {
            List<RecommendedEvent> recommendations = recommendationService.getRecommendationsForUser(
                    request.getUserId(),
                    request.getMaxResults()
            );
            sendAll(recommendations, responseObserver);
        } catch (Exception ex) {
            responseObserver.onError(ex);
        }
    }

    @Override
    public void getSimilarEvents(SimilarEventsRequestProto request,
                                 StreamObserver<RecommendedEventProto> responseObserver) {
        try {
            List<RecommendedEvent> recommendations = recommendationService.getSimilarEvents(
                    request.getEventId(),
                    request.getUserId(),
                    request.getMaxResults()
            );
            sendAll(recommendations, responseObserver);
        } catch (Exception ex) {
            responseObserver.onError(ex);
        }
    }

    @Override
    public void getInteractionsCount(InteractionsCountRequestProto request,
                                     StreamObserver<RecommendedEventProto> responseObserver) {
        try {
            List<RecommendedEvent> recommendations = recommendationService.getInteractionsCount(
                    request.getEventIdsList(),
                    request.getMaxResults()
            );
            sendAll(recommendations, responseObserver);
        } catch (Exception ex) {
            responseObserver.onError(ex);
        }
    }

    private void sendAll(List<RecommendedEvent> recommendations,
                         StreamObserver<RecommendedEventProto> responseObserver) {
        for (RecommendedEvent recommendation : recommendations) {
            responseObserver.onNext(RecommendedEventProto.newBuilder()
                    .setEventId(recommendation.eventId())
                    .setScore(recommendation.score())
                    .build());
        }
        responseObserver.onCompleted();
    }
}
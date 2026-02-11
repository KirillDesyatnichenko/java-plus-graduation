package ru.yandex.practicum.client.recommendation;

import com.google.protobuf.Timestamp;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.stats.proto.collector.ActionTypeProto;
import ru.yandex.practicum.stats.proto.collector.UserActionControllerGrpc;
import ru.yandex.practicum.stats.proto.collector.UserActionProto;

import java.time.Instant;

@Service
@Slf4j
public class CollectorGrpcClient {

    @GrpcClient("collector-service")
    private UserActionControllerGrpc.UserActionControllerBlockingStub stub;

    public void collectUserAction(long userId, long eventId, UserActionType actionType) {
        try {
            UserActionProto request = UserActionProto.newBuilder()
                    .setUserId(userId)
                    .setEventId(eventId)
                    .setActionType(mapActionType(actionType))
                    .setTimestamp(toTimestamp(Instant.now()))
                    .build();
            stub.collectUserAction(request);
        } catch (Exception ex) {
            log.error("Не удалось отправить действие пользователя в Collector: {}", ex.getMessage());
        }
    }

    private ActionTypeProto mapActionType(UserActionType actionType) {
        if (actionType == null) {
            return ActionTypeProto.ACTION_VIEW;
        }
        return switch (actionType) {
            case VIEW -> ActionTypeProto.ACTION_VIEW;
            case REGISTER -> ActionTypeProto.ACTION_REGISTER;
            case LIKE -> ActionTypeProto.ACTION_LIKE;
        };
    }

    private Timestamp toTimestamp(Instant instant) {
        return Timestamp.newBuilder()
                .setSeconds(instant.getEpochSecond())
                .setNanos(instant.getNano())
                .build();
    }
}
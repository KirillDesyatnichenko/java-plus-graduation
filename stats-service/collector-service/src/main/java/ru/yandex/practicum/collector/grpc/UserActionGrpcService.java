package ru.yandex.practicum.collector.grpc;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.yandex.practicum.collector.service.UserActionProducer;
import ru.yandex.practicum.stats.proto.collector.ActionTypeProto;
import ru.yandex.practicum.stats.proto.collector.UserActionControllerGrpc;
import ru.yandex.practicum.stats.proto.collector.UserActionProto;

import java.time.Instant;

@GrpcService
@RequiredArgsConstructor
public class UserActionGrpcService extends UserActionControllerGrpc.UserActionControllerImplBase {
    private final UserActionProducer producer;

    @Override
    public void collectUserAction(UserActionProto request, StreamObserver<Empty> responseObserver) {
        UserActionAvro action = new UserActionAvro();
        action.setUserId(Math.toIntExact(request.getUserId()));
        action.setEventId(Math.toIntExact(request.getEventId()));
        action.setActionType(mapActionType(request.getActionType()));
        action.setTimestamp(toInstant(request));

        producer.send(action);

        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    private ActionTypeAvro mapActionType(ActionTypeProto actionType) {
        if (actionType == null) {
            return ActionTypeAvro.VIEW;
        }
        return switch (actionType) {
            case ACTION_VIEW -> ActionTypeAvro.VIEW;
            case ACTION_REGISTER -> ActionTypeAvro.REGISTER;
            case ACTION_LIKE -> ActionTypeAvro.LIKE;
            case UNRECOGNIZED -> ActionTypeAvro.VIEW;
        };
    }

    private Instant toInstant(UserActionProto request) {
        if (!request.hasTimestamp()) {
            return Instant.now();
        }
        long seconds = request.getTimestamp().getSeconds();
        long nanos = request.getTimestamp().getNanos();
        return Instant.ofEpochSecond(seconds, nanos);
    }
}
package ru.yandex.practicum.stats.avro;

import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.common.serialization.Serializer;

import java.io.ByteArrayOutputStream;
import java.util.Map;

public class SpecificRecordAvroSerializer implements Serializer<SpecificRecord> {

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
    }

    @Override
    public byte[] serialize(String topic, SpecificRecord data) {
        if (data == null) {
            return null;
        }
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(output, null);
            SpecificDatumWriter<SpecificRecord> writer = new SpecificDatumWriter<>(data.getSchema());
            writer.write(data, encoder);
            encoder.flush();
            return output.toByteArray();
        } catch (Exception ex) {
            throw new IllegalStateException("Не удалось сериализовать Avro сообщение", ex);
        }
    }

    @Override
    public void close() {
    }
}
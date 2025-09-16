package com.trading.aggregator.v2.utils;

import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;

public class ProtoJsonUtil {

    private void ProtoJsonUtils() {}

    public static String toJson(Message proto) {
        try {
            return JsonFormat.printer()
                    .includingDefaultValueFields()
                    .omittingInsignificantWhitespace()
                    .print(proto);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert proto to JSON", e);
        }
    }

    /** Convert JSON string to Protobuf Message */
    public static <T extends Message> T fromJson(String json, java.util.function.Supplier<? extends Message.Builder> builderSupplier) {
        try {
            Message.Builder builder = builderSupplier.get();
            JsonFormat.parser().ignoringUnknownFields().merge(json, builder);
            return (T) builder.build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JSON to protobuf", e);
        }
    }

}

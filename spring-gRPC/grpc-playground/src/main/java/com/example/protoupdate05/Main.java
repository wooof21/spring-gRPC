package com.example.protoupdate05;

import com.example.grpcplayground.models.protoupdate.v1.Television;
import com.example.grpcplayground.models.protoupdate.v2.Type;
import com.example.protoupdate05.parser.V1Parser;
import com.example.protoupdate05.parser.V2Parser;
import com.example.protoupdate05.parser.V4Parser;
import com.google.protobuf.InvalidProtocolBufferException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Main {

    public static void main(String[] args) throws InvalidProtocolBufferException {

        log.info("--------------------------------------------");
        var tv1 = Television.newBuilder()
                .setBrand("Samsung")
                .setYear(2000)
                .build();

        V1Parser.parse(tv1.toByteArray());

        log.info("--------------------------------------------");

        var tv2 = com.example.grpcplayground.models.protoupdate.v2.Television.newBuilder()
                .setBrand("Samsung")
                .setModel(2001)
                .setType(Type.HD)
                .build();

        /**
         *  still able to parse using the old parser
         *  but the updated field(year to model), will be parsed as old field
         *  and the new field(type) will be ignored
         *  since when proto serialize/deserialize, it's not using field names,
         *  but field numbers, so the old parser will still work
         *  eg. old field(year) -> 2, new field(model) -> 2
         *  proto only serialize the field number(2),
         *  when deserializing, it will look for field number(2) in the old parser,
         *  and it will find the old field(year) and parse it
         */
        log.info("Use V1Parser to parse V2 data.");
        V1Parser.parse(tv2.toByteArray());

        // use new parser to parse old data
        // the old field(year) will be parsed as new field(model)
        log.info("Use V2Parser to parse V1 data.");
        V2Parser.parse(tv1.toByteArray());

        log.info("--------------------------------------------");

        var tv3 = com.example.grpcplayground.models.protoupdate.v3.Television.newBuilder()
                .setBrand("Samsung")
                .setModel(2001)
                .setType(com.example.grpcplayground.models.protoupdate.v3.Type.UHD)
                .build();

        /**
         * When the field number is also changed,
         * old parser will not be able to parse the new data
         */
        log.info("Use V1Parser to parse V3 data.");
        /**
         * Television V1: brand: "Samsung"
         * 3: 2001
         * 4: 1
         */
        V1Parser.parse(tv3.toByteArray());

        /**
         * Television V2: brand: "Samsung"
         * type: UNKNOWN_ENUM_VALUE_Type_2001
         * 4: 1
         */
        log.info("Use V2Parser to parse V3 data.");
        V2Parser.parse(tv3.toByteArray());

        log.info("--------------------------------------------");

        var tv4 = com.example.grpcplayground.models.protoupdate.v4.Television.newBuilder()
                .setBrand("Samsung")
                .setType(com.example.grpcplayground.models.protoupdate.v4.Type.UHD)
                .build();
        log.info("Use V4Parser to parse V4 data.");
        V4Parser.parse(tv4.toByteArray());

        log.info("Use V1Parser to parse V4 data.");
        V1Parser.parse(tv4.toByteArray());

        log.info("Use V2Parser to parse V4 data.");
        V2Parser.parse(tv4.toByteArray());
    }

}

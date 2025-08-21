package com.example.protoupdate05.parser;

import com.example.grpcplayground.models.protoupdate.v1.Television;
import com.google.protobuf.InvalidProtocolBufferException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class V1Parser {

    public static void parse(byte[] data) throws InvalidProtocolBufferException {
        var tv = Television.parseFrom(data);

        log.info("Television V1: {}", tv);
    }
}

package com.example.protoupdate05.parser;

import com.example.grpcplayground.models.protoupdate.v4.Television;
import com.google.protobuf.InvalidProtocolBufferException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class V4Parser {

    public static void parse(byte[] data) throws InvalidProtocolBufferException {
        var tv = Television.parseFrom(data);

        log.info("Television V4: {}", tv);
    }
}

package com.example.type03.collection;

import com.example.grpcplayground.models.types.collection.Book;
import com.example.grpcplayground.models.types.collection.Library;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Set;

@Slf4j
public class Main {

    public static void main(String[] args) {

        list();

        set();
    }

    private static void list() {
        var b1 = Book.newBuilder()
                .setTitle("Effective Java")
                .setAuthor("Joshua Bloch")
                .setPublicationYear(1997)
                .build();
        var b2 = Book.newBuilder()
                .setTitle("Clean Code")
                .setAuthor("Robert C. Martin")
                .setPublicationYear(2008)
                .build();
        var b3 = Book.newBuilder()
                .setTitle("Design Patterns")
                .setAuthor("Erich Gamma, Richard Helm, Ralph Johnson, John Vlissides")
                .setPublicationYear(1994)
                .build();

        var list = List.of(b1, b2, b3);

        var library = Library.newBuilder()
                .setName("City Library")
                // add a list of books
                .addAllBooks(list)
//                .addBooks(b1)
//                .addBooks(b2)
//                .addBooks(b3)
                .build();

        log.info("Library List: {}", library);

        log.info("Get Book by Index 1: {}", library.getBooks(1));

        log.info("Get All Books: {}", library.getBooksList());
    }

    private static void set() {
        var b1 = Book.newBuilder()
                .setTitle("Effective Java")
                .setAuthor("Joshua Bloch")
                .setPublicationYear(1997)
                .build();
        var b2 = Book.newBuilder()
                .setTitle("Effective Java")
                .setAuthor("Joshua Bloch")
                .setPublicationYear(1997)
                .build();

        var set = new java.util.HashSet<>(Set.of(b1));
        set.add(b2);

        var library = Library.newBuilder()
                .setName("City Library")
                // add a list of books
                .addAllBooks(set)
                .build();

        log.info("Library Set: {}", library);
    }
}

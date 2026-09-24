package com.vectordb.service;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TextChunkerTest {

    private final TextChunker chunker = new TextChunker();

    @Test
    public void testShortTextSingleChunk() {
        String text = "Quick brown fox jumps over the lazy dog.";
        List<String> chunks = chunker.chunkText(text, 50, 10);

        assertEquals(1, chunks.size());
        assertEquals(text, chunks.get(0));
    }

    @Test
    public void testLongTextMultiChunkWithOverlap() {
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= 30; i++) {
            sb.append("word").append(i).append(" ");
        }
        String text = sb.toString().trim();

        List<String> chunks = chunker.chunkText(text, 10, 3);

        assertTrue(chunks.size() > 1);
        assertTrue(chunks.get(0).startsWith("word1"));
        assertTrue(chunks.get(1).contains("word8") || chunks.get(1).contains("word7"));
    }

    @Test
    public void testEmptyOrNullText() {
        assertTrue(chunker.chunkText(null, 10, 2).isEmpty());
        assertTrue(chunker.chunkText("   ", 10, 2).isEmpty());
    }
}

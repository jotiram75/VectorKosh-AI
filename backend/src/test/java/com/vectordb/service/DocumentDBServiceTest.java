package com.vectordb.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DocumentDBServiceTest {

    private DocumentDBService docService;

    @BeforeEach
    public void setUp() {
        docService = new DocumentDBService();
    }

    @Test
    public void testInsertAndSearchDoc() {
        List<Float> emb1 = List.of(0.9f, 0.1f, 0.0f);
        List<Float> emb2 = List.of(0.1f, 0.9f, 0.0f);

        int id1 = docService.insert("Java Tutorial", "Java is a programming language.", emb1);
        int id2 = docService.insert("Python Tutorial", "Python is interpreted.", emb2);

        assertEquals(2, docService.size());
        assertEquals(3, docService.getDims());

        List<DocumentDBService.DocHit> hits = docService.search(List.of(0.85f, 0.15f, 0.0f), 2);

        assertFalse(hits.isEmpty());
        assertEquals(id1, hits.get(0).item.getId());
        assertEquals("Java Tutorial", hits.get(0).item.getTitle());
    }

    @Test
    public void testRemoveDocResetsDimsWhenEmpty() {
        List<Float> emb = List.of(1.0f, 0.0f);
        int id = docService.insert("Test Doc", "Text", emb);
        assertEquals(2, docService.getDims());

        boolean removed = docService.remove(id);
        assertTrue(removed);
        assertEquals(0, docService.size());
        assertEquals(0, docService.getDims());
    }
}

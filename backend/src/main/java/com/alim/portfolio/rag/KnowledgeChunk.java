package com.alim.portfolio.rag;

import lombok.Getter;

/**
 * ─── RAG: Knowledge Chunk ────────────────────────────────────────────────────
 *
 * A KnowledgeChunk represents one piece of Alim's knowledge base.
 * Think of it like a flashcard — a small, focused passage about one topic.
 *
 * Why small chunks?
 * If you store "everything about Alim" as one big document, your retrieval
 * becomes useless — every query returns the same giant blob. Small, focused
 * chunks let you retrieve only what's actually relevant to the question.
 *
 * Each chunk stores:
 *  - text   : the raw text content
 *  - vector : a numerical representation of the text's meaning (embedding)
 *  - topic  : a label for debugging / logging
 */
@Getter
public class KnowledgeChunk {

    private final String topic;
    private final String text;
    private final double[] vector; // TF-IDF style embedding

    public KnowledgeChunk(String topic, String text, double[] vector) {
        this.topic  = topic;
        this.text   = text;
        this.vector = vector;
    }
}

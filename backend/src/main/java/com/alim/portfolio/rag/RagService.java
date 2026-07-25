package com.alim.portfolio.rag;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * ─── RAG: Retrieval-Augmented Generation Service ─────────────────────────────
 *
 * THEORY: What this service does
 * ─────────────────────────────
 * RAG has two phases:
 *
 *  1. INDEXING (at startup, once):
 *     - Split Alim's knowledge into small focused chunks
 *     - Convert each chunk's text into a vector (embedding)
 *     - Store all (chunk, vector) pairs in an in-memory "vector store"
 *
 *  2. RETRIEVAL (on each user query):
 *     - Convert the user's question into a vector
 *     - Find the chunks whose vectors are most similar to the query vector
 *     - Return the top-K chunks as context for the LLM
 *
 * EMBEDDING APPROACH: Term Frequency (TF) with vocabulary
 * ─────────────────────────────────────────────────────────
 * Real RAG uses a neural embedding model (e.g. OpenAI text-embedding-3-small)
 * that maps text to dense 1536-dimensional vectors capturing deep semantics.
 *
 * We implement a lightweight TF vector over a fixed vocabulary.
 * It captures keyword overlap, which works well for a structured portfolio
 * knowledge base. The same math (cosine similarity) applies — only the
 * quality of the embedding differs.
 *
 * To upgrade: replace buildVector() with a call to an embedding API.
 * Everything else — the chunk store, cosine similarity, retrieval — stays identical.
 *
 * COSINE SIMILARITY
 * ─────────────────
 * Measures the angle between two vectors. Range: -1 to 1.
 *   1.0 = identical direction (same meaning)
 *   0.0 = perpendicular (unrelated)
 *  -1.0 = opposite direction
 *
 * Formula: cos(θ) = (A · B) / (|A| × |B|)
 *   A · B  = dot product (sum of element-wise products)
 *   |A|    = magnitude (square root of sum of squares)
 */
@Slf4j
@Service
public class RagService {

    // ─── Vector Vocabulary ────────────────────────────────────────────────────
    // These are the terms we use to build vectors. Each term maps to one
    // dimension in the vector space. More terms = higher-dimensional, more precise.
    private static final String[] VOCABULARY = {
        // Work & Career
        "work", "job", "company", "brac", "engineer", "software", "developer",
        "experience", "professional", "career", "employed", "office", "gulshan",
        "manager", "team",
        // Education
        "education", "university", "degree", "study", "graduate", "cgpa", "gpa",
        "bachelor", "noakhali", "science", "technology", "hsc", "ssc", "school",
        // Skills & Tech
        "java", "spring", "angular", "postgresql", "docker", "redis", "typescript",
        "backend", "frontend", "api", "rest", "database", "hibernate", "jpa",
        "security", "microservices", "fullstack", "skill", "technology", "tech",
        "stack", "programming", "language", "framework", "jUnit", "mockito",
        // Projects
        "project", "built", "smartmf", "agmai", "payroll", "bits", "hrpayroll",
        "users", "scalable", "optimized", "query", "performance",
        // Competitive Programming
        "competitive", "icpc", "codeforces", "leetcode", "codechef", "solve",
        "problem", "contest", "programming", "algorithm", "rank", "pupil",
        "knight", "hacker", "meta",
        // Personal
        "personal", "hobby", "cricket", "sister", "family", "religion", "islam",
        "birthday", "dob", "age", "height", "weight", "address", "dhaka",
        "bangladesh", "name", "asif", "alim",
        // Goals & Future
        "goal", "future", "plan", "senior", "lead", "grow", "opportunity",
        "open", "hire", "available", "remote", "salary",
        // Contact
        "contact", "email", "linkedin", "github", "reach", "connect"
    };

    // vocabulary index map for O(1) lookup during vector building
    private final Map<String, Integer> vocabIndex = new HashMap<>();

    // in-memory vector store: our "database" of (chunk, vector) pairs
    private final List<KnowledgeChunk> knowledgeBase = new ArrayList<>();

    @PostConstruct
    public void init() {
        // Build vocabulary index
        for (int i = 0; i < VOCABULARY.length; i++) {
            vocabIndex.put(VOCABULARY[i], i);
        }

        // ─── Index the knowledge base ─────────────────────────────────────────
        // Each chunk is a focused passage about ONE topic. This is intentional.
        // Mixing topics in a chunk hurts retrieval precision.
        indexChunk("work_current",
            "Alim works at Brac IT Services as a Software Engineer since December 2024. " +
            "His office is at Nafi Tower (16th floor), Gulshan 1, Dhaka 1212. " +
            "His line manager is Abdul Ahad and skip manager is Md. Khairul Basher.");

        indexChunk("work_projects",
            "Alim built scalable REST APIs for SmartMF serving 5 million users and agmai " +
            "serving 100k users. He optimized PostgreSQL queries achieving 40% faster load time. " +
            "He also developed BitsHrPayroll for employee management.");

        indexChunk("education_university",
            "Alim graduated with a B.Sc. in Computer Science and Engineering (CSE) " +
            "from Noakhali Science and Technology University in 2024 with a CGPA of 3.46 out of 4.0.");

        indexChunk("education_earlier",
            "Alim completed HSC in Science from Noakhali Govt. College with GPA 4.92/5.00 in 2018. " +
            "SSC from Noannai Union High School with GPA 4.89/5.00 in 2016. " +
            "JSC from Al Farooq Academy School and College with GPA 4.88/5.00 in 2013. " +
            "PSC from Begum Saleha Jamal Ideal Kindergarten with first division in 2010.");

        indexChunk("skills_backend",
            "Alim's backend skills include Java, Spring Boot, Spring Security, Hibernate, JPA, " +
            "REST API design, PostgreSQL, MySQL, Redis, Docker, CI/CD pipelines, JUnit, Mockito. " +
            "He is strong in OOP, SOLID principles, Clean Architecture, and System Design.");

        indexChunk("skills_frontend",
            "Alim's frontend skills include Angular and TypeScript. " +
            "He is primarily a backend-focused full-stack developer.");

        indexChunk("competitive_programming",
            "Alim is a competitive programmer who has solved over 4200 problems. " +
            "He participated in ICPC, IUCP, NCPC, Meta Hacker Cup, Samsung contests. " +
            "His ratings: Pupil on Codeforces, 4-star on CodeChef, Knight on LeetCode.");

        indexChunk("personal_life",
            "Alim was born on 23 March 1999, is 27 years old, 5 feet 5 inches tall, weighs 62 kg. " +
            "He has one sister, no girlfriend. His hobby is cricket. His religion is Islam. " +
            "He lives at Mohakhali TV Gate, Dhaka 1212, Bangladesh.");

        indexChunk("goals_availability",
            "Alim is open to new job opportunities, especially backend-heavy or full-stack roles. " +
            "He wants to grow into a Senior Engineer and eventually a tech lead. " +
            "He is interested in system design and distributed systems. " +
            "He is open to remote work. He prefers discussing salary directly.");

        indexChunk("contact",
            "Alim can be reached by email at asifalimnstu@gmail.com. " +
            "He usually responds within 24 hours and is happy to connect with people in tech.");

        log.info("RAG: Indexed {} knowledge chunks", knowledgeBase.size());
    }

    /**
     * Retrieve the most relevant chunks for a given query.
     *
     * This is the "R" in RAG — Retrieval.
     * We embed the query, then rank all chunks by cosine similarity.
     *
     * @param query  the user's question
     * @param topK   how many chunks to return (typically 2-4)
     * @return       ordered list of most relevant chunks
     */
    public List<KnowledgeChunk> retrieve(String query, int topK) {
        double[] queryVector = buildVector(query);

        return knowledgeBase.stream()
            .map(chunk -> {
                double score = cosineSimilarity(queryVector, chunk.getVector());
                log.debug("RAG: chunk='{}' score={:.3f}", chunk.getTopic(), score);
                return Map.entry(chunk, score);
            })
            .sorted(Map.Entry.<KnowledgeChunk, Double>comparingByValue().reversed())
            .limit(topK)
            .peek(e -> log.debug("RAG: chunk='{}' score={:.3f}", e.getKey().getTopic(), e.getValue()))
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }

    /**
     * Format retrieved chunks into a context block for injection into the prompt.
     *
     * The LLM sees this block and uses it to answer accurately.
     */
    public String buildContext(List<KnowledgeChunk> chunks) {
        if (chunks.isEmpty()) return "";

        StringBuilder sb = new StringBuilder();
        sb.append("\n\n== Relevant Context ==\n");
        chunks.forEach(c -> sb.append("- ").append(c.getText()).append("\n"));
        return sb.toString();
    }

    // ─── Private helpers ──────────────────────────────────────────────────────

    private void indexChunk(String topic, String text) {
        double[] vector = buildVector(text);
        knowledgeBase.add(new KnowledgeChunk(topic, text, vector));
    }

    /**
     * Build a TF (term frequency) vector over our fixed vocabulary.
     *
     * How it works:
     *  1. Tokenize the text into lowercase words
     *  2. For each vocab term, count how many times it appears
     *  3. The result is a vector where each dimension = count of that term
     *
     * In production: replace this with an embedding API call.
     * The vector dimensions would be ~1536 instead of VOCABULARY.length,
     * but the downstream cosine similarity code stays exactly the same.
     */
    private double[] buildVector(String text) {
        double[] vector = new double[VOCABULARY.length];
        String[] tokens = text.toLowerCase()
            .replaceAll("[^a-z0-9 ]", " ")
            .split("\\s+");

        for (String token : tokens) {
            Integer idx = vocabIndex.get(token);
            if (idx != null) {
                vector[idx]++;
            }
        }
        return vector;
    }

    /**
     * Cosine similarity between two vectors.
     *
     * cos(θ) = (A · B) / (|A| × |B|)
     *
     * Returns a value between 0 and 1 for non-negative vectors.
     * 1 = identical, 0 = completely unrelated.
     */
    private double cosineSimilarity(double[] a, double[] b) {
        double dot = 0, magA = 0, magB = 0;
        for (int i = 0; i < a.length; i++) {
            dot  += a[i] * b[i];
            magA += a[i] * a[i];
            magB += b[i] * b[i];
        }
        if (magA == 0 || magB == 0) return 0;
        return dot / (Math.sqrt(magA) * Math.sqrt(magB));
    }
}

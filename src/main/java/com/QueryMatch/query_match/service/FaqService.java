package com.QueryMatch.query_match.service;

 // adjust to wherever your FaqEntry lives
import com.QueryMatch.query_match.records.FaqEntry;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.IntStream;

@Service
public class FaqService {

    private static final Set<String> STOP_WORDS = Set.copyOf(List.of(
            "a", "an", "the", "i", "me", "my", "we", "you", "your", "is", "are", "was", "be", "am",
            "do", "does", "did", "how", "what", "why", "can", "to", "of", "in", "on", "for", "and",
            "or", "it", "this", "that", "with", "get", "there", "if", "so", "as", "at", "by"));

    private final double threshold;
    private final List<FaqEntry> faqs = new ArrayList<>();
    private final Map<String, Double> idf = new HashMap<>();
    private List<Map<String, Double>> faqVectors = List.of();

    public FaqService(@Value("${faq.match.threshold:0.2}") double threshold) {
        this.threshold = threshold;
    }

    @PostConstruct
    void init() {
        faqs.addAll(List.of(

                new FaqEntry(
                        "What is artificial intelligence and how does it work?",
                        "Artificial intelligence enables machines to perform tasks such as reasoning, prediction, language processing, and pattern recognition using algorithms and trained models."
                ),

                new FaqEntry(
                        "What is the difference between AI, machine learning, and deep learning?",
                        "AI is the broader field of building intelligent systems. Machine learning is a subset of AI that learns patterns from data, while deep learning uses multilayer neural networks to learn complex representations."
                ),

                new FaqEntry(
                        "What is a large language model and how does it generate responses?",
                        "A large language model is trained on large amounts of data to predict and generate text. It generates responses by repeatedly predicting likely next tokens based on the input and its learned parameters."
                ),

                new FaqEntry(
                        "What is Retrieval Augmented Generation?",
                        "Retrieval Augmented Generation combines information retrieval with language generation. It retrieves relevant external information and supplies it as context to an LLM to produce a grounded response."
                ),

                new FaqEntry(
                        "What is the difference between semantic search and keyword search?",
                        "Keyword search primarily matches words and terms, whereas semantic search uses representations of meaning to find relevant content even when the query and document use different wording."
                ),

                new FaqEntry(
                        "How do embeddings help in finding similar questions?",
                        "Embeddings represent text as numerical vectors. A similarity algorithm such as cosine similarity compares these vectors to identify questions with similar meanings."
                ),

                new FaqEntry(
                        "Can semantic search work without an LLM?",
                        "Yes. A pretrained embedding model can generate text vectors without generating responses. The application can compare those vectors and retrieve predefined answers without using an LLM."
                ),

                new FaqEntry(
                        "What is vectorization in natural language processing?",
                        "Vectorization converts text into numerical representations that algorithms can process. Common methods include Bag of Words, TF-IDF, and learned word or sentence embeddings."
                ),

                new FaqEntry(
                        "What is cosine similarity and why is it used?",
                        "Cosine similarity measures the cosine of the angle between two vectors. It is commonly used in information retrieval to compare the direction of text embeddings independently of their magnitudes."
                ),

                new FaqEntry(
                        "How can I build a semantic search engine using Java?",
                        "Use a pretrained sentence-embedding model to vectorize stored documents and user queries. Store the vectors, calculate cosine similarity or use a vector index, and return the highest-ranking results."
                ),

                new FaqEntry(
                        "What is Spring Boot auto-configuration?",
                        "Spring Boot auto-configuration automatically configures application components based on the dependencies on the classpath and the application's existing configuration."
                ),

                new FaqEntry(
                        "How does dependency injection work in Spring?",
                        "Spring manages application objects as beans and supplies their dependencies through constructor, setter, or field injection. Constructor injection is generally preferred for mandatory dependencies."
                ),

                new FaqEntry(
                        "What is the difference between monolithic and microservices architecture?",
                        "A monolithic application packages most functionality into one deployable unit. Microservices split functionality into independently deployable services, introducing additional operational and distributed-system complexity."
                ),

                new FaqEntry(
                        "How do microservices communicate with each other?",
                        "Microservices can communicate synchronously through HTTP or gRPC and asynchronously through message brokers such as Kafka or RabbitMQ. The choice depends on latency, reliability, coupling, and consistency requirements."
                ),

                new FaqEntry(
                        "How do you handle failures in a distributed system?",
                        "Distributed systems use timeouts, bounded retries with backoff, circuit breakers, idempotency, observability, and graceful degradation to limit the impact of failures."
                ),

                new FaqEntry(
                        "What is the difference between synchronous and asynchronous communication?",
                        "Synchronous communication makes the caller wait for a response. Asynchronous communication allows work to continue independently, often using queues, events, or callbacks."
                ),

                new FaqEntry(
                        "How does caching improve application performance?",
                        "Caching stores frequently accessed data in a faster storage layer to reduce database queries, computation, and response latency. Cache invalidation, expiration, and consistency must be managed carefully."
                ),

                new FaqEntry(
                        "What is the difference between Redis and a relational database?",
                        "Redis is an in-memory data store commonly used for caching, counters, and fast data access. Relational databases provide structured tables, SQL queries, and transactional capabilities for persistent business data."
                ),

                new FaqEntry(
                        "How do database indexes improve query performance?",
                        "Database indexes maintain additional data structures that help locate rows without scanning the entire table. They can accelerate reads but consume storage and add overhead to inserts, updates, and deletes."
                ),

                new FaqEntry(
                        "What is database transaction isolation?",
                        "Transaction isolation controls how concurrent transactions interact and which intermediate changes they can observe. Higher isolation can prevent more anomalies but may reduce concurrency."
                ),

                new FaqEntry(
                        "What is eventual consistency in distributed systems?",
                        "Eventual consistency means that replicas may temporarily contain different values but are expected to converge when updates stop and replication completes."
                ),

                new FaqEntry(
                        "How does a load balancer work?",
                        "A load balancer distributes incoming requests across multiple servers using policies such as round robin or least connections. It can also perform health checks and route traffic away from unhealthy instances."
                ),

                new FaqEntry(
                        "What is the difference between Docker and Kubernetes?",
                        "Docker provides tools for building and running containers. Kubernetes orchestrates containers across a cluster and manages deployment, scaling, networking, and recovery."
                ),

                new FaqEntry(
                        "How does CI/CD improve software delivery?",
                        "Continuous integration automates code integration and testing. Continuous delivery or deployment automates release preparation or deployment, reducing manual effort and enabling more frequent releases."
                ),

                new FaqEntry(
                        "What is horizontal scaling versus vertical scaling?",
                        "Vertical scaling increases the resources of a single machine. Horizontal scaling adds more machines or application instances to distribute workload and improve capacity."
                ),

                new FaqEntry(
                        "How should REST APIs handle errors?",
                        "REST APIs should use appropriate HTTP status codes, consistent error response structures, meaningful messages, input validation, and centralized exception handling without exposing sensitive internal details."
                ),

                new FaqEntry(
                        "What is API idempotency and why is it important?",
                        "An operation is idempotent when repeating it produces the same intended effect as performing it once. Idempotency keys can help prevent duplicate processing of retryable payment or order requests."
                ),

                new FaqEntry(
                        "How do you secure a Spring Boot REST API?",
                        "Use Spring Security, authenticate requests, enforce authorization, validate input, protect secrets, use TLS, configure CORS appropriately, and apply rate limiting and security monitoring where needed."
                ),

                new FaqEntry(
                        "How do Java virtual threads improve scalability?",
                        "Virtual threads are lightweight threads managed by the Java runtime. They can support large numbers of concurrent tasks, particularly those that spend time waiting on blocking I/O, without requiring one operating-system thread per task."
                ),

                new FaqEntry(
                        "What is the difference between concurrency and parallelism?",
                        "Concurrency is the management of multiple tasks whose execution overlaps. Parallelism means multiple tasks actually execute at the same time, typically on different processing cores."
                ),

                new FaqEntry(
                        "How do you monitor a production Spring Boot application?",
                        "Use Spring Boot Actuator for health and metrics endpoints, centralized logs, distributed tracing, and monitoring platforms to track latency, error rates, resource usage, and service availability."
                )

        ));

        // Index question + answer text so more keywords can match
        List<List<String>> docs = faqs.stream()
                .map(f -> tokenize(f.question() + " " + f.answer()))
                .toList();

        // Inverse document frequency
        Map<String, Integer> docFreq = new HashMap<>();
        for (List<String> doc : docs) {
            for (String term : new HashSet<>(doc)) {
                docFreq.merge(term, 1, Integer::sum);
            }
        }
        int n = docs.size();
        docFreq.forEach((term, df) -> idf.put(term, Math.log((1.0 + n) / (1.0 + df)) + 1.0));

        this.faqVectors = docs.stream().map(this::toVector).toList();
    }

    public Optional<FaqMatch> findAnswer(String userQuestion) {
        List<FaqMatch> top = search(userQuestion, 1);
        if (top.isEmpty() || top.get(0).score() < threshold) {
            return Optional.empty();
        }
        return Optional.of(top.get(0));
    }

    public List<FaqMatch> search(String userQuestion, int topK) {
        Map<String, Double> queryVec = toVector(tokenize(userQuestion));

        return IntStream.range(0, faqs.size())
                .mapToObj(i -> new FaqMatch(
                        faqs.get(i).question(),
                        faqs.get(i).answer(),
                        cosine(queryVec, faqVectors.get(i))))
                .sorted(Comparator.comparingDouble(FaqMatch::score).reversed())
                .limit(topK)
                .toList();
    }

    // ---- helpers ----

    private List<String> tokenize(String text) {
        List<String> tokens = new ArrayList<>();
        for (String raw : text.toLowerCase().split("[^a-z0-9]+")) {
            if (raw.isBlank() || STOP_WORDS.contains(raw)) continue;
            tokens.add(stem(raw));
        }
        return tokens;
    }

    // Very light stemming so "keys"/"key", "requests"/"request", "getting"/"get" line up
    private String stem(String w) {
        if (w.length() > 5 && w.endsWith("ing")) return w.substring(0, w.length() - 3);
        if (w.length() > 4 && w.endsWith("ed")) return w.substring(0, w.length() - 2);
        if (w.length() > 3 && w.endsWith("s") && !w.endsWith("ss")) return w.substring(0, w.length() - 1);
        return w;
    }

    private Map<String, Double> toVector(List<String> tokens) {
        Map<String, Integer> tf = new HashMap<>();
        for (String t : tokens) tf.merge(t, 1, Integer::sum);

        Map<String, Double> vec = new HashMap<>();
        tf.forEach((term, count) -> {
            Double weight = idf.get(term);
            if (weight != null) { // ignore words not in the FAQ vocabulary
                vec.put(term, count * weight);
            }
        });
        return vec;
    }

    private double cosine(Map<String, Double> a, Map<String, Double> b) {
        if (a.isEmpty() || b.isEmpty()) return 0;
        Map<String, Double> small = a.size() <= b.size() ? a : b;
        Map<String, Double> large = small == a ? b : a;

        double dot = 0;
        for (var e : small.entrySet()) {
            Double other = large.get(e.getKey());
            if (other != null) dot += e.getValue() * other;
        }
        double normA = Math.sqrt(a.values().stream().mapToDouble(v -> v * v).sum());
        double normB = Math.sqrt(b.values().stream().mapToDouble(v -> v * v).sum());
        return (normA == 0 || normB == 0) ? 0 : dot / (normA * normB);
    }

    public record FaqMatch(String question, String answer, double score) {}
}
package com.QueryMatch.query_match.controller;

import com.QueryMatch.query_match.service.FaqService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/faq")
public class FaqController {

    private final FaqService faqService;

    public FaqController(FaqService faqService) {
        this.faqService = faqService;
    }

    // Best single answer, or a fallback if nothing is close enough
    @PostMapping("/ask")
    public ResponseEntity<?> ask(@RequestBody AskRequest req) {
        return faqService.findAnswer(req.question())
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.ok(Map.of(
                        "answer", "Sorry, I couldn't find a matching FAQ. Please contact developer-support@example.com.",
                        "matched", false)));
    }

    // Top-N suggestions with scores, useful for tuning the threshold
    @PostMapping("/suggest")
    public List<FaqService.FaqMatch> suggest(@RequestBody AskRequest req,
                                             @RequestParam(defaultValue = "3") int topK) {
        return faqService.search(req.question(), topK);
    }

    public record AskRequest(String question) {}
}
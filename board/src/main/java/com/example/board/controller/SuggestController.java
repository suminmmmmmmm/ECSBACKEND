package com.example.board.controller;

import com.example.board.dto.SuggestionDto;
import com.example.board.service.SuggestService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SuggestController {

    private final SuggestService suggestService;

    @GetMapping("/suggest")
    public List<SuggestionDto> suggest(@RequestParam String q,
                                       @RequestParam(defaultValue = "10") int limit) {
        return suggestService.suggest(q, limit);
    }

    // 선택한 항목 학습(선택)
    @PostMapping("/suggest/learn")
    public void learn(@RequestParam String term) {
        suggestService.learn(term);
    }
}

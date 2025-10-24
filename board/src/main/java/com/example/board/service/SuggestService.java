package com.example.board.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.example.board.dto.SuggestionDto;

import jakarta.annotation.PostConstruct;

@Service
public class SuggestService {

    private final Trie trie = new Trie();

    @PostConstruct
   void init() {
    int[] cnt = {0};
    try (var in = new BufferedReader(new InputStreamReader(
            Objects.requireNonNull(
                    getClass().getResourceAsStream("/wordlist/wordlist_ko.txt"),
                    "wordlist_ko.txt not found under resources/wordlist/"
            ), StandardCharsets.UTF_8))) {

        in.lines()
          .map(String::strip)
          .filter(s -> !s.isEmpty() && !s.startsWith("#"))
          .forEach(term -> { trie.insert(term, 1); cnt[0]++; });

        System.out.println("✅ wordlist loaded: " + cnt[0] + " terms");

    } catch (Exception e) {
        System.err.println("⚠️ wordlist load failed -> fallback words");
        for (String t : List.of("다", "다람쥐", "다리", "다정", "다음", "다운로드")) {
            trie.insert(t, 1); cnt[0]++;
        }
        System.out.println("✅ fallback loaded: " + cnt[0] + " terms");
    }
}

    public List<SuggestionDto> suggest(String q, int limit) {
    if (q == null || q.isBlank()) return List.of();

    // ✅ 한글 입력 정규화 (조합형 → 완성형)
    q = Normalizer.normalize(q.trim(), Normalizer.Form.NFC);

    var entries = trie.startsWith(q, Math.max(limit, 1));
    List<SuggestionDto> out = new ArrayList<>(entries.size());
    for (Trie.Entry e : entries) out.add(new SuggestionDto(e.term(), e.score()));
    return out;
}


    // 사용자가 실제로 선택한 항목을 학습(점수↑) — 선택사항
    public void learn(String term) {
        if (term != null && !term.isBlank()) {
            trie.insert(term.trim(), 1);
        }
    }

    // -------- 간단 Trie 구현 --------
    static class Trie {
        static class Node {
            Map<Character, Node> child = new HashMap<>();
            boolean isWord; int freq; String word;
        }
        private final Node root = new Node();

       public void insert(String word, int addFreq) {
    // ✅ 저장도 NFC로 통일
    word = Normalizer.normalize(word, Normalizer.Form.NFC);

    Node cur = root;
    for (char ch : word.toCharArray()) {
        cur = cur.child.computeIfAbsent(ch, c -> new Node());
    }
    cur.isWord = true;
    cur.word = word;
    cur.freq += Math.max(addFreq, 1);
}


        public List<Entry> startsWith(String prefix, int limit) {
            Node cur = root;
            for (char ch : prefix.toCharArray()) {
                cur = cur.child.get(ch);
                if (cur == null) return List.of();
            }
            List<Entry> res = new ArrayList<>();
            dfs(cur, res);
            // 점수 내림차순 → 길이 오름차순 → 사전순
            res.sort(Comparator.comparingInt(Entry::score).reversed()
                    .thenComparingInt(e -> e.term.length())
                    .thenComparing(Entry::term));
            return res.size() > limit ? res.subList(0, limit) : res;
        }

        private void dfs(Node n, List<Entry> out) {
            if (n.isWord) out.add(new Entry(n.word, n.freq));
            for (Node nx : n.child.values()) dfs(nx, out);
        }

        record Entry(String term, int score) {}
    }
}

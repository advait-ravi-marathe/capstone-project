package capstone.coverstory.support;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Independent instructor oracle; intentionally does not call ClauseChunker. */
final class CAP120ReferenceChunker {
    private static final Pattern TOKEN = Pattern.compile("\\S+");
    record Span(int start, int end) { }
    record Config(int size, int overlap) { }
    record Window(int start, int end, int tokens) { }
    record Score(List<Window> windows, int contextTokens, int sourceTokens,
                 int recoveredClauses, int totalClauses, double recall) { }

    Score evaluate(String source, List<Span> clauses, Config config) {
        byte[] bytes = source.getBytes(StandardCharsets.UTF_8);
        validateSpans(bytes, clauses);
        if (config.size() <= 0 || config.overlap() < 0 || config.overlap() >= config.size())
            throw new IllegalArgumentException("invalid chunk configuration");
        List<Window> result = new ArrayList<>();
        Matcher matcher = TOKEN.matcher(source);
        List<Span> tokens = new ArrayList<>();
        while (matcher.find()) {
            int start = source.substring(0, matcher.start()).getBytes(StandardCharsets.UTF_8).length;
            int end = source.substring(0, matcher.end()).getBytes(StandardCharsets.UTF_8).length;
            tokens.add(new Span(start, end));
        }
        int stride = config.size() - config.overlap();
        for (int first = 0; first < tokens.size(); first += stride) {
            int after = Math.min(first + config.size(), tokens.size());
            result.add(new Window(tokens.get(first).start(), tokens.get(after - 1).end(), after - first));
            if (after == tokens.size()) break;
        }
        int recovered = 0;
        for (Span clause : clauses) {
            boolean covered = result.stream().anyMatch(w -> w.start() <= clause.start() && w.end() >= clause.end());
            if (covered) recovered++;
        }
        int cost = result.stream().mapToInt(Window::tokens).sum();
        return new Score(List.copyOf(result), cost, tokens.size(), recovered, clauses.size(),
                clauses.isEmpty() ? 1.0 : (double) recovered / clauses.size());
    }

    static void validateSpans(byte[] source, List<Span> spans) {
        int priorStart = -1, priorEnd = -1;
        for (Span span : spans) {
            if (span.start() < 0 || span.end() <= span.start() || span.end() > source.length)
                throw new IllegalArgumentException("malformed or out-of-source span");
            if (span.start() < priorStart || span.start() < priorEnd)
                throw new IllegalArgumentException("overlapping or unordered spans");
            // Both endpoints must be UTF-8 code point boundaries (not continuation bytes).
            if ((source[span.start()] & 0xC0) == 0x80 || (span.end() < source.length && (source[span.end()] & 0xC0) == 0x80))
                throw new IllegalArgumentException("span splits a UTF-8 code point");
            priorStart = span.start(); priorEnd = span.end();
        }
    }
}

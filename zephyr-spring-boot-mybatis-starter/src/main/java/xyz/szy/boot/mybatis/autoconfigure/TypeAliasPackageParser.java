package xyz.szy.boot.mybatis.autoconfigure;

import com.google.common.base.CharMatcher;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import java.io.Serializable;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TypeAliasPackageParser {
    private static final Pattern regex = Pattern.compile("\\((.*?)\\)");
    private static final Splitter or = Splitter.on('|').omitEmptyStrings().trimResults();
    private static final Splitter comma = Splitter.on(CharMatcher.is(',').or(CharMatcher.is(';').or(CharMatcher.is('\t').or(CharMatcher.is('\n'))))).omitEmptyStrings().trimResults();

    public static String flatPackageNames(String inputs) {
        return Joiner.on(',').join(resolveTypeAliasPackageNames(inputs));
    }

    static Set<String> resolveTypeAliasPackageNames(String inputs) {
        Set<String> result = new HashSet<>();
        for (String input : comma.split(inputs)) {
            result.addAll(resolvePackageNames(input));
        }
        return result;
    }

    static Set<String> resolvePackageNames(String input) {
        Set<String> packageNames = new HashSet<>();
        Matcher matcher = regex.matcher(input);
        ArrayList<MatchPosition> matchPositions = new ArrayList<>();

        while(matcher.find()) {
            matchPositions.add(new MatchPosition(matcher.start(), matcher.end(), matcher.group(1)));
        }

        if (matchPositions.isEmpty()) {
            packageNames.add(input);
        } else {
            resolveRegexPackageNames(input, matchPositions, packageNames);
        }

        return packageNames;
    }

    private static void resolveRegexPackageNames(String input, List<MatchPosition> matchPositions, Set<String> packageNames) {
        Multimap<Integer, String> current = HashMultimap.create();
        int currentIndex = 0;
        current.put(currentIndex, "");

        int end;
        for(Iterator<MatchPosition> iterator = matchPositions.iterator(); iterator.hasNext(); currentIndex = end) {
            MatchPosition matchPosition = iterator.next();
            int start = matchPosition.start();
            end = matchPosition.end();
            String betweenCurrentAndStart = input.substring(currentIndex, start);
            String text = matchPosition.text();
            for (String part : or.split(text)) {
                for (String c : current.get(currentIndex)) {
                    current.put(end, c + betweenCurrentAndStart + part);
                }
            }
        }
        int length = input.length();
        Iterator<String> iterator = current.get(currentIndex).iterator();
        String packageName;
        while(iterator.hasNext()) {
            packageName = iterator.next();
            String betweenLastMatchEndAndInputEnd = input.substring(currentIndex, length);
            current.put(length, packageName + betweenLastMatchEndAndInputEnd);
        }
        iterator = current.get(length).iterator();
        while(iterator.hasNext()) {
            packageName = iterator.next();
            packageNames.add(packageName);
        }

    }

    public static class MatchPosition implements Serializable {
        private static final long serialVersionUID = -8954011461860739607L;
        private final int start;
        private final int end;
        private final String text;

        public MatchPosition(int start, int end, String text) {
            this.start = start;
            this.end = end;
            this.text = text;
        }

        public int start() {
            return this.start;
        }

        public int end() {
            return this.end;
        }

        public String text() {
            return this.text;
        }
    }
}

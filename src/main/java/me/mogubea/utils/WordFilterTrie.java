package me.mogubea.utils;
import me.mogubea.main.Main;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class WordFilterTrie {
    private final TrieNode root = new TrieNode();
    private final List<String> replaceWords;
    private final Map<Character, List<Character>> leetspeakMap;
    private final Random random;

    public WordFilterTrie(Main plugin) {
        Set<String> words = Set.of("cock", "rape", "rapist", "nazi", "cum", "fag", "faggot", "fggt", "cunt", "kibidi", "skibidi", "rizz", "gyatt", "sigma", "nigga", "nigger", "niggr", "retard", "aggot", "etard", "vagina", "clitoris", "pedophile", "paedophile", "molest", "dickhead", "asshole");
        Set<String> allWords = new HashSet<>(words);

        replaceWords = List.of("nya", "mreow", "meow", "purr", "bark", "woof", "mlem");
        random = plugin.getRandom();

        this.leetspeakMap = Map.of(
                'a', List.of('a', '@', '4'),
                'e', List.of('e', '3'),
                'u', List.of('v'),
                'i', List.of('i', '1', '!', '|', '[', ']'),
                'o', List.of('o', '0', '@'),
                's', List.of('s', '$', '5'),
                't', List.of('t', '7'),
                'c', List.of('k'),
                'k', List.of('c'),
                'l', List.of('l', '1', '|')
        );

        for (String word : words)
            allWords.addAll(generateLeetspeakVariants(word.toLowerCase()));

        for (String badWord : allWords)
            addWordToTrie(badWord.toLowerCase());
    }

    private void addWordToTrie(String word) {
        TrieNode node = root;
        for (char c : word.toCharArray())
            node = node.children.computeIfAbsent(c, k -> new TrieNode());
        node.isEndOfWord = true;
    }

    // Filter the input string
    public String filter(String input) {
        StringBuilder result = new StringBuilder();
        int i = 0;

        while (i < input.length()) {
            TrieNode node = root;
            int start = i;
            boolean found = false;
            int lastMatchEnd = -1;

            // Traverse the Trie while allowing for optional non-alphanumeric characters
            for (int j = i; j < input.length(); j++) {
                char c = Character.toLowerCase(input.charAt(j));

                if (c != '$' && c != '@' && c != '|' && !Character.isLetterOrDigit(c)) {
                    continue; // Skip non-alphanumeric characters
                }

                node = node.children.get(c);
                if (node == null) {
                    break; // No match in the Trie
                }

                if (node.isEndOfWord) {
                    found = true;
                    lastMatchEnd = j; // Mark the end of the matched word
                }
            }

            if (found) {
                // Replace the matched word with a replacement and handle spacing
                String replacement = replaceWords.get(random.nextInt(replaceWords.size()));

                if (Character.isWhitespace(input.charAt(start)))
                    result.append(" ");

                result.append(replacement);

                i = lastMatchEnd + 1; // Move the pointer past the matched word
            } else {
                // No match: append the current character
                result.append(input.charAt(i));
                i++;
            }
        }

        return result.toString().trim(); // Trim any leading/trailing spaces
    }

    private @NotNull Set<String> generateLeetspeakVariants(String word) {
        Set<String> variants = new HashSet<>();
        generateVariantsRecursive(word.toCharArray(), 0, new StringBuilder(), variants);
        return variants;
    }

    private void generateVariantsRecursive(char @NotNull [] word, int index, StringBuilder current, Set<String> variants) {
        if (index == word.length) {
            variants.add(current.toString());
            return;
        }

        char c = word[index];
        List<Character> replacements = leetspeakMap.getOrDefault(c, List.of(c));
        for (char replacement : replacements) {
            current.append(replacement);
            generateVariantsRecursive(word, index + 1, current, variants);
            current.deleteCharAt(current.length() - 1); // Backtrack
        }
    }

    private static class TrieNode {
        Map<Character, TrieNode> children = new HashMap<>();
        boolean isEndOfWord = false;
    }
}
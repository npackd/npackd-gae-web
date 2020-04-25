package com.googlecode.npackdweb;

import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Pattern;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.StopwordAnalyzerBase;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.FlattenGraphFilter;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.miscellaneous.WordDelimiterGraphFilter;
import org.apache.lucene.analysis.pattern.PatternReplaceCharFilter;
import org.apache.lucene.analysis.synonym.SynonymGraphFilter;
import org.apache.lucene.analysis.synonym.SynonymMap;
import org.apache.lucene.util.CharsRef;

/**
 * Like EnglishAnalyzer, but also split words, e.g. "ImageGlass" to "Image" and
 * "Glass".
 */
public class MyEnglishAnalyzer extends StopwordAnalyzerBase {

    /**
     * An unmodifiable set containing some common English words that are not
     * usually useful for searching.
     */
    public static final CharArraySet ENGLISH_STOP_WORDS_SET;

    /**
     * An unmodifiable set containing protected words that are not modified.
     */
    public static final CharArraySet PROTECTED_WORDS_SET;

    private static SynonymMap synonyms;

    static {
        final List<String> stopWords = Arrays.asList(
                "a", "an", "and", "are", "as", "at", "be", "but", "by",
                "desktop",
                "for", "if", "in", "into", "is", "it",
                "no", "not", "of", "on", "or", "such",
                "that", "the", "their", "then", "there", "these",
                "they", "this", "to", "was", "will", "with",
                // applies to every package
                "windows", "window", "win", "x", "86", "software", "programs",
                "program", "exe", "executables", "executable",
                "setups", "msi",
                "alpha", "beta", "release", "prerelease",
                "setup", "packages", "package", "versions", "version", "bits",
                "bit"
        );
        final CharArraySet stopSet = new CharArraySet(stopWords, false);
        ENGLISH_STOP_WORDS_SET = CharArraySet.unmodifiableSet(stopSet);

        final List<String> protectedWords = Arrays.asList(
                "c++", "c#", "f#"
        );
        final CharArraySet protectedSet =
                new CharArraySet(protectedWords, true);
        PROTECTED_WORDS_SET = CharArraySet.unmodifiableSet(protectedSet);

        final SynonymMap.Builder b = new SynonymMap.Builder();
        try {
            b.add(new CharsRef("cpp"), new CharsRef("c++"), false);
            synonyms = b.build();
        } catch (IOException ex) {
            NWUtils.LOG.log(Level.SEVERE, null, ex);
        }
    }

    /**
     * -
     */
    public MyEnglishAnalyzer() {
        super(ENGLISH_STOP_WORDS_SET);
    }

    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        final Tokenizer source = new WhitespaceTokenizer();

        TokenStream result = new WordDelimiterGraphFilter(source,
                WordDelimiterGraphFilter.GENERATE_WORD_PARTS |
                WordDelimiterGraphFilter.GENERATE_NUMBER_PARTS |
                WordDelimiterGraphFilter.SPLIT_ON_CASE_CHANGE |
                WordDelimiterGraphFilter.SPLIT_ON_NUMERICS |
                WordDelimiterGraphFilter.STEM_ENGLISH_POSSESSIVE,
                PROTECTED_WORDS_SET);
        result = new FlattenGraphFilter(result); // required on index analyzers after graph filters

        result = new SynonymGraphFilter(result, synonyms, true);

        result = new LowerCaseFilter(result);

        result = new StopFilter(result, stopwords);
        result = new PorterStemFilter(result);
        return new TokenStreamComponents(source, result);
    }

    @Override
    protected Reader initReader(String fieldName, Reader reader) {
        // remove version numbers
        return new PatternReplaceCharFilter(Pattern.
                compile("\\d+([\\._\\-\\+]\\d+){1,5}"),
                "",
                reader);
    }

}

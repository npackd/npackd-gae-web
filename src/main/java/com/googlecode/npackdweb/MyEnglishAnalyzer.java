package com.googlecode.npackdweb;

import java.io.Reader;
import java.util.regex.Pattern;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.StopwordAnalyzerBase;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.FlattenGraphFilter;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.miscellaneous.WordDelimiterGraphFilter;
import org.apache.lucene.analysis.pattern.PatternReplaceCharFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;

/**
 * Like EnglishAnalyzer, but also split words, e.g. "ImageGlass" to "Image" and
 * "Glass".
 */
public class MyEnglishAnalyzer extends StopwordAnalyzerBase {

    /**
     * -
     */
    public MyEnglishAnalyzer() {
        super(EnglishAnalyzer.ENGLISH_STOP_WORDS_SET);
    }

    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        final Tokenizer source = new StandardTokenizer();
        TokenStream result = new WordDelimiterGraphFilter(source,
                WordDelimiterGraphFilter.GENERATE_WORD_PARTS |
                WordDelimiterGraphFilter.GENERATE_NUMBER_PARTS |
                WordDelimiterGraphFilter.SPLIT_ON_CASE_CHANGE |
                WordDelimiterGraphFilter.SPLIT_ON_NUMERICS |
                WordDelimiterGraphFilter.STEM_ENGLISH_POSSESSIVE,
                CharArraySet.EMPTY_SET);
        result = new FlattenGraphFilter(result); // required on index analyzers after graph filters

        result = new LowerCaseFilter(result);
        result = new StopFilter(result, stopwords);
        result = new PorterStemFilter(result);
        return new TokenStreamComponents(source, result);
    }

    @Override
    protected Reader initReader(String fieldName, Reader reader) {
        // remove version numbers
        return new PatternReplaceCharFilter(Pattern.
                compile("\\d+(\\.\\d+){1,5}"),
                "",
                reader);
    }

}

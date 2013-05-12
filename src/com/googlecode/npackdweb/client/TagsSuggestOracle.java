package com.googlecode.npackdweb.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gwt.user.client.ui.SuggestOracle;

/**
 * Completion for tags.
 */
public class TagsSuggestOracle extends SuggestOracle {
    /**
     * Suggestion.
     */
    public static class SimpleSuggestion implements Suggestion {
        private String displayString;
        private String replacementString;

        /**
         * Constructor for <code>SimpleSuggestion</code>.
         * 
         * @param replacementString
         *            the string to enter into the SuggestBox's text box if the
         *            suggestion is chosen
         * @param displayString
         *            the display string
         */
        public SimpleSuggestion(String replacementString, String displayString) {
            this.replacementString = replacementString;
            this.displayString = displayString;
        }

        public String getDisplayString() {
            return displayString;
        }

        public String getReplacementString() {
            return replacementString;
        }
    }

    private List<String> tags;

    /**
     * -
     * 
     * @param tags
     *            Tags
     */
    public TagsSuggestOracle(List<String> tags) {
        this.tags = tags;
    }

    /**
     * Sets new tags.
     * 
     * @param newTags
     *            neue Tags
     */
    public void setTags(List<String> newTags) {
        this.tags = newTags;
    }

    @Override
    public void requestSuggestions(Request req, Callback cb) {
        String q = req.getQuery();
        if (q == null)
            q = "";
        int index = q.lastIndexOf(',');
        String last;
        String start;
        Set<String> usedTags;
        if (index >= 0) {
            last = q.substring(index + 1);
            start = q.substring(0, index + 1);
            String[] usedTags_ = start.split(",");
            usedTags = new HashSet<String>();
            for (String t : usedTags_) {
                if (t.trim().length() != 0)
                    usedTags.add(t.trim());
            }
        } else {
            last = q;
            start = "";
            usedTags = Collections.emptySet();
        }

        last = last.trim().toLowerCase();

        Collection<SuggestOracle.Suggestion> sugg = new ArrayList<SuggestOracle.Suggestion>();
        for (String tag : tags) {
            String tl = tag.toLowerCase();
            boolean add = false;
            if (!usedTags.contains(tag))
                add = tl.startsWith(last);
            else
                add = tl.equals(last);

            if (add)
                sugg.add(new SimpleSuggestion(start + tag, start + tag));
        }

        SuggestOracle.Response resp = new SuggestOracle.Response(sugg);
        cb.onSuggestionsReady(req, resp);
    }

    @Override
    public void requestDefaultSuggestions(Request request, Callback callback) {
        this.requestSuggestions(request, callback);
    }
}

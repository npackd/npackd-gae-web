package com.googlecode.npackdweb.client;

import java.util.Arrays;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.user.client.ui.SuggestBox;

public class PackageEditorEntryPoint implements EntryPoint {

    @Override
    public void onModuleLoad() {
        Document d = Document.get();

        if (d.getElementById("package-form") == null)
            return;

        Element tags_ = d.getElementById("tags");
        if (tags_ != null) {
            TagsSuggestOracle oracle = new TagsSuggestOracle(
                    Arrays.asList(new String[] { "cat-books", "cat-business",
                            "cat-communications", "cat-dev", "cat-education",
                            "cat-finance", "cat-food", "cat-games",
                            "cat-health", "cat-lifestyle", "cat-music",
                            "cat-news", "cat-photo", "cat-productivity",
                            "cat-security", "cat-shopping", "cat-sports",
                            "cat-tools", "cat-travel", "cat-video",

                    }));

            final SuggestBox b = SuggestBox.wrap(oracle, tags_);
            b.addKeyDownHandler(new KeyDownHandler() {
                @Override
                public void onKeyDown(KeyDownEvent event) {
                    if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
                        event.preventDefault();
                        return;
                    }
                }
            });
            b.getValueBox().addFocusHandler(new FocusHandler() {
                @Override
                public void onFocus(FocusEvent event) {
                    b.showSuggestionList();
                }
            });
        }
    }

}

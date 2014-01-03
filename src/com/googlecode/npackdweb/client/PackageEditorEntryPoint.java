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
			TagsSuggestOracle oracle =
					new TagsSuggestOracle(Arrays.asList(new String[] {
							"Communications", "Development", "Education",
							"Finance", "Games", "Music", "News", "Photo",
							"Productivity", "Security", "Tools", "Video",

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

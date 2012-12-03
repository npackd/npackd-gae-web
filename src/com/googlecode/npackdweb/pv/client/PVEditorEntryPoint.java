package com.googlecode.npackdweb.pv.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.LIElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Editor for a package version.
 */
public class PVEditorEntryPoint implements EntryPoint {
	@Override
	public void onModuleLoad() {
		Document d = Document.get();

		Button addDep = Button.wrap(d.getElementById("addDep"));
		addDep.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				addDep();
			}
		});
		Button removeDep = Button.wrap(d.getElementById("removeDep"));
		removeDep.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				removeDep();
			}
		});
		RootPanel rp = RootPanel.get("fields");
	}

	private void removeDep() {
		Document d = Document.get();
		Element ul = d.getElementById("deps");
		int n = ul.getChildCount();
		if (n > 0)
			ul.removeChild(ul.getChild(n - 1));
	}

	private void addDep() {
		Document d = Document.get();
		Element ul = d.getElementById("deps");
		int n = ul.getChildCount();

		LIElement li = d.createLIElement();
		InputElement p = d.createTextInputElement();
		p.setName("depPackage." + n);
		p.setSize(80);
		li.appendChild(p);
		InputElement v = d.createTextInputElement();
		v.setName("depVersions." + n);
		v.setSize(20);
		li.appendChild(v);
		ul.appendChild(li);
	}
}

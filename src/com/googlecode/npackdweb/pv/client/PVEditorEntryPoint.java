package com.googlecode.npackdweb.pv.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.TableCellElement;
import com.google.gwt.dom.client.TableRowElement;
import com.google.gwt.dom.client.TextAreaElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.DoubleClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;

/**
 * Editor for a package version.
 */
public class PVEditorEntryPoint implements EntryPoint {
	@Override
	public void onModuleLoad() {
		Document d = Document.get();

		Element url_ = d.getElementById("url");
		final TextBox url = TextBox.wrap(url_);
		url.setTitle("http:// or https:// address to download the "
		        + "package binary file. Double click to open in a new window.");
		url.addDoubleClickHandler(new DoubleClickHandler() {
			@Override
			public void onDoubleClick(DoubleClickEvent event) {
				String txt = url.getText();
				if (!txt.trim().isEmpty())
					Window.open(txt, null, null);
			}
		});

		Element addDep_ = d.getElementById("addDep");
		if (addDep_ != null) {
			Button addDep = Button.wrap(addDep_);
			addDep.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					addDep();
				}
			});
		}
		Element removeDep_ = d.getElementById("removeDep");
		if (removeDep_ != null) {
			Button removeDep = Button.wrap(removeDep_);
			removeDep.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					removeDep();
				}
			});
		}

		Element addFile_ = d.getElementById("addFile");
		if (addFile_ != null) {
			Button addFile = Button.wrap(addFile_);
			addFile.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					addFile();
				}
			});
		}
		Element removeFile_ = d.getElementById("removeFile");
		if (removeFile_ != null) {
			Button removeFile = Button.wrap(removeFile_);
			removeFile.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					removeFile();
				}
			});
		}
		Element addNSISFiles_ = d.getElementById("addNSISFiles");
		if (addNSISFiles_ != null) {
			Button addNSISFiles = Button.wrap(addNSISFiles_);
			addNSISFiles.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					addNSISFiles();
				}
			});
		}
		Element addInnoSetupFiles_ = d.getElementById("addInnoSetupFiles");
		if (addInnoSetupFiles_ != null) {
			Button addInnoSetupFiles = Button.wrap(addInnoSetupFiles_);
			addInnoSetupFiles.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					addInnoSetupFiles();
				}
			});
		}
		Element addMSIFiles_ = d.getElementById("addMSIFiles");
		if (addMSIFiles_ != null) {
			Button addMSIFiles = Button.wrap(addMSIFiles_);
			addMSIFiles.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					addMSIFiles();
				}
			});
		}

		RootPanel.get("fields");
	}

	private void addInnoSetupFiles() {
		addFile(
		        ".Npackd\\Install.bat",
		        "for /f \"delims=\" %%x in ('dir /b *.exe') do set setup=%%x\r\n"
		                + "\"%setup%\" /SP- /VERYSILENT /SUPPRESSMSGBOXES /NOCANCEL /NORESTART /DIR=\"%CD%\" /SAVEINF=\"%CD%\\.Npackd\\InnoSetupInfo.ini\" /LOG=\"%CD%\\.Npackd\\InnoSetupInstall.log\"\r\n"
		                + "set err=%errorlevel%\r\n"
		                + "type .Npackd\\InnoSetupInstall.log\r\n"
		                + "if %err% neq 0 exit %err%\r\n");
		addFile(
		        ".Npackd\\Uninstall.bat",
		        "unins000.exe /VERYSILENT /SUPPRESSMSGBOXES /NORESTART /LOG=\"%CD%\\.Npackd\\InnoSetupUninstall.log\"\r\n"
		                + "set err=%errorlevel%\r\n"
		                + "type .Npackd\\InnoSetupUninstall.log\r\n"
		                + "if %err% neq 0 exit %err%\r\n");
	}

	private void addMSIFiles() {
		addFile(
		        ".Npackd\\Install.bat",
		        "if \"%npackd_cl%\" equ \"\" set npackd_cl=..\\com.googlecode.windows-package-manager.NpackdCL-1\r\n"
		                + "set onecmd=\"%npackd_cl%\\npackdcl.exe\" \"path\" \"--package=com.googlecode.windows-package-manager.NpackdInstallerHelper\" \"--versions=[1.1, 2)\"\r\n"
		                + "for /f \"usebackq delims=\" %%x in (`%%onecmd%%`) do set npackdih=%%x\r\n"
		                + "call \"%npackdih%\\InstallMSI.bat\" INSTALLDIR yes\r\n");
	}

	private void addNSISFiles() {
		addFile(".Npackd\\Install.bat",
		        "for /f \"delims=\" %%x in ('dir /b *.exe') do set setup=%%x\r\n"
		                + "\"%setup%\" /S /D=%CD%\r\n");
		addFile(".Npackd\\Uninstall.bat", "uninst.exe /S _?=%CD%\r\n");
	}

	private void removeFile() {
		Document d = Document.get();
		Element div = d.getElementById("files");
		int n = div.getChildCount();
		if (n > 0)
			div.removeChild(div.getChild(n - 1));
	}

	private void addFile() {
		addFile("", "");
	}

	private void addFile(String path, String content) {
		Document d = Document.get();
		Element div = d.getElementById("files");
		int n = div.getChildCount();

		DivElement newEntry = d.createDivElement();

		DivElement p = d.createDivElement();
		p.setInnerText("File path " + n + ":");
		newEntry.appendChild(p);

		InputElement v = d.createTextInputElement();
		v.setName("path." + n);
		v.setSize(80);
		v.setValue(path);
		newEntry.appendChild(v);

		DivElement p2 = d.createDivElement();
		p2.setInnerText("File content " + n + ":");
		newEntry.appendChild(p2);

		TextAreaElement ta = d.createTextAreaElement();
		ta.setName("content." + n);
		ta.setCols(80);
		ta.setRows(20);
		ta.setAttribute("wrap", "off");
		ta.setInnerText(content);
		newEntry.appendChild(ta);

		div.appendChild(newEntry);
	}

	private void removeDep() {
		Document d = Document.get();
		Element table = d.getElementById("deps");
		Element tbody = table.getFirstChildElement();
		int n = tbody.getChildCount();
		if (n > 0)
			tbody.removeChild(tbody.getChild(n - 1));
	}

	private void addDep() {
		Document d = Document.get();
		Element table = d.getElementById("deps");
		Element tbody = table.getFirstChildElement();
		int n = tbody.getChildCount();

		TableRowElement tr = d.createTRElement();

		TableCellElement td = d.createTDElement();
		InputElement p = d.createTextInputElement();
		p.setName("depPackage." + n);
		p.setSize(80);
		td.appendChild(p);
		tr.appendChild(td);

		td = d.createTDElement();
		InputElement v = d.createTextInputElement();
		v.setName("depVersions." + n);
		v.setSize(20);
		td.appendChild(v);
		tr.appendChild(td);

		td = d.createTDElement();
		InputElement ev = d.createTextInputElement();
		ev.setName("depEnvVar." + n);
		ev.setSize(20);
		td.appendChild(ev);
		tr.appendChild(td);

		tbody.appendChild(tr);
	}
}

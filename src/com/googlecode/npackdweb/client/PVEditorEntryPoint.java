package com.googlecode.npackdweb.client;

import java.util.Arrays;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.TableCellElement;
import com.google.gwt.dom.client.TableRowElement;
import com.google.gwt.dom.client.TextAreaElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Event.NativePreviewHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Editor for a package version.
 */
public class PVEditorEntryPoint implements EntryPoint {
    private static final int VK_H = 72;
    private static final int VK_G = 71;
    private static final int VK_D = 68;
    private static final int VK_S = 83;
    private static final int VK_C = 67;

    private RootPanel rp;

    @Override
    public void onModuleLoad() {
        Document d = Document.get();

        Element url_ = d.getElementById("url");
        if (url_ != null) {
            final TextBox url = TextBox.wrap(url_);

            ImageElement im_ = d.createImageElement();
            im_.setSrc("/Link.png");
            im_.setTitle("Stars the file download");
            url_.getParentElement().insertAfter(im_, url_);
            Image im = Image.wrap(im_);
            im.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    String txt = url.getText();
                    if (!txt.trim().isEmpty())
                        Window.open(txt, null, null);
                }
            });
        }

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
        Element tags_ = d.getElementById("tags");
        if (tags_ != null) {
            TagsSuggestOracle oracle = new TagsSuggestOracle(
                    Arrays.asList(new String[] { "stable", "stable64", "libs",
                            "unstable" }));

            // MultipleTextBox textBox = new MultipleTextBox(tags_);
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

        rp = RootPanel.get();
        rp.sinkEvents(Event.KEYEVENTS);
        rp.addDomHandler(new KeyPressHandler() {
            @Override
            public void onKeyPress(KeyPressEvent event) {

            }
        }, KeyPressEvent.getType());

        Event.addNativePreviewHandler(new NativePreviewHandler() {
            @Override
            public void onPreviewNativeEvent(NativePreviewEvent event) {
                NativeEvent ne = event.getNativeEvent();
                EventTarget et = ne.getEventTarget();
                /*
                Logger logger = Logger.getLogger("MyLogger");
                logger.log(Level.SEVERE, "Message: " + ne.getAltKey()
                        + ne.getCtrlKey() + !ne.getMetaKey() + Element.is(et)
                        + Element.as(et).getNodeName().toLowerCase() + " "
                        + ne.getKeyCode());*/
                if (ne.getAltKey()
                        && ne.getCtrlKey()
                        && !ne.getMetaKey()
                        && Element.is(et)
                        && Element.as(et).getNodeName().toLowerCase()
                                .equals("body")) {

                    final int c = ne.getKeyCode();
                    if (c == VK_H || c == VK_G || c == VK_D || c == VK_S
                            || c == VK_C) {
                        ne.preventDefault();
                        Scheduler.get().scheduleDeferred(new Command() {
                            @Override
                            public void execute() {
                                processShortcut(c);
                            }
                        });
                    }
                }
            }
        });
    }

    private void processShortcut(int c) {
        switch (c) {
        case VK_H:
            showKeyboardShortcuts();
            break;
        case VK_G:
            goto_();
            break;
        case VK_D:
            download();
            break;
        case VK_S:
            save();
            break;
        case VK_C:
            copy();
            break;
        }
    }

    private void goto_() {
        Document d = Document.get();

        Element url_ = d.getElementById("packageURL");
        if (url_ != null) {
            String href = url_.getAttribute("href");
            if (!href.trim().isEmpty())
                Window.open(href, null, null);
        }
    }

    private void download() {
        Document d = Document.get();

        Element url_ = d.getElementById("url");
        if (url_ != null) {
            String href = url_.getAttribute("value");
            if (!href.trim().isEmpty())
                Window.open(href, null, null);
        }
    }

    private void save() {
        Document d = Document.get();

        Element b_ = d.getElementById("save");
        if (b_ != null) {
            InputElement.as(b_).click();
        }
    }

    private void copy() {
        Document d = Document.get();

        Element b_ = d.getElementById("copy");
        if (b_ != null) {
            InputElement.as(b_).click();
        }
    }

    private void showKeyboardShortcuts() {
        PopupPanel p = new PopupPanel(true, false) {
            @Override
            protected void onPreviewNativeEvent(NativePreviewEvent event) {
                if (!event.isCanceled() && !event.isConsumed()) {
                    NativeEvent ne = event.getNativeEvent();
                    if (ne.getKeyCode() == KeyCodes.KEY_ESCAPE) {
                        ne.preventDefault();
                        hide();
                    }
                }
                super.onPreviewNativeEvent(event);
            }
        };
        VerticalPanel vp = new VerticalPanel();
        HTML h = new HTML("<h3>Keyboard shortcuts</h3>");
        vp.add(h);
        Label label = new Label("Ctrl-Alt-H show this list");
        vp.add(label);
        label = new Label("Ctrl-Alg-G open home page");
        vp.add(label);
        label = new Label("Ctrl-Alt-D download the binary");
        vp.add(label);
        label = new Label("Ctrl-Alt-S saves this package version");
        vp.add(label);
        label = new Label("Ctrl-Alt-C creates a copy of this package version");
        vp.add(label);

        p.setWidget(vp);
        p.setPopupPosition(100, 100);
        p.center();
        p.show();
    }

    private void addInnoSetupFiles() {
        addFile(".Npackd\\Install.bat",
                "for /f \"delims=\" %%x in ('dir /b *.exe') do set setup=%%x\r\n"
                        + "\"%setup%\" /SP- /VERYSILENT /SUPPRESSMSGBOXES /NOCANCEL /NORESTART /DIR=\"%CD%\" /SAVEINF=\"%CD%\\.Npackd\\InnoSetupInfo.ini\" /LOG=\"%CD%\\.Npackd\\InnoSetupInstall.log\"\r\n"
                        + "set err=%errorlevel%\r\n"
                        + "type .Npackd\\InnoSetupInstall.log\r\n"
                        + "if %err% neq 0 exit %err%\r\n");
        addFile(".Npackd\\Uninstall.bat",
                "unins000.exe /VERYSILENT /SUPPRESSMSGBOXES /NORESTART /LOG=\"%CD%\\.Npackd\\InnoSetupUninstall.log\"\r\n"
                        + "set err=%errorlevel%\r\n"
                        + "type .Npackd\\InnoSetupUninstall.log\r\n"
                        + "if %err% neq 0 exit %err%\r\n");
    }

    private void addMSIFiles() {
        addFile(".Npackd\\Install.bat",
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
        if (n > 1)
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
        p.setName("depPackage." + (n - 1));
        p.setSize(80);
        td.appendChild(p);
        tr.appendChild(td);

        td = d.createTDElement();
        InputElement v = d.createTextInputElement();
        v.setName("depVersions." + (n - 1));
        v.setSize(20);
        td.appendChild(v);
        tr.appendChild(td);

        td = d.createTDElement();
        InputElement ev = d.createTextInputElement();
        ev.setName("depEnvVar." + (n - 1));
        ev.setSize(20);
        td.appendChild(ev);
        tr.appendChild(td);

        tbody.appendChild(tr);
    }
}

package com.googlecode.npackdweb;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.googlecode.npackdweb.wlib.HTMLWriter;

/**
 * Shows a message
 */
public class MessagePage extends MyPage {
    private List<String> msg = new ArrayList<>();

    /**
     * @param msg
     *            message to be shown
     */
    public MessagePage(String msg) {
        this.msg.add(msg);
    }

    public MessagePage(List<String> messages) {
        this.msg.addAll(messages);
    }

    @Override
    public String createContent(HttpServletRequest request) throws IOException {
        HTMLWriter w = new HTMLWriter();
        for (String s : msg) {
            w.t(s);
            w.e("br");
        }
        return w.toString();
    }

    @Override
    public String getTitle() {
        return "Message";
    }
}

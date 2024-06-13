package com.googlecode.npackdweb.wlib;

/**
 * Fluent interface for HTML creation.
 */
public class HTMLWriter {
    private static final char[] EOL = {'\r', '\n'};

    /**
     * 0 - es wurde noch nichts ausgegeben 1 - in <root> 2 - in <root><node>
     * ...
     */
    private int level;

    /**
     * [readwrite] true = formatierte Ausgabe
     */
    private boolean pretty;

    private boolean text_since_start_tag;

    private final StringBuilder b;

    /**
     * -
     */
    public HTMLWriter() {
        this.b = new StringBuilder();
    }

    /**
     * Schreibt &lt;?xml version=&quot;1.0&quot;?&gt;
     *
     * @return HTML
     */
    public StringBuilder documentStart() {
        b.append("<?xml version=\"1.0\"?>");
        if (this.isPretty()) {
            b.append(EOL);
        }
        return b;
    }

    private void indent() {
        if (this.level > 0) {
            b.append(EOL);
            b.repeat(" ", this.level);
        }
    }

    /**
     * @return HTML
     */
    public StringBuilder getContent() {
        return b;
    }

    /**
     * Creates a tag and closes it.
     *
     * @param tag name of the tag. Example: "textarea"
     * @return this
     */
    public HTMLWriter e(final String tag) {
        if (this.isPretty() && this.level > 0) {
            indent();
        }

        b.append('<').append(tag).append("/>");
        return this;
    }

    /**
     * Creates a tag.
     *
     * @param tag name of the tag. Example: "textarea"
     * @param attrsAndContent names and values for the attributes. The text
     * content of the tag may be the last element. An attribute is not created
     * if either the name or the value is null. The null content is handled as
     * empty.
     * @return this
     */
    public HTMLWriter e(final String tag, String... attrsAndContent) {
        assert attrsAndContent.length % 2 == 0;

        if (this.isPretty() && this.level > 0) {
            indent();
        }

        b.append('<').append(tag);
        for (int i = 0; i + 1 < attrsAndContent.length; i += 2) {
            String name = attrsAndContent[i];
            String value = attrsAndContent[i + 1];
            if (name != null && value != null) {
                b.append(' ');
                b.append(name);
                b.append("=\"");
                encodeHTML(b, value);
                b.append('"');
            }
        }
        b.append('>');
        if (attrsAndContent.length % 2 != 0) {
            encodeHTML(b, attrsAndContent[attrsAndContent.length - 1]);
        }
        b.append("</");
        b.append(tag);
        b.append('>');

        return this;
    }

    /**
     * Writes an end tag.
     *
     * @param tag name of the tag
     * @return this
     */
    public HTMLWriter end(String tag) {
        if (this.isPretty()) {
            b.append(EOL);
            b.repeat(" ", (this.level - 1));
        }

        b.append("</").append(tag).append('>');

        level--;
        
        return this;
    }

    /**
     * XML comment
     *
     * @param txt content
     * @return this
     */
    public HTMLWriter comment(final String txt) {
        b.append("<!--").append(txt).append("-->");
        return this;
    }

    /**
     * Starts a tag.
     *
     * @param tag name of the tag. Example: "textarea"
     * @param attrs names and values for the attributes. An attribute is not
     * created if either the name or the value is null.
     * @return this
     */
    public HTMLWriter start(final String tag, String... attrs) {
        if (this.isPretty() && this.level > 0) {
            indent();
        }

        b.append('<').append(tag);
        for (int i = 0; i < attrs.length; i += 2) {
            String name = attrs[i];
            String value = attrs[i + 1];
            if (name != null && value != null) {
                b.append(' ');
                b.append(name);
                b.append("=\"");
                encodeHTML(b, value);
                b.append('"');
            }
        }
        b.append('>');

        level++;

        return this;
    }

    /**
     * Starts a tag.
     *
     * @param tag name of the tag. Example: "textarea"
     * @return this
     */
    public HTMLWriter start(final String tag) {
        if (this.isPretty() && this.level > 0) {
            indent();
        }

        b.append('<').append(tag).append('>');

        level++;

        return this;
    }

    /**
     * Encodes as HTML.
     *
     * @param sb output
     * @param v text or null
     */
    private static void encodeHTML(StringBuilder sb, String v) {
        if (v != null) {
            for (int i = 0; i < v.length(); i++) {
                char c = v.charAt(i);
                switch (c) {
                    case '"':
                        sb.append("&quot;");
                        break;
                    case '>':
                        sb.append("&gt;");
                        break;
                    case '<':
                        sb.append("&lt;");
                        break;
                    case '&':
                        sb.append("&amp;");
                        break;
                    default:
                        sb.append(c);
                }
            }
        }
    }

    @Override
    public String toString() {
        return this.b.toString();
    }

    /**
     * Writes text.
     *
     * @param txt the text. null will be treated as an empty string.
     * @return this
     */
    public HTMLWriter t(String txt) {
        if (txt != null) {
            encodeHTML(b, txt);
        }
        return this;
    }

    /**
     * Adds HTML
     *
     * @param html HTML
     */
    public void unencoded(String html) {
        this.b.append(html);
    }

    /**
     * @return true = formatierte Ausgabe
     */
    public boolean isPretty() {
        return pretty;
    }

    /**
     * @param pretty true = formatierte Ausgabe
     */
    public void setPretty(boolean pretty) {
        this.pretty = pretty;
    }
}

package com.googlecode.npackdweb.wlib;

import java.util.List;

import com.googlecode.npackdweb.NWUtils;

/**
 * Fluent interface for HTML creation.
 */
public class HTMLWriter {
	private StringBuilder b;

	/**
	 * -
	 */
	public HTMLWriter() {
		this.b = new StringBuilder();
	}

	/**
	 * @return HTML
	 */
	public StringBuilder getContent() {
		return b;
	}

	/**
	 * Creates a tag and goes into it.
	 * 
	 * @param tag
	 *            name of the tag. Example: "textarea"
	 * @return this
	 */
	public HTMLWriter e(final String tag) {
		b.append('<').append(tag).append("></").append(tag).append('>');
		return this;
	}

	/**
	 * Creates a tag.
	 * 
	 * @param tag
	 *            name of the tag. Example: "textarea"
	 * @param attrsAndContent
	 *            names and values for the attributes. The text content of the
	 *            tag may be the last element. An attribute is not created if
	 *            either the name or the value is null.
	 * @return this
	 */
	public HTMLWriter e(final String tag, String... attrsAndContent) {
		assert attrsAndContent.length % 2 == 0;
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
		if (attrsAndContent.length % 2 != 0)
			encodeHTML(b, attrsAndContent[attrsAndContent.length - 1]);
		b.append("</");
		b.append(tag);
		b.append('>');

		return this;
	}

	/**
	 * Creates a tag.
	 * 
	 * @param tagAndAttrs
	 *            name of the tag and attributes separated by spaces. Example:
	 *            "textarea wrap=off"
	 * @param content
	 *            text content for the tag. null is treated as an empty string
	 * @return this
	 */
	public HTMLWriter ew(final String tagAndAttrs, String content) {
		List<String> p = NWUtils.split(tagAndAttrs, ' ');

		String tag = p.get(0);
		b.append('<').append(tag);
		for (int i = 1; i < p.size(); i++) {
			String nameAndValue = p.get(i);
			int pos = nameAndValue.indexOf('=');

			b.append(' ');
			b.append(nameAndValue, 0, pos);
			b.append("=\"");
			encodeHTML(b, nameAndValue.substring(pos + 1));
			b.append('"');
		}
		b.append('>');
		if (content != null)
			encodeHTML(b, content);
		b.append("</");
		b.append(tag);
		b.append('>');

		return this;
	}

	/**
	 * Writes an end tag.
	 * 
	 * @param tag
	 *            name of the tag
	 * @return this
	 */
	public HTMLWriter end(String tag) {
		b.append("</").append(tag).append('>');
		return this;
	}

	/**
	 * Starts a tag.
	 * 
	 * @param tag
	 *            name of the tag. Example: "textarea"
	 * @param attrsAndContent
	 *            names and values for the attributes. The text content of the
	 *            tag may be the last element. An attribute is not created if
	 *            either the name or the value is null.
	 * @return this
	 */
	public HTMLWriter start(final String tag, String... attrsAndContent) {
		b.append('<').append(tag);
		for (int i = 0; i < attrsAndContent.length; i += 2) {
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

		return this;
	}

	/**
	 * Starts a tag.
	 * 
	 * @param tag
	 *            name of the tag. Example: "textarea"
	 * @return this
	 */
	public HTMLWriter start(final String tag) {
		b.append('<').append(tag).append('>');

		return this;
	}

	private static void encodeHTML(StringBuilder sb, String v) {
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

	@Override
	public String toString() {
		return this.b.toString();
	}

	/**
	 * Writes text.
	 * 
	 * @param txt
	 *            the text
	 * @return this
	 */
	public HTMLWriter t(String txt) {
		encodeHTML(b, txt);
		return this;
	}

	/**
	 * Adds HTML
	 * 
	 * @param html
	 *            HTML
	 */
	public void unencoded(String html) {
		this.b.append(html);
	}
}

package org.zhinanzhen.b.utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFHeaderFooter;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/** 填充 Word 内容控件及跨 run 的占位符，不重建段落、图片或页眉页脚。 */
public final class PortalLetterTemplateRenderer {

	private static final String WORD_NS = "http://schemas.openxmlformats.org/wordprocessingml/2006/main";
	private static final String XML_NS = "http://www.w3.org/XML/1998/namespace";
	private static final Pattern SUBCLASS = Pattern.compile("(?<!\\d)\\d{3}(?!\\d)");
	public static final String UNCONFIRMED_OPTION = "To be confirmed by the registered migration agent";

	private PortalLetterTemplateRenderer() {
	}

	public static void fill(XWPFDocument document, Map<String, String> fields, List<String> optionCandidates,
			Date generatedAt) {
		Map<String, String> values = new LinkedHashMap<String, String>(fields);
		List<Node> roots = new ArrayList<Node>();
		roots.add(document.getDocument().getDomNode());
		for (XWPFHeaderFooter header : document.getHeaderList())
			roots.add(header._getHdrFtr().getDomNode());
		for (XWPFHeaderFooter footer : document.getFooterList())
			roots.add(footer._getHdrFtr().getDomNode());
		for (Node root : roots) {
			for (Node control : descendants(root, "sdt")) {
				Node properties = child(control, "sdtPr");
				if ("SELECTED_OPTION".equals(attribute(child(properties, "tag"), "val"))) {
					values.put("SELECTED_OPTION", selectOption(properties, optionCandidates));
				}
			}
		}
		if (!values.containsKey("SELECTED_OPTION"))
			values.put("SELECTED_OPTION", UNCONFIRMED_OPTION);
		for (Node root : roots) {
			for (Node control : descendants(root, "sdt")) {
				Node properties = child(control, "sdtPr");
				String tag = attribute(child(properties, "tag"), "val");
				if (!values.containsKey(tag))
					continue;
				String value = values.get(tag) == null ? "Not recorded" : values.get(tag);
				setControlText(child(control, "sdtContent"), value);
				removeChild(properties, "showingPlcHdr");
				// 已填入实际数据的生成件不再从模板的自定义 XML 恢复占位内容。
				removeChild(properties, "dataBinding");
				if ("DATE".equals(tag) && generatedAt != null) {
					Node date = child(properties, "date");
					if (date instanceof Element)
						((Element) date).setAttributeNS(WORD_NS, "w:fullDate",
								new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(generatedAt)
										+ "T00:00:00Z");
				}
				if ("SELECTED_OPTION".equals(tag))
					setDropdownValue(properties, value);
			}
			for (Node paragraph : descendants(root, "p")) {
				for (Map.Entry<String, String> entry : values.entrySet())
					replaceAcrossTextNodes(paragraph, "{{" + entry.getKey() + "}}",
							entry.getValue() == null ? "Not recorded" : entry.getValue());
			}
		}
	}

	public static boolean hasContentControls(XWPFTableCell cell) {
		return !descendants(cell.getCTTc().getDomNode(), "sdt").isEmpty();
	}

	private static String selectOption(Node properties, List<String> candidates) {
		List<String> options = new ArrayList<String>();
		for (Node item : descendants(properties, "listItem")) {
			String label = attribute(item, "displayText");
			if (label.isEmpty())
				label = attribute(item, "value");
			if (!label.isEmpty() && !label.toLowerCase(Locale.ENGLISH).startsWith("select "))
				options.add(label);
		}
		if (candidates != null) {
			for (String candidate : candidates) {
				if (candidate == null || candidate.trim().isEmpty())
					continue;
				String normalized = normalize(candidate);
				for (String option : options) {
					if (normalized.equals(normalize(option)))
						return option;
				}
				Set<String> textMatches = new LinkedHashSet<String>();
				for (String option : options) {
					if (normalized.contains(normalize(option)))
						textMatches.add(option);
				}
				Set<String> specificMatches = new LinkedHashSet<String>(textMatches);
				for (String option : textMatches) {
					for (String other : textMatches) {
						if (!option.equals(other) && normalize(other).contains(normalize(option)))
							specificMatches.remove(option);
					}
				}
				if (specificMatches.size() == 1)
					return specificMatches.iterator().next();
				if (specificMatches.size() > 1)
					continue;
				Set<String> subclasses = subclasses(candidate);
				Set<String> matched = new LinkedHashSet<String>();
				for (String option : options) {
					Set<String> optionSubclasses = subclasses(option);
					optionSubclasses.retainAll(subclasses);
					if (!optionSubclasses.isEmpty())
						matched.add(option);
				}
				if (matched.size() == 1)
					return matched.iterator().next();
			}
		}
		// 189、485 等有多个分支，资料不足时不能默认选择第一个选项。
		return UNCONFIRMED_OPTION;
	}

	private static Set<String> subclasses(String value) {
		Set<String> values = new LinkedHashSet<String>();
		Matcher matcher = SUBCLASS.matcher(value);
		while (matcher.find())
			values.add(matcher.group());
		return values;
	}

	private static String normalize(String value) {
		return value.toLowerCase(Locale.ENGLISH).replaceAll("[^\\p{L}\\p{N}]", "");
	}

	private static void setDropdownValue(Node properties, String value) {
		Node dropdown = child(properties, "dropDownList");
		if (dropdown == null)
			return;
		for (Node item : descendants(dropdown, "listItem")) {
			if (value.equals(attribute(item, "displayText"))) {
				((Element) dropdown).setAttributeNS(WORD_NS, "w:lastValue", attribute(item, "value"));
				return;
			}
		}
		Element item = dropdown.getOwnerDocument().createElementNS(WORD_NS, "w:listItem");
		item.setAttributeNS(WORD_NS, "w:displayText", value);
		item.setAttributeNS(WORD_NS, "w:value", value);
		dropdown.appendChild(item);
		((Element) dropdown).setAttributeNS(WORD_NS, "w:lastValue", value);
	}

	private static void setControlText(Node content, String value) {
		if (content == null)
			return;
		List<Node> texts = descendants(content, "t");
		if (texts.isEmpty()) {
			Node parent = content;
			List<Node> paragraphs = descendants(content, "p");
			if (!paragraphs.isEmpty()) {
				parent = paragraphs.get(0);
			} else if (!isWord(content.getParentNode().getParentNode(), "p")) {
				parent = content.getOwnerDocument().createElementNS(WORD_NS, "w:p");
				content.appendChild(parent);
			}
			Element run = content.getOwnerDocument().createElementNS(WORD_NS, "w:r");
			Element text = content.getOwnerDocument().createElementNS(WORD_NS, "w:t");
			parent.appendChild(run);
			run.appendChild(text);
			texts.add(text);
		}
		writeText(texts.get(0), value);
		for (int i = 1; i < texts.size(); i++)
			writeText(texts.get(i), "");
	}

	private static void replaceAcrossTextNodes(Node paragraph, String token, String replacement) {
		List<Node> texts = descendants(paragraph, "t");
		StringBuilder combined = new StringBuilder();
		List<Integer> starts = new ArrayList<Integer>();
		for (Node text : texts) {
			starts.add(combined.length());
			combined.append(text(text));
		}
		String original = combined.toString();
		int position = original.lastIndexOf(token);
		while (position >= 0) {
			int end = position + token.length();
			for (int i = 0; i < texts.size(); i++) {
				int start = starts.get(i);
				int nodeEnd = i + 1 < starts.size() ? starts.get(i + 1) : original.length();
				if (nodeEnd <= position || start >= end)
					continue;
				String current = text(texts.get(i));
				String prefix = start <= position ? current.substring(0, position - start) : "";
				String suffix = nodeEnd >= end ? current.substring(end - start) : "";
				writeText(texts.get(i), prefix + (start <= position ? replacement : "") + suffix);
			}
			position = position == 0 ? -1 : original.lastIndexOf(token, position - 1);
		}
	}

	private static String text(Node node) {
		StringBuilder value = new StringBuilder();
		for (Node part = node.getFirstChild(); part != null; part = part.getNextSibling()) {
			if (part.getNodeType() == Node.TEXT_NODE)
				value.append(part.getNodeValue());
		}
		return value.toString();
	}

	private static void writeText(Node node, String value) {
		while (node.getFirstChild() != null)
			node.removeChild(node.getFirstChild());
		node.appendChild(node.getOwnerDocument().createTextNode(value));
		((Element) node).setAttributeNS(XML_NS, "xml:space", "preserve");
	}

	private static String attribute(Node node, String name) {
		return node instanceof Element ? ((Element) node).getAttributeNS(WORD_NS, name) : "";
	}

	private static Node child(Node parent, String name) {
		if (parent == null)
			return null;
		for (Node node = parent.getFirstChild(); node != null; node = node.getNextSibling()) {
			if (isWord(node, name))
				return node;
		}
		return null;
	}

	private static void removeChild(Node parent, String name) {
		Node node = child(parent, name);
		if (node != null)
			parent.removeChild(node);
	}

	private static boolean isWord(Node node, String name) {
		return node != null && WORD_NS.equals(node.getNamespaceURI()) && name.equals(node.getLocalName());
	}

	private static List<Node> descendants(Node root, String name) {
		List<Node> result = new ArrayList<Node>();
		collect(root, name, result);
		return result;
	}

	private static void collect(Node root, String name, List<Node> result) {
		if (root == null)
			return;
		if (isWord(root, name))
			result.add(root);
		for (Node node = root.getFirstChild(); node != null; node = node.getNextSibling())
			collect(node, name, result);
	}
}

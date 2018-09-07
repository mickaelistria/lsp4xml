/**
 *  Copyright (c) 2018 Angelo ZERR
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lsp4xml.contentmodel.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.lsp4xml.contentmodel.model.CMAttributeDeclaration;
import org.eclipse.lsp4xml.contentmodel.model.CMElementDeclaration;
import org.eclipse.lsp4xml.settings.XMLFormattingOptions;
import org.eclipse.lsp4xml.utils.XMLBuilder;

/**
 * XML generator used to generate an XML fragment with formatting from a given
 * element declaration (XML Schema element declaration, DTD element, etc).
 */
public class XMLGenerator {

	private final XMLFormattingOptions formattingOptions;
	private final String whitespacesIndent;
	private final String lineDelimiter;
	private final boolean canSupportSnippets;
	private int maxLevel;

	/**
	 * XML generator constructor.
	 * 
	 * @param formattingOptions  the formatting options (uses spaces or tabs for
	 *                           indentation, etc)
	 * @param whitespacesIndent  the whitespaces to use to indent XML children
	 *                           elements.
	 * @param lineDelimiter      the line delimiter to use when several XML elements
	 *                           must be generated.
	 * @param canSupportSnippets true if snippets can be supported and false
	 *                           otherwise.
	 */
	public XMLGenerator(XMLFormattingOptions formattingOptions, String whitespacesIndent, String lineDelimiter,
			boolean canSupportSnippets, int maxLevel) {
		this.formattingOptions = formattingOptions;
		this.whitespacesIndent = whitespacesIndent;
		this.lineDelimiter = lineDelimiter;
		this.canSupportSnippets = canSupportSnippets;
	}

	public String generate(CMElementDeclaration elementDeclaration) {
		return generate(elementDeclaration, null);
	}

	/**
	 * Returns the XML generated from the given element declaration.
	 * 
	 * @param elementDeclaration
	 * @param prefix
	 * @return the XML generated from the given element declaration.
	 */
	public String generate(CMElementDeclaration elementDeclaration, String prefix) {
		XMLBuilder xml = new XMLBuilder(formattingOptions, whitespacesIndent, lineDelimiter);
		generate(elementDeclaration, prefix, 0, 0, xml, new ArrayList<CMElementDeclaration>());
		return xml.toString();
	}

	private int generate(CMElementDeclaration elementDeclaration, String prefix, int level, int snippetIndex,
			XMLBuilder xml, List<CMElementDeclaration> generatedElements) {
		if (generatedElements.contains(elementDeclaration)) {
			return snippetIndex;
		}
		generatedElements.add(elementDeclaration);
		if (level > 0) {
			xml.linefeed();
			xml.indent(level);
		}
		xml.startElement(prefix, elementDeclaration.getName(), false);
		// Attributes
		Collection<CMAttributeDeclaration> attributes = elementDeclaration.getAttributes();
		int attributeIndex = 0;
		for (CMAttributeDeclaration attributeDeclaration : attributes) {
			if (attributeDeclaration.isRequired()) {
				String value = "";
				if (canSupportSnippets) {
					snippetIndex++;
					value = ("$" + snippetIndex);
				}
				xml.addAttribute(attributeDeclaration.getName(), value, attributeIndex, level);
				attributeIndex++;
			}
		}
		// Elements children
		Collection<CMElementDeclaration> children = elementDeclaration.getElements();
		if (children.size() > 0) {
			xml.closeStartElement();
			if ((level > maxLevel)) {
				level++;
				for (CMElementDeclaration child : children) {
					snippetIndex = generate(child, prefix, level, snippetIndex, xml, generatedElements);
				}
				level--;
				xml.linefeed();
				xml.indent(level);
			} else {
				if (canSupportSnippets) {
					snippetIndex++;
					xml.addContent("$" + snippetIndex);
				}
			}
			xml.endElement(prefix, elementDeclaration.getName());
		} else if (elementDeclaration.isEmpty()) {
			xml.endElement();
		} else {
			xml.closeStartElement();
			if (canSupportSnippets) {
				snippetIndex++;
				xml.addContent("$" + snippetIndex);
			}
			xml.endElement(prefix, elementDeclaration.getName());
		}
		return snippetIndex;
	}

}

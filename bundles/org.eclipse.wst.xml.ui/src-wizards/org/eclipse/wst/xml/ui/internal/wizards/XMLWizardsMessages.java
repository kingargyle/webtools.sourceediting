/**********************************************************************
 * Copyright (c) 2005 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.wst.xml.ui.internal.wizards;

import org.eclipse.osgi.util.NLS;

/**
 * Strings used by XML Wizards
 * 
 * @since 1.0
 */
public class XMLWizardsMessages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.wst.xml.ui.internal.wizards.wizardResource";//$NON-NLS-1$

	public static String _UI_DIALOG_NEW_TITLE;
	public static String _UI_DIALOG_TITLE_INVALID_GRAMMAR;
	public static String _UI_DIALOG_MESSAGE_INVALID_GRAMMAR;
	public static String _UI_RADIO_XML_FROM_DTD;
	public static String _UI_RADIO_XML_FROM_SCHEMA;
	public static String _UI_RADIO_XML_FROM_SCRATCH;
	public static String _UI_WIZARD_CREATE_XML_HEADING;
	public static String _UI_WIZARD_CREATE_XML_EXPL;
	public static String _UI_WIZARD_CREATE_XML_FILE_HEADING;
	public static String _UI_WIZARD_CREATE_XML_FILE_EXPL;
	public static String _UI_WIZARD_SELECT_DTD_FILE_DESC;
	public static String _UI_WIZARD_SELECT_DTD_FILE_TITLE;
	public static String _UI_WIZARD_SELECT_XSD_FILE_DESC;
	public static String _UI_WIZARD_SELECT_XSD_FILE_TITLE;
	public static String _UI_WIZARD_SELECT_ROOT_HEADING;
	public static String _UI_WIZARD_SELECT_ROOT_EXPL;
	public static String _UI_LABEL_ROOT_ELEMENT;
	public static String _UI_WARNING_TITLE_NO_ROOT_ELEMENTS;
	public static String _UI_WARNING_MSG_NO_ROOT_ELEMENTS;
	public static String _UI_LABEL_NO_LOCATION_HINT;
	public static String _UI_WARNING_MSG_NO_LOCATION_HINT_1;
	public static String _UI_WARNING_MSG_NO_LOCATION_HINT_2;
	public static String _UI_WARNING_MSG_NO_LOCATION_HINT_3;
	public static String _UI_WIZARD_CONTENT_OPTIONS;
	public static String _UI_WIZARD_CREATE_REQUIRED;// commented out
	public static String _UI_WIZARD_CREATE_OPTIONAL;// commented out
	public static String _UI_WIZARD_CREATE_OPTIONAL_ATTRIBUTES;
	public static String _UI_WIZARD_CREATE_OPTIONAL_ELEMENTS;
	public static String _UI_WIZARD_CREATE_FIRST_CHOICE;
	public static String _UI_WIZARD_FILL_ELEMENTS_AND_ATTRIBUTES;
	public static String _UI_LABEL_DOCTYPE_INFORMATION;
	public static String _UI_LABEL_SYSTEM_ID;
	public static String _UI_LABEL_PUBLIC_ID;
	public static String _UI_WARNING_URI_NOT_FOUND_COLON;
	public static String _UI_INVALID_GRAMMAR_ERROR;
	public static String _ERROR_BAD_FILENAME_EXTENSION;
	public static String _ERROR_FILE_ALREADY_EXISTS;
	public static String _ERROR_ROOT_ELEMENT_MUST_BE_SPECIFIED;
	public static String _UI_LABEL_ERROR_SCHEMA_INVALID_INFO;
	public static String _UI_LABEL_ERROR_DTD_INVALID_INFO;
	public static String _UI_LABEL_ERROR_CATALOG_ENTRY_INVALID;
	public static String _UI_LABEL_NAMESPACE_INFORMATION;
	public static String Validation_Plugins_Unavailable;
	public static String Validation_cannot_be_performed;
	
	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, XMLWizardsMessages.class);
	}

	private XMLWizardsMessages() {
		// cannot create new instance
	}
}

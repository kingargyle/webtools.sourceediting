/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jst.jsp.core.internal.java;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jst.jsp.core.internal.Logger;
import org.eclipse.text.edits.DeleteEdit;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;


/**
 * Adds the notion of IDocuments (jsp Document and java Document)
 * Used for TextEdit translation
 * @author pavery
 */
public class JSPTranslationExtension extends JSPTranslation {
	
	// for debugging
	private static final boolean DEBUG;
	static {
		String value= Platform.getDebugOption("org.eclipse.jst.jsp.core/debug/jsptranslation"); //$NON-NLS-1$
		DEBUG= value != null && value.equalsIgnoreCase("true"); //$NON-NLS-1$
	}
	
	// just a convenience data structure
	// to keep track of java position deltas
	private class PositionDelta {
		
		public boolean isDeleted = false;
		public int preOffset = 0;
		public int preLength = 0;
		public int postOffset = 0;
		public int postLength = 0;
		
		public PositionDelta(int preOffset, int preLength) {
			this.preOffset = preOffset;
			this.preLength = preLength;
		}
		public void setPostEditData(int postOffset, int postLength, boolean isDeleted) {
			this.postOffset = postOffset;
			this.postLength = postLength;
			this.isDeleted = isDeleted;
		}
	}
	
	private IDocument fJspDocument = null;
	private IDocument fJavaDocument = null;
	
	public JSPTranslationExtension(IDocument jspDocument, IDocument javaDocument, IJavaProject javaProj, JSPTranslator translator) {
		super(javaProj, translator);
		fJspDocument = jspDocument;
		fJavaDocument = javaDocument;
		
		// make sure positions are added to Java and JSP documents
		// this is necessary for text edits
		addPositionsToDocuments();
	}
	
	public IDocument getJspDocument() {
		return fJspDocument;
	}
	
	public IDocument getJavaDocument() {
		return fJavaDocument;
	}
	
	public String getJavaText() {
		return getJavaDocument() != null ? getJavaDocument().get() : ""; //$NON-NLS-1$
	}
	
	/**
	 * Returns a corresponding TextEdit for the JSP file given a TextEdit for a Java file.
	 * Note: This method actually applies the Java edit to the internal Java document.
	 * 
	 * @param javaEdit
	 * @return the corresponding JSP edits (not applied to the document yet)
	 */
	public TextEdit getJspEdit(TextEdit javaEdit) {

		if(javaEdit == null)
			return null;
		
		List jspEdits = new ArrayList();

		int offset = javaEdit.getOffset();
		int length = javaEdit.getLength();
		
		if(javaEdit instanceof MultiTextEdit && javaEdit.getChildren().length > 0) {
	
			IRegion r = TextEdit.getCoverage(getAllEdits(javaEdit));
			offset = r.getOffset();
			length = r.getLength();
		}
		
		// get java ranges that will be affected by the edit
		Position[] javaPositions = getJavaRanges(offset, length);
		
		// record position data before the change
		Position[] jspPositions = new Position[javaPositions.length];
		PositionDelta[] deltas = new PositionDelta[javaPositions.length];
		for(int i=0; i<javaPositions.length; i++) {
			deltas[i] = new PositionDelta(javaPositions[i].offset, javaPositions[i].length);
			// isIndirect means the position doesn't actually exist as exact text 
			// mapping from java <-> jsp (eg. an import statement)
			if(!isIndirect(javaPositions[i].offset))
				jspPositions[i] = (Position)getJava2JspMap().get(javaPositions[i]);
		}

		//if(DEBUG) {
			System.out.println("================================================");
			System.out.println("deltas:");
			String javaText = getJavaText();
			for(int i=0; i<deltas.length; i++) 
				System.out.println("pos[" + deltas[i].preOffset + ":" + deltas[i].preLength + "]" + javaText.substring(deltas[i].preOffset, deltas[i].preOffset + deltas[i].preLength) );
			System.out.println("===============================================");
		//}
		
		// apply the edit to the java document
		try {
			javaEdit.apply(getJavaDocument());
		} catch (MalformedTreeException e) {
			Logger.logException(e);
		} catch (BadLocationException e) {
			Logger.logException(e);
		}
		// now at this point Java positions are unreliable since they were updated after applying java edit.
		
		String newJavaText = getJavaDocument().get();
		if(DEBUG) 
			System.out.println("java post format text:\n" + newJavaText); //$NON-NLS-1$
		
		// record post edit data
		for(int i=0; i<javaPositions.length; i++)
			deltas[i].setPostEditData(javaPositions[i].offset, javaPositions[i].length, javaPositions[i].isDeleted);
		
		// create appropriate text edits for deltas
		Position jspPos = null;
		String replaceText = ""; //$NON-NLS-1$
		for(int i=0; i<deltas.length; i++) {
			jspPos = jspPositions[i];
			// can be null if it's an indirect mapping position
			// or if something was added into java that was not originally in JSP (like a new import...)

			if(jspPos != null) {
				if(deltas[i].isDeleted) {
					jspEdits.add(new DeleteEdit(jspPos.offset, jspPos.length));
				}
				else {
					replaceText = newJavaText.substring(deltas[i].postOffset, deltas[i].postOffset + deltas[i].postLength);
					jspEdits.add(new ReplaceEdit(jspPos.offset, jspPos.length, replaceText));
				}
				if(DEBUG) 
					debugReplace(deltas, jspPos, replaceText, i);
			}
			else {
				// the new Java text has no corresponding JSP position
				// possible new import?
				if(isImport(javaPositions[i].getOffset()) && replaceText.lastIndexOf("import ") != -1) { //$NON-NLS-1$
					replaceText = newJavaText.substring(deltas[i].postOffset, deltas[i].postOffset + deltas[i].postLength);
					String importText = replaceText.substring(replaceText.lastIndexOf("import "), replaceText.indexOf(";")); //$NON-NLS-1$ //$NON-NLS-2$
					// evenutally need to check if it's XML-JSP
					importText = "<%@page import=\"" + importText + "\" %>\n"; //$NON-NLS-1$ //$NON-NLS-2$
					jspEdits.add(new InsertEdit(0, importText));
				}
			}
		}
		return createMultiTextEdit((TextEdit[])jspEdits.toArray(new TextEdit[jspEdits.size()]));
	}
	

	/**
	 * Combines an array of edits into one MultiTextEdit (with the appropriate coverage region)
	 * @param edits
	 * @return
	 */
	private TextEdit createMultiTextEdit(TextEdit[] edits) {
		
		if(edits.length == 0)
			return new MultiTextEdit();
			
		IRegion region = TextEdit.getCoverage(edits);
		MultiTextEdit multiEdit = new MultiTextEdit(region.getOffset(), region.getLength());
		for (int i = 0; i < edits.length; i++) {
			addToMultiEdit(edits[i], multiEdit);
		}
		return multiEdit;
	}
	
	
	private void addToMultiEdit(TextEdit edit, MultiTextEdit multiEdit) {
		
		// check for overlap here
		// discard overlapping edits..
		// possible exponential performance hit... need a better way...
		TextEdit[] children = multiEdit.getChildren();
		for (int i = 0; i < children.length; i++) {
			if(children[i].covers(edit))
				// don't add
				return;
		}
		multiEdit.addChild(edit);
	}
	
	
	/**
	 * @param translation
	 */
	private void addPositionsToDocuments() {
		
		// can be null if it's a NullJSPTranslation
		if(getJavaDocument() != null && getJspDocument() != null) {

			HashMap java2jsp = getJava2JspMap();
			Iterator it = java2jsp.keySet().iterator();
			Position javaPos = null;
			while(it.hasNext()) {
				javaPos =(Position)it.next();
				try {
					
					fJavaDocument.addPosition(javaPos);
					
				} catch (BadLocationException e) {
					if(DEBUG) {
						System.out.println("tyring to add Java Position:[" + javaPos.offset + ":" + javaPos.length + "] to " + getJavaPath()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$					
						//System.out.println("substring :[" + fJavaDocument.get().substring(javaPos.offset) + "]"); //$NON-NLS-1$ //$NON-NLS-2$
						Logger.logException(e);
					}
				}
				
				try {
					
					fJspDocument.addPosition((Position)java2jsp.get(javaPos));
					
				} catch (BadLocationException e) {
					if(DEBUG) {
						System.out.println("tyring to add JSP Position:[" + ((Position)java2jsp.get(javaPos)).offset + ":" +((Position)java2jsp.get(javaPos)).length + "] to " + getJavaPath()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						Logger.logException(e);
					}
				}
			}
		}
	}
	
	/**
	 * Recursively gets all child edits
	 * @param javaEdit
	 * @return all child edits
	 */
	private TextEdit[] getAllEdits(TextEdit javaEdit) {
		
		List result = new ArrayList();
		if(javaEdit instanceof MultiTextEdit) {
			TextEdit[] children = javaEdit.getChildren();
			for (int i = 0; i < children.length; i++) 
				result.addAll(Arrays.asList(getAllEdits(children[i])));
		}
		else 
			result.add(javaEdit);
		return (TextEdit[])result.toArray(new TextEdit[result.size()]);
	}

	/**
	 * @param deltas
	 * @param jspPos
	 * @param replaceText
	 * @param jspText
	 * @param i
	 */
	private void debugReplace(PositionDelta[] deltas, Position jspPos, String replaceText, int i) {
		String jspChunk;
		jspChunk = getJspDocument().get().substring(jspPos.offset, jspPos.offset + jspPos.length);
		if(!deltas[i].isDeleted) {
			System.out.println("replacing:"); //$NON-NLS-1$
			System.out.println("jsp:[" + jspChunk + "]"); //$NON-NLS-1$ //$NON-NLS-2$
			System.out.println("w/ :[" + replaceText + "]"); //$NON-NLS-1$ //$NON-NLS-2$
			System.out.println("--------------------------------"); //$NON-NLS-1$
		}
	}
}

/*******************************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Jens Lukowski/Innoopract - initial renaming/restructuring
 *     
 *******************************************************************************/
package org.eclipse.wst.xml.core.internal.contentmodel.modelquery;

import org.eclipse.wst.xml.core.internal.contentmodel.util.CMDocumentCacheListener;

/**
 *
 */
public interface CMDocumentManagerListener extends CMDocumentCacheListener
{                    
  public void propertyChanged(CMDocumentManager cmDocumentManager, String propertyName);
}       

/*******************************************************************************
 * Copyright (c) 2005 BEA Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     BEA Systems - initial implementation
 *     
 *******************************************************************************/
/* Generated By:JJTree: Do not edit this line. ASTValueSuffix.java */

package org.eclipse.jst.jsp.core.internal.java.jspel;

public class ASTValueSuffix extends SimpleNode {
	
  protected Token propertyNameToken;
  
  public ASTValueSuffix(int id) {
    super(id);
  }

  public ASTValueSuffix(JSPELParser p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(JSPELParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }

public Token getPropertyNameToken() {
	return propertyNameToken;
}

public void setPropertyNameToken(Token propertyNameToken) {
	this.propertyNameToken = propertyNameToken;
}
}
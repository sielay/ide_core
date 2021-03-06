/**
 * This file Copyright (c) 2005-2007 Aptana, Inc. This program is
 * dual-licensed under both the Aptana Public License and the GNU General
 * Public license. You may elect to use one or the other of these licenses.
 * 
 * This program is distributed in the hope that it will be useful, but
 * AS-IS and WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE, TITLE, or
 * NONINFRINGEMENT. Redistribution, except as permitted by whichever of
 * the GPL or APL you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or modify this
 * program under the terms of the GNU General Public License,
 * Version 3, as published by the Free Software Foundation.  You should
 * have received a copy of the GNU General Public License, Version 3 along
 * with this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 * 
 * Aptana provides a special exception to allow redistribution of this file
 * with certain Eclipse Public Licensed code and certain additional terms
 * pursuant to Section 7 of the GPL. You may view the exception and these
 * terms on the web at http://www.aptana.com/legal/gpl/.
 * 
 * 2. For the Aptana Public License (APL), this program and the
 * accompanying materials are made available under the terms of the APL
 * v1.0 which accompanies this distribution, and is available at
 * http://www.aptana.com/legal/apl/.
 * 
 * You may view the GPL, Aptana's exception and additional terms, and the
 * APL in the file titled license.html at the root of the corresponding
 * plugin containing this source file.
 * 
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.ide.core.model;

import java.util.Map;

/**
 * @author Kevin Sawicki (ksawicki@aptana.com)
 */
public class SimpleServiceRequest implements IServiceRequest
{

	private String contentType;
	private String content;
	private String method;
	private String authHeader;

	/**
	 * Creates an empty base cloud request
	 * 
	 * @param contentType
	 * @param content
	 * @param method
	 * @param authorization
	 */
	public SimpleServiceRequest(String contentType, String content, String method, String authorization)
	{
		this.contentType = contentType;
		this.content = content;
		this.method = method;
		this.authHeader = authorization;
	}

	/**
	 * @see com.aptana.ide.core.model.IServiceRequest#getContents()
	 */
	public String getContents()
	{
		return this.content;
	}

	/**
	 * Sets the contents
	 * 
	 * @param content
	 */
	public void setContents(String content)
	{
		this.content = content;
	}

	/**
	 * @see com.aptana.ide.core.model.IServiceRequest#getContentType()
	 */
	public String getContentType()
	{
		return this.contentType;
	}

	/**
	 * Sets the content type
	 * 
	 * @param contentType
	 */
	public void setContentType(String contentType)
	{
		this.contentType = contentType;
	}

	/**
	 * @see com.aptana.ide.core.model.IServiceRequest#containsBody()
	 */
	public boolean containsBody()
	{
		return this.content != null && this.content.length() > 0;
	}

	/**
	 * @see com.aptana.ide.core.model.IServiceRequest#getRequestType()
	 */
	public String getRequestType()
	{
		return this.method;
	}

	/**
	 * Sets the request type
	 * 
	 * @param method
	 */
	public void setRequestType(String method)
	{
		this.method = method;
	}

	/**
	 * @see com.aptana.ide.core.model.IServiceRequest#getAuthentication()
	 */
	public String getAuthentication()
	{
		return this.authHeader;
	}

	/**
	 * Sets the authentication header
	 * 
	 * @param authentication
	 */
	public void setAuthentication(String authentication)
	{
		this.authHeader = authentication;
	}

	/**
	 * @see com.aptana.ide.core.model.IServiceRequest#getAccept()
	 */
	public String getAccept()
	{
		return getContentType();
	}
	
	/**
	 * Returns null.
	 * 
	 * @see com.aptana.ide.core.model.IServiceRequest#getRequestProperties()
	 */
	public Map<String, String> getRequestProperties() {
		return null;
	}
}

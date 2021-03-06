/**
 * This file Copyright (c) 2005-2009 Aptana, Inc. This program is
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
 * with certain other free and open source software ("FOSS") code and certain additional terms
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

package com.aptana.ide.filesystem.ftp;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;

import com.aptana.ide.core.StringUtils;
import com.aptana.ide.core.epl.IMemento;
import com.aptana.ide.core.io.ConnectionContext;
import com.aptana.ide.core.io.ConnectionPoint;
import com.aptana.ide.core.io.CoreIOPlugin;
import com.aptana.ide.core.io.vfs.IConnectionFileManager;

/**
 * @author Max Stepanov
 *
 */
public class FTPConnectionPoint extends ConnectionPoint implements IBaseFTPConnectionPoint {

	public static final String TYPE = TYPE_FTP;

	private static final String ELEMENT_HOST = "host"; //$NON-NLS-1$
	private static final String ELEMENT_PORT = "port"; //$NON-NLS-1$
	private static final String ELEMENT_PATH = "path"; //$NON-NLS-1$
	private static final String ELEMENT_LOGIN = "login"; //$NON-NLS-1$
	private static final String ELEMENT_PASSIVE = "passive"; //$NON-NLS-1$
	private static final String ELEMENT_TRANSFER_TYPE = "transferType"; //$NON-NLS-1$
	private static final String ELEMENT_ENCODING = "encoding"; //$NON-NLS-1$
	private static final String ELEMENT_TIMEZONE = "timezone"; //$NON-NLS-1$
		
	private String host;
	private int port = IFTPConstants.FTP_PORT_DEFAULT;
	private IPath path = Path.ROOT;
	private String login = StringUtils.EMPTY;
	private char[] password;
	private boolean passiveMode = true;
	private String transferType = IFTPConstants.TRANSFER_TYPE_BINARY;
	private String encoding = IFTPConstants.ENCODING_DEFAULT;
	private String timezone = null;
	
	private IFTPConnectionFileManager connectionFileManager;
	
	/**
	 * Default constructor
	 */
	public FTPConnectionPoint() {
		super(TYPE);
	}

	/* (non-Javadoc)
	 * @see com.aptana.ide.core.io.ConnectionPoint#loadState(com.aptana.ide.core.io.epl.IMemento)
	 */
	@Override
	protected void loadState(IMemento memento) {
		super.loadState(memento);
		IMemento child = memento.getChild(ELEMENT_HOST);
		if (child != null) {
			host = child.getTextData();
		}
		child = memento.getChild(ELEMENT_PORT);
		if (child != null) {
			try {
				port = Integer.parseInt(child.getTextData());
			} catch (NumberFormatException e) {
			}
		}
		child = memento.getChild(ELEMENT_PATH);
		if (child != null) {
			path = Path.fromPortableString(child.getTextData());
		}
		child = memento.getChild(ELEMENT_LOGIN);
		if (child != null) {
			login = child.getTextData();
		}
		child = memento.getChild(ELEMENT_PASSIVE);
		if (child != null) {
			passiveMode = Boolean.parseBoolean(child.getTextData());
		}
		child = memento.getChild(ELEMENT_TRANSFER_TYPE);
		if (child != null) {
			transferType = child.getTextData();
		}
		child = memento.getChild(ELEMENT_ENCODING);
		if (child != null) {
			encoding = child.getTextData();
		}
		child = memento.getChild(ELEMENT_TIMEZONE);
		if (child != null) {
			timezone = child.getTextData();
		}
	}

	/* (non-Javadoc)
	 * @see com.aptana.ide.core.io.ConnectionPoint#saveState(com.aptana.ide.core.io.epl.IMemento)
	 */
	@Override
	protected void saveState(IMemento memento) {
		super.saveState(memento);
		memento.createChild(ELEMENT_HOST).putTextData(host);
		if (IFTPConstants.FTP_PORT_DEFAULT != port) {
			memento.createChild(ELEMENT_PORT).putTextData(Integer.toString(port));
		}
		if (!Path.ROOT.equals(path)) {
			memento.createChild(ELEMENT_PATH).putTextData(path.toPortableString());
		}
		if (login.length() != 0) {
			memento.createChild(ELEMENT_LOGIN).putTextData(login);
		}
		memento.createChild(ELEMENT_PASSIVE).putTextData(Boolean.toString(passiveMode));
		if (!IFTPConstants.TRANSFER_TYPE_AUTO.equals(transferType)) {
			memento.createChild(ELEMENT_TRANSFER_TYPE).putTextData(transferType);
		}
		if (!IFTPConstants.ENCODING_DEFAULT.equals(encoding)) {
			memento.createChild(ELEMENT_ENCODING).putTextData(encoding);
		}
		if (timezone != null && timezone.length() != 0) {
			memento.createChild(ELEMENT_TIMEZONE).putTextData(timezone);
		}
	}

	/* (non-Javadoc)
	 * @see com.aptana.ide.core.ftp.IBaseRemoteConnectionPoint#getHost()
	 */
	public String getHost() {
		return host;
	}

	/* (non-Javadoc)
	 * @see com.aptana.ide.core.ftp.IBaseRemoteConnectionPoint#setHost(java.lang.String)
	 */
	public void setHost(String host) {
		this.host = host;
		notifyChanged();
		resetConnectionFileManager();
	}

	/* (non-Javadoc)
	 * @see com.aptana.ide.core.ftp.IBaseRemoteConnectionPoint#getPort()
	 */
	public int getPort() {
		return port;
	}

	/* (non-Javadoc)
	 * @see com.aptana.ide.core.ftp.IBaseRemoteConnectionPoint#setPort(int)
	 */
	public void setPort(int port) {
		this.port = port;
		notifyChanged();
		resetConnectionFileManager();
	}

	/* (non-Javadoc)
	 * @see com.aptana.ide.core.ftp.IBaseRemoteConnectionPoint#getPath()
	 */
	public IPath getPath() {
		return path;
	}

	/* (non-Javadoc)
	 * @see com.aptana.ide.core.ftp.IBaseRemoteConnectionPoint#setPath(org.eclipse.core.runtime.IPath)
	 */
	public void setPath(IPath path) {
		this.path = path;
		notifyChanged();
		resetConnectionFileManager();
	}

	/* (non-Javadoc)
	 * @see com.aptana.ide.core.ftp.IBaseRemoteConnectionPoint#getLogin()
	 */
	public String getLogin() {
		return login;
	}

	/* (non-Javadoc)
	 * @see com.aptana.ide.core.ftp.IBaseRemoteConnectionPoint#setLogin(java.lang.String)
	 */
	public void setLogin(String login) {
		this.login = login;
		notifyChanged();
		resetConnectionFileManager();
	}

	/* (non-Javadoc)
	 * @see com.aptana.ide.core.ftp.IBaseRemoteConnectionPoint#getPassword()
	 */
	public char[] getPassword() {
		return password;
	}

	/* (non-Javadoc)
	 * @see com.aptana.ide.core.ftp.IBaseRemoteConnectionPoint#setPassword(char[])
	 */
	public void setPassword(char[] password) {
		this.password = password;
		notifyChanged();
		resetConnectionFileManager();
	}

	/**
	 * @return the passiveMode
	 */
	public boolean isPassiveMode() {
		return passiveMode;
	}

	/**
	 * @param passiveMode the passiveMode to set
	 */
	public void setPassiveMode(boolean passiveMode) {
		this.passiveMode = passiveMode;
		notifyChanged();
		resetConnectionFileManager();
	}

	/**
	 * @return the transferType
	 */
	public String getTransferType() {
		return transferType;
	}

	/**
	 * @param transferType the transferType to set
	 */
	public void setTransferType(String transferType) {
		this.transferType = transferType;
		notifyChanged();
		resetConnectionFileManager();
	}

	/**
	 * @return the encoding
	 */
	public String getEncoding() {
		return encoding;
	}

	/**
	 * @param encoding the encoding to set
	 */
	public void setEncoding(String encoding) {
		this.encoding = encoding;
		notifyChanged();
		resetConnectionFileManager();
	}

	/**
	 * @return the timezone
	 */
	public String getTimezone() {
		return timezone;
	}

	/**
	 * @param timezone the timezone to set
	 */
	public void setTimezone(String timezone) {
		this.timezone = timezone;
		notifyChanged();
		resetConnectionFileManager();
	}

	/* (non-Javadoc)
	 * @see com.aptana.ide.core.io.ConnectionPoint#connect(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void connect(IProgressMonitor monitor) throws CoreException {
		if (isConnected()) {
			return;
		}
		ConnectionContext context = CoreIOPlugin.getConnectionContext(this);
		if (context != null) {
			CoreIOPlugin.setConnectionContext(connectionFileManager, context);
		}
		getConnectionFileManager().connect(monitor);
	}

	/* (non-Javadoc)
	 * @see com.aptana.ide.core.io.ConnectionPoint#disconnect(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void disconnect(IProgressMonitor monitor) throws CoreException {
		if (isConnected()) {
			getConnectionFileManager().disconnect(monitor);
		}
	}

	/* (non-Javadoc)
	 * @see com.aptana.ide.core.io.ConnectionPoint#isConnected()
	 */
	@Override
	public boolean isConnected() {
		return connectionFileManager != null && connectionFileManager.isConnected();
	}

	/* (non-Javadoc)
	 * @see com.aptana.ide.core.io.ConnectionPoint#canDisconnect()
	 */
	@Override
	public boolean canDisconnect() {
		return isConnected() && true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.PlatformObject#getAdapter(java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Object getAdapter(Class adapter) {
		if (IConnectionFileManager.class == adapter) {
			return getConnectionFileManager();
		}
		return super.getAdapter(adapter);
	}
	
	private synchronized void resetConnectionFileManager() {
		connectionFileManager = null;
	}
	
	private synchronized IConnectionFileManager getConnectionFileManager() {
		if (connectionFileManager == null) {
			// find contributed first
			connectionFileManager = (IFTPConnectionFileManager) super.getAdapter(IFTPConnectionFileManager.class);
			if (connectionFileManager == null) {
				connectionFileManager = new FTPConnectionFileManager();
			}
			ConnectionContext context = CoreIOPlugin.getConnectionContext(this);
			if (context != null) {
				CoreIOPlugin.setConnectionContext(connectionFileManager, context);
			}
			connectionFileManager.init(host, port, path, login, password, passiveMode, transferType, encoding, timezone);
		}
		return connectionFileManager;
	}

	
}

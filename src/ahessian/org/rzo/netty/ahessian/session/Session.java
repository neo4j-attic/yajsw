package org.rzo.netty.ahessian.session;

import java.util.Collection;

import org.jboss.netty.util.Timeout;

/**
 * A session object.
 * TODO for now session objects just hold the id. Handling of session timeout is missing
 */
public interface Session
{
	
	/**
	 * Gets the id of the session.
	 * 
	 * @return the session id
	 */
	public String getId();
	
	/**
	 * TODO Destroy the current session and associated pipeline.
	 */
	public void invalidate();
	
	/**
	 * Adds a listener which is notified if the channel associated is closed
	 * 
	 * @param listener the listener
	 */
	public void addClosedListener(Runnable listener);
	public void addInvalidatedListener(Runnable listener);
	
	public Object getAttribute(String name);
	public Collection<String> getAttributeNames();
	public void removeAttribute(String name);
	public void setAttribute(String name, Object value);
	
	public long getCreationTime();
	public long getLastConnectedTime();
	public long getLastEstablishedTime();
	public long getMaxInactiveInterval();
	public void setMaxInactiveInterval(long time);
	public boolean isNew();
	public void setNew(boolean newValue);
	
	public Collection<Session> allSessions();
	
	public void setTimeOut(Timeout timeOut);
	
	public Timeout removeTimeout();

	

}

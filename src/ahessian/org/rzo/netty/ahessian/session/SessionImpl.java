package org.rzo.netty.ahessian.session;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.netty.util.Timeout;

/**
 * The Class SessionImpl.
 */
class SessionImpl implements Session
{
	
	/** The _id. */
	String _id;
	
	/** The _listeners. */
	List<Runnable> _listeners = Collections.synchronizedList(new ArrayList<Runnable>());
	
	private Map<String, Object> _attributes = new HashMap<String, Object>();
	
	long _created;
	
	boolean _new = true;
	
	Timeout _timeOut = null;
	
	/**
	 * Instantiates a new session impl.
	 * 
	 * @param id the id
	 */
	SessionImpl(String id)
	{
		_id = id;
		_created = System.currentTimeMillis();
		
	}

	/* (non-Javadoc)
	 * @see handler.session.Session#getId()
	 */
	
	public String getId()
	{
		return _id;
	}

	/* (non-Javadoc)
	 * @see handler.session.Session#addClosedListener(java.lang.Runnable)
	 */
	
	public void addClosedListener(Runnable listener)
	{
		_listeners.add(listener);
	}

	/* (non-Javadoc)
	 * @see handler.session.Session#close()
	 */
	
	public void close()
	{
		synchronized(_listeners)
		{
			for (Runnable listener : _listeners)
				listener.run();
		}
	}

	public void addInvalidatedListener(Runnable listener)
	{
		// TODO Auto-generated method stub
		
	}

	public Collection<Session> allSessions()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Object getAttribute(String name)
	{
		return _attributes.get(name);
	}

	public Collection<String> getAttributeNames()
	{
		return new ArrayList(_attributes.keySet());
	}

	public long getCreationTime()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	public long getLastConnectedTime()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	public long getLastEstablishedTime()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	public long getMaxInactiveInterval()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	public void invalidate()
	{
		// TODO Auto-generated method stub
		
	}

	public boolean isNew()
	{
		return _new;
	}

	public void removeAttribute(String name)
	{
		_attributes.remove(name);
	}

	public void setAttribute(String name, Object value)
	{
		_attributes.put(name, value);
	}

	public void setMaxInactiveInterval(long time)
	{
		// TODO Auto-generated method stub
		
	}

	public void setNew(boolean newValue)
	{
		_new = newValue;
	}
	
	public void setTimeOut(Timeout timeOut)
	{
		_timeOut = timeOut;
	}
	
	public Timeout removeTimeout()
	{
		Timeout result = _timeOut;
		_timeOut = null;
		return result;
	}

}
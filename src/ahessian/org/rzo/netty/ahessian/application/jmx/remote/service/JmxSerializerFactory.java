package org.rzo.netty.ahessian.application.jmx.remote.service;

import com.caucho.hessian4.io.AbstractSerializerFactory;
import com.caucho.hessian4.io.Deserializer;
import com.caucho.hessian4.io.HessianProtocolException;
import com.caucho.hessian4.io.Serializer;

public class JmxSerializerFactory extends AbstractSerializerFactory
{
	Serializer _objectNameSerializer = new ObjectNameSerializer();
	Deserializer _objectNameDeserializer = new ObjectNameDeserializer();

	public Deserializer getDeserializer(Class cl) throws HessianProtocolException
	{
		if (cl.getName().equals("javax.management.ObjectName"))
		return _objectNameDeserializer;
		return null;
	}

	public Serializer getSerializer(Class cl) throws HessianProtocolException
	{
		if (cl.getName().equals("javax.management.ObjectName"))
			return _objectNameSerializer;
			return null;
	}

}

package com.sojava.beehive.hibernate.postgresql.datatype;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.type.SerializationException;
import org.hibernate.usertype.UserType;
import org.postgresql.util.PGobject;
import org.springframework.util.ObjectUtils;

import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;

public class JsonDataUserType implements UserType {

	@Override
	public void nullSafeSet(PreparedStatement st, Object value, int index, SessionImplementor session) throws HibernateException, SQLException {
		if (value == null) {
			st.setNull(index, Types.OTHER);
		} else {
			st.setObject(index, value, Types.OTHER);
		}
	}

	@Override
	public Object nullSafeGet(ResultSet rs, String[] names, SessionImplementor session, Object owner) throws HibernateException, SQLException {
		PGobject o = (PGobject) rs.getObject(names[0]);

		if (o.getValue() != null) {
			if (o.getType().equalsIgnoreCase("jsonb")) return deepCopy(o.getValue());
			else return o.getValue();
		}
		return null;
	}

	@Override
	public JSON deepCopy(Object originalValue) throws HibernateException {
		if (originalValue == null) {
			return null;
		}
		try {
			return JSONObject.fromObject(originalValue.toString());//toString();
		}
		catch(JSONException e) {
			return JSONArray.fromObject(originalValue.toString());
		}
	}

	@Override
	public Serializable disassemble(Object value) throws HibernateException {
		Object copy = deepCopy(value);

		if (copy instanceof Serializable) {
			return (Serializable) copy;
		}

		throw new SerializationException(String.format("Cannot serialize '%s', %s is not Serializable.", value, value.getClass()), null);
	}

	@Override
	public Object assemble(Serializable cached, Object owner) throws HibernateException {
		return deepCopy(cached);
	}

	@Override
	public Object replace(Object original, Object target, Object owner) throws HibernateException {
		return deepCopy(original);
	}

	@Override
	public boolean isMutable() {
		return true;
	}

	@Override
	public int hashCode(Object x) throws HibernateException {
		if (x == null) {
			return 0;
		}

		return x.hashCode();
	}

	@Override
	public boolean equals(Object x, Object y) throws HibernateException {
		return ObjectUtils.nullSafeEquals(x, y);
	}

	@Override
	public Class<?> returnedClass() {
		return String.class;
	}

	@Override
	public int[] sqlTypes() {
		return new int[]{Types.JAVA_OBJECT};
	}
}

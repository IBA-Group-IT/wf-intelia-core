package com.ibagroup.wf.intelia.core.robots.factory;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ibagroup.wf.intelia.core.annotations.Wire;
import com.ibagroup.wf.intelia.core.to.BaseTO;
import com.ibagroup.wf.intelia.core.utils.BindingUtils;
import groovy.lang.Binding;

public class RobotsFactoryHelper {

	private static final Map<Class<?>, Object> PRIMITIVE_OR_WRAPPER_DEFAULT_VALUES = new HashMap<>();

	static {
		PRIMITIVE_OR_WRAPPER_DEFAULT_VALUES.put(Boolean.class, false);
		PRIMITIVE_OR_WRAPPER_DEFAULT_VALUES.put(Character.class, '\u0000');
		PRIMITIVE_OR_WRAPPER_DEFAULT_VALUES.put(Byte.class, (byte) 0);
		PRIMITIVE_OR_WRAPPER_DEFAULT_VALUES.put(Short.class, (short) 0);
		PRIMITIVE_OR_WRAPPER_DEFAULT_VALUES.put(Integer.class, 0);
		PRIMITIVE_OR_WRAPPER_DEFAULT_VALUES.put(Long.class, 0L);
		PRIMITIVE_OR_WRAPPER_DEFAULT_VALUES.put(Float.class, 0F);
		PRIMITIVE_OR_WRAPPER_DEFAULT_VALUES.put(Double.class, 0D);

		PRIMITIVE_OR_WRAPPER_DEFAULT_VALUES.put(boolean.class, false);
		PRIMITIVE_OR_WRAPPER_DEFAULT_VALUES.put(char.class, '\u0000');
		PRIMITIVE_OR_WRAPPER_DEFAULT_VALUES.put(byte.class, (byte) 0);
		PRIMITIVE_OR_WRAPPER_DEFAULT_VALUES.put(short.class, (short) 0);
		PRIMITIVE_OR_WRAPPER_DEFAULT_VALUES.put(int.class, 0);
		PRIMITIVE_OR_WRAPPER_DEFAULT_VALUES.put(long.class, 0L);
		PRIMITIVE_OR_WRAPPER_DEFAULT_VALUES.put(float.class, 0F);
		PRIMITIVE_OR_WRAPPER_DEFAULT_VALUES.put(double.class, 0D);
	}

	public static void setField(Object where, Field field, Object value) {
		boolean accessible = field.isAccessible();
		field.setAccessible(true);
		try {
			field.set(where, value);

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		field.setAccessible(accessible);
	}

    public static Object getFieldValue(Object where, Field field) {
        boolean accessible = field.isAccessible();
        field.setAccessible(true);
        try {
            return field.get(where);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            field.setAccessible(accessible);
        }
    }

	public static <T> void wireObject(T robot, Map<Class<?>, Object> wiringObjects, Binding binding) {
		FieldUtils.getFieldsListWithAnnotation(robot.getClass(), Wire.class).stream().forEach(field -> {
			if (!wiringObjects.entrySet().stream().anyMatch(clsEntry -> {
				if (field.getType().isAssignableFrom(clsEntry.getKey())) {
					setField(robot, field, clsEntry.getValue());
					return true;
				}
				return false;
			})) {
				// trying to wire from binding
				Wire wireanno = field.getAnnotation(Wire.class);
				if (null != binding) {
					String name = wireanno.name().trim().isEmpty() ? field.getName() : wireanno.name();
					Object value = BindingUtils.getTypedPropertyValue(binding, name);
					if(null != value){
						wireField(field, name, robot, value);
						return;
					}
				}
				if (wireanno.required()) {
					// if no match found and required then throw unable to
					// wire Exception
					throw new IllegalArgumentException(
							"Can' wire [" + field.getName() + "], unable to find bean type [" + field.getType() + "] or item with name [" + wireanno.name() + "]");
				}
			}
		});
	}

	public static void wireField(Field field, String name, Object where, Object value) {
		Gson gson = new GsonBuilder().create();
		if (!field.getType().isAssignableFrom(value.getClass()) && value instanceof String
				&& StringUtils.isNotBlank(value.toString())) {
			String strValue = (String) value;
			if (field.getType().isAssignableFrom(Map.class)) {
			    value = gson.fromJson(strValue, Map.class);
			} else if (field.getType().isAssignableFrom(Array.class)) {
				value = gson.fromJson(strValue, Array.class);
			} else if (field.getType().isAssignableFrom(List.class)) {
			     if (Class.class.isAssignableFrom((((ParameterizedType)field.getGenericType()).getActualTypeArguments()[0]).getClass())
			             && (BaseTO.class.isAssignableFrom((Class<?>) (((ParameterizedType)field.getGenericType()).getActualTypeArguments()[0])))) {
			         Class clazz = (Class) ((ParameterizedType)field.getGenericType()).getActualTypeArguments()[0];
			         Type type = new ListParameterizedType(clazz);
			         value = (Object)gson.fromJson(strValue, type);
//Option-2
//					Type type = com.google.gson.internal.$Gson$Types.newParameterizedTypeWithOwner(null, ArrayList.class, clazz);
//					value = (Object)gson.fromJson(strValue, type);
				} else {
				    value = gson.fromJson(strValue, List.class);
				}
			} else if (BaseTO.class.isAssignableFrom(field.getType())) {
			    Class clazz = (Class) (field.getType());
				value = (Object)gson.fromJson(strValue, clazz);
			}
		}
		setField(where, field, value);
	}
	
	/**
	 * Checking equality of method without catering for declaring class
	 *
	 * @param m1 - first method to check
	 * @param m2 - second method to check
	 * @return - true - if methods are equal by name and paramtypes and return types
	 */
	public static boolean methodEquals(Method m1, Method m2) {
		if (m1.getName().equals(m2.getName())) {
			if (!m1.getReturnType().equals(m2.getReturnType())) {
				return false;
			}
			return equalParamTypes(m1.getParameterTypes(), m2.getParameterTypes());
		}
		return false;
	}

	public static boolean equalParamTypes(Class<?>[] params1, Class<?>[] params2) {
		/* Avoid unnecessary cloning */
		if (params1.length == params2.length) {
			for (int i = 0; i < params1.length; i++) {
				if (params1[i] != params2[i]) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	public static Object defaultReturnValue(Method overridden) {
		Class<?> ret = overridden.getReturnType();
		if (ret.isPrimitive()) {
			return PRIMITIVE_OR_WRAPPER_DEFAULT_VALUES.get(ret);
		}
		return null;
	}
	
	private static class ListParameterizedType implements ParameterizedType {

		private Type type;

	    private ListParameterizedType(Type type) {
	        this.type = type;
	    }

	    @Override
	    public Type[] getActualTypeArguments() {
	        return new Type[] {type};
	    }

	    @Override
	    public Type getRawType() {
	        return ArrayList.class;
	    }

	    @Override
	    public Type getOwnerType() {
	        return null;
	    }

	}
}

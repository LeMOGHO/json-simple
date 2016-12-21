/*
 * $Id: JSONObject.java,v 1.1 2006/04/15 14:10:48 platform Exp $
 * Created on 2006-4-10
 */
package json.simple.master;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A JSON object. Key value pairs are unordered. JSONObject supports
 * java.util.Map interface.
 *
 * @author FangYidong<fangyidong@yahoo.com.cn>
 */
public class JSONObject extends HashMap implements Map, JSONAware, JSONStreamAware {

    private static final long serialVersionUID = -503443796854799292L;

    public JSONObject() {
        super();
    }

    /**
     * Allows creation of a JSONObject from a Map. After that, both the
     * generated JSONObject and the Map can be modified independently.
     *
     * @param map
     */
    public JSONObject(Map map) {
        super(map);
    }

    /**
     * Encode a map into JSON text and write it to out. If this map is also a
     * JSONAware or JSONStreamAware, JSONAware or JSONStreamAware specific
     * behaviours will be ignored at this top level.
     *
     * @see org.json.simple.JSONValue#writeJSONString(Object, Writer)
     *
     * @param map
     * @param out
     */
    public static void writeJSONString(Map map, Writer out) throws IOException {
        if (map == null) {
            out.write("null");
            return;
        }

        boolean first = true;
        Iterator iter = map.entrySet().iterator();

        out.write('{');
        while (iter.hasNext()) {
            if (first) {
                first = false;
            } else {
                out.write(',');
            }
            Map.Entry entry = (Map.Entry) iter.next();
            out.write('\"');
            out.write(escape(String.valueOf(entry.getKey())));
            out.write('\"');
            out.write(':');
            JSONValue.writeJSONString(entry.getValue(), out);
        }
        out.write('}');
    }

    public void writeJSONString(Writer out) throws IOException {
        writeJSONString(this, out);
    }

    /**
     * Convert a map to JSON text. The result is a JSON object. If this map is
     * also a JSONAware, JSONAware specific behaviours will be omitted at this
     * top level.
     *
     * @see org.json.simple.JSONValue#toJSONString(Object)
     *
     * @param map
     * @return JSON text, or "null" if map is null.
     */
    public static String toJSONString(Map map) {
        final StringWriter writer = new StringWriter();

        try {
            writeJSONString(map, writer);
            return writer.toString();
        } catch (IOException e) {
            // This should never happen with a StringWriter
            throw new RuntimeException(e);
        }
    }

    public String toJSONString() {
        return toJSONString(this);
    }

    public String toString() {
        return toJSONString();
    }

    public static String toString(String key, Object value) {
        StringBuffer sb = new StringBuffer();
        sb.append('\"');
        if (key == null) {
            sb.append("null");
        } else {
            JSONValue.escape(key, sb);
        }
        sb.append('\"').append(':');

        sb.append(JSONValue.toJSONString(value));

        return sb.toString();
    }

    /**
     * Escape quotes, \, /, \r, \n, \b, \f, \t and other control characters
     * (U+0000 through U+001F). It's the same as JSONValue.escape() only for
     * compatibility here.
     *
     * @see org.json.simple.JSONValue#escape(String)
     *
     * @param s
     * @return
     */
    public static String escape(String s) {
        return JSONValue.escape(s);
    }

    /**
     * Automatically adds the properties of the obj Object to this JSONObject
     * with the property name as key and his value as value. You can use this
     * method in pair with getDirect() to parse/get objects. The method is set
     * to use getters to identify properties to be stored as json so make sure
     * every property in the class you want to store haves his getter.
     *
     * @param obj the object you want to parse in JSON
     * @param classe the class object type of obj
     */
    public void putDirect(Object obj, Class<?> classe) {

        try {
//            System.err.println(obj.getClass().getName()+"\n"+classe.getName());
//            obj = classe.forName(classe.getName()).newInstance();
            obj = classe.cast(obj);
            Field[] fields = classe.getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
            }
            for (Method meth : classe.getMethods()) {   //System.out.println(meth.getName());
                if (meth.getName().toLowerCase().startsWith("get")) {
                    for (Field field : fields) {
                        if (meth.getName().toLowerCase().contains(field.getName().toLowerCase())) {
                            this.put(field.getName(), meth.invoke(obj, null));
                        }
                    }
                }
            }
             for (Field field : fields) {
                field.setAccessible(false);
            }
        } catch (Exception ex) {
            Logger.getLogger(JSONObject.class.getName()).log(Level.SEVERE, null, ex);
//            ex.printStackTrace();
        }
    }

    /**
     * Retrives an object of a particular type from the JSONObject the specified
     * type may have setters corresponding to the field that are going to be
     * retrived from the json. And the class type we want an object from must
     * have a () constructor.
     *
     * @param classe the class type of the object we want to obtain
     * @return
     */
    public Object getDirect(Class<?> classe) {

        Object obj = new Object();
        try {
            obj = classe.forName(classe.getName()).newInstance();
            Field[] fields = classe.getFields();
            for (Field field : fields) {
                field.setAccessible(true);
                for (Method meth : classe.getMethods()) {
                    if (meth.getName().toLowerCase().contains("get" + field.getName().toLowerCase())) {
                        switch (field.getAnnotatedType().getType().getTypeName().toLowerCase()) {
                            case "string":
                                meth.invoke(obj, (String) get(field.getName()));
                                break;
                            case "int":
                                meth.invoke(obj, Integer.parseInt((String) get(field.getName())));
                                break;
                            case "char":
                                meth.invoke(obj, ((String) get(field.getName())).charAt(0));
                                break;
                            case "double":
                                meth.invoke(obj, Double.parseDouble((String) get(field.getName())));
                                break;
                            case "float":
                                meth.invoke(obj, Float.parseFloat((String) get(field.getName())));
                                break;
                            case "long":
                                meth.invoke(obj, Long.parseLong((String) get(field.getName())));
                                break;
                            case "boolean":
                                meth.invoke(obj, Boolean.parseBoolean((String) get(field.getName())));
                                break;
                            case "short":
                                meth.invoke(obj, Short.parseShort((String) get(field.getName())));
                                break;
                        }
                    }
                }
                
                field.setAccessible(false);
            }

        } catch (Exception ex) {
            Logger.getLogger(JSONObject.class.getName()).log(Level.SEVERE, null, ex);
        }

        return obj;
    }
}

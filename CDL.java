public class CDL {

    /**
     * Get the next value. The value can be wrapped in quotes. The value can
     * be empty.
     * @param x A JSONTokener of the source text.
     * @return The value string, or null if empty.
     * @throws JSONException if the quoted string is badly formed.
     */
    private static String getValue(JSONTokener x) throws JSONException {
        char c;
        do {
            c = x.next();
        } while (c <= ' ' && c != 0);
        switch (c) {
        case 0:
            return null;
        case '"':
        case '\'':
            return x.nextString(c);
        case ',':
            x.back();
            return "";
        default:
            x.back();
            return x.nextTo(',');
        }
    }

    /**
     * Produce a JSONArray of strings from a row of comma delimited values.
     * @param x A JSONTokener of the source text.
     * @return A JSONArray of strings.
     * @throws JSONException
     */
    public static JSONArray rowToJSONArray(JSONTokener x) throws JSONException {
        JSONArray ja = new JSONArray();
        for (;;) {
            String value = getValue(x);
            if (value == null) {
                return null;
            }
            ja.put(value);
            for (;;) {
                char c = x.next();
                if (c == ',') {
                    break;
                }
                if (c != ' ') {
                    if (c == '\n' || c == '\r' || c == 0) {
                        return ja;
                    }
                    throw x.syntaxError("Bad character '" + c + "' (" +
                            (int)c + ").");
                }
            }
        }
    }

    /**
     * Produce a JSONObject from a row of comma delimited text, using a
     * parallel JSONArray of strings to provides the names of the elements.
     * @param names A JSONArray of names. This is commonly obtained from the
     *  first row of a comma delimited text file using the rowToJSONArray
     *  method.
     * @param x A JSONTokener of the source text.
     * @return A JSONObject combining the names and values.
     * @throws JSONException
     */
    public static JSONObject rowToJSONObject(JSONArray names, JSONTokener x)
            throws JSONException {
        JSONArray ja = rowToJSONArray(x);
        return ja != null ? ja.toJSONObject(names) :  null;
    }

    /**
     * Produce a JSONArray of JSONObjects from a comma delimited text string,
     * using the first row as a source of names.
     * @param string The comma delimited text.
     * @return A JSONArray of JSONObjects.
     * @throws JSONException
     */
    public static JSONArray toJSONArray(String string) throws JSONException {
        return toJSONArray(new JSONTokener(string));
    }

    /**
     * Produce a JSONArray of JSONObjects from a comma delimited text string,
     * using the first row as a source of names.
     * @param x The JSONTokener containing the comma delimited text.
     * @return A JSONArray of JSONObjects.
     * @throws JSONException
     */
    public static JSONArray toJSONArray(JSONTokener x) throws JSONException {
        return toJSONArray(rowToJSONArray(x), x);
    }

    /**
     * Produce a JSONArray of JSONObjects from a comma delimited text string
     * using a supplied JSONArray as the source of element names.
     * @param names A JSONArray of strings.
     * @param string The comma delimited text.
     * @return A JSONArray of JSONObjects.
     * @throws JSONException
     */
    public static JSONArray toJSONArray(JSONArray names, String string)
            throws JSONException {
        return toJSONArray(names, new JSONTokener(string));
    }

    /**
     * Produce a JSONArray of JSONObjects from a comma delimited text string
     * using a supplied JSONArray as the source of element names.
     * @param names A JSONArray of strings.
     * @param x A JSONTokener of the source text.
     * @return A JSONArray of JSONObjects.
     * @throws JSONException
     */
    public static JSONArray toJSONArray(JSONArray names, JSONTokener x)
            throws JSONException {
        if (names == null || names.length() == 0) {
            return null;
        }
        JSONArray ja = new JSONArray();
        for (;;) {
            JSONObject jo = rowToJSONObject(names, x);
            if (jo == null) {
                break;
            }
            ja.put(jo);
        }
        if (ja.length() == 0) {
            return null;
        }
        return ja;
    }


    /**
     * Produce a comma delimited text row from a JSONArray. Values containing
     * the comma character will be quoted.
     * @param ja A JSONArray of strings.
     * @return A string ending in NEWLINE.
     */
    public static String rowToString(JSONArray ja) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < ja.length(); i += 1) {
            if (i > 0) {
                sb.append(',');
            }
            Object o = ja.opt(i);
            if (o != null) {
                String s = o.toString();
                if (s.indexOf(',') >= 0) {
                    if (s.indexOf('"') >= 0) {
                        sb.append('\'');
                        sb.append(s);
                        sb.append('\'');
                    } else {
                        sb.append('"');
                        sb.append(s);
                        sb.append('"');
                    }
                } else {
                    sb.append(s);
                }
            }
        }
        sb.append('\n');
        return sb.toString();

    }

    /**
     * Produce a comma delimited text from a JSONArray of JSONObjects. The
     * first row will be a list of names obtained by inspecting the first
     * JSONObject.
     * @param ja A JSONArray of JSONObjects.
     * @return A comma delimited text.
     * @throws JSONException
     */
    public static String toString(JSONArray ja) throws JSONException {
        JSONObject jo = ja.optJSONObject(0);
        if (jo != null) {
            JSONArray names = jo.names();
            if (names != null) {
                return rowToString(names) + toString(names, ja);
            }
        }
        return null;
    }

    /**
     * Produce a comma delimited text from a JSONArray of JSONObjects using
     * a provided list of names. The list of names is not included in the
     * output.
     * @param names A JSONArray of strings.
     * @param ja A JSONArray of JSONObjects.
     * @return A comma delimited text.
     * @throws JSONException
     */
    public static String toString(JSONArray names, JSONArray ja)
            throws JSONException {
        if (names == null || names.length() == 0) {
            return null;
        }
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < ja.length(); i += 1) {
            JSONObject jo = ja.optJSONObject(i);
            if (jo != null) {
                sb.append(rowToString(jo.toJSONArray(names)));
            }
        }
        return sb.toString();
    }
}

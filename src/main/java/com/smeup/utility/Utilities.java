package com.smeup.utility;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utilities
{
    public static String encode(String aUnencoded, String aCharset)
    {
        byte[] aBytes = null;
        try
        {
            aBytes = aUnencoded.getBytes(aCharset);
        }
        catch(UnsupportedEncodingException ignored)
        {
        }
        return encode(aBytes, aCharset);
    }

    /**
     * Returns the encoded form of the given unencoded string.
     * 
     * @param unencoded
     *            the string to encode
     * @return the encoded form of the unencoded string
     */
    public static String encode(byte[] aBytes, String aCharset)
    {
        ByteArrayOutputStream vOut = new ByteArrayOutputStream(
                    (int) (aBytes.length * 1.37));
        Base64Encoder vEncodedOut = new Base64Encoder(vOut);
        try
        {
            vEncodedOut.write(aBytes);
            vEncodedOut.close();
            return vOut.toString(aCharset);
        }
        catch(IOException ignored)
        {
            return null;
        }
    }

//    public static String resolveVariable(String aString,
//                SPIWsCConnectorInput aInput)
//    {
//        String vRet = aString;
//
//        if(aInput != null && aString!=null)
//        {
//            Iterator<String> vIterKey = aInput.getKeys().iterator();
//            while(vIterKey.hasNext())
//            {
//                String vKey = (String) vIterKey.next();
//                String vVariableNameCandidate = "[A38." + vKey + "]";
//                if(vRet.indexOf(vVariableNameCandidate) > -1)
//                {
//                    String vVariableValue = aInput.getData(vKey);
//                    if(vVariableValue == null)
//                        vVariableValue = "";
//                    vRet = vRet.replace(vVariableNameCandidate, vVariableValue);
//                }
//            }
//        }
//
//        return vRet;
//    }

    private static String encodeURIComponent(String c) throws UnsupportedEncodingException
    {
        return URLEncoder.encode(c, "UTF-8");
    }

    public static String encodeURL(String aUrl) throws UnsupportedEncodingException
    {
        Pattern p = Pattern.compile("\\b(\\w[^=?]*)=([^&]*)");
        Matcher m = p
                    .matcher(aUrl);
        StringBuffer sb = new StringBuffer();
        while(m.find())
        {
            String key = m.group(1);
            String value = m.group(2);
            m.appendReplacement(sb,
                                encodeURIComponent(key) + "="
                                            + encodeURIComponent(value));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    public static void main(String[] args) throws java.lang.Exception
    {
        System.out.println(encodeURL("https://webuptest.smeup.com/jenkins/job/WebUPDevIT/api/json?tree=builds[id,status,actions[failCount,skipCount,totalCount]{6}]{0,8}"));
    }
}

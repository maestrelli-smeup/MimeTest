package com.smeup.test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.BodyPart;
import javax.mail.Header;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

public class MimeMessageTest {
	
//	private static final String _PATH = "/home/skioda86/git/MimeTest/src/main/resources/TEST.txt";
//	private static final String _PATH = "/home/skioda86/git/MimeTest/src/main/resources/TEST2.txt";
//	private static final String _PATH = "/home/skioda86/git/MimeTest/src/main/resources/TEST3.txt";
	private static final String _PATH = "/home/olimaest/Lavoro/Dati prova/Agricar/RESP.txt";
	private static final String _MULTIPART_FORM_DATA = "multipart/form-data";

    public static void main(String[] args) {
        try {
        	String[] vRes= processResponse(new FileInputStream(new File(_PATH)));
        	if(vRes!=null)
        	{
        		for (int i = 0; i < vRes.length; i++) {
					String vPartToString = vRes[i];
					System.out.println(vPartToString);
				}
        	}
        }
        catch(FileNotFoundException ex) {
            // TODO Auto-generated catch block
            ex.printStackTrace();
        }
        catch(IOException ex) {
            // TODO Auto-generated catch block
            ex.printStackTrace();
        }
    }
    
    public static String[] processResponse(InputStream aStream)
    {
    	String[] vRet= null;
        try {
        	MimeMultipart vMMP= new MimeMultipart(
        			new ByteArrayDataSource(aStream, _MULTIPART_FORM_DATA));
        	
            int vPart= vMMP.getCount();            
//            log("n. part= " + vPart);
            if(vPart > 1) {
            	vRet= processMultiPart(vMMP, vPart);
            }else {
            	new MessagingException();
            }
        }
        catch(FileNotFoundException ex) {
            // TODO Auto-generated catch block
            ex.printStackTrace();
        }
        catch(MessagingException ex) {
            // TODO Auto-generated catch block
            ex.printStackTrace();
        }
        catch(IOException ex) {
            // TODO Auto-generated catch block
            ex.printStackTrace();
        }
    	return vRet;
    }
    
    private static String[] processMultiPart(MimeMultipart aMMP, int aPart) throws MessagingException, IOException {
    	String[] vRet= new String[aPart];
     	for (int i = 0; i < aPart; i++) {
    		
			BodyPart bodyPart = aMMP.getBodyPart(i);
			
			Enumeration<Header> vHeaders = bodyPart.getAllHeaders();
			ArrayList<Header> vHeaderList = new ArrayList<>();
			
			while(vHeaders.hasMoreElements()) {
	        	vHeaderList.add((Header) vHeaders.nextElement());
	        }
			
			Pattern pattern = Pattern.compile("(^|[^=\\\"])+text/(.*)?;");
			Matcher patternMatcher = pattern.matcher(bodyPart.getContentType());
			
			if(patternMatcher.find()){
				log("\nPart: " + i + "\nContentType: " + bodyPart.getContentType());
				vRet[i]= processTextData((String) bodyPart.getContent(), vHeaderList); 
			}
			
			pattern = Pattern.compile("(^|[^=\"])+application/(.*);");
			patternMatcher = pattern.matcher(bodyPart.getContentType());
			
			if(patternMatcher.find()) {  
				log("\nPart: " + i + "\nContentType: " + bodyPart.getContentType());
				vRet[i]= processBinaryData(bodyPart.getInputStream(), vHeaderList);
			}
    	}
     	return vRet;
    }
    
    private static String processTextData(String aText, ArrayList<Header> aHeaderList) {	
    	String vRet= null;
    	Document result = createDocument(aText,aHeaderList);
    	vRet= result.asXML().toString();
        log(vRet);
        return vRet;
    }
    
    private static String processBinaryData(InputStream aStream, ArrayList<Header> aHeaderList) {
    	String vRet= null;
//    	createFile(aStream);
    	 
    	String charset = null;
    	
    	for (Header header : aHeaderList) {
    		
			if(header.getName().equals("Content-Type")) {
				String[] vContentTypeParams = header.getValue().split(";");
				
				for(int i=0; i<vContentTypeParams.length; i++) {
					if(vContentTypeParams[i].trim().matches("charset=")) {
						charset = vContentTypeParams[i].substring(vContentTypeParams[i].trim().indexOf("="));
					}
				}
			}
		}
    	
    	if(charset == null) {
    		charset = "UTF-8";	
    	}
    	
    	String binaryEncoded = createBase64(aStream,charset);
    	Document result = createDocument(binaryEncoded,aHeaderList);
    	
    	vRet= result.asXML().toString();
    	log(vRet);
    	return vRet;
    }
    
    
    public void createFile(InputStream aStream) {
    	FileOutputStream vStream= null;
        try
        {
            vStream= new FileOutputStream(new File("/home/maeoli/git/MimeTest/src/main/resources/prova"+".tgz"));
            
            byte[] buffer    =   new byte[10*1024];

            for (int length; (length = aStream.read(buffer)) != -1; ){
                vStream.write(buffer, 0, length);
            }

            aStream.close();
            vStream.close();
            
        }
        catch(FileNotFoundException ex)
        {
            // TODO Auto-generated catch block
            ex.printStackTrace();
        }
        catch(IOException ex)
        {
            // TODO Auto-generated catch block
            ex.printStackTrace();
        }
    }
    
    
    private static void log(String s) {
    	if(s != null) {
    		System.out.println(s);
    	}
    }
    
    
    private static String createBase64(InputStream aStream, String aCharset) {
    	ByteArrayOutputStream responseOutStream = new ByteArrayOutputStream();
    	
    	byte[] buffer = new byte[4096];
		int len;
		
		try {
			
			while((len = aStream.read(buffer)) > -1) {
				responseOutStream.write(buffer,0,len);
			}
			responseOutStream.flush();
			responseOutStream.close();
		}catch(IOException e) {
			
		}
    	
    	return com.smeup.utility.Base64Encoder.encode(responseOutStream.toByteArray(), aCharset); 
    	
    }
    
        
    private static Document createDocument(String aText, ArrayList<Header> aHeaderList) {
    	
    	Document doc = DocumentHelper.createDocument();
    	
    	Element root = doc.addElement("ROOT");
    	
    	Element element = root.addElement("ELEMENT");
    	for (Header header : aHeaderList) {
    		element.addAttribute(header.getName(),header.getValue());
		}
    	element.addCDATA(aText);
    	return doc;
    }

}

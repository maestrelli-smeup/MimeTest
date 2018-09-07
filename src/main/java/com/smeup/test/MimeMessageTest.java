package com.smeup.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

public class MimeMessageTest
{

    public static void main(String[] args)
    {
        MimeMessage vMsg= new MimeMessage((Session)null);
        try
        {
            MimeMultipart vMMP= new MimeMultipart(new ByteArrayDataSource(new FileInputStream(new File("C:\\temp\\RESP.txt")), "multipart/form-data"));
//            vMsg= new MimeMessage(null, new FileInputStream(new File("C:\\temp\\RESP.txt")));
            
            int vPart= vMMP.getCount();
            System.out.println("n. part= "+vPart);
            
            for (int i = 0; i < vPart; i++) {
                BodyPart bodyPart = vMMP.getBodyPart(i);
                String vCT= bodyPart.getContentType();
                System.out.println(vCT);
                if (bodyPart.isMimeType("text/plain")) {
                    System.out.println("text/plain " + bodyPart.getContentType());
                    processTextData((String)bodyPart.getContent());
                } else if (bodyPart.isMimeType("application/octet-stream")) {
                    System.out.println("application/octet-stream " + bodyPart.getContentType());
                    processBinaryData(bodyPart.getInputStream());
                } else {
                    System.out.println("default " + bodyPart.getContentType());
                }
            }
        }
        catch(FileNotFoundException ex)
        {
            // TODO Auto-generated catch block
            ex.printStackTrace();
        }
        catch(MessagingException ex)
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
    
    private static void processTextData(String aText)
    {
        System.out.println(aText);
    }
    
    private static void processBinaryData(InputStream aStream)
    {
        FileOutputStream vStream= null;
        try
        {
            vStream= new FileOutputStream(new File("c:\\temp\\"+ System.currentTimeMillis()+".tar.gz"));

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

}

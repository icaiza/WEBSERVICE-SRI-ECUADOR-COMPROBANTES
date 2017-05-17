package wssri;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;

import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;


import org.w3c.dom.NodeList;

public class Wssri {
    private static final String WSDL = "https://cel.sri.gob.ec/comprobantes-electronicos-ws/AutorizacionComprobantes?wsdl";
    public static void main(String[] args) {
        
        new NewJFrame().setVisible(true);
        
        
        String numero = args.length == 0 ? "0105201701099000419600120010660000081920000819211" : args[0].trim();
        String params = "<x:Envelope xmlns:x=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ec=\"http://ec.gob.sri.ws.autorizacion\">" +
            "    <x:Header/>" +
            "    <x:Body>" +
            "        <ec:autorizacionComprobante>" +
            "            <claveAccesoComprobante>"+numero+"</claveAccesoComprobante>" +
            "        </ec:autorizacionComprobante>" +
            "    </x:Body>" +
            "</x:Envelope>";
        
        HttpURLConnection conn = null;
        URL uriLogin;
        try {
            uriLogin = new URL(WSDL);
            conn = (HttpURLConnection) uriLogin.openConnection();
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setReadTimeout(15000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("POST");       
        //Sender POST
            DataOutputStream wr = new DataOutputStream ( conn.getOutputStream () );
            wr.writeBytes (params);
            wr.flush ();
            wr.close ();
            
            if (conn.getResponseCode() == conn.HTTP_OK) {
                StringBuilder responseStrBuilder = new StringBuilder();
                InputStream in = new BufferedInputStream(conn.getInputStream());
                BufferedReader streamReader = new BufferedReader(new InputStreamReader(in, "UTF-8"));

                String inputStr;
                while ((inputStr = streamReader.readLine()) != null) {
                    responseStrBuilder.append(inputStr);
                }
                System.out.println(
                    "Respuesta: " + responseStrBuilder.toString()
                );
                
                String xml = responseStrBuilder.toString();
                
                
                

                
                MessageFactory factory;
                try {
                    factory = MessageFactory.newInstance();
                    SOAPMessage message = factory.createMessage( new MimeHeaders(), new ByteArrayInputStream(xml.getBytes(Charset.forName("UTF-8"))));
                    SOAPBody body = message.getSOAPBody();
                    //NodeList returnList = body.getElementsByTagName("web:RES");
                    NodeList list = body.getElementsByTagName("autorizaciones");
                    
                    
                    Writer out = new BufferedWriter(new OutputStreamWriter( new FileOutputStream("comprobante_"+numero+".xml"), "UTF-8"));
                    try {
                        out.write( body.getElementsByTagName("autorizacion").item(0).getTextContent().replaceAll("\\p{Cntrl}", "\n").replaceAll("\t\r", "")  );
                    } finally {
                        out.close();
                    }
                        
                } catch (SOAPException ex) {
                    Logger.getLogger(Wssri.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                




    
                
                
                
                
                
                
                
                
            }else{
                System.err.println("HTTP: " + conn.getResponseMessage());
            }

        } catch (IOException ex) {
            Logger.getLogger(Wssri.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
}

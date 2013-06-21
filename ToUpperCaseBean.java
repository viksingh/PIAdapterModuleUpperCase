//Latest project
package com.pi.usermodule;

import javax.ejb.CreateException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import com.sap.aii.af.lib.mp.module.*;
import com.sap.engine.interfaces.messaging.api.*;
import com.sap.engine.interfaces.messaging.api.auditlog.*;
import com.sap.engine.interfaces.messaging.api.exception.InvalidParamException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;

public class ToUpperCaseBean implements SessionBean, Module {
  public static final String VERSION_ID = "$Id://tc/aii/30_REL/src/_adapters/_module/java/user/module/XMLElementEncrypt.java#1 $";
	static final long serialVersionUID = 7435850550539048631L;
	private SessionContext myContext;
	public void ejbRemove() {
	}
	public void ejbActivate() {
	}
	public void ejbPassivate() {
	}
	public void setSessionContext(SessionContext context) {
		myContext = context;
	}
	public void ejbCreate() throws CreateException {
	}

	public ModuleData process(ModuleContext moduleContext, ModuleData inputModuleData) throws ModuleException {
		AuditAccess audit = null;
		// Create the location always new to avoid serialization/transient of location
		try {
		} catch (Exception t) {
			t.printStackTrace();
			ModuleException me = new ModuleException("Unable to create trace location", t);
			throw me;
		}
		Object obj = null;
		Message msg = null;
		MessageKey key = null;
		try {
			obj = inputModuleData.getPrincipalData();
			msg = (Message) obj;
			if (msg.getMessageDirection().equals(MessageDirection.OUTBOUND))
				key = new MessageKey(msg.getMessageId(), MessageDirection.OUTBOUND);
			else
				key = new MessageKey(msg.getMessageId(), MessageDirection.INBOUND);
			audit = PublicAPIAccessFactory.getPublicAPIAccess().getAuditAccess();
			audit.addAuditLogEntry(key, AuditLogStatus.SUCCESS, " toUpperCase: Module called");

			String property = null;
			property = (String) moduleContext.getContextData("property");

			audit.addAuditLogEntry(key, AuditLogStatus.SUCCESS, " toUpperCase: Property"+property);
			
			String propertyNamespace = null;
			propertyNamespace = (String) moduleContext.getContextData("propertyNamespace");
			audit.addAuditLogEntry(key, AuditLogStatus.SUCCESS, " toUpperCase: Property Namespace"+propertyNamespace);

			

			MessagePropertyKey propKey = new MessagePropertyKey(property, propertyNamespace);
			String propValue = null;
			propValue = msg.getMessageProperty(propKey);
			audit.addAuditLogEntry(key, AuditLogStatus.SUCCESS, " toUpperCase: Property Value"+propValue);			

			String pattern = null;
			pattern = (String) moduleContext.getContextData("pattern");

			if(propValue.toUpperCase().contains(pattern.toUpperCase())){
				audit.addAuditLogEntry(key, AuditLogStatus.SUCCESS, "toUpperCase : Pattern matched ");				 

				//Boiler plate to get data
				obj = inputModuleData.getPrincipalData();
				msg = (Message) obj;
				XMLPayload xmlpayload = msg.getDocument();
				InputStream inStream = (InputStream)xmlpayload.getInputStream();
				//Get Dom
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				DocumentBuilder Parser = factory.newDocumentBuilder();
				Document source = Parser.parse(inStream);
				Source docSource = new DOMSource(source);

				//Do conversion
				NodeList entries = source.getElementsByTagName("*");
				audit.addAuditLogEntry(key, AuditLogStatus.SUCCESS, "toUpperCase: Converting to uppercase");				 
				for (int i=0; i<entries.getLength(); i++) {
					try{					
						if ( entries.item(i).getFirstChild().getNodeValue() != null && entries.item(i).getFirstChild().getNodeValue() != ""){					
							entries.item(i).getFirstChild().setNodeValue(entries.item(i).getFirstChild().getNodeValue().toUpperCase());
						}
					}catch (Exception e){
					}
				}
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				Result result = new StreamResult(out);
				TransformerFactory factory1 = TransformerFactory.newInstance();
				Transformer transformer = factory1.newTransformer();
				transformer.transform(docSource, result);
				byte[] docContent = out.toByteArray();				

				if(docContent != null) {
					try {
						xmlpayload.setContent(docContent);
					} catch (InvalidParamException e) {
						e.printStackTrace();
					}
					inputModuleData.setPrincipalData(msg);
					audit.addAuditLogEntry(key, AuditLogStatus.SUCCESS, " toUpperCase: processing completed");					
				}				
			}
		}
		catch (Exception e) {
			ModuleException me = new ModuleException(e);
//			throw me;
		}
		return inputModuleData;
	}

	private String getTextValue(Element ele, String tag) {
		String textVal = null;
		NodeList nl = ele.getElementsByTagName(tag);
		if (nl != null && nl.getLength() > 0) {
			Element el = (Element) nl.item(0);

			try {	
				textVal = el.getFirstChild().getNodeValue();
			} catch (Exception e) {}		
		}

		return textVal;
	}

	private String setTextValue(Element ele, String tag,String val) {
		String textVal = null;
		NodeList nl = ele.getElementsByTagName(tag);
		if (nl != null && nl.getLength() > 0) {
			Element el = (Element) nl.item(0);

			try {	
				el.getFirstChild().setNodeValue(val);
			} catch (Exception e) {}		
		}

		return textVal;
	}



}

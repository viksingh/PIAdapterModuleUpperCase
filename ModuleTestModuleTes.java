package com.testing.ModuleTest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Set;

import com.nestle.idocpreprocess.IdocPreProcessBean;
import com.sap.aii.af.lib.mp.module.ModuleData;
import com.sap.aii.af.lib.mp.module.ModuleException;
import com.sap.engine.interfaces.messaging.api.Message;
import com.sap.engine.interfaces.messaging.api.MessageClass;
import com.sap.engine.interfaces.messaging.api.MessageDirection;
import com.sap.engine.interfaces.messaging.api.MessagePropertyKey;
import com.sap.engine.interfaces.messaging.api.XMLPayload;
import com.sap.engine.interfaces.messaging.api.exception.InvalidParamException;
import com.sap.engine.interfaces.messaging.api.exception.PayloadFormatException;

public class ModuleTest {

	public static void main(String[] args) throws IOException, ModuleException,
			InvalidParamException, PayloadFormatException {

		String encoding = "UTF-8";
		Hashtable<String, String> contextData = new Hashtable<String, String>();

		IdocPreProcessBean bean = new IdocPreProcessBean();

		// Set PI message details
		Message msg = new MessageImpl("PartyA", "ServiceA",
				"Interface1", // Sender details
				"PartyB",
				"ServiceB", // Receiver details
				MessageClass.APPLICATION_MESSAGE, MessageDirection.INBOUND,
				"005056B777881ED491EE4E73A55ACD5D", // Message ID
				"FTP", // Transport Protocol
				"");

		// Get payload from file
		InputStream inpStr = new FileInputStream(new File(
				"C:\\temp\\RFCBean\\KR101.txt"));
//				"C:\\temp\\RFCBean\\DESADV.0006"));
		ByteArrayOutputStream baos = InputStreamToBAOS(inpStr);

		// Set XML payload as the main document of the message
		XMLPayload xml = new XMLPayloadImpl();
		xml.setContent(baos.toByteArray(), encoding);
		msg.setDocument(xml);

		// Set message as the module data
		ModuleData data = new ModuleData();
		data.setPrincipalData(msg);

		// Set Dynamic Configuration
		addDynCfg(msg, "http://sap.com/xi/XI/System/File", "FileName",
				"FileA.txt");

		// Set optional module parameters
		contextData.put("PARAM1", "value1");
		ModuleContextImpl context = new ModuleContextImpl("abcdef1234567890",
				contextData);

		// Display pre dynamic configuration in console
		Set<MessagePropertyKey> mpkSet = msg.getMessagePropertyKeys();
		if (mpkSet.size() != 0) {
			System.out
					.println("==============================================");
			System.out
					.println("Dynamic Configuration before module processing");
			System.out
					.println("==============================================");
			for (MessagePropertyKey mpk : mpkSet) {
				System.out.println(mpk.getPropertyNamespace() + ";"
						+ mpk.getPropertyName() + ";"
						+ msg.getMessageProperty(mpk));
			}
		}

		// --------------------------------------------
		// Execute Module processing
		// --------------------------------------------
		data = bean.process(context, data);

		// Output to file
		Message output = (Message) data.getPrincipalData();
		ByteArrayInputStream bais = (ByteArrayInputStream) output.getDocument()
				.getInputStream();
		baos = InputStreamToBAOS(bais);
		FileOutputStream fileOutStr = new FileOutputStream(new File(
				"C:\\temp\\RFCBean\\output.txt"));
		baos.writeTo(fileOutStr);
		fileOutStr.close();

		mpkSet = output.getMessagePropertyKeys();
		if (mpkSet.size() != 0) {
			System.out
					.println("==============================================");
			System.out.println("Dynamic Configuration after module processing");
			System.out
					.println("==============================================");
			for (MessagePropertyKey mpk : mpkSet) {
				System.out.println(mpk.getPropertyNamespace() + ";"
						+ mpk.getPropertyName() + ";"
						+ output.getMessageProperty(mpk));
			}
		}

	}

	private static ByteArrayOutputStream InputStreamToBAOS(InputStream inStream)
			throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int read = 0;
		while ((read = inStream.read(buffer, 0, buffer.length)) != -1) {
			baos.write(buffer, 0, read);
		}
		baos.flush();
		return baos;
	}

	private static void addDynCfg(Message msg, String namespace,
			String attribute, String value) throws InvalidParamException {
		MessagePropertyKey key = new MessagePropertyKey(attribute, namespace);
		msg.setMessageProperty(key, value);
	}

}

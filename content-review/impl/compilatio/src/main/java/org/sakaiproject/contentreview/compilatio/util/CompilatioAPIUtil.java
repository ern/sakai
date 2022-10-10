/**
 * Copyright (c) 2016 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.contentreview.compilatio.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import jakarta.xml.soap.MessageFactory;
import jakarta.xml.soap.SOAPBody;
import jakarta.xml.soap.SOAPConnection;
import jakarta.xml.soap.SOAPConnectionFactory;
import jakarta.xml.soap.SOAPElement;
import jakarta.xml.soap.SOAPEnvelope;
import jakarta.xml.soap.SOAPException;
import jakarta.xml.soap.SOAPMessage;
import jakarta.xml.soap.SOAPPart;

import org.sakaiproject.contentreview.exception.SubmissionException;
import org.sakaiproject.contentreview.exception.TransientSubmissionException;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import lombok.extern.slf4j.Slf4j;

/**
 * This is a utility class for wrapping the SOAP calls to the Compilatio Service
 *
 */
@Slf4j
public class CompilatioAPIUtil {


	public static Document callCompilatioReturnDocument(String apiURL, Map<String, String> parameters, String secretKey,
			final int timeout) throws TransientSubmissionException, SubmissionException {

		SOAPConnectionFactory soapConnectionFactory;
		Document xmlDocument = null;
		try {
			soapConnectionFactory = SOAPConnectionFactory.newInstance();

			SOAPConnection soapConnection = soapConnectionFactory.createConnection();

			MessageFactory messageFactory = MessageFactory.newInstance();
			SOAPMessage soapMessage = messageFactory.createMessage();
			SOAPPart soapPart = soapMessage.getSOAPPart();
			SOAPEnvelope envelope = soapPart.getEnvelope();
			SOAPBody soapBody = envelope.getBody();
			SOAPElement soapBodyAction = soapBody.addChildElement(parameters.get("action"));
			parameters.remove("action");
			// api key
			SOAPElement soapBodyKey = soapBodyAction.addChildElement("key");
			soapBodyKey.addTextNode(secretKey);

			Set<Entry<String, String>> ets = parameters.entrySet();
			Iterator<Entry<String, String>> it = ets.iterator();
			while (it.hasNext()) {
				Entry<String, String> param = it.next();
				SOAPElement soapBodyElement = soapBodyAction.addChildElement(param.getKey());
				soapBodyElement.addTextNode(param.getValue());
			}
			
			URL endpoint = new URL(null, apiURL, new URLStreamHandler() {
				@Override
				protected URLConnection openConnection(URL url) throws IOException {
					URL target = new URL(url.toString());
					URLConnection connection = target.openConnection();
					// Connection settings
					connection.setConnectTimeout(timeout);
					connection.setReadTimeout(timeout);
					return(connection);
				}
			});
			
			SOAPMessage soapResponse = soapConnection.call(soapMessage, endpoint);

			// loading the XML document
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			soapResponse.writeTo(out);
			DocumentBuilderFactory builderfactory = DocumentBuilderFactory.newInstance();
			builderfactory.setNamespaceAware(true);

			DocumentBuilder builder = builderfactory.newDocumentBuilder();
			xmlDocument = builder.parse(new InputSource(new StringReader(out.toString())));
			soapConnection.close();

		} catch (UnsupportedOperationException | SOAPException | IOException | ParserConfigurationException | SAXException e) {
			log.error(e.getLocalizedMessage(), e);
		}
		return xmlDocument;

	}
	
	public static Map<String, String> packMap(String... vargs) {
		Map<String, String> map = new HashMap<>();
		if (vargs.length % 2 != 0) {
			throw new IllegalArgumentException("You need to supply an even number of vargs for the key-val pairs.");
		}
		for (int i = 0; i < vargs.length; i += 2) {
			map.put(vargs[i], vargs[i + 1]);
		}
		return map;
	}

}

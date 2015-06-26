/**
 * This software was developed at the National Institute of Standards and Technology by employees of
 * the Federal Government in the course of their official duties. Pursuant to title 17 Section 105
 * of the United States Code this software is not subject to copyright protection and is in the
 * public domain. This is an experimental system. NIST assumes no responsibility whatsoever for its
 * use by other parties, and makes no guarantees, expressed or implied, about its quality,
 * reliability, or any other characteristic. We would appreciate acknowledgement if the software is
 * used. This software can be redistributed and/or modified freely provided that any derivative
 * works bear some notice that they are derived from it, and any modified versions bear some notice
 * that they have been modified.
 */

package gov.nist.hit.core.hl7v2.service.soap.unit;

import gov.nist.hit.core.domain.MessageModel;
import gov.nist.hit.core.service.exception.MessageParserException;
import gov.nist.hit.iz.service.XmlMessageParserImpl;
import gov.nist.hit.iz.service.soap.SoapMessageParser;
import gov.nist.hit.iz.service.soap.SoapMessageParserImpl;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

public class SoapMessageParserImplTest {

  SoapMessageParser parser = new SoapMessageParserImpl(new XmlMessageParserImpl());

  @Test
  public void testParse() throws MessageParserException, IOException {
    MessageModel model = parser.parse(getSoapContent());
  }

  public String getSoapContent() throws IOException {
    return IOUtils.toString(SoapMessageParserImplTest.class
        .getResourceAsStream("/soapMessages/2.xml"));
  }

  // public String getSoapContent2() throws IOException {
  // return IOUtils.toString(SoapMessageParserImplTest.class
  // .getResourceAsStream("/soapMessages/2.xml"));
  // }
}

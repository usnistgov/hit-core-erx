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
package gov.nist.hit.core.hl7v2.service.message;

import gov.nist.hit.core.domain.MessageElement;
import gov.nist.hit.core.domain.MessageModel;
import gov.nist.hit.core.hl7v2.domain.message.MessageElementData;
import gov.nist.hit.core.hl7v2.domain.util.Util;
import gov.nist.hit.core.service.MessageParser;
import gov.nist.hit.core.service.exception.MessageParserException;
import hl7.v2.instance.ComplexComponent;
import hl7.v2.instance.ComplexField;
import hl7.v2.instance.Component;
import hl7.v2.instance.Field;
import hl7.v2.instance.Group;
import hl7.v2.instance.Location;
import hl7.v2.instance.Message;
import hl7.v2.instance.SegOrGroup;
import hl7.v2.instance.Segment;
import hl7.v2.instance.SimpleComponent;
import hl7.v2.instance.SimpleField;
import hl7.v2.profile.Profile;
import hl7.v2.profile.Range;
import hl7.v2.profile.Req;
import ncpdp.script.profile.XMLDeserializer;

import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;

import scala.collection.Iterator;
import scala.collection.immutable.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Harold Affo
 * 
 */
@Service("er7MessageParser")
public class Er7MessageParser implements MessageParser {
  static final Logger logger = LoggerFactory.getLogger(Er7MessageParser.class);


  private final static String SEGMENT = "SEGMENT";
  private final static String FIELD = "FIELD";
  private final static String COMPONENT = "COMPONENT";
  private final static String SUB_COMPONENT = "SUB_COMPONENT";
  private final static String NODE_SEGMENT = "segment";
  private final static String NODE_FIELD = "field";
  private final static String NODE_COMPONENT = "component";
  private final static String NODE_SUB_COMPONENT = "subcomponent";

  /**
	 * 
	 */
  @Override
  public MessageModel parse(String er7Message, String... options) throws MessageParserException {
    try {
      logger.debug("p-0");
      System.out.println("0-sys");
      String profileXml = options[0];
      if (options.length == 1) {
        throw new MessageParserException("No Conformance Profile Provided to Parse the Message");
      }
      String conformanceProfileId = options[1];
      if (!"".equals(er7Message) && er7Message != null && !"".equals(conformanceProfileId)) {
        logger.debug("p-1");
        InputStream profileStream = IOUtils.toInputStream(profileXml);
        logger.debug("p-2");
        Profile profile = XMLDeserializer.deserialize(profileStream).get();
        logger.debug("p-3");
        JParser p = new JParser();
        logger.debug("p-4");
        Message message = p.jparse(er7Message, profile.messages().apply(conformanceProfileId));
        logger.debug("p-5");
        return toModel(message);
      }
    } catch (RuntimeException e) {
      throw new MessageParserException(e);
    } catch (Exception e) {
      throw new MessageParserException(e);
    }
    return new MessageModel();
  }

  /**
   * 
   * @param message
   * @return
   */
  private MessageModel toModel(Message message) {
    MessageElement root = new MessageElement();
    List<SegOrGroup> children = message.children();
    if (children != null && !children.isEmpty()) {
      scala.collection.Iterator<SegOrGroup> it = children.iterator();
      while (it.hasNext()) {
        process(it.next(), "", root);
      }
    }
    return new MessageModel(root);
  }

  /**
   * 
   * @param c
   * @param parent
   */
  private void process(Component c, MessageElement parent) {
    Location loc = c.location();
    Req req = c.req();
    MessageElementData data =
        new MessageElementData(loc.uidPath(), loc.desc(), req.usage().toString(), -1, null,
            loc.line(), loc.column(), -1, c.position(), c.instance(), null, COMPONENT);
    MessageElement el = new MessageElement(NODE_COMPONENT, data, parent);
    if (c instanceof SimpleComponent) {
      SimpleComponent s = (SimpleComponent) c;
      MessageElementData value =
          new MessageElementData(loc.uidPath(), loc.desc(), req.usage().toString(), -1, null,
              loc.line(), loc.column(), -1, s.position(), c.instance(), s.value().raw(), COMPONENT);
      new MessageElement("value", value, el);
    } else {
      ComplexComponent cc = (ComplexComponent) c;
      List<SimpleComponent> children = cc.children();
      if (children != null && !children.isEmpty()) {
        Iterator<SimpleComponent> it = children.iterator();
        while (it.hasNext()) {
          process(it.next(), el);
        }
      }
    }
  }

  /**
   * 
   * @param s
   * @param parent
   */
  private void process(SimpleComponent s, MessageElement parent) {
    Location loc = s.location();
    Req req = s.req();
    MessageElementData data =
        new MessageElementData(loc.uidPath(), loc.desc(), req.usage().toString(), -1, null,
            loc.line(), loc.column(), -1, s.position(), s.instance(), null, SUB_COMPONENT);
    MessageElement el = new MessageElement(NODE_SUB_COMPONENT, data, parent);
    MessageElementData value =
        new MessageElementData(loc.uidPath(), loc.desc(), req.usage().toString(), -1, null,
            loc.line(), loc.column(), -1, s.position(), s.instance(), s.value().raw(),
            SUB_COMPONENT);
    new MessageElement("value", value, el);
  }

  /**
   * 
   * @param f : field
   * @param parent : parent
   */
  private void process(Field f, MessageElement parent) {
    Location loc = f.location();
    Req req = f.req();
    Range card = Util.getOption(req.cardinality());
    String rep = f.toString();
    MessageElementData data =
        new MessageElementData(loc.uidPath(), loc.desc(), req.usage().toString(), card.min(),
            card.max(), loc.line(), loc.column(), -1, f.position(), f.instance(), null, FIELD);

    MessageElement el = new MessageElement(NODE_FIELD, data, parent);
    if (f instanceof SimpleField) {
      SimpleField s = (SimpleField) f;
      MessageElementData value =
          new MessageElementData(loc.uidPath(), loc.desc(), req.usage().toString(), card.min(),
              card.max(), loc.line(), loc.column(), -1, f.position(), f.instance(),
              s.value().raw(), FIELD);
      new MessageElement("value", value, el);
    } else {
      ComplexField c = (ComplexField) f;
      List<Component> children = c.children();
      if (children != null && !children.isEmpty()) {
        Iterator<Component> it = children.iterator();
        while (it.hasNext()) {
          process(it.next(), el);
        }
      }

    }
  }

  /**
   * 
   * @param e
   * @param parentName
   * @param parent
   */
  private void process(SegOrGroup e, String parentName, MessageElement parent) {
    if (e == null) {
      return;
    }
    if (e instanceof Segment) {
      Segment s = (Segment) e;
      Location loc = s.location();
      Req req = s.req();
      Range card = Util.getOption(req.cardinality());
      MessageElementData data =
          new MessageElementData(loc.uidPath(), loc.desc(), req.usage().toString(), card.min(),
              card.max(), loc.line(), loc.column(), -1, s.position(), s.instance(), null, SEGMENT);
      MessageElement el = new MessageElement(NODE_SEGMENT, data, parent);
      List<Field> children = s.children();
      if (children != null && !children.isEmpty()) {
        Iterator<Field> it = children.iterator();
        while (it.hasNext()) {
          process(it.next(), el);
        }
      }

    } else if (e instanceof Group) {
      Group g = (Group) e;
      List<SegOrGroup> children = g.children();
      if (children != null && !children.isEmpty()) {
        scala.collection.Iterator<SegOrGroup> it = children.iterator();
        while (it.hasNext()) {
          process(it.next(), "", parent);
        }
      }
    }
  }

}

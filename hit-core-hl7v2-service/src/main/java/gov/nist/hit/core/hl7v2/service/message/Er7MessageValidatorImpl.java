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

import gov.nist.hit.core.domain.ValidationResult;
import gov.nist.hit.core.service.exception.ValidationException;
import hl7.v2.profile.XMLDeserializer;
import hl7.v2.validation.SyncHL7Validator;
import hl7.v2.validation.content.ConformanceContext;
import hl7.v2.validation.content.DefaultConformanceContext;
import hl7.v2.validation.vs.ValueSetLibrary;

import org.apache.commons.io.IOUtils;

public class Er7MessageValidatorImpl implements Er7MessageValidator {

  /**
   * 
   * @param message
   * @param title
   * @param options : options[0]: xmlProfile, options[1]: tableLibraries contents, options[2]:
   *        validation context
   * @return
   * @throws ValidationException
   */
  @Override
  public String validatetoJson(String title, String er7Message, String profileXml,
      String constraintsXml, String valueSets) throws ValidationException {
    try {
      hl7.v2.profile.Profile profile =
          XMLDeserializer.deserialize(IOUtils.toInputStream(profileXml)).get();
      ConformanceContext c =
          DefaultConformanceContext.apply(IOUtils.toInputStream(constraintsXml)).get();
      // The plugin map. This should be empty if no plugin is used
      // Map<String, Function3<Plugin, Element, Separators, EvalResult>> pluginMap =
      // Map$.MODULE$.empty();
      ValueSetLibrary valueSetLibrary =
          valueSets != null ? ValueSetLibrary.apply(IOUtils.toInputStream(valueSets)).get() : null;
      SyncHL7Validator validator = new SyncHL7Validator(profile, valueSetLibrary, c);
      scala.collection.Iterable<String> keys = profile.messages().keys();
      String key = keys.iterator().next();
      gov.nist.validation.report.Report report = validator.check(er7Message, key);
      String res = report.toJson();
      return res;
    } catch (RuntimeException e) {
      throw new ValidationException(e);
    } catch (Exception e) {
      throw new ValidationException(e);
    }
  }

  @Override
  public ValidationResult validate(String message, String title, String... options) {
    throw new UnsupportedOperationException();
  }

}

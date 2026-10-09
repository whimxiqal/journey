package net.whimxiqal.journey.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ValidatorTest {

  @Test
  void normalizeWaypointKeyCollapsesSeparatorsAndPunctuation() {
    Assertions.assertEquals("sunken_forest", Validator.normalizeWaypointKey("Sunken Forest?"));
    Assertions.assertEquals("sunken_forest", Validator.normalizeWaypointKey("Sunken Forest"));
    Assertions.assertEquals("sunken_forest", Validator.normalizeWaypointKey("sunken-forest"));
    Assertions.assertEquals("sunken_forest", Validator.normalizeWaypointKey("sunken_forest"));
    Assertions.assertEquals("poi_sunken_forest", Validator.normalizeWaypointKey("poi-sunken_forest"));
  }

  @Test
  void safeScopeKeyRejectsQuestionMark() {
    Assertions.assertTrue(Validator.isSafeScopeKey("poi-sunken_forest"));
    Assertions.assertTrue(Validator.isSafeScopeKey("Sunken Forest"));
    Assertions.assertFalse(Validator.isSafeScopeKey("Sunken Forest?"));
  }
}

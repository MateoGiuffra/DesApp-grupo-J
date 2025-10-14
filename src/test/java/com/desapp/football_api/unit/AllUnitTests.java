package com.desapp.football_api.unit;

import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.IncludeTags;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

/**
 * Aggregates all tests tagged with @Tag("unit") so they can be executed via:
 *   gradlew test --tests "com.desapp.football_api.unit.*"
 * This keeps CI filters working even though real tests live in subpackages
 * like controller, services, model, utils, etc.
 */
@Suite
@SuiteDisplayName("All Unit Tests Suite")
@IncludeEngines("junit-jupiter")
@SelectPackages("com.desapp.football_api")
@IncludeTags("unit")
public class AllUnitTests {
}

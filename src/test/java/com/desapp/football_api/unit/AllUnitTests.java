package com.desapp.football_api.unit;

import org.junit.platform.suite.api.IncludeTags;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

/**
 * Aggregates all tests tagged with @Tag("unit") so they can be executed via:
 *   gradlew test --tests "com.desapp.football_api.unit.*"
 * This keeps CI filters working even though real tests live in subpackages
 * like controller, services, model, utils, etc.
 */
@Suite
@SelectPackages({
        "com.desapp.football_api.controller",
        "com.desapp.football_api.services",
        "com.desapp.football_api.security",
        "com.desapp.football_api.utils",
        "com.desapp.football_api.model"
})
@IncludeTags("unit")
public class AllUnitTests {
}

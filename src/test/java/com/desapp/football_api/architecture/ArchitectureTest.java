package com.desapp.football_api.architecture;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Tag;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Architecture tests to keep the codebase consistent.
 * These are tagged as unit so they run with the regular "test" task.
 */
@Tag("unit")
@AnalyzeClasses(packages = "com.desapp.football_api")
public class ArchitectureTest {

    // Naming rules
    @ArchTest
    static final ArchRule controllers_should_be_suffixed_with_Controller =
            classes().that().resideInAnyPackage("..controller..")
                    .and().areAnnotatedWith(RestController.class).or().areAnnotatedWith(Controller.class)
                    .should().haveSimpleNameEndingWith("Controller");

    @ArchTest
    static final ArchRule service_interfaces_should_be_suffixed_with_Service =
            classes().that().resideInAnyPackage("..services")
                    .and().areInterfaces()
                    .should().haveSimpleNameEndingWith("Service");

    @ArchTest
    static final ArchRule service_impls_should_be_suffixed_with_ServiceImpl =
            classes().that().resideInAnyPackage("..services.impl..")
                    .and().areAnnotatedWith(Service.class)
                    .should().haveSimpleNameEndingWith("ServiceImpl");

    @ArchTest
    static final ArchRule repositories_should_be_suffixed_with_Repository =
            classes().that().resideInAnyPackage("..repository..")
                    .and().areInterfaces()
                    .should().haveSimpleNameEndingWith("Repository");

    // Package placement rules
    @ArchTest
    static final ArchRule controllers_should_reside_in_controller_packages =
            classes().that().haveSimpleNameEndingWith("Controller")
                    .should().resideInAnyPackage("..controller..")
                    .because("Controllers must be under a controller package");

    @ArchTest
    static final ArchRule repositories_should_reside_in_repository_packages =
            classes().that().haveSimpleNameEndingWith("Repository")
                    .should().resideInAnyPackage("..repository..");

    @ArchTest
    static final ArchRule services_should_reside_in_services_packages =
            classes().that().haveSimpleNameEndingWith("Service")
                    .or().haveSimpleNameEndingWith("ServiceImpl")
                    .should().resideInAnyPackage("..services..");

    // Annotation rules
    @ArchTest
    static final ArchRule controllers_must_be_annotated =
            classes().that().haveSimpleNameEndingWith("Controller")
                    .should().beAnnotatedWith(RestController.class)
                    .orShould().beAnnotatedWith(org.springframework.stereotype.Controller.class);

    @ArchTest
    static final ArchRule repositories_must_be_annotated =
            classes().that().haveSimpleNameEndingWith("Repository")
                    .should().beAnnotatedWith(Repository.class);

    // Coupling and layering guardrails without using Architectures.layeredArchitecture
    @ArchTest
    static final ArchRule controllers_should_not_be_accessed_by_other_layers =
            noClasses().that().resideOutsideOfPackage("..controller..")
                    .should().dependOnClassesThat().resideInAnyPackage("..controller..");

    @ArchTest
    static final ArchRule repositories_should_only_be_used_by_services =
            noClasses().that().resideOutsideOfPackage("..services..")
                    .should().dependOnClassesThat().resideInAnyPackage("..repository..");

    @ArchTest
    static final ArchRule services_should_not_depend_on_controllers =
            noClasses().that().resideInAnyPackage("..services..")
                    .should().dependOnClassesThat().resideInAnyPackage("..controller..");

    @ArchTest
    static final ArchRule controllers_should_not_depend_on_repositories =
            noClasses().that().resideInAnyPackage("..controller..")
                    .should().dependOnClassesThat().resideInAnyPackage("..repository..");
}

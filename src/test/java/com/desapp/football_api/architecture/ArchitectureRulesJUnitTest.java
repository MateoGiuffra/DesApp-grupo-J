package com.desapp.football_api.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@Tag("unit")
class ArchitectureRulesJUnitTest {

    private static final JavaClasses CLASSES = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.desapp.football_api");

    // Naming rules
    @Test
    @DisplayName("Controllers should end with Controller")
    void controllers_should_be_suffixed_with_Controller() {
        ArchRule rule = classes().that().resideInAnyPackage("..controller..")
                .and().areAnnotatedWith(RestController.class).or().areAnnotatedWith(Controller.class)
                .should().haveSimpleNameEndingWith("Controller");
        rule.check(CLASSES);
    }

    @Test
    @DisplayName("Service interfaces should end with Service")
    void service_interfaces_should_be_suffixed_with_Service() {
        ArchRule rule = classes().that().resideInAnyPackage("..services")
                .and().areInterfaces()
                .should().haveSimpleNameEndingWith("Service");
        rule.check(CLASSES);
    }

    @Test
    @DisplayName("Service implementations should end with ServiceImpl")
    void service_impls_should_be_suffixed_with_ServiceImpl() {
        ArchRule rule = classes().that().resideInAnyPackage("..services.impl..")
                .and().areAnnotatedWith(Service.class)
                .should().haveSimpleNameEndingWith("ServiceImpl");
        rule.check(CLASSES);
    }

    @Test
    @DisplayName("Repositories should end with Repository")
    void repositories_should_be_suffixed_with_Repository() {
        ArchRule rule = classes().that().resideInAnyPackage("..repository..")
                .and().areInterfaces()
                .should().haveSimpleNameEndingWith("Repository");
        rule.check(CLASSES);
    }

    // Package placement rules
    @Test
    @DisplayName("Controllers should reside in controller packages")
    void controllers_should_reside_in_controller_packages() {
        ArchRule rule = classes().that().haveSimpleNameEndingWith("Controller")
                .should().resideInAnyPackage("..controller..");
        rule.check(CLASSES);
    }

    @Test
    @DisplayName("Repositories should reside in repository packages")
    void repositories_should_reside_in_repository_packages() {
        ArchRule rule = classes().that().haveSimpleNameEndingWith("Repository")
                .should().resideInAnyPackage("..repository..");
        rule.check(CLASSES);
    }

    @Test
    @DisplayName("Services should reside in services packages")
    void services_should_reside_in_services_packages() {
        ArchRule rule = classes().that().haveSimpleNameEndingWith("Service")
                .or().haveSimpleNameEndingWith("ServiceImpl")
                .should().resideInAnyPackage("..services..");
        rule.check(CLASSES);
    }

    // Annotation rules
    @Test
    @DisplayName("Controllers must be annotated with @RestController or @Controller")
    void controllers_must_be_annotated() {
        ArchRule rule = classes().that().haveSimpleNameEndingWith("Controller")
                .should().beAnnotatedWith(RestController.class)
                .orShould().beAnnotatedWith(org.springframework.stereotype.Controller.class);
        rule.check(CLASSES);
    }

    @Test
    @DisplayName("Repositories must be annotated with @Repository")
    void repositories_must_be_annotated() {
        ArchRule rule = classes().that().haveSimpleNameEndingWith("Repository")
                .should().beAnnotatedWith(Repository.class);
        rule.check(CLASSES);
    }

    // Coupling and layering guardrails
    @Test
    @DisplayName("Other layers should not access controllers")
    void controllers_should_not_be_accessed_by_other_layers() {
        ArchRule rule = noClasses().that().resideOutsideOfPackage("..controller..")
                .should().dependOnClassesThat().resideInAnyPackage("..controller..");
        rule.check(CLASSES);
    }

    @Test
    @DisplayName("Services should not depend on controllers")
    void services_should_not_depend_on_controllers() {
        ArchRule rule = noClasses().that().resideInAnyPackage("..services..")
                .should().dependOnClassesThat().resideInAnyPackage("..controller..");
        rule.check(CLASSES);
    }

    @Test
    @DisplayName("Controllers should not depend on repositories")
    void controllers_should_not_depend_on_repositories() {
        ArchRule rule = noClasses().that().resideInAnyPackage("..controller..")
                .should().dependOnClassesThat().resideInAnyPackage("..repository..");
        rule.check(CLASSES);
    }
}

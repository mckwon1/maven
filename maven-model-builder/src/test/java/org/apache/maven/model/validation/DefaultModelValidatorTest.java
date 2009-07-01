package org.apache.maven.model.validation;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.InputStream;
import java.util.List;

import org.apache.maven.model.DefaultModelBuildingRequest;
import org.apache.maven.model.Model;
import org.apache.maven.model.ModelBuildingRequest;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.PlexusTestCase;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class DefaultModelValidatorTest
    extends PlexusTestCase
{

    private DefaultModelValidator validator;

    private Model read( String pom )
        throws Exception
    {
        String resource = "/poms/validation/" + pom;
        InputStream is = getClass().getResourceAsStream( resource );
        assertNotNull( "missing resource: " + resource, is );
        return new MavenXpp3Reader().read( is );
    }

    private ModelValidationResult validate( String pom )
        throws Exception
    {
        return validateEffective( pom, ModelBuildingRequest.VALIDATION_LEVEL_STRICT );
    }

    private ModelValidationResult validateEffective( String pom, int level )
        throws Exception
    {
        ModelBuildingRequest request = new DefaultModelBuildingRequest().setValidationLevel( level );

        return validator.validateEffectiveModel( read( pom ), request );
    }

    private ModelValidationResult validateRaw( String pom, int level )
        throws Exception
    {
        ModelBuildingRequest request = new DefaultModelBuildingRequest().setValidationLevel( level );

        return validator.validateRawModel( read( pom ), request );
    }

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();

        validator = (DefaultModelValidator) lookup( ModelValidator.class );
    }

    @Override
    protected void tearDown()
        throws Exception
    {
        this.validator = null;

        super.tearDown();
    }

    private void assertViolations( ModelValidationResult result, int errors, int warnings )
    {
        assertEquals( errors, result.getErrors().size() );
        assertEquals( warnings, result.getWarnings().size() );
    }

    public void testMissingModelVersion()
        throws Exception
    {
        ModelValidationResult result = validate( "missing-modelVersion-pom.xml" );

        assertViolations( result, 1, 0 );

        assertEquals( "'modelVersion' is missing.", result.getErrors().get( 0 ) );
    }

    public void testMissingArtifactId()
        throws Exception
    {
        ModelValidationResult result = validate( "missing-artifactId-pom.xml" );

        assertViolations( result, 1, 0 );

        assertEquals( "'artifactId' is missing.", result.getErrors().get( 0 ) );
    }

    public void testMissingGroupId()
        throws Exception
    {
        ModelValidationResult result = validate( "missing-groupId-pom.xml" );

        assertViolations( result, 1, 0 );

        assertEquals( "'groupId' is missing.", result.getErrors().get( 0 ) );
    }

    public void testInvalidIds()
        throws Exception
    {
        ModelValidationResult result = validate( "invalid-ids-pom.xml" );

        assertViolations( result, 2, 0 );

        assertEquals( "'groupId' with value 'o/a/m' does not match a valid id pattern.", result.getErrors().get( 0 ) );

        assertEquals( "'artifactId' with value 'm$-do$' does not match a valid id pattern.", result.getErrors().get( 1 ) );
    }

    public void testMissingType()
        throws Exception
    {
        ModelValidationResult result = validate( "missing-type-pom.xml" );

        assertViolations( result, 1, 0 );

        assertEquals( "'packaging' is missing.", result.getErrors().get( 0 ) );
    }

    public void testMissingVersion()
        throws Exception
    {
        ModelValidationResult result = validate( "missing-version-pom.xml" );

        assertViolations( result, 1, 0 );

        assertEquals( "'version' is missing.", result.getErrors().get( 0 ) );
    }

    public void testInvalidAggregatorPackaging()
        throws Exception
    {
        ModelValidationResult result = validate( "invalid-aggregator-packaging-pom.xml" );

        assertViolations( result, 1, 0 );

        assertTrue( result.getErrors().get( 0 ).indexOf( "Aggregator projects require 'pom' as packaging." ) > -1 );
    }

    public void testMissingDependencyArtifactId()
        throws Exception
    {
        ModelValidationResult result = validate( "missing-dependency-artifactId-pom.xml" );

        assertViolations( result, 1, 0 );

        assertTrue( result.getErrors().get( 0 ).indexOf( "'dependencies.dependency.artifactId' is missing." ) > -1 );
    }

    public void testMissingDependencyGroupId()
        throws Exception
    {
        ModelValidationResult result = validate( "missing-dependency-groupId-pom.xml" );

        assertViolations( result, 1, 0 );

        assertTrue( result.getErrors().get( 0 ).indexOf( "'dependencies.dependency.groupId' is missing." ) > -1 );
    }

    public void testMissingDependencyVersion()
        throws Exception
    {
        ModelValidationResult result = validate( "missing-dependency-version-pom.xml" );

        assertViolations( result, 1, 0 );

        assertTrue( result.getErrors().get( 0 ).indexOf( "'dependencies.dependency.version' is missing" ) > -1 );
    }

    public void testMissingDependencyManagementArtifactId()
        throws Exception
    {
        ModelValidationResult result = validate( "missing-dependency-mgmt-artifactId-pom.xml" );

        assertViolations( result, 1, 0 );

        assertTrue( result.getErrors().get( 0 ).indexOf(
                                                         "'dependencyManagement.dependencies.dependency.artifactId' is missing." ) > -1 );
    }

    public void testMissingDependencyManagementGroupId()
        throws Exception
    {
        ModelValidationResult result = validate( "missing-dependency-mgmt-groupId-pom.xml" );

        assertViolations( result, 1, 0 );

        assertTrue( result.getErrors().get( 0 ).indexOf(
                                                         "'dependencyManagement.dependencies.dependency.groupId' is missing." ) > -1 );
    }

    public void testMissingAll()
        throws Exception
    {
        ModelValidationResult result = validate( "missing-1-pom.xml" );

        assertViolations( result, 4, 0 );

        List<String> messages = result.getErrors();

        assertTrue( messages.contains( "\'modelVersion\' is missing." ) );
        assertTrue( messages.contains( "\'groupId\' is missing." ) );
        assertTrue( messages.contains( "\'artifactId\' is missing." ) );
        assertTrue( messages.contains( "\'version\' is missing." ) );
        // type is inherited from the super pom
    }

    public void testMissingPluginArtifactId()
        throws Exception
    {
        ModelValidationResult result = validate( "missing-plugin-artifactId-pom.xml" );

        assertViolations( result, 1, 0 );

        assertEquals( "'build.plugins.plugin.artifactId' is missing.", result.getErrors().get( 0 ) );
    }

    public void testMissingPluginVersion()
        throws Exception
    {
        ModelValidationResult result =
            validateEffective( "missing-plugin-version-pom.xml", ModelBuildingRequest.VALIDATION_LEVEL_MAVEN_3_1 );

        assertViolations( result, 1, 0 );

        assertEquals( "'build.plugins.plugin.version' is missing for org.apache.maven.plugins:maven-it-plugin",
                      result.getErrors().get( 0 ) );

        result = validateEffective( "missing-plugin-version-pom.xml", ModelBuildingRequest.VALIDATION_LEVEL_MAVEN_3_0 );

        assertViolations( result, 0, 1 );
    }

    public void testMissingRepositoryId()
        throws Exception
    {
        ModelValidationResult result =
            validateRaw( "missing-repository-id-pom.xml", ModelBuildingRequest.VALIDATION_LEVEL_STRICT );

        assertViolations( result, 4, 0 );

        assertEquals( "'repositories.repository.id' is missing.", result.getErrors().get( 0 ) );

        assertEquals( "'repositories.repository.url' is missing.", result.getErrors().get( 1 ) );

        assertEquals( "'pluginRepositories.pluginRepository.id' is missing.", result.getErrors().get( 2 ) );

        assertEquals( "'pluginRepositories.pluginRepository.url' is missing.", result.getErrors().get( 3 ) );
    }

    public void testMissingResourceDirectory()
        throws Exception
    {
        ModelValidationResult result = validate( "missing-resource-directory-pom.xml" );

        assertViolations( result, 2, 0 );

        assertEquals( "'build.resources.resource.directory' is missing.", result.getErrors().get( 0 ) );

        assertEquals( "'build.testResources.testResource.directory' is missing.", result.getErrors().get( 1 ) );
    }

}
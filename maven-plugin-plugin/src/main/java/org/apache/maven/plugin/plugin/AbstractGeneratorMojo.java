package org.apache.maven.plugin.plugin;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.descriptor.InvalidPluginDescriptorException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.project.MavenProject;
import org.apache.maven.tools.plugin.extractor.ExtractionException;
import org.apache.maven.tools.plugin.generator.Generator;
import org.apache.maven.tools.plugin.scanner.MojoScanner;
import org.apache.maven.tools.plugin.util.PluginUtils;

import java.io.File;
import java.io.IOException;
import java.util.Set;

/**
 * Abstract class for this Plugin.
 *
 * @author <a href="mailto:jason@maven.org">Jason van Zyl</a>
 * @version $Id$
 */
public abstract class AbstractGeneratorMojo
    extends AbstractMojo
{
    /**
     * The project currently being built.
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    protected MavenProject project;

    /**
     * The component used for scanning the source tree for mojos.
     *
     * @component
     * @required
     */
    protected MojoScanner mojoScanner;

    /**
     * The goal prefix that will appear before the ":".
     *
     * @parameter
     */
    protected String goalPrefix;

    /**
     * The role names of mojo extractors to use.
     * <p/>
     * If not set, all mojo extractors will be used. If set to an empty extractor name, no mojo extractors
     * will be used.
     * <p/>
     * Example:
     * <p/>
     * <pre>
     *  &lt;!-- Use all mojo extractors --&gt;
     *  &lt;extractors/&gt;
     *  
     *  &lt;!-- Use no mojo extractors --&gt;
     *  &lt;extractors&gt;
     *      &lt;extractor/&gt;
     *  &lt;/extractors&gt;
     *  
     *  &lt;!-- Use only bsh mojo extractor --&gt;
     *  &lt;extractors&gt;
     *      &lt;extractor&gt;bsh&lt;/extractor&gt;
     *  &lt;/extractors&gt;
     * </pre>
     *
     * @parameter
     */
    protected Set/* <String> */extractors;

    /**
     * @return the output directory where files will be generated.
     */
    protected abstract File getOutputDirectory();

    /**
     * @return the wanted <code>Generator</code> implementation.
     */
    protected abstract Generator createGenerator();

    /** {@inheritDoc} */
    public void execute()
        throws MojoExecutionException
    {
        if ( !project.getPackaging().equals( "maven-plugin" ) )
        {
            return;
        }

        String defaultGoalPrefix = PluginDescriptor.getGoalPrefixFromArtifactId( project.getArtifactId() );
        if ( goalPrefix == null )
        {
            goalPrefix = defaultGoalPrefix;
        }
        else if ( !goalPrefix.equals( defaultGoalPrefix ) )
        {
            getLog().warn(
                           "\n\nGoal prefix is specified as: '" + goalPrefix + "'. "
                               + "Maven currently expects it to be '" + defaultGoalPrefix + "'.\n" );
        }

        mojoScanner.setActiveExtractors( extractors );

        // TODO: could use this more, eg in the writing of the plugin descriptor!
        PluginDescriptor pluginDescriptor = new PluginDescriptor();

        pluginDescriptor.setGroupId( project.getGroupId() );

        pluginDescriptor.setArtifactId( project.getArtifactId() );

        pluginDescriptor.setVersion( project.getVersion() );

        pluginDescriptor.setGoalPrefix( goalPrefix );

        pluginDescriptor.setName( project.getName() );

        pluginDescriptor.setDescription( project.getDescription() );

        try
        {
            pluginDescriptor.setDependencies( PluginUtils.toComponentDependencies( project.getRuntimeDependencies() ) );

            mojoScanner.populatePluginDescriptor( project, pluginDescriptor );

            getOutputDirectory().mkdirs();

            createGenerator().execute( getOutputDirectory(), pluginDescriptor );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Error writing plugin descriptor", e );
        }
        catch ( InvalidPluginDescriptorException e )
        {
            throw new MojoExecutionException( "Error extracting plugin descriptor: \'" + e.getLocalizedMessage() + "\'",
                                              e );
        }
        catch ( ExtractionException e )
        {
            throw new MojoExecutionException( "Error extracting plugin descriptor: \'" + e.getLocalizedMessage() + "\'",
                                              e );
        }
    }
}

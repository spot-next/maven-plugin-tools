package org.apache.maven.tools.plugin.util;

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

import junit.framework.TestCase;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;

/**
 * @author jdcasey
 */
public class TestUtils
    extends TestCase
{

    public void testDirnameFunction_METATEST() throws UnsupportedEncodingException
    {
        String classname = getClass().getName().replace( '.', '/' ) + ".class";
        String basedir = TestUtils.dirname( classname );

        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        URL resource = cl.getResource( classname );

        assertEquals( URLDecoder.decode( resource.getPath(), "UTF-8" ), basedir + classname );
    }

    public static String dirname( String file ) throws UnsupportedEncodingException
    {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        URL fileResource = cl.getResource( file );

        String fullPath = fileResource.getPath();

        String path = fullPath.substring( 0, fullPath.length() - file.length() );

        return URLDecoder.decode( path, "UTF-8" ); // necessary for JDK 1.5+, where spaces are escaped to %20
    }

}
/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gradle.plugins.ide.eclipse

class EclipseCustomSourceAndJavadocLocationIntegrationTest extends AbstractEclipseIntegrationSpec {

    def "Custom source and javadoc location"() {
        setup:
        File customSource = temporaryFolder.file('guava-custom-source.jar')
        File customJavadoc = temporaryFolder.file('guava-custom-javadoc.jar')

        buildFile << """
            apply plugin: 'java'
            apply plugin: 'eclipse'

            repositories {
                jcenter()
            }

            dependencies {
                compile 'com.google.guava:guava:18.0'
            }

            eclipse {
                classpath {
                    file {
                        whenMerged { classpath ->
                            def guava = classpath.entries.find { it.path.contains('guava-18') }
                            guava.setJavadoc(new File('${customJavadoc.absolutePath.replace('\\', '\\\\')}'))
                            guava.setSource(new File('${customSource.absolutePath.replace('\\', '\\\\')}'))
                        }
                    }
                }
            }
        """

        when:
        run "eclipse"

        then:
        def cp = getClasspath()
        cp.lib('guava-18.0.jar').assertHasJavadoc(customJavadoc)
        cp.lib('guava-18.0.jar').assertHasSource(customSource)
    }
}

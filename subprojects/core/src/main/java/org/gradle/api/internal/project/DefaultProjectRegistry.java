/*
 * Copyright 2007-2008 the original author or authors.
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
package org.gradle.api.internal.project;

import org.gradle.api.InvalidUserDataException;
import org.gradle.api.Project;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class DefaultProjectRegistry<T extends ProjectIdentifier> implements ProjectRegistry<T> {
    private Set<T> projects = new TreeSet<T>();
    private Map<String, T> projectsByPath = new HashMap<String, T>();
    private Map<String, Set<T>> subProjectsByPath = new HashMap<String, Set<T>>();
    private Map<String, Set<T>> allProjectsByPath = new HashMap<String, Set<T>>();

    public void addProject(T project) {
        projects.add(project);
        projectsByPath.put(project.getPath(), project);
        subProjectsByPath.put(project.getPath(), new TreeSet<T>());
        TreeSet<T> allProjects = new TreeSet<T>();
        allProjects.add(project);
        this.allProjectsByPath.put(project.getPath(), allProjects);
        addProjectToParentSubProjects(project);
    }

    public T removeProject(String path) {
        T project = projectsByPath.remove(path);
        assert project != null;
        projects.remove(project);
        subProjectsByPath.remove(path);
        allProjectsByPath.remove(path);
        ProjectIdentifier loopProject = project.getParentIdentifier();
        while (loopProject != null) {
            subProjectsByPath.get(loopProject.getPath()).remove(project);
            allProjectsByPath.get(loopProject.getPath()).remove(project);
            loopProject = loopProject.getParentIdentifier();
        }
        return project;
    }

    private void addProjectToParentSubProjects(T project) {
        ProjectIdentifier loopProject = project.getParentIdentifier();
        while (loopProject != null) {
            subProjectsByPath.get(loopProject.getPath()).add(project);
            allProjectsByPath.get(loopProject.getPath()).add(project);
            loopProject = loopProject.getParentIdentifier();
        }
    }

    public Set<T> getAllProjects() {
        return Collections.unmodifiableSet(projects);
    }

    @Override
    public T getRootProject() {
        return getProject(Project.PATH_SEPARATOR);
    }

    public T getProject(String path) {
        return projectsByPath.get(path);
    }

    public T getProject(final File projectDir) {
        T match = null;
        Set<T> multipleMatches = null;
        for (T project : projects) {
            if (project.getProjectDir().equals(projectDir)) {
                if (match == null) {
                    match = project;
                } else {
                    if (multipleMatches == null) {
                        multipleMatches = new TreeSet<T>();
                        multipleMatches.add(match);
                    }
                    multipleMatches.add(project);
                }
            }
        }
        if (multipleMatches != null) {
            throw new InvalidUserDataException(String.format("Found multiple projects with project directory '%s': %s", projectDir, multipleMatches));
        }

        return match;
    }

    public Set<T> getAllProjects(String path) {
        Set<T> projects = allProjectsByPath.get(path);
        return projects != null ? projects : Collections.<T>emptySet();
    }

    public Set<T> getSubProjects(String path) {
        Set<T> projects = subProjectsByPath.get(path);
        return projects != null ? projects : Collections.<T>emptySet();
    }
}

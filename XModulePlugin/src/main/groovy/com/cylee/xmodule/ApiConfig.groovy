package com.cylee.xmodule;

import com.android.build.gradle.BaseExtension;
import com.android.build.gradle.api.AndroidSourceSet;

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.execution.TaskExecutionListener
import org.gradle.api.tasks.TaskState
import org.gradle.api.tasks.compile.JavaCompile
import groovy.io.FileType;

public class ApiConfig {
    private File apiDir;
    private List<String> fileNamePaths;
    private Project mProject;

    public ApiConfig(Project project) {
        mProject = project;
    }

    public void config() {
        configProject(mProject)
        mProject.getGradle().addListener(new TaskExecutionListener() {
            @Override
            void beforeExecute(Task task) {
                if (task.getProject() ==  mProject && task instanceof JavaCompile) {
                    task.doLast {
                        if (fileNamePaths == null || fileNamePaths.empty) {
                            return
                        }
                        List<File> checkClasses = new LinkedList<>();
                        task.getOutputs().files.forEach {
                            file ->
                                if (file.isDirectory()) {
                                    file.eachFileRecurse(FileType.FILES) {
                                        if (isBuildFile(it)) {
                                            checkClasses.add(it)
                                        }
                                    }
                                } else {
                                    if (isBuildFile(file)) {
                                        checkClasses.add(file)
                                    }
                                }
                        }
                        checkClasses.forEach {
                            String filePath = it.getAbsolutePath();
                            filePath = filePath.replaceAll("\\\\", "/");
                            for (fn in fileNamePaths) {
                                if (filePath.contains(fn)) {
                                    it.delete()
                                    break
                                }
                            }
                        }
                    }
                }
            }

            @Override
            void afterExecute(Task task, TaskState taskState) {

            }
        })
    }

    boolean isBuildFile(File file) {
        String name = file.getName();
        return name.endsWith("dex") || name.endsWith("class");
    }

    private void configProject(Project project) {
        final String apiDirName = "src/main/api";
        apiDir = project.file(apiDirName)
        if (apiDir != null && apiDir.exists() && apiDir.isDirectory()) {
            BaseExtension extension = project.getExtensions().findByType(BaseExtension.class)
            if (extension != null) {
                AndroidSourceSet mainSourceSet = extension.getSourceSets().findByName("main");
                if (mainSourceSet != null) {
                    mainSourceSet.java.srcDirs += apiDirName
                }
            }
            fileNamePaths = new LinkedList<>();
            apiDir.eachFileRecurse(FileType.FILES) {
                it ->
                    String path = it.path
                    if (path.endsWith(".java") || path.endsWith(".kt")) {
                        path = path.replaceAll("\\\\", "/");
                        int index = path.indexOf(apiDirName);
                        int dotIndex = path.lastIndexOf(".");
                        if (index > -1 && index < dotIndex) {
                            fileNamePaths.add(path.substring(index + apiDirName.length(), dotIndex))
                        }
                    }
            }
        }
    }
}

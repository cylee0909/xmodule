package com.cylee.xmodule

import com.android.build.gradle.BaseExtension
import com.android.build.gradle.api.AndroidSourceSet
import groovy.io.FileType
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.execution.TaskExecutionListener
import org.gradle.api.file.FileTreeElement
import org.gradle.api.specs.Spec
import org.gradle.api.tasks.TaskState
import org.gradle.api.tasks.compile.JavaCompile

public class ApiConfig {
    public static final String MODULE_PATH = "src/main/modules";
    public static final String MODULE_NAME = "modules";
    public static final String API_SOURCE_NAME = "src/main/api";
    public static final String MODULES_SOURCE_NAME = "src/main/modules";
    private Map<String, List<String>> filterFileConfigs;
    private Project mProject;
    private XModule mConfig;
    private Map<String, ModuleConfig> mModuleConfigs;

    public ApiConfig(Project project, XModule config) {
        mProject = project;
        mConfig = config;
    }

    public void config() {
        mProject.getGradle().addListener()
        configProject(mProject);

        mProject.getGradle().addListener(new TaskExecutionListener() {
            @Override
            void beforeExecute(Task task) {
                if (task.getProject() ==  mProject && task instanceof JavaCompile) {
//                    if (filterFileConfigs == null || filterFileConfigs.size() == 0) {
//                        return
//                    }
//
//                    List<String> filterNames = new ArrayList<>()
//                    Set<String> filterFolders = filterFileConfigs.keySet();
//                    for (String name : filterFolders) {
//                        ModuleConfig config = mModuleConfigs.get(name);
//                        if (config == null || !config.compileToDex) {
//                            filterNames.addAll(filterFileConfigs.get(name))
//                        }
//                    }
                    JavaCompile javaCompile = (JavaCompile)task;
                    javaCompile.exclude(new Spec<FileTreeElement>() {
                        @Override
                        boolean isSatisfiedBy(FileTreeElement fileTreeElement) {
                            File current = fileTreeElement.getFile()
                            String path = current.getAbsolutePath()
                            path = path.replaceAll('\\\\', '/')
                            if (path.contains(MODULE_PATH)) {
                                File parent = current.getParentFile()
                                while (parent != null && parent.isDirectory() && parent.getName() != MODULE_NAME) {
                                    current = parent
                                    parent = parent.getParentFile()
                                }
                                ModuleConfig config = mModuleConfigs.get(current.name)
                                return config != null && !config.compileToDex
                            }
                            return false
                        }
                    })
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
        mModuleConfigs = new HashMap<>();
        File modulesDir = project.file(MODULES_SOURCE_NAME)
        File apiDir = project.file(API_SOURCE_NAME)
        if (!modulesDir.exists()) {
            modulesDir.mkdirs()
        }

        if (!apiDir.exists()) {
            apiDir.mkdirs();
        }

        if (apiDir.exists()) {
            addDirToSource(API_SOURCE_NAME);
        }

        if (modulesDir.exists() && modulesDir.isDirectory()) {
            filterFileConfigs = new HashMap<>();
            File[] sourceDirs = modulesDir.listFiles(new FileFilter() {
                @Override
                boolean accept(File file) {
                    boolean accept = file.isDirectory();
                    if (accept) {
                        String name = file.getName();
                        mModuleConfigs.put(name, new ModuleConfig(name));
                    }
                    return accept;
                }
            });

            mProject.afterEvaluate {
                if (mConfig.getModuleCofig() != null) {
                    mConfig.getModuleCofig().execute(mModuleConfigs.values())
                }
            }

            if (sourceDirs != null && sourceDirs.length > 0) {
                for (File dir : sourceDirs) {
                    String name = dir.getName();
                    configDir(name)
                }
            }
        }
    }

    private void addDirToSource(String dirName) {
        BaseExtension extension = mProject.getExtensions().findByType(BaseExtension.class)
        if (extension != null) {
            AndroidSourceSet mainSourceSet = extension.getSourceSets().findByName("main");
            if (mainSourceSet != null) {
                mainSourceSet.java.srcDirs += mProject.file(dirName).getAbsolutePath();
            }
        }
    }

    private void configDir(String sourceName) {
        String dirName = "${MODULES_SOURCE_NAME}/$sourceName"
        File dir = mProject.file(dirName)
        if (dir != null && dir.exists() && dir.isDirectory()) {
            addDirToSource(dirName);
            ArrayList<String> filterNames = new ArrayList();
            dir.eachFileRecurse(FileType.FILES) {
                it ->
                    String path = it.path
                    if (path.endsWith(".java") || path.endsWith(".kt")) {
                        path = path.replaceAll("\\\\", "/");
                        int index = path.indexOf(dirName);
                        int dotIndex = path.lastIndexOf(".");
                        if (index > -1 && index < dotIndex) {
                            filterNames.add(path.substring(index + dirName.length(), dotIndex))
                        }
                    }
            }
            if (!filterNames.isEmpty()) {
                filterFileConfigs.put(sourceName, filterNames);
            }
        }
    }
}

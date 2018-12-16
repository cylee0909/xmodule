package com.cylee.xmodule

import com.android.build.api.transform.*
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.api.AndroidSourceSet
import com.android.build.gradle.internal.pipeline.TransformManager
import groovy.io.FileType
import org.apache.commons.io.FileUtils
import org.gradle.api.Project

/**
 *  create by cylee 2018/12/16
 */
public class ApiTransform extends Transform {
    private Project mProject
    private File apiDir;
    private List<String> fileNamePaths;

    public ApiTransform(Project project) {
        mProject = project
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
                        int index = path.indexOf(apiDirName);
                        int dotIndex = path.lastIndexOf(".");
                        if (index > -1 && index < dotIndex) {
                            fileNamePaths.add(path.substring(index + apiDirName.length(), dotIndex))
                        }
                    }
            }
        }
    }

    @Override
    public String getName() {
        return "api";
    }

    @Override
    public Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS;
    }

    @Override
    public Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.PROJECT_ONLY;
    }

    @Override
    public boolean isIncremental() {
        return true;
    }

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        transformInvocation.inputs.each { input ->
            input.directoryInputs.each { dirInput ->
                def destDir = transformInvocation.outputProvider.getContentLocation(dirInput.name, dirInput.contentTypes, dirInput.scopes, Format.DIRECTORY)
                FileUtils.copyDirectory(dirInput.file, destDir, new FileFilter() {
                    @Override
                    boolean accept(File file) {
                        if (file.isFile() && fileNamePaths != null) {
                            boolean filter = false;
                            String filePath = file.getAbsolutePath();
                            int dotIndex = filePath.lastIndexOf(".");
                            if (dotIndex > -1) {
                                filePath = filePath.substring(0, dotIndex);
                            }
                            for (it in fileNamePaths) {
                                if (filePath.contains(it)) {
                                    filter = true
                                    break;
                                }
                            }
                            return !filter
                        }
                        return true;
                    }
                })
            }
        }
    }
}

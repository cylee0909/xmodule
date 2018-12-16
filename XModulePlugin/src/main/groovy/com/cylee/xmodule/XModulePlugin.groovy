package com.cylee.xmodule

import org.gradle.api.Plugin
import org.gradle.api.Project

class XModulePlugin implements Plugin<Project> {
    void apply(Project project) {
        project.android.registerTransform(new ApiTransform(project))
    }
}
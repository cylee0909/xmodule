package com.cylee.xmodule

import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project

class ModuleConfig {
    /**
     * 模块名称
     */
    String name;
    /**
     * 是否编译到dex，默认false
     */
    boolean compileToDex;

    ModuleConfig(String name) {
        this.name = name
    }
}

class XModule {
    private List<ModuleConfig> mModules;
    private boolean logEnable;
    private Action<? super Collection<ModuleConfig>> mModuleCofig;

    public Action<? super Collection<ModuleConfig>> getModuleCofig() {
        return mModuleCofig;
    }

    void modules(Action<? super Collection<ModuleConfig>> action) {
        mModuleCofig = action;
    }

    boolean getLogEnable() {
        return logEnable
    }

    void setLogEnable(boolean debug) {
        this.logEnable = debug
    }
}

class XModulePlugin implements Plugin<Project> {
    void apply(Project project) {
        XModule extension = project.extensions.create("xmodule", XModule)
        new ApiConfig(project, extension).config();
    }
}
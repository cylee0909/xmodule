# Xmodule简介
## Xmodule是什么
Xmodule是一个gradle插件，用来支持Android项目的模块化，以实现模块间的运行时及编译期的解构。

## Xmodule怎么使用

### 接入方法

* 项目根目录的build.gradle中添加依赖

```groovy
buildscript {
    repositories {
         maven {
                url  "https://dl.bintray.com/cylee0909/xmodule"
            }
    }
    dependencies {
    	classpath 'com.cylee.libx:xmodule:0.3.0'
    }
}
```



* Sync Project with gradle files

* 在需要模块化或者使用模块的module里引入插件

  在对应的模块的build.gradle中导入插件`apply plugin: 'com.cylee.xmodule'`

* 配置插件【可选】

  ```groovy
  xmodule {
      modules {
          it.forEach {
              String moduleName = it.name;
              if ("push".equals(moduleName)) {
                  println("Xmodule : find the module --> $moduleName")
                  it.compileToDex = !checkCompileModule("lib_umeng_push");
              }
          }
      }
  }
  ```

### 使用过程

对于Xmodule而言，要进行模块化的模块和使用模块的模块都需要进行相关的配置，需要独立到单独的Module里（Android Studio里 File -> new -> new Module..）, 对于多数app来说，其app也是一个Module

#### 模块提供者

模块提供者（主要是那些需要模块化的代码），需抽离到一个独立的Module里。

* 引入插件。引入插件后，更新一下依赖，此Module下会自动生成 `src/main/api` 及 `src/main/modules`文件夹。

* 梳理模块需要对外提供的api。定义一个对外接口（eg, com.test.ITest），此接口需要实现INoProguard接口, 此接口除了Android及java相关api不要依赖任何第三方api, 以便于编译器的解耦。将接口文件移动到`src/main/api `文件夹下的对应package下。

* 对于第二步定义的接口需要引用的一些Java bean文件，也同样放到`src/main/api`对应的package下

* 在`src/main/java`下对应的文件夹下定义第二步声明接口的实现类，建议实现类和接口在同一package下，并且名称为接口名加`_Impl`的形式。例如对于`com.test.ITest`接口，其实现类为`com.test.ITest_Impl`

#### 模块使用者

模块使用者（主要是需要使用上述模块提供者的模块），需要抽离到一个独立的Module里

* 引入插件
  引入插件后，更新一下依赖，此Module下会自动生成 `src/main/api` 及 `src/main/modules`文件夹。

* 梳理需要依赖的模块，并找到对应的模块提供者，在`src/main/modules`下为对应的模块创建文件夹，文件夹名称为模块名称（此名称用于xmodule的配置），将其`src/main/api`下的相关代码拷贝到`src/main/modules/`下对应模块文件夹下

* 调用相关模块的方法。

  1、根据接口查找对应的实现类。注意：此处每次调用findApi都会通过反射来创建响应的实例，所以上层调用出需注意缓存。

  ```java
  ITest testImpl = ApiFinder.findApi(ITest.class);
  // ITest testImpl = ApiFinder.findApiWithSuffix(ITest.class, "CustomSuffix");
  // ITest testImpl = ApiFinder.findApiAbsolute(ITest.class, "com.test2.ItestImpl");
  ```

  2、调用实现类的相关方法。

  可以使用`src/main/modules`下对应模块文件夹下的任何类来进行相关的调用。

  ```java
  if (testImpl != null) {
   	testImpl.test();   
  }
  ```

### 相关配置

  对于`src/main/modules`下的所有class 默认情况下是不会被编译到相关的dex里的。因此，如果对应的模块提供者没有参与编译，最终运行起来会报响应的接口文件不存在的错误。因此，我们需要在模块使用者对应的build.gradle文件里进行相关的配置，在对应的模块提供者没有被编译时配置对应`src/main/api`下的class编译到dex中，保证正常运行。

  ```groovy
  // xmodule和android是同一层级
  xmodule {
      modules {
          it.forEach {
              String moduleName = it.name;
              if ("push".equals(moduleName)) {
                  println("Xmodule : find the module --> $moduleName")
                  it.compileToDex = !(push模块提供者不参与编译);
              }
          }
      }
  }
  ```


### 问题联系

欢迎各位同学试用，提issue。

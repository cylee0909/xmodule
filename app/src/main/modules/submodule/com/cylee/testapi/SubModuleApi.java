/**
 * Created by cylee on 2018/12/16.
 */
package com.cylee.testapi;

import android.util.Log;

import test.testapi.MainApi;

public class SubModuleApi {
    public static void test() {
        Log.d("cylee", "SubModuleApi I am in testmodule");
        MainApi.test();
    }
}

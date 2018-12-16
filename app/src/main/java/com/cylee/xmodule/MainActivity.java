package com.cylee.xmodule;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import com.cylee.testapi.SubModuleApi;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SubModuleApi.test();
    }
}

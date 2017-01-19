package com.jojo.jiaminsun.nestingscroll;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.jojo.jiaminsun.nestingscroll.One.EventDispatchPlanActivity;
import com.jojo.jiaminsun.nestingscroll.Three.CoordinatorLayoutActivity;
import com.jojo.jiaminsun.nestingscroll.Two.NestingScrollActivity;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private ListView mListView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ArrayList<String> mData = new ArrayList<>();
        mData.add("原始事件分发方案");
        mData.add("使用NestScrollParent与NestScrollChild接口");
        mData.add("使用CoordinatorLayout");
        mListView = (ListView) findViewById(R.id.listView);
        mListView.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mData));
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = null;
                if(position == 0){
                    intent = new Intent(MainActivity.this, EventDispatchPlanActivity.class);
                } else if(position == 1){
                    intent = new Intent(MainActivity.this, NestingScrollActivity.class);
                } else if(position == 2){
                    intent = new Intent(MainActivity.this, CoordinatorLayoutActivity.class);
                }

                if(intent != null){
                    startActivity(intent);
                    //overridePendingTransition(R.anim.slide_in_right, R.anim.slide_still);
                }
            }
        });

    }
}

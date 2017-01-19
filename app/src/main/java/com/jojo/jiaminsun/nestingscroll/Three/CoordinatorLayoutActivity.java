package com.jojo.jiaminsun.nestingscroll.Three;

import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.jojo.jiaminsun.nestingscroll.R;
import com.jojo.jiaminsun.nestingscroll.Two.MyRecyclerAdapter;
import com.jojo.jiaminsun.nestingscroll.Util;

public class CoordinatorLayoutActivity extends AppCompatActivity {

    private View mHeaderView;
    private LinearLayout mTargetLayout;
    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private SparseArray<RecyclerView> mPageMap = new SparseArray<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coordinator_layout);

        mHeaderView = findViewById(R.id.book_header);
        CoordinatorLayout.LayoutParams headerLp = (CoordinatorLayout.LayoutParams) mHeaderView
                .getLayoutParams();
        headerLp.setBehavior(new CoverBehavior(Util.dp2px(this, 30), 0));

        mTargetLayout = (LinearLayout) findViewById(R.id.scroll_view);
        CoordinatorLayout.LayoutParams targetLp = (CoordinatorLayout.LayoutParams) mTargetLayout
                .getLayoutParams();
        targetLp.setBehavior(new TargetBehavior(this, Util.dp2px(this, 70), 0));

        mTabLayout = (TabLayout) findViewById(R.id.tab_layout);
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mViewPager.setAdapter(mPagerAdapter);
        mTabLayout.setupWithViewPager(mViewPager);

    }

    private RecyclerView getPageView(int pos) {
        RecyclerView view = mPageMap.get(pos);
        if (view == null) {
            RecyclerView recyclerView = new RecyclerView(this);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(new MyRecyclerAdapter());
            mPageMap.put(pos, recyclerView);
            return recyclerView;
        }
        return view;
    }

    private PagerAdapter mPagerAdapter = new PagerAdapter() {
        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public Object instantiateItem(final ViewGroup container, int position) {
            View view = getPageView(position);
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            container.addView(view, params);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "item " + (position + 1);
        }
    };
}

package com.example.myapplication.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.myapplication.DBHelper;
import com.example.myapplication.R;
import com.example.myapplication.adapters.AdapterNewsListSectioned;
import com.example.myapplication.models.ModelNews;
import com.example.myapplication.models.ModelResponseNewsAll;
import com.example.myapplication.utils.API.API;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class LazyFragmentNewsUniversal extends AbstractLazyLoadFragment {
    private static final String ARG_NEWS_TYPE = "news_type";// 0: faculty, 1: announcement, 2: files

    List<ModelNews> modelNewsItems = new ArrayList<ModelNews>();
    ModelResponseNewsAll model_response_news_all;
    private boolean isRefreshing = false;//是否刷新中
    private View thisView;
    private RecyclerView recycler_view;
    private AdapterNewsListSectioned adapter_news_list_sectioned;
    private SwipeRefreshLayout swipe_refresh_layout;

    public static LazyFragmentNewsUniversal newInstance(int newsType) {
        if (newsType < 1 || newsType > 3)
            newsType = 0;
        LazyFragmentNewsUniversal fragment = new LazyFragmentNewsUniversal();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_NEWS_TYPE, newsType);
        fragment.setArguments(bundle);
        return fragment;
    }

    // 获取当前Fragment是学院还是通知还是文件
    public int getNewsType() {
        if (getArguments() != null) {
            return getArguments().getInt(ARG_NEWS_TYPE);
        }
        return 0;
    }


    // 调用本方法请务必保证本方法在UI线程外运行
    public void refreshNews(final boolean force) {
        try {
            DBHelper helper = new DBHelper(getContext());
            API api = new API(getContext(), force);

            int type = getNewsType();
            Log.e("NewsType", String.valueOf(type));
            switch (type) {
                default:
                    //TODO: FACULTY
                    modelNewsItems.clear();
                    modelNewsItems.add(new ModelNews("MUST+提示", "这里是你所在学院的新闻，下拉即可刷新新闻列表", "2019-06-21", true, "", R.drawable.image_junyi));
                    break;
                case 2:
                    model_response_news_all = api.news_announcements_get(api.loginRecord().getToken(), 0, 10);
                    modelNewsItems.clear();
                    if (model_response_news_all != null && model_response_news_all.getCode() == 0) {
                        modelNewsItems.addAll(model_response_news_all.getNews_list());
                    }
                    break;
                case 3:
                    model_response_news_all = api.news_documents_get(api.loginRecord().getToken(), 0, 20);
                    modelNewsItems.clear();
                    if (model_response_news_all != null && model_response_news_all.getCode() == 0) {
                        modelNewsItems.addAll(model_response_news_all.getNews_list());
                    }
                    break;
            }

            //TODO: 这里会报NullPointerException
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    adapter_news_list_sectioned.notifyDataSetChanged();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void swipeRefreshLayoutOnRefresh() {
        //检查是否处于刷新状态
        if (!isRefreshing) {
            isRefreshing = true;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    refreshNews(true);
                    FragmentActivity activity = getActivity();
                    if (activity != null)
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                swipe_refresh_layout.setRefreshing(false);
                                isRefreshing = false;
                            }
                        });
                }
            }).start();
        }
    }


    // 当用户看到这个页面的时候，再去加载数据
    // 当用户看到这个页面的时候，再去加载数据
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        thisView = inflater.inflate(R.layout.fragment_news_universal, container, false);
        swipe_refresh_layout = (SwipeRefreshLayout) thisView.findViewById(R.id.swipe_refresh_layout);
        swipe_refresh_layout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayoutOnRefresh();
            }
        });

        adapter_news_list_sectioned = new AdapterNewsListSectioned(thisView.getContext(), modelNewsItems);
        adapter_news_list_sectioned.setOnItemClickListener(new AdapterNewsListSectioned.OnItemClickListener() {
            @Override
            public void onItemClick(View view, ModelNews obj, int position) {
                Snackbar.make(thisView, "Item " + obj.getTitle() + " clicked", Snackbar.LENGTH_SHORT).show();
            }
        });

        recycler_view = (RecyclerView) thisView.findViewById(R.id.recycler_view);
        recycler_view.setLayoutManager(new LinearLayoutManager(thisView.getContext()));
        recycler_view.setHasFixedSize(true);
        recycler_view.setAdapter(adapter_news_list_sectioned);
        recycler_view.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                NestedScrollView scroller = (NestedScrollView) thisView.findViewById(R.id.nested_scroll_view);
                if (scroller != null) {
                    scroller.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
                        @Override
                        public void onScrollChange(NestedScrollView v, int scrollx, int scrolly, int oldscrollx, int oldscrolly) {
                           /* if (scrolly > oldscrolly) {
                                Log.i("recyclerView", "Scroll DOWN");
                            }
                            if (scrolly < oldscrolly) {
                                Log.i("recyclerView", "Scroll UP");
                            }
                            if (scrolly == 0) {
                                Log.i("recyclerView", "TOP SCROLL");
                            }*/
                            if (scrolly == (v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight())) {
                                Log.e("recyclerView", "BOTTOM SCROLL,刷子你");
                                Snackbar.make(thisView, "触底，这里是加载更多刷新操作", Snackbar.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
               /* if (!recyclerView.canScrollVertically(1) && isVisible()) {
                    Log.e("FragmentNewsUnivertract", "刷子你");
                    Snackbar.make(thisView, "触底，这里是加载更多刷新操作", Snackbar.LENGTH_SHORT).show();
                }*/
            }
        });
        Log.d("Fragment ModelNews All", "onCreateView");
        return thisView;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onFirstVisible() {
        super.onFirstVisible();
        Log.e("FragmentNewsUniversal", "onFirstVisible");
        swipe_refresh_layout.setRefreshing(true);
        new Thread(new Runnable() {
            @Override
            public void run() {
                refreshNews(false);
                FragmentActivity activity = getActivity();
                if (activity != null)
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            swipe_refresh_layout.setRefreshing(false);
                            isRefreshing = false;
                        }
                    });
            }
        }).start();
    }

    @Override
    protected void onVisibilityChange(boolean isVisible) {
        super.onVisibilityChange(isVisible);
    }
}

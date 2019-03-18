package com.chejdj.wanandroid.ui.home;

import android.content.Intent;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;

import com.chejdj.wanandroid.R;
import com.chejdj.wanandroid.network.bean.article.Article;
import com.chejdj.wanandroid.network.bean.article.ArticleData;
import com.chejdj.wanandroid.network.bean.homepage.HomeBanner;
import com.chejdj.wanandroid.ui.base.WanAndroidBaseFragment;
import com.chejdj.wanandroid.ui.commonarticlelist.CommonArticleAdapter;
import com.chejdj.wanandroid.ui.home.contract.HomeContract;
import com.chejdj.wanandroid.ui.home.presenter.HomePresenter;
import com.chejdj.wanandroid.ui.search.SearchActivity;
import com.chejdj.wanandroid.ui.webviewarticle.WebViewArticleActivity;
import com.chejdj.wanandroid.util.CommonHandler;
import com.youth.banner.Banner;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;


public class HomeFragment extends WanAndroidBaseFragment implements HomeContract.View {
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout refreshLayout;
    private List<Article> articleList;
    private List<HomeBanner> bannerList;
    private Banner homeBanner;
    private int currentPage = 0;
    private int totalPage = 0;
    private CommonArticleAdapter commonArticleAdapter;
    private CommonHandler delayHandler;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_home;
    }

    @Override
    protected void initView() {
        initData();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(OrientationHelper.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));

        View headView = LayoutInflater.from(getContext()).inflate(R.layout.banner_home, null);
        homeBanner = headView.findViewById(R.id.home_banner);
        homeBanner.setImageLoader(new HomeBannerLoader());
        homeBanner.setImages(bannerList);
        homeBanner.start();

        commonArticleAdapter = new CommonArticleAdapter(R.layout.item_article, articleList);
        commonArticleAdapter.addHeaderView(headView);
        commonArticleAdapter.setEnableLoadMore(true);

        recyclerView.setAdapter(commonArticleAdapter);

        presenter = new HomePresenter(this);
        ((HomePresenter) presenter).start();
        refreshAndloadMore();

    }

    private void initData() {
        articleList = new ArrayList<>();
        bannerList = new ArrayList<>();
        delayHandler = new CommonHandler(getActivity()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        ((HomePresenter) presenter).start();
                        refreshLayout.setRefreshing(false);
                }
            }
        };
    }


    private void refreshAndloadMore() {
        commonArticleAdapter.setOnLoadMoreListener(() -> {
            if (currentPage + 1 <= totalPage) {
                ((HomePresenter) presenter).getArticles(currentPage + 1);
            } else {
                commonArticleAdapter.loadMoreEnd();
            }
        }, recyclerView);
        refreshLayout.setOnRefreshListener(() -> delayHandler.sendMessageDelayed(delayHandler.obtainMessage(1), 2000));
        homeBanner.setOnBannerListener((position) -> {
            HomeBanner banner = bannerList.get(position);
            Article article = new Article();
            article.setTitle(banner.getTitle());
            article.setLink(banner.getUrl());
            article.setPublishTime(0);
            article.setAuthor(banner.getDesc());
            WebViewArticleActivity.startArticleActivity(getActivity(), article, false);
        });
        commonArticleAdapter.setOnItemClickListener((adapter, view, position) -> {
            Article article = articleList.get(position);
            WebViewArticleActivity.startArticleActivity(getActivity(), article, false);
        });
    }

    //获取banner数据
    @Override
    public void showBanner(List<HomeBanner> images) {
        homeBanner.update(images);
    }

    //获取到Articles
    @Override
    public void showArticles(ArticleData articleData) {
        if (totalPage == 0) {
            totalPage = articleData.getPageCount();
        }
        currentPage = articleData.getCurPage();
        commonArticleAdapter.addData(articleData.getListData());
        commonArticleAdapter.loadMoreComplete();
    }

    @OnClick(R.id.home_search)
    public void intentToSearchActivity() {
        Intent intent = new Intent(getActivity(), SearchActivity.class);
        startActivity(intent);
    }


}
package com.sumx4ever.italker.frags.search;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.sumx4ever.common.app.PresenterFragment;
import com.sumx4ever.common.widget.EmptyView;
import com.sumx4ever.common.widget.PortraitView;
import com.sumx4ever.common.widget.recycler.RecyclerAdapter;
import com.sumx4ever.italker.R;
import com.sumx4ever.italker.activities.PersonalActivity;
import com.sumx4ever.italker.activities.SearchActivity;
import com.sumx4ever.italker.factory.model.card.UserCard;
import com.sumx4ever.italker.factory.presenter.contact.FollowContract;
import com.sumx4ever.italker.factory.presenter.contact.FollowPresenter;
import com.sumx4ever.italker.factory.presenter.search.SearchContract;
import com.sumx4ever.italker.factory.presenter.search.SearchUserPresenter;

import net.qiujuer.genius.ui.Ui;
import net.qiujuer.genius.ui.compat.UiCompat;
import net.qiujuer.genius.ui.drawable.LoadingCircleDrawable;
import net.qiujuer.genius.ui.drawable.LoadingDrawable;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 搜索人的界面实现
 */
public class SearchUserFragment extends PresenterFragment<SearchContract.Presenter> implements SearchActivity.SearchFragment, SearchContract.UserView {

    @BindView(R.id.empty)
    EmptyView mEmptyView;

    @BindView(R.id.recycler)
    RecyclerView mRecycler;

    private RecyclerAdapter<UserCard> mAdapter;

    public SearchUserFragment() {
        // Required empty public constructor
    }


    @Override
    protected int getContentLayoutId() {
        return R.layout.fragment_search_user;
    }

    @Override
    protected void initWidget(View root) {
        super.initWidget(root);

        // 初始化Recycler
        mRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecycler.setAdapter(mAdapter = new RecyclerAdapter<UserCard>() {

            @Override
            protected int getItemViewType(int position, UserCard userCard) {
                // 返回cell的布局id
                return R.layout.cell_search_list;
            }

            @Override
            protected ViewHolder<UserCard> onCreateViewHolder(View root, int viewType) {
                return new SearchUserFragment.ViewHolder(root);
            }
        });

        // 初始化占位布局
        mEmptyView.bind(mRecycler);
        setPlaceHolderView(mEmptyView);
    }

    @Override
    protected void initData() {
        super.initData();
        // 发起首次搜索
        search("");

    }

    @Override
    public void search(String content) {
        // Activity -> Fragment -> Presenter -> Net
        mPresenter.search(content);
    }

    @Override
    protected SearchContract.Presenter initPresenter() {
        return new SearchUserPresenter(this);
    }

    @Override
    public void onSearchDone(List<UserCard> userCards) {
        // 数据成功情况下返回数据
        mAdapter.replace(userCards);
        // 如果有数据，则OK，如果没有数据，就显示空布局
        mPlaceHolderView.triggerOkOrEmpty(mAdapter.getItemCount() > 0);
    }

    /**
     * 每一个Cell的布局操作
     */
    class ViewHolder extends RecyclerAdapter.ViewHolder<UserCard> implements FollowContract.View {
        @BindView(R.id.im_portrait)
        PortraitView mPortraitView;

        @BindView(R.id.txt_name)
        TextView mName;

        @BindView(R.id.im_follow)
        ImageView mFollow;

        private FollowContract.Presenter mPresenter;

        public ViewHolder(View itemView) {
            super(itemView);
            // 当前View和Presenter绑定
            new FollowPresenter(this);
        }

        @Override
        protected void onBind(UserCard userCard) {
            mPortraitView.setup(Glide.with(SearchUserFragment.this), userCard);
            mName.setText(userCard.getName());
            mFollow.setEnabled(!userCard.isFollow());
        }

        @OnClick(R.id.im_follow)
        void onFollowClick() {
            // 发起关注
            mPresenter.follow(mData.getId());
        }

        @OnClick(R.id.im_portrait)
        void onPortraitClick() {
            PersonalActivity.show(getContext(), mData.getId());
        }

        @Override
        public void onFollowSucceed(UserCard userCard) {
            // 更改当前界面状态
            if (mFollow.getDrawable() instanceof LoadingDrawable) {
                ((LoadingDrawable) mFollow.getDrawable()).stop();
                // 设置为默认的
                mFollow.setImageResource(R.drawable.sel_opt_done_add);
            }
            // 发起更新
            updateData(userCard);
        }

        @Override
        public void showError(int str) {
            // 更改当前界面状态
            if (mFollow.getDrawable() instanceof LoadingDrawable) {
                // 失败则停止动画，并且显示一个圆圈
                LoadingDrawable drawable = (LoadingDrawable) mFollow.getDrawable();
                drawable.setProgress(1);
                drawable.stop();
            }
        }

        @Override
        public void showLoading() {
            int minSize = (int) Ui.dipToPx(getResources(), 22);
            int maxSize = (int) Ui.dipToPx(getResources(), 30);
            // 初始化一个圆形的动画的Drawable
            LoadingDrawable drawable = new LoadingCircleDrawable(minSize, maxSize);
            drawable.setBackgroundColor(0);

            int[] color = new int[]{UiCompat.getColor(getResources(), R.color.white_alpha_208)};
            drawable.setForegroundColor(color);
            // 设置进去
            mFollow.setImageDrawable(drawable);
            // 启动动画
            drawable.start();
        }

        @Override
        public void setPresenter(FollowContract.Presenter presenter) {
            mPresenter = presenter;
        }
    }
}

package com.example.xyzreader.ui;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ShareCompat.IntentBuilder;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.graphics.Palette;
import android.support.v7.graphics.Palette.PaletteAsyncListener;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.data.ArticleLoader.Query;

public class ArticleDetailFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = "ArticleDetailFragment";

    public static final String ARG_ITEM_ID = "item_id";
    private Cursor mCursor;
    private long mItemId;
    private View mRootView;
    private CoordinatorLayout mCoordinatorLayout;
    private CollapsingToolbarLayout mCollapsingToolbarLayout;
    private Toolbar mToolbar;
    private View mPhotoContainerView;
    private ImageView mPhotoView;
    private FloatingActionButton shareActionButton;
    private TextView bylineView;
    private TextView titleView;
    private TextView bodyView;
    private LinearLayout metaBar;


    public ArticleDetailFragment() {
    }

    public static ArticleDetailFragment newInstance(long itemId) {
        Bundle arguments = new Bundle();
        arguments.putLong(ARG_ITEM_ID, itemId);
        ArticleDetailFragment fragment = new ArticleDetailFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            mItemId = getArguments().getLong(ARG_ITEM_ID);

            setHasOptionsMenu(true);
        }
    }

    public ArticleDetailActivity getActivityCast() {
        return (ArticleDetailActivity) getActivity();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        getLoaderManager().initLoader(0, null, this);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_article_detail, container, false);
        mCoordinatorLayout = (CoordinatorLayout)
                mRootView.findViewById(R.id.draw_insets_frame_layout);
        mPhotoView = (ImageView) mRootView.findViewById(R.id.photo);
        mPhotoContainerView = mRootView.findViewById(R.id.photo_container);
        shareActionButton = (FloatingActionButton) mRootView.findViewById(R.id.share_fab);
        titleView = (TextView) mRootView.findViewById(R.id.article_title);
        bylineView = (TextView) mRootView.findViewById(R.id.article_byline);
        bodyView = (TextView) mRootView.findViewById(R.id.article_body);
        metaBar = (LinearLayout) mRootView.findViewById(R.id.meta_bar);
        mCollapsingToolbarLayout = (CollapsingToolbarLayout) mRootView.findViewById(R.id.photo_container);
        // set up the toolbar
        mToolbar = (Toolbar) mRootView.findViewById(R.id.detail_toolbar);


        getLoaderManager().initLoader(0, null, this);

        return mRootView;
    }


    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newInstanceForItemId(getActivity(), mItemId);
    }

    @Override
    public void onStart() {
        super.onStart();
        // animating the share botton
        if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
            shareActionButton.setAlpha(0f);
            shareActionButton.setScaleX(0f);
            shareActionButton.setScaleY(0f);
            shareActionButton.setTranslationZ(1f);
            shareActionButton.animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .translationZ(25f)
                    .setInterpolator(new FastOutSlowInInterpolator())
                    .setStartDelay(300)
                    .start();
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        mCursor = cursor;
        if (mCursor != null && !mCursor.moveToFirst()) {
            Log.e(TAG, "Error reading item detail cursor");
            mCursor.close();
            mCursor = null;
        }

        Typeface mainTypeface = Typeface.createFromAsset(getResources().getAssets(), "Roboto-Regular.ttf");


        if (mCursor != null) {
            mRootView.setVisibility(View.VISIBLE);
            final String title = mCursor.getString(Query.TITLE);

            bylineView.setText(Html.fromHtml(
                    DateUtils.getRelativeTimeSpanString(
                            mCursor.getLong(Query.PUBLISHED_DATE),
                            System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                            DateUtils.FORMAT_ABBREV_ALL).toString()
                            + " by <font color='#ffffff'>"
                            + mCursor.getString(Query.AUTHOR)
                            + "</font>"));
            bodyView.setText(Html.fromHtml(mCursor.getString(Query.BODY)));
            if (mToolbar != null) {
                ((ArticleDetailActivity) getActivity()).setSupportActionBar(mToolbar);
                mToolbar.setNavigationIcon(R.drawable.ic_arrow_back);
                ((ArticleDetailActivity) getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);
                mToolbar.setNavigationOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        getActivity().finish();
                    }
                });
            }

            titleView.setText(title);
            bodyView.setTypeface(mainTypeface);
            bylineView.setTypeface(mainTypeface);
            titleView.setTypeface(mainTypeface);
            Glide.with(getActivity())
                    .load(mCursor.getString(Query.PHOTO_URL))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop()
                    .dontAnimate()
                    .listener(new RequestListener<String, GlideDrawable>() {
                        @Override
                        public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                            Bitmap bitmap = ((GlideBitmapDrawable) resource.getCurrent()).getBitmap();
                            Palette.from(bitmap).generate(new PaletteAsyncListener() {
                                @Override
                                public void onGenerated(Palette palette) {
                                    int defaultColor = 0xFF333333;
                                    int color = palette.getDarkVibrantColor(defaultColor);
                                    metaBar.setBackgroundColor(color);
                                    if (mCollapsingToolbarLayout != null) {
                                        int scrimColor = palette.getDarkMutedColor(defaultColor);
                                        mCollapsingToolbarLayout.setStatusBarScrimColor(scrimColor);
                                        mCollapsingToolbarLayout.setContentScrimColor(scrimColor);
                                    }
                                }
                            });


                            return false;
                        }
                    })
                    .into(mPhotoView);

            shareActionButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(Intent.createChooser(IntentBuilder.from(getActivity())
                            .setType("text/plain")
                            .setText("Some sample text")
                            .getIntent(), getString(R.string.action_share)));

                }
            });
        } else {
            Snackbar.make(mCoordinatorLayout, R.string.errer_message, Snackbar.LENGTH_LONG).show();
        }


    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mCursor = null;
    }

}

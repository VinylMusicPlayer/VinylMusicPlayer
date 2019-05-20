package com.poupa.vinylmusicplayer.glide.artistimage;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.bumptech.glide.Priority;
import com.bumptech.glide.integration.okhttp3.OkHttpStreamFetcher;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.data.DataFetcher;
import com.bumptech.glide.load.model.GlideUrl;
import com.poupa.vinylmusicplayer.deezer.DeezerApiService;
import com.poupa.vinylmusicplayer.deezer.DeezerResponse;
import com.poupa.vinylmusicplayer.util.MusicUtil;
import com.poupa.vinylmusicplayer.util.PreferenceUtil;

import java.io.InputStream;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class ArtistImageFetcher implements DataFetcher<InputStream> {
    public static final String TAG = ArtistImageFetcher.class.getSimpleName();
    private Context context;
    private final DeezerApiService deezerRestClient;
    private final ArtistImage model;
    private volatile boolean isCancelled;
    private Call<DeezerResponse> call;
    private OkHttpClient okhttp;
    private OkHttpStreamFetcher streamFetcher;

    ArtistImageFetcher(Context context, DeezerApiService deezerRestClient, OkHttpClient okhttp, ArtistImage model) {
        this.context = context;
        this.deezerRestClient = deezerRestClient;
        this.okhttp = okhttp;
        this.model = model;
    }

    @NonNull
    @Override
    public Class<InputStream> getDataClass() {
        return InputStream.class;
    }

    @NonNull
    @Override
    public DataSource getDataSource() {
        return DataSource.REMOTE;
    }

    @Override
    public void loadData(@NonNull Priority priority, @NonNull DataCallback<? super InputStream> callback) {
        try {
            if (!MusicUtil.isArtistNameUnknown(model.artistName) && PreferenceUtil.isAllowedToDownloadMetadata(context)) {
                call = deezerRestClient.getArtistImage(model.artistName);
                call.enqueue(new Callback<DeezerResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<DeezerResponse> call, @NonNull Response<DeezerResponse> response) {
                        if (isCancelled) {
                            callback.onDataReady(null);
                            return;
                        }

                        try {
                            DeezerResponse deezerResponse = response.body();
                            String url = deezerResponse.getData().get(0).getPictureMedium();
                            streamFetcher = new OkHttpStreamFetcher(okhttp, new GlideUrl(url));
                            streamFetcher.loadData(priority, callback);
                        } catch (Exception e) {
                            callback.onLoadFailed(new Exception("No artist image url found"));
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<DeezerResponse> call, @NonNull Throwable throwable) {
                        callback.onLoadFailed(new Exception(throwable));
                    }
                });
            }
        } catch (Exception e) {
            callback.onLoadFailed(e);
        }
    }

    @Override
    public void cleanup() {
        if (streamFetcher != null) {
            streamFetcher.cleanup();
        }
    }

    @Override
    public void cancel() {
        isCancelled = true;
        if (call != null) {
            call.cancel();
        }
        if (streamFetcher != null) {
            streamFetcher.cancel();
        }
    }
}

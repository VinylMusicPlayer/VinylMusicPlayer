package com.poupa.vinylmusicplayer.deezer

import android.content.Context
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import retrofit2.http.GET
import retrofit2.http.Query
import java.io.File
import java.util.*

private const val BASE_QUERY_ARTIST = "search/artist"
private const val BASE_URL = "https://api.deezer.com/"

/**
 * @author Paolo Valerdi
 */

interface DeezerApiService {

    @GET("$BASE_QUERY_ARTIST&limit=1")
    fun getArtistImage(
            @Query("q") artistName: String
    ) : Call<DeezerResponse>

    companion object {
        operator fun invoke(context: Context): DeezerApiService {

            val okHttpClient = OkHttpClient.Builder()
                    .cache(createDefaultCache(context))
                    .addInterceptor(createCacheControlInterceptor())
                    .build()

            return Retrofit.Builder()
                    .client(okHttpClient)
                    .baseUrl(BASE_URL)
                    .callFactory(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create()
        }

        private fun createDefaultCache(context: Context): Cache? {
            val cacheDir = File(context.applicationContext.cacheDir.absolutePath, "/okhttp-deezer/")
            if (cacheDir.mkdir() or cacheDir.isDirectory) {
                return Cache(cacheDir, 1024 * 1024 * 10)
            }
            return null
        }

        private fun createCacheControlInterceptor(): Interceptor {
            return Interceptor { chain ->
                val modifiedRequest = chain.request().newBuilder()
                        .addHeader("Cache-Control",
                                String.format(
                                        Locale.getDefault(),
                                        "max-age=%d, max-stale=%d",
                                        31536000, 31536000
                                )
                        ).build()
                chain.proceed(modifiedRequest)
            }
        }
    }
}
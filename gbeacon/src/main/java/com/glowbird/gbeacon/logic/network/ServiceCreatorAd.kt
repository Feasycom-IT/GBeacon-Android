
import android.util.Log
import com.glowbird.gbeacon.logic.utils.SSLUtil
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.X509TrustManager

/**
 *　ServiceCreator 对象负责创建 Retrofit 服务实例
 */

object ServiceCreatorAd {
    // 基础 URL, 用于与 Feasycom API 通信
    private const val BASE_URL = "https://feasycomm.com/api/"

    private val loggingInterceptor = HttpLoggingInterceptor { message -> Log.e("ServiceCreatorAd","Http => $message") }

    private val httpClient = OkHttpClient.Builder()
        .sslSocketFactory(SSLUtil.getSSLSocketFactory(),object : X509TrustManager {
            override fun checkClientTrusted(p0: Array<out X509Certificate>?, p1: String?) {
            }

            override fun checkServerTrusted(p0: Array<out X509Certificate>?, p1: String?) {
            }

            override fun getAcceptedIssuers(): Array<X509Certificate?>? {
                return arrayOfNulls(0)
            }

        })
        .hostnameVerifier(SSLUtil.getHostnameVerifier())//配置
        .addNetworkInterceptor(loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY))
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    // 创建 Retrofit 实例
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(httpClient)
        .addConverterFactory(GsonConverterFactory.create()) // 添加 Gson 转换器
        .build()

    /**
     * 创建指定服务类的 Retrofit 实例
     * @param T 服务类的类型
     * @param serviceClass 服务类的 Class 对象
     * @return 指定服务类的 Retrofit 实例
     */
    fun <T> create(serviceClass: Class<T>): T = retrofit.create(serviceClass)

    /**
     * 创建指定服务类的 Retrofit实例
     * 使用 inline 函数和 reified 类型参数避免了传递 Class 对象
     * @param T 服务类的类型
     * @return 指定服务类的 Retrofit 实例
     */
    inline fun <reified T> create(): T = create(T::class.java)

}
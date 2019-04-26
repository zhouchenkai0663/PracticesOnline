package net.lzzy.practicesonline.activities.network;

import android.text.TextUtils;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;

import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


/**
 * Created by lzzy_gxy on 2019/4/19.
 * Description:
 */
public class ApiService {
    public static final OkHttpClient CLIENT = new OkHttpClient();

    public static String get(String addross, JSONObject json) throws IOException {
        URL url = new URL(addross);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        try {
            //设置请求方式,请求超时信息
            connection.setRequestMethod("GET");
            connection.setReadTimeout(5000);
            connection.setConnectTimeout(5000);
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line).append("\n");
            }
            reader.close();
            return builder.toString();
        } finally {
            connection.disconnect();
        }

    }

    public static void post(String address, JSONObject json) throws IOException {
        URL url = new URL(address);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POTS");
//        conn.setReadTimeout(5000);
//        conn.setConnectTimeout(5000);
        //设置运行输入,输出:
        conn.setDoOutput(true);
        conn.setChunkedStreamingMode(0);
        conn.setRequestProperty("Content-Type", "application/json");
        // 定义缓冲区 格式
        byte[] data = json.toString().getBytes(StandardCharsets.UTF_8);
        // 定义读取的长度
        conn.setRequestProperty("Content-Length", String.valueOf(data.length));
        //Post方式不能缓存,需手动设置为false
        conn.setUseCaches(false);
        //获取输出流
        try (OutputStream stream = conn.getOutputStream()) {
            stream.write(data);
            stream.flush();
        } finally {
            conn.disconnect();
        }
    }

    public static String okGet(String address) throws IOException {
        Request request = new Request.Builder().url(address).build();
        try (Response response = CLIENT.newCall(request).execute()) {
            if (response.isSuccessful()) {
                return response.body().string();
            } else {
                throw new IOException("错误码," + response.code());
            }
        }
    }
        //带参数的okGet方法
    public static String okGet(String address, String args, HashMap<String, Object> headers) throws IOException {
        if (!TextUtils.isEmpty(args)) {
            address = address.concat("?").concat(args);
        }
        Request.Builder builder = new Request.Builder().url(address);
        if (headers != null && headers.size() > 0) {
            for (Object o : headers.entrySet()) {
                Map.Entry entry = (Map.Entry) o;
                String key = entry.getKey().toString();
                Object val = entry.getValue();
                if (val instanceof String) {
                    builder = builder.header(key, val.toString());
                } else if (val instanceof List) {
                    for (String v : ApiService.<List<String>>cast(val)) {
                        builder = builder.addHeader(key, v);

                    }
                }
            }
        }
        Request request = builder.build();
        try (Response response = CLIENT.newCall(request).execute()) {
            if (response.isSuccessful()) {
                return response.body().string();
            } else {
                throw new IOException("错误码," + response.code());
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T cast(Object obj) {
        return (T) obj;
    }

    public static int okPost(String address, JSONObject json) throws IOException {
        RequestBody body = RequestBody.create(MediaType.parse("application/json;charset=utf-8"),
                json.toString());
        Request request = new Request.Builder()
                .url(address)
                .post(body)
                .build();
        try (Response response = CLIENT.newCall(request).execute()) {
            return response.code();
        }
    }

    //public static  int okPost(String address, JSONObject json){}

    public static String okRequest(String address, JSONObject json) throws IOException {
        RequestBody body = RequestBody.create(MediaType.parse("application/json;charset=utf-8"),
                json.toString());
        Request request = new Request.Builder()
                .url(address)
                .post(body)
                .build();
        try (Response response = CLIENT.newCall(request).execute()) {
            return response.body().string();
        }
    }
}

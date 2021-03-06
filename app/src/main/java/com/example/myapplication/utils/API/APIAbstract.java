package com.example.myapplication.utils.API;

import android.support.annotation.Nullable;
import android.util.Log;

import com.example.myapplication.interfaces.IAPI;
import com.example.myapplication.utils.RSAUtils;
import com.example.myapplication.utils.Tools;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

// MUSTPlus APIPersistence 网络访问部分实现
// 由 APIPersistence 类调用，用来获取新数据
// **这个类无数据持久化的功能**
// **只是 IAPI 接口的实现**
public abstract class APIAbstract implements IAPI {

    private String URL = "http://192.168.50.45:8000/";
    private String AUTH_SECRET = "flw4\\-t94!09tesldfgio30";

    private String http_get(String url, String token, @Nullable TreeMap<String, String> url_params, @Nullable TreeMap<String, String> headers) throws IOException {
        OkHttpClient client = new OkHttpClient();
        HttpUrl.Builder httpUrl = boxing(url, url_params, null, token);
        Request request = new Request.Builder().url(httpUrl.build()).build();
        Response response = client.newCall(request).execute();
        ResponseBody body = response.body();
        if (body == null)
            throw new IOException("NULL BODY ERROR");
        return body.string();
    }

    private String http_post(String url, String token, @Nullable TreeMap<String, String> url_params, @Nullable TreeMap<String, String> body_params, @Nullable TreeMap<String, String> headers) throws IOException {
        OkHttpClient client = new OkHttpClient();
        HttpUrl.Builder httpUrl = boxing(url, url_params, body_params, token);
        FormBody.Builder formBodyBuilder = new FormBody.Builder();
        for (Map.Entry<String, String> entry : body_params.entrySet()) {
            formBodyBuilder.add(entry.getKey(), entry.getValue());
        }
        RequestBody requestBody = formBodyBuilder.build();
        Request request = new Request.Builder().url(httpUrl.build()).post(requestBody).build();
        Response response = client.newCall(request).execute();
        ResponseBody body = response.body();
        if (body == null)
            throw new IOException("NULL BODY ERROR");
        return body.string();
    }

    private String http_delete(String url, String token, @Nullable TreeMap<String, String> url_params, @Nullable TreeMap<String, String> body_params, @Nullable TreeMap<String, String> headers) throws IOException {
        OkHttpClient client = new OkHttpClient();
        HttpUrl.Builder httpUrl = boxing(url, url_params, body_params, token);
        FormBody.Builder formBodyBuilder = new FormBody.Builder();
        for (Map.Entry<String, String> entry : body_params.entrySet()) {
            formBodyBuilder.add(entry.getKey(), entry.getValue());
        }
        RequestBody requestBody = formBodyBuilder.build();
        Request request = new Request.Builder().url(httpUrl.build()).delete(requestBody).build();
        Response response = client.newCall(request).execute();
        ResponseBody body = response.body();
        if (body == null)
            throw new IOException("NULL BODY ERROR");
        return body.string();
    }

    // 方法介绍：用来给 HTTP请求 添加 token time sign 三个参数，使用方法很简单
    // 只要传入 HttpUrl.Builder 和 TreeMap<String, String> 和 TreeMap<String, String> 和 token 即可
    // 因为Java内部使用指针，传入的 httpUrl 和 params 都是指针形式
    // 所以在这个方法内 put() 和 addQueryParameter() 会影响到 caller 那边（也就是外面）的值
    // 相当于（但严格意义上不是）引用传递了
    private void boxing(HttpUrl.Builder httpUrl, @Nullable TreeMap<String, String> get_params, @Nullable TreeMap<String, String> post_params, String token) {
        if (get_params == null) {
            get_params = new TreeMap<String, String>();
        }
        get_params.put("token", token);
        get_params.put("time", String.valueOf(System.currentTimeMillis() / 1000));
        get_params.put("sign", calcSign(get_params, post_params));
        for (Map.Entry<String, String> entry : get_params.entrySet()) {
            httpUrl.addQueryParameter(entry.getKey(), entry.getValue());
        }
    }

    // boxing 的另一种写法，内部帮你获得 HttpUrl.Builder
    private HttpUrl.Builder boxing(String url, @Nullable TreeMap<String, String> get_params, @Nullable TreeMap<String, String> post_params, String token) {
        HttpUrl.Builder httpUrl = Objects.requireNonNull(HttpUrl.parse(url)).newBuilder();
        boxing(httpUrl, get_params, post_params, token);
        return httpUrl;
    }

    @Override
    public String calcSign(TreeMap<String, String> get_data, TreeMap<String, String> post_data) {
        StringBuilder get = new StringBuilder();
        if (get_data != null) {
            for (Map.Entry<String, String> entry : get_data.entrySet()) {
                get.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
            }
            get = get.deleteCharAt(get.length() - 1);
        }

        StringBuilder post = new StringBuilder();
        if (post_data != null) {
            for (Map.Entry<String, String> entry : post_data.entrySet()) {
                post.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
            }
            post = post.deleteCharAt(post.length() - 1);
        }

        return Tools.MD5(String.valueOf(get) + post + AUTH_SECRET);
    }

    @Override
    public String authHash() throws IOException {
        String url = URL + "auth/hash";
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();

        Response response = client.newCall(request).execute();//发送请求
        ResponseBody body = response.body();
        //TODO: 已知Exception: 接收 response.body() 時，如果網絡異常，會中途中斷，產生Exception導致程序崩潰

        if (body == null)
            throw new IOException("Cannot get hash, response null");
        else
            return body.string();
    }

    @Override
    public String authLogin(String pubkey, String username, String password, String token, String cookies, String captcha) throws IOException {
        try {
            String url = URL + "auth/login";
            Log.i("AUTHLOGIN_username", username);
            Log.i("AUTHLOGIN_password", password);
            Log.i("AUTHLOGIN_token", token);
            Log.i("AUTHLOGIN_cookies", cookies);
            Log.i("AUTHLOGIN_captcha", captcha);
            OkHttpClient client = new OkHttpClient();
            RequestBody body = new FormBody.Builder()
                    .add("username", RSAUtils.encrypt(username))
                    .add("password", RSAUtils.encrypt(password))
                    .add("token", RSAUtils.encrypt(token))
                    .add("cookies", RSAUtils.encrypt(cookies))
                    .add("captcha", RSAUtils.encrypt(captcha))
                    .build();
            Request request = new Request.Builder().url(url).post(body).build();

            Response response = client.newCall(request).execute();
            ResponseBody responseBody = response.body();

            if (responseBody == null)
                throw new IOException("Cannot loginRecord, response null");
            else
                return responseBody.string();
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }

    @Override
    public String authLogout(String token) throws IOException {
        String url = URL + "auth/logout";
        return http_post(url, token, null, null, null);
    }

    @Override
    public String course(String token, Integer course_id) throws IOException {
        String url = URL + "course/" + course_id;
        return http_get(url, token, null, null);
    }


    @Override
    public String courseComment(String token, Integer course_id, APIOperation operation, @Nullable Double rank, @Nullable String content, @Nullable Integer comment_id) throws IOException {
        String url = URL + "course/" + course_id + "/comment";
        TreeMap<String, String> get_params = new TreeMap<>();
        TreeMap<String, String> post_params = new TreeMap<>();
        if (operation == APIOperation.GET) { // 获取评论

            return http_get(url, token, null, null);

        } else if (operation == APIOperation.POST) { // 发布评论

            if (rank == null || content == null) {
                throw new IOException("NULL RANK OR CONTENT ERROR");
            }
            post_params.put("rank", rank.toString());
            post_params.put("content", content);
            return http_post(url, token, null, post_params, null);

        } else if (operation == APIOperation.DELETE) { // 删除评论

            if (comment_id == null) {
                throw new IOException("NULL COMMENT_ID ERROR");
            }
            get_params.put("id", course_id.toString());
            return http_delete(url, token, get_params, null, null);

        }
        throw new IOException("Operation Invalid");
    }

    @Override
    public String courseThumbsUp(String token, Integer comment_id, APIOperation operation) throws IOException {
        String url = URL + "course/thumbs_up";
        TreeMap<String, String> url_params = new TreeMap<>();
        url_params.put("comment_id", comment_id.toString());
        if (operation == APIOperation.POST) { // 点赞
            return http_post(url, token, url_params, null, null);
        } else if (operation == APIOperation.DELETE) { // 取消点赞
            return http_delete(url, token, url_params, null, null);
        }
        throw new IOException("Operation Invalid");
    }

    @Override
    public String courseThumbsDown(String token, Integer comment_id, APIOperation operation) throws IOException {
        String url = URL + "course/thumbs_down";
        TreeMap<String, String> url_params = new TreeMap<>();
        url_params.put("comment_id", comment_id.toString());
        if (operation == APIOperation.POST) { // 踩
            return http_post(url, token, url_params, null, null);
        } else if (operation == APIOperation.DELETE) { // 取消踩
            return http_delete(url, token, url_params, null, null);
        }
        throw new IOException("Operation Invalid");
    }

    @Override
    public String courseFtp(String token, Integer course_id, APIOperation operation) {
        return null;
    }

    @Override
    public String newsAll(String token, Integer from, Integer count) throws IOException {
        String url = URL + "news/all";
        TreeMap<String, String> params = new TreeMap<>();
        params.put("from", String.valueOf(from));
        params.put("count", String.valueOf(count));
        return http_get(url, token, params, null);
    }

    @Override
    public String newsAnnouncements(String token, Integer from, Integer count) throws IOException {
        String url = URL + "news/announcements";
        TreeMap<String, String> params = new TreeMap<>();
        params.put("from", String.valueOf(from));
        params.put("count", String.valueOf(count));
        return http_get(url, token, params, null);
    }

    @Override
    public String newsDocuments(String token, Integer from, Integer count) throws IOException {
        String url = URL + "news/documents";
        TreeMap<String, String> params = new TreeMap<>();
        params.put("from", String.valueOf(from));
        params.put("count", String.valueOf(count));
        return http_get(url, token, params, null);
    }

    @Override
    public String newsFaculty(String token, String faculty_name_zh, Integer from, Integer count) {
        return null;
    }

    @Override
    public String newsDepartment(String token, String department_name_zh, Integer from, Integer count) {
        return null;
    }

    @Override
    public String studentMe(String token) throws IOException {
        String url = URL + "student/me";
        return http_get(url, token, null, null);
    }

    @Override
    public String teacher(String token, String name_zh) {
        return null;
    }

    @Override
    public String timetable(String token, Integer intake, Integer week) throws IOException {
        String url = URL + "timetable";
        TreeMap<String, String> params = new TreeMap<>();
        params.put("intake", String.valueOf(intake));
        params.put("week", String.valueOf(week));
        return http_get(url, token, params, null);
    }

    @Override
    public String semester() throws IOException {
        String url = URL + "basic/semester";
        Request request = new Request.Builder().get().url(url).build();
        Response response = new OkHttpClient().newCall(request).execute();
        ResponseBody body = response.body();
        if (body == null)
            throw new IOException("NULL ERROR");
        else
            return body.string();
    }

    @Override
    public String week() throws IOException {
        String url = URL + "basic/week";
        Request request = new Request.Builder().get().url(url).build();
        Response response = new OkHttpClient().newCall(request).execute();
        ResponseBody body = response.body();
        if (body == null)
            throw new IOException("NULL ERROR");
        else
            return body.string();
    }
}

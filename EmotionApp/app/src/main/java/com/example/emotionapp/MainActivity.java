package com.example.emotionapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Random;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private SharedPreferences sharedPreferences;
    private Button buttonViewProfile, buttonSendData;
    private GridView gridView;
    private ThumbnailAdapter adapter;
    private WebView webView;
    private TextView textViewHeartRate;
    private TextView textViewEmotionsKeywords;  // 새로운 TextView 추가
    private Handler heartRateHandler = new Handler();
    private Runnable heartRateRunnable;
    private Random random = new Random();
    private int currentHeartRate = 0; // 심박수를 저장할 변수

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = getSharedPreferences("UserProfile", Context.MODE_PRIVATE);

        // 프로필 정보가 저장되어 있는지 확인
        if (isUserProfileStored()) {
            setContentView(R.layout.activity_main);

            // 심박수 표시 텍스트뷰
            textViewHeartRate = findViewById(R.id.textViewHeartRate);
            textViewEmotionsKeywords = findViewById(R.id.textViewEmotionsKeywords);  // 새로운 TextView 초기화

            // "내 정보 보기" 버튼 설정
            buttonViewProfile = findViewById(R.id.buttonViewProfile);
            buttonViewProfile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showProfilePopup();
                }
            });

            // "지금 내 상태는" 버튼 설정
            buttonSendData = findViewById(R.id.buttonSendData);
            buttonSendData.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showEmotionInputDialog();
                }
            });

            gridView = findViewById(R.id.gridView);
            adapter = new ThumbnailAdapter(this);
            gridView.setAdapter(adapter);

            webView = findViewById(R.id.webView);
            webView.getSettings().setJavaScriptEnabled(true); // 자바스크립트를 활성화합니다.
            webView.setWebChromeClient(new WebChromeClient());
            webView.setWebViewClient(new WebViewClient());

            // 심박수 업데이트 시작
            startHeartRateUpdates();
        } else {
            // 프로필 정보가 없으면 ProfileActivity로 이동
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private boolean isUserProfileStored() {
        return sharedPreferences.contains("name") &&
                sharedPreferences.contains("age") &&
                sharedPreferences.contains("gender");
    }

    private void showProfilePopup() {
        // SharedPreferences에서 프로필 정보 가져오기
        String name = sharedPreferences.getString("name", "");
        int age = sharedPreferences.getInt("age", -1);
        String gender = sharedPreferences.getString("gender", "");

        // 다이얼로그 생성
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("프로필 정보");

        String message = "이름: " + name + "\n나이: " + age + "\n성별: " + gender;
        builder.setMessage(message);

        builder.setPositiveButton("확인", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showEmotionInputDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("현재 하고 싶은 말");

        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_text_input, null);
        builder.setView(dialogView);

        final EditText inputText = dialogView.findViewById(R.id.inputText);

        builder.setPositiveButton("전송", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String userInput = inputText.getText().toString();
                createAndSendJSON(currentHeartRate, userInput); // 실제 심박수를 사용
            }
        });
        builder.setNegativeButton("취소", null);

        builder.show();
    }

    private void createAndSendJSON(int heartRate, String userInput) {
        int age = sharedPreferences.getInt("age", -1);
        String gender = sharedPreferences.getString("gender", "");
        if (gender.equals("남자")) {
            gender = "male";
        } else if (gender.equals("여자")) {
            gender = "female";
        }

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("heart_rate", heartRate);
            jsonObject.put("text", userInput);
            jsonObject.put("age", age);
            jsonObject.put("gender", gender);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        sendToServer(jsonObject);
    }

    private void sendToServer(JSONObject jsonObject) {
        OkHttpClient client = new OkHttpClient();
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(jsonObject.toString(), JSON);
        Request request = new Request.Builder()
                .url("http://175.197.91.11:1133/analyze_without_heart_rate")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "서버 요청 실패", e);
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "서버 요청 실패", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String responseData = response.body().string();
                    runOnUiThread(() -> processResponse(responseData));
                } else {
                    Log.e(TAG, "서버 응답 실패: " + response.message());
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "서버 응답 실패", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void processResponse(String response) {
        try {
            JSONObject jsonResponse = new JSONObject(response);
            String emotionsKeywords = jsonResponse.getString("emotions_keywords");
            JSONArray recommendedVideos = jsonResponse.getJSONArray("recommended_videos");

            // 감정 및 키워드 텍스트뷰에 설정
            textViewEmotionsKeywords.setText(emotionsKeywords);

            // Display emotions and keywords
            Toast.makeText(this, emotionsKeywords, Toast.LENGTH_LONG).show();

            adapter.clear();
            for (int i = 0; i < recommendedVideos.length(); i++) {
                JSONObject videoObject = recommendedVideos.getJSONObject(i);
                String title = videoObject.getString("title");
                String thumbnailUrl = videoObject.getString("thumbnail_url");
                String videoUrl = videoObject.getString("url");

                Thumbnail thumbnail = new Thumbnail(title, thumbnailUrl, videoUrl);
                adapter.addThumbnail(thumbnail);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void startHeartRateUpdates() {
        heartRateRunnable = new Runnable() {
            @Override
            public void run() {
                currentHeartRate = generateRandomHeartRate();
                textViewHeartRate.setText("현재 심박수: " + currentHeartRate);
                heartRateHandler.postDelayed(this, 5000); // 5초마다 심박수 업데이트
            }
        };
        heartRateHandler.post(heartRateRunnable);
    }

    private int generateRandomHeartRate() {
        return random.nextInt(40) + 60; // 60에서 100 사이의 임의의 BPM 값 생성
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        heartRateHandler.removeCallbacks(heartRateRunnable); // 액티비티가 파괴될 때 심박수 업데이트 중지
    }
}

package com.example.emotionapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    private EditText editTextName, editTextAge;
    private Spinner spinnerGender;
    private Button buttonSubmit;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // SharedPreferences 초기화
        sharedPreferences = getSharedPreferences("UserProfile", Context.MODE_PRIVATE);

        // XML 레이아웃에서 위젯들을 가져옴
        editTextName = findViewById(R.id.editTextName);
        editTextAge = findViewById(R.id.editTextAge);
        spinnerGender = findViewById(R.id.spinnerGender);
        buttonSubmit = findViewById(R.id.buttonSubmit);

        // 성별 옵션 배열을 가져와서 Spinner에 설정
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.gender_options,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(adapter);

        // 제출 버튼 클릭 리스너 설정
        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitProfile();
            }
        });
    }

    // 프로필 제출 메서드
    private void submitProfile() {
        String name = editTextName.getText().toString();
        String ageStr = editTextAge.getText().toString();
        String gender = spinnerGender.getSelectedItem().toString();

        if (name.isEmpty() || ageStr.isEmpty()) {
            Toast.makeText(this, "이름과 나이를 모두 입력하세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 나이 문자열을 정수로 변환
        int age = Integer.parseInt(ageStr);

        // SharedPreferences에 데이터 저장
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("name", name);
        editor.putInt("age", age);
        editor.putString("gender", gender);
        editor.apply();

        // 다음 액티비티로 전달할 Intent 생성
        Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}

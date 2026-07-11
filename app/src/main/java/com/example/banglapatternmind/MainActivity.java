package com.example.banglapatternmind;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity {
    private static final int SESSION_SIZE = 20;
    private static final String PREFS = "pattern_mind";
    private static final String CACHE_KEY = "cached_questions";
    private static final String UPDATE_URL = "";

    private final List<Question> allQuestions = new ArrayList<>();
    private final List<Question> session = new ArrayList<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private LinearLayout root;
    private TextView title;
    private TextView subtitle;
    private TextView questionText;
    private TextView scoreText;
    private ProgressBar progress;
    private LinearLayout optionsBox;
    private Button primaryButton;
    private Button updateButton;

    private int index = 0;
    private int score = 0;
    private boolean answered = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildLayout();
        loadQuestions();
        showHome();
    }

    private void buildLayout() {
        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        scrollView.setBackgroundColor(Color.rgb(247, 241, 229));

        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(22), dp(28), dp(22), dp(28));
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        scrollView.addView(root);

        title = text("", 30, true);
        subtitle = text("", 17, false);
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setLineSpacing(dp(3), 1.0f);

        progress = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progress.setMax(SESSION_SIZE);
        progress.setProgress(0);
        LinearLayout.LayoutParams progressParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(12));
        progressParams.setMargins(0, dp(18), 0, dp(18));

        scoreText = text("", 16, true);
        questionText = text("", 24, true);
        questionText.setGravity(Gravity.CENTER);
        questionText.setLineSpacing(dp(4), 1.0f);
        questionText.setPadding(0, dp(18), 0, dp(16));

        optionsBox = new LinearLayout(this);
        optionsBox.setOrientation(LinearLayout.VERTICAL);
        optionsBox.setGravity(Gravity.CENTER);

        primaryButton = button("");
        updateButton = button("নতুন প্রশ্ন আনুন");

        root.addView(title);
        root.addView(subtitle);
        root.addView(progress, progressParams);
        root.addView(scoreText);
        root.addView(questionText);
        root.addView(optionsBox);
        root.addView(primaryButton);
        root.addView(updateButton);

        setContentView(scrollView);
    }

    private void showHome() {
        title.setText("মনের প্যাটার্ন");
        subtitle.setText("প্রতিটি সেশনে ২০টি ছোট প্যাটার্ন-ধাঁধা। ধীরে, আরামে, নিজের মতো করে।");
        progress.setVisibility(View.GONE);
        scoreText.setText("");
        questionText.setText("আজকের অনুশীলন শুরু করবেন?");
        optionsBox.removeAllViews();
        primaryButton.setText("২০ প্রশ্ন শুরু করুন");
        primaryButton.setVisibility(View.VISIBLE);
        primaryButton.setOnClickListener(v -> startSession());
        updateButton.setVisibility(View.VISIBLE);
        updateButton.setOnClickListener(v -> updateQuestions());
    }

    private void startSession() {
        if (allQuestions.size() < SESSION_SIZE) {
            dialog("প্রশ্ন কম আছে", "কমপক্ষে ২০টি প্রশ্ন দরকার।");
            return;
        }
        session.clear();
        List<Question> shuffled = new ArrayList<>(allQuestions);
        Collections.shuffle(shuffled);
        session.addAll(shuffled.subList(0, SESSION_SIZE));
        index = 0;
        score = 0;
        showQuestion();
    }

    private void showQuestion() {
        answered = false;
        Question q = session.get(index);
        title.setText("প্রশ্ন " + banglaNumber(index + 1) + " / " + banglaNumber(SESSION_SIZE));
        subtitle.setText(q.category);
        progress.setVisibility(View.VISIBLE);
        progress.setProgress(index + 1);
        scoreText.setText("সঠিক: " + banglaNumber(score));
        questionText.setText(q.prompt);
        optionsBox.removeAllViews();
        primaryButton.setVisibility(View.GONE);
        updateButton.setVisibility(View.GONE);

        for (int i = 0; i < q.options.size(); i++) {
            final int optionIndex = i;
            Button optionButton = button(q.options.get(i));
            optionButton.setOnClickListener(v -> chooseAnswer(optionButton, optionIndex));
            optionsBox.addView(optionButton);
        }
    }

    private void chooseAnswer(Button selected, int optionIndex) {
        if (answered) return;
        answered = true;
        Question q = session.get(index);
        boolean correct = optionIndex == q.answer;
        if (correct) score++;

        for (int i = 0; i < optionsBox.getChildCount(); i++) {
            View child = optionsBox.getChildAt(i);
            child.setEnabled(false);
            if (child instanceof Button) {
                if (i == q.answer) {
                    child.setBackgroundColor(Color.rgb(47, 111, 115));
                    ((Button) child).setTextColor(Color.WHITE);
                } else if (child == selected && !correct) {
                    child.setBackgroundColor(Color.rgb(166, 68, 54));
                    ((Button) child).setTextColor(Color.WHITE);
                }
            }
        }

        questionText.setText(correct ? "খুব ভালো! উত্তর ঠিক।" : "ভুল হয়েছে, সমস্যা নেই। ঠিক উত্তরটি সবুজ।");
        primaryButton.setVisibility(View.VISIBLE);
        primaryButton.setText(index == SESSION_SIZE - 1 ? "ফলাফল দেখুন" : "পরের প্রশ্ন");
        primaryButton.setOnClickListener(v -> {
            index++;
            if (index >= SESSION_SIZE) showResult();
            else showQuestion();
        });
    }

    private void showResult() {
        title.setText("সেশন শেষ");
        subtitle.setText("আজকের ২০টি প্রশ্ন সম্পন্ন হয়েছে।");
        progress.setVisibility(View.GONE);
        scoreText.setText("");
        optionsBox.removeAllViews();
        questionText.setText("আপনার স্কোর: " + banglaNumber(score) + " / " + banglaNumber(SESSION_SIZE) + "\n\nনিয়মিত ছোট অনুশীলনই সবচেয়ে ভালো।");
        primaryButton.setVisibility(View.VISIBLE);
        primaryButton.setText("আরেকটি সেশন");
        primaryButton.setOnClickListener(v -> startSession());
        updateButton.setVisibility(View.VISIBLE);
        updateButton.setText("শুরুর পাতায় ফিরুন");
        updateButton.setOnClickListener(v -> showHome());
    }

    private void loadQuestions() {
        try {
            SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
            String cached = prefs.getString(CACHE_KEY, null);
            String json = cached != null ? cached : readAsset("pattern_questions_bn.json");
            parseQuestions(json);
        } catch (Exception e) {
            dialog("লোড করা যায়নি", "প্রশ্নগুলো খোলা যায়নি।");
        }
    }

    private void updateQuestions() {
        if (UPDATE_URL.trim().isEmpty()) {
            dialog("আপডেট প্রস্তুত", "নিয়মিত আপডেটের জন্য MainActivity.java ফাইলে UPDATE_URL-এ আপনার JSON লিংক বসাতে হবে। এখন অ্যাপের ভিতরের প্রশ্নব্যাংক থেকে সেশন তৈরি হচ্ছে।");
            return;
        }
        questionText.setText("নতুন প্রশ্ন আনা হচ্ছে...");
        executor.execute(() -> {
            try {
                HttpURLConnection connection = (HttpURLConnection) new URL(UPDATE_URL).openConnection();
                connection.setConnectTimeout(8000);
                connection.setReadTimeout(8000);
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
                StringBuilder builder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) builder.append(line);
                String json = builder.toString();
                List<Question> parsed = parseQuestionList(json);
                if (parsed.size() < SESSION_SIZE) throw new IllegalArgumentException("Need 20 questions");
                getSharedPreferences(PREFS, MODE_PRIVATE).edit().putString(CACHE_KEY, json).apply();
                mainHandler.post(() -> {
                    allQuestions.clear();
                    allQuestions.addAll(parsed);
                    dialog("আপডেট হয়েছে", "নতুন প্রশ্নগুলো যোগ হয়েছে।");
                    showHome();
                });
            } catch (Exception e) {
                mainHandler.post(() -> dialog("আপডেট হয়নি", "ইন্টারনেট বা প্রশ্নের ফরম্যাটে সমস্যা হয়েছে।"));
            }
        });
    }

    private String readAsset(String fileName) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(getAssets().open(fileName), StandardCharsets.UTF_8));
        StringBuilder builder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) builder.append(line);
        return builder.toString();
    }

    private void parseQuestions(String json) throws Exception {
        allQuestions.clear();
        allQuestions.addAll(parseQuestionList(json));
    }

    private List<Question> parseQuestionList(String json) throws Exception {
        List<Question> questions = new ArrayList<>();
        JSONObject root = new JSONObject(json);
        JSONArray items = root.getJSONArray("questions");
        for (int i = 0; i < items.length(); i++) {
            JSONObject item = items.getJSONObject(i);
            JSONArray optionsJson = item.getJSONArray("options");
            List<String> options = new ArrayList<>();
            for (int j = 0; j < optionsJson.length(); j++) options.add(optionsJson.getString(j));
            questions.add(new Question(
                    item.getString("category"),
                    item.getString("prompt"),
                    options,
                    item.getInt("answer")
            ));
        }
        return questions;
    }

    private TextView text(String value, int sp, boolean bold) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(sp);
        view.setTextColor(Color.rgb(38, 43, 43));
        view.setGravity(Gravity.CENTER);
        view.setTypeface(Typeface.DEFAULT, bold ? Typeface.BOLD : Typeface.NORMAL);
        view.setPadding(0, dp(6), 0, dp(6));
        return view;
    }

    private Button button(String value) {
        Button btn = new Button(this);
        btn.setText(value);
        btn.setTextSize(18);
        btn.setAllCaps(false);
        btn.setTextColor(Color.rgb(24, 38, 38));
        btn.setBackgroundColor(Color.rgb(255, 252, 246));
        btn.setPadding(dp(12), dp(12), dp(12), dp(12));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, dp(7), 0, dp(7));
        btn.setLayoutParams(params);
        return btn;
    }

    private void dialog(String heading, String message) {
        new AlertDialog.Builder(this)
                .setTitle(heading)
                .setMessage(message)
                .setPositiveButton("ঠিক আছে", null)
                .show();
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private String banglaNumber(int number) {
        char[] bengaliDigits = {'০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯'};
        String source = String.valueOf(number);
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < source.length(); i++) {
            char c = source.charAt(i);
            builder.append(Character.isDigit(c) ? bengaliDigits[c - '0'] : c);
        }
        return builder.toString();
    }

    private static class Question {
        final String category;
        final String prompt;
        final List<String> options;
        final int answer;

        Question(String category, String prompt, List<String> options, int answer) {
            this.category = category;
            this.prompt = prompt;
            this.options = options;
            this.answer = answer;
        }
    }
}

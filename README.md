# মনের প্যাটার্ন

এই Android app-টি Bengali number-series practice sessions-এর জন্য বানানো। প্রতিটি সেশনে 20টি প্রশ্ন আসে, প্রশ্নগুলো shuffled হয়, এবং শেষে স্কোর দেখা যায়। এতে তিনটি mode আছে: সহজ, মাঝারি, কঠিন।

## Android Studio ছাড়া কীভাবে APK বানাবেন

1. এই folder-টি GitHub repository-তে upload করুন।
2. GitHub-এ repository খুলে `Actions` tab-এ যান।
3. `Build Android APK` workflow চালান।
4. run শেষ হলে `BanglaPatternMind-debug-apk` artifact download করুন।
5. zip খুললে `app-debug.apk` পাবেন। সেটি ফোনে install করা যাবে।

## নতুন প্রশ্ন যোগ করা

`app/src/main/assets/pattern_questions_bn.json` ফাইলে একই ফরম্যাটে নতুন প্রশ্ন যোগ করুন। `easy`, `moderate`, এবং `tough` - প্রতিটি mode-এ অন্তত 20টি প্রশ্ন থাকা দরকার।

## নিয়মিত অনলাইন আপডেট

App-এর "নতুন প্রশ্ন আনুন" button GitHub repository-এর raw JSON file থেকে `easy`, `moderate`, এবং `tough` - তিনটি mode-এর নতুন প্রশ্ন cache করবে।

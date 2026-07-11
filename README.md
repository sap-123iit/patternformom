# মনের প্যাটার্ন

এই Android app-টি Bengali pattern-practice sessions-এর জন্য বানানো। প্রতিটি সেশনে ২০টি প্রশ্ন আসে, প্রশ্নগুলো shuffled হয়, এবং শেষে স্কোর দেখা যায়।

## Android Studio ছাড়া কীভাবে APK বানাবেন

1. এই folder-টি GitHub repository-তে upload করুন।
2. GitHub-এ repository খুলে `Actions` tab-এ যান।
3. `Build Android APK` workflow চালান।
4. run শেষ হলে `BanglaPatternMind-debug-apk` artifact download করুন।
5. zip খুললে `app-debug.apk` পাবেন। সেটি ফোনে install করা যাবে।

## নতুন প্রশ্ন যোগ করা

`app/src/main/assets/pattern_questions_bn.json` ফাইলে একই ফরম্যাটে নতুন প্রশ্ন যোগ করুন। অন্তত ২০টি প্রশ্ন থাকা দরকার।

## নিয়মিত অনলাইন আপডেট

আপনি যদি পরে একটি JSON file অনলাইনে host করেন, তাহলে `MainActivity.java` ফাইলে `UPDATE_URL`-এর ভেতরে সেই link বসান। তারপর app-এর "নতুন প্রশ্ন আনুন" button চাপলে নতুন bank cache হবে।

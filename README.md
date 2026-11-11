# সোহান ভাই (Sohan Bhai) — Android App

## Setup

1. Gemini API key বসানো
   - ফাইল: `app/src/main/assets/API_KEY`
   - এখানে শুধু আপনার API key লিখুন (একটি লাইন, অন্য কিছু না)।
   - APK তৈরির পরে MT Manager দিয়েও APK-এর `assets/API_KEY` ফাইলে বসাতে পারবেন।
   - সতর্কতা: APK-এর ভেতরের key বের করা যায়। Google AI Studio-তে key-তে অ্যাপ/IP restriction দিন।

2. APK তৈরি
   - GitHub-এ push করলে `.github/workflows/android.yml` নিজে থেকে debug APK বানাবে।
   - Actions ট্যাব → সর্বশেষ run → Artifacts থেকে `sohan-bhai-debug-apk` ডাউনলোড করুন।
   - অথবা লোকালি: `gradle :app:assembleDebug` (JDK 17 + Gradle 8.9 লাগবে)।

## ফিচার
- হোম স্ক্রিন: প্রোফাইল কার্ড, ঘূর্ণায়মান এআই বাটন, ফিচার গ্রিড, আবহাওয়া (Open-Meteo, কী লাগে না, অটো লোকেশন), অ্যানিমেটেড তারার আকাশ।
- এআই চ্যাট: টাইপিং অ্যানিমেশন, লাইভ টেক্সট রিভিল, মাইক্রোফোন ইনপুট, ফাইল/PDF অ্যাটাচমেন্ট, চ্যাট ইতিহাস (৩-ডট মেনু)।
- ছবি তৈরি ও ছবি এডিট (Gemini image model)।
- ইতিহাস শুধু অ্যাপের ভেতরে থাকে (ডিভাইসে, বাইরে কোথাও পাঠানো হয় না)।
- কোনো সেটিংস পেজ নেই।

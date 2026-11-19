# RK AI Assistant — Android Starter

এটি Android-এর জন্য প্রথম কার্যকর স্টার্টার প্রজেক্ট।

## এখন যা কাজ করে
- বাংলা টেক্সট চ্যাটের ইন্টারফেস
- Android Speech Recognizer দিয়ে ভয়েস ইনপুট
- Android Text-to-Speech দিয়ে বাংলা উত্তর পড়ে শোনানো
- কালো/সবুজ নিয়ন থিম

## কল করার নিরাপদ পদ্ধতি
- 'জিম, অমুককে ফোন করো' বললে Contacts picker খুলবে।
- যোগাযোগের নাম/নম্বর তুমি নিজে নির্বাচন করবে।
- Android Dialer নম্বর দেখাবে; কল করার আগে তোমাকে নিজে Call চাপতে হবে। অ্যাপ নিজে থেকে কল করবে না।

## এখনো যা যুক্ত হয়নি
- অনলাইন AI মডেলের সংযোগ (এর জন্য নিরাপদ backend/API configuration লাগবে)
- AI avatar animation
- Document AI / Photo AI
- কথোপকথন স্থায়ীভাবে সংরক্ষণ

## APK তৈরি করার নিয়ম
1. কম্পিউটারে Android Studio ইনস্টল করুন।
2. Android Studio থেকে **Open** নির্বাচন করে এই প্রজেক্টের `RK_AI_Assistant_Android` ফোল্ডার খুলুন।
3. Gradle Sync শেষ হতে দিন। Android SDK Platform 35 ইনস্টল করতে বললে ইনস্টল করুন।
4. ফোনে Developer options ও USB debugging চালু করে USB দিয়ে যুক্ত করুন, অথবা Android Emulator ব্যবহার করুন।
5. **Build > Build Bundle(s) / APK(s) > Build APK(s)** নির্বাচন করুন।
6. APK সাধারণত `app/build/outputs/apk/debug/app-debug.apk`-এ পাওয়া যাবে।

## গুরুত্বপূর্ণ
এটি এখনই একটি UI + voice demo। ডেমো রিপ্লাইগুলো আসল AI নয়। API key সরাসরি APK-তে রাখা নিরাপদ নয়; অনলাইন AI যুক্ত করার সময় আলাদা backend ব্যবহার করতে হবে।

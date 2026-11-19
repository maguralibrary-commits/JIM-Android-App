# শুধু Android ফোন দিয়ে JIM APK তৈরি

কম্পিউটার ছাড়াই ফোনের Chrome ব্রাউজার দিয়ে GitHub-এর ক্লাউডে APK বানানো যাবে।

## যা লাগবে
- Android ফোন ও ইন্টারনেট
- Chrome
- একটি GitHub account (না থাকলে github.com-এ Sign up)

## ধাপ
1. এই ZIP ফাইলটি ফোনে Download করে Extract করুন। Files by Google বা ফোনের File Manager ব্যবহার করতে পারেন।
2. Chrome-এ https://github.com/new খুলুন।
3. Repository name দিন: `JIM-Android-App`। Public বা Private যেকোনোটি বেছে **Create repository** চাপুন।
4. নতুন repository-তে **Add file > Upload files** চাপুন।
5. ZIP-এর ভেতরের সব ফাইল ও `.github` ফোল্ডারসহ আপলোড করুন। `.github/workflows/build-apk.yml` ফাইলটি অবশ্যই থাকতে হবে। GitHub মোবাইল ওয়েবসাইটে hidden `.github` ফোল্ডার দেখা/আপলোড কঠিন হলে Chrome-এর Desktop site চালু করুন।
6. পেজের নিচে **Commit changes** চাপুন।
7. Repository-র **Actions** ট্যাবে যান। `Build JIM APK` workflow চলবে। প্রথমবার ২–৫ মিনিট বা বেশি লাগতে পারে।
8. Workflow-তে সবুজ check এলে run-টি খুলুন। নিচের **Artifacts** অংশে `JIM-debug-APK` চাপ দিয়ে APK ZIP ডাউনলোড করুন।
9. ফোনের Downloads থেকে artifact ZIP Extract করুন। ভেতরে `app-debug.apk` পাবেন।
10. APK-তে চাপ দিয়ে Install করুন। Android চাইলে Chrome/Files অ্যাপের জন্য “Install unknown apps” অনুমতি দিন—শুধু এই ফাইলটি বিশ্বাস করলে।
11. ইনস্টল শেষে JIM খুলুন। মাইক্রোফোন অনুমতি দিন।

## সমস্যা হলে
- Actions-এ লাল X হলে run খুলে error-এর শেষ অংশ কপি করে সাহায্যের জন্য পাঠান।
- `.github/workflows/build-apk.yml` আপলোড না হলে workflow চলবে না।
- APK হলো debug build; Play Store release নয়।
- অ্যাপের বর্তমান সংস্করণ ডেমো: ভয়েস ইনপুট, বাংলা TTS এবং Contacts বেছে Android Dialer খোলা আছে। বাস্তব AI backend ও পূর্ণ ফোন নিয়ন্ত্রণ এখনও নেই।

# FragmentContainerViewIdBugDemo
minimal reproduce project demonstrating an apparent bug in FragmentContainerView's id check logic in the context of a dynamic feature module

# Characteristic stack trace
```
2022-01-04 18:15:14.139 29700-29700/com.example.fcvidbugdemo E/AndroidRuntime: FATAL EXCEPTION: main
    Process: com.example.fcvidbugdemo, PID: 29700
    java.lang.RuntimeException: Unable to start activity ComponentInfo{com.example.fcvidbugdemo/com.example.dynamicfeature.DynamicFeatureActivity}: android.view.InflateException: Binary XML file line #17 in com.example.fcvidbugdemo.dynamicfeature:layout/activity_dynamic_feature: Binary XML file line #17 in com.example.fcvidbugdemo.dynamicfeature:layout/activity_dynamic_feature: Error inflating class androidx.fragment.app.FragmentContainerView
        at android.app.ActivityThread.performLaunchActivity(ActivityThread.java:3792)
        ...
     Caused by: android.view.InflateException: Binary XML file line #17 in com.example.fcvidbugdemo.dynamicfeature:layout/activity_dynamic_feature: Binary XML file line #17 in com.example.fcvidbugdemo.dynamicfeature:layout/activity_dynamic_feature: Error inflating class androidx.fragment.app.FragmentContainerView
     Caused by: android.view.InflateException: Binary XML file line #17 in com.example.fcvidbugdemo.dynamicfeature:layout/activity_dynamic_feature: Error inflating class androidx.fragment.app.FragmentContainerView
     Caused by: java.lang.IllegalStateException: FragmentContainerView must have an android:id to add Fragment com.example.dynamicfeature.DynamicFeatureFragment
        at androidx.fragment.app.FragmentContainerView.<init>(FragmentContainerView.java:171)
        at androidx.fragment.app.FragmentLayoutInflaterFactory.onCreateView(FragmentLayoutInflaterFactory.java:52)
        ...
```

# Cause
https://android.googlesource.com/platform/frameworks/support/+/refs/heads/androidx-main/fragment/fragment/src/main/java/androidx/fragment/app/FragmentContainerView.kt#146 shows FragmentContainerView considering its view ID to be invalid if it's less than or equal to 0, but legal Android resource IDs can be interpreted by Dalvik/ART as negative values.  This is because View.getId() returns a signed 32 bit integer, so its max is 2<sup>31</sup>-1 and min is -2<sup>31</sup>, whereas Android resource IDs use the full unsigned 32 bit integer range from 1 to 2<sup>32</sup>-1 [see https://android.googlesource.com/platform/frameworks/base/+/master/core/java/android/view/View.java#28042].  When a resource ID value exceeds what the signed integer max, it overflows to negative values.  These can wrap all the way around to -1 for a resource with hex ID 0xFFFFFFFF, so the only truly invalid Android resource ID (when represented by a signed 32 bit integer) is 0. 

The role dynamic features play in this issue is unclear, but it seems that being part of a dynamic feature module imposes a huge bump up of resource IDs generated by AAPT; this makes the problem more likely to come up, but it could theoretically happen in a project without dynamic feature modules as well.

# Proposed Fix
Change the FragmentContainerView constructor such that any view ID not equal to 0 is considered valid.
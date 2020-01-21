# AndroidGodEyeDemo

## Release

1. update VERSION_NAME in gradle.properties(same as AndroidGodEye)
2. // Debug dashboard
   debugImplementation "cn.hikyson.godeye:godeye-monitor:${VERSION_NAME}"
   // Release no dashboard
   releaseImplementation "cn.hikyson.godeye:godeye-monitor-no-op:${VERSION_NAME}"
   改成
   implementation "cn.hikyson.godeye:godeye-monitor:${VERSION_NAME}"
3. ./gradlew assembleRelease
4. upload to fir.im: [https://fir.im/5k67](https://fir.im/5k67)

## Keystore

AndroidGodEye.keystore
pwd: androidgodeye
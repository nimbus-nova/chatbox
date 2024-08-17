## Quick Start

0. clone the repo down by using

```
git clone --recurse-submodules git@github.com:nimbus-nova/chatbox.git
```
1. Follow below to get MLC working
https://llm.mlc.ai/docs/deploy/android.html


2. modify `setup_mlc.sh` to your setting

3. run `setup_mlc.sh` in your this project directory
You should see
```
./chatbox
  app
  dist
  setup_mlc.sh
  build.gradle


```

4. Update `dist/lib/mlc4j/build.gradle`

```
    id 'org.jetbrains.kotlin.plugin.serialization' version '1.7.1'
```
to
```
    id 'org.jetbrains.kotlin.plugin.serialization'
```


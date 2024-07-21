source $HOME/.cargo/env # Rust
export ANDROID_NDK=$HOME/Android/Sdk/ndk/26.1.10909125 # TODO: update to whatever SDK you have here
export TVM_NDK_CC=$ANDROID_NDK/toolchains/llvm/prebuilt/linux-x86_64/bin/aarch64-linux-android24-clang
export JAVA_HOME=$HOME/android-studio/jbr
export TVM_SOURCE_DIR=$HOME/mlc-llm/3rdparty/tvm
export MLC_LLM_SOURCE_DIR=$HOME/mlc-llm

mlc_llm package # make sure to activate your virtualenv first

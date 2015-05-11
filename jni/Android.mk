# Copyright (C) 2009 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

LOCAL_PATH := $(call my-dir)

  
include $(CLEAR_VARS)  
  
LOCAL_MODULE    := bdac
LOCAL_SRC_FILES := analfunction/function.c analfunction/bdac.c analfunction/analbeat.c analfunction/qrsdet.c analfunction/classify.c analfunction/qrsfilt.c analfunction/rythmchk.c analfunction/postclas.c analfunction/noisechk.c analfunction/match.c 

LOCAL_CFLAGS := -DUSE_FILE32API -DGL_GLEXT_PROTOTYPES=1

include $(BUILD_SHARED_LIBRARY)  

include $(CLEAR_VARS)  
  
LOCAL_MODULE    := wfdb
LOCAL_SRC_FILES := readEcg/wfdbinit.c readEcg/wfdbio.c readEcg/annot.c readEcg/signal.c readEcg/calib.c readEcg/read.c
LOCAL_LDLIBS += -L$(SYSROOT)/usr/lib -llog
LOCAL_CFLAGS := -DUSE_FILE32API -DGL_GLEXT_PROTOTYPES=1

include $(BUILD_SHARED_LIBRARY)

include $(CLEAR_VARS)

LOCAL_MODULE    := fft
LOCAL_SRC_FILES := FFT/fft.c
LOCAL_LDLIBS += -L$(SYSROOT)/usr/lib -llog
LOCAL_CFLAGS := -DUSE_FILE32API -DGL_GLEXT_PROTOTYPES=1

include $(BUILD_SHARED_LIBRARY)
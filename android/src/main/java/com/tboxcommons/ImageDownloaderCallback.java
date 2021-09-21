package com.tboxcommons;

import android.graphics.Bitmap;

interface ImageDownloaderCallback {
  void downloadCompleted(Bitmap bitmap);
}

package com.welcu.android.zxingfragmentlib;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.welcu.android.zxingfragmentlib.camera.CameraManager;

/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


/**
 * This activity opens the camera and does the actual scanning on a background thread. It draws a
 * viewfinder to help the user place the barcode correctly, shows feedback as the image processing
 * is happening, and then overlays the results when a scan is successful.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 * @author Sean Owen
 */
public class BarCodeScannerFragment extends Fragment implements SurfaceHolder.Callback, IConstants {

    private static final String TAG = BarCodeScannerFragment.class.getSimpleName();

    private static final long DEFAULT_INTENT_RESULT_DURATION_MS = 1500L;
    private static final long BULK_MODE_SCAN_DELAY_MS = 1000L;
    public static final long DEFAULT_PAUSE_INTERVAL_DURATION = 2000L;

    public static final int HISTORY_REQUEST_CODE = 0x0000bacc;

    public CameraManager cameraManager;
    private BarCodeScannerHandler handler;
    private Result savedResultToShow;
    private ViewfinderView viewfinderView;
    private Result lastResult;
    private boolean hasSurface;
    private Collection<BarcodeFormat> decodeFormats;
    private Map<DecodeHintType, ?> decodeHints;
    private String characterSet;
    private InactivityTimer inactivityTimer;
    private BeepManager beepManager;
    private AmbientLightManager ambientLightManager;
    private IResultCallback mCallBack;
    private Rect customFramingRect;
    public ImageView flashView;
    public ProgressBar flashLoading;
    private boolean isTorch = false;
    public boolean isTurningFlash = false;
    private boolean isPhotoCapture = false;
    private Bitmap resultBitmap;
    
    ViewfinderView getViewfinderView() {
        return viewfinderView;
    }

    public Handler getHandler() {
        return handler;
    }

    CameraManager getCameraManager() {
        return cameraManager;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        hasSurface = false;
        inactivityTimer = new InactivityTimer(this.getActivity());
        beepManager = new BeepManager(this.getActivity());
        ambientLightManager = new AmbientLightManager(this.getActivity());

        PreferenceManager.setDefaultValues(this.getActivity(), R.xml.preferences, false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance) {
        View view = inflater.inflate(R.layout.capture, container, false);

        Window window = getActivity().getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        startScan();
    }

    @Override
    public void onPause() {
        stopScan();

        super.onPause();
    }

    /**
     * Initializes the camera and viewfinderView.
     */
    public void startScan() {
        if (cameraManager != null) {
            Log.e(TAG, "startScan: scan already started.");
            return;
        }

        // CameraManager must be initialized here, not in onCreate(). This is necessary because we don't
        // want to open the camera driver and measure the screen size if we're going to show the help on
        // first launch. That led to bugs where the scanning rectangle was the wrong size and partially
        // off screen.
        cameraManager = new CameraManager(this.getActivity().getApplication(), getView());        

        viewfinderView = (ViewfinderView) getView().findViewById(R.id.viewfinder_view);
        viewfinderView.setCameraManager(cameraManager);
        
        flashLoading = (ProgressBar) getView().findViewById(R.id.flash_loading);
        flashView = (ImageView) getView().findViewById(R.id.flash_view);
        flashView.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.photo_flash_off_selector));
        flashView.setOnClickListener(new OnClickListener() {
    		@Override
    		public void onClick(View arg0) {	
    			if (!isTurningFlash && cameraManager.getPreviewing()) {
    				isTurningFlash = true;
    				if (!isTorch) {
    					setTorchOn();				
    				} else {
    					setTorchOff();				
    				}
    				flashView.setVisibility(View.INVISIBLE);
    				flashLoading.setVisibility(View.VISIBLE);
    				isTorch = !isTorch;						
    			}
    		}        	
        });

        if (getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
        	flashView.setVisibility(View.VISIBLE);
        } else {
        	flashView.setVisibility(View.GONE);
        }

        handler = null;
        lastResult = null;

        SurfaceView surfaceView = (SurfaceView) getView().findViewById(R.id.preview_view);
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        if (hasSurface) {
            // The activity was paused but not stopped, so the surface still exists. Therefore
            // surfaceCreated() won't be called, so init the camera here.
            initCamera(surfaceHolder);
        } else {
            // Install the callback and wait for surfaceCreated() to init the camera.
            surfaceHolder.addCallback(this);
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }
        
        setupConfigFromIntent();   
        
        if (customFramingRect!=null) {
        	cameraManager.setManualFramingRect(customFramingRect);
        }    
        
        cameraManager.takePicture = isPhotoCapture;

        beepManager.updatePrefs();
        ambientLightManager.start(cameraManager);
        inactivityTimer.onResume();        
        characterSet = null;        
    }
    
    @SuppressLint("NewApi")
	public void setupConfigFromIntent() {
    	Intent intent = getActivity().getIntent();
    	if (intent != null) {
    		if (intent.hasExtra("SCAN_DIMENSION")) {
	        	if (intent.getStringExtra("SCAN_DIMENSION").equals("1D")) {
	        		decodeFormats = DecodeFormatManager.ONE_D_FORMATS;
	        	} else {
	        		decodeFormats = DecodeFormatManager.QR_CODE_FORMATS;
	        	}
	        }
	        if (intent.hasExtra("SCAN_HEIGHT") && intent.hasExtra("SCAN_WIDTH")) {
	            int width = intent.getIntExtra("SCAN_WIDTH", 0);
	            int height = intent.getIntExtra("SCAN_HEIGHT", 0);            	          	
	            if (width > 0 && height > 0) {	            	
	            	Point screenResolution = cameraManager.getViewResolution();
            		int leftOffset = (screenResolution.x - width) / 2;	            	
	                int topOffset = (screenResolution.y - height) / 2;
            		customFramingRect = new Rect(leftOffset, topOffset, leftOffset + width, topOffset + height);	                
	            }
	        }
	        if (intent.hasExtra("SCAN_PHOTO_CAPTURE")) {
	        	isPhotoCapture = intent.getBooleanExtra("SCAN_PHOTO_CAPTURE", true);	
	        }
	             
    	}
    }

    public void stopScan() {
        if (cameraManager == null) {
            Log.e(TAG, "stopScan: scan already stopped");
            return;
        }

        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        }
        inactivityTimer.onPause();
        ambientLightManager.stop();
        cameraManager.closeDriver();
        cameraManager = null;

        if (!hasSurface) {
            SurfaceView surfaceView = (SurfaceView) getView().findViewById(R.id.preview_view);
            SurfaceHolder surfaceHolder = surfaceView.getHolder();
            surfaceHolder.removeCallback(this);
        }
    }

    public void setTorch(boolean state) {
      if (cameraManager!=null) {
        cameraManager.setTorch(state, this);
      }
    }

    public void setTorchOn() {
      setTorch(true);
    }

    public void setTorchOff() {
      setTorch(false);
    }

    @Override
    public void onDestroy() {
        inactivityTimer.shutdown();
        super.onDestroy();
    }

    //    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_FOCUS:
            case KeyEvent.KEYCODE_CAMERA:
                // Handle these events so they don't launch the Camera app
                return true;
            // Use volume up/down to turn on light
            case KeyEvent.KEYCODE_VOLUME_DOWN:
//                cameraManager.setTorch(false);
                setTorchOff();
                return true;
            case KeyEvent.KEYCODE_VOLUME_UP:
                setTorchOn();
                return true;
        }
        return false; //super.onKeyDown(keyCode, event);
    }

    private void decodeOrStoreSavedBitmap(Bitmap bitmap, Result result) {
        // Bitmap isn't used yet -- will be used soon
        if (handler == null) {
            savedResultToShow = result;
        } else {
            if (result != null) {
                savedResultToShow = result;
            }
            if (savedResultToShow != null) {
                Message message = Message.obtain(handler, DECODE_SUCCEDED, savedResultToShow);
                handler.sendMessage(message);
            }
            savedResultToShow = null;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (holder == null) {
            Log.e(TAG, "*** WARNING *** surfaceCreated() gave us a null surface!");
        }
        if (!hasSurface) {
            hasSurface = true;
            initCamera(holder);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        hasSurface = false;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    public void setmCallBack(IResultCallback mCallBack) {
        this.mCallBack = mCallBack;
    }

    public void setFramingRect(int width, int height, int left, int top) {
      setFramingRect(new Rect(left, top, left + width, top + height));
    }

    public void setFramingRect(Rect rect) {
      this.customFramingRect = rect;
      if (cameraManager!=null) {
        cameraManager.setManualFramingRect(rect);
      }
    }

    public interface IResultCallback {
        void result(Result lastResult);
    }

    /**
     * A valid barcode has been found, so give an indication of success and show the results.
     *
     * @param rawResult   The contents of the barcode.
     * @param scaleFactor amount by which thumbnail was scaled
     * @param barcode     A greyscale bitmap of the camera data which was decoded.
     */
    public void handleDecode(Result rawResult, Bitmap barcode, float scaleFactor) {
        inactivityTimer.onActivity();
        lastResult = rawResult;

//        beepManager.playBeepSoundAndVibrate();
        drawResultPoints(barcode, scaleFactor, rawResult);

        if (viewfinderView != null) {
        	resultBitmap = barcode;
        	viewfinderView.drawResultBitmap(barcode);
        }     
        
        if (mCallBack != null) {
            mCallBack.result(rawResult);
        }  

        restartPreviewAfterDelay(DEFAULT_PAUSE_INTERVAL_DURATION);
    }
    
    private boolean isPortrait() {
    	if (getView().getWidth() < getView().getHeight()) 
    		return true;
    	else
    		return false;
    }

    /**
     * Superimpose a line for 1D or dots for 2D to highlight the key features of the barcode.
     *
     * @param barcode     A bitmap of the captured image.
     * @param scaleFactor amount by which thumbnail was scaled
     * @param rawResult   The decoded results which contains the points to draw.
     */
    private void drawResultPoints(Bitmap barcode, float scaleFactor, Result rawResult) {
        ResultPoint[] points = rawResult.getResultPoints();
        if (points != null && points.length > 0 && barcode != null) {
            Canvas canvas = new Canvas(barcode);
            Paint paint = new Paint();
            paint.setColor(getResources().getColor(R.color.result_points));
            
            if (points.length == 2) {
                paint.setStrokeWidth(4.0f);
                drawLine(canvas, paint, points[0], points[1], scaleFactor);
            } else if (points.length == 4 &&
                    (rawResult.getBarcodeFormat() == BarcodeFormat.UPC_A ||
                            rawResult.getBarcodeFormat() == BarcodeFormat.EAN_13)) {
                // Hacky special case -- draw two lines, for the barcode and metadata
                drawLine(canvas, paint, points[0], points[1], scaleFactor);
                drawLine(canvas, paint, points[2], points[3], scaleFactor);
            } else {
                paint.setStrokeWidth(10.0f);
                for (ResultPoint point : points) {
                    canvas.drawPoint(scaleFactor * point.getX(), scaleFactor * point.getY(), paint);
                }
            }
            
            if (isPortrait()) {
            	Log.d(TAG, "rotating results canvas");
            	canvas.rotate(90);
            }
        }
    }   

    private static void drawLine(Canvas canvas, Paint paint, ResultPoint a, ResultPoint b, float scaleFactor) {
        if (a != null && b != null) {
            canvas.drawLine(scaleFactor * a.getX(),
                    scaleFactor * a.getY(),
                    scaleFactor * b.getX(),
                    scaleFactor * b.getY(),
                    paint);
        }
    }

    private void sendReplyMessage(int id, Object arg, long delayMS) {
        Message message = Message.obtain(handler, id, arg);
        if (delayMS > 0L) {
            handler.sendMessageDelayed(message, delayMS);
        } else {
            handler.sendMessage(message);
        }
    }

    private void initCamera(SurfaceHolder surfaceHolder) {
        if (surfaceHolder == null) {
            throw new IllegalStateException("No SurfaceHolder provided");
        }
        if (cameraManager.isOpen()) {
            Log.w(TAG, "initCamera() while already open -- late SurfaceView callback?");
            return;
        }
        try {
            cameraManager.openDriver(surfaceHolder);
            // Creating the handler starts the preview, which can also throw a RuntimeException.
            if (handler == null) {
                handler = new BarCodeScannerHandler(this, decodeFormats, decodeHints, characterSet, cameraManager);
            }
            decodeOrStoreSavedBitmap(null, null);
        } catch (IOException ioe) {
            Log.w(TAG, ioe);
        } catch (RuntimeException e) {
            // Barcode Scanner has seen crashes in the wild of this variety:
            // java.?lang.?RuntimeException: Fail to connect to camera service
            Log.w(TAG, "Unexpected error initializing camera", e);
        }

    }

    public void restartPreviewAfterDelay(long delayMS) {    	
        if (handler != null) {
            handler.sendEmptyMessageDelayed(RESTART_PREVIEW, delayMS);
        }
    }

    public void drawViewfinder() {
        viewfinderView.drawViewfinder();
    }
    
    public void stopPreview() {
    	cameraManager.stopPreview();
    }
    
    public void startPreview() {
    	cameraManager.startPreview();
    }
    
    public Bitmap getResultBitmap() {
    	return resultBitmap;
    }
    
    public boolean isPhotoCapture() {
    	return isPhotoCapture;
    }
    
    public boolean isPreviewStopped() {
    	return !cameraManager.getPreviewing();
    }
    
    public byte[] getPhotoFromCamera() {
    	return cameraManager.photoFromCamera;
    }
    
    public void resetFlashView() {
    	if (isTurningFlash) {
    		flashView.setVisibility(View.VISIBLE);
			flashLoading.setVisibility(View.INVISIBLE);		
			isTurningFlash = false;
			isTorch = !isTorch;
    	}
    }
    
}

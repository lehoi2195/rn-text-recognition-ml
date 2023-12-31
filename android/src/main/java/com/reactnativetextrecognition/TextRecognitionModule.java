package com.reactnativetextrecognition;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.module.annotations.ReactModule;

// Deps
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import java.io.IOException;
import java.net.URL;

// ML
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

@ReactModule(name = TextRecognitionModule.NAME)
public class TextRecognitionModule extends ReactContextBaseJavaModule {
  public static final String NAME = "TextRecognition";
  private final ReactApplicationContext reactContext;

  public TextRecognitionModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;
  }

  @Override
  @NonNull
  public String getName() {
    return NAME;
  }


  @ReactMethod
  public void recognize(String imgPath, Promise promise) {
    Log.v(getName(), "image path: " + imgPath);

    try {
      if (imgPath != null) {
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        InputImage image = InputImage.fromFilePath(this.reactContext, android.net.Uri.parse(imgPath));

        Task<Text> result =
          recognizer.process(image)
            .addOnSuccessListener(new OnSuccessListener<Text>() {
              @Override
              public void onSuccess(Text visionText) {
                Log.v(getName(), visionText.getText());
                promise.resolve(getDataAsArray(visionText));
              }
            })
            .addOnFailureListener(
              new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                  Log.w(getName(), e);
                  promise.reject("something went wrong", e.getMessage());
                }
              });
      } else {
        throw new IOException("Could not decode a file path into a bitmap.");
      }
    }
    catch(Exception e) {
      Log.w(getName(), e.toString(), e);
      promise.reject("something went wrong", e.getMessage());
    }
  }

  @ReactMethod
  public void recognizeText(String imgPath, Promise promise) {
    Log.v(getName(), "image path: " + imgPath);

    try {
      if (imgPath != null) {
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        InputImage image = InputImage.fromFilePath(this.reactContext, android.net.Uri.parse(imgPath));

        Task<Text> result =
          recognizer.process(image)
            .addOnSuccessListener(new OnSuccessListener<Text>() {
              @Override
              public void onSuccess(Text visionText) {
                Log.v(getName(), visionText.getText());
                WritableArray textBlocks = new WritableNativeArray();
                for (Text.TextBlock block : visionText.getTextBlocks()) {
                  textBlocks.pushString(block.getText());
                }
                promise.resolve(textBlocks);

              }
            })
            .addOnFailureListener(
              new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                  Log.w(getName(), e);
                  promise.reject("something went wrong", e.getMessage());
                }
              });
      } else {
        throw new IOException("Could not decode a file path into a bitmap.");
      }
    }
    catch(Exception e) {
      Log.w(getName(), e.toString(), e);
      promise.reject("something went wrong", e.getMessage());
    }
  }

  private WritableArray getDataAsArray(Text visionText) {
    WritableArray data = Arguments.createArray();

    for (Text.TextBlock block: visionText.getTextBlocks()) {
      WritableArray blockElements = Arguments.createArray();

      for (Text.Line line: block.getLines()) {
        WritableArray lineElements = Arguments.createArray();
        for (Text.Element element: line.getElements()) {
          WritableMap e = Arguments.createMap();
          e.putString("text_element", element.getText());
          lineElements.pushMap(e);
        }

        WritableMap l = Arguments.createMap();
        l.putString("text_line", line.getText());
        l.putArray("elements", lineElements);

        blockElements.pushMap(l);
      }

      WritableMap info = Arguments.createMap();
      info.putString("text_block", block.getText());
      info.putArray("lines", blockElements);
      data.pushMap(info);
    }

    return data;
  }
}

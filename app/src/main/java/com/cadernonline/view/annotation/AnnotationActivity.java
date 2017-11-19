package com.cadernonline.view.annotation;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.view.menu.MenuPopupHelper;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.cadernonline.R;
import com.cadernonline.event.UpdateAnnotationsEvent;
import com.cadernonline.model.Annotation;
import com.cadernonline.model.Discipline;
import com.cadernonline.util.Callback;
import com.cadernonline.view.BaseActivity;
import com.kbeanie.multipicker.api.AudioPicker;
import com.kbeanie.multipicker.api.CameraImagePicker;
import com.kbeanie.multipicker.api.CameraVideoPicker;
import com.kbeanie.multipicker.api.ImagePicker;
import com.kbeanie.multipicker.api.Picker;
import com.kbeanie.multipicker.api.VideoPicker;
import com.kbeanie.multipicker.api.callbacks.AudioPickerCallback;
import com.kbeanie.multipicker.api.callbacks.ImagePickerCallback;
import com.kbeanie.multipicker.api.callbacks.VideoPickerCallback;
import com.kbeanie.multipicker.api.entity.ChosenAudio;
import com.kbeanie.multipicker.api.entity.ChosenImage;
import com.kbeanie.multipicker.api.entity.ChosenVideo;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.greenrobot.eventbus.EventBus;
import org.wordpress.aztec.Aztec;
import org.wordpress.aztec.AztecAttributes;
import org.wordpress.aztec.AztecText;
import org.wordpress.aztec.Html;
import org.wordpress.aztec.ITextFormat;
import org.wordpress.aztec.glideloader.GlideImageLoader;
import org.wordpress.aztec.glideloader.GlideVideoThumbnailLoader;
import org.wordpress.aztec.plugins.shortcodes.AudioShortcodePlugin;
import org.wordpress.aztec.plugins.shortcodes.CaptionShortcodePlugin;
import org.wordpress.aztec.plugins.shortcodes.VideoShortcodePlugin;
import org.wordpress.aztec.plugins.shortcodes.handlers.CaptionHandler;
import org.wordpress.aztec.plugins.wpcomments.toolbar.PageToolbarButton;
import org.wordpress.aztec.toolbar.AztecToolbar;
import org.wordpress.aztec.toolbar.IAztecToolbarClickListener;
import org.wordpress.aztec.watchers.BlockElementWatcher;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cafe.adriel.androidaudioconverter.AndroidAudioConverter;
import cafe.adriel.androidaudioconverter.callback.IConvertCallback;
import cafe.adriel.androidaudioconverter.model.AudioFormat;
import cafe.adriel.androidaudiorecorder.AndroidAudioRecorder;
import cafe.adriel.androidaudiorecorder.model.AudioChannel;
import cafe.adriel.androidaudiorecorder.model.AudioSampleRate;
import cafe.adriel.androidaudiorecorder.model.AudioSource;
import id.zelory.compressor.Compressor;

public class AnnotationActivity extends BaseActivity implements
        IAztecToolbarClickListener, AztecText.OnImageTappedListener,
        AztecText.OnVideoTappedListener, AztecText.OnAudioTappedListener,
        ImagePickerCallback, VideoPickerCallback, AudioPickerCallback {
    private static final String KEY_DISCIPLINE = "discipline";
    private static final String KEY_ANNOTATION = "annotation";

    private static final int REQUEST_RECORD_AUDIO = 0;

    @BindView(R.id.toolbar)
    Toolbar vToolbar;
    @BindView(R.id.format_toolbar)
    AztecToolbar vFormatToolbar;
    @BindView(R.id.text)
    AztecText vText;

    private Discipline discipline;
    private Annotation annotation;

    private Aztec aztec;
    private GlideVideoThumbnailLoader videoThumbGetter;
    private MenuItem editMenuItem;

    private CameraImagePicker cameraImagePicker;
    private ImagePicker galleryImagePicker;
    private CameraVideoPicker recordVideoPicker;
    private VideoPicker galleryVideoPicker;
    private AudioPicker galleryAudioPicker;
    private String wavAudioPath = Environment.getExternalStorageDirectory() + "/recorded_audio.wav";
    private String mp3AudioPath = Environment.getExternalStorageDirectory() + "/recorded_audio.mp3";

    public static void start(Context context, Discipline discipline, Annotation annotation) {
        Intent i = new Intent(context, AnnotationActivity.class);
        i.putExtra(KEY_DISCIPLINE, discipline);
        i.putExtra(KEY_ANNOTATION, annotation);
        context.startActivity(i);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_annotation);
        ButterKnife.bind(this);

        discipline = getIntent().getParcelableExtra(KEY_DISCIPLINE);
        annotation = getIntent().getParcelableExtra(KEY_ANNOTATION);

        if(annotation == null){
            annotation = ParseObject.create(Annotation.class);
            annotation.setSubject(getString(R.string.untitled));
            annotation.setText("");
            final EditText vSubject = new EditText(this);
            new AlertDialog.Builder(this)
                    .setTitle(R.string.title)
                    .setView(vSubject)
                    .setPositiveButton(R.string.save, (dialog, which) -> {
                        String subject = vSubject.getText().toString();
                        if(!TextUtils.isEmpty(subject)) {
                            annotation.setSubject(subject);
                            getSupportActionBar().setTitle(annotation.getSubject());
                        }
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .create()
                    .show();
        }

        setSupportActionBar(vToolbar);
        getSupportActionBar().setTitle(annotation.getSubject());
        getSupportActionBar().setSubtitle(discipline.getName());
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle videoExtras = new Bundle();
        videoExtras.putInt(MediaStore.EXTRA_DURATION_LIMIT, 2 * 60);
        videoExtras.putInt(MediaStore.EXTRA_VIDEO_QUALITY, 0);
        cameraImagePicker = new CameraImagePicker(this);
        cameraImagePicker.setImagePickerCallback(this);
        cameraImagePicker.shouldGenerateThumbnails(false);
        galleryImagePicker = new ImagePicker(this);
        galleryImagePicker.setImagePickerCallback(this);
        galleryImagePicker.shouldGenerateThumbnails(false);
        recordVideoPicker = new CameraVideoPicker(this);
        recordVideoPicker.setVideoPickerCallback(this);
        recordVideoPicker.setExtras(videoExtras);
        galleryVideoPicker = new VideoPicker(this);
        galleryVideoPicker.setVideoPickerCallback(this);
        galleryAudioPicker = new AudioPicker(this);
        galleryAudioPicker.setAudioPickerCallback(this);

        videoThumbGetter = new GlideVideoThumbnailLoader(this);
        aztec = Aztec.with(vText, vFormatToolbar, this)
                .setImageGetter(new GlideImageLoader(this))
                .setVideoThumbnailGetter(videoThumbGetter)
                .setOnImageTappedListener(this)
                .setOnVideoTappedListener(this)
                .setOnAudioTappedListener(this)
                .addPlugin(new CaptionShortcodePlugin())
                .addPlugin(new VideoShortcodePlugin())
                .addPlugin(new AudioShortcodePlugin())
                .addPlugin(new PageToolbarButton(vText));

        new BlockElementWatcher(vText)
                .add(new CaptionHandler(vText))
                .install(vText);

        vText.setTextColor(Color.BLACK);
        vText.fromHtml(annotation.getText());
        vText.post(() -> vText.setFocusable(false));
    }

    @Override
    public void onBackPressed() {
        if(isTextChanged()){
            showConfirmExitDialog();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_annotation, menu);
        editMenuItem = menu.findItem(R.id.action_edit);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if(isTextChanged()){
                    showConfirmExitDialog();
                } else {
                    supportFinishAfterTransition();
                }
                break;
            case R.id.action_edit:
                toggleEdit();
                break;
            case R.id.action_save:
                save(false);
                break;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK) {
            switch (requestCode){
                case Picker.PICK_IMAGE_CAMERA: cameraImagePicker.submit(data); break;
                case Picker.PICK_IMAGE_DEVICE: galleryImagePicker.submit(data); break;
                case Picker.PICK_VIDEO_CAMERA: recordVideoPicker.submit(data); break;
                case Picker.PICK_VIDEO_DEVICE: galleryVideoPicker.submit(data); break;
                case Picker.PICK_AUDIO: galleryAudioPicker.submit(data); break;
                case REQUEST_RECORD_AUDIO:
                    convertAudioToMp3(wavAudioPath, filePath ->
                            uploadFile(filePath, url -> {
                                vText.append("\n[audio src=\""+url+"\"]\n");
                                vText.fromHtml(vText.toFormattedHtml());
                            }));
                    break;
            }
        }
    }

    @Override
    public void onImageTapped(AztecAttributes aztecAttributes, int i, int i1) {
        String url = aztecAttributes.getValue("src");
        if(!TextUtils.isEmpty(url)) {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                intent.setDataAndType(Uri.parse(url), "image/*");
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                try {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(browserIntent);
                } catch (ActivityNotFoundException e2) { }
            }
        }
    }

    @Override
    public void onVideoTapped(AztecAttributes aztecAttributes) {
        String url = aztecAttributes.getValue("src");
        if(!TextUtils.isEmpty(url)) {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                intent.setDataAndType(Uri.parse(url), "video/*");
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                try {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(browserIntent);
                } catch (ActivityNotFoundException e2) { }
            }
        }
    }

    @Override
    public void onAudioTapped(AztecAttributes aztecAttributes) {
        String url = aztecAttributes.getValue("src");
        if(!TextUtils.isEmpty(url)){
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                intent.setDataAndType(Uri.parse(url), "audio/*");
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                try {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(browserIntent);
                } catch (ActivityNotFoundException e2) { }
            }
        }
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onToolbarMediaButtonClicked() {
        PopupMenu mediaMenu = new PopupMenu(this, aztec.getToolbar(), Gravity.BOTTOM);
        mediaMenu.inflate(R.menu.menu_media);
        mediaMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.action_add_image: showAddImageOptions(); break;
                case R.id.action_add_video: showAddVideoOptions(); break;
                case R.id.action_add_audio: showAddAudioOptions(); break;
            }
            return true;
        });

        MenuPopupHelper popupHelper = new MenuPopupHelper(this, (MenuBuilder) mediaMenu.getMenu(), aztec.getToolbar());
        popupHelper.setForceShowIcon(true);
        popupHelper.show();
    }

    @Override
    public void onToolbarCollapseButtonClicked() {

    }

    @Override
    public void onToolbarExpandButtonClicked() {

    }

    @Override
    public void onToolbarFormatButtonClicked(ITextFormat iTextFormat, boolean b) {

    }

    @Override
    public void onToolbarHeadingButtonClicked() {

    }

    @Override
    public void onToolbarHtmlButtonClicked() {

    }

    @Override
    public void onToolbarListButtonClicked() {

    }

    @Override
    public void onImagesChosen(List<ChosenImage> list) {
        if(!list.isEmpty()) {
            String imagePath = list.get(0).getOriginalPath();
            try {
                File compressedFile = new Compressor(this)
                        .setMaxWidth(1024)
                        .setMaxHeight(1024)
                        .setQuality(90)
                        .setCompressFormat(Bitmap.CompressFormat.JPEG)
                        .setDestinationDirectoryPath(Environment.getExternalStoragePublicDirectory(
                                Environment.DIRECTORY_PICTURES).getAbsolutePath())
                        .compressToFile(new File(imagePath));
                uploadFile(compressedFile.getPath(), url -> Glide.with(this)
                        .load(url)
                        .into(new SimpleTarget<GlideDrawable>() {
                            @Override
                            public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
                                AztecAttributes attrs = new AztecAttributes();
                                attrs.setValue("src", url);
                                vText.append("\n");
                                vText.insertImage(resource, attrs);
                                vText.append("\n");
                            }
                        }));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onVideosChosen(List<ChosenVideo> list) {
        if(!list.isEmpty()) {
            String videoPath = list.get(0).getOriginalPath();
            uploadFile(videoPath, url -> {
                videoThumbGetter.loadVideoThumbnail(url, new Html.VideoThumbnailGetter.Callbacks() {
                    @Override
                    public void onThumbnailLoaded(Drawable drawable) {
                        AztecAttributes attrs = new AztecAttributes();
                        attrs.setValue("src", url);
                        vText.append("\n");
                        vText.insertVideo(drawable, attrs);
                        vText.append("\n");
                    }
                    @Override
                    public void onThumbnailLoading(Drawable drawable) { }
                    @Override
                    public void onThumbnailFailed() { }
                }, 800);
            });
        }
    }

    @Override
    public void onAudiosChosen(List<ChosenAudio> list) {
        if(!list.isEmpty()) {
            String audioPath = list.get(0).getOriginalPath();
            uploadFile(audioPath, url -> {
                vText.append("\n[audio src=\""+url+"\"]\n");
                vText.fromHtml(vText.toFormattedHtml());
            });
        }
    }

    @Override
    public void onError(String error) {
        showError(error);
    }

    private void toggleEdit(){
        vText.setFocusable(!vText.isFocusable());
        if(vText.isFocusable()){
            vText.setFocusableInTouchMode(true);
            vFormatToolbar.setVisibility(View.VISIBLE);
            if(editMenuItem != null){
                editMenuItem.setTitle(R.string.visualize);
                editMenuItem.setIcon(R.drawable.ic_remove_red_eye);
            }
        } else {
            vText.setFocusableInTouchMode(false);
            vFormatToolbar.setVisibility(View.GONE);
            if(editMenuItem != null){
                editMenuItem.setTitle(R.string.edit);
                editMenuItem.setIcon(R.drawable.ic_mode_edit);
            }
        }
    }

    private void save(boolean closeApp){
        String subject = getSupportActionBar().getTitle().toString();
        String text = getFormattedText();
        annotation.setSubject(subject);
        annotation.setText(text);
        annotation.setDiscipline(discipline);
        annotation.setUser(ParseUser.getCurrentUser());
        annotation.saveInBackground(e -> {
            if(e == null){
                EventBus.getDefault().postSticky(new UpdateAnnotationsEvent());
                Toast.makeText(this, R.string.annotation_saved, Toast.LENGTH_SHORT).show();
                vText.fromHtml(annotation.getText());
                if(closeApp){
                    finish();
                }
            } else {
                showError(e.getMessage());
            }
        });
    }

    private void uploadFile(String filePath, Callback<String> callback){
        showProgress();
        ParseFile mediaFile = new ParseFile(new File(filePath));
        mediaFile.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e == null){
                    callback.run(mediaFile.getUrl());
                } else {
                    showError(e.getMessage());
                }
                hideProgress();
            }
        });
    }

    private void showAddImageOptions(){
        new AlertDialog.Builder(this)
                .setTitle(R.string.add_image)
                .setItems(R.array.add_image_options, (dialog, which) -> {
                    switch (which){
                        case 0: cameraImagePicker.pickImage(); break;
                        case 1: galleryImagePicker.pickImage(); break;
                    }
                })
                .create()
                .show();
    }

    private void showAddVideoOptions(){
        new AlertDialog.Builder(this)
                .setTitle(R.string.add_video)
                .setItems(R.array.add_video_options, (dialog, which) -> {
                    switch (which){
                        case 0: recordVideoPicker.pickVideo(); break;
                        case 1: galleryVideoPicker.pickVideo(); break;
                    }
                })
                .create()
                .show();
    }

    private void showAddAudioOptions(){
        new AlertDialog.Builder(this)
                .setTitle(R.string.add_audio)
                .setItems(R.array.add_audio_options, (dialog, which) -> {
                    switch (which){
                        case 0: recordAudio(); break;
                        case 1: galleryAudioPicker.pickAudio(); break;
                    }
                })
                .create()
                .show();
    }

    private void showConfirmExitDialog(){
        new AlertDialog.Builder(this)
                .setMessage(R.string.save_changes_before_exit)
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    save(true);
                })
                .setNegativeButton(R.string.no, null)
                .create()
                .show();
    }

    private void recordAudio(){
        AndroidAudioRecorder.with(this)
                .setFilePath(wavAudioPath)
                .setColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimary, getTheme()))
                .setRequestCode(REQUEST_RECORD_AUDIO)
                .setSource(AudioSource.MIC)
                .setChannel(AudioChannel.STEREO)
                .setSampleRate(AudioSampleRate.HZ_44100)
                .setKeepDisplayOn(true)
                .record();
    }

    private void convertAudioToMp3(String audioPath, Callback<String> callback){
        showProgress();
        AndroidAudioConverter.with(this)
                .setFile(new File(audioPath))
                .setFormat(AudioFormat.MP3)
                .setCallback(new IConvertCallback() {
                    @Override
                    public void onSuccess(File file) {
                        hideProgress();
                        callback.run(file.getPath());
                    }
                    @Override
                    public void onFailure(Exception e) {
                        showError(e.getMessage());
                        hideProgress();
                    }
                })
                .convert();
    }

    private String getFormattedText(){
        return vText.toFormattedHtml()
                .replaceAll("\\[video src", "<video controls src")
                .replaceAll("\\[video controls=\"controls\" src", "<video controls src")
                .replaceAll("\\[video controls=\"\" src", "<video controls src")
                .replaceAll("\\[audio src", "<audio controls src")
                .replaceAll("\\[audio controls=\"controls\" src", "<audio controls src")
                .replaceAll("\\[audio controls=\"\" src", "<audio controls src")
                .replaceAll("\"]", "\"/>");
    }

    private boolean isTextChanged(){
        return !annotation.getText().equals(getFormattedText());
    }
}
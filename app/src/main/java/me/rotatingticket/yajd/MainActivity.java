package me.rotatingticket.yajd;

import android.app.SearchManager;
import android.app.SearchableInfo;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import com.github.clans.fab.FloatingActionButton;
import com.googlecode.tesseract.android.TessBaseAPI;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.List;

import me.rotatingticket.yajd.dict.core.WordEntry;
import me.rotatingticket.yajd.repository.FullTextTranslateRepository;
import me.rotatingticket.yajd.service.ClipboardTranslationService;
import me.rotatingticket.yajd.service.ScreenTranslationService;
import me.rotatingticket.yajd.util.WordEntryAdapter;
import me.rotatingticket.yajd.util.zinnia.Character;
import me.rotatingticket.yajd.view.CandidateWordEntryView;
import me.rotatingticket.yajd.view.CanvasView;
import me.rotatingticket.yajd.view.FloatingWindow;
import me.rotatingticket.yajd.viewmodel.FullTextTranslateActivityViewModel;
import me.rotatingticket.yajd.viewmodel.MainActivityViewModel;
import me.rotatingticket.yajd.webservice.BingTranslatorWebservice;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TESS_LANG = "jpn";
    private static final String TESS_DATA_PATH = "tessdata/jpn.traineddata";
    private CardView candidatesCardView;
    private CanvasView canvasView;
    private RecyclerView handwritingCandidatesView;

    private EditText searchViewSrcEditText;
    private WordEntryAdapter wordEntryAdapter;

    private MainActivityViewModel viewModel;
    private Character handwritingCharacter;
    private HandwritingCandidatesAdapter handwritingCandidatesAdapter;
    private WindowManager windowManager;
    private FloatingWindow floatingView;
    private TessBaseAPI tessBaseAPI;
    private ImageReader imageReader;
    private VirtualDisplay virtualDisplay;
    private MediaProjectionManager mediaProjectionManager;
    private static final int PROJECTION_REQUEST_CODE = 222;
    private Thread thread;
    private boolean processing;
    private Handler mainHandler;

    private static class HandwritingCandidatesAdapter extends RecyclerView.Adapter {

        static class HandwritingCandidatesViewHolder extends RecyclerView.ViewHolder {
            TextView view;

            HandwritingCandidatesViewHolder(View itemView,
                                            View.OnClickListener onClickListener) {
                super(itemView);
                view = (TextView) itemView;
                view.setOnClickListener(onClickListener);
            }
        }

        private List<String> candidates;
        private LayoutInflater layoutInflater;
        private View.OnClickListener itemOnClickListener;

        public HandwritingCandidatesAdapter(List<String> candidates,
                                            Context context,
                                            View.OnClickListener onClickListener) {
            super();
            this.candidates = candidates;
            this.layoutInflater = LayoutInflater.from(context);
            this.itemOnClickListener = onClickListener;
        }

        public void setCandidates(List<String> candidates) {
            this.candidates = candidates;
            notifyDataSetChanged();
        }

        @Override
        public HandwritingCandidatesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            TextView itemView = (TextView) layoutInflater.inflate(R.layout.item_handwriting_candidates, parent, false);
            return new HandwritingCandidatesViewHolder(itemView, itemOnClickListener);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            ((HandwritingCandidatesViewHolder)holder).view.setText(candidates.get(position));
        }

        @Override
        public int getItemCount() {
            if (candidates != null) {
                return candidates.size();
            } else {
                return 0;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mainHandler = new Handler(Looper.getMainLooper());

        setContentView(R.layout.activity_main);

        viewModel = ViewModelProviders.of(this).get(MainActivityViewModel.class);
        candidatesCardView = findViewById(R.id.candidates_card_view);

        ListView candidatesView = findViewById(R.id.candidates_view);
        setUpCandidatesView(candidatesView);

        SearchView searchView = findViewById(R.id.search_view);
        setUpSearchView(searchView);

        CardView handwritingView = findViewById(R.id.handwriting_view);
        setUpHandwriting(handwritingView);

        View fabMenu = findViewById(R.id.fab_menu);
        setUpFabMenu(fabMenu);

        startClipboardTranslationService();
    }

    private void startClipboardTranslationService() {
        Intent intent = new Intent(this, ClipboardTranslationService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
    }

    /**
     * Prepare the candidate list view.
     * Set the observer of viewModel's getCandidates to update the ListView.
     * @param candidatesView the candidate list view.
     */
    private void setUpCandidatesView(ListView candidatesView) {
        candidatesView.setOnItemClickListener((parent, view, position, id) -> {
            WordEntry wordEntry = wordEntryAdapter.getItem(position);
            launchLookUpResultActivity(wordEntry);
        });

        viewModel.getCandidates().observe(this, candidates -> {
            // only show the candidate list view card if have candidates.
            candidatesCardView.setVisibility(
                  candidates == null || candidates.size() == 0 ? View.INVISIBLE : View.VISIBLE
            );
            if (wordEntryAdapter == null) {
                // create the adapter first time the candidates changed.
                wordEntryAdapter = new WordEntryAdapter(
                      this,
                      CandidateWordEntryView.class,
                      R.layout.search_candidate,
                      candidates);
                candidatesView.setAdapter(wordEntryAdapter);
            } else {
                wordEntryAdapter.setList(candidates);
            }
        });
    }

    /**
     * Prepare the search view.
     * Trigger the query for search suggestion when the query string changed.
     * @param searchView the search view.
     */
    private void setUpSearchView(SearchView searchView) {
        Resources resources = getResources();

        // store the EditText in the search view
        int searchViewSrcId = resources.getIdentifier("@android:id/search_src_text", null, null);
        searchViewSrcEditText = searchView.findViewById(searchViewSrcId);

        int searchViewCloseId = resources.getIdentifier("@android:id/search_close_btn", null, null);
        ImageView closeButton = searchView.findViewById(searchViewCloseId);
        closeButton.setOnClickListener(v -> {
            if (searchViewSrcEditText.getText().length() == 0) {
                return;
            }
            searchViewSrcEditText.setText("");
            if (!viewModel.getHandWritingToggledRealValue()) {
                searchViewSrcEditText.requestFocus();
                enableSearchViewIme();
            }
        });

        // setup search config
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        assert searchManager != null;
        SearchableInfo searchableInfo = searchManager.getSearchableInfo(new ComponentName(this, LookUpResultActivity.class));
        searchView.setSearchableInfo(searchableInfo);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // do not care the submit event
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                viewModel.query(newText);
                return true;
            }
        });
    }

    /**
     * Close the IME popover.
     */
    private void closeSearchViewIme() {
        searchViewSrcEditText.clearFocus();
    }

    /**
     * Disable IME popover on the SearchView.
     * @param close true if you want to close the popover now.
     */
    private void disableSearchViewIme(boolean close) {
        searchViewSrcEditText.setShowSoftInputOnFocus(false);
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        assert imm != null;
        imm.hideSoftInputFromWindow(searchViewSrcEditText.getWindowToken(), 0);

        if (close) {
            closeSearchViewIme();
        }
    }

    /**
     * Enable and Open the IME popover.
     */
    private void enableSearchViewIme() {
        searchViewSrcEditText.setShowSoftInputOnFocus(true);

        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        assert imm != null;
        imm.showSoftInput(searchViewSrcEditText, 0);
    }

    /**
     * Launch a LookUpResultActivity to display a WordEntry.
     * @param wordEntry The WordEntry to display.
     */
    private void launchLookUpResultActivity(WordEntry wordEntry) {
        Intent intent = new Intent(this, LookUpResultActivity.class);
        intent.setAction(Intent.ACTION_VIEW);
        Uri data = new Uri.Builder()
              .appendQueryParameter(LookUpResultActivity.VIEW_ACTION_DATA_KEY, wordEntry.getWord())
              .build();
        intent.setData(data);
        startActivity(intent);
    }

    /**
     * Toggle the handwriting panel.
     * @param view Clicked view.
     */
    public void toggleHandwriting(View view) {
        viewModel.toggleHandwriting();
    }

    /**
     * Setup the handwriting.
     * @param handwritingView
     */
    private void setUpHandwriting(View handwritingView) {
        canvasView = handwritingView.findViewById(R.id.canvas);

        // on panel toggled
        viewModel.getHandwritingToggled().observe(this, handwritingToggled -> {
            assert handwritingToggled != null;
            if (handwritingToggled) {
                // disable IME and display the handwriting panel
                disableSearchViewIme(true);
                launchHandwriting(handwritingView);
            } else {
                // enable IME and close the handwriting panel
                enableSearchViewIme();
                closeHandwriting(handwritingView);
            }
        });

        // setup handwriting candidates list
        handwritingCandidatesView = handwritingView.findViewById(R.id.handwriting_candidates_view);
        handwritingCandidatesAdapter = new HandwritingCandidatesAdapter(
              null,
              getApplicationContext(),
              v -> {
                  insertText(((TextView)v).getText().toString());
                  refreshHandwriting();
              });
        handwritingCandidatesView.setAdapter(handwritingCandidatesAdapter);

        // on handwriting candidates changed
        viewModel.getHandwritingCandidates().observe(this, handwritingResult -> {
            // invisible if there is no handwriting candidates
            handwritingCandidatesView.setVisibility(
                  handwritingResult == null || handwritingResult.size() == 0 ? View.GONE : View.VISIBLE
            );
            // update the display
            handwritingCandidatesAdapter.setCandidates(handwritingResult);
        });

        // process the stroke event on the canvas
        canvasView.setOnStrokeListener(new CanvasView.OnStrokeListener() {
            @Override
            public boolean onStrokeBegin(float x, float y) {
                // perpare the character if there is no one.
                if (handwritingCharacter == null) {
                    prepareCharacter();
                }
                handwritingCharacter.beginStroke((int)x, (int)y);
                return false;
            }

            @Override
            public boolean onStrokeMove(float x, float y) {
                handwritingCharacter.draw((int)x, (int)y);
                return false;
            }

            @Override
            public boolean onStrokeEnd(float x, float y) {
                handwritingCharacter.endStroke((int)x, (int)y);
                // recognize (at background)
                viewModel.recognizeCharacter(handwritingCharacter);
                return false;
            }
        });
    }

    /**
     * Insert target text to the SearchView after cursor.
     * @param text Target text.
     */
    public void insertText(String text) {
        searchViewSrcEditText.getText().insert(searchViewSrcEditText.getSelectionStart(), text);
    }

    /**
     * Launch the handwriting panel.
     * @param handWritingView The panel view.
     */
    public void launchHandwriting(View handWritingView) {
        handWritingView.setVisibility(View.VISIBLE);
    }

    /**
     * Finalize current character (If there is one). Then prepare a new character.
     */
    private void prepareCharacter() {
        if (handwritingCharacter != null) {
            handwritingCharacter.close();
        }
        handwritingCharacter = new Character();
        handwritingCharacter.setWidth(canvasView.getWidth());
        handwritingCharacter.setHeight(canvasView.getHeight());
    }

    /**
     * Refresh the handwriting for next input.
     */
    public void refreshHandwriting() {
        clearHandwriting();
        prepareCharacter();
    }

    /**
     * Clear the handwriting panel and corresponding data.
     */
    public void clearHandwriting() {
        canvasView.clear();
        clearHandwritingCandidates();
        if (handwritingCharacter != null) {
            handwritingCharacter.clear();
            handwritingCharacter = null;
        }
    }

    /**
     * Close the handwriting panel.
     * @param handwritingView The panel view.
     */
    public void closeHandwriting(View handwritingView) {
        handwritingView.setVisibility(View.GONE);
        clearHandwriting();
    }

    /**
     * Clear the handwriting candidates.
     */
    void clearHandwritingCandidates() {
        handwritingCandidatesAdapter.setCandidates(null);
        handwritingCandidatesView.setVisibility(View.GONE);
    }

    /**
     * Setup Floating Action Button Menu.
     * @param fabMenu View of target floating action button menu.
     */
    private void setUpFabMenu(View fabMenu) {
        View jumpToTranslateBtn = fabMenu.findViewById(R.id.btn_jump_to_translate);
        jumpToTranslateBtn.setOnClickListener(
              view -> startActivity(new Intent(this, FullTextTranslateActivity.class))
        );

        FloatingActionButton startScreenTranslateBtn = fabMenu.findViewById(R.id.btn_start_screen_translation);

        mediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);

        startScreenTranslateBtn.setOnClickListener(view -> {
            if (!Settings.canDrawOverlays(getApplicationContext())) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent,1000);
            } else {
                Intent intent = mediaProjectionManager.createScreenCaptureIntent();
                startActivityForResult(intent, PROJECTION_REQUEST_CODE);
            }
        });
    }

    private void prepareScreenTranslation() {
        if (floatingView == null) {
            createFloatingWindow();
        }
        if (ScreenTranslationService.getOnAccessibilityEventListener() == null) {
            ScreenTranslationService.setOnAccessibilityEventListener(event -> {
                Log.e("YAJD_S", String.format("%s %s %s %s", String.valueOf(event.getAction()), String.valueOf(floatingView), String.valueOf(floatingView != null && floatingView.isCapturing()), String.valueOf(event)));
                if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED && floatingView != null && floatingView.isCapturing()) {
                    Log.e("YAJD_ASS", event.getPackageName().toString());
                    try {
                        startOcr(event.getEventTime());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                Log.e("YAJD", event.getPackageName().toString());
            });
        }
        Log.e("YAJD", String.valueOf(ScreenTranslationService.getOnAccessibilityEventListener()));
    }

    private void createFloatingWindow() {
        floatingView = (FloatingWindow) getLayoutInflater().inflate(R.layout.floating_window, null, false);
        floatingView.setOnLayoutParamsChangedListener((WindowManager.LayoutParams layoutParams) -> {
            Log.e("YAJD_LAYOUTS", String.format("%d %d", layoutParams.x, layoutParams.y));
            windowManager.updateViewLayout(floatingView, layoutParams);
            floatingView.invalidate();
        });

        windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        assert windowManager != null;

        int windowType = WindowManager.LayoutParams.TYPE_PHONE;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            windowType = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }
        WindowManager.LayoutParams floatingLayoutParams = new WindowManager.LayoutParams(
              600,
              600,
              0,
              0,
              windowType,
              WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
              PixelFormat.RGBA_8888);
        floatingLayoutParams.gravity = Gravity.LEFT | Gravity.TOP;
        floatingView.setParam(floatingLayoutParams);
        windowManager.addView(floatingView, floatingLayoutParams);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        prepareScreenTranslation();
        prepareMediaProjection(resultCode, data);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }


    private void prepareTesseract() throws IOException {
        prepareTessData(this);
        tessBaseAPI = new TessBaseAPI();
        String tessLangPath = getFilesDir().getAbsolutePath();
        tessBaseAPI.init(tessLangPath, TESS_LANG);
    }

    private void prepareTessData(@NonNull Context context) throws IOException {
        File file = new File(context.getFilesDir(), TESS_DATA_PATH);
        if (file.exists()) {
            return;
        }
        initializeTessData(context);
    }

    private void initializeTessData(@NonNull Context context) throws IOException {
        File folder = new File(context.getFilesDir(), "tessdata");
        folder.mkdirs();
        InputStream inputStream = context.getResources().openRawResource(R.raw.jpn);
        File internalFile = new File(context.getFilesDir(), TESS_DATA_PATH);
        OutputStream outputStream = new FileOutputStream(internalFile);
        IOUtils.copy(inputStream, outputStream);
    }

    private void prepareMediaProjection(int resultCode, Intent resultData) {
        Point displaySize = new Point();
        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        assert windowManager != null;
        windowManager.getDefaultDisplay().getRealSize(displaySize);

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();

        mediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        assert mediaProjectionManager != null;
        MediaProjection mediaProjection = mediaProjectionManager .getMediaProjection(resultCode, resultData);
        imageReader = ImageReader.newInstance(displaySize.x, displaySize.y, PixelFormat.RGBA_8888, 2);
        virtualDisplay = mediaProjection.createVirtualDisplay(
              "SCREEN_CAPTURE_TRANSLATION",
              displaySize.x,
              displaySize.y,
              displayMetrics.densityDpi,
              DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
              imageReader.getSurface(),
              null,
              null);
    }

    private void startOcr(long timestamp) throws IOException {
        if (processing) {
            return;
        }
        processing = true;
        Log.e("YAJD_OCR_TIME", String.format("%d %d %d", timestamp - SystemClock.uptimeMillis(), timestamp, SystemClock.uptimeMillis()));
        if (SystemClock.uptimeMillis() - timestamp > 200) {
            Log.e("YAJD_OCR", "SLOW EVENT");
            return;
        }

        prepareTesseract();
        Log.e("YAJD_OCR", "started");
        if (imageReader == null) {
            Log.e("YAJD_OCR", "NULL imageReader");
            return;
        }

        int[] coordinate = new int[2];
        floatingView.getLocationOnScreen(coordinate);
        Log.e("YAJD_OCR", String.valueOf(coordinate));
        windowManager.removeView(floatingView);
        AsyncTask.execute(() -> {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            String result;
            try {
                result = OCR(coordinate[0], coordinate[1]);
                Log.e("YAJD_OCR", String.valueOf(result));
                if (result != null && result.length() != 0) {
                    Response<String> response = FullTextTranslateRepository
                          .getInstance(getApplication())
                          .getWebservice()
                          .shortTranslate(result, "ja", "zh-cn")
                          .execute();
                    String translated = response.body();
                    floatingView.setContent(translated);
                    Log.e("YAJD_OCR", "CONTENT_SETTED");
                }
                Log.e("YAJD_OCR", "finished");
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                mainHandler.post(() -> {
                    windowManager.addView(floatingView, floatingView.getLayoutParams());
                    processing = false;
                });
            }
        });
    }

    private synchronized String OCR(int x, int y) {
        Log.e("YAJD_OCR", "inner");
        Image image = imageReader.acquireLatestImage();
        if (image == null) {
            Log.e("YAJD_OCR", "NULL image");
            return null;
        }
        Bitmap fullscreen = imageToBitmap(image);
        if (fullscreen == null) {
            Log.e("YAJD_OCR", "NULL fullscreen");
            return null;
        }
        Bitmap croped = Bitmap.createBitmap(fullscreen, x, y, floatingView.getWidth(), floatingView.getHeight());
        if (croped == null) {
            Log.e("YAJD_OCR", "CROPED null");
            return null;
        }
        tessBaseAPI.setImage(croped);
        return tessBaseAPI.getUTF8Text();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("YAJD_OCR", "DESTROIED");
    }

    private Bitmap imageToBitmap(Image image) throws OutOfMemoryError {
        Image.Plane[] planes = image.getPlanes();
        ByteBuffer buffer = planes[0].getBuffer();
        int pixelStride = planes[0].getPixelStride();
        int rowStride = planes[0].getRowStride();
        int rowPadding = rowStride - pixelStride * image.getWidth();

        Bitmap bitmap;
        try {
            bitmap = Bitmap.createBitmap(image.getWidth() + rowPadding / pixelStride, image.getHeight(), Bitmap.Config.ARGB_8888);
        } catch (NullPointerException e) {
            return null;
        }
        bitmap.copyPixelsFromBuffer(buffer);
        image.close();

        return bitmap;
    }
}

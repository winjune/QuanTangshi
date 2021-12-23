package animalize.github.com.quantangshi.UIPoem;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.sothree.slidinguppanel.PanelSlideListener;
import com.sothree.slidinguppanel.PanelState;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import animalize.github.com.quantangshi.Data.RawPoem;
import animalize.github.com.quantangshi.Database.MyDatabaseHelper;
import animalize.github.com.quantangshi.R;
import animalize.github.com.quantangshi.StudyActivity;


public class OnePoemActivity
        extends AppCompatActivity
        implements PoemController, View.OnClickListener, PanelSlideListener {

    private final static int STUDY_REQ_CODE = 666;

    private final static int NO = 1;
    private final static int NEIGHBOR = 2;
    private final static int RECENT = 3;
    private final static int TAG = 4;

    private RawPoem currentPoem;

    private SlidingUpPanelLayout slider;
    // 供ANCHORED时调整height用
    private FrameLayout swichFrame;

    private PoemView poemView;
    private TagView tagView;
    private RecentView recentView;
    private NeighbourView neighbourView;

    private Button mTButton;
    private Button mSButton;
    private Button mSpButton;

    private int currentView = TAG;
    private boolean collapsed = true;
    private Button neighborButton, recentButton, tagButton;

    private float studyPosi = 0;
    private String[] studyTags;

    public static void actionStart(Context context) {
        Intent i = new Intent(context, OnePoemActivity.class);
        context.startActivity(i);
    }

    public static void actionStart(Context context, int id) {
        Intent i = new Intent(context, OnePoemActivity.class);
        i.putExtra("poem_id", id);
        context.startActivity(i);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poem_main);

        // intent
        Intent intent = getIntent();
        int intentID = intent.getIntExtra("poem_id", -1);

        // 得到 诗view
        poemView = findViewById(R.id.poem_view);
        tagView = findViewById(R.id.tag_view);
        tagView.setPoemController(this);

        recentView = findViewById(R.id.recent_view);
        recentView.setPoemController(this);

        neighbourView = findViewById(R.id.neighbour_view);
        neighbourView.setPoemController(this);

        swichFrame = findViewById(R.id.switch_frame);

        slider = findViewById(R.id.sliding_layout);
        slider.addPanelSlideListener(this);

        // 显示tag
        tagButton = findViewById(R.id.show_tag);
        tagButton.setOnClickListener(this);

        // 显示最近列表
        recentButton = findViewById(R.id.show_recent);
        recentButton.setOnClickListener(this);

        // 显示邻近
        neighborButton = findViewById(R.id.show_neighbour);
        neighborButton.setOnClickListener(this);

        // 学习
        Button mDicButton = findViewById(R.id.start_study);
        mDicButton.setOnClickListener(this);

        // 繁体、简体、简体+
        mTButton = findViewById(R.id.button_t);
        mTButton.setOnClickListener(this);
        mSButton = findViewById(R.id.button_s);
        mSButton.setOnClickListener(this);
        mSpButton = findViewById(R.id.button_sp);
        mSpButton.setOnClickListener(this);

        // 下一首随机诗
        Button b = findViewById(R.id.next_random);
        b.setOnClickListener(this);

        // 读取配置
        SharedPreferences pref = getPreferences(MODE_PRIVATE);
        int mode = pref.getInt("mode", 2);
        // 模式
        setPoemMode(mode);

        // 读取诗
        boolean saveID;
        if (intentID == -1) {
            // load上回的
            int id = pref.getInt("poem_id", 1);
            toPoemByID(id);
            saveID = false;
        } else {
            toPoemByID(intentID);
            intent.removeExtra("poem_id");
            saveID = true;
        }

        // 各种UI
        boolean showPoem = savedInstanceState == null;
        updateUIForPoem(showPoem, saveID);
    }

    @Override
    public void setPoemID(int id) {
        if (currentPoem == null || currentPoem.getId() != id) {
            toPoemByID(id);
            updateUIForPoem(true, true);

            studyPosi = 0;
            studyTags = null;
        }
    }

    @Override
    public void setHasTag(boolean has) {
        poemView.setHasTag(has);
    }

    private void randomPoem() {
        // 随机一首诗
        int temp;
        if (currentPoem != null) {
            temp = currentPoem.getId();
        } else {
            temp = 1;
        }

        do {
            currentPoem = MyDatabaseHelper.randomPoem();
        } while (currentPoem.getId() == temp);

        studyPosi = 0;
        studyTags = null;
    }

    private void toPoemByID(int id) {
        currentPoem = MyDatabaseHelper.getPoemById(id);
    }

    private void updateUIForPoem(boolean showPoem, boolean saveID) {
        // 诗
        poemView.setPoem(currentPoem, showPoem);

        // 最近
        recentView.setPoem(poemView.getInfoItem());
        recentView.LoadRecentList();

        // 邻近
        neighbourView.setPoem(currentPoem);
        neighbourView.loadNeighbour();

        // 显示tag
        tagView.setPoemId(currentPoem.getId());

        // 写入
        if (saveID) {
            SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
            editor.putInt("poem_id", currentPoem.getId());
            editor.apply();
        }
    }

    private void setPoemModeSave(int mode) {
        setPoemMode(mode);

        SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
        editor.putInt("mode", mode);
        editor.apply();
    }

    private void setPoemMode(int mode) {
        poemView.setMode(mode);

        if (mode == 0) {
            mTButton.setTextColor(Color.BLUE);
            mSButton.setTextColor(Color.BLACK);
            mSpButton.setTextColor(Color.BLACK);
        } else if (mode == 1) {
            mTButton.setTextColor(Color.BLACK);
            mSButton.setTextColor(Color.BLUE);
            mSpButton.setTextColor(Color.BLACK);
        } else {
            mTButton.setTextColor(Color.BLACK);
            mSButton.setTextColor(Color.BLACK);
            mSpButton.setTextColor(Color.BLUE);
        }
    }

    private void setBold(Button b, String t) {
        SpannableString temp;
        temp = new SpannableString(t);
        temp.setSpan(new StyleSpan(Typeface.BOLD), 0, t.length(), 0);
        b.setText(temp);
    }

    private void setBoldButton() {
        setBoldButton(currentView);
    }

    private void setBoldButton(int mode) {
        if (collapsed) {
            mode = NO;
        }

        switch (mode) {
            case NO:
                neighborButton.setText("邻近");
                recentButton.setText("最近");
                tagButton.setText("标签");
                break;

            case NEIGHBOR:
                setBold(neighborButton, "邻近");
                recentButton.setText("最近");
                tagButton.setText("标签");
                break;

            case RECENT:
                neighborButton.setText("邻近");
                setBold(recentButton, "最近");
                tagButton.setText("标签");
                break;

            case TAG:
                neighborButton.setText("邻近");
                recentButton.setText("最近");
                setBold(tagButton, "标签");

                break;
        }
    }

    private void setView(int view) {
        currentView = view;

        switch (view) {
            case NEIGHBOR:
                neighbourView.setVisibility(View.VISIBLE);
                recentView.setVisibility(View.GONE);
                tagView.setVisibility(View.GONE);
                break;

            case RECENT:
                neighbourView.setVisibility(View.GONE);
                recentView.setVisibility(View.VISIBLE);
                tagView.setVisibility(View.GONE);
                break;

            case TAG:
                neighbourView.setVisibility(View.GONE);
                recentView.setVisibility(View.GONE);
                tagView.setVisibility(View.VISIBLE);
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (slider.getPanelState() != PanelState.COLLAPSED) {
            slider.setPanelState(PanelState.COLLAPSED);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode,
                                    Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == STUDY_REQ_CODE && resultCode == RESULT_FIRST_USER) {
            studyPosi = data.getFloatExtra("posi", 0);
            studyTags = data.getStringArrayExtra("tags");
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt("view", currentView);
        outState.putBoolean("collapsed", collapsed);

        outState.putFloat("posi", poemView.getYPosi());
        outState.putFloat("study_posi", studyPosi);
        outState.putStringArray("study_tags", studyTags);

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        poemView.setPoem(currentPoem, true);

        currentView = savedInstanceState.getInt("view");
        setView(currentView);

        collapsed = savedInstanceState.getBoolean("collapsed");
        setBoldButton();

        float posi = savedInstanceState.getFloat("posi");
        poemView.setYPosi(posi);

        studyPosi = savedInstanceState.getFloat("study_posi");
        studyTags = savedInstanceState.getStringArray("study_tags");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.next_random:
                randomPoem();
                updateUIForPoem(true, true);
                break;

            case R.id.show_tag:
                if (slider.getPanelState() == PanelState.COLLAPSED) {
                    slider.setPanelState(PanelState.ANCHORED);
                }
                setView(TAG);
                setBoldButton();
                break;

            case R.id.show_recent:
                if (slider.getPanelState() == PanelState.COLLAPSED) {
                    slider.setPanelState(PanelState.ANCHORED);
                }
                setView(RECENT);
                setBoldButton();
                break;

            case R.id.show_neighbour:
                neighbourView.centerPosition();

                if (slider.getPanelState() == PanelState.COLLAPSED) {
                    slider.setPanelState(PanelState.ANCHORED);
                }
                setView(NEIGHBOR);
                setBoldButton();
                break;

            case R.id.start_study:
                StudyActivity.actionStart(
                        OnePoemActivity.this,
                        STUDY_REQ_CODE,
                        currentPoem.getId(),
                        studyPosi,
                        studyTags
                );
                break;

            case R.id.button_t:
                poemView.setMode(0);
                setPoemModeSave(0);
                break;

            case R.id.button_s:
                poemView.setMode(1);
                setPoemModeSave(1);
                break;

            case R.id.button_sp:
                poemView.setMode(2);
                setPoemModeSave(2);
                break;
        }
    }

    @Override
    public void onPanelSlide(View panel, float slideOffset) {

    }

    @Override
    public void onPanelStateChanged(View panel, PanelState previousState, PanelState newState) {
        if (newState == PanelState.DRAGGING) {
            return;
        } else if (newState == PanelState.ANCHORED) {
            Rect r = new Rect();
            if (swichFrame.getGlobalVisibleRect(r)) {
                swichFrame.getLayoutParams().height = r.height();
                swichFrame.requestLayout();
            }
            collapsed = false;
        } else if (newState == PanelState.COLLAPSED) {
            collapsed = true;
        } else {
            swichFrame.setLayoutParams(
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.MATCH_PARENT)
            );
            collapsed = false;
        }
        setBoldButton();
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}

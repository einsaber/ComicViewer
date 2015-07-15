package jp.gr.java_conf.ein.comicviewer.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import jp.gr.java_conf.ein.comicviewer.MainPagerAdapter;
import jp.gr.java_conf.ein.comicviewer.model.FileData;


/**
 * 画像表示アクティビティ
 */
public class ViewerActivity extends Activity {

    /** 画像ファイルリスト */
    private List<String> imageFileNameList = new ArrayList<>();

    ViewPager viewPager = null;

    private int windowWidth;
    private int windowHeight;

    /** カレントディレクトリ or カレントアーカイブ */
    private FileData current = null;

    boolean isInitialized = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // TODO 設定化
        // タイトルなし
        // this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        viewPager = new ViewPager(this);
        viewPager.setOffscreenPageLimit(2);

        viewPager.setOnTouchListener(new View.OnTouchListener() {
            private boolean down = false;
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {
                    case MotionEvent.ACTION_MOVE:
                        down = false;
                        break;
                    case MotionEvent.ACTION_DOWN:
                        down = true;
                        break;
                    case MotionEvent.ACTION_UP:
                        if (down) {
                            // シングルタップを検知 タップ位置でページ遷移
                            boolean isLeft = event.getX() < windowWidth / 2;
                            if (!isLeft) {
                                nextPage();
                            } else {
                                backPage();
                            }
                        }
                        down = false;
                        break;
                }
                return false;
            }
        });

        // レイアウトにセット
        setContentView(viewPager);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        windowHeight = viewPager.getHeight();
        windowWidth = viewPager.getWidth();

        // アダプタの初期化(一度だけ行う)
        if (!isInitialized) {
            initAdapter();
            isInitialized = true;
        }
    }

    /**
     * アダプタの初期化
     */
    private void initAdapter() {

        // 選択されたファイル名を取得
        String selectedFilePath = getIntent().getStringExtra("selectedFile");
        String selectedFileName = null;

        if (selectedFilePath == null) {
            // デフォ？？
            // SDカードのFileを取得
            File file = Environment.getExternalStorageDirectory();
            current = new FileData(file.getAbsolutePath(), file.getName(), true, false);
        } else {
            // 選択されたファイルからzip読み込みorファイル読み込み。
            File selectedFile = new File(selectedFilePath);
            selectedFileName = selectedFile.getName();

            if (selectedFilePath.endsWith(".zip")) {
                current = new FileData(selectedFile.getAbsolutePath(), selectedFile.getName(), false, true);
            } else {
                File parent = selectedFile.getParentFile();
                current = new FileData(parent.getAbsolutePath(), parent.getName(), true, false);
            }
        }

        if (current != null && current.isArchive()) {
            // カレントがアーカイブならアーカイブの中身から画像ファイルを抽出してリスト化
            ZipFile zipFile = null;
            try {
                zipFile = new ZipFile(current.getFilePath());
                Enumeration<? extends ZipEntry> enums = zipFile.entries();

                while (enums.hasMoreElements()) {
                    ZipEntry entry = (ZipEntry) enums.nextElement();
                    String fileName = entry.getName().toLowerCase();
                    if (fileName.endsWith(".jpg")) {
                        imageFileNameList.add(entry.getName());
                    }
                }
            } catch (IOException e) {
                Log.e("ComicViewer", "cannot open file.", e);
            } finally {
                if (zipFile != null) {
                    try {
                        zipFile.close();
                    } catch (IOException e) {
                        Log.e("ComicViewer", "cannot close file.", e);
                    }
                }
            }
        } else {
            // 通常ディレクトリの場合、ディレクトリ内の画像をリスト化
            File dir = new File(current.getFilePath());
            File[] fileList = dir.listFiles();
            int i = 0;
            while (i < fileList.length) {
                String fileName = fileList[i].getName().toLowerCase();
                if (fileName.endsWith(".jpg")) {
                    imageFileNameList.add(fileList[i].getName());
                }
                ++i;
            }
        }

        // カスタム PagerAdapter を生成
        MainPagerAdapter adapter = new MainPagerAdapter(this);

        // ファイルリストを並び替え
        Collections.sort(imageFileNameList);
        boolean leftToRight = true; // TODO 設定値化
        if (leftToRight) {
            Collections.reverse(imageFileNameList);
        }
        adapter.setImageFileNameList(imageFileNameList);

        adapter.setCurrent(current);

        viewPager.setAdapter(adapter);

        // ファイルリストで選択された画像を最初に表示する
        Integer currentPageIndex = null;
        for (String imageFileName : imageFileNameList) {
            if (imageFileName.equals(selectedFileName)) {
                currentPageIndex = imageFileNameList.indexOf(imageFileName);
                break;
            }
        }
        if (currentPageIndex == null) {
            if (leftToRight) {
                viewPager.setCurrentItem(imageFileNameList.size() - 1);
            } else {
                viewPager.setCurrentItem(0);
            }
        } else {
            viewPager.setCurrentItem(currentPageIndex);
        }

    }
    /**
     * 次のページに移動する
     */
    public void nextPage() {
        int currentIndex = viewPager.getCurrentItem();
        if (currentIndex < imageFileNameList.size() - 1) {
            ++currentIndex;
            viewPager.setCurrentItem(currentIndex);
        }
    }
    /**
     * 前のページに移動する
     */
    public void backPage() {
        int currentIndex = viewPager.getCurrentItem();
        if (currentIndex > 0) {
            --currentIndex;
            viewPager.setCurrentItem(currentIndex);
        }
    }

    /** メニューID  */
    private final int MENU_ID1 = Menu.FIRST;
    private final int MENU_ID2 = Menu.FIRST + 1;

    /**
     * メニューを作成する
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        menu.add(0,
                MENU_ID1,
                0,
                "Select File").setIcon(android.R.drawable.ic_menu_crop);
        menu.add(0,
                MENU_ID2,
                1,
                "Exit").setIcon(android.R.drawable.ic_menu_close_clear_cancel);
        return true;
    }
    /**
     * メニューボタン押下時の処理
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        return true;
    }

    /**
     * メニューを選択時の処理
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_ID1:
                // ファイル選択アクティビティ
                Intent intent = new Intent(ViewerActivity.this, FilerActivity.class);
                if (current != null) {
                    intent.putExtra("current", current.getFilePath());
                }
                startActivityForResult(intent, 0);

                finish();
                return true;
            case MENU_ID2:
                //Activity終了
                finish();
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}

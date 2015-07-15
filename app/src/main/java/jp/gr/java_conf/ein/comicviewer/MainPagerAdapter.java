package jp.gr.java_conf.ein.comicviewer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import jp.gr.java_conf.ein.comicviewer.model.FileData;

/**
 * 画像表示アクティビティで使用するページャのアダプタ
 * 実際の画像表示を行う
 *
 * Created by ein on 2015/04/18.
 */
public class MainPagerAdapter extends PagerAdapter {

    /** カレントディレクトリorカレントアーカイブ */
    FileData current;

    /** 表示する画像ファイル名のリスト */
    private List<String> imageFileNameList;

    private Context context;

    public MainPagerAdapter(Context context) {
        this.context = context;
        imageFileNameList = new ArrayList<String>();
    }

    /**
     * 表示する画像ファイル名を追加する
     */
    public void add(String item) {
        imageFileNameList.add(item);
    }

    /**
     * 表示する画像ファイル名リストを設定する
     */
    public void setImageFileNameList(List<String> list) {
        imageFileNameList.clear();
        imageFileNameList.addAll(list);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        int windowSizeWidth = container.getWidth();
        int windowSizeHeight = container.getHeight();

        // リストから取得
        String item = imageFileNameList.get(position);

        ImageView imageView = new ImageView(context);
        // コンテナに追加
        container.addView(imageView);

        int imageWidthSize = 0;
        int imageHeightSize = 0;

        if (current != null) {
            // 画像セット
            Bitmap bmp = null;
            if (current.isArchive()) {
                // アーカイブから画像データを取得
                ZipFile zipFile = null;
                InputStream stream = null;
                try {
                    zipFile = new ZipFile(current.getFilePath());
                    ZipEntry entry = zipFile.getEntry(imageFileNameList.get(position));
                    stream = zipFile.getInputStream(entry);

                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    BitmapFactory.decodeStream(stream, null, options);
                    stream.close();

                    setOptions(options, windowSizeWidth, windowSizeHeight);
                    stream = zipFile.getInputStream(entry);
                    bmp = BitmapFactory.decodeStream(stream, null, options);
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
                    if (stream != null) {
                        try {
                            stream.close();
                        } catch (IOException e) {
                            Log.e("ComicViewer", "cannot close stream.", e);
                        }
                    }
                }
            } else {
                // 通常ファイルから画像データ取得
                File imageFile = new File(current.getFilePath() + "/" + imageFileNameList.get(position));
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(imageFile.getPath(), options);
                setOptions(options, windowSizeWidth, windowSizeHeight);
                bmp = BitmapFactory.decodeFile(imageFile.getPath(), options);
            }

            if (bmp != null) {
                // ビットマップデータを画面サイズに合わせて拡縮
                imageHeightSize = bmp.getHeight();
                imageWidthSize = bmp.getWidth();
                imageView.setImageBitmap(bmp);

                imageView.setScaleType(ImageView.ScaleType.MATRIX);

                float ratio = 1.0f;
                float widthRatio =  (float) windowSizeWidth / (float) imageWidthSize;
                float heightRatio =  (float) windowSizeHeight / (float) imageHeightSize;
                // 縦横どちらの拡縮比を合わせるか
                if (widthRatio < heightRatio) {
                    ratio = widthRatio;
                } else {
                    ratio = heightRatio;
                }

                // 拡縮後のサイズ
                int resizedWidth = (int) (imageWidthSize * ratio);
                int resizedHeight = (int) (imageHeightSize * ratio);

                Matrix m = imageView.getImageMatrix();
                m.reset();
                m.postScale(ratio, ratio);
                // 画面中央に配置
                m.postTranslate((windowSizeWidth - resizedWidth) / 2, (windowSizeHeight - resizedHeight) / 2);

                imageView.setImageMatrix(m);
            }
        }

        return imageView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        // コンテナから View を削除
        container.removeView((View) object);
    }

    @Override
    public int getCount() {
        // リストのアイテム数を返す
        return imageFileNameList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        // Object 内に View が存在するか判定する
        return view == (ImageView) object;
    }

    /**
     * カレントとなるファイルオブジェクトを設定
     */
    public void setCurrent(FileData current) {
        this.current = current;
    }

    /**
     * 画像データなしOptionに画像データ読み込み用の情報を設定する
     * ・読み込みサイズ
     * ・色数
     */
    private void setOptions(final BitmapFactory.Options options, final int windowSizeWidth, final int windowSizeHeight) {
        int imageWidthSize = options.outWidth;
        int imageHeightSize = options.outHeight;

        int widthRatio = windowSizeWidth / imageWidthSize;
        int heightRatio = windowSizeHeight / imageHeightSize;
        int ratio;
        if (widthRatio > heightRatio) {
            ratio = widthRatio;
        } else {
            ratio = heightRatio;
        }

        options.inJustDecodeBounds = false;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        if (ratio >= 2) {
            // 縮小比が2倍以上の場合のみ設定
            options.inSampleSize = ratio;
        }
    }
}

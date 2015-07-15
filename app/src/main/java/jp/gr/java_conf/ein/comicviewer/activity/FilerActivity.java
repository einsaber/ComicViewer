package jp.gr.java_conf.ein.comicviewer.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import jp.gr.java_conf.ein.comicviewer.R;
import jp.gr.java_conf.ein.comicviewer.model.FileData;

/**
 * ファイル選択アクティビティ
 */
public class FilerActivity extends Activity {
    private ListView listView = null;
    /** リスト表示用ファイルデータ */
    private List<FileData> listData = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filer);

        // TODO FileData化
        String selectedFilePath = getIntent().getStringExtra("selectedFile");

        File file = Environment.getExternalStorageDirectory();
        setFileList(file.getAbsolutePath());

        listView  = (ListView)findViewById(R.id.ListView);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, R.layout.list_content, getDisplayFileList());
        listView.setAdapter(arrayAdapter);

        // TODO タブルタップ、長押し対応
        // アイテムクリック
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                ListView listView = (ListView) parent;
                String item = (String) listView.getItemAtPosition(position);

                FileData selectedFile = listData.get(position);

                if (selectedFile.isDirectory()) {
                    // ディレクトリが選択された場合は中身を表示
                    setFileList(selectedFile.getFilePath());
                    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                            FilerActivity.this, R.layout.list_content, getDisplayFileList());
                    listView.setAdapter(arrayAdapter);
                } else {
                    // ディレクトリ以外の場合は画像表示アクティビティへ移動
                    Intent intent = new Intent(FilerActivity.this, ViewerActivity.class);
                    intent.putExtra("selectedFile", selectedFile.getFilePath());
                    startActivityForResult(intent, 0);
                    finish();
                }
            }
        });
    }

    /**
     * 選択されたディレクトリのパスから
     * そのディレクトリ内部のファイルをファイルリストにセットする。
     * @param directoryPath
     */
    private void setFileList(String directoryPath) {
        listData.clear();

        File directory = new File(directoryPath);
        File fileList[] = directory.listFiles();

        FileData parent = new FileData(directory.getParent(), "...", true, false);
        listData.add(parent);

        if (fileList == null) {
            return;
        }

        int listSize = fileList.length;
        int m = 0;
        while(listSize > m){
            File subFile = fileList[m];
            String fileName = subFile.getName().toLowerCase();
            if (subFile.isDirectory() || fileName.endsWith(".jpg") || fileName.endsWith(".zip")) {
                FileData fileData = new FileData(subFile.getAbsolutePath(), subFile.getName(), subFile.isDirectory(), false);
                listData.add(fileData);
            }
            m++;
        }

        // ファイルリストを昇順で並び替え ディレクトリ優先
        Collections.sort(listData, new Comparator<FileData>() {
            @Override
            public int compare(FileData lhs, FileData rhs) {
                if (rhs.isDirectory()) {
                    return 1;
                }
                return lhs.getFileName().compareTo(rhs.getFileName());
            }
        });
    }

    /**
     * 現在のリストデータから表示用文字列の配列を返す
     */
    private String[] getDisplayFileList() {
        String displayFileList[] = new String[listData.size()];
        int index = 0;
        for (FileData file : listData) {
            displayFileList[index] = file.getFileName();
            ++index;
        }
        return displayFileList;
    }
}

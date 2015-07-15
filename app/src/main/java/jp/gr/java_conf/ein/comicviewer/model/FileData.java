package jp.gr.java_conf.ein.comicviewer.model;

/**
 * Created by ein on 2015/04/18.
 */
public class FileData {
    /** ファイル名 */
    private String fileName;
    /** ファイルパス(フルパス) */
    private String filePath;
    /** ディレクトリかどうか デフォルト:false */
    private boolean isDirectory = false;
    /** アーカイブかどうか デフォルト:false */
    private boolean isArchive = false;

    public FileData(String filePath, String fileName, boolean isDirectory, boolean isArchive) {
        this.filePath = filePath;
        this.fileName = fileName;
        this.isDirectory = isDirectory;
        this.isArchive = isArchive;
    }

    /** ファイル名 */
    public String getFileName() {
        return fileName;
    }
    /** ファイル名 */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /** ファイルパス(フルパス) */
    public String getFilePath() {
        return filePath;
    }
    /** ファイルパス(フルパス) */
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    /** ディレクトリかどうか デフォルト:false */
    public boolean isDirectory() {
        return isDirectory;
    }
    /** ディレクトリかどうか デフォルト:false */
    public void setDirectory(boolean isDirectory) {
        this.isDirectory = isDirectory;
    }

    /** アーカイブかどうか デフォルト:false */
    public boolean isArchive() {
        return isArchive;
    }
    /** アーカイブかどうか デフォルト:false */
    public void setIsArchive(boolean isArchive) {
        this.isArchive = isArchive;
    }
}

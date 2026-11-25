package com.bit.dbs.utils;

import com.bit.dbs.exception.BasicException;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

public class UploadUtils {

    private static final String FILE_TYPE_IMG = "img";

    private static final String FILE_TYPE_VIDEO = "video";

    /**
     * 上传根目录。优先取 -Dbook.upload.dir，其次取环境变量 BOOK_UPLOAD_DIR，
     * 默认使用工作目录下的 uploads（避免写入 war 内部 classpath）。
     */
    private static final String UPLOAD_ROOT = resolveUploadRoot();

    public static String getUploadRoot() {
        return UPLOAD_ROOT;
    }

    private static String resolveUploadRoot() {
        String dir = System.getProperty("book.upload.dir");
        if (dir == null || dir.trim().isEmpty()) {
            dir = System.getenv("BOOK_UPLOAD_DIR");
        }
        if (dir == null || dir.trim().isEmpty()) {
            dir = System.getProperty("user.dir") + File.separator + "uploads";
        }
        return dir.endsWith(File.separator) ? dir : dir + File.separator;
    }

    /**
     * 图片上传，返回相对于上传根目录的访问路径 如： /static/img/detail/20211003_xxxx.jpg
     */
    public static String uploadPicture(MultipartFile file, String path) {
        return upload(file, FILE_TYPE_IMG, path);
    }

    /**
     * 视频上传，返回相对于上传根目录的访问路径 如： /static/video/db1/xxx/20211003_xxxx.mp4
     */
    public static String uploadVideo(MultipartFile file, String path) {
        return upload(file, FILE_TYPE_VIDEO, path);
    }

    /**
     * 文件上传
     */
    private static String upload(MultipartFile file, String fileType, String path) {
        String fileName = file.getOriginalFilename();
        String pathPrefix, filePath;
        if (FILE_TYPE_IMG.equals(fileType)) {
            pathPrefix = "static/img";
        } else if (FILE_TYPE_VIDEO.equals(fileType)) {
            pathPrefix = "static/video";
        } else {
            pathPrefix = "static/other";
        }
        pathPrefix += path;
        try {
            File directory = new File(UPLOAD_ROOT + pathPrefix);
            if (!directory.exists() && !directory.mkdirs()) {
                throw new IOException("无法创建上传目录：" + directory.getAbsolutePath());
            }
            pathPrefix += CommonUtils.dateToStr() + "_";
            filePath = pathPrefix + fileName;
            File dest = new File(UPLOAD_ROOT + filePath);
            if (dest.exists()) {
                filePath = pathPrefix + CommonUtils.timeToStr() + "_" + fileName;
                dest = new File(UPLOAD_ROOT + filePath);
            }
            file.transferTo(dest);
        } catch (IOException e) {
            e.printStackTrace();
            throw new BasicException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "文件上传失败！");
        }
        return "/" + filePath;
    }

    /**
     * 文件删除
     */
    public static void removeFile(String path) {
        try {
            String relative = path.startsWith("/") ? path.substring(1) : path;
            File dest = new File(UPLOAD_ROOT + relative);
            if (dest.exists() && !dest.delete()) {
                throw new IOException("无法删除文件：" + dest.getAbsolutePath());
            }
        } catch (Exception e) {
            throw new BasicException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "文件删除失败！");
        }
    }
}

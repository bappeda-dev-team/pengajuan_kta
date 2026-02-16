package cc.kertaskerja.pengajuan_kta.dto.external;

import lombok.*;

public class FileDownloadDTO {
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DownloadRes {
        private byte[] data;
        private String filename;
        private String contentType;
    }
}

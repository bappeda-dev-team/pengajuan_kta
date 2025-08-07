package cc.kertaskerja.pengajuan_kta.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DokumenPendukungDTO {
    private String url;
    private String fileName;
    private String contentType;
}
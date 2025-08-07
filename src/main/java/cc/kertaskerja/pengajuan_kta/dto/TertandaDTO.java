package cc.kertaskerja.pengajuan_kta.dto;

import lombok.*;

@Getter
@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TertandaDTO {
    private String nama;
    private String tanda_tangan;
    private String jabatan;
    private String nip;
    private String pangkat;
}

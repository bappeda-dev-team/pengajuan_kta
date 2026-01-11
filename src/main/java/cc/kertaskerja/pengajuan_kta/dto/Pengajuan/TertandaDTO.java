package cc.kertaskerja.pengajuan_kta.dto.Pengajuan;

import lombok.*;

@Getter
@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TertandaDTO {
    private String nama;
    private String jabatan;
    private String nip;
    private String pangkat;
}

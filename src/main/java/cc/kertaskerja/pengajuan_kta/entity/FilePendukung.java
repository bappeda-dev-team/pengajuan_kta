package cc.kertaskerja.pengajuan_kta.entity;

import cc.kertaskerja.pengajuan_kta.common.BaseAuditable;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "file_pendukung")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FilePendukung extends BaseAuditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_url", columnDefinition = "TEXT", nullable = false)
    private String fileUrl;

    @Column(name = "nama_file", length = 255, nullable = false)
    private String namaFile;

    // Remove the direct formUuid field since we're using the relationship
    // @Column(name = "form_uuid", nullable = false)
    // private UUID formUuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "form_uuid", referencedColumnName = "uuid", nullable = false)
    @JsonBackReference
    private FormPengajuan formPengajuan;
    
    // Helper method to get the UUID if needed
    public UUID getFormUuid() {
        return formPengajuan != null ? formPengajuan.getUuid() : null;
    }
}
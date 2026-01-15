package cc.kertaskerja.pengajuan_kta.dto.Rekomendasi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonIgnoreProperties(ignoreUnknown = true)
public class FilePendukungDTO {
    @NotBlank(message = "rekom_uuid tidak boleh kosong!")
    private String rekom_uuid;

    @NotBlank(message = "Url File wajib diisi")
    private String file_url;

    @NotBlank(message = "Nama file tidak boleh kosong!")
    private String nama_file;
}

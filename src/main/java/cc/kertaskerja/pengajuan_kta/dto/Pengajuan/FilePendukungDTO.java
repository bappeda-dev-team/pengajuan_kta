package cc.kertaskerja.pengajuan_kta.dto.Pengajuan;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.ALWAYS) // always show fields
@JsonIgnoreProperties(ignoreUnknown = true)
public class FilePendukungDTO {
    @NotBlank(message = "form_uuid tidak boleh kosong!")
    private String form_uuid;

    @NotBlank(message = "Url File wajib diisi")
    private String file_url;

    @NotBlank(message = "Nama file tidak boleh kosong!")
    private String nama_file;
}

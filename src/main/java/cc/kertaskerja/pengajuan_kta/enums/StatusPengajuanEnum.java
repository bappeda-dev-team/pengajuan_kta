package cc.kertaskerja.pengajuan_kta.enums;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.STRING)
public enum StatusPengajuanEnum {
    DRAFT,
    PENDING,
    APPROVED,
    REJECTED,
    VERIFIED
}

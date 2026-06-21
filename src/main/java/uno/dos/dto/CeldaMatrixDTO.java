package uno.dos.dto;

import lombok.Data;
import uno.dos.models.entity.NivelSkill;

@Data
public class CeldaMatrixDTO {

    private Long capacitacionId;
    private NivelSkill nivel = NivelSkill.VACIO;
}
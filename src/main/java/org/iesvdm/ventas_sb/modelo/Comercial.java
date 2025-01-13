package org.iesvdm.ventas_sb.modelo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Comercial {

    private Integer id;
    private String nombre;
    private String apellido1;
    private String apellido2;
    private float comisión;

}

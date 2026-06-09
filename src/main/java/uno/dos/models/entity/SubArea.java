package uno.dos.models.entity;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "subareas")
public class SubArea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombreSubArea;

    @ManyToOne
    @JoinColumn(name = "area_id")
    private Area area;

    private Boolean activo = true;
    
    @OneToMany(
            mappedBy = "subArea",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY
    )
    private List<Habilidad> habilidades;
}
package madstodolist.model;

import javax.persistence.*;

@Entity
@Table(name = "equipos")
public class Equipo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    public Equipo () {}

    public Equipo (String nombre){
        this.nombre = nombre;
    }

    public Long getId() {
        return id;
    }

    public String getNombre(){
        return nombre;
    }
}

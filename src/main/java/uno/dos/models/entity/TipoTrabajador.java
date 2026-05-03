package uno.dos.models.entity;

public enum TipoTrabajador {

    HOURLY("Por hora"),
    SALARY("Sueldo"),
    AMBOS("Ambos");

    private final String descripcion;

    TipoTrabajador(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
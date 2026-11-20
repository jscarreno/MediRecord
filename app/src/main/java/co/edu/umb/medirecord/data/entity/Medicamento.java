package co.edu.umb.medirecord.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/** Medicamento registrado por el usuario (tabla "medicamento"). */
@Entity(tableName = "medicamento")
public class Medicamento {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    public String nombre = "";

    /** Texto libre, p. ej. "500 mg", "10 ml", "2 gotas". */
    @NonNull
    public String dosis = "";

    /** Presentación: pastilla, jarabe, gotas, etc. */
    @NonNull
    public String tipo = "";

    public String notas;

    public long creadoEn;
}

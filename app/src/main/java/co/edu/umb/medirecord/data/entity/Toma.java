package co.edu.umb.medirecord.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Registro de una toma (tomada u omitida) para un horario en una fecha.
 * El índice único (horarioId, fecha) impide registros duplicados del mismo día.
 */
@Entity(tableName = "toma",
        foreignKeys = @ForeignKey(entity = Horario.class,
                parentColumns = "id",
                childColumns = "horarioId",
                onDelete = ForeignKey.CASCADE),
        indices = @Index(value = {"horarioId", "fecha"}, unique = true))
public class Toma {

    public static final String ESTADO_TOMADA = "TOMADA";
    public static final String ESTADO_OMITIDA = "OMITIDA";

    @PrimaryKey(autoGenerate = true)
    public long id;

    public long horarioId;

    /** Fecha local en formato yyyy-MM-dd. */
    @NonNull
    public String fecha = "";

    @NonNull
    public String estado = ESTADO_TOMADA;

    /** Momento (epoch millis) en que el usuario registró la toma. */
    public long registradaEn;

    public Toma() {
    }

    @Ignore
    public Toma(long horarioId, @NonNull String fecha, @NonNull String estado, long registradaEn) {
        this.horarioId = horarioId;
        this.fecha = fecha;
        this.estado = estado;
        this.registradaEn = registradaEn;
    }
}

package co.edu.umb.medirecord.data.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Hora del día en la que se debe tomar un medicamento.
 * Se guarda como minutos desde la medianoche (08:30 = 510).
 */
@Entity(tableName = "horario",
        foreignKeys = @ForeignKey(entity = Medicamento.class,
                parentColumns = "id",
                childColumns = "medicamentoId",
                onDelete = ForeignKey.CASCADE),
        indices = @Index("medicamentoId"))
public class Horario {

    @PrimaryKey(autoGenerate = true)
    public long id;

    public long medicamentoId;

    public int minutos;

    public Horario() {
    }

    @Ignore
    public Horario(long medicamentoId, int minutos) {
        this.medicamentoId = medicamentoId;
        this.minutos = minutos;
    }
}

package co.edu.umb.medirecord.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import co.edu.umb.medirecord.data.entity.Toma;
import co.edu.umb.medirecord.data.model.RegistroHistorial;
import co.edu.umb.medirecord.data.model.ResumenAdherencia;
import co.edu.umb.medirecord.data.model.TomaDelDia;

@Dao
public interface TomaDao {

    /** REPLACE + índice único (horarioId, fecha): una sola toma por horario y día. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long registrar(Toma toma);

    @Query("DELETE FROM toma WHERE horarioId = :horarioId AND fecha = :fecha")
    int deshacer(long horarioId, String fecha);

    @Query("SELECT COUNT(*) FROM toma")
    int contarTodas();

    @Query("SELECT h.id AS horarioId, h.minutos AS minutos, m.id AS medicamentoId, m.nombre AS nombre, "
            + "m.dosis AS dosis, m.tipo AS tipo, t.estado AS estado, t.registradaEn AS registradaEn "
            + "FROM horario h INNER JOIN medicamento m ON m.id = h.medicamentoId "
            + "LEFT JOIN toma t ON t.horarioId = h.id AND t.fecha = :fecha "
            + "ORDER BY h.minutos, m.nombre COLLATE NOCASE")
    LiveData<List<TomaDelDia>> observarDia(String fecha);

    @Query("SELECT h.id AS horarioId, h.minutos AS minutos, m.id AS medicamentoId, m.nombre AS nombre, "
            + "m.dosis AS dosis, m.tipo AS tipo, t.estado AS estado, t.registradaEn AS registradaEn "
            + "FROM horario h INNER JOIN medicamento m ON m.id = h.medicamentoId "
            + "LEFT JOIN toma t ON t.horarioId = h.id AND t.fecha = :fecha "
            + "ORDER BY h.minutos, m.nombre COLLATE NOCASE")
    List<TomaDelDia> obtenerDia(String fecha);

    @Query("SELECT t.id AS tomaId, t.fecha AS fecha, t.estado AS estado, t.registradaEn AS registradaEn, "
            + "h.minutos AS minutos, m.nombre AS nombre, m.dosis AS dosis "
            + "FROM toma t INNER JOIN horario h ON h.id = t.horarioId "
            + "INNER JOIN medicamento m ON m.id = h.medicamentoId "
            + "ORDER BY t.fecha DESC, h.minutos DESC LIMIT 300")
    LiveData<List<RegistroHistorial>> observarHistorial();

    @Query("SELECT COALESCE(SUM(CASE WHEN estado = 'TOMADA' THEN 1 ELSE 0 END), 0) AS tomadas, "
            + "COALESCE(SUM(CASE WHEN estado = 'OMITIDA' THEN 1 ELSE 0 END), 0) AS omitidas "
            + "FROM toma WHERE fecha >= :desde")
    LiveData<ResumenAdherencia> observarResumen(String desde);

    @Query("SELECT COALESCE(SUM(CASE WHEN estado = 'TOMADA' THEN 1 ELSE 0 END), 0) AS tomadas, "
            + "COALESCE(SUM(CASE WHEN estado = 'OMITIDA' THEN 1 ELSE 0 END), 0) AS omitidas "
            + "FROM toma WHERE fecha >= :desde")
    ResumenAdherencia obtenerResumen(String desde);
}

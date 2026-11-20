package co.edu.umb.medirecord.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import java.util.List;

import co.edu.umb.medirecord.data.entity.Horario;
import co.edu.umb.medirecord.data.entity.Medicamento;
import co.edu.umb.medirecord.data.model.MedicamentoConHorarios;
import co.edu.umb.medirecord.data.model.TomaDelDia;

@Dao
public interface MedicamentoDao {

    @Insert
    long insertar(Medicamento medicamento);

    @Update
    void actualizar(Medicamento medicamento);

    @Query("DELETE FROM medicamento WHERE id = :id")
    int eliminar(long id);

    @Insert
    List<Long> insertarHorarios(List<Horario> horarios);

    @Query("DELETE FROM horario WHERE id IN (:ids)")
    void eliminarHorarios(List<Long> ids);

    @Query("SELECT * FROM horario WHERE medicamentoId = :medicamentoId ORDER BY minutos")
    List<Horario> horariosDe(long medicamentoId);

    @Query("SELECT * FROM horario")
    List<Horario> todosLosHorarios();

    @Transaction
    @Query("SELECT * FROM medicamento ORDER BY nombre COLLATE NOCASE")
    LiveData<List<MedicamentoConHorarios>> observarTodos();

    @Transaction
    @Query("SELECT * FROM medicamento WHERE id = :id")
    MedicamentoConHorarios obtener(long id);

    /** Nombres de los demás medicamentos (para detectar duplicados con tildes/mayúsculas en Java). */
    @Query("SELECT nombre FROM medicamento WHERE id != :excluirId")
    List<String> nombresExcepto(long excluirId);

    /** Datos de un horario para la notificación, con el estado de la toma en :fecha. */
    @Query("SELECT h.id AS horarioId, h.minutos AS minutos, m.id AS medicamentoId, m.nombre AS nombre, "
            + "m.dosis AS dosis, m.tipo AS tipo, t.estado AS estado, t.registradaEn AS registradaEn "
            + "FROM horario h INNER JOIN medicamento m ON m.id = h.medicamentoId "
            + "LEFT JOIN toma t ON t.horarioId = h.id AND t.fecha = :fecha "
            + "WHERE h.id = :horarioId")
    TomaDelDia infoHorario(long horarioId, String fecha);
}

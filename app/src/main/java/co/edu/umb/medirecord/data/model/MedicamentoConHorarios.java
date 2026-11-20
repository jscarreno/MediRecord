package co.edu.umb.medirecord.data.model;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.ArrayList;
import java.util.List;

import co.edu.umb.medirecord.data.entity.Horario;
import co.edu.umb.medirecord.data.entity.Medicamento;

/** Relación 1:N medicamento → horarios, armada por Room. */
public class MedicamentoConHorarios {

    @Embedded
    public Medicamento medicamento;

    @Relation(parentColumn = "id", entityColumn = "medicamentoId")
    public List<Horario> horarios = new ArrayList<>();
}

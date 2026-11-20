package co.edu.umb.medirecord.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import co.edu.umb.medirecord.data.dao.MedicamentoDao;
import co.edu.umb.medirecord.data.dao.TomaDao;
import co.edu.umb.medirecord.data.entity.Horario;
import co.edu.umb.medirecord.data.entity.Medicamento;
import co.edu.umb.medirecord.data.entity.Toma;

/** Base de datos local SQLite gestionada con Room. Singleton por proceso. */
@Database(entities = {Medicamento.class, Horario.class, Toma.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static final String NOMBRE_BD = "medirecord.db";
    private static volatile AppDatabase instancia;

    public abstract MedicamentoDao medicamentoDao();

    public abstract TomaDao tomaDao();

    public static AppDatabase get(Context context) {
        if (instancia == null) {
            synchronized (AppDatabase.class) {
                if (instancia == null) {
                    instancia = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, NOMBRE_BD).build();
                }
            }
        }
        return instancia;
    }
}

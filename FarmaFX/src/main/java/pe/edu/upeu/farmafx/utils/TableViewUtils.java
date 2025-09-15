package pe.edu.upeu.farmafx.utils;

import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public final class TableViewUtils {

    private TableViewUtils() {}

    public static void lockColumns(TableView<?> table) {
        // Deshabilita orden y reorden para columnas actuales (y subcolumnas)
        for (TableColumn<?, ?> c : table.getColumns()) {
            disableCol(c);
        }
        table.getSortOrder().clear();
        table.setTableMenuButtonVisible(false);
    }

    private static void disableCol(TableColumn<?, ?> col) {
        col.setSortable(false);
        col.setReorderable(false);
        for (TableColumn<?, ?> sc : col.getColumns()) {
            disableCol(sc);
        }
    }
}
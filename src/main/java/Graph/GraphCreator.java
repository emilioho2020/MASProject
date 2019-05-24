package Graph;

import com.github.rinde.rinsim.geom.*;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;

import static com.google.common.collect.Lists.newArrayList;

public class GraphCreator {

    static private ImmutableTable<Integer, Integer, Point> createMatrix (
            int cols, int rows, double VEHICLE_LENGTH, Point offset) {

        final ImmutableTable.Builder<Integer, Integer, Point> builder =
                ImmutableTable.builder();
        for (int c = 0; c < cols; c++) {
            for (int r = 0; r < rows; r++) {
                builder.put(r, c, new Point(
                        offset.x + c * VEHICLE_LENGTH * 2,
                        offset.y + r * VEHICLE_LENGTH * 2));
            }
        }
        return builder.build();
    }

    static public ListenableGraph<LengthData> createGraph(double VEHICLE_LENGTH, int n) {
        final Graph<LengthData> g = new TableGraph<>();
        final Table<Integer, Integer, Point> matrix = createMatrix(n, n,
                VEHICLE_LENGTH, new Point(0, 0));
        for (int i = 0; i < matrix.columnMap().size(); i++) {
            final Iterable<Point> forward_path_c;
            final Iterable<Point> forward_path_r;
            final Iterable<Point> reverse_path_c;
            final Iterable<Point> reverse_path_r;
            forward_path_c = matrix.column(i).values();
            reverse_path_c = Lists.reverse(newArrayList(matrix.column(i).values()));
            forward_path_r = matrix.row(i).values();
            reverse_path_r = Lists.reverse(newArrayList(matrix.row(i).values()));
            Graphs.addPath(g, forward_path_c);
            Graphs.addPath(g, reverse_path_c);
            Graphs.addPath(g, forward_path_r);
            Graphs.addPath(g, reverse_path_r);
        }
        return new ListenableGraph<>(g);
    }
}

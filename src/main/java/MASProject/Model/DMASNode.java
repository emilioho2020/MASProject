package MASProject.Model;

import com.github.rinde.rinsim.geom.Point;
import com.google.common.base.Optional;


//User of the DMASModel
public interface DMASNode {

    Optional<Point> getPosition();
}

package org.jzy3d.javafx;

import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;

import org.jzy3d.chart.Chart;
import org.jzy3d.chart.controllers.camera.AbstractCameraController;
import org.jzy3d.chart.controllers.thread.camera.CameraThreadController;
import org.jzy3d.maths.Coord2d;

public class JavaFXCameraMouseController extends AbstractCameraController {
    static {
        DEFAULT_UPDATE_VIEW = true;
    }
    
    protected Node node;
    
    public JavaFXCameraMouseController(Node node) {
        super();
        register(node);
    }

    public JavaFXCameraMouseController(Chart chart, Node node) {
        super(chart);
        register(node);
        register(chart);
        
        CameraThreadController threadCam = new CameraThreadController(chart);
        threadCam.setStep(0.005f);
        addSlaveThreadController(threadCam);
    }
    
    public Node getNode() {
        return node;
    }

    public void setNode(Node node) {
        register(node);
    }

    private void register(Node node) {
        this.node = node;
        
        if(node==null)
            return;
        
        // ON MOUSE PRESS
        node.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                node.setCursor(Cursor.MOVE);
                mousePressed(mouseEvent);
                //console(mouseEvent);
                handleSlaveThread(mouseEvent);
            }

        });
          
        // ON MOUSE DRAG
        node.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                mouseDragged(mouseEvent);
                //console(mouseEvent);
            }
        });
    }
    
    /* ###################################*/

    protected void mousePressed(MouseEvent e) {
        if (handleSlaveThread(e))
            return;
        prevMouse.x = (float)e.getX();
        prevMouse.y = (float)e.getY();
    }

    protected void mouseDragged(MouseEvent e) {
        Coord2d mouse = new Coord2d(e.getX(), e.getY());
        // Rotate
        if (isLeftDown(e)) {
            Coord2d move = mouse.sub(prevMouse).div(100);
            rotate(move);
            for(Chart chart: targets){
                chart.render();
            }
        }
        // Shift
        else if (isRightDown(e)) {
            Coord2d move = mouse.sub(prevMouse);
            if (move.y != 0)
                shift(move.y / 500);
        }
        prevMouse = mouse;
    }

    protected void mouseWheelMoved(MouseEvent e) {
        stopThreadController();

        //float factor = NewtMouseUtilities.convertWheelRotation(e, 1.0f, 10.0f);
        //zoomZ(factor);
    }

    public boolean handleSlaveThread(MouseEvent e) {
        if (isDoubleClick(e)) {
            if (threadController != null) {
                threadController.start();
                return true;
            }
        }
        if (threadController != null)
            threadController.stop();
        return false;
    }

    public static boolean isLeftDown(MouseEvent e) {
        return true;//(e.getModifiers() & InputEvent.BUTTON1_MASK) == InputEvent.BUTTON1_MASK;
    }

    public static boolean isRightDown(MouseEvent e) {
        return false;//(e.getModifiers() & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK;
    }

    public static boolean isDoubleClick(MouseEvent e) {
        //System.out.println(e.getClickCount());
        return (e.getClickCount() > 1);
    }

    public static void console(MouseEvent mouseEvent) {
        System.out.println(JavaFXCameraMouseController.class.getName() + " " + mouseEvent.getX() + ", " + mouseEvent.getY());
    }
}

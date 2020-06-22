package net.haesleinhuepf.spimcat.transform;

import fiji.util.gui.GenericDialogPlus;
import ij.IJ;
import ij.gui.GenericDialog;
import net.haesleinhuepf.AbstractIncubatorPlugin;
import net.haesleinhuepf.IncubatorUtilities;
import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clijx.CLIJx;
import net.haesleinhuepf.spimcat.io.CLIJxVirtualStack;

import java.awt.*;
import java.awt.event.*;

public class RigidTransform3D extends AbstractIncubatorPlugin {

    Scrollbar registrationTranslationXSlider = null;
    Scrollbar registrationTranslationYSlider = null;
    Scrollbar registrationTranslationZSlider = null;

    Scrollbar registrationRotationXSlider = null;
    Scrollbar registrationRotationYSlider = null;
    Scrollbar registrationRotationZSlider = null;


    protected void configure() {
        setSource(IJ.getImage());


        GenericDialog gdp = new GenericDialog("Rigid Transform");
        //gdp.addCheckbox("Do noise and background subtraction (Difference of Gaussian)", formerDoNoiseAndBackgroundRemoval);
        //gdp.addSlider("Sigma 1 (in 0.1 pixel)", 0, 100, formerSigma1);
        //gdp.addSlider("Sigma 2 (in 0.1 pixel)", 0, 100, formerSigma2);
        //gdp.addMessage("View transform");
        //gdp.addSlider("View Translation X (in pixel)", -100, 100, formerViewTranslationX);
        //gdp.addSlider("View Translation Y (in pixel)", -100, 100, formerViewTranslationY);
        //gdp.addSlider("View Translation Z (in pixel)", -100, 100, formerViewTranslationZ);
        //gdp.addSlider("View Rotation X (in degrees)", -180, 180, formerViewRotationX);
        //gdp.addSlider("View Rotation Y (in degrees)", -180, 180, formerViewRotationY);
        //gdp.addSlider("View Rotation Z (in degrees)", -180, 180, formerViewRotationZ);

        gdp.addSlider("Translation X (in pixel)", -100, 100, 0);
        gdp.addSlider("Translation Y (in pixel)", -100, 100, 0);
        gdp.addSlider("Translation Z (in pixel)", -100, 100, 0);
        gdp.addSlider("Rotation X (in degrees)", -180, 180, 0);
        gdp.addSlider("Rotation Y (in degrees)", -180, 180, 0);
        gdp.addSlider("Rotation Z (in degrees)", -180, 180, 0);

        registrationTranslationXSlider = (Scrollbar) gdp.getSliders().get(0);
        registrationTranslationYSlider = (Scrollbar) gdp.getSliders().get(1);
        registrationTranslationZSlider = (Scrollbar) gdp.getSliders().get(2);

        registrationRotationXSlider = (Scrollbar) gdp.getSliders().get(3);
        registrationRotationYSlider = (Scrollbar) gdp.getSliders().get(4);
        registrationRotationZSlider = (Scrollbar) gdp.getSliders().get(5);

        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                refresh();
            }
        };

        KeyAdapter keyAdapter = new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                refresh();
            }
        };

        for (Scrollbar item : new Scrollbar[]{
            registrationTranslationXSlider,
            registrationTranslationYSlider,
            registrationTranslationZSlider,
            registrationRotationXSlider,
            registrationRotationYSlider,
            registrationRotationZSlider
        }) {
            item.addKeyListener(keyAdapter);
            item.addMouseListener(mouseAdapter);
        }

        gdp.setModal(false);
        gdp.setOKLabel("Done");
        gdp.showDialog();

        System.out.println("Dialog shown");


    }

    private String getTransform() {
        return
                        "-center" +
                        " translateX=" + registrationTranslationXSlider.getValue() +
                        " translateY=" + registrationTranslationYSlider.getValue() +
                        " translateZ=" + registrationTranslationZSlider.getValue() +
                        " rotateX=" + registrationRotationXSlider.getValue() +
                        " rotateY=" + registrationRotationYSlider.getValue() +
                        " rotateZ=" + registrationRotationZSlider.getValue() +
                        " center";
    }

    @Override
    protected boolean parametersWereChanged() {
        return former_transform.compareTo(getTransform()) != 0;
    }

    ClearCLBuffer result = null;
    String former_transform = "";
    protected synchronized void refresh()
    {
        String transform = getTransform();

        if (former_transform.compareTo(transform) == 0 && !sourceWasChanged()) {
            System.out.println("Cancel");
            return;
        }

        CLIJx clijx = CLIJx.getInstance();
        ClearCLBuffer pushed = CLIJxVirtualStack.imagePlusToBuffer(my_source);//clijx.pushCurrentZStack(my_source);
        validateSource();

        System.out.println(clijx.reportMemory());

        if (result == null) {
            result = clijx.create(pushed);
        }

        //double registrationTranslationX = registrationTranslationXSlider.getValue();
        //double registrationTranslationY = registrationTranslationYSlider.getValue();
        //double registrationTranslationZ = registrationTranslationZSlider.getValue();

        //double registrationRotationX = registrationRotationXSlider.getValue() * Math.PI / 180.0;
        //double registrationRotationY = registrationRotationYSlider.getValue() * Math.PI / 180.0;
        //double registrationRotationZ = registrationRotationZSlider.getValue() * Math.PI / 180.0;


        former_transform = transform;

        System.out.println(transform.replace("\n", " "));

        clijx.affineTransform3D(pushed, result, transform);

        pushed.close();

        setTarget(CLIJxVirtualStack.bufferToImagePlus(result));
        my_target.setTitle("Rigid transformed " + my_source.getTitle());
    }


    @Override
    protected void refreshView() {
        my_target.setZ(my_source.getZ());
    }

}
